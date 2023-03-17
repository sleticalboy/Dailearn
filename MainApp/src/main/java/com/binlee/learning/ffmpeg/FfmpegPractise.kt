package com.binlee.learning.ffmpeg

import android.Manifest.permission
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder.AudioSource
import android.os.CountDownTimer
import android.os.SystemClock
import android.os.Vibrator
import android.provider.MediaStore.Video.Media
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.bean.ModuleItem
import com.binlee.learning.databinding.ActivityAvPractiseBinding
import com.example.ffmpeg.FfmpegHelper
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.LinkedList
import java.util.Locale

/**
 * Created on 2022/8/3
 *
 * @author binlee
 */
class FfmpegPractise : BaseActivity() {

  // java 中的构造代码块
  init {
    //
  }

  private lateinit var mBind: ActivityAvPractiseBinding
  private val dataSet = arrayListOf(
    ModuleItem("打印媒体 meta 信息", "dump_meta"),
    ModuleItem("音频提取", "extract_audio"),
  )
  private var mAVFormat = AVFormat.A_PCM
  private var mCurrentPath: String? = null

  private var mRecordThread: Thread? = null
  private var mOutput: RandomAccessFile? = null
  private val mRecording = MutableLiveData(false)
  private val mLongPressAction = Runnable {
    Log.d(TAG, "onTouch() action long press")
    startRecordAudio()
  }
  private var mStopAction: (() -> Unit)? = null

  private var mPlayThread: Thread? = null
  private var mPlaying = MutableLiveData(false)
  private var mTrack: AudioTrack? = null

  private var flag: String? = null

  override fun layout(): View {
    mBind = ActivityAvPractiseBinding.inflate(layoutInflater)
    return mBind.root
  }

  @Suppress("ClickableViewAccessibility")
  override fun initView() {
    mBind.recyclerView.adapter = DataAdapter(dataSet)
    Toast.makeText(this, ffmpegVersions, Toast.LENGTH_SHORT).show()

    val timeout = ViewConfiguration.getLongPressTimeout() / 2 * 3
    mBind.btnStartRecord.setOnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        // 长按开始录制
        v.postDelayed(mLongPressAction, timeout.toLong())
      } else if (event.action == MotionEvent.ACTION_MOVE) {
        Log.d(TAG, "onTouch() action move: raw(${event.rawX}, ${event.rawY}) -> (${event.x}, ${event.y})")
      } else if (event.action == MotionEvent.ACTION_UP) {
        Log.d(TAG, "onTouch() action up")
        // 抬起结束录制
        v.removeCallbacks(mLongPressAction)
        mStopAction?.invoke()
      } else if (event.action == MotionEvent.ACTION_CANCEL) {
        Log.d(TAG, "onTouch() action cancel")
      }
      true
    }
    mBind.btnStartPlay.setOnClickListener { playOrPause() }
    mBind.btnScanAudio.setOnClickListener { scanAudioFiles() }
    val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked)
    mBind.lvMediaList.adapter = adapter
    mBind.lvMediaList.setOnItemClickListener { _, _, position, _ ->
      mCurrentPath = "${getExternalFilesDir("audio")}/${adapter.getItem(position)}"
      mBind.lvMediaList.setItemChecked(position, true)
    }
    mBind.lvMediaList.setOnItemLongClickListener { _, _, position, _ ->
      if (File("${getExternalFilesDir("audio")}/${adapter.getItem(position)}").delete()) {
        Toast.makeText(application, "${adapter.getItem(position)} 删除成功!", Toast.LENGTH_SHORT).show()
        adapter.remove(adapter.getItem(position))
      }
      mCurrentPath = null
      true
    }
    mBind.rgAudioFormat.setOnCheckedChangeListener { _, checkedId ->
      mAVFormat = when (checkedId) {
        R.id.rb_wav -> AVFormat.A_WAV
        R.id.rb_aac -> AVFormat.A_AAC
        else -> AVFormat.A_PCM
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun scanAudioFiles() {
    val files = getExternalFilesDir("audio")?.list()?.asList()
    files?.let {
      (mBind.lvMediaList.adapter as ArrayAdapter<String>).clear()
      (mBind.lvMediaList.adapter as ArrayAdapter<String>).addAll(it)
    }
  }

  override fun initData() {
    mRecording.observe(this) { recording ->
      mBind.btnStartRecord.text = if (recording) "正在录制" else "长按录制"
    }
    mPlaying.observe(this) { playing ->
      mBind.btnStartPlay.text = if (playing) "暂停" else "播放"
    }
    scanAudioFiles()
  }

  private fun onClickItem(item: ModuleItem) {
    Log.d(TAG, "item click with: ${item.title}")
    if (item.cls == "record_audio") {
      // startRecordAudio()
    } else {
      flag = item.cls
      // 打开相册选视频
      val intent = Intent(Intent.ACTION_PICK).setType("video/*")
      startActivityForResult(intent, PICK_VIDEO)
    }
  }

  abstract class Timer(interval: Long): CountDownTimer(60 * 60_000L/*1h*/, interval) {

    private var counter = 0

    final override fun onTick(millisUntilFinished: Long) {
      counter = (60 * 60 - millisUntilFinished / 1000).toInt()
      onCount(counter)
    }

    final override fun onFinish() {
      Log.d(TAG, "onFinish() called")
    }

    abstract fun onCount(counter: Int)

    fun count(): Int {
      return counter
    }
  }

  private val mTimer = object : Timer(1000L/*1s*/) {
    override fun onCount(counter: Int) {
      mBind.tvRecordedDuration.text = getString(R.string.text_recorded_duration, counter)
    }
  }

  private fun startRecordAudio() {
    if (ActivityCompat.checkSelfPermission(this, permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(permission.RECORD_AUDIO), 0x44100)
      return
    }
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(50L)
    // 构造 AudioRecord
    val size = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2
    val record = AudioRecord(AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT, size)
    // 开始录音
    record.startRecording()
    // 开启子线程从 audio buffer 中读取数据
    mRecordThread = Thread {
      val buffer = ByteArray(size)
      while (true) {
        val readBytes = record.read(buffer, 0, buffer.size)
        if (readBytes <= 0) {
          updateWavHeader(mOutput!!, mAVFormat)
          mRecording.postValue(false)
          Log.d(TAG, "startRecordAudio() record over!")
          if (mAVFormat != AVFormat.A_AAC) {
            runOnUiThread { onRecordOver() }
          }
          break
        }
        if (mAVFormat == AVFormat.A_AAC) {
          // 如果是 aac 格式，则要通过编码器将 pcm 数据编码成 aac 数据并添加 adts 头后写入文件
          // 把数据封装起来加入队列，另起一个线程从队列取数据送入编码器，等待编码器处理完数据之后再写入文件中
          synchronized(mPcmPackets) {
            mPcmPackets.addFirst(Pair(buffer, readBytes))
            Log.d(TAG, "startRecordAudio() queue size: ${mPcmPackets.size}")
          }
          ensureEncoderThread()
        } else if (mAVFormat == AVFormat.A_PCM || mAVFormat == AVFormat.A_WAV) {
          // 如果是 pcm 或者 wav 格式，可以直接写入文件
          writeBuffer(buffer, readBytes)
        }
      }

      record.release()
      mOutput?.close()
      mOutput = null
    }
    mRecordThread?.start()
    mTimer.start()
    Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show()
    mStopAction = {
      Log.d(TAG, "startRecordAudio() stop record")
      try {
        record.stop()
      } catch (_: IllegalStateException) {
      }
      mTimer.cancel()
    }

    mRecording.value = true
  }

  @Suppress("UNCHECKED_CAST")
  private fun onRecordOver() {
    mBind.tvRecordedDuration.animate()
      .alpha(0f)
      .setDuration(1000L)
      .setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          mBind.tvRecordedDuration.text = ""
          mBind.tvRecordedDuration.alpha = 1f
        }
      })
    mCurrentPath?.let {
      if (mTimer.count() < 3) {
        // 丢弃文件
        if (File(it).delete()) {
          Log.e(TAG, "onRecordOver() too short, discard: $it")
        }
        mCurrentPath = null
      } else {
        (mBind.lvMediaList.adapter as ArrayAdapter<String>).add(it.substring(it.lastIndexOf('/') + 1))
        Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun updateWavHeader(output: RandomAccessFile, format: AVFormat) {
    // 保留原始指针位置
    val pointer = output.filePointer
    if (format == AVFormat.A_WAV) {
      val pcmSize = output.length() - WavHeader.SIZE
      val header = WavHeader(AudioFormat.CHANNEL_IN_MONO, 44100, AudioFormat.ENCODING_PCM_16BIT, pcmSize)
      output.seek(0)
      output.write(header.array())
      Log.d(TAG, "updateWavHeader() $header, last: $pointer, pos: ${output.filePointer}")
    }
    // 恢复到原始指针位置
    output.seek(pointer)
  }

  private fun writeBuffer(buffer: ByteArray, readBytes: Int) {
    if (mOutput == null) {
      // /storage/emulated/0/Android/data/com.binlee.learning/files/audio/
      mCurrentPath = "${getExternalFilesDir("audio")}/${generateName(mAVFormat)}"
      mOutput = RandomAccessFile(mCurrentPath, "rw")
      if (mAVFormat == AVFormat.A_WAV) {
        // 先跳过 44 字节，后面再把真正的文件头写入
        mOutput!!.seek(WavHeader.SIZE.toLong())
      }
      Log.d(TAG, "writeBuffer() create file: $mCurrentPath, pos: ${mOutput!!.filePointer}")
    }
    if (readBytes <= 0) return
    mOutput?.write(buffer, 0, readBytes)
  }

  private var mAacEncoder: MediaCodec? = null
  private val mPcmPackets = LinkedList<Pair<ByteArray, Int>>()
  private var mEncoderThread: Thread? = null

  private fun ensureEncoderThread() {
    if (mEncoderThread != null) return

    mEncoderThread = Thread {
      // 初始化编码器
      initializeEncoder()
      // 开启循环处理
      processPcmPackets()
    }
    // 启动编码线程，开始给编码器送数据
    mEncoderThread?.start()
  }

  private fun initializeEncoder() {
    // 创建 aac 编码器
    mAacEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    // 配置编码器
    val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
    format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 44100 * 1 * AudioFormat.ENCODING_PCM_16BIT)
    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
    mAacEncoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    // 启动编码器
    mAacEncoder?.start()
  }

  private fun processPcmPackets() {
    while (true) {
      // 从队列中取数据并填充到 buffer 中后送入编码器
      readInputBuffer()

      // 从编码器取出 aac 数据，封装成 aac 帧写入文件
      if (writeOutputBuffer()) break

      Log.w(TAG, "processPcmPackets() next loop!!!!!!")
    }
    Log.e(TAG, "processPcmPackets() exit!!")
    runOnUiThread { onRecordOver() }
  }

  private fun readInputBuffer() {
    // 找到可用的输入 buffer 索引
    val index = mAacEncoder!!.dequeueInputBuffer(ENCODER_TIMEOUT)
    if (index < 0) {
      Log.w(TAG, "readInputBuffer() no input buffer($index), queue(${mPcmPackets.size})")
      return
    }

    val buffer = mAacEncoder!!.getInputBuffer(index)
    Log.i(TAG, "readInputBuffer() $index -> $buffer, recording: ${mRecording.value}")

    // 从队列中取出数据并填充到 buffer 中
    val pair: Pair<ByteArray, Int>?
    synchronized(mPcmPackets) {
      if (mPcmPackets.size == 0 && mRecording.value == true) {
        mAacEncoder!!.queueInputBuffer(index, 0, 0, 0, 0)
        return
      }
      pair = if (mPcmPackets.size == 0 && mRecording.value == false) {
        null
      } else {
        Log.d(TAG, "readInputBuffer() take pcm packet >>>>>>>>>>")
        mPcmPackets.removeLast()
      }
    }
    Log.d(TAG, "readInputBuffer() <<<<<<<<<< take pcm packet")
    if (pair == null) {
      Log.e(TAG, "readInputBuffer() pcm packet queue is empty!")
      // 数据送完了
      mAacEncoder!!.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
    } else {
      // TODO: pcm 数据长度会不会超出 buffer 限制？
      Log.d(TAG, "readInputBuffer() buffer size: ${buffer?.capacity()}, pcm size: ${pair.second}")
      buffer?.clear()
      buffer?.put(pair.first, 0, pair.second)
      // 将 buffer 送入解码器
      mAacEncoder!!.queueInputBuffer(index, 0, pair.second, 0, 0)
    }
  }

  private fun writeOutputBuffer() : Boolean {
    // 从解码器取编码好的数据，添加 adts 头组成 aac 帧，写入文件
    val bufferInfo = BufferInfo()
    // 找到可用的输出 buffer
    val index = mAacEncoder!!.dequeueOutputBuffer(bufferInfo, ENCODER_TIMEOUT)

    AVUtil.dumpBufferFlags(bufferInfo.flags)
    // 检测 buffer 信息
    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
      return true
    }
    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
      Log.i(TAG, "writeOutputBuffer() codec config")
      return false
    }

    when (index) {
      MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
        Log.i(TAG, "writeOutputBuffer() output format changed to ${mAacEncoder?.outputFormat}")
      }
      MediaCodec.INFO_TRY_AGAIN_LATER -> {
        Log.e(TAG, "writeOutputBuffer() try again later, sleep 50ms")
        SystemClock.sleep(50L)
      }
      MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
        Log.i(TAG, "writeOutputBuffer() output buffers changed")
      }
      else -> {
        val buffer = mAacEncoder!!.getOutputBuffer(index)
        Log.i(TAG, "writeOutputBuffer() output buffer $index -> $buffer")

        // 一个 aac 帧由 adts 头（7 字节）和 aac 数据包组成
        val aacFrame = ByteArray(7 + bufferInfo.size)
        // 从输出 buffer 中取出 aac 数据
        buffer?.get(aacFrame, 7, bufferInfo.size)
        buffer?.clear()
        // 添加 adts 头
        addAdtsHeader(aacFrame)
        // 写入文件
        writeBuffer(aacFrame, aacFrame.size)
        Log.i(TAG, "writeOutputBuffer() written size: ${aacFrame.size} , raw size: ${bufferInfo.size}")
        // 释放输出 buffer
        mAacEncoder!!.releaseOutputBuffer(index, false)
      }
    }
    return false
  }

  private fun addAdtsHeader(packet: ByteArray) {
    val profile = 2 // AAC LC
    val freqIdx = 4 // 44.1KHz
    val chanCfg = 2 // CPE

    // fill in ADTS data
    packet[0] = 0xFF.toByte()
    packet[1] = 0xF9.toByte()
    packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
    packet[3] = ((chanCfg and 3 shl 6) + (packet.size shr 11)).toByte()
    packet[4] = (packet.size and 0x7FF shr 3).toByte()
    packet[5] = ((packet.size and 7 shl 5) + 0x1F).toByte()
    packet[6] = 0xFC.toByte()
  }

  private fun playOrPause() {
    if (mCurrentPath == null) {
      Toast.makeText(this, "请录制或选择音频文件", Toast.LENGTH_SHORT).show()
      return
    }

    if (mCurrentPath!!.endsWith(AVFormat.A_AAC.suffix)) {
      Toast.makeText(this, "AAC 格式暂不支持！", Toast.LENGTH_SHORT).show()
      return
    }

    if (mTrack != null) {
      if (mTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING) {
        // 不会立即暂停，会在写入数据全部播放完成之后暂停
        // mTrack!!.stop()

        // 立即暂停，不会丢弃已写入数据，下次继续播放
        mTrack!!.pause()
        // 丢弃已写入但未播放的数据
        // mTrack!!.flush()
        mPlaying.value = false
        return
      } else if (mTrack!!.playState == AudioTrack.PLAYSTATE_PAUSED) {
        mTrack!!.play()
        mPlaying.value = true
        return
      }
    }

    mTrack?.let {
      if (it.state == AudioTrack.STATE_INITIALIZED) {
        it.release()
      }
    }

    val size = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2
    mTrack = AudioTrack(
      AudioAttributes.Builder()
        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
        .build(),
      AudioFormat.Builder()
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .setSampleRate(44100)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .build(),
      size, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
    )
    mTrack!!.play()
    mPlayThread = Thread {
      val input = RandomAccessFile(mCurrentPath, "r")
      val buffer = ByteArray(size)
      var first = true
      while (true) {
        if (mPlaying.value != true) {
          SystemClock.sleep(100L)
          continue
        }
        if (first && mCurrentPath!!.endsWith("wav")) {
          input.seek(WavHeader.SIZE.toLong())
          first = false
        }
        val read = input.read(buffer)
        if (read < 0) {
          Log.e(TAG, "playOrPause() play over! read: $read")
          break
        }

        val written = mTrack!!.write(buffer, 0, read)
        if (written < 0) {
          Log.e(TAG, "playOrPause() write audio data failed: $written")
          break
        }
      }
      mTrack!!.stop()
      mTrack!!.release()
      input.close()
      // 子线程更新数据时，需要 post 到主线程
      mPlaying.postValue(false)
    }
    mPlayThread?.start()
    mPlaying.value = true
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 0x44100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      startRecordAudio()
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PICK_VIDEO && resultCode == RESULT_OK) {
      Log.d(TAG, "onActivityResult() video url: ${data?.data}")
      if (data?.data == null) return

      val columns = arrayOf(Media.DATA, Media.WIDTH, Media.HEIGHT)
      //从系统表中查询指定Uri对应的照片
      try {
        contentResolver.query(data.data!!, columns, null, null, null).use { cursor ->
          cursor!!.moveToFirst()
          // 获取媒体绝对路径
          val filepath = cursor.getString(0)
          Log.d(TAG, "onActivityResult() url: $data, path: $filepath")
          if ("dump_meta" == flag) {
            FfmpegHelper.dumpMetaInfo(filepath)
          } else if ("extract_audio" == flag) {
            // 输出文件路径 /storage/emulated/0/Android/data/com.binlee.learning/files/audio/
            val output = File(getExternalFilesDir("audio"), generateName(AVFormat.A_AAC))
            if (output.exists()) output.delete()
            output.createNewFile()

            val res = FfmpegHelper.extractAudio(filepath, output.absolutePath)
            // 提取音频数据之后，打印 meta 信息
            if (res == 0) FfmpegHelper.dumpMetaInfo(output.absolutePath)
            // output.delete()
          }
        }
      } catch (e: Throwable) {
        e.printStackTrace()
      }
    }
  }

  private inner class DataAdapter(private val dataSet: ArrayList<ModuleItem>) :
    RecyclerView.Adapter<ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val itemView = TextView(parent.context)
      itemView.setBackgroundResource(R.drawable.module_item_bg)
      return ItemHolder(itemView)
    }

    override fun getItemCount(): Int {
      return dataSet.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
      val item = dataSet[position]
      holder.textView.text = item.title
      holder.textView.setOnClickListener {
        onClickItem(item)
      }
    }
  }

  private class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView = itemView as TextView

    init {
      textView.gravity = Gravity.CENTER
      textView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
      textView.setPadding(32, 16, 32, 16)
      textView.textSize = 24F
      textView.setTextColor(Color.BLUE)
    }
  }

  companion object {
    private const val TAG = "FfmpegPractise"
    private const val PICK_VIDEO = 0x1001
    private const val ENCODER_TIMEOUT = 250L
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    private val ffmpegVersions: String = FfmpegHelper.getVersions()

    private fun generateName(format: AVFormat): String {
      return "${DATE_FORMAT.format(System.currentTimeMillis())}${format.suffix}"
    }
  }
}
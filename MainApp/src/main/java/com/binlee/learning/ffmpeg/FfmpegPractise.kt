package com.binlee.learning.ffmpeg

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder.AudioSource
import android.os.SystemClock
import android.provider.MediaStore.Video.Media
import android.util.Log
import android.view.Gravity
import android.view.View
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
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

  private var flag: String? = null

  override fun layout(): View {
    mBind = ActivityAvPractiseBinding.inflate(layoutInflater)
    return mBind.root
  }

  override fun initView() {
    mBind.recyclerView.adapter = DataAdapter(dataSet)
    Toast.makeText(this, ffmpegVersions, Toast.LENGTH_SHORT).show()

    mBind.btnStartRecord.setOnClickListener { startRecordAudio() }
    mBind.btnStartPlay.setOnClickListener { playOrPause() }
    mBind.btnScanAudio.setOnClickListener { scanAudioFiles() }
    val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked)
    mBind.lvMediaList.adapter = adapter
    mBind.lvMediaList.setOnItemClickListener { parent, view, position, id ->
      mRecordPath = "${getExternalFilesDir("audio")}/${adapter.getItem(position)}"
      mBind.lvMediaList.setItemChecked(position, true)
    }
    scanAudioFiles()
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
    mPlaying.observe(this) { playing ->
      mBind.btnStartPlay.text = if (playing) "暂停" else "播放"
    }
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

  private var mRecordThread: Thread? = null
  private var mRecordPath: String? = null
  private var mOutput: OutputStream? = null
  private var mTimer: Int = 0
  private val  mRecordTimer = object : Runnable {
    override fun run() {
      mBind.tvRecordedDuration.text = getString(R.string.text_recorded_duration, ++mTimer)
      if (mRecordThread?.isAlive == true) {
        mBind.root.postDelayed(this, 1000L)
      }
    }
  }

  private fun startRecordAudio() {
    if (ActivityCompat.checkSelfPermission(this, permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(permission.RECORD_AUDIO), 0x44100)
      return
    }
    // 构造 AudioRecord
    val size = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
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
          record.release()
          mOutput?.close()
          mOutput = null
          convertToWav()
          Log.d(TAG, "startRecordAudio() record over!")
          runOnUiThread {
            Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show()
            mBind.tvRecordedDuration.text = ""
          }
          break
        }

        // 写入文件
        writeBuffer(buffer, readBytes)
      }
    }
    mRecordThread?.start()
    Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show()

    // 录音时长
    var duration = mBind.etDuration.text.toString()
    if (duration.isEmpty()) duration = "10"
    mBind.root.postDelayed({
      mBind.root.removeCallbacks(mRecordTimer)
      record.stop()
    }, duration.toLong() * 1000)
    mBind.root.postDelayed(mRecordTimer, 1000L)
  }

  private fun convertToWav() {
    val source = FileInputStream(mRecordPath)
    val wavFile = mRecordPath!!.replace("pcm", "wav")
    val sink = FileOutputStream(wavFile)
    val header = WavHeader(AudioFormat.CHANNEL_IN_MONO, 44100, AudioFormat.ENCODING_PCM_16BIT, source.available())
    sink.write(header.array())
    source.copyTo(sink)
    sink.close()
    Log.d(TAG, "convertToWav() file: $wavFile")
  }

  private fun writeBuffer(buffer: ByteArray, readBytes: Int) {
    if (mOutput == null) {
      // /storage/emulated/0/Android/data/com.binlee.learning/files/audio/
      mRecordPath = "${getExternalFilesDir("audio")}/${fullTime()}.pcm"
      mOutput = FileOutputStream(mRecordPath)
      Log.d(TAG, "writeBuffer() create file: $mRecordPath")
    }
    if (readBytes > 0) {
      mOutput?.write(buffer, 0, readBytes)
      mOutput?.flush()
      Log.d(TAG, "writeBuffer() size: $readBytes")
    }
  }

  private var mPlayThread: Thread? = null
  private var mInput: InputStream? = null
  private var mPlaying = MutableLiveData(false)
  private var mTrack: AudioTrack? = null

  private fun playOrPause() {
    if (mRecordPath == null) {
      Toast.makeText(this, "请先录制或扫描音频文件", Toast.LENGTH_SHORT).show()
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

    mTrack?.release()

    val size = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
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
      mInput = FileInputStream(mRecordPath)
      val buffer = ByteArray(size)
      var first = true
      while (true) {
        if (mPlaying.value != true) {
          SystemClock.sleep(100L)
          continue
        }
        if (first && mRecordPath!!.endsWith("wav")) {
          val header = ByteArray(WavHeader.SIZE)
          mInput!!.read(header)
          first = false
          Log.d(TAG, "playOrPause() header: ${header.contentToString()}")
        }
        val read = mInput!!.read(buffer)
        Log.d(TAG, "playOrPause() read: $read")
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
      mInput!!.close()
      mInput = null
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
            val output = File(getExternalFilesDir("audio"), "${fullTime()}.aac")
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
    private val DATE_FORMAT = SimpleDateFormat("yyyy-mm-dd_hh-mm-ss", Locale.US)

    private val ffmpegVersions: String = FfmpegHelper.getVersions()

    private fun fullTime(): String {
      return DATE_FORMAT.format(System.currentTimeMillis())
    }
  }
}
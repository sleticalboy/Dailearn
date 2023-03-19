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
import android.media.AudioTrack
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
import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.AVFormat.A_WAV
import com.binlee.learning.ffmpeg.header.WavHeader
import com.example.ffmpeg.FfmpegHelper
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.thread

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
  private var mAVFormat = A_AAC
  private var mCurrentPath: String? = null
  private var mTimer: Int = 0
  private lateinit var recorder: IRecorder
  private val mLongPressAction = Runnable {
    Log.d(TAG, "onTouch() action long press")
    startRecordAudio()
  }

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
        recorder.stop()
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
        R.id.rb_wav -> A_WAV
        R.id.rb_aac -> A_AAC
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

  private fun startRecordAudio() {
    if (ActivityCompat.checkSelfPermission(this, permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(permission.RECORD_AUDIO), 0x44100)
      return
    }

    recorder = RecorderFactory.create(mAVFormat)
    recorder.setOutputFile(generateName(this, mAVFormat))
    recorder.start(object : IRecorder.Callback {
      override fun onStarted() {
        mBind.btnStartRecord.text = "正在录制"
        // 震动一下
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50L)
      }

      override fun onTimer(timer: Int) {
        mTimer = timer
        mBind.tvRecordedDuration.text = getString(R.string.text_recorded_duration, timer)
      }

      override fun onFinished(path: String) {
        mBind.btnStartRecord.text = "长按录制"
        mCurrentPath = path
        onRecordOver()
      }
    })
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
      if (mTimer < 3) {
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

  private fun playOrPause() {
    if (mCurrentPath == null) {
      Toast.makeText(this, "请录制或选择音频文件", Toast.LENGTH_SHORT).show()
      return
    }

    if (mCurrentPath!!.endsWith(A_AAC.suffix)) {
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
    thread(start = true, name = "PCM-Player") {
      val input = RandomAccessFile(mCurrentPath, "r")
      val buffer = ByteArray(size)
      var first = true
      while (true) {
        if (mPlaying.value != true) {
          SystemClock.sleep(250L)
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
            val output = File(generateName(this, A_AAC))
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
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    private val ffmpegVersions: String = FfmpegHelper.getVersions()

    private fun generateName(context: Context, format: AVFormat): String {
      return "${context.getExternalFilesDir("audio")}/${DATE_FORMAT.format(System.currentTimeMillis())}${format.suffix}"
    }
  }
}
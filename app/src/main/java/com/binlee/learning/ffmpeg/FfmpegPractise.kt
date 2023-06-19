package com.binlee.learning.ffmpeg

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Vibrator
import android.provider.MediaStore.Video.Media
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.bean.ModuleItem
import com.binlee.learning.databinding.ActivityAvPractiseBinding
import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.AVFormat.A_PCM
import com.binlee.learning.ffmpeg.AVFormat.A_WAV
import com.binlee.learning.ffmpeg.AVFormat.NONE
import com.binlee.learning.ffmpeg.AVFormat.fromPath
import com.binlee.learning.ffmpeg.IPlayer.State
import com.binlee.learning.ffmpeg.IPlayer.State.PLAYING
import com.binlee.learning.ffmpeg.IPlayer.State.STOPPED
import com.binlee.learning.ffmpeg.model.FileItem
import com.binlee.learning.ffmpeg.ui.FileListAdapter
import com.example.ffmpeg.FfmpegHelper
import java.io.File
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

  private lateinit var binding: ActivityAvPractiseBinding

  private var mAVFormat = NONE
  private var mCurrentPath: String? = null
  private var mTimer: Int = 0
  private var mRecorder: IRecorder? = null
  private var mPlayer: IPlayer? = null
  private val mLongPress = Runnable {
    Log.d(TAG, "onTouch() action long press")
    startRecordAudio()
  }

  private lateinit var audioAdapter: FileListAdapter
  private lateinit var videoAdapter: FileListAdapter

  private var isAudio = false

  override fun layout(): View {
    binding = ActivityAvPractiseBinding.inflate(layoutInflater)
    return binding.root
  }

  @Suppress("ClickableViewAccessibility")
  override fun initView() {
    Toast.makeText(this, ffmpegVersions, Toast.LENGTH_SHORT).show()

    binding.tvDirName.text = getExternalFilesDir("audio")?.absolutePath
    registerForContextMenu(binding.btnSettings)

    // 判断触点在屏幕左侧还是右侧，给对应的 list 设置选中状态
    val metrics = DisplayMetrics()
    (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
    val half = metrics.widthPixels / 2f
    binding.viewCover.setOnTouchListener { _, event ->
      isAudio = event.rawX < half
      // Log.d(TAG, "onTouch() raw(${event.rawX}, ${event.rawY}) -> (${event.x}, ${event.y})" +
      //     " ${if (isAudio) "audio" else "video"} list")
      binding.tvDirName.text = getExternalFilesDir(if (isAudio) "audio" else "video")?.absolutePath
      false
    }

    val timeout = ViewConfiguration.getLongPressTimeout() / 2 * 3
    binding.btnStartRecord.setOnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        // 长按开始录制
        v.postDelayed(mLongPress, timeout.toLong())
      } else if (event.action == MotionEvent.ACTION_MOVE) {
        Log.d(TAG, "onTouch() action move: raw(${event.rawX}, ${event.rawY}) -> (${event.x}, ${event.y})")
      } else if (event.action == MotionEvent.ACTION_UP) {
        Log.d(TAG, "onTouch() action up")
        // 抬起结束录制
        v.removeCallbacks(mLongPress)
        mRecorder?.stop()
      } else if (event.action == MotionEvent.ACTION_CANCEL) {
        Log.d(TAG, "onTouch() action cancel")
      }
      true
    }
    binding.btnStartPlay.setOnClickListener { playOrPause() }
    binding.btnScanFiles.setOnClickListener { scanFiles(isAudio) }

    audioAdapter = FileListAdapter()
    videoAdapter = FileListAdapter()
    binding.lvAudioList.adapter = audioAdapter
    binding.lvVideoList.adapter = videoAdapter
    binding.lvAudioList.setOnItemClickListener { _, _, position, _ -> onListItemClick(position) }
    binding.lvVideoList.setOnItemClickListener { _, _, position, _ -> onListItemClick(position) }
    // 上下文菜单：删除、提取音频、打印媒体 meta 信息
    registerForContextMenu(binding.lvAudioList)
    registerForContextMenu(binding.lvVideoList)

    binding.rgAudioFormat.setOnCheckedChangeListener { _, checkedId ->
      mAVFormat = when (checkedId) {
        R.id.rb_wav -> A_WAV
        R.id.rb_aac -> A_AAC
        else -> A_PCM
      }
      binding.rgCodecOptions.visibility = if (mAVFormat == A_AAC) View.VISIBLE else View.GONE
    }
    binding.rgCodecOptions.setOnCheckedChangeListener { _, checkedId ->
      if (checkedId == R.id.rb_media_codec) {
        binding.cbAsync.visibility = View.VISIBLE
      } else {
        binding.cbAsync.visibility = View.GONE
      }
    }

    // 默认选中 aac
    binding.rbAac.performClick()
    // mBind.rgAudioFormat.check(R.id.rb_aac)
    // mBind.root.postDelayed({ mBind.rbAac.performClick() }, 500L)
    // mBind.root.postDelayed({ mBind.rgAudioFormat.check(R.id.rb_aac) }, 500L)
    // 默认选中硬件解码
    binding.rbMediaCodec.performClick()
  }

  private fun onListItemClick(position: Int) {
    val adapter = if (isAudio) audioAdapter else videoAdapter
    adapter.setChecked(position)
    mCurrentPath = adapter.getPath(position)
    mAVFormat = fromPath(mCurrentPath)
    when (mAVFormat) {
      A_WAV -> {
        binding.rbWav.isChecked = true
      }
      A_AAC -> {
        binding.rbAac.isChecked = true
      }
      A_PCM -> {
        binding.rbPcm.isChecked = true
      }
      else -> {}
    }
  }

  // 这里的 menuInfo 是 AdapterContextMenuInfo，包含当前点击的位置
  override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
    Log.d(TAG, "onCreateContextMenu() v = $v, menuInfo = $menuInfo")
    // 上下文菜单：删除、提取音频、打印媒体 meta 信息
    if (v is AbsListView && menuInfo is AdapterView.AdapterContextMenuInfo) {
      v.performItemClick(v.getChildAt(menuInfo.position), menuInfo.position, menuInfo.id)

      menu?.setHeaderTitle("请选择：")
      // menuInflater.inflate(R.menu.av_operation_list, menu)
      menu?.add(0, R.id.delete, 0, "删除")
      menu?.add(0, R.id.extract_audio, 1, "提取音频")?.isVisible = !isAudio
      menu?.add(0, R.id.dump_meta_info, 2, "打印 meta 信息")
    }
  }

  override fun onContextItemSelected(item: MenuItem): Boolean {
    Log.d(TAG, "onContextItemSelected() menu info: ${item.menuInfo}")
    if (item.menuInfo is AdapterView.AdapterContextMenuInfo && mCurrentPath != null) {
      if (item.itemId == R.id.delete) {
        mCurrentPath?.let {
          val file = FileItem(File(it), false)
          if (file.delete()) {
            Toast.makeText(application, "$file 删除成功!", Toast.LENGTH_SHORT).show()
            (if (isAudio) audioAdapter else videoAdapter).remove(file)
          }
        }

      } else if (item.itemId == R.id.extract_audio) {
        mCurrentPath?.let {
          val output = generateName(this, A_AAC)
          val res = extractAudio(it, output)
          // 提取音频数据之后，打印 meta 信息
          if (res == 0) FfmpegHelper.dumpMetaInfo(output)
        }
      } else if (item.itemId == R.id.dump_meta_info) {
        mCurrentPath?.let { dumpMetaInfo(it) }
      }
    }
    return super.onContextItemSelected(item)
  }

  override fun initData() {
    scanFiles(true)
    scanFiles(false)
  }

  @Suppress("UNCHECKED_CAST")
  private fun scanFiles(isAudio: Boolean) {
    val files = getExternalFilesDir(if (isAudio) "audio" else "video")?.listFiles()?.asList()
    Log.d(TAG, "scanFiles() isAudio = $isAudio, $files")
    files?.let {
      (if (isAudio) audioAdapter else videoAdapter).replaceAll(it)
    }
  }

  private fun onClickItem(item: ModuleItem) {
    Log.d(TAG, "item click with: ${item.title}")
    // 打开相册选视频
    val intent = Intent(Intent.ACTION_PICK).setType("video/*")
    startActivityForResult(intent, P_PICK_VIDEO)
  }

  private fun startRecordAudio() {
    if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
      askPermission(arrayOf(Manifest.permission.RECORD_AUDIO))
      return
    }

    mRecorder = RecorderFactory.create(mAVFormat, binding.cbAsync.isChecked)
    mRecorder!!.setOutputFile(generateName(this, mAVFormat))
    mRecorder!!.start(object : IRecorder.Callback {
      override fun onStarted() {
        binding.btnStartRecord.text = "正在录制"
        // 震动一下
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50L)
      }

      override fun onTimer(timer: Int) {
        mTimer = timer
        binding.tvRecordedDuration.text = getString(R.string.text_recorded_duration, timer)
      }

      override fun onFinished(path: String) {
        binding.btnStartRecord.text = "长按录制"
        mCurrentPath = path
        onRecordOver()
      }
    })
  }

  @Suppress("UNCHECKED_CAST")
  private fun onRecordOver() {
    binding.tvRecordedDuration.animate()
      .alpha(0f)
      .setDuration(1000L)
      .setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          binding.tvRecordedDuration.text = ""
          binding.tvRecordedDuration.alpha = 1f
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
        audioAdapter.add(FileItem(File(it), false))
        Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun playOrPause() {
    if (mCurrentPath == null) {
      Toast.makeText(this, "请录制或选择音频文件", Toast.LENGTH_SHORT).show()
      return
    }

    if (mAVFormat == NONE) {
      Toast.makeText(this, "Unsupported format: $mAVFormat", Toast.LENGTH_SHORT).show()
      return
    }

    Log.d(TAG, "playOrPause() $mPlayer -> $mCurrentPath")

    // pause -> play(resume)
    if (mPlayer?.isPlaying() == true) {
      mPlayer?.pause()
      return
    }

    // play(resume)
    if (mPlayer?.isPaused() == true) {
      mPlayer?.resume()
      return
    }

    mPlayer = PlayerFactory.create(mAVFormat, binding.cbAsync.isChecked)
    Log.d(TAG, "playOrPause() new player: $mPlayer")
    mPlayer!!.setInputFile(mCurrentPath!!)
    mPlayer!!.start(object : IPlayer.Callback {
      override fun onState(state: State) {
        Log.d(TAG, "onState() state = $state")
        binding.btnStartPlay.text = if (state == PLAYING) "暂停" else "播放"
        if (state == STOPPED) mPlayer = null
      }
    })
  }

  override fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
    if (grantResults[0]) startRecordAudio()
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == P_PICK_VIDEO && resultCode == RESULT_OK) {
      Log.d(TAG, "onActivityResult() video url: ${data?.data}")
      if (data?.data == null) return

      if ("dump_meta" == "flag") {
        dumpMetaInfo(queryPath(this, data.data!!))
      } else if ("extract_audio" == "flag") {
        // 输出文件路径 /storage/emulated/0/Android/data/com.binlee.learning/files/audio/
        val output = generateName(this, A_AAC)

        with(File(output)) { if (exists()) delete() }

        val res = extractAudio(queryPath(this, data.data!!), output)
        // 提取音频数据之后，打印 meta 信息
        if (res == 0) FfmpegHelper.dumpMetaInfo(output)
      }
    }
  }

  private fun dumpMetaInfo(path: String) {
    FfmpegHelper.dumpMetaInfo(path)
  }

  private fun extractAudio(input: String, output: String): Int {
    return FfmpegHelper.extractAudio(input, output)
  }

  companion object {
    private const val TAG = "FfmpegPractise"
    private const val P_PICK_VIDEO = 0x1001
    private const val P_RECORD_AUDIO = 0x44100
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    private val ffmpegVersions: String = FfmpegHelper.getVersions()

    private fun generateName(context: Context, format: AVFormat): String {
      val type = if (format.isAudio) "audio" else "video"
      return "${context.getExternalFilesDir(type)}/${DATE_FORMAT.format(System.currentTimeMillis())}${format.suffix}"
    }

    private fun queryPath(context: Context, uri: Uri): String {
      val columns = arrayOf(Media.DATA, Media.WIDTH, Media.HEIGHT)
      //从系统表中查询指定Uri对应的照片
      context.contentResolver.query(uri, columns, null, null, null).use { cursor ->
        cursor!!.moveToFirst()
        // 获取媒体绝对路径
        val path = cursor.getString(0)
        Log.d(TAG, "onActivityResult() url: $uri, path: $path")
        cursor.close()
        return path
      }
    }
  }
}
package com.binlee.learning.ffmpeg.player

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.binlee.learning.ffmpeg.AVFormat
import com.binlee.learning.ffmpeg.AVFormat.A_PCM
import com.binlee.learning.ffmpeg.IPlayer
import com.binlee.learning.ffmpeg.IPlayer.Callback
import com.binlee.learning.ffmpeg.IPlayer.State
import com.binlee.learning.ffmpeg.IPlayer.State.PAUSED
import com.binlee.learning.ffmpeg.IPlayer.State.PLAYING
import com.binlee.learning.ffmpeg.IPlayer.State.STOPPED
import com.binlee.learning.ffmpeg.Packet
import java.io.RandomAccessFile
import kotlin.concurrent.thread

/**
 * Created on 2023/3/20
 *
 * @author binlee
 */
open class BasePlayer(protected val format: AVFormat = A_PCM): IPlayer {

  protected val TAG = javaClass.simpleName

  private var track: AudioTrack? = null

  protected var input: RandomAccessFile? = null
  protected val buffer = ByteArray(1024)

  private val innerCallback = object: Callback {
    private val handler = Handler(Looper.getMainLooper())
    override fun onState(state: State) {
      handler.post { userCallback?.onState(state) }
    }
  }
  private var userCallback: Callback? = null

  override fun setInputFile(path: String) {
    this.input = RandomAccessFile(path, "r")
  }

  override fun start(callback: Callback) {
    if (track != null) return

    userCallback = callback

    val size = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2

    // mTrack = AudioTrack(
    //   AudioAttributes.Builder()
    //     .setLegacyStreamType(AudioManager.STREAM_MUSIC)
    //     .build(),
    //   AudioFormat.Builder()
    //     .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
    //     .setSampleRate(44100)
    //     .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
    //     .build(),
    //   size, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
    // )
    track = AudioTrack(
      AudioManager.STREAM_MUSIC,
      44100,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
      size,
      AudioTrack.MODE_STREAM
    )
    track!!.play()
    innerCallback.onState(PLAYING)

    onFileOpened()

    // player thread
    thread(start = true, name = "$TAG-thread") {
      Log.e(TAG, "player thread enter!")
      while (true) {
        if (!isPlaying()) {
          SystemClock.sleep(250L)
          continue
        }

        val packet = dequeuePacket()
        // Log.i(TAG, "player thread: packet size: ${packet?.size}")
        if (packet == null || packet.size < 0) {
          Log.e(TAG, "player thread: EOF!")
          input?.close()
          break
        }
        val written = track!!.write(packet.buffer, 0, packet.size)
        if (written < 0) {
          Log.e(TAG, "player thread: write audio data failed: $written")
          break
        }
      }
      Log.e(TAG, "player thread exit!")
      track!!.release()
      track = null
      innerCallback.onState(STOPPED)
    }
  }

  override fun pause() {
    // pause -> play(resume)
    if (isPlaying()) {
      // 不会立即暂停，会在写入数据全部播放完成之后暂停
      // mTrack!!.stop()

      // 立即暂停，不会丢弃已写入数据，下次继续播放
      track!!.pause()
      // 丢弃已写入但未播放的数据
      // mTrack!!.flush()
      innerCallback.onState(PAUSED)
      return
    }
  }

  override fun resume() {
    if (isPaused()) {
      track!!.play()
      innerCallback.onState(PLAYING)
      return
    }
  }

  override fun isPlaying(): Boolean {
    return track?.playState == AudioTrack.PLAYSTATE_PLAYING
  }

  override fun isPaused(): Boolean {
    return track?.playState == AudioTrack.PLAYSTATE_PAUSED
  }

  protected open fun onFileOpened() {
  }

  protected open fun enqueuePacket(packet: Packet) {
  }

  protected open fun dequeuePacket(): Packet? {
    val readBytes = input!!.read(buffer)
    return Packet(buffer, readBytes)
  }
}
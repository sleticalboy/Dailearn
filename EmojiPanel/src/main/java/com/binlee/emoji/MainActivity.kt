package com.binlee.emoji

import android.content.Intent
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.binlee.emoji.concurrent.DeadLock
import com.binlee.emoji.ui.fragment.GenericFragment
import com.binlee.emoji.ui.fragment.MainActivityFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val mHandler = ClientHandler(Looper.myLooper())
    private val mClientMessenger = Messenger(mHandler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        show(GenericFragment::class.java.name)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return if (showFragment(item.itemId)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun showFragment(itemId: Int): Boolean {
        return when (itemId) {
            R.id.emoji_panel -> {
                return true
            }
            R.id.http_service -> {
                show(MainActivityFragment::class.java.name)
                return true
            }
            R.id.remote_upgrade -> {
                requestUpgrade()
                return true
            }
            R.id.generic -> {
                show(GenericFragment::class.java.name)
                return true
            }
            R.id.concurrent -> {
                DeadLock.run()
                return true
            }
            else -> false
        }
    }

    private fun show(clazz: String) {
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag(clazz)
        Log.d(TAG, "show() find $fragment for class: $clazz")
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, clazz)
        }
        if (!fragment.isAdded) {
            transaction.add(R.id.flContainer, fragment, clazz)
        }
        supportFragmentManager.fragments.forEach {
            Log.i(TAG, "before show() $it visible: ${it.isVisible}")
            if (it != fragment && it.isVisible) transaction.hide(it)
        }
        transaction.show(fragment)
        // commit 是在 Handler 里执行的，因此不能马上得到 Fragment 是否显示的状态
        transaction.commitAllowingStateLoss()
        // 通过 Handler#post() 可以准确得到状态
        mHandler.post {
            supportFragmentManager.fragments.forEach {
                Log.i(TAG, "after show() $it visible: ${it.isVisible}")
            }
        }
    }

    private fun requestUpgrade() {
        val service = Intent()
        // Service 不支持隐式 Intent 启动, 需要设置具体的包名和类名才可以
        service.action = "com.sleticalboy.action.REMOTE_UPGRADE"
        // com.binlee.learning.components.service.UpgradeService
        service.setClassName("com.binlee.learning", "com.binlee.learning.components.service.UpgradeService")
        service.putExtra("_mac", "fake mac address")
        service.putExtra("_file_url", "fake file url")
        service.putExtra("_messenger", mClientMessenger)
        val component = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
        Log.d(TAG, "requestUpgrade() $component")
    }

    private fun postRemote(replyTo: Messenger?, what: Int, obj: String) {
        val msg = Message.obtain()
        msg.what = what
        msg.replyTo = mClientMessenger
        // 不能使用 msg.obj 传递数据，否则："Can't marshal non-Parcelable objects across processes."
        msg.data.putString("msg_obj", obj)
        try {
            replyTo?.send(msg)
        } catch (e: RemoteException) {
            Log.d(TAG, "post() error", e)
        }
    }

    private inner class ClientHandler(looper: Looper? = Looper.getMainLooper()) : Handler() {

        override fun handleMessage(msg: Message) {
            Log.d(TAG, "handleMessage() called with: what = ${msg.what}")
            if (msg.what == 1) {
                Log.d(TAG, "handleMessage() obj: " + msg.data.getString("msg_obj"))
                postRemote(msg.replyTo, 2, "client receive server")
            }
        }
    }
}

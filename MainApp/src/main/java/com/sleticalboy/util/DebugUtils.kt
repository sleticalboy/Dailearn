package com.sleticalboy.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import java.lang.reflect.Method
import java.util.*

/**
 * Created on 20-3-27.
 *
 * @author binlee sleticalboy@gmail.com
 */
object DebugUtils {
    private const val TAG = "DebugUtils"

    /* copy from IBinder */
    private const val SYSPROPS_TRANSACTION = '_'.toInt() shl 24 or ('S'.toInt() shl 16) or ('P'.toInt() shl 8) or 'R'.toInt()
    private const val SERVICE_MANAGER = "android.os.ServiceManager"
    private const val SYSTEM_PROP = "android.os.SystemProperties"
    private var sServiceMgr: Class<*>? = null
    private var sSystemProp: Class<*>? = null
    private var sListServices: Method? = null
    private var sCheckService: Method? = null
    private var sSet: Method? = null
    private var sIsWorking = false

    fun openSettings(context: Context) {
        val intent = Intent("android.settings.SETTINGS")
        val pkg = "com.android.settings"
        intent.component = ComponentName(pkg, "$pkg.Settings")
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun debugLayout(open: Boolean) {
        // show layout bound
        // final boolean isEnabled = SystemProperties.getBoolean("debug.layout", false/*default*/);
        if (sIsWorking) return
        sIsWorking = true
        try {
            ensure()
            sSet!!.invoke(null, "debug.layout", if (open) "true" else "false")
        } catch (e: Throwable) {
            Log.e(TAG, "debugLayout set properties error", e)
        }
        PokerTask().execute()
    }

    @Throws(Throwable::class)
    private fun ensure() {
        // android.os.SystemProperties
        if (sServiceMgr == null) {
            sServiceMgr = Class.forName(SERVICE_MANAGER)
        }
        if (sListServices == null) {
            sListServices = sServiceMgr!!.getDeclaredMethod("listServices")
        }
        if (sCheckService == null) {
            sCheckService = sServiceMgr!!.getDeclaredMethod("checkService", String::class.java)
        }
        // android.os.SystemProperties
        if (sSystemProp == null) {
            sSystemProp = Class.forName(SYSTEM_PROP)
        }
        if (sSet == null) {
            sSet = sSystemProp!!.getDeclaredMethod("set", String::class.java, String::class.java)
        }
    }

    class PokerTask : AsyncTask<Void?, Void?, Void?>() {

        @Throws(Throwable::class)
        fun listServices(): Array<String?> {
            return sListServices!!.invoke(null) as Array<String?>
        }

        @Throws(Throwable::class)
        fun checkService(service: String?): IBinder {
            return sCheckService!!.invoke(null, service) as IBinder
        }

        override fun doInBackground(vararg params: Void?): Void? {
            var services = arrayOfNulls<String>(0)
            try {
                services = listServices()
            } catch (e: Throwable) {
                Log.e(TAG, "List all services error", e)
            }
            val failedServices: MutableList<String?> = ArrayList()
            for (service in services) {
                val data = Parcel.obtain()
                try {
                    checkService(service).transact( /*IBinder.*/SYSPROPS_TRANSACTION, data,
                            null, 0)
                } catch (e: Throwable) {
                    // Log.i(TAG, "Someone wrote a bad service '" + service
                    //         + "' that doesn't like to be poked", e);
                    failedServices.add(service)
                } finally {
                    data.recycle()
                }
            }
            Log.w(TAG, "All failed Services:$failedServices")
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            sIsWorking = false
        }
    }
}
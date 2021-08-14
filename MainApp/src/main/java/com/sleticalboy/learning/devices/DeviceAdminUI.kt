package com.sleticalboy.learning.devices

import android.os.Bundle
import android.view.View
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityDevicesBinding

/**
 * Created on 20-2-17.
 *
 * @author sleticalboy
 * @description
 */
class DeviceAdminUI : BaseActivity() {

    private var mBind: ActivityDevicesBinding? = null
    private var mManager: DevicesManager? = null

    override fun prepareWork(savedInstanceState: Bundle?) {
        mManager = DevicesManager(this)
    }

    override fun layout(): View {
        // R.layout.activity_devices
        mBind = ActivityDevicesBinding.inflate(layoutInflater)
        return mBind!!.root
    }

    override fun initView() {
        // activate and deactivate device admin
        mBind!!.activeDeviceAdmin.setOnClickListener { mManager!!.startActivate() }
        mBind!!.stopDeviceAdmin.setOnClickListener { mManager!!.startDeactivate() }
        // screen lock
        mBind!!.wayOfLock.setOnClickListener { mManager!!.setWayOfLock() }
        mBind!!.lockNow.setOnClickListener { mManager!!.lockScreenNow() }
        mBind!!.lockDelay.setOnClickListener { mManager!!.lockScreenDelay(6000L) }
        // reset device
        mBind!!.resetDevice.setOnClickListener { mManager!!.resetDevice() }
        // forbid using camera
        mBind!!.forbidCamera.setOnClickListener { mManager!!.forbidCamera() }
        // reset password
        mBind!!.resetPassword.setOnClickListener { mManager!!.resetPassword() }
        // encrypt storage
        mBind!!.encryptStorage.setOnClickListener { mManager!!.encryptStorage() }
    }
}
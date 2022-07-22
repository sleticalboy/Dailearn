package com.sleticalboy.learning.devices

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.sleticalboy.learning.R

/**
 * Created on 20-2-17.
 *
 * @author sleticalboy
 */
class DevicesManager(private val mContext: Context) {

  private val mMgr: DevicePolicyManager
  private val mComponent: ComponentName

  private fun isActive(): Boolean {
    return mMgr.isAdminActive(mComponent)
  }

  fun disableAdmin() {
    mMgr.removeActiveAdmin(mComponent)
  }

  fun startActivate() {
    if (isActive()) {
      Toast.makeText(mContext, R.string.activated, Toast.LENGTH_SHORT).show()
      return
    }
    // 会直接跳转到当前应用的 激活/关闭 界面
    // this will go directly to the activate/de-activate screen of the app you choose:
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponent)
    intent.putExtra(
      DevicePolicyManager.EXTRA_ADD_EXPLANATION,
      mContext.getString(R.string.settings_device_admin_desc)
    )
    mContext.startActivity(intent)
  }

  fun startDeactivate() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.not_activated, Toast.LENGTH_SHORT).show()
      return
    }
    // 会跳转到设备管理器列表
    // this will go to the list of admin apps
    val intent = Intent()
    val pkg = "com.android.settings"
    intent.component = ComponentName(pkg, "$pkg.DeviceAdminSettings")
    mContext.startActivity(intent)
  }

  fun setWayOfLock() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
    mMgr.setPasswordQuality(mComponent, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED)
    mContext.startActivity(intent)
  }

  fun lockScreenNow() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    mMgr.lockNow()
  }

  fun lockScreenDelay(delay: Long) {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    mMgr.setMaximumTimeToLock(mComponent, delay)
  }

  fun resetDevice() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    // mMgr.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
  }

  fun forbidCamera() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    val disabled = mMgr.getCameraDisabled(mComponent)
    Log.d("DevicesManager", "old state: $disabled")
    val res = if (disabled) R.string.enable_camera else R.string.disable_camera
    try {
      mMgr.setCameraDisabled(mComponent, !disabled)
      Toast.makeText(mContext, res, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Log.d("DevicesManager", "forbidCamera() error.$e")
    }
  }

  fun resetPassword() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    // 只能对未设置密码的设备重置密码, 若设备已有密码，则会抛出
    // java.lang.SecurityException: Admin cannot change current password
    try {
      val ret = mMgr.resetPassword(
        "2580",
        DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY
      )
      Log.d("DevicesManager", "ret:$ret")
    } catch (e: Exception) {
      Log.w("DevicesManager", "reset password error", e)
    }
  }

  fun encryptStorage() {
    if (!isActive()) {
      Toast.makeText(mContext, R.string.active_device_admin, Toast.LENGTH_SHORT).show()
      return
    }
    // final int result = mMgr.setStorageEncryption(mComponent, true);
    // Log.d("DevicesManager", "result:" + result);
  }

  init {
    mMgr = mContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    mComponent = ComponentName(mContext.packageName, DevicesReceiver::class.java.name)
  }
}
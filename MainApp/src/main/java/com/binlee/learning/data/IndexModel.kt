package com.binlee.learning.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.binlee.learning.anims.JsonAnimUI
import com.binlee.learning.anims.TransitionUI
import com.binlee.learning.bean.ModuleItem
import com.binlee.learning.bt.BluetoothUI
import com.binlee.learning.camera.v1.LiveCameraActivity
import com.binlee.learning.components.ProviderPractise
import com.binlee.learning.components.ServicePractise
import com.binlee.learning.csv.AlphaActivity
import com.binlee.learning.csv.AutoSwitchUI
import com.binlee.learning.csv.CustomViewActivity
import com.binlee.learning.csv.HeaderActivity
import com.binlee.learning.dev.DebugUI
import com.binlee.learning.devices.DeviceAdminUI
import com.binlee.learning.dialogs.DialogsUI
import com.binlee.learning.others.AlarmActivity
import com.binlee.learning.others.ImageConvertUI
import com.binlee.learning.others.NotificationsUI
import com.binlee.learning.rv.ClassifyActivity
import com.binlee.learning.rv.DecorationActivity
import com.binlee.learning.rv.NestedRvActivity
import com.binlee.learning.rv.PagerActivity
import com.binlee.learning.rv.WheelRVActivity

/**
 * Created on 20-3-30.
 *
 * @author binlee sleticalboy@gmail.com
 */
class IndexModel internal constructor() {

  private val mModuleSource: MutableLiveData<Result<List<ModuleItem>>> = MutableLiveData()

  fun getModules(): LiveData<Result<List<ModuleItem>>> {
    mModuleSource.value = getTasks()
    return mModuleSource
  }

  private fun getTasks(): Result<List<ModuleItem>> {
    val array = arrayOf(
      // debug tools
      ModuleItem("调试工具", DebugUI::class.java),
      // bluetooth
      ModuleItem("蓝牙模块", BluetoothUI::class.java),
      // device admin
      ModuleItem("设备管理", DeviceAdminUI::class.java),
      // components
      ModuleItem("Service", ServicePractise::class.java),
      ModuleItem("ContentProvider", ProviderPractise::class.java),
      // animations
      ModuleItem("转场动画", TransitionUI::class.java),
      ModuleItem("lottie 动画", JsonAnimUI::class.java),
      // custom View & // special effects of View
      ModuleItem("自动切换View使用", AutoSwitchUI::class.java),
      ModuleItem("自定义 View", CustomViewActivity::class.java),
      ModuleItem("头部悬停效果", HeaderActivity::class.java),
      ModuleItem("控件透明度", AlphaActivity::class.java),
      // camera
      ModuleItem("TextureView 实现实时预览", LiveCameraActivity::class.java),
      // Bitmap
      ModuleItem("图片和字符串相互转换", ImageConvertUI::class.java),
      // Tasks
      ModuleItem("定时任务", AlarmActivity::class.java),
      // RecyclerView
      ModuleItem("RecyclerView 分类别显示", ClassifyActivity::class.java),
      ModuleItem("RecyclerView 分页", PagerActivity::class.java),
      ModuleItem("RecyclerView 拖拽排序", DecorationActivity::class.java),
      ModuleItem("RecyclerView 轮播", WheelRVActivity::class.java),
      ModuleItem("RecyclerView 横竖嵌套", NestedRvActivity::class.java),
      // notifications
      ModuleItem("通知呼吸灯闪烁", NotificationsUI::class.java),
      // dialogs
      ModuleItem("dialogs", DialogsUI::class.java),
    )
    return Result.Success(array.toList())
  }

}
package com.sleticalboy.learning.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sleticalboy.learning.anims.JsonAnimUI
import com.sleticalboy.learning.anims.TransitionUI
import com.sleticalboy.learning.bean.ModuleItem
import com.sleticalboy.learning.bt.BluetoothUI
import com.sleticalboy.learning.camera.v1.LiveCameraActivity
import com.sleticalboy.learning.components.ProviderPractise
import com.sleticalboy.learning.components.ServicePractise
import com.sleticalboy.learning.csv.AlphaActivity
import com.sleticalboy.learning.csv.AutoSwitchUI
import com.sleticalboy.learning.csv.CustomViewActivity
import com.sleticalboy.learning.csv.HeaderActivity
import com.sleticalboy.learning.debug.DebugUI
import com.sleticalboy.learning.devices.DeviceAdminUI
import com.sleticalboy.learning.others.AlarmActivity
import com.sleticalboy.learning.dialogs.DialogsUI
import com.sleticalboy.learning.others.ImageConvertUI
import com.sleticalboy.learning.others.NotificationsUI
import com.sleticalboy.learning.rv.*

/**
 * Created on 20-3-30.
 *
 * @author binlee sleticalboy@gmail.com
 */
class IndexModel internal constructor() {

    private val mModuleSource: MutableLiveData<Result<List<ModuleItem>>> = MutableLiveData()

    fun getModuleSource(): LiveData<Result<List<ModuleItem>>> {
        mModuleSource.value = getTasks()
        return mModuleSource
    }

    private fun getTasks(): Result<List<ModuleItem>> {
        val array = arrayOf( // debug tools
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
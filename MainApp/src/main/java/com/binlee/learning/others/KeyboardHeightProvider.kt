package com.binlee.learning.others

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import com.binlee.util.UiUtils.getStatusBarHeight

/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed.
 */
class KeyboardHeightProvider private constructor(
  private val window: Window
) : PopupWindow(
  window.context
), OnGlobalLayoutListener, Application.ActivityLifecycleCallbacks {
  /**
   * The keyboard height observer
   */
  private var mObserver: HeightObserver? = null

  /**
   * The view that is used to calculate the keyboard height
   */
  private val popupView: View = View(window.context)

  /**
   * The parent view
   */
  private val parentView: View
  private var mNegativeHiddenHeight = 0
  private var mKeyboardHeight = 0

  override fun onGlobalLayout() {
    handleOnGlobalLayout()
  }

  /**
   * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
   * PopupWindows are not allowed to be registered before the onResume has finished
   * of the Activity.
   */
  private fun observe(observer: HeightObserver?) {
    mObserver = observer
    setBackgroundDrawable(ColorDrawable(0))
    parentView.post {
      // 此方法必须放在 Activity#onResume() 之后执行，用 View#post() 可以保证
      if (!isShowing && parentView.windowToken != null) {
        showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
      }
    }
  }

  /**
   * Close the keyboard height provider,
   * this provider will not be used anymore.
   */
  private fun dispose() {
    mObserver = null
    popupView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    val app = window.context.applicationContext as Application
    app.unregisterActivityLifecycleCallbacks(this)
    dismiss()
  }

  /**
   * Get the screen orientation
   *
   * @return the screen orientation
   */
  private val screenOrientation: Int
    get() = window.context.resources.configuration.orientation

  /**
   * Popup window itself is as big as the window of the Activity.
   * The keyboard can then be calculated by extracting the popup view bottom
   * from the activity window height.
   */
  private fun handleOnGlobalLayout() {
    val screenSize = Point()
    (window.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
      .defaultDisplay.getSize(screenSize)
    val rect = Rect()
    popupView.getWindowVisibleDisplayFrame(rect)
    val stateHeight = getStatusBarHeight()

    // REMIND, you may like to change this using the fullscreen size of the phone
    // and also using the status bar and navigation bar heights of the phone to calculate
    // the keyboard height. But this worked fine on a Nexus.
    val orientation = screenOrientation
    var keyboardHeight = screenSize.y - rect.bottom - stateHeight
    val negative = keyboardHeight < 0
    // 解决高度为负数的情况
    if (negative) {
      mNegativeHiddenHeight = -keyboardHeight
      keyboardHeight = 0
    }
    if (keyboardHeight != 0) {
      keyboardHeight += mNegativeHiddenHeight
    }
    if (mKeyboardHeight != keyboardHeight) {
      mKeyboardHeight = keyboardHeight
      notifyKeyboardHeightChanged(keyboardHeight, orientation)
    }
  }

  private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
    mObserver?.onHeightChanged(height, orientation)
  }

  init {
    popupView.layoutParams = ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
    contentView = popupView
    softInputMode = (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    inputMethodMode = INPUT_METHOD_NEEDED
    parentView = window.findViewById(android.R.id.content)
    // 设置 PopupWindow 宽高
    width = 0
    height = WindowManager.LayoutParams.MATCH_PARENT
    popupView.viewTreeObserver.addOnGlobalLayoutListener(this)
    val app = window.context.applicationContext as Application
    app.registerActivityLifecycleCallbacks(this)
  }

  companion object {

    @JvmStatic
    fun inject(activity: Activity, observer: HeightObserver?) {
      inject(activity.window, observer)
    }

    @JvmStatic
    fun inject(dialog: Dialog, observer: HeightObserver?) {
      inject(requireNotNull(dialog.window), observer)
    }

    @JvmStatic
    fun inject(window: Window, observer: HeightObserver?) {
      KeyboardHeightProvider(window).observe(observer)
    }
  }

  /**
   * The observer that will be notified when the height of
   * the keyboard has changed
   */
  interface HeightObserver {
    /**
     * Called when the keyboard height has changed, 0 means keyboard is closed,
     * >= 1 means keyboard is opened.
     *
     * @param height      The height of the keyboard in pixels
     * @param orientation The orientation either: Configuration.ORIENTATION_PORTRAIT or
     * Configuration.ORIENTATION_LANDSCAPE
     */
    fun onHeightChanged(height: Int, orientation: Int)
  }

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
  }

  override fun onActivityPaused(activity: Activity) {
  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
  }

  override fun onActivityDestroyed(activity: Activity) {
    if (activity.window == window) dispose()
  }
}
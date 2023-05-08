package com.quvideo.mobile.component.{pkg-name};

import android.content.Context;
import com.quvideo.mobile.component.common.AIConstants;
import com.quvideo.mobile.component.common.AINoInitException;
import com.quvideo.mobile.component.common.IModelApi;
import com.quvideo.mobile.component.common._QAIBaseManager;
import com.quvideo.mobile.component.common._QModelManager;

/**
 * Created on 2022/10/28
 *
 * @author binlee
 */
public class QE{module-name}Client {

  private static final String CUR_MODEL_REL_DIR = "{lower-name}";
  private volatile static boolean isInit = false;

  public static synchronized void init(Context context) {
    if (isInit) return;
    _QAIBaseManager.init(context);
    _QAIBaseManager.loadLibrary("XY{module-name}");
    _QModelManager.addCacheModelApi(IModelApi.fromAssetsDir(getAiType(), CUR_MODEL_REL_DIR));
    isInit = true;
  }

  private static void checkInit() {
    if (!isInit) {
      throw new AINoInitException();
    }
  }

  /** 创建算法实例 */
  public static AI{module-name} create() {
    checkInit();
    return new AI{module-name}();
  }

  public static int getVersion() {
    return Q{module-name}.getVersion();
  }

  // called via reflection from ModelManager
  @SuppressWarnings("unused")
  public static String getAlgoVersion() {
    return Q{module-name}.nativeGetVersion();
  }

  public static int getAiType() {
    return AIConstants.AI_TYPE_{upper-name}/* {ai-type} */;
  }
}

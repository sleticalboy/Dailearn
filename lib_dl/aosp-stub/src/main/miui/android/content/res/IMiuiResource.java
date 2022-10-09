package android.content.res;

import android.util.DisplayMetrics;

/**
 * Created on 2022/10/9
 *
 * @author binlee
 */
public interface IMiuiResource {

  Resources createResources(AssetManager assets, DisplayMetrics metrics, Configuration config);
}

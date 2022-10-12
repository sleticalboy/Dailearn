package com.binlee.dl.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;

/**
 * Created on 2022/10/8
 *
 * @author binlee
 */
public final class DlThemeContext extends ContextThemeWrapper {

  private final Resources mResources;
  private int mThemeRes;

  public DlThemeContext(Context base, Resources resources, int theme) {
    super(base, theme);
    mResources = resources;
    mThemeRes = theme;
  }

  @Override public Resources getResources() {
    return mResources;
  }

  @Override public AssetManager getAssets() {
    return mResources.getAssets();
  }

  @Override public void setTheme(int themeRes) {
    super.setTheme(themeRes);
    mThemeRes = themeRes;
  }

  // private Resources.Theme mTheme;
  // @Override public void setTheme(Resources.Theme theme) {
  //   mTheme = theme;
  // }
  //
  // @Override public Resources.Theme getTheme() {
  //   // 返回 plugin 自己的 theme
  //   if (mTheme == null) {
  //     mTheme = getResources().newTheme();
  //     mTheme.applyStyle(mThemeRes, true);
  //   }
  //   return mTheme;
  // }

  public int getThemeResource() {
    return mThemeRes;
  }
}

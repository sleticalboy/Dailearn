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

  public DlThemeContext(Context base, Resources resources, int theme) {
    super(base, theme);
    mResources = resources;
  }

  @Override public Resources getResources() {
    return mResources;
  }

  @Override public AssetManager getAssets() {
    return mResources.getAssets();
  }
}

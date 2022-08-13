package com.binlee.learning.plugin;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022-08-09.
 *
 * @author binlee
 */
public final class ResourceLoader {

  private static final String TAG = "ResourceLoader";

  private static final Map<String, Boolean> PLUGIN_RESOURCES = new HashMap<>();

  private ResourceLoader() {
    //no instance
  }

  @NonNull public static Resources proxy(@Nullable String pluginPath, @NonNull Resources parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return parent;

    if (PLUGIN_RESOURCES.containsKey(pluginPath)) return parent;

    if (!new File(pluginPath).exists()) return parent;

    // 通过 apk 路径构建新的 Resources，使得主 app 可以加载插件中的资源

    try {
      PLUGIN_RESOURCES.put(pluginPath, null);
      //Class<?> clazz = Class.forName("android.content.res.ApkAssets");
      //Method method = clazz.getDeclaredMethod("loadFromPath", String.class);
      //method.setAccessible(true);
      //Object apkAssets = method.invoke(null, pluginPath);
      final AssetManager assets = AssetManager.class.newInstance();
      Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
      method.setAccessible(true);
      final Object ret = method.invoke(assets, pluginPath);
      // 先从主 app 中获取资源，如果获取不到再从插件中获取
      final ResourcesWrapper wrapper = new ResourcesWrapper(assets, parent);
      Log.d(TAG, "proxy() pluginPath: " + pluginPath + ", addAssetPath: " + ret
              + ", resources: " + wrapper);
      return wrapper;
    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
            | InvocationTargetException e) {
      final Boolean reported = PLUGIN_RESOURCES.get(pluginPath);
      if (reported == null || !reported) {
        e.printStackTrace();
        PLUGIN_RESOURCES.put(pluginPath, true);
      }
    }
    return parent;
  }
  
  private static class ResourcesWrapper extends Resources {
    
    private final Resources mParent;
    private final Resources mPlugin;
    
    public ResourcesWrapper(AssetManager assets, Resources parent) {
      super(parent.getAssets(), parent.getDisplayMetrics(), parent.getConfiguration());
      mPlugin = new Resources(assets, parent.getDisplayMetrics(), parent.getConfiguration());
      mParent = parent;
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
      try {
        return mParent.getText(id);
      } catch (NotFoundException e) {
        return mPlugin.getText(id);
      }
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
      try {
        return mParent.getQuantityText(id, quantity);
      } catch (NotFoundException e) {
        return mPlugin.getQuantityText(id, quantity);
      }
    }

    @Override
    public String getString(int id) throws NotFoundException {
      try {
        return mParent.getString(id);
      } catch (NotFoundException e) {
        return mPlugin.getString(id);
      }
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
      try {
        return mParent.getString(id, formatArgs);
      } catch (NotFoundException e) {
        return mPlugin.getString(id, formatArgs);
      }
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
      throws NotFoundException {
      try {
        return mParent.getQuantityString(id, quantity, formatArgs);
      } catch (NotFoundException e) {
        return mPlugin.getQuantityString(id, quantity, formatArgs);
      }
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
      try {
        return mParent.getQuantityString(id, quantity);
      } catch (NotFoundException e) {
        return mPlugin.getQuantityString(id, quantity);
      }
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
      try {
        return mParent.getText(id, def);
      } catch (Exception e) {
        return mPlugin.getText(id, def);
      }
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
      try {
        return mParent.getTextArray(id);
      } catch (NotFoundException e) {
        return mPlugin.getTextArray(id);
      }
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
      try {
        return mParent.getStringArray(id);
      } catch (NotFoundException e) {
        return mPlugin.getStringArray(id);
      }
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
      try {
        return mParent.getIntArray(id);
      } catch (NotFoundException e) {
        return mPlugin.getIntArray(id);
      }
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
      try {
        return mParent.obtainTypedArray(id);
      } catch (NotFoundException e) {
        return mPlugin.obtainTypedArray(id);
      }
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
      try {
        return mParent.getDimension(id);
      } catch (NotFoundException e) {
        return mPlugin.getDimension(id);
      }
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
      try {
        return mParent.getDimensionPixelOffset(id);
      } catch (NotFoundException e) {
        return mPlugin.getDimensionPixelOffset(id);
      }
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
      try {
        return mParent.getDimensionPixelSize(id);
      } catch (NotFoundException e) {
        return mPlugin.getDimensionPixelSize(id);
      }
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
      try {
        return mParent.getFraction(id, base, pbase);
      } catch (Exception e) {
        return mPlugin.getFraction(id, base, pbase);
      }
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
      return this.getDrawable(id, null);
    }

    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
      try {
        return ResourcesCompat.getDrawable(mParent, id, null);
      } catch (NotFoundException e) {
        return ResourcesCompat.getDrawable(mPlugin, id, null);
      }
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
      // If the developer only overrode the three-arg method, this will cause issues; however,
      // this is very unlikely given that (a) nobody calls this method and (b) nobody overrides
      // this method.
      return this.getDrawableForDensity(id, density, null);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
      try {
        return mParent.getDrawableForDensity(id, density, theme);
      } catch (Exception e) {
        return mPlugin.getDrawableForDensity(id, density, theme);
      }
    }

    @Override
    public android.graphics.Movie getMovie(int id) throws NotFoundException {
      try {
        return mParent.getMovie(id);
      } catch (NotFoundException e) {
        return mPlugin.getMovie(id);
      }
    }

    @Override
    public int getColor(int id) throws NotFoundException {
      try {
        return mParent.getColor(id);
      } catch (NotFoundException e) {
        return mPlugin.getColor(id);
      }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int getColor(int id, @Nullable Theme theme) throws NotFoundException {
      try {
        return mParent.getColor(id, theme);
      } catch (NotFoundException e) {
        return mPlugin.getColor(id, theme);
      }
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
      try {
        return ResourcesCompat.getColorStateList(mParent, id, null);
      } catch (NotFoundException e) {
        return ResourcesCompat.getColorStateList(mPlugin, id, null);
      }
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
      try {
        return mParent.getBoolean(id);
      } catch (NotFoundException e) {
        return mPlugin.getBoolean(id);
      }
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
      try {
        return mParent.getInteger(id);
      } catch (NotFoundException e) {
        return mPlugin.getInteger(id);
      }
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
      try {
        return mParent.getLayout(id);
      } catch (NotFoundException e) {
        return mPlugin.getLayout(id);
      }
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
      try {
        return mParent.getAnimation(id);
      } catch (NotFoundException e) {
        return mPlugin.getAnimation(id);
      }
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
      try {
        return mParent.getXml(id);
      } catch (NotFoundException e) {
        return mParent.getXml(id);
      }
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
      try {
        return mParent.openRawResource(id);
      } catch (NotFoundException e) {
        return mPlugin.openRawResource(id);
      }
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
      try {
        return mParent.openRawResource(id, value);
      } catch (NotFoundException e) {
        return mPlugin.openRawResource(id, value);
      }
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
      try {
        return mParent.openRawResourceFd(id);
      } catch (NotFoundException e) {
        return mPlugin.openRawResourceFd(id);
      }
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        mParent.getValue(id, outValue, resolveRefs);
      } catch (NotFoundException e) {
        mPlugin.getValue(id, outValue, resolveRefs);
      }
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        mParent.getValueForDensity(id, density, outValue, resolveRefs);
      } catch (NotFoundException e) {
        mPlugin.getValueForDensity(id, density, outValue, resolveRefs);
      }
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        mParent.getValue(name, outValue, resolveRefs);
      } catch (NotFoundException e) {
        mPlugin.getValue(name, outValue, resolveRefs);
      }
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
      try {
        return mParent.obtainAttributes(set, attrs);
      } catch (Exception e) {
        return mPlugin.obtainAttributes(set, attrs);
      }
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
      mParent.updateConfiguration(config, metrics);
      if (mPlugin != null) { // called from mParent's constructor. So, need to check.
        mParent.updateConfiguration(config, metrics);
      }
    }

    // @Override
    // public DisplayMetrics getDisplayMetrics() {
    //   return mParent.getDisplayMetrics();
    // }

    // @Override
    // public Configuration getConfiguration() {
    //   return mParent.getConfiguration();
    // }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
      try {
        return mParent.getIdentifier(name, defType, defPackage);
      } catch (Exception e) {
        return mPlugin.getIdentifier(name, defType, defPackage);
      }
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
      try {
        return mParent.getResourceName(resid);
      } catch (NotFoundException e) {
        return mPlugin.getResourceName(resid);
      }
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
      try {
        return mParent.getResourcePackageName(resid);
      } catch (NotFoundException e) {
        return mPlugin.getResourcePackageName(resid);
      }
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
      try {
        return mParent.getResourceTypeName(resid);
      } catch (NotFoundException e) {
        return mPlugin.getResourceTypeName(resid);
      }
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
      try {
        return mParent.getResourceEntryName(resid);
      } catch (NotFoundException e) {
        return mPlugin.getResourceEntryName(resid);
      }
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
      throws XmlPullParserException, IOException {
      try {
        mParent.parseBundleExtras(parser, outBundle);
      } catch (IOException | XmlPullParserException e) {
        mPlugin.parseBundleExtras(parser, outBundle);
      }
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle)
      throws XmlPullParserException {
      try {
        mParent.parseBundleExtra(tagName, attrs, outBundle);
      } catch (XmlPullParserException e) {
        mPlugin.parseBundleExtra(tagName, attrs, outBundle);
      }
    }
  }
}

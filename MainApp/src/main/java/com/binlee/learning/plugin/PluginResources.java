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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Created on 2022-08-09.
 *
 * @author binlee
 */
public final class PluginResources {

  private static final String TAG = "PluginResources";

  private static class ResourcesCache {
    private final Map<String, Boolean> mRegistry = new HashMap<>();
    private Resources mParent;
    private ResourcesWrapper mResources;

    private void setParent(Resources parent) {
      if (mParent == null) mParent = parent;
    }

    private boolean contains(String key) {
      return mRegistry.containsKey(key);
    }

    private void register(AssetManager assets, String pluginPath, Resources parent) {
      if (mParent == null) mParent = parent;

      if (mResources == null) mResources = new ResourcesWrapper(mParent, pluginPath);

      mResources.linkLast(assets, mParent.getDisplayMetrics(), mParent.getConfiguration(), pluginPath);
    }

    private void unregister(String pluginPath) {
      mResources.unlink(pluginPath);
      mRegistry.remove(pluginPath);
    }

    private void setReported(String key, boolean reported) {
      mRegistry.put(key, reported);
    }

    public Boolean getReported(String key) {
      return mRegistry.get(key);
    }
  }

  private static final ResourcesCache sCache = new ResourcesCache();

  private PluginResources() {
    //no instance
  }

  public static void install(@Nullable String pluginPath, @NonNull Resources parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return;

    if (sCache.contains(pluginPath)) return;

    if (!new File(pluginPath).exists()) return;

    sCache.setParent(parent);
    sCache.setReported(pluginPath, false);

    // 通过 apk 路径构建新的 Resources，使得主 app 可以加载插件中的资源
    try {
      // Class<?> clazz = Class.forName("android.content.res.ApkAssets");
      // Method method = clazz.getDeclaredMethod("loadFromPath", String.class);
      // method.setAccessible(true);
      // Object apkAssets = method.invoke(null, pluginPath);
      final AssetManager assets = AssetManager.class.newInstance();
      Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
      method.setAccessible(true);
      final Object ret = method.invoke(assets, pluginPath);
      Log.d(TAG, "proxy() pluginPath: " + pluginPath + ", addAssetPath: " + ret + ", parent: " + parent);
      sCache.register(assets, pluginPath, parent);
    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
            | InvocationTargetException e) {
      Boolean reported = sCache.getReported(pluginPath);
      if (reported == null || !reported) {
        e.printStackTrace();
        sCache.setReported(pluginPath, true);
      }
    }
  }

  public static Resources peek() {
    return sCache.mResources;
  }

  public static void remove(String pluginPath) {
    sCache.unregister(pluginPath);
  }

  // 先从主 app 中获取资源，如果获取不到再从插件中获取
  private static class ResourcesWrapper extends Resources {

    private final String mPluginPath;
    private Resources mParent;
    private ResourcesWrapper mChild;
    
    public ResourcesWrapper(Resources parent, String pluginPath) {
      this(parent.getAssets(), parent.getDisplayMetrics(), parent.getConfiguration(), pluginPath);
      mParent = parent;
    }

    public ResourcesWrapper(AssetManager assets, DisplayMetrics metrics, Configuration config, String pluginPath) {
      super(assets, metrics, config);
      mPluginPath = pluginPath;
    }

    private void linkLast(AssetManager assets, DisplayMetrics metrics, Configuration config,
      String pluginPath) {
      ResourcesWrapper resources = mChild;
      while (resources != null) {
        if (resources.mChild == null) {
          resources.mChild = new ResourcesWrapper(assets, metrics, config, pluginPath);
          break;
        }
        resources = resources.mChild;
      }
    }

    public void unlink(String pluginPath) {
      ResourcesWrapper child = mChild;
      while (child != null) {
        if (child.mPluginPath.equals(pluginPath)) {
          if (child.mChild != null) {
            mChild = child.mChild;
            break;
          }
        }
        child = child.mChild;
      }
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getText(id);
      } catch (Throwable ignored) {
      }
      return super.getText(id);
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getQuantityText(id, quantity);
      } catch (NotFoundException ignored) {
      }
      return super.getQuantityText(id, quantity);
    }

    @Override
    public String getString(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getString(id);
      } catch (NotFoundException ignored) {
      }
      return super.getString(id);
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getString(id, formatArgs);
      } catch (NotFoundException ignored) {
      }
      return super.getString(id, formatArgs);
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
      throws NotFoundException {
      try {
        if (mParent != null) return mParent.getQuantityString(id, quantity, formatArgs);
      } catch (NotFoundException ignored) {
      }
      return super.getQuantityString(id, quantity, formatArgs);
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getQuantityString(id, quantity);
      } catch (NotFoundException ignored) {
      }
      return super.getQuantityString(id, quantity);
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
      try {
        if (mParent != null) return mParent.getText(id, def);
      } catch (Throwable ignored) {
      }
      return super.getText(id, def);
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getTextArray(id);
      } catch (NotFoundException ignored) {
      }
      return super.getTextArray(id);
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getStringArray(id);
      } catch (NotFoundException ignored) {
      }
      return super.getStringArray(id);
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getIntArray(id);
      } catch (NotFoundException ignored) {
      }
      return super.getIntArray(id);
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.obtainTypedArray(id);
      } catch (NotFoundException ignored) {
      }
      return super.obtainTypedArray(id);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getDimension(id);
      } catch (NotFoundException ignored) {
      }
      return super.getDimension(id);
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getDimensionPixelOffset(id);
      } catch (NotFoundException ignored) {
      }
      return super.getDimensionPixelOffset(id);
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getDimensionPixelSize(id);
      } catch (NotFoundException ignored) {
      }
      return super.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
      try {
        if (mParent != null) return mParent.getFraction(id, base, pbase);
      } catch (Throwable ignored) {
      }
      return super.getFraction(id, base, pbase);
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
      return this.getDrawable(id, null);
    }

    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
      try {
        if (mParent != null) return ResourcesCompat.getDrawable(mParent, id, null);
      } catch (NotFoundException ignored) {
      }
      return super.getDrawable(id, theme);
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
        if (mParent != null) return mParent.getDrawableForDensity(id, density, theme);
      } catch (Exception ignored) {
      }
      return super.getDrawableForDensity(id, density, theme);
    }

    @Override
    public android.graphics.Movie getMovie(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getMovie(id);
      } catch (NotFoundException ignored) {
      }
      return super.getMovie(id);
    }

    @Override
    public int getColor(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getColor(id);
      } catch (NotFoundException ignored) {
      }
      return super.getColor(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int getColor(int id, @Nullable Theme theme) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getColor(id, theme);
      } catch (NotFoundException ignored) {
      }
      return super.getColor(id, theme);
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
      try {
        if (mParent != null) return ResourcesCompat.getColorStateList(mParent, id, null);
      } catch (NotFoundException ignored) {
      }
      return super.getColorStateList(id);
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getBoolean(id);
      } catch (NotFoundException ignored) {
      }
      return super.getBoolean(id);
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getInteger(id);
      } catch (NotFoundException ignored) {
      }
      return super.getInteger(id);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getLayout(id);
      } catch (NotFoundException ignored) {
      }
      return super.getLayout(id);
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getAnimation(id);
      } catch (NotFoundException ignored) {
      }
      return super.getAnimation(id);
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getXml(id);
      } catch (NotFoundException ignored) {
      }
      return super.getXml(id);
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.openRawResource(id);
      } catch (NotFoundException ignored) {
      }
      return super.openRawResource(id);
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
      try {
        if (mParent != null) return mParent.openRawResource(id, value);
      } catch (NotFoundException ignored) {
      }
      return super.openRawResource(id, value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
      try {
        if (mParent != null) return mParent.openRawResourceFd(id);
      } catch (NotFoundException ignored) {
      }
      return super.openRawResourceFd(id);
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        if (mParent != null) {
          mParent.getValue(id, outValue, resolveRefs);
          return;
        }
      } catch (NotFoundException ignored) {
      }
      super.getValue(id, outValue, resolveRefs);
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        if (mParent != null) {
          mParent.getValueForDensity(id, density, outValue, resolveRefs);
          return;
        }
      } catch (NotFoundException ignored) {
      }
      super.getValueForDensity(id, density, outValue, resolveRefs);
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        if (mParent != null) {
          mParent.getValue(name, outValue, resolveRefs);
          return;
        }
      } catch (NotFoundException ignored) {
      }
      super.getValue(name, outValue, resolveRefs);
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
      try {
        if (mParent != null) return mParent.obtainAttributes(set, attrs);
      } catch (Exception ignored) {
      }
      return super.obtainAttributes(set, attrs);
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
      if (mParent != null) { // called from mParent's constructor. So, need to check.
        mParent.updateConfiguration(config, metrics);
      }
      super.updateConfiguration(config, metrics);
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
      try {
        if (mParent != null) return mParent.getIdentifier(name, defType, defPackage);
      } catch (Exception ignored) {
      }
      return super.getIdentifier(name, defType, defPackage);
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getResourceName(resid);
      } catch (NotFoundException ignored) {
      }
      return super.getResourceName(resid);
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getResourcePackageName(resid);
      } catch (NotFoundException ignored) {
      }
      return super.getResourcePackageName(resid);
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getResourceTypeName(resid);
      } catch (NotFoundException ignored) {
      }
      return super.getResourceTypeName(resid);
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
      try {
        if (mParent != null) return mParent.getResourceEntryName(resid);
      } catch (NotFoundException ignored) {
      }
      return super.getResourceEntryName(resid);
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
      throws XmlPullParserException, IOException {
      try {
        if (mParent != null) {
          mParent.parseBundleExtras(parser, outBundle);
          return;
        }
      } catch (IOException | XmlPullParserException ignored) {
      }
      super.parseBundleExtras(parser, outBundle);
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle)
      throws XmlPullParserException {
      try {
        if (mParent != null) {
          mParent.parseBundleExtra(tagName, attrs, outBundle);
          return;
        }
      } catch (XmlPullParserException ignored) {
      }
      super.parseBundleExtra(tagName, attrs, outBundle);
    }
  }
}

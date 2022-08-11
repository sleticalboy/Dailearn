package com.binlee.learning;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Created on 2022-08-09.
 *
 * @author binlee
 */
public class ResourceLoader {

  private static final String TAG = "ResourceLoader";

  private static final Set<String> PLUGIN_RESOURCES = new HashSet<>();

  private ResourceLoader() {
    //no instance
  }

  @NonNull public static Resources proxy(@Nullable String pluginPath, @NonNull Resources parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return parent;

    if (PLUGIN_RESOURCES.contains(pluginPath)) return parent;

    // 通过 apk 路径构建新的 Resources，使得主 app 可以加载插件中的资源
    Log.d(TAG, "proxy() pluginPath: " + pluginPath);

    try {
      //Class<?> clazz = Class.forName("android.content.res.ApkAssets");
      //Method method = clazz.getDeclaredMethod("loadFromPath", String.class);
      //method.setAccessible(true);
      //Object apkAssets = method.invoke(null, pluginPath);
      final AssetManager assets = AssetManager.class.newInstance();
      Method method = AssetManager.class.getDeclaredMethod("addAssetsPath", String.class);
      method.setAccessible(true);
      Object ret = method.invoke(assets, pluginPath);
      Log.d(TAG, "proxy() invoke AssetManager#addAssetsPath() ret: " + ret);
      PLUGIN_RESOURCES.add(pluginPath);
      // 先从主 app 中获取资源，如果获取不到再从插件中获取
      return new ResourcesWrapper(assets, parent);
    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return parent;
  }
  
  private static class ResourcesWrapper extends Resources {
    
    private final Resources mPlugin;
    
    public ResourcesWrapper(AssetManager assets, Resources parent) {
      super(parent.getAssets(), parent.getDisplayMetrics(), parent.getConfiguration());
      mPlugin = new Resources(assets, parent.getDisplayMetrics(), parent.getConfiguration());
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
      try {
        return super.getText(id);
      } catch (NotFoundException e) {
        return mPlugin.getText(id);
      }
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
      try {
        return super.getQuantityText(id, quantity);
      } catch (NotFoundException e) {
        return mPlugin.getQuantityText(id, quantity);
      }
    }

    @Override
    public String getString(int id) throws NotFoundException {
      return mPlugin.getString(id);
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
      return mPlugin.getString(id, formatArgs);
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
      throws NotFoundException {
      return mPlugin.getQuantityString(id, quantity, formatArgs);
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
      return mPlugin.getQuantityString(id, quantity);
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
      return mPlugin.getText(id, def);
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
      return mPlugin.getTextArray(id);
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
      return mPlugin.getStringArray(id);
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
      return mPlugin.getIntArray(id);
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
      return mPlugin.obtainTypedArray(id);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
      return mPlugin.getDimension(id);
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
      return mPlugin.getDimensionPixelOffset(id);
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
      return mPlugin.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
      return mPlugin.getFraction(id, base, pbase);
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
      return this.getDrawable(id, null);
    }

    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
      try {
        return ResourcesCompat.getDrawable(this, id, null);
      } catch (NotFoundException e) {
        return ResourcesCompat.getDrawable(mPlugin, id, null);
      }
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
      // If the developer only overrode the three-arg method, this will cause issues; however,
      // this is very unlikely given that (a) nobody calls this method and (b) nobody overrides
      // this method.
      return super.getDrawableForDensity(id, density);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
      return super.getDrawableForDensity(id, density, theme);
    }

    @Override
    public android.graphics.Movie getMovie(int id) throws NotFoundException {
      return mPlugin.getMovie(id);
    }

    @Override
    public int getColor(int id) throws NotFoundException {
      return mPlugin.getColor(id);
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
      return mPlugin.getColorStateList(id);
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
      return mPlugin.getBoolean(id);
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
      return mPlugin.getInteger(id);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
      return mPlugin.getLayout(id);
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
      return mPlugin.getAnimation(id);
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
      return mPlugin.getXml(id);
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
      return mPlugin.openRawResource(id);
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
      return mPlugin.openRawResource(id, value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
      return mPlugin.openRawResourceFd(id);
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      mPlugin.getValue(id, outValue, resolveRefs);
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      try {
        super.getValueForDensity(id, density, outValue, resolveRefs);
      } catch (NotFoundException e) {
        mPlugin.getValueForDensity(id, density, outValue, resolveRefs);
      }
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      mPlugin.getValue(name, outValue, resolveRefs);
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
      return mPlugin.obtainAttributes(set, attrs);
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
      super.updateConfiguration(config, metrics);
      if (mPlugin != null) { // called from super's constructor. So, need to check.
        mPlugin.updateConfiguration(config, metrics);
      }
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
      return mPlugin.getDisplayMetrics();
    }

    @Override
    public Configuration getConfiguration() {
      return mPlugin.getConfiguration();
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
      return mPlugin.getIdentifier(name, defType, defPackage);
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
      return mPlugin.getResourceName(resid);
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
      return mPlugin.getResourcePackageName(resid);
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
      return mPlugin.getResourceTypeName(resid);
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
      return mPlugin.getResourceEntryName(resid);
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
      throws XmlPullParserException, IOException {
      mPlugin.parseBundleExtras(parser, outBundle);
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle)
      throws XmlPullParserException {
      mPlugin.parseBundleExtra(tagName, attrs, outBundle);
    }
  }
}

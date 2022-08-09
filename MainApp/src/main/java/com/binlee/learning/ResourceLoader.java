package com.binlee.learning;

import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.resources.Compatibility;
import androidx.core.content.res.ResourcesCompat;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Created on 2022-08-09.
 *
 * @author binlee
 */
public class ResourceLoader {

  private static final String TAG = "ResourceLoader";

  private ResourceLoader() {
    //no instance
  }

  @NonNull public static Resources proxy(@NonNull Resources parent) {
    return new WrapperResource(parent);
  }
  
  private static class WrapperResource extends Resources {
    
    private final Resources mParent;
    
    public WrapperResource(Resources parent) {
      super(parent.getAssets(), parent.getDisplayMetrics(), parent.getConfiguration());
      mParent = parent;
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
      return mParent.getText(id);
    }

    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
      return mParent.getQuantityText(id, quantity);
    }

    @Override
    public String getString(int id) throws NotFoundException {
      return mParent.getString(id);
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
      return mParent.getString(id, formatArgs);
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs)
      throws NotFoundException {
      return mParent.getQuantityString(id, quantity, formatArgs);
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
      return mParent.getQuantityString(id, quantity);
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
      return mParent.getText(id, def);
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
      return mParent.getTextArray(id);
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
      return mParent.getStringArray(id);
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
      return mParent.getIntArray(id);
    }

    @Override
    public TypedArray obtainTypedArray(int id) throws NotFoundException {
      return mParent.obtainTypedArray(id);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
      return mParent.getDimension(id);
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
      return mParent.getDimensionPixelOffset(id);
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
      return mParent.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
      return mParent.getFraction(id, base, pbase);
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
      return ResourcesCompat.getDrawable(mParent, id, null);
    }

    @RequiresApi(21)
    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
      return ResourcesCompat.getDrawable(mParent, id, theme);
    }

    @RequiresApi(15)
    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
      // If the developer only overrode the three-arg method, this will cause issues; however,
      // this is very unlikely given that (a) nobody calls this method and (b) nobody overrides
      // this method.
      return ResourcesCompat.getDrawableForDensity(mParent, id, density, null);
    }

    @RequiresApi(21)
    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
      return ResourcesCompat.getDrawableForDensity(mParent, id, density, theme);
    }

    @Override
    public android.graphics.Movie getMovie(int id) throws NotFoundException {
      return mParent.getMovie(id);
    }

    @Override
    public int getColor(int id) throws NotFoundException {
      return mParent.getColor(id);
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
      return mParent.getColorStateList(id);
    }

    @Override
    public boolean getBoolean(int id) throws NotFoundException {
      return mParent.getBoolean(id);
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
      return mParent.getInteger(id);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
      return mParent.getLayout(id);
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
      return mParent.getAnimation(id);
    }

    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
      return mParent.getXml(id);
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
      return mParent.openRawResource(id);
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
      return mParent.openRawResource(id, value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
      return mParent.openRawResourceFd(id);
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      mParent.getValue(id, outValue, resolveRefs);
    }

    @RequiresApi(15)
    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      Compatibility.Api15Impl.getValueForDensity(mParent, id, density, outValue, resolveRefs);
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
      throws NotFoundException {
      mParent.getValue(name, outValue, resolveRefs);
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
      return mParent.obtainAttributes(set, attrs);
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
      super.updateConfiguration(config, metrics);
      if (mParent != null) { // called from super's constructor. So, need to check.
        mParent.updateConfiguration(config, metrics);
      }
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
      return mParent.getDisplayMetrics();
    }

    @Override
    public Configuration getConfiguration() {
      return mParent.getConfiguration();
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
      return mParent.getIdentifier(name, defType, defPackage);
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
      return mParent.getResourceName(resid);
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
      return mParent.getResourcePackageName(resid);
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
      return mParent.getResourceTypeName(resid);
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
      return mParent.getResourceEntryName(resid);
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
      throws XmlPullParserException, IOException {
      mParent.parseBundleExtras(parser, outBundle);
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle)
      throws XmlPullParserException {
      mParent.parseBundleExtra(tagName, attrs, outBundle);
    }
  }
}

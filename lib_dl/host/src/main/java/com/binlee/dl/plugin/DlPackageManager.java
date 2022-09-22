package com.binlee.dl.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import android.util.ArrayMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public final class DlPackageManager extends PackageManager {

  // packageName -> DlApk
  private final Map<String, DlApk> mPackages = new ArrayMap<>();
  // pluginPath -> packageName
  private final Map<String, String> mPlugins = new ArrayMap<>();
  // 宿主包管理器
  private final PackageManager mDelegate;
  private final Context mHostContext;

  /**
   * 安装插件
   *
   * @param pluginPath 插件路径
   */
  public void install(String pluginPath) {
    final String packageName = mPlugins.get(pluginPath);
    if (packageName != null && mPackages.get(packageName) != null) {
      return;
    }
    // 1、类加载器：独立的、与宿主合并的
    // 2、资源：theme、context、独立的、与宿主合并的
    // 3、package info pms
    int flags = GET_ACTIVITIES | GET_SERVICES | GET_RECEIVERS | GET_PROVIDERS | GET_SIGNATURES;
    final PackageInfo packageInfo = mDelegate.getPackageArchiveInfo(pluginPath, flags);
    mPackages.put(packageInfo.packageName, new DlApk(mHostContext, packageInfo, pluginPath));
    mPlugins.put(pluginPath, packageInfo.packageName);
  }

  /**
   * 卸载插件
   *
   * @param pluginPath 插件路径
   */
  public void uninstall(String pluginPath) {
    final String packageName = mPlugins.get(pluginPath);
    if (packageName == null || !mPackages.containsKey(packageName)) {
      return;
    }
    final DlApk dlApk = mPackages.remove(packageName);
    if (dlApk != null) dlApk.release();
  }

  public DlPackageManager(Context hostContext) {
    mDelegate = hostContext.getPackageManager();
    mHostContext = hostContext;
  }

  @Override public PackageInfo getPackageInfo(@NonNull String packageName, int flags) throws NameNotFoundException {
    final DlApk dlApk = mPackages.get(packageName);
    if (dlApk != null) {
      return dlApk.getPackageInfo();
    }
    return mDelegate.getPackageInfo(packageName, flags);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override public PackageInfo getPackageInfo(@NonNull VersionedPackage versionedPackage, int flags)
    throws NameNotFoundException {
    return mDelegate.getPackageInfo(versionedPackage, flags);
  }

  @Override public String[] currentToCanonicalPackageNames(@NonNull String[] packageNames) {
    return new String[0];
  }

  @Override public String[] canonicalToCurrentPackageNames(@NonNull String[] packageNames) {
    return new String[0];
  }

  @Nullable @Override public Intent getLaunchIntentForPackage(@NonNull String packageName) {
    return null;
  }

  @Nullable @Override public Intent getLeanbackLaunchIntentForPackage(@NonNull String packageName) {
    return null;
  }

  @Override public int[] getPackageGids(@NonNull String packageName) throws NameNotFoundException {
    return new int[0];
  }

  @Override public int[] getPackageGids(@NonNull String packageName, int flags) throws NameNotFoundException {
    return new int[0];
  }

  @Override public int getPackageUid(@NonNull String packageName, int flags) throws NameNotFoundException {
    return 0;
  }

  @Override public PermissionInfo getPermissionInfo(@NonNull String permName, int flags) throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public List<PermissionInfo> queryPermissionsByGroup(@Nullable String permissionGroup, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public PermissionGroupInfo getPermissionGroupInfo(@NonNull String groupName, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
    return null;
  }

  @NonNull @Override public ApplicationInfo getApplicationInfo(@NonNull String packageName, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public ActivityInfo getActivityInfo(@NonNull ComponentName component, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public ActivityInfo getReceiverInfo(@NonNull ComponentName component, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public ServiceInfo getServiceInfo(@NonNull ComponentName component, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public ProviderInfo getProviderInfo(@NonNull ComponentName component, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public List<PackageInfo> getInstalledPackages(int flags) {
    return null;
  }

  @NonNull @Override public List<PackageInfo> getPackagesHoldingPermissions(@NonNull String[] permissions, int flags) {
    return null;
  }

  @Override public int checkPermission(@NonNull String permName, @NonNull String packageName) {
    return PERMISSION_DENIED;
  }

  @Override public boolean isPermissionRevokedByPolicy(@NonNull String permName, @NonNull String packageName) {
    return false;
  }

  @Override public boolean addPermission(@NonNull PermissionInfo info) {
    return false;
  }

  @Override public boolean addPermissionAsync(@NonNull PermissionInfo info) {
    return false;
  }

  @Override public void removePermission(@NonNull String permName) {

  }

  @Override public int checkSignatures(@NonNull String packageName1, @NonNull String packageName2) {
    return SIGNATURE_NO_MATCH;
  }

  @Override public int checkSignatures(int uid1, int uid2) {
    return SIGNATURE_NO_MATCH;
  }

  @Nullable @Override public String[] getPackagesForUid(int uid) {
    return new String[0];
  }

  @Nullable @Override public String getNameForUid(int uid) {
    return null;
  }

  @NonNull @Override public List<ApplicationInfo> getInstalledApplications(int flags) {
    return null;
  }

  @Override public boolean isInstantApp() {
    return false;
  }

  @Override public boolean isInstantApp(@NonNull String packageName) {
    return false;
  }

  @Override public int getInstantAppCookieMaxBytes() {
    return 0;
  }

  @NonNull @Override public byte[] getInstantAppCookie() {
    return new byte[0];
  }

  @Override public void clearInstantAppCookie() {

  }

  @Override public void updateInstantAppCookie(@Nullable byte[] cookie) {

  }

  @Nullable @Override public String[] getSystemSharedLibraryNames() {
    return new String[0];
  }

  @NonNull @Override public List<SharedLibraryInfo> getSharedLibraries(int flags) {
    return null;
  }

  @Nullable @Override public ChangedPackages getChangedPackages(int sequenceNumber) {
    return null;
  }

  @NonNull @Override public FeatureInfo[] getSystemAvailableFeatures() {
    return new FeatureInfo[0];
  }

  @Override public boolean hasSystemFeature(@NonNull String featureName) {
    return false;
  }

  @Override public boolean hasSystemFeature(@NonNull String featureName, int version) {
    return false;
  }

  @Nullable @Override public ResolveInfo resolveActivity(@NonNull Intent intent, int flags) {
    return null;
  }

  @NonNull @Override public List<ResolveInfo> queryIntentActivities(@NonNull Intent intent, int flags) {
    return null;
  }

  @NonNull @Override
  public List<ResolveInfo> queryIntentActivityOptions(@Nullable ComponentName caller, @Nullable Intent[] specifics,
    @NonNull Intent intent, int flags) {
    return null;
  }

  @NonNull @Override public List<ResolveInfo> queryBroadcastReceivers(@NonNull Intent intent, int flags) {
    return null;
  }

  @Nullable @Override public ResolveInfo resolveService(@NonNull Intent intent, int flags) {
    return null;
  }

  @NonNull @Override public List<ResolveInfo> queryIntentServices(@NonNull Intent intent, int flags) {
    return null;
  }

  @NonNull @Override public List<ResolveInfo> queryIntentContentProviders(@NonNull Intent intent, int flags) {
    return null;
  }

  @Nullable @Override public ProviderInfo resolveContentProvider(@NonNull String authority, int flags) {
    return null;
  }

  @NonNull @Override public List<ProviderInfo> queryContentProviders(@Nullable String processName, int uid, int flags) {
    return null;
  }

  @NonNull @Override public InstrumentationInfo getInstrumentationInfo(@NonNull ComponentName className, int flags)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public List<InstrumentationInfo> queryInstrumentation(@NonNull String targetPackage, int flags) {
    return null;
  }

  @Nullable @Override
  public Drawable getDrawable(@NonNull String packageName, int resid, @Nullable ApplicationInfo appInfo) {
    return null;
  }

  @NonNull @Override public Drawable getActivityIcon(@NonNull ComponentName activityName) throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public Drawable getActivityIcon(@NonNull Intent intent) throws NameNotFoundException {
    return null;
  }

  @Nullable @Override public Drawable getActivityBanner(@NonNull ComponentName activityName)
    throws NameNotFoundException {
    return null;
  }

  @Nullable @Override public Drawable getActivityBanner(@NonNull Intent intent) throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public Drawable getDefaultActivityIcon() {
    return null;
  }

  @NonNull @Override public Drawable getApplicationIcon(@NonNull ApplicationInfo info) {
    return null;
  }

  @NonNull @Override public Drawable getApplicationIcon(@NonNull String packageName) throws NameNotFoundException {
    return null;
  }

  @Nullable @Override public Drawable getApplicationBanner(@NonNull ApplicationInfo info) {
    return null;
  }

  @Nullable @Override public Drawable getApplicationBanner(@NonNull String packageName) throws NameNotFoundException {
    return null;
  }

  @Nullable @Override public Drawable getActivityLogo(@NonNull ComponentName activityName)
    throws NameNotFoundException {
    return null;
  }

  @Nullable @Override public Drawable getActivityLogo(@NonNull Intent intent) throws NameNotFoundException {
    return null;
  }

  @Nullable @Override public Drawable getApplicationLogo(@NonNull ApplicationInfo info) {
    return null;
  }

  @Nullable @Override public Drawable getApplicationLogo(@NonNull String packageName) throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public Drawable getUserBadgedIcon(@NonNull Drawable drawable, @NonNull UserHandle user) {
    return null;
  }

  @NonNull @Override
  public Drawable getUserBadgedDrawableForDensity(@NonNull Drawable drawable, @NonNull UserHandle user,
    @Nullable Rect badgeLocation, int badgeDensity) {
    return null;
  }

  @NonNull @Override public CharSequence getUserBadgedLabel(@NonNull CharSequence label, @NonNull UserHandle user) {
    return null;
  }

  @Nullable @Override
  public CharSequence getText(@NonNull String packageName, int resid, @Nullable ApplicationInfo appInfo) {
    return null;
  }

  @Nullable @Override
  public XmlResourceParser getXml(@NonNull String packageName, int resid, @Nullable ApplicationInfo appInfo) {
    return null;
  }

  @NonNull @Override public CharSequence getApplicationLabel(@NonNull ApplicationInfo info) {
    return null;
  }

  @NonNull @Override public Resources getResourcesForActivity(@NonNull ComponentName activityName)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public Resources getResourcesForApplication(@NonNull ApplicationInfo app)
    throws NameNotFoundException {
    return null;
  }

  @NonNull @Override public Resources getResourcesForApplication(@NonNull String packageName)
    throws NameNotFoundException {
    return null;
  }

  @Override public void verifyPendingInstall(int id, int verificationCode) {

  }

  @Override public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {

  }

  @Override public void setInstallerPackageName(@NonNull String targetPackage, @Nullable String installerPackageName) {

  }

  @Nullable @Override public String getInstallerPackageName(@NonNull String packageName) {
    return null;
  }

  @Override public void addPackageToPreferred(@NonNull String packageName) {

  }

  @Override public void removePackageFromPreferred(@NonNull String packageName) {

  }

  @NonNull @Override public List<PackageInfo> getPreferredPackages(int flags) {
    return null;
  }

  @Override public void addPreferredActivity(@NonNull IntentFilter filter, int match, @Nullable ComponentName[] set,
    @NonNull ComponentName activity) {

  }

  @Override public void clearPackagePreferredActivities(@NonNull String packageName) {

  }

  @Override
  public int getPreferredActivities(@NonNull List<IntentFilter> outFilters, @NonNull List<ComponentName> outActivities,
    @Nullable String packageName) {
    return 0;
  }

  @Override public void setComponentEnabledSetting(@NonNull ComponentName componentName, int newState, int flags) {

  }

  @Override public int getComponentEnabledSetting(@NonNull ComponentName componentName) {
    return COMPONENT_ENABLED_STATE_DEFAULT;
  }

  @Override public void setApplicationEnabledSetting(@NonNull String packageName, int newState, int flags) {

  }

  @Override public int getApplicationEnabledSetting(@NonNull String packageName) {
    return COMPONENT_ENABLED_STATE_DEFAULT;
  }

  @Override public boolean isSafeMode() {
    return false;
  }

  @Override public void setApplicationCategoryHint(@NonNull String packageName, int categoryHint) {

  }

  @NonNull @Override public PackageInstaller getPackageInstaller() {
    return mDelegate.getPackageInstaller();
  }

  @Override public boolean canRequestPackageInstalls() {
    return false;
  }
}

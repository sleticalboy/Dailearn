package com.binlee.dl.plugin;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.util.Log;
import java.io.File;

/**
 * Created on 2022/9/28
 *
 * @author binlee
 */
public final class DlPackageUtil {

  private static final String TAG = "DlPackageUtil";

  private DlPackageUtil() {
    //no instance
  }

  public static PackageParser.Package parsePackage(String pluginPath) {
    final PackageParser parser = new PackageParser();
    try {
      return parser.parsePackage(new File(pluginPath), PackageParser.PARSE_MUST_BE_APK);
    } catch (PackageParser.PackageParserException e) {
      throw new IllegalArgumentException("Invalid apk file format: " + pluginPath, e);
    }
  }

  public static PackageInfo generatePackageInfo(Context hostContext, PackageParser.Package pkg,
    String pluginPath) {
    if (pkg == null) {
      throw new IllegalArgumentException("empty package");
    }
    // Package 转 package info
    final PackageInfo info = new PackageInfo();
    info.applicationInfo = pkg.applicationInfo;
    info.applicationInfo.sourceDir = pluginPath;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
      || (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1 && Build.VERSION.PREVIEW_SDK_INT != 0)) {
      try {
        info.signatures = pkg.mSigningDetails.signatures;
      } catch (Throwable tr) {
        try {
          final PackageInfo packageInfo = hostContext.getPackageManager()
            .getPackageInfo(hostContext.getPackageName(), PackageManager.GET_SIGNATURES);
          info.signatures = packageInfo.signatures;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
      }
    } else {
      info.signatures = pkg.mSignatures;
    }
    info.packageName = pkg.packageName;
    info.versionCode = pkg.mVersionCode;
    info.versionName = pkg.mVersionName;
    info.permissions = new PermissionInfo[0];
    // instrumentations
    info.instrumentation = new InstrumentationInfo[pkg.instrumentation.size()];
    for (int i = 0; i < info.instrumentation.length; i++) {
      info.instrumentation[i] = pkg.instrumentation.get(i).info;
    }
    // activities
    info.activities = new ActivityInfo[pkg.activities.size()];
    for (int i = 0; i < info.activities.length; i++) {
      info.activities[i] = pkg.activities.get(i).info;
    }
    // services
    info.services = new ServiceInfo[pkg.services.size()];
    for (int i = 0; i < info.services.length; i++) {
      info.services[i] = pkg.services.get(i).info;
    }
    // receivers: 静态广播
    info.receivers = new ActivityInfo[pkg.receivers.size()];
    for (int i = 0; i < info.receivers.length; i++) {
      info.receivers[i] = pkg.receivers.get(i).info;
      // dumpReceiver(pkg.receivers.get(i));
    }
    // providers
    info.providers = new ProviderInfo[pkg.providers.size()];
    for (int i = 0; i < info.providers.length; i++) {
      info.providers[i] = pkg.providers.get(i).info;
      dumpProvider(pkg.providers.get(i));
    }
    return info;
  }

  private static void dumpProvider(PackageParser.Provider provider) {
    final StringBuilder buffer = new StringBuilder();
    Log.d(TAG, "dumpReceiver() name: " + provider.className
      + ", authority: " + provider.info.authority
      + ", readPermission: " + provider.info.readPermission
      + ", writePermission: " + provider.info.writePermission
    );
  }

  private static void dumpReceiver(PackageParser.Activity receiver) {
    final StringBuilder buffer = new StringBuilder();
    for (PackageParser.ActivityIntentInfo intent : receiver.intents) {
      for (int i = 0; i < intent.countActions(); i++) {
        buffer.append(intent.getAction(i)).append('\n');
      }
    }
    Log.d(TAG, "dumpReceiver() name: " + receiver.className
      + ", permission: " + receiver.info.permission
      + ", intents: " + buffer);
  }
}

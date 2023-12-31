//
// Created by weishu on 2018/6/7.
//

#ifndef DAILEARN_JNI_ART_RUNTIME_H
#define DAILEARN_JNI_ART_RUNTIME_H

#include "jni.h"
#include "string"

#include "mutex"
#include "set"
#include "vector"

struct JavaVMExt {
  void *functions;
  void *runtime;
};

// Refer: https://android.googlesource.com/platform/art/+/master/runtime/experimental_flags.h
struct ExperimentalFlags {
  uint32_t value;
};

// Refer: https://android.googlesource.com/platform/art/+/master/runtime/hidden_api.h
// Hidden API enforcement policy
// This must be kept in sync with ApplicationInfo.ApiEnforcementPolicy in
// frameworks/base/core/java/android/content/pm/ApplicationInfo.java
enum class EnforcementPolicy {
  kDisabled             = 0,
  kJustWarn             = 1,  // keep checks enabled, but allow everything (enables logging)
  kEnabled              = 2,  // ban conditionally blocked & blocklist
  kMax = kEnabled,
};

// ART counterpart of the compat framework (go/compat-framework).
// Created in order to avoid repeated up-calls to Java.
struct CompatFramework {
  // A set of disabled compat changes for the running app, all other changes are enabled.
  std::set<uint64_t> disabled_compat_changes_;

  // A set of reported compat changes for the running app.
  std::set<uint64_t> reported_compat_changes_;
  // Mutex reported_compat_changes_lock_ DEFAULT_MUTEX_ACQUIRED_AFTER;
};

struct PartialRuntime {
  // Specifies target SDK version to allow workarounds for certain API levels.
  int32_t target_sdk_version_;

  // Implicit checks flags.
  bool implicit_null_checks_;       // NullPointer checks are implicit.
  bool implicit_so_checks_;         // StackOverflow checks are implicit.
  bool implicit_suspend_checks_;    // Thread suspension checks are implicit.

  // Whether or not the sig chain (and implicitly the fault handler) should be
  // disabled. Tools like dex2oat or patchoat don't need them. This enables
  // building a statically link version of dex2oat.
  bool no_sig_chain_;

  // Force the use of native bridge even if the app ISA matches the runtime ISA.
  bool force_native_bridge_;

  // Whether or not a native bridge has been loaded.
  //
  // The native bridge allows running native code compiled for a foreign ISA. The way it works is,
  // if standard dlopen fails to load native library associated with native activity, it calls to
  // the native bridge to load it and then gets the trampoline for the entry to native activity.
  //
  // The option 'native_bridge_library_filename' specifies the name of the native bridge.
  // When non-empty the native bridge will be loaded from the given file. An empty value means
  // that there's no native bridge.
  bool is_native_bridge_loaded_;

  // Whether we are running under native debugger.
  bool is_native_debuggable_;

  // whether or not any async exceptions have ever been thrown. This is used to speed up the
  // MterpShouldSwitchInterpreters function.
  bool async_exceptions_thrown_;

  // Whether Java code needs to be debuggable.
  bool is_java_debuggable_;

  // The maximum number of failed boots we allow before pruning the dalvik cache
  // and trying again. This option is only inspected when we're running as a
  // zygote.
  uint32_t zygote_max_failed_boots_;

  // Enable experimental opcodes that aren't fully specified yet. The intent is to
  // eventually publish them as public-usable opcodes, but they aren't ready yet.
  //
  // Experimental opcodes should not be used by other production code.
  ExperimentalFlags experimental_flags_;

  // Contains the build fingerprint, if given as a parameter.
  std::string fingerprint_;

  // Oat file manager, keeps track of what oat files are open.
  // OatFileManager* oat_file_manager_;
  void *oat_file_manager_;

  // Whether or not we are on a low RAM device.
  bool is_low_memory_mode_;

  // Whether or not we use MADV_RANDOM on files that are thought to have random access patterns.
  // This is beneficial for low RAM devices since it reduces page cache thrashing.
  bool madvise_random_access_;

  // Whether the application should run in safe mode, that is, interpreter only.
  bool safe_mode_;

  // Whether access checks on hidden API should be performed.
  EnforcementPolicy hidden_api_policy_;
};

// Android R: https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r3/runtime/runtime.h#1182
struct PartialRuntimeR {
  // Specifies target SDK version to allow workarounds for certain API levels.
  uint32_t target_sdk_version_;

  // ART counterpart for the compat framework (go/compat-framework).
  CompatFramework compat_framework_;

  // Implicit checks flags.
  bool implicit_null_checks_;       // NullPointer checks are implicit.
  bool implicit_so_checks_;         // StackOverflow checks are implicit.
  bool implicit_suspend_checks_;    // Thread suspension checks are implicit.

  // Whether or not the sig chain (and implicitly the fault handler) should be
  // disabled. Tools like dex2oat don't need them. This enables
  // building a statically link version of dex2oat.
  bool no_sig_chain_;

  // Force the use of native bridge even if the app ISA matches the runtime ISA.
  bool force_native_bridge_;

  // Whether or not a native bridge has been loaded.
  //
  // The native bridge allows running native code compiled for a foreign ISA. The way it works is,
  // if standard dlopen fails to load native library associated with native activity, it calls to
  // the native bridge to load it and then gets the trampoline for the entry to native activity.
  //
  // The option 'native_bridge_library_filename' specifies the name of the native bridge.
  // When non-empty the native bridge will be loaded from the given file. An empty value means
  // that there's no native bridge.
  bool is_native_bridge_loaded_;

  // Whether we are running under native debugger.
  bool is_native_debuggable_;

  // whether or not any async exceptions have ever been thrown. This is used to speed up the
  // MterpShouldSwitchInterpreters function.
  bool async_exceptions_thrown_;

  // Whether anything is going to be using the shadow-frame APIs to force a function to return
  // early. Doing this requires that (1) we be debuggable and (2) that mterp is exited.
  bool non_standard_exits_enabled_;

  // Whether Java code needs to be debuggable.
  bool is_java_debuggable_;

  bool monitor_timeout_enable_;
  uint64_t monitor_timeout_ns_;

  // Whether or not this application can be profiled by the shell user,
  // even when running on a device that is running in user mode.
  bool is_profileable_from_shell_ = false;

  // Whether or not this application can be profiled by system services on a
  // device running in user mode, but not necessarily by the shell user.
  bool is_profileable_ = false;

  // The maximum number of failed boots we allow before pruning the dalvik cache
  // and trying again. This option is only inspected when we're running as a
  // zygote.
  uint32_t zygote_max_failed_boots_;

  // Enable experimental opcodes that aren't fully specified yet. The intent is to
  // eventually publish them as public-usable opcodes, but they aren't ready yet.
  //
  // Experimental opcodes should not be used by other production code.
  ExperimentalFlags experimental_flags_;

  // Contains the build fingerprint, if given as a parameter.
  std::string fingerprint_;

  // Oat file manager, keeps track of what oat files are open.
  // OatFileManager* oat_file_manager_;
  void *oat_file_manager_;

  // Whether or not we are on a low RAM device.
  bool is_low_memory_mode_;

  // Whether or not we use MADV_RANDOM on files that are thought to have random access patterns.
  // This is beneficial for low RAM devices since it reduces page cache thrashing.
  bool madvise_random_access_;

  // Limiting size (in bytes) for applying MADV_WILLNEED on vdex files
  // A 0 for this will turn off madvising to MADV_WILLNEED
  size_t madvise_willneed_vdex_filesize_;

  // Limiting size (in bytes) for applying MADV_WILLNEED on odex files
  // A 0 for this will turn off madvising to MADV_WILLNEED
  size_t madvise_willneed_odex_filesize_;

  // Limiting size (in bytes) for applying MADV_WILLNEED on art files
  // A 0 for this will turn off madvising to MADV_WILLNEED
  size_t madvise_willneed_art_filesize_;

  // Whether the application should run in safe mode, that is, interpreter only.
  bool safe_mode_;

  // Whether access checks on hidden API should be performed.
  EnforcementPolicy hidden_api_policy_;
  // Whether access checks on core platform API should be performed.
  EnforcementPolicy core_platform_api_policy_;
  // Whether access checks on test API should be performed.
  EnforcementPolicy test_api_policy_;

  // List of signature prefixes of methods that have been removed from the blacklist, and treated
  // as if whitelisted.
  std::vector<std::string> hidden_api_exemptions_;
};


int unseal(JNIEnv *env, jint targetSdkVersion);

#endif // DAILEARN_JNI_ART_RUNTIME_H
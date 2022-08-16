/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.binlee.learning.plugin;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Exposes plugin's dex files as files in the application data directory.
 * {@link DexExtractor} is taking the file lock in the dex dir on creation and release it
 * during close.
 */
final class DexExtractor implements Closeable {

  private static final String TAG = "DexExtractor";

  /** 解压时，重复 3 次提高成功率 */
  private static final int MAX_EXTRACT_ATTEMPTS = 3;

  /** Size of reading buffers. */
  private static final int BUFFER_SIZE = 0x4000;

  private static final String DEX_FORMAT = "classes%s.dex";
  private static final String LOCK_FILENAME = "PluginDex.lock";
  private final File sourceApk;
  private final File dexDir;
  private final RandomAccessFile lockRaf;
  private final FileChannel lockChannel;
  // 文件锁
  private final FileLock cacheLock;

  DexExtractor(File sourceApk, File dexDir) throws IOException {
    Log.i(TAG, "init<>(" + sourceApk.getPath() + ", " + dexDir.getPath() + ")");
    this.sourceApk = sourceApk;
    this.dexDir = dexDir;
    File lockFile = new File(dexDir, LOCK_FILENAME);
    lockRaf = new RandomAccessFile(lockFile, "rw");
    try {
      lockChannel = lockRaf.getChannel();
      try {
        Log.i(TAG, "Blocking on lock " + lockFile.getPath());
        cacheLock = lockChannel.lock();
      } catch (IOException | RuntimeException | Error e) {
        closeQuietly(lockChannel);
        throw e;
      }
      Log.i(TAG, lockFile.getPath() + " locked");
    } catch (IOException | RuntimeException | Error e) {
      closeQuietly(lockRaf);
      throw e;
    }
  }

  /**
   * Extracts plugin's dex files into the application data directory.
   *
   * @return a list of files that were created. The list may be empty if there
   * are no dex files. Never return null.
   * @throws IOException if encounters a problem while reading or writing
   * dex files
   */
  List<File> extractDex() throws IOException {
    if (!cacheLock.isValid()) {
      throw new IllegalStateException("DexExtractor was closed");
    }
    try {
      return performExtractions();
    } catch (IOException ioe) {
      Log.w(TAG, "Failed to reload existing extracted dex files, falling back to fresh extraction", ioe);
      throw ioe;
    }
  }

  @Override public void close() throws IOException {
    cacheLock.release();
    lockChannel.close();
    lockRaf.close();
  }

  // 解压 apk，拿到 dex 文件
  private List<File> performExtractions() throws IOException {
    // It is safe to fully clear the dex dir because we own the file lock so no other process is
    // extracting or running optimizing dexopt. It may cause crash of already running
    // applications if for whatever reason we end up extracting again over a valid extraction.
    clearDexDir();

    final List<File> files = new ArrayList<>();
    // zip 格式
    // Archive:  com.sample.plugin.apk
    // Length   Method    Size    Cmpr    Date    Time   CRC-32   Name
    // --------  ------  -------  ---- ---------- ----- --------  ----
    // 10732944  Defl:N  4464951  58%  1981-01-01 01:01 3e9016df  classes.dex
    //  1274320  Defl:N   427501  67%  1981-01-01 01:01 60995752  classes2.dex
    //  4239832  Defl:N  1755941  59%  1981-01-01 01:01 ec9f9b3c  classes3.dex
    //     6688  Defl:N     3609  46%  1981-01-01 01:01 f6713aa0  classes4.dex
    //     2116  Defl:N     1259  41%  1981-01-01 01:01 cca53e17  classes5.dex
    final ZipFile apk = new ZipFile(sourceApk);
    try {
      for (int i = 1; ; i++) {
        final String index = i > 1 ? Integer.toString(i) : "";
        // classes{%s}.dex
        final ZipEntry dexFile = apk.getEntry(String.format(DEX_FORMAT, index));
        if (dexFile == null) break;

        // com.sample.plugin.apk.{%s}.dex
        final String dexName = sourceApk.getName() + index + ".dex";
        final File extractedFile = new File(dexDir, dexName);
        files.add(extractedFile);

        Log.i(TAG, "Extraction is needed for file " + extractedFile);
        int numAttempts = 0;
        boolean isExtractionSuccessful = false;
        // 重试 3 次
        while (numAttempts < MAX_EXTRACT_ATTEMPTS && !isExtractionSuccessful) {
          numAttempts++;
          try {
            // 这里我们直接把 dex 解压出来
            extractEntry(apk, dexFile, extractedFile);
            isExtractionSuccessful = true;
          } catch (IOException e) {
            isExtractionSuccessful = false;
          }

          // Log size and of the extracted zip file
          Log.i(TAG, "Extraction " + (isExtractionSuccessful ? "succeeded" : "failed")
            + " '" + extractedFile.getAbsolutePath() + "': length " + extractedFile.length());
          if (!isExtractionSuccessful) {
            // Delete the extracted file
            if (extractedFile.exists()) {
              boolean deleted = extractedFile.delete();
              Log.w(TAG, "Delete bad file '" + extractedFile.getPath() + "': " + deleted);
            }
          }
        }
        if (!isExtractionSuccessful) {
          throw new IOException("Could not create zip file " + extractedFile.getAbsolutePath()
            + " for secondary dex (" + index + ")");
        }
      }
    } finally {
      closeQuietly(apk);
    }
    Log.i(TAG, "performExtractions() found " + files.size() + " dex files");
    return files;
  }

  /** Clear the dex dir from all files but the lock. */
  private void clearDexDir() {
    File[] files = dexDir.listFiles(pathname -> !pathname.getName().equals(LOCK_FILENAME));
    if (files == null) {
      Log.w(TAG, "Failed to list plugin dex dir content (" + dexDir.getPath() + ").");
      return;
    }
    for (File oldFile : files) {
      Log.i(TAG, "Trying to delete old file " + oldFile.getPath() + " of size " + oldFile.length());
      if (!oldFile.delete()) {
        Log.w(TAG, "Failed to delete old file " + oldFile.getPath());
      } else {
        Log.i(TAG, "Deleted old file " + oldFile.getPath());
      }
    }
  }

  private static void extractEntry(ZipFile apk, ZipEntry dexFile, File extractTo) throws IOException {
    Log.i(TAG, "Extracting " + extractTo.getPath());
    final InputStream in = apk.getInputStream(dexFile);
    try {
      final FileOutputStream out = new FileOutputStream(extractTo);
      final byte[] buffer = new byte[BUFFER_SIZE];
      int length;
      while ((length = in.read(buffer)) != -1) {
        out.write(buffer, 0, length);
      }
      closeQuietly(out);
      if (!extractTo.setReadOnly()) {
        throw new IOException("Failed to mark readonly \"" + extractTo.getAbsolutePath() +
          "\" (extractTo of \"" + extractTo.getAbsolutePath() + "\")");
      }
    } finally {
      closeQuietly(in);
    }
  }

  /** Closes the given {@code Closeable}. Suppresses any IO exceptions. */
  private static void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      Log.w(TAG, "Failed to close resource", e);
    }
  }
}

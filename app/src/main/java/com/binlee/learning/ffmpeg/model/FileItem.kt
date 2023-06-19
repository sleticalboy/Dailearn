package com.binlee.learning.ffmpeg.model

import java.io.File

class FileItem(var file: File, var checked: Boolean = false) {
  fun delete(): Boolean {
    return file.delete()
  }

  val path: String? = file.absolutePath
  val name: CharSequence? = file.name
}
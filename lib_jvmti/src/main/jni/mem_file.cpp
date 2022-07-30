//
// Created by binlee on 2022/7/15.
//

#include <sys/mman.h>
#include <sys/fcntl.h>
#include <unistd.h>
#include <map>
#include <cerrno>
#include "mem_file.h"
#include "jni_logger.h"

#define LOG_TAG "MemFile"

namespace jvmti {

void MemFile::Append(const char *data, int length) {
  if (!Open()) {
    ALOGE("%s abort fd: %d", __func__, _fd)
    return;
  }

  // 写入数据的长度
  // ALOGD("%s write data size: %d", __func__, length)

  if (buf_offset + length >= buf_size) {
    // 文件过小要扩容
    Resize(buf_offset + length);
  }
  // 从文件尾部开始追加，补齐后写入
  memcpy(mem_buf + buf_offset / 4, data, length);
  int mod = length % 4;
  buf_offset += mod != 0 ? length + 4 - mod : length;
  // ALOGD("%s write to mem_buf by memcpy done, offset: %d", __func__, buf_offset)
}

bool MemFile::Open() {

  if (_fd > 0 && mem_buf != nullptr) return true;

  FILE *fp = fopen(_path, "w+");
  if (fp == nullptr) {
    ALOGE("%s open or create file %s error: %d", __func__, _path, errno)
    return false;
  }
  fclose(fp);
  // 文件句柄会在 MemFile 析构的时候关闭
  _fd = open(_path, O_RDWR);
  if (errno || _fd == -1) {
    ALOGE("%s failed as errno: %d, fd: %d", __func__, errno, _fd)
    if (_fd != -1) close(_fd);
    _fd = -1;
    return false;
  }
  if (buf_size == 0) {
    buf_size = getpagesize();
    // 必须先 ftruncate 再 mmap，否则映射出来的内存地址不能 write
    int res = ftruncate(_fd, buf_size);
    if (res != 0) {
      ALOGE("%s ftruncate res: %d", __func__, res)
      return false;
    }
  }

  // 如果文件已有内容，要覆盖还是要追加？
  // 如果追加的话，如何追加？
  // 1、将原文件内容全部读取出来，上传到服务器/加载到内存中；
  // 2、将原内容使用内存数据进行覆盖；
  mem_buf = (int *) mmap(nullptr, buf_size, PROT_READ | PROT_WRITE, MAP_SHARED, _fd, 0);
  if (mem_buf == MAP_FAILED) {
    Close();
    ALOGE("%s mmap failed: %d", __func__, errno)
    return false;
  }
  ALOGD("%s mmap mem_buf: %p, fd: %d", __func__, mem_buf, _fd)
  return true;
}

bool MemFile::Resize(int resize) {
  int old_size = buf_size;
  do {
    buf_size *= 2;
  } while (buf_size < resize);
  // 文件扩容
  int res = ftruncate(_fd, buf_size);
  if (res != 0) {
    ALOGE("%s ftruncate res: %d", __func__, res)
    return false;
  }
  // 解除原有映射并重新映射
  munmap(mem_buf, old_size);
  mem_buf = (int *) mmap(nullptr, buf_size, PROT_READ | PROT_WRITE, MAP_SHARED, _fd, 0);
  if (mem_buf == MAP_FAILED) {
    ALOGE("%s mmap failed: %d", __func__, errno)
    return false;
  }
  ALOGD("%s buf_size: %d", __func__, buf_size)
  return true;
}

void MemFile::Close() {
  munmap(mem_buf, buf_size);
  close(_fd);
  _fd = -1;
  mem_buf = nullptr;
  _path = nullptr;
}

}


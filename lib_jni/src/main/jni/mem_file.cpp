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

void jvmti::MemFile::Append(const char *data, int length) {
  if (!Open()) {
    ALOGE("%s abort fd: %d", __func__, _fd)
    return;
  }
  ALOGD("%s page size: %d, buffer size is %d", __func__, getpagesize(), buf_size)

  // 写入数据的长度
  int write_size = strlen(data);
  ALOGD("%s write data size: %d", __func__, write_size)

  if (buf_offset + write_size >= buf_size) {
    // 文件过小要扩容
    Resize(buf_offset + write_size);
  }

  // res = close(fd);
  // if (res != 0) {
  //   ALOGE("%s close res: %d", __func__, res)
  // }

  ALOGD("%s start write to mem_buf, offset: %d", __func__, buf_offset)
  // 从文件尾部开始追加
  memcpy(mem_buf + buf_offset / 4, data, write_size);
  buf_offset += write_size;
  // 内存对齐
  if (write_size % 4 != 0) {
    buf_offset += (4 - write_size % 4);
  }
  ALOGD("%s write to mem_buf by memcpy done, offset: %d", __func__, buf_offset)

  // res = munmap(buffer, buffer_size);
  // if (res != 0) {
  //   ALOGE("%s munmap res: %d", __func__, res)
  // }
}

bool jvmti::MemFile::Open() {
  FILE *fp = fopen(_path, "w+");
  if (fp == nullptr) {
    ALOGE("%s create file error: %p", __func__, fp)
    return false;
  }
  fclose(fp);
  // 找个时机把文件句柄关闭了，否则会内存泄露
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
    close(_fd);
    ALOGE("%s mmap failed: %d", __func__, errno)
    return false;
  }
  ALOGD("%s mmap mem_buf: %p, fd: %d", __func__, mem_buf, _fd)
  return true;
}

bool jvmti::MemFile::Resize(int resize) {
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

void jvmti::MemFile::Close() {
  close(_fd);
  _fd = -1;
  mem_buf = nullptr;
}


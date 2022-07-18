//
// Created by binlee on 2022/7/15.
//

#ifndef DAILY_WORK_MEM_FILE_H
#define DAILY_WORK_MEM_FILE_H

namespace jvmti {
  class MemFile {
  public:
    MemFile(const char *path) {
      _path = path;
      Open();
    }

    void Append(const char *data, int length);

    ~MemFile() {
      Close();
    }

  private:
    const char *_path = nullptr;
    int _fd = -1;
    // 内存映射出的起始地址
    int *mem_buf = nullptr;
    // buffer 偏移
    int buf_offset = 0;
    // 内存映射大小指定为页大小的整数倍, 首次指定为一个页大小
    int buf_size = 0;

    // 打开 mmap 映射
    bool Open();

    // 扩展文件大小到指定尺寸，最好是 page size 整数倍
    bool Resize(int resize);

    // 关闭 mmap 映射
    void Close();
  };
}

#endif //DAILY_WORK_MEM_FILE_H

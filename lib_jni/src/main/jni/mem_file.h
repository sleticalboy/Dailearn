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

  /**
   * 写入数据
   * @param data 数据
   * @param length 数据长度
   */
  void Append(const char *data, int length);

  ~MemFile() {
    Close();
  }

private:
  // 文件路径
  const char *_path = nullptr;
  // 文件打开后的文件描述符
  int _fd = -1;
  // 内存映射出的起始地址
  int *mem_buf = nullptr;
  // 内存地址偏移
  int buf_offset = 0;
  // 内存映射大小指定为页大小的整数倍, 首次指定为一个页大小
  int buf_size = 0;

  /**
   * 打开文件并做 mmap 映射
   * @return 是否成功
   */
  bool Open();

  /**
   * 扩展文件大小到指定尺寸，最好是 page size 整数倍
   * @param resize 期望扩展大小
   * @return 是否成功
   */
  bool Resize(int resize);

  /**
   * 关闭文件并做 munmap 映射
   */
  void Close();
};
} // namespace jvmti

#endif //DAILY_WORK_MEM_FILE_H

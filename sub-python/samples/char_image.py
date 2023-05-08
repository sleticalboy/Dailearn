# -*- coding=utf-8 -*-

import argparse

from PIL import Image
from com.binlee.python.util import file_util

# 命令行输入参数处理
parser = argparse.ArgumentParser()

parser.add_argument('file')  # 输入文件
parser.add_argument('--width', type=int, default=80)  # 输出字符画宽
parser.add_argument('--height', type=int, default=80)  # 输出字符画高

# 获取参数
args = parser.parse_args()

IMG = args.file
WIDTH = args.width
HEIGHT = args.height

ascii_char = list("$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\|()1{}[]?-_+~<>i!lI;:,\"^`'. ")


# 将256灰度映射到70个字符上
def get_char(r, g, b, alpha=256):
    if alpha == 0:
        return ' '
    gray = int(0.2126 * r + 0.7152 * g + 0.0722 * b)

    unit = (256.0 + 1) / len(ascii_char)
    return ascii_char[int(gray / unit)]


if __name__ == '__main__':
    # 打开图片并调整到指定大小
    im = Image.open(IMG)
    print(f'w: {im.width}, h: {im.height}, fmt: {im.format}, fmt_desc: {im.format_description}')
    im = im.resize((WIDTH, HEIGHT), Image.NEAREST)

    # 图片转成的字符画载体
    lines: list[str] = []
    for i in range(HEIGHT):
        txt = ''
        for j in range(WIDTH):
            # 像素点映射成字符
            txt += get_char(*im.getpixel((j, i)))
        lines.append(txt + '\n')
    # 输出字符画
    print(''.join(lines))

    # 字符画输出到文件
    parent = file_util.create_out_dir(__file__)
    name, ext = file_util.split_filename(IMG)
    with open(f"{parent}/{name}-output.txt", 'w') as f:
        f.writelines(lines)

    im.save(f'{parent}/{name}-{WIDTH}x{HEIGHT}{ext}')
    im.close()

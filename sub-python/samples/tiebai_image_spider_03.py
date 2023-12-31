import re
import sys

from com.binlee.python.util import img_util
from com.binlee.python.util import web_util
from com.binlee.python.util import file_util


def parse_image_urls(html: str) -> list[str]:
    image_urls: list[str] = []
    pattern = re.compile(r'src="(.+?\.jpg)" pic_ext')
    temp_urls: list[str] = re.findall(pattern=pattern, string=html)
    # 过滤出非法的 url
    url_pattern = re.compile(r'http.?://.+?')
    for url in temp_urls:
        if re.match(url_pattern, url):
            image_urls.append(url)
    return image_urls


def get_images(root_url: str):
    # 获取网页内容
    html = web_util.get_content(root_url)
    # print(f"get_images() {html}")
    # 解析 src 标签获取所有图片 url
    image_urls: list[str] = parse_image_urls(html)
    # 保存图片
    saver = img_util.ImageSaver(originals=image_urls,
                                output_dir=file_util.create_out_dir(__file__, 'images'))
    saver.save()


if __name__ == '__main__':
    print(f"args: {sys.argv}")

    get_images(sys.argv[1])

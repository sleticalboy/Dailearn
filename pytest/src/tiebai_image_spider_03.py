import os
import re
import sys

import requests

from util import img_util


def get_web_page(url: str) -> str:
    with requests.request(method="get", url=url) as r:
        if r.status_code != 200:
            return ""
        if r.headers['Content-Type'].__contains__("text/"):
            return r.text


def parse_image_urls(html: str) -> list[str]:
    pattern = re.compile(r'src="http.//(.+?\.jpg)" pic_ext')
    image_urls: list[str] = re.findall(pattern=pattern, string=html)
    # pattern = re.compile(r'src=\\&quot;(.+?\.jpg)\\&quot; pic_ext')
    # urls: list[str] = re.findall(pattern=pattern, string=html)
    # for url in urls:
    #     image_urls.append(url.replace('\/', '/'))
    # for url in image_urls:
    #     print(f"image url: {url}")
    return image_urls


def get_images(root_url: str):
    # 获取网页内容
    html = get_web_page(root_url)
    # print(f"get_images() {html}")
    # 解析 src 标签获取所有图片 url
    image_urls: list[str] = parse_image_urls(html)

    # 文件夹名称
    filename: str = os.path.basename(__file__)
    target_dir = f"{os.getcwd()}/../out/images/{filename[:filename.rfind('.')]}"
    print(f"get_images() target dir: {target_dir}")
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)
    # 保存图片
    img_util.save_images(image_urls, target_dir)


if __name__ == '__main__':
    print(f"args: {sys.argv}")

    get_images(sys.argv[1])

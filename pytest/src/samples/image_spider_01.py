import os
import sys
import urllib.parse

import jsonpath
import requests

from src.com.binlee.python.util import img_util
from src.com.binlee.python.util import file_util


def get_images(args: list[str]):
    url = 'https://www.duitang.com/napi/blog/list/by_search/?kw={}&start={}'
    kw = urllib.parse.quote(args[0])
    start = args[1]
    url = url.format(kw, start)
    print(f"get_images() url: {url}")

    response = requests.get(url=url)
    print(f"get_images() response header: {response.headers}")
    if not response.headers["Content-Type"].__contains__("application/json"):
        response.close()
        return
    image_urls: list[str] = jsonpath.jsonpath(response.json(), "$..path")
    response.close()

    img_util.save_images(image_urls, file_util.create_dir(f"{os.getcwd()}/../out/images", __file__))


if __name__ == '__main__':
    print(f"args: {sys.argv}")
    # 获取图片
    get_images(sys.argv[1:])

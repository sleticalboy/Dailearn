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

    with requests.get(url=url) as r:
        if r.headers["Content-Type"].find("application/json") > 0:
            image_urls: list[str] = jsonpath.jsonpath(r.json(), "$..path")
            saver = img_util.ImageSaver(originals=image_urls,
                                        output_dir=file_util.create_out_dir(__file__, 'images'))
            saver.save()


if __name__ == '__main__':
    print(f"args: {sys.argv}")
    # 获取图片
    get_images(sys.argv[1:])

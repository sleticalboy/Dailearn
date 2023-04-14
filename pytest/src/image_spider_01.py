import os
import sys
import urllib.parse

import jsonpath
import requests


def save_images(urls: list[str]):
    path = f"{os.getcwd()}/../out/images"
    if not os.path.exists(path):
        os.mkdir(path)
    for url in urls:
        # 文件名
        file = f"{path}/{url[url.rindex('/') + 1:]}"
        r = requests.get(url)
        with open(file=file, mode='wb') as f:
            f.write(r.content)
            f.close()
        print(f"save to '{file}', size: {r.headers['Content-Length']}")
        r.close()


def get_images(args: list[str]):
    url = 'https://www.duitang.com/napi/blog/list/by_search/?kw={}&start={}'
    kw = urllib.parse.quote(args[0])
    start = args[1]
    url = url.format(kw, start)
    print(f"get_images() url: {url}")

    response = requests.get(url=url)
    print(f"get_images() response header: {response.headers}")
    if response.headers["Content-Type"].__contains__("application/json"):
        image_urls: list[str] = jsonpath.jsonpath(response.json(), "$..path")
        response.close()
        save_images(image_urls)


if __name__ == '__main__':
    print(f"args: {sys.argv}")
    # 获取图片
    get_images(sys.argv[1:])

import json
import os
import re
import sys
import urllib.parse

from src.com.binlee.python.util import img_util
from src.com.binlee.python.util import web_util
from src.com.binlee.python.util import file_util

def get_images(args: list[str]):
    # 不打印 https 警告
    web_util.disable_https_warnings()
    # 多请求一次是为了拿到 baiduid
    baiduid = web_util.get_cookie('https://image.baidu.com/search/index', 'BAIDUID')
    if not baiduid:
        return

    # https://image.baidu.com/search/index?word={encKw}&oq={encKw}&tn=baiduimage&ct=201326592
    # 关键词 (url encode)
    kw = urllib.parse.quote(args[0])
    # 分页标识
    page_num = 0
    url = f"https://image.baidu.com/search/index?word={kw}&oq={kw}&tn=baiduimage&ct=201326592&pn={page_num}"
    print(f"get_images() url: {url}, kw: {args[0]}, page: {page_num}")
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/112.0.0.0 Safari/537.36",
        # 最重要的是 BAIDUID，这个一定要有，否则请求不到数据
        # 'Cookie': 'BAIDUID=74EC180410B44D03FABD63B2E6333B15:FG=1',
        'Cookie': f'BAIDUID={baiduid}',
        'Host': 'image.baidu.com',
    }
    html = web_util.get_page_content(url=url, headers=headers)
    if not html:
        return
    # 图片数据（json）在 js 中，使用正则截取出来然后取出单个图片数据分析
    # 'imgData', (.+?)}]); -> json -> ['data'] -> [] -> [0] -> json
    pattern = re.compile(r"\('imgData', (.+?)\n\)")
    result: list[str] = re.findall(pattern, html)
    if not result or len(result) == 0:
        return
    data = json.loads(result[0])['data']
    print(f"get_images() page: {page_num} size: {len(data)}")

    thumbnails: list[str] = []
    large_urls: list[str] = []
    raw_sources: list[str] = []
    for item in data:
        if len(item) == 0:
            continue
        thumbnails.append(item['thumbURL'])
        large_urls.append(item['replaceUrl'][0]['ObjURL'])
        raw_sources.append(item['replaceUrl'][0]['FromURL'])

    print(f"get_images() thumbnail: {len(thumbnails)}, large: {len(large_urls)}, raw: {len(raw_sources)}")
    # https://image.baidu.com/search/index?tn=baiduimage&ipn=r&ct=201326592&cl=2&lm=-1&st=-1&sf=1&fmq=&pv=&ic=0&
    # nc=1&se=1&showtab=0&fb=0&face=0&istype=2&ie=utf-8&fm=index&pos=history&word=%E7%BE%8E%E5%A5%B3
    img_util.save_images(thumbnails, file_util.create_dir(f"{os.getcwd()}/../out/images", __file__))


if __name__ == '__main__':
    print(f"args: {sys.argv}")
    # 获取图片
    get_images(sys.argv[1:])

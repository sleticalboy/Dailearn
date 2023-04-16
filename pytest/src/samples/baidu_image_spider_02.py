import json
import os
import re
import sys
import urllib.parse

from src.com.binlee.python.util import file_util
from src.com.binlee.python.util import img_util
from src.com.binlee.python.util import web_util


def parse_one_page(keyword: str, page_num: int, baiduid: str) -> int:
    url = f"https://image.baidu.com/search/index?word={keyword}&oq={keyword}&tn=baiduimage&ct=201326592&pn={page_num}"
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
        return -1
    # 图片数据（json）在 js 中，使用正则截取出来然后取出单个图片数据分析
    # 'imgData', (.+?)}]); -> json -> ['data'] -> [] -> [0] -> json
    pattern = re.compile(r"\('imgData', (.+?)\n\)")
    result: list[str] = re.findall(pattern, html)
    if not result or len(result) == 0:
        return -1
    data = json.loads(result[0])['data']

    saver = img_util.ImageSaver(file_util.create_dir(f"{os.getcwd()}/../out/images", __file__))
    for item in data:
        if len(item) == 0:
            continue
        saver.add_thumbnail(item['thumbURL'])
        saver.add_original(item['objURL'])
        saver.add_sources(item['replaceUrl'][0]['FromURL'])
    saver.save()
    return saver.size()


def get_images(keyword: str, total: int):
    # 不打印 https 警告
    web_util.disable_https_warnings()
    # 多请求一次是为了拿到 baiduid
    baiduid = web_util.get_cookie('https://image.baidu.com/search/index', 'BAIDUID')
    if not baiduid:
        return

    # https://image.baidu.com/search/index?word={encKw}&oq={encKw}&tn=baiduimage&ct=201326592&pn={pn}
    keyword = urllib.parse.quote(keyword)
    # 分页标识
    page_num = 0
    # 图片张数
    counter = 0

    while True:
        n = parse_one_page(keyword, page_num, baiduid)
        if n > 0:
            counter += n
        if counter >= total:
            break
        page_num += 1


if __name__ == '__main__':
    kw = input("请输入关键词：")
    num = input("请输入图片张数：")
    if not num.isdigit():
        print(f"格式异常，请输入合法数字：{num}", file=sys.stderr)
        exit(1)
    # 获取图片
    get_images(kw, int(num))
    # 测试代码
    # get_images("美女", 1)

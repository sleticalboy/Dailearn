import json
import os
import re
import sys
import urllib.parse

from src.com.binlee.python.util import file_util
from src.com.binlee.python.util import img_util
from src.com.binlee.python.util import web_util


def parse_one_page(kw: str, page_num: int, baiduid: str, saver: img_util.ImageSaver):
    # 首次查询时返回是 html 数据
    url = (
        f"https://image.baidu.com/search/index?word={kw}&oq={kw}&&pn={page_num}&tn=baiduimage&ct=201326592")
    # 滚动页面时触发分页查询返回数据是 json 数据
    query_url = (
        f"https://image.baidu.com/search/acjson?word={kw}&queryWord={kw}&pn={page_num}&tn=resultjson_com&ct=201326592")
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/112.0.0.0 Safari/537.36",
        # 最重要的是 BAIDUID，这个一定要有，否则请求不到数据
        # 'Cookie': 'BAIDUID=74EC180410B44D03FABD63B2E6333B15:FG=1',
        'Cookie': f'BAIDUID={baiduid}',
        'Host': 'image.baidu.com',
    }
    content, mime = web_util.get_content(url=url if page_num == 0 else query_url, headers=headers)
    if not content or not mime:
        return
    data = {}
    if mime.find('html') >= 0:
        # 图片数据（json）在 js 中，使用正则截取出来然后取出单个图片数据分析
        # 'imgData', (.+?)}]); -> json -> ['data'] -> [] -> [0] -> json
        pattern = re.compile(r"\('imgData', (.+?)\n\)")
        result: list[str] = re.findall(pattern, content)
        if not result or len(result) == 0:
            return
        data = json.loads(result[0])['data']
    elif mime.find('json') >= 0:
        data = json.loads(content)['data']

    if len(data) == 0:
        return

    before = saver.size()
    for item in data:
        if len(item) == 0:
            continue
        image = img_util.Image(thumbnail=item['thumbURL'],
                               original=item['replaceUrl'][0]['ObjURL'],
                               source=item['replaceUrl'][0]['FromURL'])
        saver.add_image(image)
        # print(f"add: \n{image}\n")
    print(f"parse_one_page() pn({page_num}), data({len(data)}), saver({before}, {saver.size()})")


def get_images(keyword: str, total: int):
    # 不打印 https 警告
    web_util.disable_https_warnings()
    # 多请求一次是为了拿到 baiduid
    baiduid = web_util.get_cookie('https://image.baidu.com/search/index', 'BAIDUID')
    if not baiduid:
        return

    # https://image.baidu.com/search/index?word={encKw}&oq={encKw}&tn=baiduimage&ct=201326592&pn={pn}
    keyword = urllib.parse.quote(keyword)
    # 分页标识，每次以 30 为等差数列
    page_num = 0
    # 请求次数
    index = 0

    saver = img_util.ImageSaver(file_util.create_dir(f"{os.getcwd()}/../out/images", __file__))
    while index < total:
        parse_one_page(keyword, page_num, baiduid, saver)
        page_num = page_num + 30
        index = index + 1
    saved = saver.save()
    print(f"get_images() saved: {saved}, total: {total * 30}")


if __name__ == '__main__':
    print("爬取百度图片，理论上每页（每次请求）返回 30 张图片，但测试发现会有重复图片")
    key_word_ = input("请输入关键词：")
    total_ = input("请输入页数：")
    if not total_.isdigit():
        print(f"格式异常，请输入合法数字：{total_}", file=sys.stderr)
        exit(1)
    # 获取图片
    get_images(key_word_, int(total_))

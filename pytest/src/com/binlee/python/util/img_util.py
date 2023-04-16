
import subprocess
import urllib.parse

import requests


def parse_url_and_name(url: str) -> (str, str):
    """
    解析真正的图片 url 和文件名
    @param url: 原始 url
    @return: 图片真正 url 和文件名
    """
    dec_url = urllib.parse.unquote(url)
    index = dec_url.find('src=')
    if index > 0:
        dec_url = dec_url[index + 4:]
    result = urllib.parse.urlparse(dec_url)

    name = ''
    if result.path:
        # '/uploads/blog/202111/17/20211117092914_579a7.thumb.1000_0.jpeg&...'
        # print(f"url.path: {result.path}")
        start = -1
        end = result.path.find('&')
        if end > 0:
            start = result.path[:end].rfind('/')
        if start >= 0 and end >= 0:
            name = result.path[start + 1: end]
        if not name:
            name = result.path[result.path.rfind('/') + 1:]
    # 长度不能超过 256
    if len(name) > 200:
        name = name[:200]
    # 后缀名
    if result.query:
        # 'imageView2/2/w/1080/format/jpg&...'
        # print(f"url.query: {result.query}")
        start = result.query.find('format/')
        end = result.query.find('&')
        if start > 0 and end > 0:
            name = f"{name}.{result.query[start + 7:end]}"
    return f"{result.scheme}://{result.netloc}{result.path}", name


def try_curl(url: str, file: str):
    with subprocess.Popen(f"curl {url} -o {file} -v", shell=True, text=True,
                          stdout=subprocess.PIPE) as p:
        p.wait()
        p.terminate()


def try_httpx(url: str, file: str):
    import logging
    import httpx
    logging.basicConfig(
        format="%(levelname)s [%(asctime)s] %(name)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
        level=logging.DEBUG
    )
    with httpx.Client(http2=True, verify=False) as client:
        r = client.get(url=url)
        print(f"try_httpx() {r.http_version} {r.headers}")
        with open(file=file, mode='wb') as f:
            f.write(r.content)


def save_image(url: str, output_dir: str):
    # 文件名
    parsed_url, name = parse_url_and_name(url)
    with requests.get(url, allow_redirects=True) as r:
        mime: str = r.headers.get('Content-Type')
        # 兜底
        if name.rfind('.') < 0:
            name = f"{name}.{mime[mime.rfind('/') + 1:]}"
        file = f"{output_dir}/{name}"
        if r.status_code == 403 or mime.find('text/') >= 0:
            # print(f"invalid image url: {url} -> {r.status_code}\n{r.headers}\n")
            # 这里失败的都是渐进式 jpeg 格式的图片，这种格式的图片 server 用的是 http2 协议进行推送用于加速客户端下载
            # 测试用 curl 是可以下载成功的，python 原生并不支持 http2，需要用第三方库 httpx 进行下载
            # pip install httpx # 支持 http1
            # pip install httpx[http2] # 支持 http2
            try_curl(url, file)
            # 测试 httpx 也失败，暂时没有解决
            # try_httpx(url, file)
            return
        # size: str = r.headers.get('Content-Length')
        with open(file=file, mode='wb') as f:
            f.write(r.content)
        # print(f"save to '{file}'\n")
        # print(f"{urllib.parse.unquote(url)}\n{parsed_url}\nmime: {mime}, size: {size}, name: {name}")


class ImageSaver:
    # 缩略图
    __thumbnails: list[str] = []
    # 原图
    __originals: list[str] = []
    # 来源
    __sources: list[str] = []
    # 保存目录
    __output_dir = str | None

    def __init__(self, output_dir: str, originals=None, thumbnails=None, sources=None):
        if sources is None:
            sources = []
        if thumbnails is None:
            thumbnails = []
        if originals is None:
            originals = []
        self.__thumbnails = thumbnails
        self.__originals = originals
        self.__sources = sources
        self.__output_dir = output_dir

    def add_thumbnail(self, url: str):
        self.__thumbnails.append(url)

    def add_original(self, url: str):
        self.__originals.append(url)

    def add_sources(self, url: str):
        self.__sources.append(url)

    def size(self):
        return len(self.__originals)

    def save(self, original: bool = True, thumbnails: bool = False):
        if original:
            for url in self.__originals:
                save_image(url, self.__output_dir)
        if thumbnails:
            for url in self.__thumbnails:
                save_image(url, self.__output_dir)

    def dump(self) -> str:
        return f"ImageSaver() size: {self.size()}"

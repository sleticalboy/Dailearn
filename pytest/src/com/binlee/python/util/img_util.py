import uuid

import requests


def try_httpx(url: str, output_dir: str):
    import httpx
    # import logging
    # logging.basicConfig(
    #     format="%(levelname)s [%(asctime)s] %(name)s - %(message)s",
    #     datefmt="%Y-%m-%d %H:%M:%S",
    #     level=logging.FATAL
    # )
    # 一定要加 ua，否则使用 httpx 内置的 ua，服务器会返回 403
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/112.0.0.0 Safari/537.36",
    }
    # 这里要优化一下，一个 host 对应一个 client
    with httpx.Client(http2=True, headers=headers) as client:
        r = client.request(method='get', url=url, headers=headers, timeout=3)
        # print(f"try_httpx() {r.http_version} {r.headers}")
        mime: str = r.headers.get('Content-type')
        # 过滤掉请求失败和非图片类型返回
        if r.status_code != 200 or mime.find('image/') < 0:
            print(f"try_httpx() failed: {url}, mime: {mime}, status: {r.status_code}")
            return False
        size: str = r.headers.get('Content-Length')
        file = f"{output_dir}/{uuid.uuid4()}.{mime[mime.rfind('/') + 1:]}"
        with open(file=file, mode='wb') as f:
            f.write(r.content)
        # print(f"try_httpx() mime: {mime}, size: {size}\n{url}\n{file}")
        return True


def save_image(url: str, output_dir: str):
    # 这里可以完全使用 httpx 库替换掉系统的 requests 库
    try:
        with requests.request(method='get', url=url, timeout=2) as r:
            mime: str = r.headers.get('Content-Type')
            if r.status_code == 403 or mime.find('text/') >= 0:
                # print(f"invalid image url: {url} -> {r.status_code}\n{r.headers}\n")
                # 这里失败的都是渐进式 jpeg 格式的图片，这种格式的图片 server 用的是 http2 协议进行推送用于加速客户端下载
                # 测试用 curl/wget 都是可以下载成功的，python 原生并不支持 http2，需要用第三方库 httpx 进行下载
                # pip install httpx # 支持 http1
                # pip install httpx[http2] # 支持 http2
                return try_httpx(url, output_dir)
            file = f"{output_dir}/{uuid.uuid4()}.{mime[mime.rfind('/') + 1:]}"
            size: str = r.headers.get('Content-Length')
            with open(file=file, mode='wb') as f:
                f.write(r.content)
            # print(f"save_image() mime: {mime}, size: {size}\n{url}\n{file}")
            return True
    except Exception as e:
        print(f"save_image() failed: {url}, {e}")
        return False


class Image:

    def __init__(self, thumbnail: str = None, original: str = None, source: str = None, fmt: str = '') -> None:
        # 缩略图
        self.thumbnail = thumbnail
        # 原图
        self.original = original
        # 来源
        self.source = source
        # 图片格式
        self.fmt = fmt

    def __hash__(self) -> int:
        return hash(self.original)

    def __eq__(self, other) -> bool:
        if isinstance(other, self.__class__):
            return self.original == other.original
        return False

    def __str__(self):
        return "{}\n{}\n{}".format(self.thumbnail, self.original, self.source)


class ImageSaver:

    # 保存目录
    __output_dir = str | None
    # 要保存的图片集合
    __images: set[Image] = set()

    def __init__(self, output_dir: str, originals=None):
        self.__output_dir = output_dir
        if originals:
            for url in originals:
                self.__images.add(Image(original=url))

    def add_image(self, image: Image):
        self.__images.add(image)

    def size(self):
        return len(self.__images)

    def save(self, original: bool = True, thumbnails: bool = False, counter: int = -1) -> int:
        saved = 0
        for image in self.__images:
            if original and image.original:
                if save_image(image.original, self.__output_dir):
                    saved = saved + 1
                    if saved == counter:
                        break
            if thumbnails and image.thumbnail:
                if save_image(image.thumbnail, self.__output_dir):
                    saved = saved + 1
                    if saved == counter:
                        break
        return saved

    def reset(self):
        self.__images.clear()

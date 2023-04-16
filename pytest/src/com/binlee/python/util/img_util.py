import requests


def save_images(urls: list[str], target_dir: str):
    if not urls or len(urls) == 0:
        return
    for url in urls:
        # 文件名
        name = url[url.rfind('/') + 1:]
        with requests.get(url) as r:
            if name.rfind('.') < 0:
                mime: str = r.headers['Content-Type']
                name = f"{name}.{mime[mime.rfind('/') + 1:]}"
            file = f"{target_dir}/{name}"
            with open(file=file, mode='wb') as f:
                f.write(r.content)
                f.close()
            print(f"save to '{file}', size: {r.headers['Content-Length']}")
            r.close()

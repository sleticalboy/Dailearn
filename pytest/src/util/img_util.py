import requests


def save_images(urls: list[str], target_dir: str):
    if not urls or len(urls) == 0:
        return
    for url in urls:
        # 文件名
        file = f"{target_dir}/{url[url.rindex('/') + 1:]}"
        with requests.get(url) as r:
            with open(file=file, mode='wb') as f:
                f.write(r.content)
                f.close()
            print(f"save to '{file}', size: {r.headers['Content-Length']}")
            r.close()

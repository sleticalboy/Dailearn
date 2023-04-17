import requests
import urllib3


def disable_https_warnings():
    """
    禁止打印 https 警告
    """
    urllib3.disable_warnings()


def get_cookie(url: str, name: str) -> str:
    """
    获取 url 响应头中的 cookie
    @param url: 目标 url
    @param name: cookie 的名字
    @return: name 对应 cookie 的 value
    """
    with requests.get(url, verify=False) as r:
        return r.cookies.__getitem__(name)


def get_content(url: str, headers: dict[str, str] = None) -> (str, str):
    """
    获取 url 响应体内容
    @param url: 目标 url
    @param headers: 请求头
    @return: 网页内容和内容格式
    """
    with requests.request(method="GET", url=url, headers=headers, verify=False) as r:
        mime = r.headers.get('Content-Type')
        if r.status_code == 200 and (mime.find("text/") >= 0 or mime.find('/json')):
            return r.text, mime
    return '', ''

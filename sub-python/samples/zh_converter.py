import requests


def fetch_book():
    text = '緣起首回開宗明義閒評兒女英雄 引古證今演說人情天理'
    url = 'https://www.gutenberg.org/cache/epub/25327/pg25327.txt'
    with requests.get(url) as res:
        if res.ok:
            text = res.text
            text = text.replace('　', ' ')
            start = '*** START OF THE PROJECT GUTENBERG EBOOK'
            end = '*** END OF THE PROJECT GUTENBERG EBOOK'
            s = text.find(start)
            e = text.rfind(end)
            return text[s + len(start):e - len(end)].strip('\r\n')
    return text


def run_converter():
    url = 'https://tool.lu/zhconvert/ajax.html'
    payload = {
        'code': fetch_book(),
        'operate': 'zh-hans',
    }
    headers = {
        'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Origin': 'https://tool.lu',
        'Referer': 'https://tool.lu/zhconvert/',
    }
    with requests.post(url, data=payload, headers=headers) as res:
        print(res.status_code)
        print(res.reason)
        print(res.headers)
        text: str = res.json()['text']
        print(text)
        pass
    pass


if __name__ == '__main__':
    run_converter()

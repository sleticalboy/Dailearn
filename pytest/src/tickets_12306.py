import re
import sys

import requests
import urllib3


class QueryArgs:
    from_city: str
    to_city: str
    date_str: str
    options: str = ""

    def __init__(self, arr: list):
        if len(arr) > 3:
            self.options = arr[0][1:]
            self.from_city = arr[1]
            self.to_city = arr[2]
            self.date_str = arr[3]
        else:
            self.from_city = arr[0]
            self.to_city = arr[1]
            self.date_str = arr[2]


def get_stations() -> dict:
    urllib3.disable_warnings()
    # 从 12306 查询所有站点信息
    # 这里可以做一层缓存，不用每次都从网络获取
    url = 'https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.9119'
    response = requests.request(method="GET", url=url, verify=False)
    pattern = u"([\u4e00-\u9fa5]+)\|([A-Z]+)"
    # 结果映射到 map 中
    stations = dict(re.findall(pattern=pattern, string=response.text))
    response.close()
    return stations


def get_ticket_info(args: QueryArgs, stations: dict):
    cookies = requests.cookies.RequestsCookieJar()
    # 通过访问url_init，存储cookie信息
    url_init = 'https://kyfw.12306.cn/otn/leftTicket/init'
    urllib3.disable_warnings()
    response = requests.request(method="GET", url=url_init, verify=False)
    print(f"get_ticket_info() init headers: {response.headers}")
    print(f"get_ticket_info() init cookies: {response.cookies}")
    if len(response.cookies) != 0:
        cookies.update(response.cookies)
    response.close()
    # 开始查询车票信息，需要携带cookie及header信息，否则访问失败
    url_query = f"https://kyfw.12306.cn/otn/leftTicket/query?" \
                f"leftTicketDTO.train_date={args.date_str}&" \
                f"leftTicketDTO.from_station={stations.get(args.from_city)}&" \
                f"leftTicketDTO.to_station={stations.get(args.to_city)}&purpose_codes=ADULT"
    print(f"get_ticket_info() query url: {url_query}")
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/112.0.0.0 Safari/537.36"
    }
    response = requests.request(method="GET", url=url_query, verify=False,
                                headers=headers, cookies=cookies)
    print(f"get_ticket_info() query: {response.headers}")
    response.close()


def query_tickets(arr: list):
    args = QueryArgs(arr=arr)
    print(f"option: {args.options} {args.from_city} -> {args.to_city} at {args.date_str}")

    stations = get_stations()
    print(f"stations: {len(stations)}")
    # 根据参数查询具体车站信息
    get_ticket_info(args, stations)


def usage():
    print(f"""命令行火车票查看器
Usage:
    {sys.argv[0]} [-gdtkz] <from> <to> <date>
Options:
    -h,--help   显示帮助菜单
    -g          高铁
    -d          动车
    -t          特快
    -k          快速
    -z          直达
Example:
    {sys.argv[0]} 北京 上海 2016-10-10
    {sys.argv[0]} -dg 成都 南京 2016-10-10
""")


if __name__ == '__main__':
    if len(sys.argv) < 4:
        exit(usage())
    else:
        query_tickets(sys.argv[1:])

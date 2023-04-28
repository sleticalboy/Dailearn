import json
import os
import random
import re
import sys
import time
import urllib.parse

import requests
import urllib3
from colorama import Fore
from prettytable import PrettyTable

from src.com.binlee.python.util import file_util


class QueryArgs:
    __from_stations = "北京 天津 秦皇岛 石家庄 大连 长春 上海 青岛 日照".split()
    __to_stations = "郑州 杭州 广州 武汉 长沙 深圳 成都 拉萨 兰州 日喀则".split()

    def __init__(self) -> None:
        self.from_ = ''
        self.to_ = ''
        self.date_ = ''
        self.__random_init__()
        self.type_ = 'GCDZTK'
        # 出发时间和行程历时是互斥的
        self.leave_first_ = True
        self.leave_last_ = False
        self.time_least_ = False
        # 是否退出
        self.quit_ = False
        # 是否忽略
        self.ignore_ = False
        # 强制刷新
        self.force_refresh_ = False

    def __random_init__(self):
        self.from_ = random.choice(self.__from_stations)
        self.to_ = random.choice(self.__to_stations)
        # 未来 15 天随机时间
        start = int(time.time() / (24 * 3600))
        random_time = random.randint(start, start + 15) * 24 * 3600
        self.date_ = time.strftime("%Y-%m-%d", time.localtime(random_time))

    def set_from(self, from_: str):
        self.force_refresh_ |= from_ != self.from_
        self.from_ = from_

    def set_to(self, to_: str):
        self.force_refresh_ |= to_ != self.to_
        self.to_ = to_

    def set_date(self, date_: str):
        self.force_refresh_ |= date_ != self.date_
        self.date_ = date_

    def set_leave_first(self):
        self.leave_first_ = True
        self.leave_last_ = False
        self.time_least_ = False

    def set_leave_last(self):
        self.leave_last_ = True
        self.leave_first_ = False
        self.time_least_ = False

    def set_time_least(self):
        self.time_least_ = True
        self.leave_first_ = False
        self.leave_last_ = False

    def reset(self):
        self.ignore_ = False
        self.force_refresh_ = False

    def dump(self) -> str:
        options = f"'{self.from_}' -> '{self.to_}' on '{self.date_}'"
        if self.leave_first_ and self.time_least_:
            options += ' 最早出发 历时最短'
        elif self.leave_last_ and self.time_least_:
            options += ' 最晚出发 历时最短'
        elif self.time_least_:
            options += ' 历时最短'
        return options


class Row:
    __raw: list[str] | None

    def __init__(self, raw: list[str]) -> None:
        self.__raw = raw
        # 车次类型：D动车，G高铁，T特快，C城际
        self.type_ = raw[0][0]
        # 发车时间
        self.start_ = raw[5]
        # 行程历时
        self.cost_ = raw[7]

    def data(self):
        return self.__raw


class Rows:
    __rows: list[Row] | None

    def __init__(self, row_data: list[str], station_map: dict[str, str]) -> None:
        self.__rows = []
        self.__parse__(row_data, station_map)

    def __parse__(self, row_data: list[str], station_map: dict[str, str]):
        for raw in row_data:
            # 字符串以 '|' 分割后数组长度为 52
            # 0    1  2            3     4   5   6   7    8     9    10      12
            # ...|预订|24000C255102|C2551|VNP|YKP|VNP|YKP|06:00|06:56|00:56|Y|...
            # 3车次 4始发站 5终点站 6经过站 7经过站 8发车 9到达 10时长
            #  13      14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32  34     35  36 37
            # |20230422|3|P2|01|03|-1| 0|--|--|--|--|--|--|--|--|--|--|有|无|-5||90M0O0|9MO| 1| 1|
            # 13日期 23软卧 26无座 28硬卧 29硬座 30二等座 31一等座 36高级软卧

            segments: list[str] = raw.split('|')
            # print(f"{len(segments)} -> {raw[raw.index('|') + 1:]}")
            # t_map: dict[str, str] = {}
            # for s in segments[4:8]:
            #     t_map[s] = station_map.get(s)
            # print(f"mapped: {t_map}")

            self.__rows.append(Row([
                # 车次
                segments[3],
                # 始发
                station_map[segments[4]],
                # 出发站
                Fore.LIGHTGREEN_EX + station_map[segments[6]] + Fore.RESET,
                # 出发时间
                Fore.LIGHTGREEN_EX + segments[8] + Fore.RESET,
                # 到达站
                Fore.LIGHTRED_EX + station_map[segments[7]] + Fore.RESET,
                # 到达时间
                Fore.LIGHTRED_EX + segments[9] + Fore.RESET,
                # 终点
                station_map[segments[5]],
                # 历时
                segments[10],
                # 一等座
                pretty_text(segments[31]),
                # 二等座
                pretty_text(segments[30]),
                # 高级软卧
                pretty_text(segments[36]),
                # 软卧
                pretty_text(segments[23]),
                # 硬卧
                pretty_text(segments[28]),
                # 硬座
                pretty_text(segments[29]),
                # 无座
                pretty_text(segments[26])
            ]))

    def size(self):
        return len(self.__rows)

    def sort(self, args: QueryArgs) -> list[list[str]]:
        temp: list[Row] = []
        # 解析发车类型
        if args.type_ != '':
            for row in self.__rows:
                # print(f"sort() r.t: {row.type_}, a.t: {args.type_}")
                for t in row.type_:
                    if t in args.type_:
                        temp.append(row)
        # 最早出发
        if args.leave_first_:
            temp.sort(key=lambda r: r.start_, reverse=False)
        # 最晚出发
        elif args.leave_last_:
            temp.sort(key=lambda r: r.start_, reverse=True)
        # 历时最短
        elif args.time_least_:
            temp.sort(key=lambda r: r.cost_, reverse=False)

        output: list[list[str]] = []
        for row in temp:
            output.append(row.data())
        return output


class TicketTask:
    __station_map__: dict[str, str]
    __pt_rows__: Rows | None

    def __init__(self) -> None:
        self.__station_map__ = {}
        self.__pt_rows__ = None

    def execute(self, args: QueryArgs):
        print(f"execute() {args.dump()}")

        if self.__station_map__ is None or len(self.__station_map__) == 0:
            self.__station_map__ = get_stations()
            print(f"execute() stations: {len(self.__station_map__)}")

        if args.force_refresh_ or self.__pt_rows__ is None or self.__pt_rows__.size() == 0:
            # 根据参数查询具体车站信息
            text, is_json = get_ticket_info(args, self.__station_map__)
            if not is_json:
                print("execute() response content is not json!")
                return
            # python 3.9+ 中的泛型
            result: list[str] = json.loads(text)["data"]["result"]
            self.__pt_rows__ = Rows(result, self.__station_map__)

        # 解析数据并填充表格
        header = '车次 始发站 出发站 出发时间 到达站 到达时间 终点站 行程历时 一等座 二等座 高级软卧 软卧 硬卧 硬座 无座'.split()
        pt = PrettyTable(field_names=header, min_width=6, align='c', valign='b', border=True, header_style='cap')
        # pt.set_style(prettytable.DEFAULT)
        # 根据参数对数据进行排序
        pt.add_rows(self.__pt_rows__.sort(args))
        print(pt)


def get_cached_stations() -> dict:
    path = f"{file_util.create_out_dir(__file__)}/stations.json"
    print(f"get_cached_stations() cache: {path}")
    if os.path.exists(path) and os.path.isfile(path):
        with open(file=path, mode='r', encoding='utf-8') as f:
            return json.load(f)
    return dict()


def set_cached_stations(stations: dict):
    path = file_util.create_out_dir(__file__)
    if not os.path.exists(path):
        os.mkdir(path)
    path = f"{path}/stations.json"
    print(f"set_cached_stations() cache: {path}")
    with open(file=path, mode='x', encoding='utf-8') as f:
        f.write(json.dumps(stations, ensure_ascii=False))


def get_stations() -> dict[str, str]:
    # 这里可以做一层缓存，不用每次都从网络获取
    stations = get_cached_stations()
    if len(stations) > 0:
        return stations

    # 从 12306 查询所有站点信息
    url = 'https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.9119'
    with requests.request(method="GET", url=url, verify=False) as r:
        # 结果映射到 map 中
        pairs = re.compile(r"([\u4e00-\u9fa5]+)\|([A-Z]+)").findall(r.text)
    stations: dict[str, str] = {}
    for k, v in pairs:
        stations[k] = v
        stations[v] = k
    set_cached_stations(stations)
    return stations


def get_ticket_info(args: QueryArgs, stations: dict) -> (str, bool):
    # 通过访问url_init，存储cookie信息
    url_init = 'https://kyfw.12306.cn/otn/leftTicket/init'
    with requests.request(method="GET", url=url_init, verify=False) as r:
        print(f"init set-cookie: {r.headers['Set-Cookie']}")
        session_id = r.cookies.__getitem__("JSESSIONID")
        print(f"init session id: {session_id}")
    # 开始查询车票信息，必须携带 JSESSIONID 否则请求失败
    # https://kyfw.12306.cn/otn/leftTicket/queryZ?leftTicketDTO.train_date=2023-04-15&leftTicketDTO.from_station=BJP&leftTicketDTO.to_station=TJP&purpose_codes=ADULT
    url_query = f"https://kyfw.12306.cn/otn/leftTicket/queryZ?" \
                f"leftTicketDTO.train_date={args.date_}&" \
                f"leftTicketDTO.from_station={stations.get(args.from_)}&" \
                f"leftTicketDTO.to_station={stations.get(args.to_)}&purpose_codes=ADULT"
    print(f"get_ticket_info() query url: {url_query}")
    from_str = args.from_ + ',' + stations.get(args.from_)
    to_str = args.to_ + ',' + stations.get(args.to_)
    today = time.strftime("%Y-%m-%d", time.localtime())
    print(f"get_ticket_info() from '{from_str}' > '{to_str}' now '{today}', date: {args.date_}")
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/112.0.0.0 Safari/537.36",
        "Referer": "https://kyfw.12306.cn/otn/leftTicket/init",
        # 最重要的是 cookie 中的 JSESSIONID
        # "_jc_save_toDate=2023-04-12; " 当前日期
        # "_jc_save_fromStation=%u5317%u4EAC%2CBJP; " 北京,BJP
        # "_jc_save_toStation=%u5929%u6D25%2CTJP; " 天津,TJP
        # "_jc_save_fromDate=2023-04-20 " 要查询的日期
        "Cookie": f"JSESSIONID={session_id}; "
                  f"_jc_save_toDate={today}; "
                  f"_jc_save_fromStation={urllib.parse.quote(from_str)}; "
                  f"_jc_save_toStation={urllib.parse.quote(to_str)}; "
                  f"_jc_save_fromDate={args.date_}"
    }
    # print(f"get_ticket_info() request header: {headers}")
    with requests.request(method="GET", url=url_query, verify=False, headers=headers) as r:
        print(f"get_ticket_info() response header: {r.headers}")
        # 解析返回的 json 数据
        return r.text, r.headers.get("Content-Type").find("/json") >= 0


def pretty_text(text: str) -> str:
    if text == '有':
        return f"{Fore.BLUE}{text}{Fore.RESET}"
    if text == '0' or text == '无' or text.strip() == '':
        return "-"
    return text


def parse_option(opt, args: QueryArgs):
    match opt:
        # 排序
        case 'LL':
            args.set_leave_last()
        case 'LF':
            args.set_leave_first()
        case 'TL':
            args.set_time_least()
        # 更新参数，重新请求
        case 'LS':
            args.set_from(input("请输入出发站："))
        case 'AS':
            args.set_to(input("请输入到达站："))
        case 'LD':
            args.set_date(input("请输入出发日期："))
        # 退出程序
        case 'Q':
            args.quit_ = True
        case 'H' | "?":
            args.ignore_ = True
            show_menu()
        case _:
            types: str = "GCDZTK"
            illegal = False
            type_ = ''
            for t in opt:
                if t in types:
                    type_ += t
                else:
                    illegal = True
                    break
            if illegal:
                args.ignore_ = True
                print(f"非法标识'{opt}'，请根据菜单重新输入")
                show_menu()
            else:
                args.type_ = type_


def handle_option(task: TicketTask, args: QueryArgs):
    if args.ignore_ or args.quit_:
        return
    print(f"handle_option() args: {args.dump()}")
    task.execute(args)


def loop_once(task: TicketTask, args: QueryArgs):
    print(f"handle_option() args: {args.dump()}")
    # 必要的三个参数一定要有
    if args.from_ == '':
        parse_option("LS", args)
        return
    if args.to_ == '':
        parse_option("AS", args)
        return
    if args.date_ == '':
        parse_option("LD", args)
        return

    # 重置参数
    args.reset()
    # 读取指令
    opt: str = input("请输入菜单选项或直接回车进行查询（不区分大小写）：").upper()
    # print(f"loop_once() read opt: {opt}")
    # 处理指令
    if opt != 'E' and opt != '':
        parse_option(opt, args)
    handle_option(task, args)


def show_menu():
    print("=================================菜单====================================")
    print("===  1、车次类型（可组合使用）：高铁（G）、城际（C）、动车（D）       ===")
    print("===                             直达（Z）、特快（T）、普快（K）       ===")
    print("===  2、出发站                ：（LS）                                ===")
    print("===  3、到达站                ：（AS）                                ===")
    print("===  4、出发日期              ：（LD）                                ===")
    print("===  5、历时最短              ：（TL）                                ===")
    print("===  6、最早出发              ：（LF）                                ===")
    print("===  7、最晚出发              ：（LL）                                ===")
    print("===  8、查询                  ：（E/回车）                            ===")
    print("===  9、帮助                  ：（H/?）                               ===")
    print("=== 10、退出                   :（Q）                                 ===")
    print("=================================菜单====================================")


if __name__ == '__main__':
    show_menu()

    urllib3.disable_warnings()
    query_args = QueryArgs()
    if len(sys.argv) > 3:
        query_args.from_ = sys.argv[1]
        query_args.to_ = sys.argv[2]
        query_args.date_ = sys.argv[3]
    ticket_task = TicketTask()
    while True:
        loop_once(ticket_task, query_args)
        if query_args.quit_:
            break
    print("退出程序！", file=sys.stderr)

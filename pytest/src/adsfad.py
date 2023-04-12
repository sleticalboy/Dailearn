"""命令行火车票查看器

Usage:
    tickets [-gdtkz] <from> <to> <date>

Options:
    -h,--help   显示帮助菜单
    -g          高铁
    -d          动车
    -t          特快
    -k          快速
    -z          直达

Example:
    tickets 北京 上海 2016-10-10
    tickets -dg 成都 南京 2016-10-10
"""
import json
import os
import re

import requests
import urllib3
from colorama import init, Fore
from docopt import docopt
from prettytable import PrettyTable

base = os.path.dirname(os.path.abspath(__file__))


class SendRequest():
    def __init__(self):
        self.cookies = requests.cookies.RequestsCookieJar()

    def get_request(self, url: str, cookies=None, headers=None) -> (str, bool):
        response = requests.get(url, verify=False, cookies=cookies, headers=headers)
        # 指定response.content>response.text解码过程中使用的编码和响应数据编码一致，防止中文乱码
        response.encoding = response.apparent_encoding
        if len(response.cookies):
            self.cookies.update(response.cookies)  # 将响应返回的cookie保存起来
            print(f"update cookie for {url}, {cookies}")
        return response.text, response.headers.get("Content-Type").__contains__("json")


class TrainCollection():
    def __init__(self):
        self.sendRequest = SendRequest()
        self.stations_info = {}

    def get_station_dict(self):
        """从12306获取车站及代号的数据"""
        url1 = 'https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.9119'
        r = self.sendRequest.get_request(url1)
        pattern = u'([\u4e00-\u9fa5]+)\|([A-Z]+)'
        return dict(re.findall(pattern, r))

    def save_stations(self, encoding='utf-8', force=False):
        """存储车站名以及代号的json文件，默认情况下，如果没有该文件就从网站获取，如果有则直接读取
        :param encoding: 指定写入的字符编码
        :param force: 指定是否强制获取最新文件
        """
        station_file = os.path.join(base, 'stations.json')
        if not force and os.path.isfile(station_file):
            with open(station_file, 'r', encoding='utf-8') as f:
                self.stations_info = json.load(f)
            return
        self.stations_info = self.get_station_dict()
        with open(station_file, 'wb') as f:
            f.write('{\n'.encode(encoding))
            for key, val in self.stations_info.items():
                f.write('"{}":"{}",\n'.format(key, val).encode(encoding))
            f.seek(-2, 2)  # 将光标移动到倒数第二个字符，删除末尾的逗号，否则json解析会出错
            f.write('\n'.encode(encoding))
            f.write('}'.encode(encoding))

    def get_tickets_info(self, date, from_station, to_station):
        '''获取车票查询结果
        :param date: 出发日期
        :param from_station: 起始站
        :param to_station: 终点站
        '''
        # 通过访问url_init，存储cookie信息
        url_init = 'https://kyfw.12306.cn/otn/leftTicket/init'
        self.sendRequest.get_request(url_init)

        # 开始查询车票信息，需要携带cookie及header信息，否则访问失败
        url = 'https://kyfw.12306.cn/otn/leftTicket/query?' \
              'leftTicketDTO.train_date={}&' \
              'leftTicketDTO.from_station={}&' \
              'leftTicketDTO.to_station={}&purpose_codes=ADULT'.format(
            date, from_station, to_station)
        header = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36'
        }
        response_text, is_json = self.sendRequest.get_request(
            url, cookies=self.sendRequest.cookies, headers=header)
        print(f"get_tickets_info() ticket info: {response_text}")
        if is_json:
            return json.loads(response_text)
        return {}

    def table_tickets(self, tickets_dict, options):
        '''将车票数据解析出来，放到list中（方便后面以表格形式展现）
        :param tickets_dict 12306返回的原始车票信息
        :param options 要查询的bool类型的参数，如是否动车，高铁，特快等
        '''
        init()
        available_trains = tickets_dict['data']['result']
        available_place = tickets_dict['data']['map']
        for raw_train in available_trains:
            raw_train_list = raw_train.split('|')
            train_no = raw_train_list[3]
            initial = train_no[0].lower()  # 标识火车类型，动车，高铁，特快
            duration = raw_train_list[10]
            if not options or initial in options:
                train = [
                    train_no,  # train number
                    '\n'.join([Fore.LIGHTGREEN_EX + available_place[raw_train_list[6]] + Fore.RESET,
                               # 始发站
                               Fore.LIGHTRED_EX + available_place[raw_train_list[7]] + Fore.RESET]),
                    # 终点站
                    '\n'.join([Fore.LIGHTGREEN_EX + raw_train_list[8] + Fore.RESET,  # 发车时间
                               Fore.LIGHTRED_EX + raw_train_list[9] + Fore.RESET]),  # 到站时间
                    duration,  # 时长
                    raw_train_list[-6] if raw_train_list[-6] and raw_train_list[
                        -6] != '无' else '--',  # 一等
                    raw_train_list[-7] if raw_train_list[-7] and raw_train_list[
                        -7] != '无' else '--',  # 二等
                    # 高级软卧
                    raw_train_list[-15] if raw_train_list[-15] and raw_train_list[
                        -15] != '无' else '--',
                    raw_train_list[-8] if raw_train_list[-8] and raw_train_list[
                        -8] != '无' else '--',  # 软卧
                    raw_train_list[-14] if raw_train_list[-14] and raw_train_list[
                        -14] != '无' else '--',  # 硬卧
                    raw_train_list[-11] if raw_train_list[-11] and raw_train_list[
                        -11] != '无' else '--',  # 硬座
                    raw_train_list[-9] if raw_train_list[-9] and raw_train_list[
                        -9] != '无' else '--',  # 无座
                ]
                yield train

    def pretty_table_tickets(self, table_ticket_list):
        header = '车次 车站 时间 历时 一等 二等 高级软卧 软卧 硬卧 硬座 无座'.split()
        pt = PrettyTable()
        pt._set_field_names(header)
        for ticket in table_ticket_list:
            pt.add_row(ticket)
        return pt

    def get_tickets_info_pertty(self, arguments: dict):
        '''获取车票查询结果
        :param arguments: 参数字典，可以由命令行传入，也可以自己构造数据传入
        '''
        self.save_stations()
        from_station = self.stations_info.get(arguments['<from>'])
        to_station = self.stations_info.get(arguments['<to>'])
        result = self.get_tickets_info(arguments['<date>'], from_station, to_station)

        # 打印结果
        options = ''.join([key for key, value in arguments.items() if value is True])
        table_ticket_list = self.table_tickets(result, options)
        pt = self.pretty_table_tickets(table_ticket_list)
        print(pt)


def main():
    arguments = docopt(__doc__)
    urllib3.disable_warnings()
    tc = TrainCollection()
    tc.get_tickets_info_pertty(arguments)


if __name__ == "__main__":
    main()

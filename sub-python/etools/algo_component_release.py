import json
import os
import re
import subprocess
import sys

import requests


def format_list(prefix: str, items: list) -> str:
    """
    格式化列表为字符串
    :param prefix: 前缀
    :param items: 列表
    :return: 格式化之后的字符串
    """
    chars_ = "abcdefghigklmnopqrstuvwxyz"
    out_str = ''
    if items and len(items) != 0:
        for index, item in enumerate(items, 0):
            if index > 25:
                break
            out_str += f"{chars_[index]}、{item}\n"
        return "\n**{}**:\n{}".format(prefix, out_str)
    return out_str


def find_git_work_dir(file: str) -> str:
    """
    找到 .git 所在的目录作
    :param file: 文件绝对路径
    :return: .git 所在的目录
    :rtype str
    """
    print(f"find_git_work_dir() input '{file}'")

    # 取文件所在目录
    temp = os.path.dirname(file)
    while not os.path.exists("{}/.git".format(temp)):
        if temp == '/':
            # 递归到根目录了，还没找到 .git 目录
            return ''
        # 目录向上一级
        temp = os.path.dirname(temp)
    return temp


def is_clean_git_repo(cwd: str) -> bool:
    """
    检测当前 git 工程是否所有文件已提交
    :param cwd: git 工程根目录
    :return: True 所有文件已提交, False 有未提交文件
    """
    proc = subprocess.Popen("git status", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    is_clean: bool = False
    for line in proc.stdout.readlines():
        line = line.replace('\n', '').strip()
        if line.isspace() or len(line) == 0:
            continue
        if line.find('Changes not staged for commit') >= 0 \
                or line.find('Changes to be committed') >= 0:
            is_clean = False
            break
        elif line.find('nothing to commit') >= 0:
            is_clean = True
            break
    proc.terminate()
    return is_clean


def get_git_tag_hash(tag_name: str, cwd: str) -> str:
    # 找到一个 tag 的 commit id
    with subprocess.Popen(f"git show {tag_name} --pretty='%h'", shell=True, text=True,
                          cwd=cwd, stdout=subprocess.PIPE) as proc:
        proc.wait()
        pattern = re.compile(r'^[0-9a-f]{8}$')
        for line in proc.stdout.readlines():
            line = line.strip().replace('\n', '')
            if line != '' and pattern.match(line):
                return line
        proc.terminate()
        return ''


class GitTag:
    def __init__(self, tag_name: str, cwd: str):
        self.name = tag_name
        self.sha1 = get_git_tag_hash(tag_name, cwd)

    def __str__(self):
        return f'"{self.name} -> {self.sha1}"'


def find_latest_and_last_tag(cwd: str) -> (GitTag, GitTag):
    """
    找到当前 git 工程的最新 tag 和上一个 tag 及其 hash 值(commit id)
    :param cwd: git 工程根目录
    :return: tag 及其 hash 值
    """

    # num -> tag
    tags: dict[int, str] = {}

    def handle_tag(tag: str):
        print(tag)
        pieces = tag.replace('v', '').split('.')
        for idx in range(len(pieces)):
            if not pieces[idx].isdigit():
                return
            if len(pieces[idx]) == 1:
                pieces[idx] = '0' + pieces[idx]
        tags[int(''.join(pieces))] = tag

    proc = subprocess.Popen("git tag --list", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    for line in proc.stdout.readlines():
        handle_tag(line.replace('\n', ''))
    proc.terminate()
    # 没有任何 tag，需要先打 tag
    if len(tags) == 0:
        print("没有检测到任何 tag，请先打 tag！", file=sys.stderr)
        exit(1)

    keys = list(tags.keys())
    keys.sort(reverse=True)
    # 只输出几个 tag 来看下结果
    short_tags = {}
    for k in keys[:10 if len(keys) > 10 else len(keys)]:
        short_tags[k] = tags[k]
    print(f'all tags: {short_tags}')

    latest = GitTag(tags[keys[0]], cwd)
    last = GitTag(tags[keys[1]], cwd)

    print(f"latest tag: {latest}, last tag: {last}")
    return latest, last


def get_server_versions() -> list[str]:
    check_url = 'http://nexus.quvideo.com/nexus/content/repositories/mobile_public_v2/com/quvideo/mobile/component/engine/commonAI/'
    with requests.get(check_url) as r:
        version_pattern = re.compile(r'\d+\.\d+\.\d+')
        version_map: dict[int, str] = {}
        for item in version_pattern.findall(''.join(r.text)):
            version_map[int(item.replace('.', ''))] = item
        return list(version_map.values())


def inc_version(text: str, release: bool = False) -> str:
    import math
    # test: 4.5.301 -> 4.5.302
    # release: 4.5.3 -> 4.6.4
    __current = text[text.rfind(' ') + 1:].replace('\'', '')
    segments = __current.split('.')
    if release:
        fix = math.floor((int(segments[2]) * math.pow(10, 3 - len(segments[2])) + 100) / 100)
        minor = int(segments[1])
        if fix >= 10:
            minor += 1
            fix = 0
        if minor >= 10:
            segments[1] = '0'
            segments[0] = str(int(segments[0]) + 1)
        else:
            segments[1] = str(minor)
        segments[2] = str(fix)
    else:
        fix = int(int(segments[2]) * math.pow(10, 3 - len(segments[2]))) + 1
        temp = str(fix)
        # 补齐
        segments[2] = '0' * (3 - len(temp)) + temp
    __next = '.'.join(segments)
    print(f"{__current} -> {segments} -> {__next}")
    return __next


def change_version_if_needed(cwd: str, release: bool) -> str:
    if release:
        return ''
    # 获取 server 端所有已知版本号
    remote_versions = get_server_versions()

    # 生成下一个可用版本号
    def gen_next_version(old: str) -> str:
        next_ = inc_version(old)
        while next_ in remote_versions:
            print(f'{next_} exists, try next!')
            next_ = inc_version(next_)
        return next_

    # 读取文件
    with open(cwd + '/config.gradle', mode='r') as cf:
        contents = cf.readlines()
    # 如果是 debug 类型，自动生成新的版本号
    next_version = ''
    # 修改 config 文件内容
    for i in range(len(contents)):
        line = contents[i]
        if 'gVersion' in line:
            # 修改版本号
            old_version = line[line.rfind(' ') + 1:-1]
            next_version = gen_next_version(old_version)
            contents[i] = line.replace(old_version, f"'{next_version}'")
            print(f"version old: {old_version}, new: '{next_version}'")
            break
    # 写入文件
    with open(cwd + '/config.gradle', mode='w') as f:
        f.writelines(contents)
    return next_version


class ReleaseNotes:
    """
    获取最近提交日志：新功能、修复问题、作者
    """

    def __init__(self, cwd, latest, last):
        self.latest_sha1: str = latest
        self.last_sha1: str = last
        self.cwd: str = cwd
        self.authors: dict[str, str] = {}
        self.features: list[str] = []
        self.issues: list[str] = []
        self.test_notes: list[str] = []
        self.engine_notes: list[str] = []
        self.__parse_logs__()

    def __parse_single_line__(self, line: str):
        segments: list = line.split(' | ')
        print(segments[-1])
        author_email = segments[2].strip().split(' ')
        self.authors[author_email[0]] = author_email[1]
        log: str = segments[3].strip()
        if log.startswith("fix:"):
            self.issues.append(log[4:].strip())
        elif log.startswith("feat:"):
            self.features.append(log[5:].strip())
        elif log.startswith("update:"):
            self.features.append(log[7:].strip())
        elif log.startswith("note:") or log.startswith("test:"):
            self.test_notes.append(log[5:].strip())
        elif log.startswith("engine:"):
            self.engine_notes.append(log[7:].strip())

    def __parse_logs__(self):
        proc = subprocess.Popen("git log --pretty='%h | %ad | %an %ae | %s' --date=short",
                                shell=True, text=True, cwd=self.cwd, stdout=subprocess.PIPE)
        # 测试版本收集上一个 tag 到最新之间的所有提交内容
        started = self.latest_sha1 == ''
        while True:
            line = proc.stdout.readline().replace('\n', '').strip()
            if not started and line.startswith(self.latest_sha1):
                started = True
                print(">>>>>找到最新 tag， 开始收集日志！>>>>>")
            if started and line.startswith(self.last_sha1):
                print("<<<<<找到上一个 tag， 中断！<<<<<")
                break
            if started and line != '':
                self.__parse_single_line__(line)
        proc.terminate()
        # 打印处理结果
        self.__dump__()

    def __dump__(self):
        print(f"\nauthors: >>>>>>>\n{self.authors}")
        print("features: >>>>>>>")
        for feature in self.features:
            print(feature.replace(' ', ''))
        print("\nissues: >>>>>>>")
        for issue in self.issues:
            print(issue.replace(' ', ''))
        print("\ntest_notes: >>>>>>>")
        for note in self.test_notes:
            print(note.replace(' ', ''))
        print("\nengine notes: >>>>>>>")
        for note in self.engine_notes:
            print(note.replace(' ', ''))
        print()

    def format(self) -> str:
        return '<at id=all></at> 本次更新包含以下内容：{}{}{}{}'.format(
            format_list('new features', self.features),
            format_list('fixed issues', self.issues),
            format_list('引擎相关', self.test_notes),
            format_list('测试注意', self.engine_notes)
        )


def notify_to_feishu(version_name: str, notes: ReleaseNotes):
    """
    通过飞书机器人 API 发送升级通知
    :param version_name: 一般来说最新 tag 即当前发布版本，debug 版本除外
    :param notes: 发布日志
    :return:
    """
    # 这是 Android 的 release note，如果 iOS 使用此脚本需要替换掉
    release_note_url: str = 'https://quvideo.feishu.cn/wiki/wikcnT1yMILsB2UH6DOYTlc4YBc#728keG'
    payload = {
        "msg_type": "interactive",
        "card": {
            "header": {
                "template": "red",
                "title": {
                    "content": f"Android 算法组件 {version_name} 发布",
                    "tag": "plain_text"
                }
            },
            "elements": [
                {
                    "tag": "div",
                    "text": {
                        "tag": "lark_md",
                        "content": notes.format()
                    }
                },
                {
                    "tag": "action",
                    "actions": [
                        {
                            "tag": "button",
                            "text": {
                                "tag": "plain_text",
                                "content": "查看更详细升级日志"
                            },
                            "type": "primary",
                            "url": release_note_url
                        }
                    ]
                }
            ]
        }
    }
    # 打印日志
    data: str = json.dumps(payload, ensure_ascii=False)
    print(f"{data}\n body size: {len(data)}")
    # 这个 url 是固定的
    url = 'https://open.feishu.cn/open-apis/bot/v2/hook/7ce4993b-afbc-46cf-b1b5-de966ac3de5b'
    headers = {"Content-Type": "application/json"}
    with requests.post(url=url, headers=headers, json=payload) as r:
        print(f"content: {r.text}")


def exec_task(cwd: str) -> int:
    """
    执行任务
    :param cwd: 工作目录
    :return: 执行任务返回值
    """
    cmd = f"bash upload-to-maven.sh upload"
    proc = subprocess.Popen(cmd, shell=True, cwd=cwd, text=True, stdout=subprocess.PIPE)
    print(f"waiting for cmd '{cmd}' running...")
    outputs: list[str] = []
    while True:
        line = proc.stdout.readline()
        if not line:
            break
        line = line.replace('\n', '').strip()
        print(line)
        if line != '':
            outputs.append(line)
    if proc.returncode is not None:
        print(f"'{cmd}' failed with {proc.returncode}")
        print('\n'.join(outputs))
    return 0 if proc.returncode is None else proc.returncode


def run_main_work_flow(args: list[str]):
    """
    执行主流程
    :param args: 参数列表，扩展用
    :return:
    """

    # git 工作目录
    git_work_dir = find_git_work_dir(__file__)
    print(f"git work dir: {git_work_dir}")

    # 校验工作目录是否合法
    if git_work_dir == '':
        print("当前目录不是一个 git 工程目录！", file=sys.stderr)
        exit(1)

    # 如果当前工程有未提交的文件：
    # 1、如果是正式版本则中断流程，提示提交代码然后再次执行脚本发布；
    # 2、如果是测试版本则自动修改版本号并发布；
    is_release = args[0] == 'release'
    if not is_clean_git_repo(cwd=git_work_dir) and is_release:
        print("有未提交的文件，请先提交！", file=sys.stderr)
        exit(1)
    # 修改版本号
    debug_version = change_version_if_needed(cwd=git_work_dir, release=is_release)
    # 找到最新 tag 和上一个 tag
    latest_tag, last_tag = find_latest_and_last_tag(cwd=git_work_dir)
    # 执行发布脚本
    ret = 0
    ret = exec_task(cwd=git_work_dir)
    print(f'exec upload task {ret}')
    if ret == 0:
        # 解析 git log 拼装 release note
        notes = ReleaseNotes(cwd=git_work_dir, latest=latest_tag.sha1 if is_release else '', last=last_tag.sha1)
        # 通过飞书机器人 API 发送升级通知
        notify_to_feishu(latest_tag.name if is_release else f'v{debug_version}(测试版)', notes)
        pass
    else:
        print(f"exec task failed: {ret}")


def usage():
    """
    打印脚本用法
    """
    print(f"usage: {sys.argv[0]} task_type[debug/release]", file=sys.stderr)
    exit(1)


if __name__ == '__main__':
    # 检验参数
    if len(sys.argv) < 2:
        usage()

    # 打印参数
    print(f"argc: {len(sys.argv)} -> task type: {sys.argv[1]}")

    # 执行主流程
    run_main_work_flow(args=sys.argv[1:])

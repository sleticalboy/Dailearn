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


def find_latest_git_tag_and_last_tag_hash(cwd: str) -> tuple[str, str]:
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

    # 找到上一个 tag 的 commit id
    proc = subprocess.Popen(f"git show {tags[keys[1]]} --pretty='%h'", shell=True, text=True,
                            cwd=cwd, stdout=subprocess.PIPE)
    last_tag_hash: str
    pattern = re.compile(r'^[0-9a-f]{8}$')
    while True:
        line = proc.stdout.readline().strip().replace('\n', '')
        if line != '' and pattern.match(line):
            last_tag_hash = line
            break
    proc.terminate()
    print(f"latest tag: {tags[keys[0]]}, last tag: {tags[keys[1]]} -> {last_tag_hash}")
    return tags[keys[0]], last_tag_hash


class ReleaseNotes:
    """
    获取最近提交日志：新功能、修复问题、作者
    """

    def __init__(self, last_tag_hash, cwd):
        self.last_tag_hash = last_tag_hash
        self.cwd = cwd
        self.authors: dict[str, str] = {}
        self.features: list[str] = []
        self.issues: list[str] = []
        self.test_notes: list[str] = []
        self.engine_notes: list[str] = []
        self.__collect__()

    def __collect__(self):
        proc = subprocess.Popen("git log --pretty='%h | %ad | %an %ae | %s' --date=short",
                                shell=True, text=True, cwd=self.cwd, stdout=subprocess.PIPE)
        # 遍历到上次 tag 中断
        is_last_tag = False
        # 通过 tag 来处理，每发布一次就打一个 tag
        while not is_last_tag:
            line = proc.stdout.readline().replace('\n', '').strip()
            if not line:
                break
            if line.startswith(self.last_tag_hash):
                print("找到上一个 tag， 中断！")
                break
            if line.isspace() or len(line) == 0:
                continue

            print(line)
            segments: list = line.split(' | ')
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
        proc.terminate()
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


def notify_to_feishu(latest_tag: str, notes: ReleaseNotes):
    """
    通过飞书机器人 API 发送升级通知
    :param latest_tag: 最新 tag，即当前发布版本
    :param notes: 发布日志
    :return:
    """
    # 这是 Android 的 release note，如果 iOS 使用此脚本需要替换掉
    release_note_url: str = 'https://quvideo.feishu.cn/wiki/wikcnT1yMILsB2UH6DOYTlc4YBc#728keG'
    payload = {
        "msg_type": "interactive",
        "card": {
            "header": {
                "template": "blue",
                "title": {
                    "content": f"Android 算法组件 {latest_tag} 发布",
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


def exec_task(cwd: str, task_type='debug') -> int:
    """
    执行任务
    :param cwd: 工作目录
    :param task_type: 任务类型 debug/release/upload
    :return: 执行任务返回值
    """
    cmd = f'upload-to-maven.sh {task_type}'
    proc = subprocess.Popen(f"bash {cmd}", shell=True, cwd=cwd, text=True, stdout=subprocess.PIPE)
    print(f"cmd '{cmd}' running...")
    proc.wait()
    if proc.returncode != 0:
        print(f"'{cmd}' failed with {proc.returncode}")
        for line in proc.stdout.readlines():
            print(line.replace('\n', '').strip())
        return proc.returncode
    return 0


def run_main_work_flow(args: list):
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

    # 如果当前工程有没有提交的文件：
    # 1、中断流程，手动提交代码然后再次执行脚本发布；
    # 2、自动提交并发布；
    if not is_clean_git_repo(cwd=git_work_dir):
        print("有未提交的文件，请先提交！", file=sys.stderr)
        # exit(1)

    # 找到最新 tag 和上一个 tag 的 hash 值
    latest_tag, last_tag_hash = find_latest_git_tag_and_last_tag_hash(cwd=git_work_dir)

    ret = 0
    # 执行发布脚本
    # ret = exec_task(cwd=git_work_dir, task_type=args[0])
    if ret == 0:
        # 解析 git log 拼装 release note
        notes = ReleaseNotes(last_tag_hash=last_tag_hash, cwd=git_work_dir)
        # 通过飞书机器人 API 发送升级通知
        notify_to_feishu(latest_tag, notes)
        pass
    else:
        print(f"exec task failed: {ret}")


def usage():
    """
    打印脚本用法
    """
    print(f"usage: {sys.argv[0]} task_type", file=sys.stderr)
    exit(1)


if __name__ == '__main__':
    # 检验参数
    if len(sys.argv) < 2:
        usage()

    # 打印参数
    print(f"argc: {len(sys.argv)} -> task type: {sys.argv[1]}")

    # 执行主流程
    run_main_work_flow(args=sys.argv[1:])

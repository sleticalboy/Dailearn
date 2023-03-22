import json
import os
import subprocess
import sys

import requests


def formatted_list(prefix: str, items: list) -> str:
    chars_ = "abcdefghigklmnopqrstuvwxyz"
    issues_ = ''
    if items is not None and len(items) != 0:
        for index, item in enumerate(items, 0):
            if index > 25:
                break
            issues_ += f"{chars_[index]}、{item}\n"
        return "\n**{0}**:\n{1}".format(prefix, issues_)
    return issues_


def is_clean_git_repo(cwd: str) -> bool:
    """
    返回当前 git 工程是否所有文件已提交
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
        if line.__contains__('Changes not staged for commit') \
                or line.__contains__('Changes to be committed'):
            is_clean = False
            break
        elif line.__contains__('nothing to commit'):
            is_clean = True
            break
    proc.terminate()
    return is_clean


def find_latest_git_tag_and_last_tag_hash(cwd: str) -> tuple[str, str]:
    """
    找到当前 git 工程的上一个 tag 及其 hash 值(commit id)
    :param cwd: git 工程根目录
    :return: tag 及其 hash 值
    """
    # num -> tag
    tags = {}
    max_ = 0
    proc = subprocess.Popen("git tag --list", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    for tag in proc.stdout.readlines():
        tag = tag.replace('\n', '')
        num = int(tag.replace('.', '')[1:])
        tags[num] = tag
        if num > max_:
            max_ = num
    proc.terminate()
    print(f"all tags: {tags}, max: {max_}, last tag: {tags[max_ - 1]}")

    # 找到上一个 tag 的 commit id
    proc = subprocess.Popen(f"git show {tags[max_ - 1]} --pretty='%h'", shell=True, text=True,
                            cwd=cwd, stdout=subprocess.PIPE)
    last_tag_hash: str = proc.stdout.readline().strip()
    proc.terminate()
    print(f"last tag hash: {last_tag_hash}")
    return tags[max_], last_tag_hash


class ReleaseNote:
    # 获取最近提交日志：新功能、修复问题、作者
    authors: dict = {}
    features: list = []
    issues: list = []
    test_notes: list = []
    engine_notes: list = []

    def __init__(self, last_tag_hash, cwd):
        self.last_tag_hash = last_tag_hash
        self.cwd = cwd
        self.__collect__()

    def __collect__(self):
        proc = subprocess.Popen("git log --pretty='%h | %ad | %an %ae | %s' --date=short",
                                shell=True, text=True, cwd=self.cwd, stdout=subprocess.PIPE)
        # 遍历到上次 tag 中断
        is_last_tag = False
        # 通过 tag 来处理，每发布一次就打一个 tag
        while not is_last_tag:
            line = proc.stdout.readline().replace('\n', '').strip()
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
            elif log.startswith("note:"):
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


def notify_to_feishu(latest_tag: str, note: ReleaseNote):
    """
    通过飞书机器人 API 发送升级通知
    :param latest_tag: 最新 tag，即当前发布版本
    :param note: 发布日志
    :return:
    """
    release_note_url: str = 'https://quvideo.feishu.cn/wiki/wikcnT1yMILsB2UH6DOYTlc4YBc#728keG'
    elements_ = [
        {
            "tag": "div",
            "text": {
                "tag": "lark_md",
                "content": f"<at id=all></at> 本次更新包含以下内容："
                           f"{formatted_list('new features', note.features)}"
                           f"{formatted_list('fixed issues', note.issues)}"
                           f"{formatted_list('引擎相关', note.test_notes)}"
                           f"{formatted_list('测试注意', note.engine_notes)}"
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
                    "url": f"{release_note_url}"
                }
            ]
        }
    ]
    data = {
        "msg_type": "interactive",
        "card": {
            "header": {
                "template": "blue",
                "title": {
                    "content": f"Android 算法组件 {latest_tag} 发布",
                    "tag": "plain_text"
                }
            },
            "elements": elements_
        }
    }
    payload: str = json.dumps(data, ensure_ascii=False)
    print(f"{payload}\n body size: {len(payload)}")

    url = 'https://open.feishu.cn/open-apis/bot/v2/hook/7ce4993b-afbc-46cf-b1b5-de966ac3de5b'
    method = 'post'
    headers = {"Content-Type": "application/json"}
    resp = requests.request(method=method, url=url, headers=headers, json=data)
    print(f"content: {resp.text}")


def exec_task(cwd: str, task_type: str) -> int:
    proc = subprocess.Popen(f"bash upload-to-maven.sh {task_type}", shell=True, text=True,
                            stdout=subprocess.PIPE, cwd=cwd)
    if proc.returncode is not None and proc.returncode != 0:
        print(f"./upload-to-maven.sh failed with {proc.returncode}")
        for line in proc.stdout.readlines():
            line = line.replace('\n', '').strip()
            print(line)
        return proc.returncode
    return 0


def run_main_work_flow(task_type: str):
    """
    执行主流程
    :param task_type: debug/release/upload
    :return:
    """
    # 校验当前工程是否合法
    if not os.path.isdir(".git"):
        print("当前目录不是一个 git 工程目录", file=sys.stderr)
        # exit(1)

    # 设置工作目录
    # cwd = '/home/binlee/code/open-source/quvideo/XYAlgLibs'
    cwd = '/home/binlee/code/open-source/quvideo/QuVideoEngineAI'

    # 如果当前工程有没有提交的文件，中断流程
    if not is_clean_git_repo(cwd=cwd):
        print("有未提交的文件，请先提交！", file=sys.stderr)
        exit(1)

    # 找到最新 tag 和上一个 tag 的 hash 值
    latest_tag, last_tag_hash = find_latest_git_tag_and_last_tag_hash(cwd=cwd)

    # 执行发布脚本
    ret: int = exec_task(cwd=cwd, task_type=task_type)
    if ret == 0:
        # 解析 git log 拼装 release note
        notes = ReleaseNote(last_tag_hash=last_tag_hash, cwd=cwd)
        # 通过飞书机器人 API 发送升级通知
        notify_to_feishu(latest_tag, notes)
    else:
        print(f"exec task failed: {ret}")


def usage() -> int:
    """
    打印脚本用法
    """
    print(f"usage: {sys.argv[0]} task_type", file=sys.stderr)
    return 1


if __name__ == '__main__':
    # 检验参数
    if len(sys.argv) < 2:
        exit(usage())

    # 打印参数
    print(f"argc: {len(sys.argv)} -> task type: {sys.argv[1]}")

    # 执行主流程
    run_main_work_flow(sys.argv[1])

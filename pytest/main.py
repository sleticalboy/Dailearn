import json
import re
import subprocess
import sys

import requests


def formatted_list(prefix: str, items: list):
    if items is not None and items.__len__() != 0:
        _issues = ''
        index: int = 1
        for item in items:
            _issues += f"{index}、{item};\n"
        return "**{0}**:\n{1}".format(prefix, _issues)
    return ''


def notifyFeishu():
    url = 'https://open.feishu.cn/open-apis/bot/v2/hook/7ce4993b-afbc-46cf-b1b5-de966ac3de5b'
    method = 'post'
    headers = {'Content-Type': 'application/json'}
    cwd = '/home/binlee/code/open-source/quvideo/QuVideoEngineAI'

    # 查看所有 tag 并找到上一个 tag
    tags = []
    last_tag: str = ''
    max_ = 0
    proc = subprocess.Popen("git tag --list", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    for tag in proc.stdout.readlines():
        tag = tag.replace('\n', '')
        tags.append(tag)
        num = int(tag.replace('.', '')[1:])
        if num > max_:
            last_tag = tag
            max_ = num
    proc.terminate()

    # 找到上一个 tag 的 commit id
    proc = subprocess.Popen(f"git show {tags[0]}", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    last_tag_hash: str = proc.stdout.readline().strip()
    proc.terminate()
    print(f"all tags: {tags}, last tag: {last_tag} {last_tag_hash}\n")

    cwd = '/home/binlee/code/open-source/quvideo/XYAlgLibs'
    # 如果当前工程有没有提交的文件，中断流程
    proc = subprocess.Popen("git status", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    for line in proc.stdout.readlines():
        line = line.replace('\n', '').strip()
        if line.isspace() or len(line) == 0:
            continue
        if line.__contains__('Changes not staged for commit') \
                or line.__contains__('Changes to be committed'):
            print("有未提交的文件，请先提交！", file=sys.stderr)
            proc.terminate()
            return 1
        elif line.__contains__('nothing to commit'):
            print("干净工程，开始获取最后一次提交信息！\n")
            proc.terminate()
            break
    proc.terminate()

    cwd = '/home/binlee/code/open-source/quvideo/QuVideoEngineAI'
    # 获取最近提交日志：新功能、修复问题、作者
    authors: dict = {}
    features: list = []
    issues: list = []
    proc = subprocess.Popen("git log", shell=True, text=True, cwd=cwd,
                            stdout=subprocess.PIPE)
    # 遍历到上次 tag 中断
    is_last_tag = False
    # 通过 tag 来处理，每发布一次就打一个 tag
    # commit bd96930bc26d4d42f4243f0d8f04a85ec4778954
    # Author: bin.li <bin.li@quvideo.com>
    # update: 一键成片算法库 v0.2.1
    # 1. 支持高帧率分析素材，禁用转场点检测时按照sample rate分析素材；
    # 2. 缺少素材时默认循环填空，视频循环时高光时刻不重复；
    # 3. 优先保证相邻素材不重复，其次满足规则。
    # commit dba7484411e181d0c145275924ff061bc441ec05
    # Author: bin.li <bin.li@quvideo.com>
    while not is_last_tag:
        line = proc.stdout.readline().replace('\n', '').strip()
        if line.startswith(last_tag_hash):
            print("找到上一个 tag， 中断！")
            break
        if line.isspace() or len(line) == 0\
                or line.startswith("Date: ")\
                or line.startswith("commit "):
            continue
        if line.startswith("Author: "):
            segments: list = line[8:].split(' ')
            if len(segments) > 1:
                authors[segments[0]] = segments[1]
            else:
                authors[segments[0]] = ''
        else:
            print(line)
            one_logs = []
            if line.startswith("fix:"):
                issues.append(one_logs)
                one_logs.append(line[4:].replace('*', '').strip())
            elif line.startswith("feat:"):
                features.append(one_logs)
                one_logs.append(line[5:].replace('*', '').strip())
            while True:
                # 循环处理这次提交内容, 直到遇到下次 commit id
                line = proc.stdout.readline().replace('\n', '').strip()
                is_last_tag = line.startswith(last_tag_hash)
                if is_last_tag or line.startswith("commit "):
                    break
                print(line)
                if len(line) != 0:
                    one_logs.append(line.replace('*', '').strip())
    proc.terminate()
    print(f"authors: {authors}")
    print("\nfeatures: >>>>>>>")
    for feature in features:
        items: list = []
        for i, item in enumerate(feature, 1):
            if re.match(r"\d+\.", item):
                items.append(item)
            else:
                items.append(f"{i}. {item}")
        print(items)
    print("\nissues: >>>>>>>")
    for issue in issues:
        print(issue)

    release_url: str = 'https://www.feishu.cn'
    release_version: str = '4.5.3'

    _card = {
        "config": {
            "wide_screen_mode": True
        },
        "elements": [
            {"tag": "markdown",
             "content": f"<at id=all></at> **<font color='red'> v{release_version} </font>"
                        # f"**[升级内容]({release_url})：\n"
                        # f"{formatted_list('features', features)}"
                        # f"{formatted_list('fixed issues', issues)}"
                        # f"{formatted_list('引擎相关', None)}"
                        # f"{formatted_list('测试注意', None)}"
             }
        ],
        "header": {
            "template": "blue",
            "title": {
                "content": f"Android 算法组件 v{release_version} 发布",
                "tag": "plain_text"
            }
        }
    }
    data = {
        "message_type": "interactive",
        "card": _card
    }
    body = json.dumps(data, ensure_ascii=False)
    print(f"{body}\n body size: {len(body)}")
    resp = requests.request(method=method, url=url, headers=headers, json=body)
    print(f"content: {resp.text}")


def exec_task():
    task = subprocess.Popen("bash upload-to-maven.sh release", shell=True, stdout=subprocess.PIPE,
                            cwd='/home/binlee/code/open-source/quvideo/QuVideoEngineAI')
    if task.returncode is not None and task.returncode != 0:
        print(f"./upload-to-maven.sh failed with {task.returncode}")
        return 1
    for line in task.stdout.readlines():
        print(str(line).strip().replace("\\n'", "").replace("b'", "").replace("\\n\"", "").replace(
            "b\"", ""))
    return 0


if __name__ == '__main__':
    print(sys.argv)
    # ret: int = exec_task()
    ret: int = 0
    if ret == 0:
        notifyFeishu()
    else:
        print(f"exec task failed: {ret}")

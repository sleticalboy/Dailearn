import json
import subprocess
import sys

import requests


def formatted_list(prefix: str, items: list):
    if items is not None and items.__len__() != 0:
        _issues = ''
        index: int = 1
        for item in items:
            _issues += f"{index}、{item};\n"
        return "**{0}**:\\n{1}".format(prefix, _issues)
    return ''


def notifyFeishu():
    # url = 'https://open.feishu.cn/open-apis/bot/v2/hook/7ce4993b-afbc-46cf-b1b5-de966ac3de5b'
    url = 'https://open.feishu.cn/open-apis/bot/v2/hook/6676a22e-4502-4d31-9f84-71baede08ad5'
    method = 'post'
    headers = {'Content-Type': 'application/json'}

    # 如果当前工程有没有提交的文件，中断流程
    pre_check = subprocess.Popen("git status", shell=True,
                                 stdout=subprocess.PIPE,
                                 # cwd='/home/binlee/code/open-source/quvideo/QuVideoEngineAI')
                                 cwd='/home/binlee/code/open-source/quvideo/XYAlgLibs')
    limit: int = 1
    while limit < 10:
        line: str = pre_check.stdout.readline().decode('utf-8').strip()
        if len(line) == 0 or line.isspace():
            continue
        if line.__contains__('Changes not staged for commit'):
            print("有未提交的文件，请先提交！")
            return 1
        elif line.__contains__('nothing to commit'):
            # print("干净工程，开始获取最后一次提交信息！")
            pre_check.terminate()
            break
        limit = limit + 1

    # 获取最近提交日志
    authors: dict = {}
    features: list = []
    issues: list = []
    git_log = subprocess.Popen("git log -l 3 --pretty='%an##%B'", shell=True,
                               stdout=subprocess.PIPE,
                               cwd='/home/binlee/code/open-source/quvideo/XYAlgLibs')
    # 遍历到上次 tag 中断
    # 通过 tag 来处理，每发布一次就打一个 tag
    limit = 1
    while limit < 10:
        line: str = git_log.stdout.readline().decode('utf-8').strip()
        if len(line) == 0 or line.isspace():
            continue
        print(f"---> {line}")
        if line.__contains__("##"):
            segments = line.split("##")
            print(f"segments: {segments}")
            if len(segments) == 2:
                authors[segments[0]] = ''
                if segments[1].__contains__('fix'):
                    issues.append(segments[1])
                else:
                    features.append(segments[1])
        else:
            features.append(line)
        limit = limit + 1
    print(f"authors: {list(authors.keys())}\nfeatures: {features}\nissues: {issues}")

    release_url: str = 'https://www.feishu.cn'
    release_version: str = '4.5.3'

    _content = {
        "config": {
            "wide_screen_mode": True
        },
        "elements": [
            {"tag": "markdown",
             "content": f"<at id=all></at> **<font color='red'> v{release_version} </font>"
                        f"**[升级内容]({release_url})：\\n"
                        f"{formatted_list('features', features)}"
                        f"{formatted_list('fixed issues', issues)}"
                        f"{formatted_list('引擎相关', None)}"
                        f"{formatted_list('测试注意', None)}",
             "_id": "element-c677cb5b-973f-4054-9bfa-6487c365816c"
             }
        ],
        "header": {
            "template": "blue",
            "title": {
                "content": f"Android 算法组件 v{release_version} 发布",
                "tag": "plain_text"
            },
            "_id": "element-b28426af-89f5-40c9-9cfb-ead36248fe34"
        }
    }
    data = {
        "message_type": "interactive",
        "card": json.dumps(_content)
    }
    print(json.dumps(data))
    resp = requests.request(method=method, url=url, headers=headers, json=json.dumps(data))
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

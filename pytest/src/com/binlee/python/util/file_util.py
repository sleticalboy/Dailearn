import os


def find_dir(file: str, anchor: str) -> str:
    """
    找到 .git 所在的目录作
    :param file: 文件绝对路径
    :param anchor: 矛点
    :return: .git 所在的目录
    :rtype str
    """
    print(f"find_git_work_dir() input '{file}'")

    # 取文件所在目录
    temp = os.path.dirname(file)
    while not os.path.exists("{}/{}".format(temp, anchor)):
        if temp == '/':
            # 递归到根目录了，还没找到 .git 目录
            return ''
        # 目录向上一级
        temp = os.path.dirname(temp)
    return temp


def get_pure_name(name: str) -> str:
    """
    获取不带后缀的文件名
    @param name: 绝对路径或者单纯的文件名
    @return 文件名
    @rtype: str
    """
    index = name.rfind('/')
    if index > 0:
        name = name[index + 1:]
    return name[:name.rfind('.')]


def create_out_dir(file: str, sub: str = '') -> str:
    if sub == '':
        target = f"{find_dir(file, '.gitignore')}/out/{get_pure_name(file)}"
    else:
        target = f"{find_dir(file, '.gitignore')}/out/{sub}/{get_pure_name(file)}"
    if not os.path.exists(target):
        os.makedirs(target)
    return target

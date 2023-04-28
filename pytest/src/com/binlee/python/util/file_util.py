import os


def find_dir(file: str, anchor: str) -> str:
    """
    找到 .git 所在的目录作
    :param file: 文件绝对路径
    :param anchor: 矛点
    :return: .git 所在的目录
    :rtype str
    """
    print(f"find_dir() input '{file}'")

    # 取文件所在目录
    temp = os.path.dirname(file)
    while not os.path.exists("{}/{}".format(temp, anchor)):
        if temp == '/':
            # 递归到根目录了，还没找到 .git 目录
            return ''
        # 目录向上一级
        temp = os.path.dirname(temp)
    return temp


def split_filename(path: str) -> (str, str):
    return os.path.splitext(os.path.basename(path))


def create_out_dir(file: str, sub: str = '') -> str:
    name, _ = split_filename(file)
    if sub == '':
        target = f"{find_dir(file, '.gitignore')}/out/{name}"
    else:
        target = f"{find_dir(file, '.gitignore')}/out/{sub}/{name}"
    if not os.path.exists(target):
        os.makedirs(target)
    return target


def ensure_path(*path: str):
    for p in path:
        if not os.path.exists(p):
            os.makedirs(p)


def find_files(root: str, suffixes: list[str] = None, tester=None) -> list[str]:
    output: list[str] = []

    def filter_file(path: str) -> bool:
        return tester is None or (tester and tester(path))

    def recursive_find(path: str):
        if os.path.isfile(path):
            if suffixes and len(suffixes) > 0:
                # tester 为空就不校验，test 不为空就校验
                _, ext = split_filename(path)
                if ext in suffixes and filter_file(path):
                    output.append(path)
            else:
                if filter_file(path):
                    output.append(path)
        elif os.path.isdir(path):
            for child in os.listdir(path):
                recursive_find(path + '/' + child)

    recursive_find(root)
    return output


def extract_file(file: str) -> list[str]:
    import zipfile
    with zipfile.ZipFile(file) as f:
        # 如果路径不存在，extractall() 方法内部会自行创建
        path = '/tmp/' + os.path.basename(file)
        f.extractall(path)
        return find_files(path)


def read_lines(path: str) -> list[str]:
    with open(path, mode='r') as f:
        return f.readlines()


def read_content(path: str, flatmap: bool = False) -> str:
    with open(path, mode='r') as f:
        if flatmap:
            __all = ''
            for line in f.readlines():
                __all = __all + line.strip().replace('\n', '')
            return __all
        return f.read()

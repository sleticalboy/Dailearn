import os


def _get_pure_name(name: str) -> str:
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


def create_dir(base: str, name: str) -> str:
    pure_name = _get_pure_name(name)
    target = f"{base}/{pure_name}"
    if not os.path.exists(target):
        os.makedirs(target)
    return target

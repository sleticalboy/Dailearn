from typing import List

from genpy.py_algo_spec_pb2 import AlgoDownloadUrl


class User(object):
    def __init__(self, name):
        self.name = name

    def say_hello(self):
        print(f"hey, my name is {self.name}")


def hello():
    print("hello() func called")


def add(a, b):
    return a + b


def print_list(li):
    print(f"list is {li}, len: {len(li)}")


def print_dict(d):
    print(f"dict is {d}, len: {len(d)}")


def get_list() -> List[str]:
    return ['30', '99']


def get_dict():
    return {'name': 'tom', 'age': 30}


def get_user() -> User:
    return User("py tom")


class CallbackProxy:

    def __init__(self):
        self.delegate = None

    def generate_path(self, suffix: str = None, additional: str = None) -> str:
        """
        生成文件路径
        :param suffix: 文件后缀，为空时会生成一个目录，否则生成一个文件路径
        :param additional: 额外的信息，会附带在路径中
        :return: 文件路径
        """
        if self.delegate is not None:
            if suffix is None and additional is None:
                return self.delegate.gen_path()
            if suffix is not None and additional is None:
                return self.delegate.gen_path(suffix=suffix)
            if suffix is not None and additional is not None:
                return self.delegate.gen_path(suffix=suffix, additional=additional)
        raise Exception("callback is not set, could not generate path")

    def publish_progress(self, progress: float):
        if self.delegate is not None:
            self.delegate.on_progress(progress)
        pass


proxy_ = CallbackProxy()


def set_cpp_callback(callback):
    proxy_.delegate = callback
    print(f'set_cpp_callback() cb: {callback}, cpp_cb: {proxy_.delegate}')


def do_hard_work():
    print(f'do_hard_work() cb is: {proxy_.delegate}, type: {type(proxy_.delegate)}')
    if proxy_.delegate is None:
        return
    print(f'do_hard_work() cb doc: {proxy_.delegate.__doc__}')
    # 调用 c++ 函数并返回处理结果
    s = proxy_.delegate.square(3)
    print(f"do_hard_work() 3's square = {s}")

    import time
    for i in range(3):
        # 把值回调给 c++
        proxy_.delegate.post_value(i, f'str {i}')
        time.sleep(0.5)
    s = proxy_.delegate.sum_int(20, 32)
    print(f"call_c_fptr() 20 + 32 = {s}")
    print('gen dir:', proxy_.generate_path())
    print('gen image name:', proxy_.generate_path(suffix="png"))
    print('gen image with additional:', proxy_.generate_path(additional="hello", suffix='png'))

    for i in range(100):
        proxy_.publish_progress(i + 1)
        time.sleep(0.05)


def parse_protobuf(buf: bytes):
    import google
    print(f'parse_protobuf() {buf}, {type(buf)}, version: {google.protobuf.__version__}')
    url_ = AlgoDownloadUrl.FromString(buf)
    print(f'url is: {url_}')
    pass


if __name__ == '__main__':
    _url = AlgoDownloadUrl()
    _url.url = "https://example.com/py.index.html"
    _url.is_local_file = False
    _url.is_cache = False
    parse_protobuf(_url.SerializeToString())

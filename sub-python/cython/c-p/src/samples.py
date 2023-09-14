from typing import List


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
        self.callback = None


proxy_ = CallbackProxy()


def set_cpp_callback(callback):
    proxy_.callback = callback
    print(f'set_cpp_callback() cb: {callback}, cpp_cb: {proxy_.callback}')


def do_hard_work():
    if proxy_.callback is None:
        return
    print(f'do_hard_work() cb is: {proxy_.callback}, type: {type(proxy_.callback)}, doc: {proxy_.callback.__doc__}')
    # 调用 c++ 函数并返回处理结果
    s = proxy_.callback.square(3)
    print(f"do_hard_work() 3's square = {s}")

    import time
    for i in range(3):
        # 把值回调给 c++
        proxy_.callback.post_value(i, f'str {i}')
        time.sleep(0.5)
    s = proxy_.callback.sum_int(20, 32)
    print(f"call_c_fptr() 20 + 32 = {s}")
    print('gen dir:', proxy_.callback.gen_path())
    print('gen image name:', proxy_.callback.gen_path(suffix="png"))
    print('gen image with additional:', proxy_.callback.gen_path(additional="hello", suffix='png'))

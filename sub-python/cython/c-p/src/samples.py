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


def get_list() -> list[str]:
    return ['30', '99']


def get_dict():
    return {'name': 'tom', 'age': 30}


def call_c_fptr(fptr):
    if fptr is None:
        return
    print(f'call_c_fptr() fn ptr is: {fptr}, type: {type(fptr)}, doc: {fptr.__doc__}')
    # 调用 c++ 函数并返回处理结果
    s = fptr.square(3)
    print(f"call_c_fptr() 3's square = {s}")

    import time
    for i in range(3):
        # 把值回调给 c++
        fptr.post_value(i, f'str {i}')
        time.sleep(0.5)
    s = fptr.sum_int(20, 32)
    print(f"call_c_fptr() 20 + 32 = {s}")

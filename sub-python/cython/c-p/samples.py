import time

import algo_args_pb2


class User(object):
    def __init__(self, name):
        self.name = name

    def say_hello(self):
        print(f"hey, my name is {self.name}")


class AlgoProc(object):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name):
        self.name = algo_name
        print(f"[{self.name}]#__init__()")

    def process(self, args):
        print(f"[{self.name}]#process() input buf: {args}, type: {type(args)}")
        if isinstance(args, str):
            args = args.encode()
        i = algo_args_pb2.AlgoInput.FromString(args)
        print(f"p algo input is: {i}, type: {type(i)}")
        time.sleep(0.5)
        o = algo_args_pb2.AlgoOutput()
        o.oargs['1'] = '1'
        o.oargs['2'] = '2'
        o.oargs['3'] = '3'
        # print(f"p algo output is: {o}, type: {type(o)}")
        return o.SerializeToString()

    def release(self):
        print(f"[{self.name}]#release()")


def hello():
    print("hello() func called")


def add(a, b):
    return a + b


def print_list(li):
    print(f"list is {li}, len: {len(li)}")


def get_list() -> list[str]:
    return ['30', '99']


def get_dict():
    return {
        'name': 'tom',
        'age': 30,
    }


if __name__ == '__main__':
    proc = AlgoProc('prj-export')
    iargs = algo_args_pb2.AlgoInput()
    iargs.iargs['a'] = 'a'
    iargs.iargs['b'] = 'b'
    iargs.iargs['c'] = 'c'
    o = proc.process(iargs.SerializeToString())
    proc.release()
    print(f'output buf: {o}, type: {type(o)}')
    oargs = algo_args_pb2.AlgoOutput.FromString(o)
    print(f'output is: {oargs}, type: {type(oargs)}')

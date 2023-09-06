from abc import abstractmethod


class AlgoProcBase(object):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name):
        self.name = algo_name
        print(f"[{self.name}]#__init__()")

    @abstractmethod
    def process(self, args: dict):
        print(f"[{self.name}]#process() input: {args}, type: {type(args)}")

    @abstractmethod
    def release(self):
        print(f"[{self.name}]#release()")

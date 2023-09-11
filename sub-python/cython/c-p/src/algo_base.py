from abc import abstractmethod


class AlgoProcBase(object):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name: str, known_args: dict):
        self.name = algo_name
        self.known_args = known_args
        print(f"[{algo_name}]#__init__()")
        for k, v in known_args.items():
            print(f'{k}: {v.decode()}')

    @abstractmethod
    def process(self, args: dict):
        print(f"[{self.name}]#process() args: {args}")

    @abstractmethod
    def release(self):
        print(f"[{self.name}]#release()")

    def check_input_args(self, keys):
        """
        检查参数是否合法
        """
        kl = self.known_args.keys()
        for k in keys:
            if k not in kl:
                raise Exception(f"Unknown key '{k}'")

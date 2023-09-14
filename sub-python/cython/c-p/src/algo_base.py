from abc import abstractmethod


class AlgoProcBase(object):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name: str, input_keys: dict, output_keys: dict):
        self.name = algo_name
        self.input_keys = input_keys
        self.output_keys = output_keys
        self.callback = None
        print(f"[{algo_name}]#__init__()")
        for k, v in input_keys.items():
            print(f'===> {k}: {v.decode()}')
        for k, v in output_keys.items():
            print(f'<=== {k}: {v.decode()}')

    def set_callback(self, callback):
        """
        给算法设置回调函数
        :param callback: 回调函数
        """
        self.callback = callback

    def on_progress(self, progress):
        """
        回调算法进度，如果有的话
        :param progress: 进度
        """
        if self.callback is not None:
            self.callback.on_progress(progress)

    def generate_path(self, suffix: str = None, additional: str = None) -> str:
        """
        生成文件路径
        :param suffix: 文件后缀，为空时会生成一个目录，否则生成一个文件路径
        :param additional: 额外的信息，会附带在路径中
        :return: 文件路径
        """
        if self.callback is not None:
            return self.callback.generate_path(additional=additional, suffix=suffix)
        raise Exception("callback is not set, could not generate path")

    @abstractmethod
    def process(self, args: dict):
        """
        算法处理
        :param args: 处理参数
        :return: 结果 和文件集合（可选）
        """
        print(f"[{self.name}]#process() args: {args}")
        pass

    @abstractmethod
    def release(self):
        """
        释放算法资源
        """
        print(f"[{self.name}]#release()")
        pass

    def check_input_args(self, keys):
        """
        检查参数是否合法
        """
        kl = self.input_keys.keys()
        for k in keys:
            if k not in kl:
                raise Exception(f"Unknown input key '{k}'")

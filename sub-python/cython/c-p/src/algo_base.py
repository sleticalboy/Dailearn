from abc import abstractmethod


class AlgoProcBase(object):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self):
        self.name = 'py-algo-base'
        self.callback = None

        self.request = None
        self.download_urls = None

        self.response = None
        self.upload_urls = None
        pass

    @abstractmethod
    def init(self, req_buf: bytes):
        print(f"[{self.name}]#init(), type: {type(req_buf)}, buf: {req_buf}")

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
    def process(self) -> bytes:
        """
        算法处理
        :return: 处理结果
        """
        raise Exception("No implementation!")

    @abstractmethod
    def release(self):
        """
        释放算法资源
        """
        raise Exception("No implementation!")

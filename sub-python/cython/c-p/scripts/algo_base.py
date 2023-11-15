from abc import abstractmethod

from algo_pb2 import AlgoDownloadUrlMap, AlgoUploadUrlMap
from algox_pb2 import AlgoStatus
from py_algo_pb2 import PyAlgoRequest, PyAlgoResponse


class NativeHelper(object):

    def __init__(self):
        self.delegate = None

    def set_delegate(self, callback):
        self.delegate = callback

    def on_progress(self, progress):
        if self.delegate is not None:
            self.delegate.on_progress(progress)

    def generate_path(self, suffix: str = None, additional: str = None) -> str:
        """
        生成文件路径
        :param suffix: 文件后缀，为空时会生成一个目录，否则生成一个文件路径
        :param additional: 额外的信息，会附带在路径中
        :return: 文件路径
        """
        if self.delegate is None:
            raise Exception("callback is not set, could not generate path")

        if suffix is None and additional is None:
            return self.delegate.gen_path()
        if suffix is not None and additional is None:
            return self.delegate.gen_path(suffix=suffix)
        if suffix is not None and additional is not None:
            return self.delegate.gen_path(suffix=suffix, additional=additional)


class AlgoProcBase(object):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self):
        self.name = 'py-algo-base'
        self.helper = NativeHelper()
        self.model_loaded = False
        # 请求
        self.request = PyAlgoRequest()
        self.download_urls = AlgoDownloadUrlMap()
        # 响应
        self.response = PyAlgoResponse()
        self.upload_urls = AlgoUploadUrlMap()
        # 算法运行状态
        self.status = AlgoStatus()
        pass

    def init(self, req_buf: str):
        self.request.ParseFromString(req_buf)
        self.download_urls.ParseFromString(self.request.download_urls_buf)

        self.name = self.request.algo_name

        print(f"[{self.name}]#__init__(), type: {type(req_buf)} req: {req_buf}")

    def set_callback(self, callback):
        """
        给算法设置回调函数
        :param callback: 回调函数
        """
        self.helper.set_delegate(callback)
        print(f'[{self.name}]#set_callback() cb: {callback}')

    def on_progress(self, progress):
        """
        回调算法进度，如果有的话
        :param progress: 进度
        """
        self.helper.on_progress(progress)

    def generate_path(self, suffix: str = None, additional: str = None) -> str:
        """
        生成文件路径
        :param suffix: 文件后缀，为空时会生成一个目录，否则生成一个文件路径
        :param additional: 额外的信息，会附带在路径中
        :return: 文件路径
        """
        return self.helper.generate_path(suffix=suffix, additional=additional)

    @abstractmethod
    def load_model(self, model_path: str, device: str = None):
        print(f"[{self.name}]#load_model('{model_path}')")
        pass

    @abstractmethod
    def process(self) -> bytes:
        """
        算法处理
        :return: 处理结果
        """
        print(f"[{self.name}]#process()")
        return b''

    @abstractmethod
    def cancel(self):
        print(f"[{self.name}]#cancel()")
        pass

    @abstractmethod
    def release(self):
        """
        释放算法资源
        """
        print(f"[{self.name}]#release()")
        pass

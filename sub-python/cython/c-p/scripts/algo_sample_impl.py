import ctypes
import os

from algo_base import AlgoProcBase

lib_path = '/home/binlee/code/Golearn/go-so/libfib.so'


class AlgoProcImpl(AlgoProcBase):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self):
        super().__init__()
        self.lib = None

    def init(self, req_buf: bytes):
        super().init(req_buf)
        from py_algo_pb2 import PyAlgoRequest, PyAlgoResponse
        from algo_pb2 import AlgoDownloadUrlMap, AlgoUploadUrlMap
        self.request = PyAlgoRequest()
        self.download_urls = AlgoDownloadUrlMap()
        self.request.ParseFromString(req_buf)
        self.download_urls.ParseFromString(self.request.download_urls_buf)

        self.name = self.request.algo_name

        self.response = PyAlgoResponse()
        self.upload_urls = AlgoUploadUrlMap()

        self.lib = ctypes.cdll.LoadLibrary(lib_path)

    def process(self) -> bytes:
        print(f"[{self.name}]#process()")

        from audio_whisper_pb2 import AudioWhisperRequest, AudioWhisperResponse
        awr = AudioWhisperRequest.FromString(self.request.request_buf)
        print(f'real request: {awr}')
        print(f'real files: {self.download_urls}')

        awr_ = AudioWhisperResponse()
        awr_.language = 'en'
        awr_.audio_duration = 3000
        awr_.out_json_url = '/xxx/s/d.json'

        self.lib.HelloGo(b"Python 3")

        self.upload_urls.kvs.setdefault(awr_.out_json_url)

        self.response.response_buf = awr_.SerializeToString()
        self.response.upload_urls_buf = self.upload_urls.SerializeToString()
        return self.response.SerializeToString()

    def release(self):
        print(f"[{self.name}]#release()")


if __name__ == '__main__':
    test_data = os.path.abspath(os.getcwd() + "/../testdata")
    from py_algo_pb2 import PyAlgoRequest, PyAlgoResponse
    from algo_pb2 import AlgoDownloadUrlMap

    _proc = AlgoProcImpl('prj-export')
    _req = PyAlgoRequest()
    _req.algo_name = 'prj-export'
    with open(test_data + '/request.pbuf.txt', 'rb') as f:
        _req.request_buf = f.read()
    with open(test_data + '/request-files.pbuf.txt', 'rb') as f:
        _req.download_urls_buf = f.read()
    with open(test_data + '/request-full-python.pbuf.txt', 'wb') as f:
        f.write(_req.SerializeToString())
    _rep_buf = _proc.process(_req.SerializeToString())
    _proc.release()
    _rep = PyAlgoResponse.FromString(_rep_buf)
    print(f'output {type(_rep)}\n{_rep}')

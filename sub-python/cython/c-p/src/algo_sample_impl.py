import os

from algo_base import AlgoProcBase


class AlgoProcImpl(AlgoProcBase):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name):
        super().__init__(algo_name)

    def process(self, request_buf: bytes) -> bytes:
        print(f"[{self.name}]#process() {type(request_buf)}\n{request_buf}")
        # b'\n\nprj-export\x128\n0http://172.16.3.9/share/binlee/audio_whisper.wav\x10\x01*\x02en\x1a\x9f\x01\x12\x9c\x01\n0http://172.16.3.9/share/binlee/audio_whisper.wav\x12h"f/home/binlee/code/open-source/quvideo/algo-agent/testdata/tmp/2e38c54c-52a6-4c3e-8baf-a4715548dead.wav'
        # b'\n\tprj-parse\x12>\n0http://172.16.3.9/share/binlee/audio_whisper.wav\x10\x01*\x02en\x8fp4\xfaNV\x1a\x9f\x01\x12\x9c\x01\n0http://172.16.3.9/share/binlee/audio_whisper.wav\x12h"f/home/binlee/code/open-source/quvideo/algo-agent/testdata/tmp/2e38c54c-52a6-4c3e-8baf-a4715548dead.wav'

        from genpy.audio_whisper_pb2 import AudioWhisperRequest, AudioWhisperResponse
        from genpy.py_algo_spec_pb2 import PyAlgoRequest, PyAlgoResponse, AlgoDownloadUrlMap, AlgoUploadUrlMap
        request = PyAlgoRequest.FromString(request_buf)
        print(f"[{self.name}]#process() real {request}")
        awr = AudioWhisperRequest.FromString(request.request_buf)
        print(f'real request: {awr}\n{awr.SerializeToString()}')

        durls = AlgoDownloadUrlMap.FromString(request.download_urls_buf)
        print(f'real files:: {durls}\n{durls.SerializeToString()}')

        awr_ = AudioWhisperResponse()
        awr_.language = 'en'
        awr_.audio_duration = 3000
        awr_.out_json_url = '/xxx/s/d.json'

        uurls = AlgoUploadUrlMap()
        uurls.kvs.setdefault(awr_.out_json_url)

        resp = PyAlgoResponse(response_buf=awr_.SerializeToString(), upload_urls_buf=uurls.SerializeToString())
        return resp.SerializeToString()

    def release(self):
        print(f"[{self.name}]#release()")


if __name__ == '__main__':
    test_data = os.path.abspath(os.getcwd() + "/../testdata")
    from genpy.py_algo_spec_pb2 import PyAlgoRequest, PyAlgoResponse
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

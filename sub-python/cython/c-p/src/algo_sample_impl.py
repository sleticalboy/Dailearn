import time

from algo_base import AlgoProcBase
from genpy.algo_args_pb2 import AlgoRequest, AlgoResponse


class AlgoProc(AlgoProcBase):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name):
        super().__init__(algo_name)

    def process(self, args):
        super().process(args)
        if isinstance(args, str):
            args = args.encode()
        request = AlgoRequest()
        request.ParseFromString(args)
        print(f"p algo request is: {request}, type: {type(request)}")
        time.sleep(0.5)
        response = AlgoResponse()
        response.outputs['1'] = '1'
        response.outputs['2'] = '2'
        response.outputs['3'] = '3'
        # print(f"p algo output is: {o}, type: {type(o)}")
        return response.SerializeToString()

    def release(self):
        super().release()


if __name__ == '__main__':
    proc = AlgoProc('prj-export')
    req = AlgoRequest()
    req.inputs['a'] = 'a'
    req.inputs['b'] = 'b'
    req.inputs['c'] = 'c'
    buf = proc.process(req.SerializeToString())
    proc.release()
    print(f'output buf: {buf}, type: {type(buf)}')
    rep = AlgoResponse()
    rep.ParseFromString(buf)
    print(f'output is: {rep}, type: {type(rep)}')

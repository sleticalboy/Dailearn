import time

from algo_base import AlgoProcBase


class AlgoProc(AlgoProcBase):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name):
        super().__init__(algo_name)

    def process(self, args: dict) -> dict:
        super().process(args)
        time.sleep(0.5)
        rep = {
            "python": b"cython",
            "world": b"world"
        }
        print(f"p algo output is: {rep}, type: {type(rep)}")
        return rep

    def release(self):
        super().release()


if __name__ == '__main__':
    _proc = AlgoProc('prj-export')
    _req = {
        'hello': b'world',
        'bytes': b'bytes',
    }
    _rep = _proc.process(_req)
    _proc.release()
    print(f'output rep: {_rep}, type: {type(_rep)}')

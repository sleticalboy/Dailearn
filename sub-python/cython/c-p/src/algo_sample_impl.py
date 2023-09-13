import time

from algo_base import AlgoProcBase


class AlgoProcImpl(AlgoProcBase):
    """
    1、在 proto 中定义算法通用入参和结果并分别编译生成 python 和 c 源文件；
    2、c 将入参序列化为 proto 字节数组并送入 python 接口处理；
    3、python 将处理结果序列化为 proto 字节数组返回给 c；
    4、c 解析处理结果并转成结构体返回给 go；
    """

    def __init__(self, algo_name, input_keys, output_keys):
        super().__init__(algo_name, input_keys, output_keys)

    def process(self, args: dict) -> (dict, dict):
        super().process(args)
        self.check_input_args(args.keys())
        print('model is being initialized...')
        time.sleep(0.5)
        print('language is being detected...')
        time.sleep(0.5)
        print('result is being writing...')
        time.sleep(0.5)
        results = {
            "language": b"en",
            "duration_ms": 4000.67,
            "data": []
        }
        print(f"p algo output is: {results}, type: {type(results)}")
        return results, {
            'output_path': args['output_path']
        }

    def release(self):
        super().release()


if __name__ == '__main__':
    _proc = AlgoProcImpl('prj-export', {}, {})
    _req = {
        'hello': b'world',
        'bytes': b'bytes',
    }
    _rep = _proc.process(_req)
    _proc.release()
    print(f'output rep: {_rep}, type: {type(_rep)}')

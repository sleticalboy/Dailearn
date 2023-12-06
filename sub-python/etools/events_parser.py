import json
import os
import sys
import threading

import requests

j = {
    "process_event_type": 69,
    "video_export_event": {
        "cover_timestamp": 0,
        "effect_resources": {
            "3345931770x1043ade00": {
                "mask_filter_url": "https://rc.camdy.cn/vcm/20004/20231009/111910/6592156889649152/0x04000000000002DC.xyt",
                "mask_json_url": "http://static-qa.xiaoying.tv/make_video/avaclon/20231128/1b4404c6cd79847e-11360406-1701141770717643115.json",
                # "mask_url": "https://static-qa.xiaoying.tv/make_video/vcm/20231117/67e7876e9a8f11c7-11231354-1700213707696644505.zip",
                # "video_url": "http://static-qa.xiaoying.tv/make_video/avaclon/20231128/1b4404c6cd79847e-11360406-1701141770717643113.mp4"
            },
            "3345877370x10907b600": {
                "mask_filter_url": "https://rc.camdy.cn/vcm/20004/20231009/111910/6592156889649152/0x04000000000002DC.xyt",
                "mask_json_url": "http://static-qa.xiaoying.tv/make_video/avaclon/20231128/56f145d297c5d355-11360405-1701141762883969553.json",
                # "mask_url": "https://rc.camdy.cn/vcm/20004/20231122/20231122142332259.zip",
                # "video_url": "http://static-qa.xiaoying.tv/make_video/avaclon/20231128/56f145d297c5d355-11360405-1701141762883969551.mp4"
            }
        },
        "export_res": "https://static-qa.xiaoying.tv/309-1/meta_human/20231128/1701141722_1512416206878220288.zip"
    }
}


def super_setattr(obj, attr, value):
    if isinstance(obj, dict):
        obj.__setitem__(attr, value)
    elif isinstance(obj, list):
        obj.append(value)
    else:
        setattr(obj, attr, value)
    pass


def replace_value(obj, key, value):
    if not isinstance(value, str) or not value.startswith('http'):
        super_setattr(obj, key, value)
    else:
        print(f'start download: {key} -> {value}')
        FileReplacer(value, obj, key).start()


class FileReplacer(threading.Thread):
    # 类属性
    workers = []
    locker = threading.Lock()
    work_dir = '/tmp'

    def __init__(self, url, obj, attr):
        super(FileReplacer, self).__init__()
        # 对象属性
        self.url = url
        self.obj = obj
        self.attr = attr
        self.progress = 0
        self.filename = os.path.join(self.__class__.work_dir, self.url[self.url.rfind('/') + 1:])

    def start(self) -> None:
        super().start()
        self.__class__.workers.append(self)

    def run(self):
        with requests.get(self.url) as res, open(self.filename, 'wb') as fp:
            self.__write_file__(res, fp)
        self.__unzip_file__()
        self.__class__.locker.acquire()
        super_setattr(self.obj, self.attr, self.filename)
        self.__class__.locker.release()

    def __write_file__(self, res, fp):
        size = 0
        total = int(res.headers.get('content-length', 0))
        for chunk in res.iter_content(chunk_size=2048):
            size += fp.write(chunk)
            self.__on_progress__(int(float(size) / total * 100))
        pass

    def __unzip_file__(self):
        if self.filename.endswith('.zip'):
            temp_name = self.filename.replace('.zip', '')
            os.system(f'mkdir -p {temp_name} && unzip -o -q {self.filename} -d {temp_name}')
            self.filename = temp_name
        pass

    def __on_progress__(self, progress):
        if progress != self.progress and progress % 10 == 0:
            print(f'write_file() {self.url} -> {self.filename}, progress: {progress}%')
            self.progress = progress

    @classmethod
    def wait(cls):
        for t in cls.workers:
            t.join()


class JsonParser:

    def __init__(self, obj_type, data, work_dir='/tmp', key_mapper=None):
        self.obj_type = obj_type
        self.data = data
        self.key_mapper = key_mapper
        FileReplacer.work_dir = work_dir

        print(f'parse_obj() data: {data}')

    def value_type(self, key, value_type):
        return self.key_mapper(key) if callable(self.key_mapper) else value_type

    def submit(self, cls: type, data):
        obj = cls()
        if isinstance(data, list):
            for it in data:
                replace_value(obj, None, it)
                pass
        elif isinstance(data, dict):
            for k, v in data.items():
                if type(v) == dict:
                    super_setattr(obj, k, self.submit(self.value_type(k, dict), v))
                elif type(v) == list:
                    super_setattr(obj, k, self.submit(self.value_type(k, list), v))
                else:
                    replace_value(obj, k, v)
        return obj

    def parse(self):
        obj = self.submit(self.obj_type, self.data)
        FileReplacer.wait()
        if self.obj_type == dict:
            return obj
        return json.dumps(obj, default=lambda o: vars(o))


if __name__ == '__main__':
    print(sys.argv)
    event_body = j['video_export_event']


    def _key_mapper(key: str) -> type:
        return dict

    json_obj = JsonParser(dict, event_body, '/tmp', key_mapper=_key_mapper).parse()
    print(json_obj)
    pass

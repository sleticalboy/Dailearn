import os
import shutil

hints: list[str] = [
    "算法库目录（绝对路径）：",
    "算法接口类型，（普通（0）、统一接口（1）：",
    "算法类型（正整数）：",
    "算法组件根目录（绝对路径）：",
    "算法组件名（多个单词时以空格分割）：",
]
header_map = {
    '"XYAICommon.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAICommon.h"',
    '<XYAICommon.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAICommon.h"',
    '"XYAISDK.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAISDK.h"',
    '<XYAISDK.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAISDK.h"',
    '"XYAIStructExchange.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAIStructExchange.h"',
    '<XYAIStructExchange.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAIStructExchange.h"',
    '"method_tracer.h"': '"../../../../AlgoBase/commonAI/src/main/jni/method_tracer.h"',
    '<method_tracer.h>': '"../../../../AlgoBase/commonAI/src/main/jni/method_tracer.h"',
}


def tree_dir(path: str, trim: str = '', level: int = 0):
    if os.path.isfile(path):
        print('  ' * level + os.path.basename(path))
    else:
        print('  ' * level + os.path.basename(path))
        for child in os.listdir(path):
            tree_dir(path + '/' + child, trim, level + 1)


def __find_files__(path: str, output: list[str], suffix: str):
    if os.path.isfile(path):
        if path.find(suffix) >= 0:
            output.append(path)
    elif os.path.isdir(path):
        for child in os.listdir(path):
            __find_files__(path + '/' + child, output, suffix)


def find_file(root: str, sub_dirs: list[str] = None, suffixes: list[str] = None) -> list[str]:
    files: list[str] = []
    if sub_dirs and len(sub_dirs) > 0:
        for sub_dir in sub_dirs:
            path = f"{root}/{sub_dir}"
            if os.path.exists(path):
                if suffixes and len(suffixes) > 0:
                    for suffix in suffixes:
                        __find_files__(path, files, suffix)
                else:
                    files.append(path)
    else:
        if suffixes and len(suffixes) > 0:
            for suffix in suffixes:
                __find_files__(root, files, suffix)
    return files


class RawDir:
    """
    算法库目录，一般结构如下
    include：头文件
    model：模型文件
    releaseNote.txt：升级日志
    lib：库文件
    """

    def __init__(self, root: str):
        if not os.path.exists(root):
            print(f"'{root}' 不存在！")
            exit(1)
        # tree_dir(root, root)
        # 更新日志: [README.md, releasenote.txt]
        self.release_notes = find_file(root, suffixes=['.md', '.txt'])
        print(f"release notes: {self.release_notes}")

        # 库文件: lib/[android, Android]/[arm64-v8a, arm32-v8a, armeabi-v7a]/*.a
        self.library_files = find_file(root + "/lib", ['android', 'Android'], suffixes=['.a'])
        print(f"libraries: {self.library_files}")

        # 头文件：include/*.h
        self.header_files = find_file(root + '/include', suffixes=['.h'])
        print(f"headers: {self.header_files}")

        # 模型文件：model/**/[*.json, *.xymodel]
        self.model_files = find_file(root + '/model', suffixes=['.json', '.xymodel'])
        print(f"models: {self.model_files}")

    def __str__(self):
        formatter = "release notes:{}\nlibraries:{}接口\nheaders:{}\nmodels:{}"
        return formatter.format(self.release_notes, self.library_files, self.header_files, self.model_files)


class PrjGenerator:
    def __init__(self, path: str, name: str):
        self.path = path
        self.name = os.path.basename(path)
        self.lower_name = name.lower().replace(' ', '_')
        self.pkg = self.lower_name.replace('_', '')

    def __str__(self):
        return f"path: {self.path}\nname: {self.name}, pkg: {self.pkg}, lower: {self.lower_name}"

    def make(self, raw_dir: RawDir):
        # algo_tempate_module
        # {self.path}/src/main/assets/engine/ai/{}/{model_files}
        # {self.path}/src/main/java/com/quvideo/mobile/component/{java_files}
        # {self.path}/src/main/AndroidManifest.xml
        # {self.path}/src/main/jni/arm64-v8a/{.a}
        # {self.path}/src/main/jni/armeabi-v7a/{.a}
        # {self.path}/src/main/jni/{release_notes}
        # {self.path}/src/main/jni/{header_files}
        # {self.path}/src/main/jni/{}_jni.cpp

        if os.path.exists(self.path):
            shutil.rmtree(self.path)

        # /home/binlee/code/Dailearn/pytest/out/algo_tempate_module

        # 模型文件
        assets_dir = f"{self.path}/src/main/assets/engine/ai/{self.lower_name}"
        os.makedirs(assets_dir)
        for item in raw_dir.model_files:
            shutil.copyfile(item, assets_dir + '/' + os.path.basename(item), follow_symlinks=False)

        # java 文件
        java_dir = f"{self.path}/src/main/java/com/quvideo/mobile/component/{self.pkg}"
        os.makedirs(java_dir)
        # 生成对应的 java 文件
        
        # 清单文件
        with open(f"{self.path}/src/main/AndroidManifest.xml", mode='w') as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(f'  <manifest package="com.quvideo.mobile.component.ai.{self.pkg}" />')

        # jni 相关
        # 库文件 ['arm64-v8a', 'arm32-v8a', 'armeabi-v7a']
        v8a_dir = f"{self.path}/src/main/jni/arm64-v8a"
        os.makedirs(v8a_dir)
        v7a_dir = f"{self.path}/src/main/jni/armeabi-v7a"
        os.makedirs(v7a_dir)
        for item in raw_dir.library_files:
            shutil.copyfile(src=item,
                            dst=(v8a_dir if item.find('arm64') >= 0 else v7a_dir) + '/' + os.path.basename(item),
                            follow_symlinks=False)
        # 头文件
        jni_dir = f"{self.path}/src/main/jni"
        for item in raw_dir.header_files:
            with open(jni_dir + '/' + os.path.basename(item), 'w') as dst:
                with open(item, 'r') as src:
                    # 修改 include 路径
                    for line in src.readlines():
                        if line.find('.h') >= 0:
                            for k, v in header_map.items():
                                if line.find(k) >= 0:
                                    line = line.replace(k, v)
                                    break
                        dst.write(line)
        # jni 实现
        with open(f"{jni_dir}/{self.lower_name}_jni.cpp") as f:
            # 通过模板读取
            pass

        # {self.path}/.gitignore
        with open(f"{self.path}/.gitignore", mode='w') as f:
            f.writelines('build\n.cxx\nlibs/arm*\n')

        # {self.path}/build.gradle
        with open(f"{self.path}/build.gradle", mode='w') as f:
            # 通过模板读取
            pass

        # {self.path}/CMakeLists.txt
        with open(f"{self.path}/CMakeLists.txt", mode='w') as f:
            # 通过模板读取
            pass

        # 混淆规则
        with open(f"{self.path}/consumer-rules.pro", mode='w') as f:
            f.write(f"-keep class com.quvideo.mobile.component.{self.pkg}.** " + "{*;}")
        with open(f"{self.path}/proguard-rules.pro", mode='w') as f:
            f.write(f"-keep class com.quvideo.mobile.component.{self.pkg}.** " + "{*;}")


class ModuleSpec:
    def __init__(self, root: str, name: str):
        parent = os.path.dirname(root)
        if not os.path.exists(parent):
            print(f"'{parent}' 不存在！")
            exit(1)

        module_dir = f"{root}/Component-AI/{name.title().replace(' ', '')}AI"
        self.__generator = PrjGenerator(module_dir, name)

    def __str__(self):
        return self.__generator.__str__()

    def generate(self, raw_dir: RawDir):
        self.__generator.make(raw_dir)


class Algo:
    raw_dir: RawDir
    itf_type: int = 0
    ai_type: int = -1
    module: ModuleSpec

    def __init__(self, algo_dir: str = '', itf_type: int = 0, ai_type: int = -1, module_dir: str = '',
                 module_name: str = ''):
        self.raw_dir = RawDir(algo_dir)
        self.itf_type = itf_type
        self.ai_type = ai_type
        self.module = ModuleSpec(module_dir, module_name)

    def create_module(self):
        self.module.generate(self.raw_dir)

    def __str__(self):
        formatter = "算法目录：{}\n接口类型：{}接口\n算法类型：{}\nmodule {}"
        return formatter.format(self.raw_dir, "普通" if self.itf_type == 0 else "统一", self.ai_type, self.module)


def usage():
    print("=====================菜单=====================")
    for hint in hints:
        print(hint)
    print("=====================菜单=====================")


if __name__ == '__main__':
    usage()

    # a = Algo(input("算法库目录（绝对路径）："),
    #          int(input("算法接口类型，（普通（0）、统一接口（1）：")),
    #          int(input("算法类型（正整数）：")),
    #          input("算法组件工程目录（绝对路径）："),
    #          input("算法组件名（多个单词时以空格分割）：").lower())
    a = Algo(algo_dir='/home/binlee/code/open-source/quvideo/XYAlgLibs/AutoCrop-component/ImageRestore',
             itf_type=0,
             ai_type=200,
             module_dir='/home/binlee/code/Dailearn/pytest/out',
             module_name='image restore test'.lower())
    a.create_module()

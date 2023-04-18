import os
import re
import shutil

hints: list[str] = [
    "算法库目录（绝对路径）：",
    "算法接口类型，（普通（0）、统一接口（1）：",
    "算法类型（正整数）：",
    "算法组件根目录（绝对路径）：",
    "算法组件名（多个单词时以空格分割）：",
]
jni_header_map = {
    '"XYAICommon.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAICommon.h"',
    '<XYAICommon.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAICommon.h"',
    '"XYAISDK.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAISDK.h"',
    '<XYAISDK.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAISDK.h"',
    '"XYAIStructExchange.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAIStructExchange.h"',
    '<XYAIStructExchange.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAIStructExchange.h"',
    '"method_tracer.h"': '"../../../../AlgoBase/commonAI/src/main/jni/method_tracer.h"',
    '<method_tracer.h>': '"../../../../AlgoBase/commonAI/src/main/jni/method_tracer.h"',
}
tmplt_map = {
    '{imported-headers}': '头文件',
    '{module-name}': '算法模块名',
    '{algo-nss}': '算法命名空间',
    '{itf-name}': '算法统一接口名字',
    '{pkg}': '包名',
    '{ai-type}': '算法类型',
    '{lower-name}': '大写_模块名',
    '{upper-name}': '小写_模块名',
    '{algo-lib-name}': '算法静态库名，去掉前缀和后缀',
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


def read_content(path: str) -> list[str]:
    with open(path, mode='r') as f:
        return f.readlines()


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
        self.name = name.title().replace(' ', '')
        self.lower_name = name.lower().replace(' ', '_')
        self.pkg = self.lower_name.replace('_', '')

    def __str__(self):
        return f"path: {self.path}\nname: {self.name}, pkg: {self.pkg}, lower: {self.lower_name}"

    def make(self, raw_dir: RawDir, ai_type: int):
        # {self.path}/src/main/assets/engine/ai/{self.lower_name}/{model_files}
        # {self.path}/src/main/java/com/quvideo/mobile/component/{java_files}
        # {self.path}/src/main/AndroidManifest.xml
        # {self.path}/src/main/jni/arm64-v8a/{.a}
        # {self.path}/src/main/jni/armeabi-v7a/{.a}
        # {self.path}/src/main/jni/{release_notes}
        # {self.path}/src/main/jni/{header_files}
        # {self.path}/src/main/jni/{self.lower_name}_jni.cpp

        if os.path.exists(self.path):
            shutil.rmtree(self.path)

        tmplt = '/home/binlee/code/android/Dailearn/pytest/src/etools/templates'

        # 模型文件
        assets_dir = f"{self.path}/src/main/assets/engine/ai/{self.lower_name}"
        os.makedirs(assets_dir)
        for item in raw_dir.model_files:
            shutil.copyfile(item, assets_dir + '/' + os.path.basename(item), follow_symlinks=False)

        # java 文件
        java_dir = f"{self.path}/src/main/java/com/quvideo/mobile/component/{self.pkg}"
        os.makedirs(java_dir)
        # 根据模板生成对应的 java 文件，一般来讲有三个文件
        # AI{self.name}.java 提供给业务使用
        # QE{self.name}Client.java 提供给业务使用（组件内部也会使用）
        # Q{self.name}.java 提供给引擎使用
        with open(java_dir + f'/Q{self.name}.java', mode='w') as f:
            pass
        with open(java_dir + f'/QE{self.name}.java', mode='w') as f:
            pass
        with open(java_dir + f'/AI{self.name}.java', mode='w') as f:
            pass

        # 清单文件
        with open(f"{self.path}/src/main/AndroidManifest.xml", mode='w') as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(f'  <manifest package="com.quvideo.mobile.component.ai.{self.pkg}" />')

        # jni 相关
        jni_dir = f"{self.path}/src/main/jni"
        os.makedirs(jni_dir)
        # 升级日志
        if len(raw_dir.release_notes) > 0:
            with open(jni_dir + '/' + os.path.basename(raw_dir.release_notes[0]), mode='w') as f:
                f.writelines(read_content(raw_dir.release_notes[0]))
        # 库文件 ['arm64-v8a', 'arm32-v8a', 'armeabi-v7a']
        v8a_dir = f"{jni_dir}/arm64-v8a"
        os.makedirs(v8a_dir)
        v7a_dir = f"{jni_dir}/armeabi-v7a"
        os.makedirs(v7a_dir)
        for item in raw_dir.library_files:
            shutil.copyfile(src=item,
                            dst=(v8a_dir if item.find('arm64') >= 0 else v7a_dir) + '/' + os.path.basename(item),
                            follow_symlinks=False)
        # 头文件
        # 顺便分析文件内容: 文件名、统一接口名、命名空间
        headers: list[str] = []
        itf_pattern = re.compile(r'class XYAI_PUBLIC (.+?) : public XYAISDK::AlgBase \{')
        itf_name = ''
        ns_pattern = re.compile(r"namespace (.+?) \{")
        algo_nss: set[str] = set()
        for item in raw_dir.header_files:
            # 记录文件名
            headers.append(f'#include "{os.path.basename(item)}"')
            with open(jni_dir + '/' + os.path.basename(item), 'w') as f:
                for line in read_content(item):
                    # 命名空间
                    ret: list[str] = ns_pattern.findall(line)
                    if ret and len(ret) > 0:
                        algo_nss.add(f"using namespace {ret[0]};")
                    # 统一接口名
                    ret: list[str] = itf_pattern.findall(line)
                    if ret and len(ret) > 0:
                        itf_name = ret[0]

                    # 修改 include 路径
                    if line.find('.h') >= 0:
                        for k, v in jni_header_map.items():
                            if line.find(k) >= 0:
                                line = line.replace(k, v)
                                break
                    f.write(line)
        # jni 实现
        with open(f"{jni_dir}/{self.lower_name}_jni.cpp", mode='w') as f:
            # 通过模板读取
            for line in read_content(tmplt + '/lower_name_jni.cpp'):
                if line.find('{imported-headers}') >= 0:
                    # 从 header 中解析出来
                    line = line.replace('{imported-headers}', '\n'.join(headers))
                elif line.find('{algo-nss}') >= 0:
                    # 从 header 中解析出来
                    line = line.replace('{algo-nss}', '\n'.join(algo_nss))
                elif line.find('{itf-name}') >= 0:
                    # 从 header 中解析出来
                    line = line.replace('{itf-name}', itf_name)
                elif line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', str(ai_type))
                else:
                    # '{module-name}' 和 '{pkg}' 可能出现在同一行
                    if line.find('{module-name}') >= 0:
                        line = line.replace('{module-name}', self.name)
                    if line.find('{pkg}') >= 0:
                        line = line.replace('{pkg}', self.pkg)
                f.write(line)

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

    def generate(self, raw_dir: RawDir, ai_type: int):
        self.__generator.make(raw_dir, ai_type)


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
        self.module.generate(self.raw_dir, self.ai_type)

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
    a = Algo(algo_dir='/home/binlee/code/quvideo/XYAlgLibs/AutoCrop-component/ImageRestore',
             itf_type=0,
             ai_type=200,
             module_dir='/home/binlee/code/android/Dailearn/pytest/out',
             module_name='image restore test'.lower())
    a.create_module()

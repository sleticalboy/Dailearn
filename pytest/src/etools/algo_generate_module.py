import os
import re
import shutil
import sys

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
replace_map = {
    '{imported-headers}': '头文件',
    '{module-name}': '算法模块名',
    '{algo-nss}': '算法命名空间',
    '{itf-name}': '算法统一接口名字',
    '{pkg-name}': '包名',
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


def get_suffix(path: str) -> str:
    index = path.rfind('.')
    if index >= 0:
        return path[index:]
    return ''


def __find_files__(path: str, output: list[str], suffixes: list[str]):
    if os.path.isfile(path):
        if get_suffix(path) in suffixes:
            output.append(path)
    elif os.path.isdir(path):
        for child in os.listdir(path):
            __find_files__(path + '/' + child, output, suffixes)


def find_file(root: str, sub_dirs: list[str] = None, suffixes: list[str] = None) -> list[str]:
    files: list[str] = []
    if sub_dirs and len(sub_dirs) > 0:
        for sub_dir in sub_dirs:
            path = root + '/' + sub_dir
            if os.path.exists(path):
                if suffixes and len(suffixes) > 0:
                    __find_files__(path, files, suffixes)
                else:
                    files.append(path)
    else:
        if suffixes and len(suffixes) > 0:
            __find_files__(root, files, suffixes)
    return files


def read_content(path: str) -> list[str]:
    with open(path, mode='r') as f:
        return f.readlines()


class AlgoSpec:
    """
    算法库目录，一般结构如下
    include：头文件
    model：模型文件
    releaseNote.txt：升级日志
    lib：库文件
    """

    def __init__(self, root: str, ai_type: str):
        if not os.path.exists(root):
            print(f"'{root}' 不存在！", file=sys.stderr)
            exit(1)
        self.ai_type = ai_type
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
    def __init__(self, path: str, name: str, name_zh: str):
        self.path = path
        # 驼峰
        self.module_name = name.title().replace(' ', '')
        # java package name 规则
        self.pkg_name = name.lower().replace(' ', '')
        # 全_小写
        self.lower_name = name.lower().replace(' ', '_')
        # 全_大写
        self.upper_name = name.upper().replace(' ', '_')
        # 中文名
        self.name_zh = name_zh

    def __str__(self):
        return f"path: {self.path}\nname: {self.module_name}, pkg: {self.pkg_name}, lower: {self.lower_name}"

    def make(self, algo_spec: AlgoSpec):
        # 测试代码
        # if os.path.exists(self.path):
        #     shutil.rmtree(self.path)
        # 模板路径，在工程目录下
        tmplt = '/home/binlee/code/Dailearn/pytest/src/etools/templates'

        # {self.path}/.gitignore
        with open(f"{self.path}/.gitignore", mode='w') as f:
            f.writelines('build\n.cxx\nlibs/arm*\n')

        # 清单文件
        with open(f"{self.path}/src/main/AndroidManifest.xml", mode='w') as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(f'  <manifest package="com.quvideo.mobile.component.ai.{self.pkg_name}" />')

        # 模型文件
        assets_dir = f"{self.path}/src/main/assets/engine/ai/{self.lower_name}"
        os.makedirs(assets_dir)
        for item in algo_spec.model_files:
            shutil.copyfile(item, assets_dir + '/' + os.path.basename(item), follow_symlinks=False)

        # native 相关
        self.__generate_native__(algo_spec, tmplt)

        # java 文件
        self.__generate_java__(algo_spec, tmplt)

        # 以上都没有出错，修改 settings.gradle、config.gradle、upload-to-maven.sh
        self.__modify_compile_scripts__()

    def __generate_java__(self, algo_spec: AlgoSpec, tmplt: str):
        # AIConstants.java 中定义了算法模块常量，这里更新一下算法类型
        ai_constants = f'{os.path.dirname(self.path)}/AlgoBase/commonAI/src/main/java/' \
                       f'com/quvideo/mobile/component/common/AIConstants.java'
        if not os.path.exists(ai_constants):
            print(f"'{ai_constants}' 不存在！", file=sys.stderr)
            exit(1)
        raw_lines = read_content(ai_constants)
        with open(ai_constants, mode='w') as f:
            for line in raw_lines:
                if line.find('{module-name-zh}') >= 0:
                    line = line.replace('{module-name-zh}', self.name_zh).replace('//', '')
                    f.write(line)
                else:
                    write_placeholder = False
                    if line.find('{upper-name}') >= 0:
                        line = line.replace('{upper-name}', self.upper_name)
                        write_placeholder = True
                    if line.find('{ai-type}') >= 0:
                        line = line.replace('{ai-type}', algo_spec.ai_type).replace('//', '')
                        write_placeholder = True
                    f.write(line)
                    if write_placeholder:
                        f.write('  ///** 算法类型-{module-name-zh} */\n')
                        f.write('  //public static final int AI_TYPE_{upper-name} = {ai-type};\n')

        # java 文件
        java_dir = f"{self.path}/src/main/java/com/quvideo/mobile/component/{self.pkg_name}"
        os.makedirs(java_dir)
        # 根据模板生成对应的 java 文件，一般来讲有三个文件
        # AI{self.name}.java 提供给业务使用
        # QE{self.name}Client.java 提供给业务使用（组件内部也会使用）
        # Q{self.name}.java 提供给引擎使用
        with open(java_dir + f'/Q{self.module_name}.java', mode='w') as f:
            for line in read_content(tmplt + '/QModuleName.java.tmplt'):
                # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                if line.find('{pkg-name}') >= 0:
                    line = line.replace('{pkg-name}', self.pkg_name)
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                f.write(line)
        with open(java_dir + f'/QE{self.module_name}Client.java', mode='w') as f:
            for line in read_content(tmplt + '/QEModuleNameClient.java.tmplt'):
                # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                if line.find('{pkg-name}') >= 0:
                    line = line.replace('{pkg-name}', self.pkg_name)
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                if line.find('{lower-name}') >= 0:
                    line = line.replace('{lower-name}', self.lower_name)
                if line.find('{upper-name}') >= 0:
                    line = line.replace('{upper-name}', self.upper_name)
                if line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', algo_spec.ai_type)
                f.write(line)
        with open(java_dir + f'/AI{self.module_name}.java', mode='w') as f:
            for line in read_content(tmplt + '/AIModuleName.java.tmplt'):
                # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                if line.find('{pkg-name}') >= 0:
                    line = line.replace('{pkg-name}', self.pkg_name)
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                f.write(line)

        # 混淆规则
        with open(f"{self.path}/consumer-rules.pro", mode='w') as f:
            f.write(f"-keep class com.quvideo.mobile.component.{self.pkg_name}.** " + "{*;}")
        with open(f"{self.path}/proguard-rules.pro", mode='w') as f:
            f.write(f"-keep class com.quvideo.mobile.component.{self.pkg_name}.** " + "{*;}")

        # {self.path}/build.gradle.tmplt
        with open(f"{self.path}/build.gradle.tmplt", mode='w') as f:
            # 通过模板读取
            for line in read_content(tmplt + '/build.gradle.tmplt'):
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                if line.find('{upper-name}') >= 0:
                    line = line.replace('{upper-name}', self.upper_name)
                if line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', algo_spec.ai_type)
                f.write(line)

    def __generate_native__(self, algo_spec: AlgoSpec, tmplt: str):
        # jni 相关
        jni_dir = f"{self.path}/src/main/jni"
        os.makedirs(jni_dir)

        # 升级日志
        if len(algo_spec.release_notes) > 0:
            with open(jni_dir + '/' + os.path.basename(algo_spec.release_notes[0]), mode='w') as f:
                f.writelines(read_content(algo_spec.release_notes[0]))

        # 库文件 ['arm64-v8a', 'arm32-v8a', 'armeabi-v7a']
        v8a_dir = f"{jni_dir}/arm64-v8a"
        os.makedirs(v8a_dir)
        v7a_dir = f"{jni_dir}/armeabi-v7a"
        os.makedirs(v7a_dir)
        libraries: list[str] = []
        for item in algo_spec.library_files:
            lib_name: str = os.path.basename(item)
            if lib_name not in libraries:
                libraries.append(lib_name.replace('lib', '').replace('.a', ''))
            shutil.copyfile(src=item,
                            dst=(v8a_dir if item.find('arm64') >= 0 else v7a_dir) + '/' + os.path.basename(item),
                            follow_symlinks=False)

        # 拷贝头文件时顺便分析文件内容: 文件名、统一接口名、命名空间
        headers: list[str] = []
        itf_pattern = re.compile(r'class XYAI_PUBLIC (.+?) : public XYAISDK::AlgBase \{')
        itf_name = ''
        ns_pattern = re.compile(r"namespace (.+?) \{")
        algo_nss: set[str] = set()
        for item in algo_spec.header_files:
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
        # jni 实现，通过模板生成 cpp 文件
        with open(f"{jni_dir}/{self.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_content(tmplt + '/lower_name_jni_0.cpp.tmplt'):
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
                    line = line.replace('{ai-type}', algo_spec.ai_type)
                else:
                    # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                    if line.find('{pkg-name}') >= 0:
                        line = line.replace('{pkg-name}', self.pkg_name)
                    if line.find('{module-name}') >= 0:
                        line = line.replace('{module-name}', self.module_name)
                f.write(line)

        # {self.path}/CMakeLists.txt.tmplt
        with open(f"{self.path}/CMakeLists.txt.tmplt", mode='w') as f:
            # 通过模板读取
            for line in read_content(tmplt + '/CMakeLists.txt.tmplt'):
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                if line.find('{lower-name}') >= 0:
                    line = line.replace('{lower-name}', self.lower_name)
                if line.find('{algo-lib-name}') >= 0:
                    line = line.replace('{algo-lib-name}', libraries[0])
                f.write(line)

    def __modify_compile_scripts__(self):
        # 修改 settings.gradle、config.gradle、upload-to-maven.sh
        settings: str = os.path.dirname(os.path.dirname(self.path)) + '/settings.gradle'
        if not os.path.exists(settings):
            print(f"'{settings}' 不存在！")
            exit(1)
        raw_lines = read_content(settings)
        with open(settings, mode='w') as f:
            for line in raw_lines:
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', os.path.basename(self.path)).replace('//', '')
                    line = line[:line.find(' /* ')]
                    f.write(line)
                    f.write("\n//include ':Component-AI:{module-name}' /* 这一行是模板占位符，不要删除！！*/\n")
                else:
                    f.write(line)
        # config.gradle upload-to-maven.sh
        config: str = os.path.dirname(os.path.dirname(self.path)) + '/config.gradle'
        if not os.path.exists(config):
            print(f"'{config}' 不存在！")
            exit(1)
        raw_lines = read_content(config)
        with open(config, mode='w') as f:
            for line in raw_lines:
                if line.find('{module-name-zh}') >= 0:
                    line = line.replace('{module-name-zh}', self.name_zh)
                    f.write(line)
                elif line.find('{module-name}') >= 0:
                    left = line.find('{module-name}')
                    right = line.rfind('{module-name}')

                    line = line.replace('{module-name}', self.module_name).replace('//', '')
                    f.write(line)

                    if left == right:
                        f.write('    //lib{module-name},\n')
                    else:
                        f.write("  // {module-name-zh}\n")
                        f.write("  //lib{module-name} = [artifact: '{module-name}AI',"
                                " module: ':Component-AI:{module-name}AI']\n")
                else:
                    f.write(line)

        compile_script: str = os.path.dirname(os.path.dirname(self.path)) + '/upload-to-maven.sh'
        if not os.path.exists(compile_script):
            print(f"'{compile_script}' 不存在！")
            exit(1)
        raw_lines = read_content(compile_script)
        new_lines: list[str] = []
        for index in range(len(raw_lines)):
            line = raw_lines[index]
            if line.find('{module-name}') >= 0:
                # 先处理上面一行，使用 pop() 方法删除上一个元素，处理之后再追加
                prev_line = new_lines.pop().replace('\n', '')
                new_lines.append(prev_line + ' \\\n')
                # 处理这一行
                line = line.replace('{module-name}', self.module_name).replace('#', '')
                new_lines.append(line[:line.find(' \\ /*')] + '\n')
                # 追加后面的
                new_lines.append('    #:Component-AI:{module-name}:"$task" \\ /* 这一行是模板占位符，不要删除！！*/\n')
            else:
                new_lines.append(line)
        with open(compile_script, mode='w') as f:
            for line in new_lines:
                f.write(line)


class ModuleSpec:
    def __init__(self, root: str, name: str, name_zh: str):
        parent = os.path.dirname(root)
        if not os.path.exists(parent):
            print(f"'{parent}' 不存在！")
            exit(1)

        module_dir = f"{root}/Component-AI/{name.title().replace(' ', '')}AI"
        self.__generator = PrjGenerator(module_dir, name, name_zh)

    def __str__(self):
        return self.__generator.__str__()

    def generate(self, raw_dir: AlgoSpec):
        self.__generator.make(raw_dir)


class GenerateTask:
    algo: AlgoSpec
    module: ModuleSpec

    def __init__(self, algo_dir: str = '', ai_type: str = '', algo_root: str = '',
                 module_name: str = '', module_name_zh: str = ''):
        self.algo = AlgoSpec(algo_dir, ai_type)
        self.module = ModuleSpec(algo_root, module_name, module_name_zh)

    def create_module(self):
        self.module.generate(self.algo)


def usage():
    menus: list[str] = [
        '操作类型（a 新增算法组件，u 更新算法组件）：a（这里是新增）',
        "算法库目录（绝对路径）：/home/binlee/code/XYAlgLibs/AutoCrop-component/ImageRestore（这里是画质修复算法库）",
        "## 如果是更新库，下面的操作都不需要了",
        "算法类型（正整数且不能与现有算法类型重复）：24（请与 iOS 协商好）",
        "算法组件名（多个单词时以空格分割）：image restore v2（程序内部会处理成首字母大写、剔除空格、驼峰命名等）",
        "算法组件名（中文）：画质修复 v2（用于生成代码注释）",
    ]
    print("=====================菜单=====================")
    print("示例：")
    for item in menus:
        print(item)
    print("=====================菜单=====================")


if __name__ == '__main__':
    usage()

    prj_root = os.path.dirname(__file__)
    if not os.path.exists(prj_root + '/Component-AI'):
        # 脚本必须在算法工程根目录执行
        print(f"脚本必须在算法工程根目录执行！当前：'{prj_root}'", file=sys.stderr, flush=True)
        exit(1)

    # task = GenerateTask(input("操作类型（a 新增算法组件，u 更新算法组件）："),
    #                     input("算法库目录（绝对路径）："),
    #                     input("算法类型（正整数）："),
    #                     input("算法组件名（多个单词时以空格分割）：").lower(),
    #                     input("算法组件名（中文）：").lower())
    task = GenerateTask(algo_dir='/home/binlee/code/open-source/quvideo/XYAlgLibs/AutoCrop-component/ImageRestore',
                        ai_type='24',
                        algo_root=prj_root,
                        module_name='image restore v2'.lower(),
                        module_name_zh='画质修复 v2')
    # task.create_module()

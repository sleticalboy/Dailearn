import os
import re
import shutil
import sys

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
    '{jni-methods}': 'jni 方法',
    '{jni-register-methods}': 'jni注册方法',
}


# ai type -> java type & sig
types_map = {
    'int': 'jint I',
    'XYAIImageFormat': 'jint I',
    'XYInt32': 'jint I',
    'XYFloat': 'jfloat F',
    'XYDouble': 'jdouble D',
    'XYHandle': 'jlong J',
    'XYLong': 'jlong J',
    'XYBool': 'jboolean Z',
    'XYChar': 'jchar C',
    'XYShort': 'jshort S',
    'void': 'void V',
    'XYVoid': 'void V',
    'XYString': 'jstring Ljava/lang/String;',
    'std::vector<float>': 'jobject Ljava/util/ArrayList; F',
    'XYAIFrameInfo': 'jobject Lcom/quvideo/mobile/component/common/AIFrameInfo;',
    'XYAIRect': 'jobject Lcom/quvideo/mobile/component/common/AIRectF;',
    # c 中定义的枚举和结构体可以在这里根据代码动态添加
}


class Struct:
    # 方法、结构体、枚举
    def __init__(self, raw: str):
        self.rtype = ''
        self.__parse__(raw)

    def __str__(self):
        return f"{self.rtype} {self.name} -> {self.fields}"

    def is_method(self) -> bool:
        return self.rtype != ''

    def __parse__(self, raw: str):
        # 第一个左括号
        first_index = raw.find('(')
        # 如果是方法
        if first_index >= 0:
            # 左半部分以空格分割
            first_half = raw[:first_index].split()
            # 方法名
            self.name = first_half[len(first_half) - 1]
            # 返回值
            if first_half[0].startswith('XYAI_'):
                self.rtype = first_half[1]
            else:
                self.rtype = first_half[0]
            fields_str = raw[first_index + 1: raw.rfind(')')]
        else:
            # enum PadType{BLACK = 0,WHITE = 1,BORDER_MEAN = 2,BORDER_BLUR = 3,BORDER_MIRROR = 4,NONE = 10};
            first_index = raw.find('{')
            # 结构体名字
            if raw.find('typedef struct') >= 0:
                segments = raw.replace('typedef struct', '').strip().split()
            else:
                segments = raw[:first_index].replace('enum', '').strip().split()
            length = len(segments)
            if segments[length - 1].find('}') >= 0:
                self.name = segments[0]
            else:
                self.name = segments[length - 1].replace(';', '')
            fields_str = raw[first_index + 1: raw.rfind('}')]

        fields: set[str] = set()
        if self.rtype == '':
            # 枚举或结构体
            if raw.find('enum') >= 0:
                # 枚举，分割后得到 k = 0 结构
                for kv in fields_str.split(','):
                    fields.add(kv)
            elif raw.find('struct') >= 0:
                # 结构体，分割后得到的结构和方法类似
                for kv in fields_str.split(';'):
                    if kv:
                        fields.add(kv)
        else:
            # 方法
            if fields_str.find(',') >= 0:
                # 多个参数
                for kv in fields_str.split(','):
                    fields.add(kv.replace('const', '').strip())
            else:
                # 只有一个参数
                fields.add(fields_str.replace('const', '').strip())
        self.fields = fields

    def gen_signature(self, pkg_name: str = None):
        if self.rtype != '':
            print(f'{self.rtype} {self.name} {self.fields}')
            signature = '('
            for p in self.fields:
                signature += types_map[p.replace('const', '').strip().split()[0].replace('*', '')].split()[1]
            signature += ')' + types_map[self.rtype].split()[1]
            print(f'sig: {signature}')
            return signature
        # 结构体签名，转成 java
        return f'jobject Lcom/quvideo/mobile/component/{pkg_name}/{self.name};'


class AlgoStructs:
    def __init__(self):
        self.__structs = set[Struct]()
        self.raw_data = set[str]()

    def __iter__(self):
        return iter(self.__structs)

    def size(self) -> int:
        return len(self.__structs)

    def add(self, raw: str) -> Struct:
        self.raw_data.add(raw)
        s = Struct(raw)
        self.__structs.add(s)
        return s


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


def read_lines(path: str) -> list[str]:
    with open(path, mode='r') as f:
        return f.readlines()


def read_content(path: str, flatmap: bool = False) -> str:
    with open(path, mode='r') as f:
        if flatmap:
            __all = ''
            for line in f.readlines():
                __all = __all + line.strip().replace('\n', '')
            return __all
        return f.read()


def parse_structs(lines: list[str]) -> set[str]:
    ss: set[str] = set()
    s = ''
    started = False
    for line in lines:
        # struct 开始
        if line.find('typedef struct') >= 0:
            started = True
        if started:
            s += line.replace('\n', '').strip()
        # struct 结束
        if started and line.find('}') >= 0 and line.find(';') >= 0:
            started = False
            ss.add(s)
            s = ''
    return ss


def parse_enums(lines: list[str]) -> set[str]:
    ss: set[str] = set()
    s = ''
    started = False
    for line in lines:
        # enum 开始
        if line.find('enum ') >= 0:
            started = True
        if started:
            s += line.replace('\n', '').strip()
        # enum 结束
        if started and line.find('}') >= 0 and line.find(';') >= 0:
            started = False
            ss.add(s)
            s = ''
    return ss


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


class ModuleGenerator:
    def __init__(self, root: str, name: str, name_zh: str):
        self.prj_root = root
        # 驼峰
        self.module_name = name.title().replace(' ', '')
        # module path
        self.path = f"{root}/Component-AI/{self.module_name}AI"
        # 模板路径，在工程目录下
        self.tmplt = root + '/templates'
        # jni 相关
        self.jni_dir = f"{self.path}/src/main/jni"

        # java package name 规则
        self.pkg_name = name.lower().replace(' ', '')
        # 全_小写
        self.lower_name = name.lower().replace(' ', '_')
        # 全_大写
        self.upper_name = name.upper().replace(' ', '_')
        # 中文名
        self.name_zh = name_zh
        # 代码生成所需要的值
        self.itf_impl_name = ''
        # 算法库名
        self.libraries = list[str]()
        # 算法库头文件
        self.headers = set[str]()
        # 命名空间
        self.using_nss = set[str]()
        # 算法中定义的普通接口方法
        self.methods = AlgoStructs()
        # 结构体
        self.structs = AlgoStructs()
        # 枚举
        self.enums = AlgoStructs()

    def __str__(self):
        return f"path: {self.path}\nname: {self.module_name}, pkg: {self.pkg_name}, lower: {self.lower_name}"

    def make(self, algo_spec: AlgoSpec):
        # 测试代码
        if os.path.exists(self.path):
            shutil.rmtree(self.path)

        # 拷贝模型文件
        assets_dir = f"{self.path}/src/main/assets/engine/ai/{self.lower_name}"
        os.makedirs(assets_dir)
        for item in algo_spec.model_files:
            shutil.copyfile(item, assets_dir + '/' + os.path.basename(item), follow_symlinks=False)

        # 生成 .gitignore 文件
        with open(f"{self.path}/.gitignore", mode='w') as f:
            f.writelines('build\n.cxx\nlibs/arm*\n')

        # 生成清单文件
        with open(f"{self.path}/src/main/AndroidManifest.xml", mode='w') as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(f'  <manifest package="com.quvideo.mobile.component.ai.{self.pkg_name}" />')

        # native 相关
        self.__generate_native__(algo_spec)

        # java 文件
        self.__generate_java__(algo_spec)

        # 以上都没有出错，修改 settings.gradle、config.gradle、upload-to-maven.sh
        # self.__modify_compile_scripts__()

    def __generate_java__(self, algo_spec: AlgoSpec):
        # AIConstants.java 中定义了算法模块常量，这里更新一下算法类型
        ai_constants = f'{os.path.dirname(self.path)}/AlgoBase/commonAI/src/main/java/' \
                       f'com/quvideo/mobile/component/common/AIConstants.java'
        if not os.path.exists(ai_constants):
            print(f"'{ai_constants}' 不存在！", file=sys.stderr)
            exit(1)
        raw_lines = read_lines(ai_constants)
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
            for line in read_lines(self.tmplt + '/QModuleName.java.tmplt'):
                # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                if line.find('{pkg-name}') >= 0:
                    line = line.replace('{pkg-name}', self.pkg_name)
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                f.write(line)
        with open(java_dir + f'/QE{self.module_name}Client.java', mode='w') as f:
            for line in read_lines(self.tmplt + '/QEModuleNameClient.java.tmplt'):
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
            for line in read_lines(self.tmplt + '/AIModuleName.java.tmplt'):
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
        with open(f"{self.path}/build.gradle", mode='w') as f:
            # 通过模板读取
            for line in read_lines(self.tmplt + '/build.gradle.tmplt'):
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                if line.find('{upper-name}') >= 0:
                    line = line.replace('{upper-name}', self.upper_name)
                if line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', algo_spec.ai_type)
                f.write(line)

    def __generate_native__(self, algo_spec: AlgoSpec):
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

        os.makedirs(self.jni_dir)

        # 拷贝升级日志
        if len(algo_spec.release_notes) > 0:
            dst = self.jni_dir + '/' + os.path.basename(algo_spec.release_notes[0])
            shutil.copyfile(algo_spec.release_notes[0], dst, follow_symlinks=False)

        # 拷贝库文件 ['arm64-v8a', 'arm32-v8a', 'armeabi-v7a']
        v8a_dir = f"{self.jni_dir}/arm64-v8a"
        os.mkdir(v8a_dir)
        v7a_dir = f"{self.jni_dir}/armeabi-v7a"
        os.mkdir(v7a_dir)
        for item in algo_spec.library_files:
            lib_name: str = os.path.basename(item)
            if lib_name not in self.libraries:
                self.libraries.append(lib_name.replace('lib', '').replace('.a', ''))
            shutil.copyfile(src=item,
                            dst=(v8a_dir if item.find('arm64') >= 0 else v7a_dir) + '/' + lib_name,
                            follow_symlinks=False)

        # 拷贝头文件时顺便分析文件内容: 文件名、命名空间、是否是统一接口、算法提供的 API、是否有定义结构体
        # 算法使用到的命名空间
        ns_pattern = re.compile(r'namespace (.+?) \{')
        # 有种特殊情况，算法侧提供了两种类型的接口，那我们优先选择使用统一接口的方式
        # 统一接口类型算法，需提取出实现类名
        itf_pattern = re.compile(r'class XYAI_PUBLIC (.+?) : public XYAISDK::AlgBase \{')
        # 普通算法只有算法接口，没有实现类
        method_pattern = re.compile(r'XYAI_PUBLIC (.+?)\);')
        for item in algo_spec.header_files:
            # 记录需要导入的头文件名
            self.headers.add(f'#include "{os.path.basename(item)}"')
            with open(self.jni_dir + '/' + os.path.basename(item), 'w') as f:
                for line in read_lines(item):
                    # 修改 include 路径
                    if line.find('.h') >= 0:
                        for k, v in jni_header_map.items():
                            if line.find(k) >= 0:
                                line = line.replace(k, v)
                                break
                    f.write(line)
            # 分析文件内容
            # 使用到的命名空间
            __text: str = read_content(path=item, flatmap=True)
            nss: list[str] = ns_pattern.findall(__text)
            if nss and len(nss) > 0:
                for ns in nss:
                    self.using_nss.add(f"using namespace {ns};")
            # 每个头文件只可能是一中类型的，要么是普通接口要么是统一接口
            if self.itf_impl_name == '':
                # 统一接口
                itf_names: list[str] = itf_pattern.findall(__text)
                if itf_names and len(itf_names) > 0:
                    self.itf_impl_name = itf_names[0]
            if self.itf_impl_name == '' and self.methods.size() == 0:
                # 普通接口
                method_signatures = method_pattern.findall(__text)
                if method_signatures and len(method_signatures) > 0:
                    for ms in method_signatures:
                        self.methods.add('XYAI_PUBLIC ' + ms + ');')
            raw_lines = read_lines(item)
            # 结构体
            for s in parse_structs(raw_lines):
                strukt = self.structs.add(s)
                print(f'find struct: {s} -> {strukt.name}')
                types_map[strukt.name] = strukt.gen_signature(self.pkg_name)
            # 枚举
            for e in parse_enums(raw_lines):
                enm = self.enums.add(e)
                print(f'find enum: {e} -> {enm.name}')
                types_map[enm.name] = "jint I"

        # 生成算法组件 jni 实现，通过模板生成 cpp 文件
        self.itf_impl_name = ''
        if self.itf_impl_name and len(self.itf_impl_name) > 0:
            self.__generate_cpp_union__(algo_spec)
        else:
            self.__generate_cpp_common__(algo_spec)

        print(f"start generate '{self.path}/CMakeLists.txt'")
        with open(f"{self.path}/CMakeLists.txt", mode='w') as f:
            # 通过模板读取
            for line in read_lines(self.tmplt + '/CMakeLists.txt.tmplt'):
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                if line.find('{lower-name}') >= 0:
                    line = line.replace('{lower-name}', self.lower_name)
                if line.find('{algo-lib-name}') >= 0:
                    line = line.replace('{algo-lib-name}', self.libraries[0])
                f.write(line)

    def __generate_cpp_union__(self, spec: AlgoSpec):
        with open(f"{self.jni_dir}/{self.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_lines(self.tmplt + '/lower_name_jni.cpp.tmplt'):
                if line.find('{imported-headers}') >= 0:
                    line = line.replace('{imported-headers}', '\n'.join(self.headers))
                elif line.find('{algo-nss}') >= 0:
                    line = line.replace('{algo-nss}', '\n'.join(self.using_nss))
                elif line.find('{itf-name}') >= 0:
                    line = line.replace('{itf-name}', self.itf_impl_name)
                elif line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', spec.ai_type)
                else:
                    # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                    if line.find('{pkg-name}') >= 0:
                        line = line.replace('{pkg-name}', self.pkg_name)
                    if line.find('{module-name}') >= 0:
                        line = line.replace('{module-name}', self.module_name)
                f.write(line)

    def __generate_cpp_common__(self, spec: AlgoSpec):
        with open(f"{self.jni_dir}/{self.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_lines(self.tmplt + '/lower_name_jni_0.cpp.tmplt'):
                if line.find('{imported-headers}') >= 0:
                    line = line.replace('{imported-headers}', '\n'.join(self.headers))
                elif line.find('{algo-nss}') >= 0:
                    line = line.replace('{algo-nss}', '\n'.join(self.using_nss))
                elif line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', spec.ai_type)
                elif line.find('{jni-methods}') >= 0:
                    line = self.__generate_jni_methods__()
                elif line.find('{jni-register-methods}') >= 0:
                    line = self.__generate_register_array__()
                else:
                    # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                    if line.find('{pkg-name}') >= 0:
                        line = line.replace('{pkg-name}', self.pkg_name)
                    if line.find('{module-name}') >= 0:
                        line = line.replace('{module-name}', self.module_name)
                f.write(line)

    def __generate_jni_methods__(self) -> str:
        methods: list[str] = []
        for m in self.methods:
            method_body = types_map[m.rtype].split()[0] + f' Q{self.module_name}_{m.name}(JNIEnv *env, jclass clazz, '
            # 参数列表
            size = len(m.fields)
            input_args = ''
            statements = ''
            for i, f in enumerate(m.fields, 1):
                segments = f.replace('const', '').strip().split()
                method_body += types_map[segments[0].replace('*', '')].split()[0] + ' ' + segments[1] + ', '
                statements += f'  {segments[0]} _{segments[1]}_ = ({segments[0]}){segments[1]};\n'
                if segments[0].find('*') >= 0:
                    input_args += '&'
                input_args += f'_{segments[1]}_, '
                if i == size:
                    if input_args.endswith(', '):
                        input_args = input_args[:len(input_args) - 2]
                    if m.rtype != 'void':
                        statements += f'  return {m.name}({input_args});\n'
                    else:
                        statements += f'  {m.name}({input_args});\n'

            # 处理结尾字符串
            if method_body.endswith(', '):
                method_body = method_body[:len(method_body) - 2]
            method_body += ') {\n'
            method_body += '  // TODO 下面是默认实现\n'
            method_body += statements
            method_body += '}\n'
            print(f'method body =====>\n{method_body}')
            methods.append(method_body)
        return '\n'.join(methods)

    def __generate_register_array__(self) -> str:
        sig_format = '  {"{}", "{}", (void *)Q{}_{}},\n'
        line = ''
        for m in self.methods:
            line += '  {"' + m.name + '", "' + m.gen_signature() + f'", (void *) Q{self.module_name}_{m.name}' + '},\n'
            # line += sig_format.format(m.name, m.gen_signature(), self.module_name, m.name)
        return line

    def __modify_compile_scripts__(self):
        # 修改 settings.gradle、config.gradle、upload-to-maven.sh
        settings: str = self.prj_root + '/settings.gradle'
        if not os.path.exists(settings):
            print(f"'{settings}' 不存在！")
            exit(1)
        raw_lines = read_lines(settings)
        with open(settings, mode='w') as f:
            for line in raw_lines:
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name).replace('//', '')
                    line = line[:line.find(' /* ')]
                    f.write(line)
                    f.write("\n//include ':Component-AI:{module-name}AI' /* 这一行是模板占位符，不要删除！！*/\n")
                else:
                    f.write(line)
        # config.gradle upload-to-maven.sh
        config: str = self.prj_root + '/config.gradle'
        if not os.path.exists(config):
            print(f"'{config}' 不存在！")
            exit(1)
        raw_lines = read_lines(config)
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

        compile_script: str = self.prj_root + '/upload-to-maven.sh'
        if not os.path.exists(compile_script):
            print(f"'{compile_script}' 不存在！")
            exit(1)
        raw_lines = read_lines(compile_script)
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
                new_lines.append('    #:Component-AI:{module-name}AI:"$task" \\ /* 这一行是模板占位符，不要删除！！*/\n')
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

        self.__generator = ModuleGenerator(root, name, name_zh)

    def __str__(self):
        return self.__generator.__str__()

    def generate(self, algo_spec: AlgoSpec):
        self.__generator.make(algo_spec)


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

    # __prj_root = os.path.dirname(__file__)
    # if not os.path.exists(__prj_root + '/Component-AI'):
    #     # 脚本必须在算法工程根目录执行
    #     print(f"脚本必须在算法工程根目录执行！当前：'{__prj_root}'", file=sys.stderr, flush=True)
    #     exit(1)
    # print(f"project root: '{__prj_root}'")

    __prj_root = '/home/binlee/code/android/Dailearn/pytest/out'

    # task = GenerateTask(input("操作类型（a 新增算法组件，u 更新算法组件）："),
    #                     input("算法库目录（绝对路径）："),
    #                     input("算法类型（正整数）："),
    #                     input("算法组件名（多个单词时以空格分割）：").lower(),
    #                     input("算法组件名（中文）：").lower())
    task = GenerateTask(algo_dir='/home/binlee/code/quvideo/XYAlgLibs/AutoCrop-component/XYAutoCrop',
                        ai_type='24',
                        algo_root=__prj_root,
                        module_name='auto crop'.lower(),
                        module_name_zh='图片智能裁剪')
    task.create_module()

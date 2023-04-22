import enum
import getpass
import os
import re
import shutil
import sys
import time


class Types:
    def __init__(self):
        pass


# ai type -> jni type & sig
types_map = {
    'int': 'jint I',
    'XYAIImageFormat': 'jint I',
    'XYInt32': 'jint I',
    'XYInt32*': 'jintarray [I',
    'XYInt32**': 'jintarray [I',
    'float': 'jfloat F',
    'XYFloat': 'jfloat F',
    'XYFloat*': 'jfloatarray [F',
    'XYFloat**': 'jfloatarray [F',
    'XYDouble': 'jdouble D',
    'XYHandle': 'jlong J',
    'XYLong': 'jlong J',
    'XYBool': 'jboolean Z',
    'XYChar': 'jchar C',
    'XYShort': 'jshort S',
    'void': 'void V',
    'XYVoid': 'void V',
    'XYChar*': 'jstring Ljava/lang/String;',
    'XYString': 'jstring Ljava/lang/String;',
    'std::string': 'jstring Ljava/lang/String;',
    'std::vector<float>': 'jobject Ljava/util/ArrayList;',
    'XYAIFrameInfo': 'jobject Lcom/quvideo/mobile/component/common/AIFrameInfo;',
    'XYAIRect': 'jobject Lcom/quvideo/mobile/component/common/AIRect;',
    'XYAIRectf': 'jobject Lcom/quvideo/mobile/component/common/AIRectF;',
    'XYAIPoint': 'jobject Lcom/quvideo/mobile/component/common/AIPoint;',
    'XYAIPointf': 'jobject Lcom/quvideo/mobile/component/common/AIPointF;',
    'InitResult': 'jobject Lcom/quvideo/mobile/component/common/AIInitResult;',
    # c 中定义的枚举和结构体可以在这里根据代码动态添加
}


def _split_field(field: str) -> (str, str):
    """
    把字符串分割成键值对
    @param field:
    @return:
    """
    __ss = field.replace('const', '').strip().split()
    return __ss[0], __ss[1]


def _typeof_java(pt: str) -> str:
    """
    获取j ava 数据类型
    @param pt:
    @return:
    """
    pieces = types_map[pt if pt in types_map else pt.replace('*', '')].split()
    if pieces[0] == 'void':
        return 'void'
    if pieces[0] == 'jobject' or pieces[0] == 'jstring':
        return pieces[1][1:len(pieces[1]) - 1].replace('/', '.')
    if pieces[0].find('array') >= 0:
        return pieces[0][1:].replace('array', '[]')
    return pieces[0][1:]


def _typeof_cpp(pt: str) -> str:
    """
    获取 cpp 数据类型
    @param pt:
    @return:
    """
    return types_map[pt if pt in types_map else pt.replace('*', '')].split()[0]


class StructType(enum.Enum):
    METHOD = 1
    STRUCT = 2
    ENUM = 3


class Struct:
    # 方法、结构体、枚举
    def __init__(self, raw: str, stype: StructType):
        self.stype = stype
        self.name = ''
        self.rtype = ''
        self.fields = list[str]()
        if stype == StructType.METHOD:
            self.__parse_method__(raw)
        else:
            self.__parse_struct_or_enum__(raw)

    def __str__(self):
        return f"{self.rtype} {self.name}({', '.join(self.fields)})"

    def __parse_method__(self, raw: str):
        # 第一个左括号
        first_index = raw.find('(')
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
        # 参数列表键值对
        for kv in fields_str.split(','):
            if kv:
                self.fields.append(kv.replace('const', '').strip())

    def __parse_struct_or_enum__(self, raw: str):
        # typedef struct MultiTaskDetectionOutput {XYInt32 object_num;XYFloat pet_score;...} MTDetBbox;
        # enum PadType{BLACK = 0,WHITE = 1,BORDER_MEAN = 2,BORDER_BLUR = 3,BORDER_MIRROR = 4,NONE = 10};
        __old = 'enum' if self.stype == StructType.ENUM else 'typedef struct'
        first_index = raw.find('{')
        # 结构体或枚举名字
        segments = raw.replace(__old, '').strip().split()
        length = len(segments)
        if segments[length - 1].find('}') >= 0:
            index = segments[0].find('{')
            self.name = segments[0] if index < 0 else segments[0][:index]
        else:
            self.name = segments[length - 1].replace(';', '')
        # 结构体以 ';' 分割，分割后得到的结构和方法类似
        # 枚举以 ',' 分割，分割后得到 k = 0 结构
        __sep = ',' if self.stype == StructType.ENUM else ';'
        for kv in raw[first_index + 1: raw.rfind('}')].split(__sep):
            if kv:
                self.fields.append(kv)

    def is_init_method(self) -> bool:
        ms = self.name.lower()
        return ms.find('init') >= 0 or ms.find('create') >= 0

    def gen_signature(self, pkg_name: str = None):
        def _sigof(pt: str) -> str:
            return types_map[pt if pt in types_map else pt.replace('*', '')].split()[1]

        if self.stype == StructType.METHOD:
            print(f'{self.rtype} {self.name} {self.fields}')
            signature = '('
            for f in (self.fields[1:] if self.is_init_method() else self.fields):
                signature += _sigof(f.replace('const', '').strip().split()[0])
            signature += ')' + types_map['InitResult' if self.is_init_method() else self.rtype].split()[1]
            print(f'signature: {signature}\n')
            return signature
        # 结构体签名，转成 java
        return f'jobject Lcom/quvideo/mobile/component/{pkg_name}/{self.name};'


class AlgoStructs:
    def __init__(self, stype: StructType):
        self.__stype = stype
        self.__structs = list[Struct]()
        self.__init_method = ''

    def __iter__(self):
        return iter(self.__structs)

    def size(self) -> int:
        return len(self.__structs)

    def add(self, raw: str) -> Struct:
        s = Struct(raw, self.__stype)
        self.__structs.append(s)
        if s.is_init_method():
            self.__init_method = s.name
        return s

    def get_init_method(self) -> str:
        return self.__init_method


def get_file_ext(path: str) -> str:
    index = path.rfind('.')
    if index >= 0:
        return path[index:]
    return ''


def __find_files__(path: str, output: list[str], suffixes: list[str]):
    if os.path.isfile(path):
        if get_file_ext(path) in suffixes:
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
        # 更新日志: [README.md, releasenote.txt]
        self.release_notes = find_file(root, suffixes=['.md', '.txt'])
        # print(f"release notes: {self.release_notes}")

        # 库文件: lib/[android, Android]/[arm64-v8a, arm32-v8a, armeabi-v7a]/*.a
        self.library_files = find_file(root + "/lib", ['android', 'Android'], suffixes=['.a'])
        # print(f"libraries: {self.library_files}")

        # 头文件：include/*.h
        self.header_files = find_file(root + '/include', suffixes=['.h'])
        # print(f"headers: {self.header_files}")

        # 模型文件：model/**/[*.json, *.xymodel]
        self.model_files = find_file(root + '/model', suffixes=['.json', '.xymodel'])
        # print(f"models: {self.model_files}")

        # ###### 代码生成所需要的变量 ######
        # 统一接口名
        self.itf_impl_name = ''
        # 算法库名
        self.libraries = list[str]()
        # 算法库头文件
        self.headers = set[str]()
        # 命名空间
        self.using_nss = set[str]()
        # 算法中定义的普通接口方法
        self.methods = AlgoStructs(StructType.METHOD)
        # 结构体
        self.structs = AlgoStructs(StructType.STRUCT)
        # 枚举
        self.enums = AlgoStructs(StructType.ENUM)
        # 常量
        self.consts = set[str]()

    def __str__(self):
        formatter = "release notes:{}\nlibraries:{}接口\nheaders:{}\nmodels:{}"
        return formatter.format(self.release_notes, self.library_files, self.header_files, self.model_files)

    def analyse(self, pkg_name: str):
        # 分析头文件内容: 文件名、命名空间、是否是统一接口、算法提供的 API、是否有定义结构体
        # 算法使用到的命名空间
        ns_pattern = re.compile(r'namespace (.+?) \{')
        # 有种特殊情况，算法侧提供了两种类型的接口，那我们优先选择使用统一接口的方式
        # 统一接口类型算法，需提取出实现类名
        itf_pattern = re.compile(r'class XYAI_PUBLIC (.+?) : public XYAISDK::AlgBase \{')
        # 普通算法只有算法接口，没有实现类
        method_pattern = re.compile(r'XYAI_PUBLIC (.+?)\);')
        for item in self.header_files:
            header_name = os.path.basename(item)
            # 记录需要导入的头文件名
            self.headers.add(f'#include "{header_name}"')
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
            # 常量
            raw_lines = read_lines(item)
            for line in raw_lines:
                line = line.replace('\n', '')
                if line.startswith('#define XYAI_') and line.find('(') < 0:
                    line = line.replace('#define', '').strip()
                    print(f"find constant: {line.split()} in '{header_name}'")
                    self.consts.add(line)
            # 结构体
            for s in parse_structs(raw_lines):
                strukt = self.structs.add(s)
                print(f"find struct: {strukt.name} in '{header_name}'")
                types_map[strukt.name] = strukt.gen_signature(pkg_name)
            # 枚举
            for e in parse_enums(raw_lines):
                enm = self.enums.add(e)
                print(f"find enum: {enm.name} in '{header_name}'")
                types_map[enm.name] = "jint I"


class ModuleGenerator:
    def __init__(self, root: str, name: str, name_zh: str, spec: AlgoSpec):
        """
        @param root: 算法组件工程根目录
        @param name: 算法模块名
        @param name_zh: 算法模块中文名
        @param spec: 算法库
        """
        parent = os.path.dirname(root)
        if not os.path.exists(parent):
            print(f"'{parent}' 不存在！")
            exit(1)
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

        # 算法库相关
        self.spec = spec

    def __str__(self):
        return f"path: {self.path}\nname: {self.module_name}, pkg: {self.pkg_name}, lower: {self.lower_name}"

    def generate(self):
        # 分析算法库提供的头文件
        self.spec.analyse(self.pkg_name)

        # 测试代码
        if os.path.exists(self.path):
            shutil.rmtree(self.path)

        # 拷贝模型文件
        assets_dir = f"{self.path}/src/main/assets/engine/ai/{self.lower_name}"
        os.makedirs(assets_dir)
        for item in self.spec.model_files:
            shutil.copyfile(item, assets_dir + '/' + os.path.basename(item), follow_symlinks=False)

        # 生成 .gitignore 文件
        with open(f"{self.path}/.gitignore", mode='w') as f:
            f.writelines('build\n.cxx\nlibs/arm*\n')

        # 生成清单文件
        with open(f"{self.path}/src/main/AndroidManifest.xml", mode='w') as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(f'  <manifest package="com.quvideo.mobile.component.ai.{self.pkg_name}" />')

        # native 相关
        self.__generate_native__()

        # java 文件
        self.__generate_java__()

        # 以上都没有出错，修改 settings.gradle、config.gradle、upload-to-maven.sh
        self.__modify_compile_scripts__()

    def __generate_java__(self):
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
                        line = line.replace('{ai-type}', self.spec.ai_type).replace('//', '')
                        write_placeholder = True
                    f.write(line)
                    if write_placeholder:
                        f.write('  ///** 算法类型-{module-name-zh} */\n')
                        f.write('  //public static final int AI_TYPE_{upper-name} = {ai-type};\n')

        # java 文件
        java_dir = f"{self.path}/src/main/java/com/quvideo/mobile/component/{self.pkg_name}"
        os.makedirs(java_dir)
        # 生成枚举和结构体
        self.__gen_java_enums__(java_dir)
        self.__gen_java_beans__(java_dir)
        # 根据模板生成对应的 java 文件，一般来讲有三个文件
        # Q{self.name}.java 提供给引擎使用
        with open(java_dir + f'/Q{self.module_name}.java', mode='w') as f:
            # 判断使用统一接口还是普通接口模板
            if self.spec.itf_impl_name != '':
                self.__generate_java_union__(f)
            else:
                self.__generate_java_common__(f)
        # QE{self.name}Client.java 提供给业务使用（组件内部也会使用）
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
                    line = line.replace('{ai-type}', self.spec.ai_type)
                f.write(line)
        # AI{self.name}.java 提供给业务使用
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
                    line = line.replace('{ai-type}', self.spec.ai_type)
                f.write(line)

    def __gen_java_enums__(self, java_dir: str):
        if self.spec.enums.size() > 0:
            package = java_dir[java_dir.find('java/') + 5:].replace('/', '.')
            today = time.strftime("%Y-%m-%d", time.localtime())
            for e in self.spec.enums:
                with open(java_dir + f'/{e.name}.java', mode='w') as f:
                    f.write(f'package {package};\n\n')
                    f.write(f'/**\n * @author {getpass.getuser()}\n * @date {today}\n */\n')
                    f.write(f'public final class {e.name}' + ' {\n')
                    for item in e.fields:
                        if item:
                            f.write(f'  public static final int {item};\n')
                    f.write('}')

    def __gen_java_beans__(self, java_dir: str):
        if self.spec.structs.size() > 0:
            package = java_dir[java_dir.find('java/') + 5:].replace('/', '.')
            today = time.strftime("%Y-%m-%d", time.localtime())
            for s in self.spec.structs:
                with open(java_dir + f'/{s.name}.java', mode='w') as bean:
                    bean.write(f'package {package};\n\n')
                    bean.write(f'/**\n * @author {getpass.getuser()}\n * @date {today}\n */\n')
                    bean.write(f'public final class {s.name}' + ' {\n')
                    if len(self.spec.consts) > 0:
                        bean.write('\n')
                        for c in self.spec.consts:
                            pieces = c.split()
                            bean.write(f'  public static final int {pieces[0]} = {pieces[1]};\n')
                    bean.write(f'\n  public {s.name}() ' + '{\n  }\n\n')
                    bean.write(f'  // generated from struct {s}\n')
                    for f in s.fields:
                        ptype, pname = _split_field(f)
                        if ptype and pname:
                            bean.write(f'  public {_typeof_java(ptype)} {pname};\n')
                    bean.write('}')

    def __generate_java_union__(self, f):
        for line in read_lines(self.tmplt + '/QModuleName.java.tmplt'):
            if line.find('{pkg-name}') >= 0:
                line = line.replace('{pkg-name}', self.pkg_name)
            elif line.find('{module-name}') >= 0:
                line = line.replace('{module-name}', self.module_name)
            f.write(line)

    def __generate_java_common__(self, f):
        for line in read_lines(self.tmplt + '/QModuleName0.java.tmplt'):
            if line.find('{pkg-name}') >= 0:
                line = line.replace('{pkg-name}', self.pkg_name)
            elif line.find('{module-name}') >= 0:
                line = line.replace('{module-name}', self.module_name)
            elif line.find('{init-method}') >= 0:
                line = line.replace('{init-method}', self.spec.methods.get_init_method())
            elif line.find('{native-methods}') >= 0:
                line = self.__gen_java_native_methods__()
            f.write(line)

    def __gen_java_native_methods__(self) -> str:
        methods: list[str] = []
        for m in self.spec.methods:
            # 构建 jni 方法，没有方法体
            method_body = '  private static native '
            # 方法返回值，算法初始化时的返回值要特殊处理
            method_body += _typeof_java('InitResult' if m.is_init_method() else m.rtype)
            # 方法参数列表，算法初始化时跳过第一个参数
            args_str = ''
            for i, f in enumerate(m.fields[1:] if m.is_init_method() else m.fields, 1):
                if i > 2 and i & i % 2 == 1:
                    args_str += '\n' + ' ' * 8
                segments = f.replace('const', '').strip().split()
                args_str += _typeof_java(segments[0]) + ' ' + segments[1] + ', '
            args_str = args_str[:len(args_str) - 2]
            method_body += f' {m.name}({args_str});\n'
            methods.append(method_body)
            print(f'java native method =======>\n{method_body}')
        return '\n'.join(methods)

    def __generate_native__(self):
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
        if len(self.spec.release_notes) > 0:
            dst = self.jni_dir + '/' + os.path.basename(self.spec.release_notes[0])
            shutil.copyfile(self.spec.release_notes[0], dst, follow_symlinks=False)

        # 拷贝库文件 ['arm64-v8a', 'arm32-v8a', 'armeabi-v7a']
        v8a_dir = f"{self.jni_dir}/arm64-v8a"
        os.mkdir(v8a_dir)
        v7a_dir = f"{self.jni_dir}/armeabi-v7a"
        os.mkdir(v7a_dir)
        for item in self.spec.library_files:
            lib_name: str = os.path.basename(item)
            if lib_name not in self.spec.libraries:
                self.spec.libraries.append(lib_name.replace('lib', '').replace('.a', ''))
            shutil.copyfile(src=item,
                            dst=(v8a_dir if item.find('arm64') >= 0 else v7a_dir) + '/' + lib_name,
                            follow_symlinks=False)

        for item in self.spec.header_files:
            with open(self.jni_dir + '/' + os.path.basename(item), 'w') as f:
                for line in read_lines(item):
                    # 修改 include 路径
                    if line.find('.h') >= 0:
                        for k, v in jni_header_map.items():
                            if line.find(k) >= 0:
                                line = line.replace(k, v)
                                break
                    f.write(line)

        # 生成算法组件 jni 实现，通过模板生成 cpp 文件
        print(f"start generate '{self.jni_dir}/{self.lower_name}_jni.cpp'")
        if self.spec.itf_impl_name != '':
            self.__generate_cpp_union__()
        else:
            self.__generate_cpp_common__()

        print(f"start generate '{self.path}/CMakeLists.txt'")
        with open(f"{self.path}/CMakeLists.txt", mode='w') as f:
            # 通过模板读取
            for line in read_lines(self.tmplt + '/CMakeLists.txt.tmplt'):
                if line.find('{module-name}') >= 0:
                    line = line.replace('{module-name}', self.module_name)
                if line.find('{lower-name}') >= 0:
                    line = line.replace('{lower-name}', self.lower_name)
                if line.find('{algo-lib-name}') >= 0:
                    line = line.replace('{algo-lib-name}', self.spec.libraries[0])
                f.write(line)

    def __generate_cpp_union__(self):
        with open(f"{self.jni_dir}/{self.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_lines(self.tmplt + '/lower_name_jni.cpp.tmplt'):
                if line.find('{imported-headers}') >= 0:
                    line = line.replace('{imported-headers}', '\n'.join(self.spec.headers))
                elif line.find('{algo-nss}') >= 0:
                    line = line.replace('{algo-nss}', '\n'.join(self.spec.using_nss))
                elif line.find('{structs-beans}') >= 0:
                    line = self.__gen_convert_methods__()
                elif line.find('{itf-name}') >= 0:
                    line = line.replace('{itf-name}', self.spec.itf_impl_name)
                elif line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', self.spec.ai_type)
                else:
                    # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                    if line.find('{pkg-name}') >= 0:
                        line = line.replace('{pkg-name}', self.pkg_name)
                    if line.find('{module-name}') >= 0:
                        line = line.replace('{module-name}', self.module_name)
                f.write(line)

    def __generate_cpp_common__(self):
        with open(f"{self.jni_dir}/{self.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_lines(self.tmplt + '/lower_name_jni_0.cpp.tmplt'):
                if line.find('{imported-headers}') >= 0:
                    line = line.replace('{imported-headers}', '\n'.join(self.spec.headers))
                elif line.find('{algo-nss}') >= 0:
                    line = line.replace('{algo-nss}', '\n'.join(self.spec.using_nss))
                elif line.find('{structs-beans}') >= 0:
                    line = self.__gen_convert_methods__()
                elif line.find('{ai-type}') >= 0:
                    line = line.replace('{ai-type}', self.spec.ai_type)
                elif line.find('{jni-methods}') >= 0:
                    line = self.__gen_native_jni_methods__()
                elif line.find('{jni-register-methods}') >= 0:
                    line = self.__gen_native_register_array__()
                else:
                    # '{pkg-name}' 和 '{module-name}' 可能出现在同一行
                    if line.find('{pkg-name}') >= 0:
                        line = line.replace('{pkg-name}', self.pkg_name)
                    if line.find('{module-name}') >= 0:
                        line = line.replace('{module-name}', self.module_name)
                f.write(line)

    def __gen_convert_methods__(self) -> str:
        return f'// {self.module_name} 组件没有需要转换的结构体\n'

    def __gen_native_jni_methods__(self) -> str:
        methods: list[str] = []
        for m in self.spec.methods:
            method_body = types_map[m.rtype].split()[0] + f' Q{self.module_name}_{m.name}'
            # jni 方法参数列表
            args_list = 'JNIEnv *env, jclass clazz, \n' + ' ' * 8
            # 方法体内的语句
            statements = ''
            # 算法接口调用时入参
            caller_args = ''
            # 那些结构体要转成 java bean
            beans: list[str] = []
            print(m)
            for i, f in enumerate(m.fields, 1):
                ptype: str
                pname: str
                ptype, pname = _split_field(f)
                # 处理方法定义参数列表
                args_list += _typeof_cpp(ptype) + ' ' + pname + ', '
                # 语句定义，java -> cpp 数据结构转换
                if i == 1 and m.is_init_method():
                    statements += f'  {ptype} _{pname}_ = nullptr;\n'
                else:
                    # 特殊处理的参数：XYChar*/std:string/XYAIRect*/XYAIFrameInfo*
                    if ptype == 'XYChar*':
                        # char* _model_path = (char *)ScopedString(env, modelPath).c_str();
                        statements += f'  {ptype} _{pname}_ = ({ptype})ScopedString(env, {pname}).c_str();\n'
                        caller_args += f'_{pname}_, '
                    elif ptype == 'std::string':
                        statements += f'  {ptype} _{pname}_ = {ptype}(ScopedString(env, {pname}).c_str());\n'
                        caller_args += f'_{pname}_, '
                    elif ptype == 'XYAIRect*':
                        statements += f'  {ptype[:len(ptype) - 1]} _{pname}_ = ' + '{0};\n'
                        caller_args += f'&_{pname}_'
                    elif ptype == 'XYAIFrameInfo*':
                        if pname.find('input') >= 0:
                            # 送给算法接口的参数
                            statements += f'  auto _{pname}_ = '
                            statements += f'std::unique_ptr<{ptype[:len(ptype) - 1]}>(AIFrameInfoJ2C(env, _{pname}_));\n'
                            caller_args += f'_{pname}_.get(), '
                        else:
                            # 需要算法填充的参数，这时返回值需要做转换
                            statements += f'  {ptype[:len(ptype) - 1]} _{pname}_ = ' + '{0};\n'
                            caller_args += f'&_{pname}_'
                            beans.append(f)
                    else:
                        statements += f'  {ptype} _{pname}_ = ({ptype}){pname};\n'
                        caller_args += f'_{pname}_, '
                        if ptype.endswith('**'):
                            beans.append(f)
                # 换行处理
                if i % 3 == 0 and i != len(m.fields):
                    args_list += '\n' + ' ' * 8
                    caller_args += '\n' + ' ' * 8
            # 方法返回
            if m.rtype.lower().find('void') >= 0:
                statements += f"  return {m.name}({caller_args.rstrip(', ')});\n"
            else:
                # 埋点代码：方法执行前
                if m.is_init_method():
                    statements += '\n  FUNC_ENTER(__func__, 0)\n'
                else:
                    statements += f'\n  FUNC_ENTER(__func__, _handle_)\n'
                statements += f"  {m.rtype} _res_ = {m.name}({caller_args.rstrip(', ')});\n"
                # 埋点代码：方法执行后
                statements += f'  FUNC_EXIT(env, __func__, _res_, {self.spec.ai_type}, AV)\n'
                if m.is_init_method():
                    statements += f'  return XIAIInitResultC2J(env, _res_, (long) _handle_));\n'
                else:
                    # 返回之前，还要把数据结构转成 java bean，需要知道是什么结构体才能就行转换
                    if len(beans) > 0:
                        statements += '  if (_res_ == XYAI_NO_ERROR) {\n'
                        statements += '    // 结构体转成 java bean\n'
                        for bean in beans:
                            ptype, pname = _split_field(bean)
                            if ptype == 'XYAIFrameInfo*':
                                # AIFrameInfoC2J(env, &frameOut, out);
                                statements += f'    AIFrameInfoC2J(env, &_{pname}_, {pname});\n'
                            # 我们目前只实现这一个，其他的用到了再处理
                            elif ptype.endswith('**'):
                                # 处理数组类型
                                statements += f"    // TODO 转换 '{ptype}' to '{pname}'\n"
                                pass
                        statements += '  }\n'
                    statements += '  return _res_;\n'
            method_body = method_body.rstrip(', ') + f"({args_list.rstrip(', ')})" + ' {\n'
            method_body += f'  // 方法原型:\n  // {m}\n'
            method_body += '  // TODO 下面是默认实现\n'
            method_body += statements
            method_body += '}\n'
            print(f'\ncpp jni method =====>\n{method_body}')
            methods.append(method_body)
        # 所有方法之间以换行符分割
        return '\n'.join(methods)

    def __gen_native_register_array__(self) -> str:
        line = ''
        for m in self.spec.methods:
            line += '  {' + f'"{m.name}", "{m.gen_signature()}", (void *) Q{self.module_name}_{m.name}' + '},\n'
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
                    f.write(line.replace('{module-name-zh}', self.name_zh))
                elif line.find('{module-name}') >= 0:
                    left = line.find('{module-name}')
                    right = line.rfind('{module-name}')

                    f.write(line.replace('{module-name}', self.module_name).replace('//', ''))

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
            f.writelines(new_lines)
            f.write('\n')


class GenerateTask:
    def __init__(self, algo_dir: str = '', ai_type: str = '', algo_root: str = '',
                 module_name: str = '', module_name_zh: str = ''):
        algo_spec = AlgoSpec(algo_dir, ai_type)
        self.__generator = ModuleGenerator(algo_root, module_name, module_name_zh, algo_spec)

    def create_module(self):
        self.__generator.generate()


def usage():
    placeholders = {
        '{imported-headers}': 'cpp 文件中需要导入的头文件',
        '{module-name}': '算法模块名',
        '{algo-nss}': '算法命名空间',
        '{itf-name}': '算法统一接口名字',
        '{pkg-name}': 'java 包名',
        '{ai-type}': '算法类型',
        '{lower-name}': '大写_模块名',
        '{upper-name}': '小写_模块名',
        '{algo-lib-name}': '算法静态库名，去掉前缀和后缀',
        '{native-methods}': 'java 层定义的 native 方法',
        '{jni-methods}': 'cpp 中要实现的在 java 层定义的 native 方法',
        '{jni-register-methods}': 'jni 静态注册方法映射数组',
        '{init-method}': '算法初始化方法，其返回值比较特殊 AIInitResult',
        '{structs-beans}': 'cpp 结构体和 java bean 相互转化方法',
    }
    align = len(max(placeholders.keys(), key=len)) + 1
    print("===============================代码模板占位符说明===============================")
    for k, v in placeholders.items():
        print(k + ' ' * (align - len(k)) + ': ' + v)
    print("===============================代码模板占位符说明===============================")
    print()

    menus: list[str] = [
        '操作类型（a 新增算法组件，u 更新算法组件）：a（这里是新增）',
        "算法库目录（绝对路径）：/home/binlee/code/XYAlgLibs/AutoCrop-component/ImageRestore（这里是画质修复算法库）",
        "！！！！！！ 如果是更新库，下面的操作都不需要了 ！！！！！！",
        "算法类型（正整数且不能与现有算法类型重复）：24（请与 iOS 协商好）",
        "算法组件名（多个单词时以空格分割）：image restore v2（程序内部会处理成首字母大写、剔除空格、驼峰命名等）",
        "算法组件名（中文）：画质修复 v2（用于生成代码注释）",
    ]
    print("================================菜单================================")
    print("操作示例：")
    for item in menus:
        print(item)
    print("================================菜单================================")


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
    task = GenerateTask(algo_dir='/home/binlee/code/quvideo/XYAlgLibs/AutoCrop-component/PetVideoClip',
                        ai_type='24',
                        algo_root=__prj_root,
                        module_name='pet video clip v2'.lower(),
                        module_name_zh='宠物一键成片 v2')
    task.create_module()

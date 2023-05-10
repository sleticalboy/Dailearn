import enum
import getpass
import json
import os
import re
import shutil
import sys
import time
import zipfile


def is_array(ct: str, name: str) -> bool:
    """
    我们根据参数名去猜测 '*' 到底表示什么：
     1、box，input、list、containers 等表示数组；
     2、其他表示非数组；
    :param ct: 参数的 cpp 类型
    :param name: 参数名
    :return:
    """
    if '**' in ct:
        return True
    if '*' in ct and name:
        if 'box' in name or 'list' in name:
            return True
    return False


class TypeMapper:
    # ai type -> jni type & sig
    # int* 是传递地址过去
    # int** 是传递数组地址过去
    __map = {
        'int': 'jint I',
        'XYAIImageFormat': 'jint I',
        'XYInt32': 'jint I int',
        'float': 'jfloat F',
        'XYFloat': 'jfloat F',
        'XYDouble': 'jdouble D',
        'XYHandle': 'jlong J',
        'XYLong': 'jlong J',
        'XYBool': 'jboolean Z',
        'char': 'jchar C',
        'XYChar': 'jchar C',
        'XYInt8': 'jchar C',
        'XYUInt8': 'jchar C',
        'XYShort': 'jshort S',
        'short': 'jshort S',
        'void': 'void V',
        'XYVoid': 'void V',
        # 这个比较特殊，就单独放这了
        'XYChar*': 'jstring Ljava/lang/String;',
        'XYString': 'jstring Ljava/lang/String;',
        'std::string': 'jstring Ljava/lang/String;',
        # 作为数组类型
        'std::vector': 'jobject [',
        'XYAIFrameInfo': 'jobject Lcom/quvideo/mobile/component/common/AIFrameInfo;',
        'XYAIUserInfo': 'jobject Lcom/quvideo/mobile/component/common/AIBaseConfig;',
        'XYAIRect': 'jobject Lcom/quvideo/mobile/component/common/AIRect;',
        'XYAIRectf': 'jobject Lcom/quvideo/mobile/component/common/AIRectF;',
        'XYAIPoint': 'jobject Lcom/quvideo/mobile/component/common/AIPoint;',
        'XYAIPointf': 'jobject Lcom/quvideo/mobile/component/common/AIPointF;',
        'InitResult': 'jobject Lcom/quvideo/mobile/component/common/AIInitResult;',
        # c 中定义的枚举和结构体在这里根据代码动态添加
    }

    def java_sig(self, pt: str, allow_missed: bool = False, name: str = None):
        if 'std::vector' in pt:
            raw_type = pt.replace('std::vector', '').replace('<', '').replace('>', '')
            sj = self.java_sig(raw_type, allow_missed, name)
            return '[' + sj if sj != '' else sj
        try:
            sig = self.__map[pt if pt in self.__map else pt.replace('*', '')].split()[1]
            return '[' + sig if is_array(pt, name) else sig
        except KeyError as e:
            if allow_missed and pt not in self.__map:
                return ''
            raise e

    def java(self, pt: str, allow_missed: bool = False, name: str = None) -> str:
        def __wrap(jtype: str) -> str:
            return jtype + '[]' if is_array(pt, name) else jtype

        if 'std::vector' in pt:
            # 原始类型
            raw_type = pt.replace('std::vector', '').replace('<', '').replace('>', '')
            tj = self.java(raw_type, allow_missed, name)
            return tj + '[]' if tj != '' else tj
        try:
            pieces = self.__map[pt if pt in self.__map else pt.replace('*', '')].split()
            if pieces[0] == 'void':
                return 'void'
            if pieces[0] == 'jobject' or pieces[0] == 'jstring':
                return __wrap(pieces[1][1:len(pieces[1]) - 1].replace('/', '.'))
            return __wrap(pieces[0][1:])
        except KeyError as e:
            if allow_missed and pt not in self.__map:
                return ''
            raise e

    def jni(self, pt: str, allow_missed: bool = False, name: str = None) -> str:
        if 'std::vector' in pt:
            left = pt.find('<')
            right = pt.find('>')
            raw_type = pt[left + 1: right]
            tj = self.jni(raw_type, allow_missed, name)
            return tj + 'Array' if tj != '' else tj
        try:
            ctype = self.__map[pt if pt in self.__map else pt.replace('*', '')].split()[0]
            return ctype + 'Array' if is_array(pt, name) else ctype
        except KeyError as e:
            if allow_missed and pt not in self.__map:
                return ''
            raise e

    def append(self, pt: str, val: str):
        self.__map[pt] = val


types_ = TypeMapper()


class StructType(enum.Enum):
    METHOD = 1
    STRUCT = 2
    ENUM = 3


class Field:
    def __init__(self, kv: str, owner: StructType = None):
        # 枚举：XYInt32 XXX = 3
        # 方法参数：XYInt32 xxx
        # 结构体字段：XYInt32 xxx[]
        self.__owner__ = owner
        self.__kv__ = kv.replace(',', '')
        self.arr_len = ''
        self.name = ''
        self.value = ''
        self.cpp_type = ''
        self.java_type = ''
        self.jni_type = ''
        self.jni_sig = ''
        self.__parse__(True)
        # print(self)

    def __parse__(self, allow_missed: bool):
        if self.java_type != '':
            return
        if self.__owner__ == StructType.ENUM:
            # 枚举类型固定为 int
            self.cpp_type = 'XYInt32'
            self.java_type = 'int'
            self.jni_type = 'jint'
            self.jni_sig = 'I'
            index = self.__kv__.find('=')
            # 枚举名字和值
            self.name = self.__kv__[:index].strip()
            self.value = self.__kv__[index + 1:].strip()
        elif self.__owner__ == StructType.STRUCT:
            # XYInt32 object_num
            # XYInt32 object_box[XYAI_VM_MAX_DET_NUM * 5]
            # XYInt32* cx
            # XYInt32 *frame_box -> XYInt32* frame_box
            # 枚举数组，其实就是 int 数组
            # VShotCropMode* shot_crop_mode
            index = self.__kv__.find(' ')
            self.cpp_type = self.__kv__[:index].strip()
            name_ = self.__kv__[index + 1:].strip()
            index = name_.find('[')
            is_arr = index > 0
            self.name = name_ if index < 0 else name_[:index]
            if '*' in self.name:
                is_arr |= True
                self.cpp_type += '*'
                self.name = self.name.replace('*', '')
            self.arr_len = '' if index < 0 else name_[index + 1:-1]
            self.java_type = types_.java(self.cpp_type, allow_missed, self.name)
            self.jni_type = types_.jni(self.cpp_type, allow_missed, self.name)
            self.jni_sig = types_.java_sig(self.cpp_type, allow_missed, self.name)
            if self.java_type != '' and is_arr and '[]' not in self.java_type:
                self.java_type += '[]'
                self.jni_type += 'Array'
                self.jni_sig = '[' + self.jni_sig
        else:
            index = self.__kv__.find(' ')
            # 数据类型和名字
            self.cpp_type = self.__kv__[:index]
            self.name = self.__kv__[index + 1:].lower()
            if '&' in self.name:
                self.name = self.name.replace('&', '')
            # java 中的数据类型，这里可能会报错，可以延后处理
            self.java_type = types_.java(self.cpp_type, allow_missed, self.name)
            # jni 中的数据类型
            self.jni_type = types_.jni(self.cpp_type, allow_missed, self.name)
            # jni 签名
            self.jni_sig = types_.java_sig(self.cpp_type, allow_missed, self.name)
            if self.java_type == '' and '&' in self.cpp_type:
                stripped_type = self.cpp_type.replace('&', '')
                self.java_type = types_.java(stripped_type, allow_missed, self.name)
                self.jni_type = types_.jni(stripped_type, allow_missed, self.name)
                self.jni_sig = types_.java_sig(stripped_type, allow_missed, self.name)

    def reparse(self):
        self.__parse__(False)

    def is_struct(self) -> bool:
        return 'jobject' == self.jni_type

    def is_array(self) -> bool:
        return 'Array' in self.jni_type

    def arr_component_type(self) -> str:
        return self.jni_type[1:-5].title()

    def __str__(self):
        return f'field: {self.__dict__}'


class Struct:
    # 可能是方法、结构体、枚举
    def __init__(self, raw: str, stype: StructType):
        self.__raw__ = raw
        self.stype = stype
        self.name = ''
        self.fields = list[Field]()
        if stype == StructType.METHOD:
            self.__parse_method__(raw)
        elif stype == StructType.ENUM:
            self.__parse_enum__(raw)
        else:
            # print(f'start parse struct: {raw}')
            self.__parse_struct__(raw)
            # print(f'finish parse struct -> {self}')

    def __str__(self):
        if self.stype == StructType.STRUCT:
            return 'struct {' + self.__raw__
        return self.__raw__

    def __parse_method__(self, raw: str):
        # 第一个左括号
        first_index = raw.find('(')
        # 左半部分以空格分割
        first_half = raw[:first_index].split()
        # 方法名
        self.name = first_half[-1]
        # 返回值
        if first_half[0].startswith('XYAI_'):
            self.rtype = Field(first_half[1] + " r___")
        else:
            self.rtype = Field(first_half[0] + " r___")
        fields_str = raw[first_index + 1: raw.rfind(')')]
        # 参数列表键值对
        for kv in fields_str.split(','):
            if kv == '':
                continue
            self.fields.append(Field(kv.replace('const', '').strip()))

    def __parse_enum__(self, raw: str):
        # enum PadType{BLACK = 0,WHITE = 1,BORDER_MEAN = 2,BORDER_BLUR = 3,BORDER_MIRROR = 4,NONE = 10};
        raw = raw.replace('enum ', '')
        left = raw.find('{')
        right = raw.rfind('}')
        # 枚举名字
        self.name = raw[:left].strip()
        if self.name == '':
            self.name = raw[right + 1:-1].strip()
        # 枚举以 ',' 分割，分割后得到 k = 0 结构
        for kv in raw[left + 1:right].strip().split(','):
            if '=' in kv:
                self.fields.append(Field(kv, self.stype))

    def __parse_struct__(self, raw: str):
        # typedef struct MultiTaskDetectionOutput {XYInt32 object_num;XYFloat pet_score;...} MTDetBbox;
        raw = raw.replace('typedef struct ', '')
        left = raw.find('{')
        right = raw.rfind('}')
        # 结构体名字
        if left > 0:
            self.name = raw[:left].strip()
        else:
            self.name = raw[right + 1:-1].strip()
        # 结构体以 ';' 分割，分割后得到的结构和方法类似
        for kv in raw[left + 1: right].strip().split(';'):
            if kv and '//' not in kv:
                self.fields.append(Field(kv, self.stype))

    def is_init_method(self) -> bool:
        """
        是否是初始化方法，算法库中的初始化方法返回值要转成 java 中的 AIInitResult 类
        """
        lmn = self.name.lower()
        return 'init' in lmn or 'create' in lmn

    def is_release_method(self):
        """
        是否是释放算法句柄方法
        """
        return 'release' in self.name.lower()

    def signature(self, pkg_name: str = None) -> str:
        if self.stype == StructType.METHOD:
            # print(f'{self}')
            signature = '('
            for f in (self.fields[1:] if self.is_init_method() else self.fields):
                signature += f.jni_sig
            signature += ')' + types_.java('InitResult' if self.is_init_method() else self.rtype.cpp_type)
            # print(f'signature: {signature}\n')
            return signature
        # 结构体签名，转成 java
        return f'jobject Lcom/quvideo/mobile/component/{pkg_name}/{self.name};'

    def gen_java_enum(self, package: str) -> str:
        """
        生成 java 枚举类
        :param package: java 包名
        """
        today = time.strftime("%Y-%m-%d", time.localtime())
        body = f'package {package};\n\n'
        body += f'/**\n * @author {getpass.getuser()}\n * @date {today}\n */\n'
        body += f'public final class {self.name}' + ' {\n'
        body += f"/* generated via {self}*/\n"
        for item in self.fields:
            body += f'  public static final {item.java_type} {item.name} = {item.value};\n'
        body += '}'
        return body

    def gen_javabean(self, package: str, consts: set[str]) -> str:
        """
        生成 java bean 类
        :param package: 包名
        :param consts: 从头文件中解析到的常量，有可能会用到
        """
        today = time.strftime("%Y-%m-%d", time.localtime())
        body = f'package {package};\n\n'
        body += f'/**\n * @author {getpass.getuser()}\n * @date {today}\n */\n'
        body += f'public final class {self.name}' + ' {\n'
        if len(consts) > 0:
            body += '\n'
            for c in consts:
                pieces = c.split()
                body += f'  public static final int {pieces[0]} = {pieces[1]};\n'
        body += f'\n  public {self.name}() ' + '{\n  }\n\n'
        body += f"/* generated via {self}*/\n"
        for f in self.fields:
            if f.java_type == '':
                f.reparse()
            if f.arr_len != '':
                body += f"  public {f.java_type} {f.name} = new {f.java_type.replace('[]', '')}[{f.arr_len}];\n"
            else:
                body += f'  public {f.java_type} {f.name};\n'
        body += '}'
        return body

    def gen_javabean_to_struct_method(self) -> str:
        """
        生成 jni 方法，把 java bean 转换成 cpp 的结构体
        """

        body = f'{self.name} *to_struct_{self.name}(JNIEnv *env, jobject obj) ' + '{\n'
        statements = f'  {self.name}* _out_ = new {self.name}();\n'
        statements += f'  jclass clazz = env->FindClass("{types_.java_sig(self.name)}");\n'
        statements += f'  jfieldId fid;\n'
        for f in self.fields:
            if f.java_type == '':
                f.reparse()
            if f.is_array():
                statements += f'  // 从 java 获取 {f.name} -> {f.java_type}\n'
                statements += f'  fid = env->GetFieldID(clazz, "{f.name}", "{f.jni_sig}");\n'
                statements += f'  auto/*{f.jni_type}*/ _{f.name}_ = ({f.jni_type}) env->GetObjectField(obj, fid);\n'
                statements += f'  _out_->{f.name} = '
                statements += f'env->Get{f.arr_component_type()}ArrayElements(_{f.name}_, JNI_FALSE);\n'
            elif 'std::string' in f.cpp_type:
                statements += f'  // 从 java 获取 {f.name} -> {f.java_type}\n'
                statements += f'  fid = env->GetFieldID(clazz, "{f.name}", "{f.jni_sig}");\n'
                statements += f'  auto/*{f.jni_type}*/ _{f.name}_ = /*({f.jni_type}) */env->GetObjectField(obj, fid);\n'
                statements += f'  _out_->{f.name} = ScopedString(env, _{f.name}_).c_str();\n'
            else:
                statements += f'  // 从 java 获取 {f.name} -> {f.java_type}\n'
                statements += f'  fid = env->GetFieldID(clazz, "{f.name}", "{f.jni_sig}");\n'
                statements += f'  _out_->{f.name} = env->Get{f.jni_type[1:].title()}Field(obj, fid);\n'
        body += statements
        body += '  return _out_;\n'
        body += '}\n'
        return body

    def gen_struct_to_javabean_method(self) -> str:
        """
        生成 jni 方法，把 cpp 结构体转换成 java bean
        """

        def _jni_mt(jni_type: str):
            return jni_type.replace('Array', '')[1:].title()

        body = f'jobject to_java_bean_{self.name}(JNIEnv *env, {self.name} *value, jobject obj) ' + '{\n'
        # print('generate -> ' + body)
        statements = f'  jfieldId fid;\n'
        statements += f'  jclass clazz = env->FindClass("{types_.java_sig(self.name)}");\n'
        statements += f'  // jobject obj = env->NewObject(clazz, env->GetMethodID(clazz, "<init>", "()V"));\n'
        for f in self.fields:
            if '[]' in f.java_type:
                statements += f'  // 从 cpp 获取 {f.name} -> {f.cpp_type}\n'
                statements += f'  fid = env->GetFieldID(clazz, "{f.name}", "{f.jni_sig}");\n'
                statements += f'  auto c_{f.name}_ = value->{f.name};\n'
                if f.arr_len == '':
                    statements += f'  int _{f.name}_len_ = value->{f.name}.size();\n'
                else:
                    statements += f'  int _{f.name}_len_ = {f.arr_len};\n'
                statements += f'  {f.jni_type} _ja_{f.name}_ = env->New{_jni_mt(f.jni_type)}Array(_{f.name}_len_);\n'
                statements += f'  env->Set{_jni_mt(f.jni_type)}ArrayRegion(_ja_{f.name}_, 0, _{f.name}_len_, c_{f.name}_);\n'
                statements += f'  env->SetObjectField(obj, fid, _ja_{f.name}_);\n'
                pass
            elif 'std::string' in f.cpp_type:
                statements += f'  // 从 cpp 获取 {f.name} -> {f.cpp_type}\n'
                statements += f'  fid = env->GetFieldID(clazz, "{f.name}", "{f.jni_sig}");\n'
                statements += f'  env->SetObjectField(obj, fid, env->NewStringUTF(value->{f.name}.c_str()));\n'
                pass
            else:
                statements += f'  // 从 cpp 获取 {f.name} -> {f.cpp_type}\n'
                statements += f'  fid = env->GetFieldID(clazz, "{f.name}", "{f.jni_sig}");\n'
                statements += f'  env->Set{_jni_mt(f.jni_type)}Field(obj, fid, value->{f.name});\n'
        body += statements
        body += '  return java_out;\n'
        body += '}\n'
        return body

    def gen_jni_method(self, module_name: str, ai_type: str) -> str:
        """
        根据头文件中定义的接口，生成对应的 jni 方法
        :param module_name: 算法模块名
        :param ai_type: 算法类型，用于 jni 层埋点统计
        """

        def handle_data_convert() -> str:
            if len(structs_to_convert) == 0:
                return ''
            converts = '  // cpp -> java 数据类型转换\n'
            converts += '  if (_res_ == XYAI_NO_ERROR) {\n'
            for sf in structs_to_convert:
                print(f"{sf.cpp_type}' 转换为 '{sf.java_type}' -> '{sf.name}'")
                converts += f"    // '{sf.cpp_type}' 转换为 '{sf.java_type}' -> '{sf.name}'\n"
                if sf.cpp_type == 'XYAIFrameInfo*':
                    converts += f'    AIFrameInfoC2J(env, &_{sf.name}_, {sf.name});\n'
                elif sf.is_array():
                    converts += (f"    env->Set{sf.arr_component_type()}ArrayRegion({sf.name}, 0, "
                                 f"env->GetArrayLength({sf.name}), _{sf.name}_);\n")
                elif sf.is_struct():
                    converts += f"    to_java_bean_{sf.cpp_type.replace('*', '')}(env, &_{sf.name}_, {sf.name});\n"
            return converts + '  }\n'

        body = f'// generated via {self}\n'
        body += ('jobject' if self.is_init_method() else self.rtype.cpp_type) + f' Q{module_name}_{self.name}'
        # jni 方法参数列表
        args_list = 'JNIEnv *env, jclass clazz'
        if len(self.fields) > 0:
            args_list += ', \n' + ' ' * 8
        # 方法体内的语句
        statements = ''
        # 算法接口调用时入参
        caller_args = ''
        # 那些结构体要转成 java bean
        structs_to_convert: list[Field] = []

        for i, f in enumerate(self.fields, 1):
            # 处理方法定义参数列表
            args_list += f.jni_type + ' ' + f.name + ', '
            # 语句定义，java -> cpp 数据结构转换
            # 特殊处理初始化和释放函数
            if self.is_init_method():
                statements += f'  {f.cpp_type} _{f.name}_ = nullptr;\n'
                caller_args += f'&_{f.name}_, '
            elif self.is_release_method():
                statements += f'  XYHandle _{f.name}_ = (XYHandle) {f.name};\n'
                caller_args += f'&_{f.name}_'
            # 特殊处理的参数：XYHandle*/XYChar*/std:string/XYAIRect*/XYAIFrameInfo*/数组
            elif 'XYHandle*' == f.cpp_type:
                statements += f'  {f.cpp_type} _{f.name}_ = ({f.cpp_type}) {f.name};\n'
                caller_args = f'_{f.name}_, '
            elif 'XYChar*' == f.cpp_type:
                # jstring -> XYChar*
                statements += f'  {f.cpp_type} _{f.name}_ = ({f.cpp_type})ScopedString(env, {f.name}).c_str();\n'
                caller_args += f'_{f.name}_, '
            elif 'std::string' == f.cpp_type:
                # jstring -> std::string
                statements += f'  {f.cpp_type} _{f.name}_ = {f.cpp_type}(ScopedString(env, {f.name}).c_str());\n'
                caller_args += f'_{f.name}_, '
            elif 'XYAIFrameInfo*' == f.cpp_type and 'input' in f.name:
                statements += f'  auto _{f.name}_ = std::unique_ptr<XYAIFrameInfo>(AIFrameInfoJ2C(env, {f.name}));\n'
                caller_args += f'_{f.name}_.get(), '
            elif f.is_array():
                left = f.cpp_type.find('*')
                right = f.cpp_type.rfind('*')
                cpp_type = f.cpp_type if left == right else f.cpp_type[:right]
                statements += f"  {cpp_type} _{f.name}_ = "
                statements += f"new {cpp_type.replace('*', '')}[env->GetArrayLength({f.name})];\n"
                caller_args += f'&_{f.name}_, '
                structs_to_convert.append(f)
            elif f.is_struct():
                statements += f"  {f.cpp_type.replace('*', '')} _{f.name}_;\n"
                caller_args += f'&_{f.name}_, '
                structs_to_convert.append(f)
            else:
                # print(f'handle primary type param: {f.cpp_type} {f.name}')
                statements += f'  {f.cpp_type} _{f.name}_ = ({f.cpp_type}){f.name};\n'
                caller_args += f'_{f.name}_, '
            # 换行处理
            if i % 3 == 0 and i != len(self.fields):
                args_list += '\n' + ' ' * 8
                caller_args += '\n' + ' ' * 8

        # 方法返回
        if 'void' in self.rtype.cpp_type.lower():
            statements += f"  {self.name}({caller_args.rstrip(', ')});\n"
        else:
            # 埋点代码：方法执行前
            statements += f"\n  FUNC_ENTER(__func__, {'0' if self.is_init_method() else '_handle_'})\n"
            # 调用算法接口处理
            statements += f"  {self.rtype.cpp_type} _res_ = {self.name}({caller_args.rstrip(', ')});\n"
            # 埋点代码：方法执行后
            statements += f'  FUNC_EXIT(env, __func__, _res_, {ai_type}, AV)\n'
            if self.is_init_method():
                statements += f'  return XIAIInitResultC2J(env, _res_, (long) _handle_));\n'
            else:
                # 方法返回前要把 cpp 数据转成 java 数据
                statements += handle_data_convert()
                # 方法返回值处理
                if self.rtype.java_type == 'java.lang.String':
                    statements += '  return env->NewStringUTF(_res_);\n'
                else:
                    statements += '  return _res_;\n'
        body = body.rstrip(', ') + f"({args_list.rstrip(', ')})" + ' {\n'
        body += '  // TODO 下面是默认实现，请根据实际需求进行修改！\n'
        body += statements
        body += '}\n'
        return body

    def gen_native_method(self) -> str:
        """
        根据头文件中定义的接口，生成 java 层的 native 方法定义
        """

        body = f'  // generated via {self}\n'
        # 构建 jni 方法，没有方法体
        body += '  private static native '
        # 方法返回值，算法初始化时的返回值要特殊处理
        body += types_.java('InitResult' if self.is_init_method() else self.rtype.cpp_type)
        # 方法参数列表，算法初始化时跳过第一个参数
        args_list = ''
        for i, f in enumerate(self.fields[1:] if self.is_init_method() else self.fields, 1):
            if i > 2 and i & i % 2 == 1:
                args_list += '\n' + ' ' * 8
            args_list += f.java_type + ' ' + f.name + ', '
        body += f" {self.name}({args_list.rstrip(', ')});\n"
        return body

    def gen_jni_method_signature(self, module_name) -> str:
        """
        生成 jni 方法签名
        :param module_name: 算法模块名
        """
        # "{java 方法名}", "{java 方法签名}", (void *) jni 方法实现
        # return fmt.format(self.name, self.signature(), module_name, self.name)
        return f'"{self.name}", "{self.signature()}", (void *) Q{module_name}_{self.name}'


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
        if self.__init_method == '' and s.is_init_method():
            self.__init_method = s.name
        return s

    def get_init_method(self) -> str:
        return self.__init_method


def ensure_path(*path: str):
    for p in path:
        if not os.path.exists(p):
            os.makedirs(p)


def get_file_ext(path: str) -> str:
    index = path.rfind('.')
    return '' if index < 0 else path[index:]


def find_files(root: str, suffixes: list[str] = None, tester=None) -> list[str]:
    output: list[str] = []

    def filter_file(path: str) -> bool:
        return tester is None or (tester and tester(path))

    def recursive_find(path: str):
        if os.path.isfile(path):
            if suffixes and len(suffixes) > 0:
                # tester 为空就不校验，test 不为空就校验
                if get_file_ext(path) in suffixes and filter_file(path):
                    output.append(path)
            else:
                if filter_file(path):
                    output.append(path)
        elif os.path.isdir(path):
            for child in os.listdir(path):
                recursive_find(path + '/' + child)

    recursive_find(root)
    return output


def extract_file(file: str) -> list[str]:
    with zipfile.ZipFile(file) as f:
        # 如果路径不存在，extractall() 方法内部会自行创建
        path = '/tmp/' + os.path.basename(file)
        f.extractall(path)
        return find_files(path)


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


def extract_structs(lines: list[str]) -> list[str]:
    ss: list[str] = []
    s = ''
    started = False
    # 结构体内部的方法，目前先不做处理
    nested_method_started = False
    for line in lines:
        if '#' in line or '//' in line:
            continue
        # struct 开始
        if 'struct' in line and '{' in line:
            started = True
            s += line.replace('\n', '').strip()
        # 跳过方法行
        if started and '(' in line and ')' in line and '{' in line:
            nested_method_started = True
            continue
        if nested_method_started:
            if '}' in line:
                nested_method_started = False
            continue
        if started and not nested_method_started and ';' in line and '(' not in line:
            s += line.replace('\n', '').strip()
        # struct 结束
        if started and '}' in line and ';' in line:
            started = False
            ss.append(s)
            s = ''
    return ss


def extract_enums(lines: list[str]) -> set[str]:
    es: set[str] = set()
    e = ''
    started = False
    for line in lines:
        if '#' in line or '//' in line:
            continue
        # enum 开始
        if 'enum' in line and '{' in line:
            started = True
        if started:
            e += line.replace('\n', '').strip()
        # enum 结束
        if started and '}' in line and ';' in line:
            started = False
            es.add(e)
            e = ''
    return es


def extract_methods(lines: list[str]) -> set[str]:
    # int XYAI_PUBLIC Chorus_init();
    # int XYAI_PUBLIC Chorus_compute(float* pWav, int length, std::vector<float>& Chorus, int &Chorus_size);
    # int XYAI_PUBLIC Chorus_release();
    ms: set[str] = set()
    m = ''
    started = False
    for line in lines:
        if '#' in line or '//' in line:
            continue
        # method 开始
        if 'XYAI_PUBLIC' in line and '(' in line:
            started = True
        if started:
            m += line.replace('\n', '').strip()
        # method 结束
        if started and ');' in line:
            started = False
            ms.add(m)
            m = ''
    return ms


class LineHandler:
    def __init__(self):
        self.func_map = dict[str, any]()

    def register(self, key: str, new: str):
        self.func_map[key] = lambda line, old: line.replace(old, new)

    def handle(self, line: str) -> str:
        for k, func in self.func_map.items():
            line = func(line, k) if k in line else line
        return line


class AlgoParser:
    """
    算法库目录，一般结构如下
    include：头文件
    model：模型文件
    releaseNote.txt：升级日志
    lib：库文件
    """

    def __init__(self, root: str, ai_type: str):
        self.ai_type = ai_type
        # 更新日志: [README.md, releasenote.txt]
        self.release_notes = find_files(root, ['.md', '.txt'])

        # 库文件: lib/[android, Android]/[arm64-v8a, arm32-v8a, armeabi-v7a]/*.a
        self.library_files = find_files(root, ['.a'])

        def header_tester(p: str) -> bool:
            p = p.lower()
            return 'xyaisdk.h' not in p and 'xyaicommon.h' not in p

        # 头文件：include/*.h
        self.header_files = find_files(root, ['.h'], tester=header_tester)

        # 模型文件：model/**/[*.json, *.xymodel]
        self.model_files = find_files(root, ['.json', '.xymodel'])
        # 模型有可能是 zip 文件，要先解压再处理
        if len(self.model_files) == 0:
            for item in find_files(root, ['.zip'], tester=lambda p: 'android' in p.lower()):
                self.model_files.extend(extract_file(item))

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
        formatter = "release notes:{}\nlibraries:{}\nheaders:{}\nmodels:{}"
        return formatter.format(self.release_notes, self.library_files, self.header_files, self.model_files)

    def parse(self, pkg_name: str):
        # 分析头文件内容: 文件名、命名空间、是否是统一接口、算法提供的 API、是否有定义结构体
        # 算法使用到的命名空间
        ns_pattern = re.compile(r'namespace (.+?) \{')
        # 有种特殊情况，算法侧提供了两种类型的接口，那我们优先选择使用统一接口的方式
        # 统一接口类型算法，需提取出实现类名
        itf_pattern = re.compile(r'class XYAI_PUBLIC (.+?) : public XYAISDK::AlgBase \{')
        # 普通算法只有算法接口，没有实现类
        for item in self.header_files:
            header_name = os.path.basename(item)
            # 记录需要导入的头文件名
            self.headers.add(f'#include "{header_name}"')
            # 常量
            raw_lines = read_lines(item)
            for line in raw_lines:
                line = line.replace('\n', '')
                if line.startswith('#define ') and '(' not in line and line.find(' ') != line.rfind(' '):
                    line = line.replace('#define', '').strip()
                    # print(f"find constant: {line.split()} in '{header_name}'")
                    self.consts.add(line)
            # 结构体
            for ss in extract_structs(raw_lines):
                print(f"find struct {ss} in '{header_name}'")
                s = self.structs.add(ss)
                types_.append(s.name, s.signature(pkg_name))
            # 枚举
            for es in extract_enums(raw_lines):
                # print(f"find {es} in '{header_name}'")
                e = self.enums.add(es)
                types_.append(e.name, "jint I")
            # 读取文件内容，去掉所有换行
            __text: str = read_content(path=item, flatmap=True)
            # 使用到的命名空间
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
                for define in extract_methods(raw_lines):
                    # print(f"find method '{define}' in '{header_name}'")
                    self.methods.add(define.replace('XYAI_PUBLIC', ''))
        # 算法库名
        for item in self.library_files:
            lib_name: str = os.path.basename(item)
            if lib_name not in self.libraries:
                self.libraries.append(lib_name.replace('lib', '').replace('.a', ''))


class PrjInfo:
    def __init__(self, root: str, name: str, name_zh: str):
        self.__raw_name__ = name
        # 算法组件工程根目录
        self.algo_root = root
        # 算法模块名，驼峰式
        self.module_name = name.title().replace(' ', '')
        # 算法模块中文名
        self.module_name_zh = name_zh
        # java package name 规则
        self.pkg_name = name.lower().replace(' ', '')
        # 全_小写
        self.lower_name = name.lower().replace(' ', '_')
        # 全_大写
        self.upper_name = name.upper().replace(' ', '_')

        # 模板路径，在工程目录下
        self.tmplt = self.algo_root + '/templates'
        # module path
        self.path = f"{root}/Component-AI/{self.module_name}AI"
        # 模型相关
        self.assets_dir = f"{self.path}/src/main/assets/engine/ai/{self.lower_name}"
        # jni 相关
        self.jni_dir = f"{self.path}/src/main/jni"
        # java 相关
        self.java_dir = f"{self.path}/src/main/java/com/quvideo/mobile/component/{self.pkg_name}"
        ensure_path(self.path, self.assets_dir, self.jni_dir, self.java_dir)

    def dict(self) -> dict[str, str]:
        return {
            'root': self.algo_root,
            'name': self.__raw_name__,
            'name_zh': self.module_name_zh,
        }


class CopyTask:
    def __init__(self, prj: PrjInfo, parser: AlgoParser):
        parent = os.path.dirname(prj.algo_root)
        if not os.path.exists(parent):
            print(f"'{parent}' 不存在！")
            exit(1)
        self.prj = prj
        # 算法库相关
        self.parser = parser

    def process(self):
        print('parse algo lib...')
        # 分析算法库提供的头文件
        self.parser.parse(self.prj.pkg_name)
        print(self.parser)
        # 拷贝模型文件
        print('copy model files...')
        for item in self.parser.model_files:
            shutil.copyfile(item, self.prj.assets_dir + '/' + os.path.basename(item), follow_symlinks=False)
        # 拷贝升级日志
        print('copy release note file...')
        if len(self.parser.release_notes) > 0:
            dst = self.prj.jni_dir + '/' + os.path.basename(self.parser.release_notes[0])
            shutil.copyfile(self.parser.release_notes[0], dst, follow_symlinks=False)
        # 拷贝库文件 ['arm64-v8a', 'arm32-v8a', 'armeabi-v7a']
        print('copy algo library files...')
        v7a_dir = f"{self.prj.jni_dir}/armeabi-v7a"
        v8a_dir = f"{self.prj.jni_dir}/arm64-v8a"
        ensure_path(v7a_dir, v8a_dir)
        if not os.path.exists(v8a_dir):
            os.mkdir(v8a_dir)
        if not os.path.exists(v7a_dir):
            os.mkdir(v7a_dir)
        for item in self.parser.library_files:
            shutil.copyfile(src=item,
                            dst=(v8a_dir if 'arm64' in item else v7a_dir) + '/' + os.path.basename(item),
                            follow_symlinks=False)
        # 拷贝头文件
        __jni_header_map = {
            '"XYAICommon.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAICommon.h"',
            '<XYAICommon.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAICommon.h"',
            '"XYAISDK.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAISDK.h"',
            '<XYAISDK.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAISDK.h"',
            '"XYAIStructExchange.h"': '"../../../../AlgoBase/commonAI/src/main/jni/XYAIStructExchange.h"',
            '<XYAIStructExchange.h>': '"../../../../AlgoBase/commonAI/src/main/jni/XYAIStructExchange.h"',
            '"method_tracer.h"': '"../../../../AlgoBase/commonAI/src/main/jni/method_tracer.h"',
            '<method_tracer.h>': '"../../../../AlgoBase/commonAI/src/main/jni/method_tracer.h"',
        }
        print('start copy header files...')
        for item in self.parser.header_files:
            with open(self.prj.jni_dir + '/' + os.path.basename(item), 'w') as f:
                for line in read_lines(item):
                    # 修改 include 路径
                    if '.h' in line:
                        for k, v in __jni_header_map.items():
                            if k in line:
                                line = line.replace(k, v)
                                break
                    f.write(line)


class GenerateTask(CopyTask):
    def __init__(self, prj: PrjInfo, parser: AlgoParser):
        """
        @param prj: 算法组件信息
        """
        super().__init__(prj, parser)
        self.__line_handler = LineHandler()

    def __register_handlers(self):
        self.__line_handler.register('{pkg-name}', self.prj.pkg_name)
        self.__line_handler.register('{module-name}', self.prj.module_name)
        self.__line_handler.register('{module-name-zh}', self.prj.module_name_zh)
        self.__line_handler.register('{lower-name}', self.prj.lower_name)
        self.__line_handler.register('{upper-name}', self.prj.upper_name)
        self.__line_handler.register('{ai-type}', self.parser.ai_type)
        self.__line_handler.register('{imported-headers}', '\n'.join(self.parser.headers))
        self.__line_handler.register('{algo-nss}', '\n'.join(self.parser.using_nss))
        self.__line_handler.register('{convert-methods}', self.__gen_convert_methods__())
        self.__line_handler.register('{itf-name}', self.parser.itf_impl_name)
        self.__line_handler.register('{jni-methods}', self.__gen_jni_methods__())
        self.__line_handler.register('{jni-register-methods}', self.__gen_native_register_array__())
        self.__line_handler.register('{init-method}', self.parser.methods.get_init_method())
        self.__line_handler.register('{native-methods}', self.__gen_native_methods__())
        self.__line_handler.register('{algo-lib-name}',
                                     self.parser.libraries[0] if len(self.parser.libraries) > 0 else '')

    def process(self):
        super().process()
        # 测试代码
        # if os.path.exists(self.prj.path):
        #     shutil.rmtree(self.prj.path)

        self.__register_handlers()

        print('start generate .gitignore file...')
        # 生成 .gitignore 文件
        with open(f"{self.prj.path}/.gitignore", mode='w') as f:
            f.writelines('build\n.cxx\nlibs/arm*\n')

        print('start generate AndroidManifest.xml file...')
        # 生成清单文件
        with open(f"{self.prj.path}/src/main/AndroidManifest.xml", mode='w') as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(f'  <manifest package="com.quvideo.mobile.component.ai.{self.prj.pkg_name}" />')

        # java 文件
        self.__generate_java__()

        # native 相关
        self.__generate_native__()

        # 以上都没有出错，修改 settings.gradle、config.gradle、upload-to-maven.sh
        self.__modify_compile_scripts__()

    def __write_line(self, line: str, f):
        f.write(self.__line_handler.handle(line.replace('//', '') if '{module-name-zh}' in line else line))

    def __generate_java__(self):
        # AIConstants.java 中定义了算法模块常量，这里更新一下算法类型
        ai_constants = f'{os.path.dirname(self.prj.path)}/AlgoBase/commonAI/src/main/java/' \
                       f'com/quvideo/mobile/component/common/AIConstants.java'
        if not os.path.exists(ai_constants):
            print(f"'{ai_constants}' 不存在！", file=sys.stderr)
            exit(1)
        raw_lines = read_lines(ai_constants)
        with open(ai_constants, mode='w') as f:
            for line in raw_lines:
                if '{module-name-zh}' in line:
                    line = line.replace('{module-name-zh}', self.prj.module_name_zh).replace('//', '')
                    f.write(line)
                else:
                    write_placeholder = False
                    if '{upper-name}' in line:
                        line = line.replace('{upper-name}', self.prj.upper_name)
                        write_placeholder = True
                    if '{ai-type}' in line:
                        line = line.replace('{ai-type}', self.parser.ai_type).replace('//', '')
                        write_placeholder = True
                    f.write(line)
                    if write_placeholder:
                        f.write('  ///** 算法类型-{module-name-zh} */\n')
                        f.write('  //public static final int AI_TYPE_{upper-name} = {ai-type};\n')

        # 生成 java 文件：生成枚举和结构体
        package = self.prj.java_dir[self.prj.java_dir.find('java/') + 5:].replace('/', '.')
        if self.parser.enums.size() > 0:
            for e in self.parser.enums:
                with open(self.prj.java_dir + f'/{e.name}.java', mode='w') as f:
                    f.write(e.gen_java_enum(package))
        if self.parser.structs.size() > 0:
            for s in self.parser.structs:
                with open(self.prj.java_dir + f'/{s.name}.java', mode='w') as f:
                    f.write(s.gen_javabean(package, self.parser.consts))
        # 根据模板生成对应的 java 文件，一般来讲有三个文件
        # Q{self.name}.java 提供给引擎使用
        # QE{self.name}Client.java 提供给业务使用（组件内部也会使用）
        # AI{self.name}.java 提供给业务使用
        with open(self.prj.java_dir + f'/Q{self.prj.module_name}.java', mode='w') as f:
            # 判断使用统一接口还是普通接口模板
            zero = '' if self.parser.itf_impl_name != '' else '0'
            for line in read_lines(self.prj.tmplt + f'/QModuleName{zero}.java.tmplt'):
                self.__write_line(line, f)
        with open(self.prj.java_dir + f'/QE{self.prj.module_name}Client.java', mode='w') as f:
            for line in read_lines(self.prj.tmplt + '/QEModuleNameClient.java.tmplt'):
                self.__write_line(line, f)
        with open(self.prj.java_dir + f'/AI{self.prj.module_name}.java', mode='w') as f:
            for line in read_lines(self.prj.tmplt + '/AIModuleName.java.tmplt'):
                self.__write_line(line, f)

        # 混淆规则
        with open(f"{self.prj.path}/consumer-rules.pro", mode='w') as f:
            f.write(f"-keep class com.quvideo.mobile.component.{self.prj.pkg_name}.** " + "{*;}")
        with open(f"{self.prj.path}/proguard-rules.pro", mode='w') as f:
            f.write(f"-keep class com.quvideo.mobile.component.{self.prj.pkg_name}.** " + "{*;}")

        # build.gradle 脚本
        with open(f"{self.prj.path}/build.gradle", mode='w') as f:
            for line in read_lines(self.prj.tmplt + '/build.gradle.tmplt'):
                self.__write_line(line, f)

    def __gen_native_methods__(self) -> str:
        methods: list[str] = []
        for m in self.parser.methods:
            body = m.gen_native_method()
            # print(f'java native method =====>\n{method_body}')
            methods.append(body)
        return '\n'.join(methods)

    def __generate_native__(self):
        # 生成算法组件 jni 实现，通过模板生成 cpp 文件
        print(f"start generate {self.prj.lower_name}_jni.cpp file...")
        if self.parser.itf_impl_name != '':
            self.__generate_cpp_union__()
        else:
            self.__generate_cpp_common__()

        print(f"start generate CMakeLists.txt file...")
        with open(f"{self.prj.path}/CMakeLists.txt", mode='w') as f:
            # 通过模板读取
            for line in read_lines(self.prj.tmplt + '/CMakeLists.txt.tmplt'):
                self.__write_line(line, f)

    def __generate_cpp_union__(self):
        with open(f"{self.prj.jni_dir}/{self.prj.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_lines(self.prj.tmplt + '/lower_name_jni.cpp.tmplt'):
                self.__write_line(line, f)

    def __generate_cpp_common__(self):
        with open(f"{self.prj.jni_dir}/{self.prj.lower_name}_jni.cpp", mode='w') as f:
            # 模板要根据算法接口类型来选择
            for line in read_lines(self.prj.tmplt + '/lower_name_jni_0.cpp.tmplt'):
                self.__write_line(line, f)

    def __gen_convert_methods__(self) -> str:
        # 没有需要转换的结构体
        if self.parser.structs.size() == 0:
            return ''
        # struct 和 java bean 相互转换，每个结构体有两组方法
        methods: list[str] = []
        for s in self.parser.structs:
            methods.append(f"// 自动生成的转换方法 '{s.name}'")
            # bean -> struct
            methods.append(s.gen_javabean_to_struct_method())
            # struct -> bean
            methods.append(s.gen_struct_to_javabean_method())
        return '\n'.join(methods)

    def __gen_jni_methods__(self) -> str:
        methods: list[str] = []
        for m in self.parser.methods:
            body = m.gen_jni_method(self.prj.module_name, self.parser.ai_type)
            # print(f'\ncpp jni method =====>\n{method_body}')
            methods.append(body)
        # 所有方法之间以换行符分割
        return '\n'.join(methods)

    def __gen_native_register_array__(self) -> str:
        signatures: list[str] = []
        for m in self.parser.methods:
            signatures.append('{' + m.gen_jni_method_signature(self.prj.module_name) + '}')
        return ',\n  '.join(signatures)

    def __modify_compile_scripts__(self):
        print('start modify settings.gradle file...')
        # 修改 settings.gradle、config.gradle、upload-to-maven.sh
        settings: str = self.prj.algo_root + '/settings.gradle'
        if not os.path.exists(settings):
            print(f"'{settings}' 不存在！")
            exit(1)
        raw_lines = read_lines(settings)
        for i in range(len(raw_lines)):
            line = raw_lines[i]
            if '{module-name}' in line:
                temp = line.replace('{module-name}', self.prj.module_name).replace('//', '')
                raw_lines[i] = temp[:temp.find(' /* ')] + '\n'
                raw_lines.insert(i + 1, line)
                break
        with open(settings, mode='w') as sf:
            sf.writelines(raw_lines)
        print('start modify config.gradle file...')
        # config.gradle upload-to-maven.sh
        config: str = self.prj.algo_root + '/config.gradle'
        if not os.path.exists(config):
            print(f"'{config}' 不存在！")
            exit(1)
        raw_lines = read_lines(config)
        new_lines = list[str]()
        for line in raw_lines:
            if '{module-name-zh}' in line:
                new_lines.append(line.replace('{module-name-zh}', self.prj.module_name_zh))
            elif '{module-name}' in line:
                left = line.find('{module-name}')
                right = line.rfind('{module-name}')

                new_lines.append(line.replace('{module-name}', self.prj.module_name).replace('//', ''))

                if left == right:
                    new_lines.append('    //lib{module-name},\n')
                else:
                    new_lines.append("  // {module-name-zh}\n")
                    new_lines.append("  //lib{module-name} = [artifact: '{module-name}AI',"
                                     " module: ':Component-AI:{module-name}AI']\n")
            else:
                if 'gVersion' in line:
                    pass
                new_lines.append(line)
        with open(config, mode='w') as cf:
            cf.writelines(new_lines)

        compile_script: str = self.prj.algo_root + '/upload-to-maven.sh'
        if not os.path.exists(compile_script):
            print(f"'{compile_script}' 不存在！")
            exit(1)
        print('start modify upload-to-maven.sh file...')
        raw_lines = read_lines(compile_script)
        for i in range(len(raw_lines)):
            line = raw_lines[i]
            if '{module-name}' in line:
                # 先处理上一行，处理之后再追加
                raw_lines[i - 1] = raw_lines[i - 1].replace('\n', '') + ' \\\n'
                # 处理这一行
                temp = line.replace('{module-name}', self.prj.module_name).replace('#', '')
                raw_lines[i] = temp[:temp.find(' \\ /*')] + '\n'
                # 追加后面的
                raw_lines.insert(i + 1, line)
                break
        with open(compile_script, mode='w') as csf:
            csf.writelines(raw_lines)


class AlgoCache:
    def __init__(self, prj_root: str):
        # 维护一个数据库： 算法库目录和算法组件目录的映射
        self.file = prj_root + '/.idea/algo_cache_db.json'
        ensure_path(os.path.dirname(self.file))
        self.__cache = json.loads(read_content(self.file, flatmap=True)) if os.path.exists(self.file) else {}

    def has(self, path: str) -> bool:
        return path in self.__cache

    def get(self, path: str) -> dict[str, any]:
        return self.__cache.get(path) if self.has(path) else None

    def update(self, path: str, value: dict[str, any], ai_type: str):
        value['ai_type'] = ai_type
        self.__cache[path] = value
        with open(self.file, mode='w') as f:
            f.write(json.dumps(self.__cache, ensure_ascii=False))


def read_opt(prompt: str, tester=None) -> str:
    if tester is None:
        return input(prompt)
    while True:
        value = input(prompt)
        if tester(value):
            return value


def run_main_flow(cwd: str):
    def path_tester(path__: str) -> bool:
        if path__ == '' or not os.path.exists(path__):
            print(f"'{path__}' 不存在, 请重新输入！")
            return False
        return True

    algo_lib_dir__ = read_opt("算法库目录（绝对路径）：", path_tester)

    def op_tester(op__: str) -> bool:
        if op__.lower() not in 'ua':
            print(f"操作类型错误：'{op__}'，请重新输入！")
            return False
        return True

    op_type__ = read_opt("操作类型（a 新增算法组件，u 更新算法组件）：", op_tester).lower()

    cache__ = AlgoCache(cwd)

    if 'u' == op_type__:
        # 通过脚本创建或更新过一次的算法模块，会有缓存文件存在
        if cache__.has(algo_lib_dir__):
            prj_info__ = cache__.get(algo_lib_dir__)
            ai_type__ = prj_info__.pop('ai_type')
            CopyTask(PrjInfo(**prj_info__), AlgoParser(algo_lib_dir__, ai_type__)).process()
            print('更新完成！')
            return
        else:
            # 非首次更新，需要以下信息
            print(f'首次更新，还需提供以下信息：')

    def num_tester(n__: str) -> bool:
        try:
            return int(n__) >= 0
        except ValueError as e:
            print(e)
            return False

    ai_type__ = read_opt("算法类型（正整数）：", tester=num_tester)
    module_name__ = read_opt("算法组件名（多个单词时以空格分割）：").lower()
    module_name_zh__ = read_opt("算法组件名（中文）：").lower()
    prj_info__ = PrjInfo(cwd, module_name__, module_name_zh__)
    if 'u' == op_type__:
        CopyTask(prj_info__, AlgoParser(algo_lib_dir__, ai_type__)).process()
        # 非脚本创建的算法模块，更新过算法库之后要同步缓存
        cache__.update(algo_lib_dir__, prj_info__.dict(), ai_type__)
        print('更新完成！')
        return
    # 新增算法模块
    GenerateTask(prj_info__, AlgoParser(algo_lib_dir__, ai_type__)).process()
    cache__.update(algo_lib_dir__, prj_info__.dict(), ai_type__)
    print('新增完成！')


def usage():
    placeholders = {
        '{imported-headers}': 'cpp 文件中需要导入的头文件',
        '{module-name}': '算法模块名',
        '{module-name-zh}': '算法模块中文名',
        '{algo-nss}': '算法命名空间',
        '{itf-name}': '算法统一接口名字',
        '{pkg-name}': 'java 包名',
        '{ai-type}': '算法类型，与模型下发有关',
        '{lower-name}': '大写_模块名',
        '{upper-name}': '小写_模块名',
        '{algo-lib-name}': '算法静态库名，去掉前缀和后缀',
        '{native-methods}': 'java 层定义的 native 方法',
        '{jni-methods}': 'cpp 中要实现的在 java 层定义的 native 方法',
        '{jni-register-methods}': 'jni 静态注册方法映射数组',
        '{init-method}': '算法初始化方法，其返回值比较特殊 AIInitResult',
        '{convert-methods}': 'cpp 结构体和 java bean 相互转化方法',
    }
    align = len(max(placeholders.keys(), key=len)) + 1
    print("===============================代码模板占位符说明===============================")
    for k, v in placeholders.items():
        print(k + ' ' * (align - len(k)) + ': ' + v)
    print("===============================代码模板占位符说明===============================")
    print()

    menus: list[str] = [
        '================================菜单================================',
        '操作类型（a 新增算法组件，u 更新算法组件）：a/A/u/U 不区分大小写',
        "算法库目录（绝对路径）：/path/to/algo/library",
        "！！！！！！ 如果是更新库，下面的操作都不需要了 ！！！！！！",
        "算法类型（正整数且不能与现有算法类型重复）：如：24（请与 iOS 协商好）",
        "算法组件名（多个单词时以空格分割）：image restore v2（程序内部会处理成首字母大写、剔除空格、驼峰命名等）",
        "算法组件名（中文）：画质修复 v2（用于生成代码注释）",
        '================================菜单================================'
    ]
    print("操作示例：\n" + '\n'.join(menus) + '\n')


def list_files(root_dir: str, output: list[str]):
    skip_list = ['AutoCrop-component', '.git', 'BasicLibs-component']
    for sub_dir in os.listdir(root_dir):
        if sub_dir not in skip_list:
            output.append(os.path.join(root_dir, sub_dir))


def run_test():
    root_ = '/home/binlee/code/open-source/quvideo/XYAlgLibs/XYCartoonLite'
    for item in find_files(root_, ['.zip'], tester=lambda p: 'android' in p.lower()):
        print(item)
        print('\n'.join(extract_file(item)))
    exit(1)
    pass


if __name__ == '__main__':

    # run_test()

    # 算法工程根目录
    cwd__ = os.path.dirname(__file__)
    cwd__ += '/../out'
    if not os.path.exists(cwd__ + '/Component-AI'):
        # 脚本必须在算法工程根目录执行
        print(f"脚本必须在算法工程根目录执行！当前：'{cwd__}'", file=sys.stderr)
        exit(1)
    print(f"project root: '{cwd__}'")
    # 使用说明
    # usage()
    # 开始运行主流程
    # run_main_flow(cwd__)
    # 新增算法模块
    algo_lib_dirs = []
    list_files('/home/binlee/code/open-source/quvideo/XYAlgLibs', algo_lib_dirs)
    list_files('/home/binlee/code/open-source/quvideo/XYAlgLibs/AutoCrop-component', algo_lib_dirs)
    algo_lib_dirs = ['/home/binlee/code/open-source/quvideo/XYAlgLibs/XYHumanPoseLite']
    # print('\n'.join(algo_lib_dirs))
    pattern = re.compile(r'[A-z][a-z]*')
    for ai, sub_dir in enumerate(algo_lib_dirs, 0):
        word: str = os.path.basename(sub_dir).replace('XY', '').replace('AI', '')
        pieces_ = []
        if '-' in word:
            pieces_ = word.split('-')
        elif '_' in word:
            pieces_ = word.split('_')
        else:
            pieces_.append(word)
        for pi in range(len(pieces_[1:])):
            pieces_[pi + 1] = pieces_[pi + 1].title()
        words = re.findall(pattern, ''.join(pieces_))
        name__ = ' '.join(words).lower()
        print(f'{word} -> {words} -> {name__}')
        GenerateTask(PrjInfo(cwd__, name__, f'zh-[{name__}]'), AlgoParser(sub_dir, str(123 + ai))).process()
        print(f"新增 '{name__}' 完成！")

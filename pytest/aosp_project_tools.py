import os
import sys
import time


def prepare_files(root: str, name: str = 'android.iml') -> (str, str):
    """
    准备文件：备份原文件
    :param root: 文件根目录
    :param name: 目标文件名
    :return: 目标文件及备份文件
    """
    target_file: str
    if root.endswith('/'):
        target_file = root + name
    else:
        target_file = root + '/' + name
    bak_file: str = target_file + '.bak'
    if not os.path.exists(bak_file):
        os.rename(target_file, bak_file)
    return target_file, bak_file


def read_input_file_lines(path: str) -> list:
    """
    读取输入文件行
    :param path: 文件路径
    :return: 文件所有行
    """
    out_lines: list = []
    with open(file=path, mode='r', encoding='utf_8') as f:
        while True:
            line: str = f.readline()
            if not line:
                break

            line = line.strip()
            if line.startswith('./'):
                out_lines.append(line[2:])
            elif line.startswith('.'):
                out_lines.append(line[1:])
        f.close()
    return out_lines


def read_and_insert(path: str, line_prefix, line_format, line_args: list) -> list:
    """
    读取输入文件所有行并插入新的行
    :param path: 文件路径
    :param line_prefix: 行前缀
    :param line_format: 格式化
    :param line_args: 格式化参数
    :return:
    """
    new_lines = []
    with open(file=path, mode='r', encoding='utf-8') as f:
        while True:
            line: str = f.readline()
            if not line:
                break

            new_lines.append(line)
            if line.strip().startswith(line_prefix) and len(line_args) > 0:
                print('line ->>> ' + line)
                for arg in line_args:
                    new_lines.append(line_format.format(arg))
                line_args.clear()
        f.close()
    return new_lines


def skip_test_folders(root: str):
    """
    修改 android.iml 文件，跳过测试工程
    :param root: aosp 工程根目录
    :return:
    """
    target_file, bak_file = prepare_files(root)
    new_lines: list = []
    with open(file=bak_file, mode='r', encoding='utf-8') as prj:
        for line in prj:
            if line.__contains__('isTestSource="true"') \
                    or line.__contains__('/cts/') \
                    or line.__contains__('/testapps/') \
                    or line.__contains__('/test/'):
                print(line.strip())
                continue
            new_lines.append(line)
        prj.close()
        print(f"total lines: {len(new_lines)}")
        print(f"time: {time.strftime('%Y-%m-%d-%H-%M-%S', time.localtime())}")
    with open(file=target_file, mode='w', encoding='utf-8') as prj:
        prj.writelines(new_lines)
        prj.close()


def exclude_folders(root: str, path: str):
    """
    修改 android.iml 文件，排除目录
    :param root aosp 工程根目录
    :param path: 需要排除的目录列表，保存在文件中
    :return:
    """
    target_file, bak_file = prepare_files(root)
    # 搜集需要排除的文件
    folder_names = read_input_file_lines(path)
    # 追加默认需要排除的文件
    folder_names.extend([
        "out/.microfactory_Linux_intermediates",
        "out/.module_paths",
        "out/.soong_ui_intermediates",
        "out/development",
        "out/soong/.minibootstrap",
        "out/soong/.glob",
        "out/soong/.bootstrap",
        "out/soong/host",
        "out/soong/ndk",
        "cts",
        "compatibility",
        "bootable",
        "build",
        "frameworks/base/api",
        "out/target/common/obj/PACKAGING",
        "tools/dexter/testdata",
        "platform_testing",
        "prebuilts",
        "frameworks/opt/setupwizard/tools",
        "packages/apps/Music/tests",
        "packages/apps/LegacyCamera/tests",
    ])

    # < excludeFolder url = "file://$MODULE_DIR$/./external/emma" />
    line_prefix = '<excludeFolder url="'
    line_format = '<excludeFolder url="file://$MODULE_DIR$/{0}" />\n'
    # 将要排除的文件插入到文件行中
    new_lines: list = read_and_insert(bak_file, line_prefix, line_format, folder_names)

    # 写入文件
    with open(file=target_file, mode='w', encoding='utf-8') as f:
        f.writelines(new_lines)
        f.close()


def skip_analyze_files(root: str, path: str):
    """
    修改 android.iwl 文件，跳过文件语法分析
    :param root aosp 工程根目录
    :param path: 需要排除的文件列表，保存在文件中
    :return:
    """
    target_file, bak_file = prepare_files(root, name='android.iws')
    # 搜集需要跳过的文件
    file_names = read_input_file_lines(path)

    # <component name="HighlightingSettingsPerFile">
    #     <setting file="file://$PROJECT_DIR$/{file_name}" root0="SKIP_INSPECTION" />
    line_prefix = '<component name="HighlightingSettingsPerFile">'
    line_format = '<setting file="file://$PROJECT_DIR$/{0}" root0="SKIP_INSPECTION" />\n'
    # 将要跳过的文件插入到文件行中
    new_lines = read_and_insert(bak_file, line_prefix, line_format, file_names)

    # 写入文件
    with open(file=target_file, mode='w', encoding='utf-8') as f:
        f.writelines(new_lines)
        f.close()


def usage() -> int:
    print(f"usage: {sys.argv[0]} [aosp project root path] [options] [file path]", file=sys.stderr)
    print("options are: skip-test-folders", file=sys.stderr)
    print("             exclude-folders [exclude folders file path]", file=sys.stderr)
    print("             skip-analyze-files [skip files file path]", file=sys.stderr)
    return 1


if __name__ == '__main__':
    print(f"args: {sys.argv}, len: {len(sys.argv)}", flush=True)
    if len(sys.argv) < 1:
        exit(usage())

    if sys.argv[2] == 'exclude-folders':
        exclude_folders(sys.argv[1], sys.argv[3])
    elif sys.argv[2] == 'skip-analyze-files':
        skip_analyze_files(sys.argv[1], sys.argv[3])
    else:
        skip_test_folders(sys.argv[1])

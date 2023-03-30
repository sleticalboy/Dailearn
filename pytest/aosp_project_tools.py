import os
import sys
import time


def prepare_files(root: str, name: str = 'android.iml') -> (str, str):
    target_file: str
    if root.endswith('/'):
        target_file = root + name
    else:
        target_file = root + '/' + name
    bak_file: str = target_file + '.bak'
    if not os.path.exists(bak_file):
        os.rename(target_file, bak_file)
    return target_file, bak_file


def read_input_file_lines(path: str, lines: list):
    with open(file=path, mode='r', encoding='utf_8') as f:
        while True:
            line: str = f.readline()
            if not line:
                break

            print('line -> ' + line)

            line = line.strip()
            if line.startswith('./'):
                lines.append(line[2:])
            elif line.startswith('.'):
                lines.append(line[1:])
        f.close()


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
    folder_names: list = [
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
    ]
    read_input_file_lines(path, folder_names)

    new_lines = []
    # 将要排除的文件追加到文件行中
    with open(file=bak_file, mode='r', encoding='utf-8') as f:
        while True:
            line: str = f.readline()
            if not line:
                break

            new_lines.append(line)
            if line.strip().startswith('<excludeFolder url="') and len(folder_names) > 0:
                print('line ->>> ' + line)
                for folder in folder_names:
                    new_lines.append(f'<excludeFolder url="file://$MODULE_DIR$/{folder}" />\n')
                folder_names.clear()
        f.close()

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
    file_names = []
    read_input_file_lines(path, file_names)

    new_lines = []
    # <component name="HighlightingSettingsPerFile">
    #     <setting file="file://$PROJECT_DIR$/{file_name}" root0="SKIP_INSPECTION" />
    # 将要排除的文件追加到文件行中
    with open(file=bak_file, mode='r', encoding='utf-8') as f:
        while True:
            line: str = f.readline()
            if not line:
                break

            new_lines.append(line)
            if line.strip() == '<component name="HighlightingSettingsPerFile">':
                print('line ->>> ' + line)
                for name in file_names:
                    new_lines.append(f'<setting file="file://$PROJECT_DIR$/{name}" root0'
                                     f'="SKIP_INSPECTION" />\n')
                file_names.clear()
        f.close()

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

import json
import os

error_like = ['_not', 'not_', '_no', 'no_', '_null', 'null_', '_fail', 'fail_', '_err', 'err_', '_error', 'error_',
              '_found', 'found_', '_invalid', 'invalid_', '_find', 'find_', '_exist', 'exist_', '_zero', 'zero_',
              '_unsupported', '_inconsistency', '_illegal', 'illegal_', 'out_of', '_too_', '_dont_', 'ok']


def like_error(name: str) -> bool:
    name = name.lower()
    for kw in error_like:
        if kw in name:
            return True
    # if '(' not in name:
    #     print(f'like_error() {name}')
    return False


def extract_consts(lines: list[str], consts_map: dict[int, set[str]]):
    begin_value: list[int] = [-1]

    def calc_value(exp: str) -> int:
        exp = exp.strip().lstrip('(').rstrip(')')
        try:
            if '+' in exp:
                pieces = exp.split('+')
                if begin_value[0] == -1:
                    return calc_value(pieces[0]) + calc_value(pieces[1])
                else:
                    return begin_value[0] + calc_value(pieces[1])
            elif '<<' in exp:
                pieces = exp.split('<<')
                if exp.lower().startswith('0b'):
                    return int(pieces[0], 2) << int(pieces[1])
            elif exp.lower().startswith('0x'):
                return int(exp, 16)
            elif exp.lower().startswith('0b'):
                return int(exp, 2)
            elif exp.isnumeric():
                return int(exp)
        except ValueError:
            pass
        return -1

    def parse_const_def(const_line: str):
        space_index = const_line.find(' ')
        if space_index < 0 or 'DIV_UP' in const_line:
            return
        pieces = const_line.split()
        name = pieces[0]
        value = ''.join(pieces[1:])
        ln = name.lower()
        if begin_value[0] == 0 and (ln.endswith('_base') or ln.endswith('_begin') or ln.endswith('_origin')):
            begin_value[0] = calc_value(value)
            # print(f'begin >>>>>>>>> {const_line} ->>>> {hex(begin_value[0])}')
        elif like_error(name):
            real_value = calc_value(value)
            if real_value != -1:
                # print(f'{const_line} ====>> {hex(real_value)}, name: {name}')
                if real_value not in consts_map:
                    consts_map[real_value] = set[str]()
                consts_map[real_value].add(name)

    for line in lines:
        line = line.replace('\n', '').strip()
        if line == '' or '#define' not in line or 'DIV_UP' in line:
            continue
        index = line.find('/*')
        if index > 0:
            line = line[:index].strip()
        index = line.find('//')
        if index > 0:
            line = line[:index].strip()
        parse_const_def(line[7:].strip())


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


def run_main_flow():
    path = '/home/binlee/code/open-source/quvideo/XYAlgLibs'
    balck_list = ['ios', 'mac', 'ubuntu', 'centos', 'windows']

    def header_filter(p: str) -> bool:
        p = p.lower()
        for bl in balck_list:
            if bl in p:
                return False
        return True

    all_consts: dict[int, set[str]] = {}
    path_filter: set[str] = set()
    for header_path in find_files(path, ['.h'], tester=header_filter):
        if header_path in path_filter:
            continue
        path_filter.add(header_path)
        with open(header_path, mode='r') as f:
            extract_consts(f.readlines(), all_consts)
    error_codes = list(all_consts.keys())
    error_codes.sort(reverse=False)

    copy_consts = {}
    for err in error_codes:
        hex_err = hex(err)
        formatted_err = hex_err[0:2] + hex_err[2:].zfill(8)
        print(f'{formatted_err}: {all_consts[err]}')
        copy_consts[formatted_err] = list(all_consts[err])
    with open('../out/errors.json', mode='w') as f:
        json.dump(copy_consts, fp=f)


def run_main_test():
    hex_num = hex(16)
    print(hex_num[0:2] + hex_num[2:].zfill(8))
    print('HELLO_WORLD'.isupper())
    exit(0)


if __name__ == '__main__':
    # run_main_test()
    run_main_flow()

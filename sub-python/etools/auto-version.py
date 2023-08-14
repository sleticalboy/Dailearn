import math


class Version:
    def __init__(self, text: str, release: bool = False):
        # test: 4.5.301 -> 4.5.302
        # release: 4.5.3 -> 4.6.4
        self.__release = release
        self.__current = text[text.rfind(' ') + 1:].replace('\'', '')
        segments = self.__current.split('.')
        if release:
            fix = math.floor((int(segments[2]) * math.pow(10, 3 - len(segments[2])) + 100) / 100)
            minor = int(segments[1])
            if fix >= 10:
                minor += 1
                fix = 0
            if minor >= 10:
                segments[1] = '0'
                segments[0] = str(int(segments[0]) + 1)
            else:
                segments[1] = str(minor)
            segments[2] = str(fix)
        else:
            fix = int(int(segments[2]) * math.pow(10, 3 - len(segments[2]))) + 1
            temp = str(fix)
            # 补齐
            segments[2] = '0' * (3 - len(temp)) + temp
        self.__next = '.'.join(segments)
        # print(f"{release} {self.__current} -> {segments} -> {'.'.join(segments)}")

    def next(self) -> str:
        return self.__next

    def __str__(self) -> str:
        return '{}: {} -> {}'.format('release' if self.__release else 'test   ', self.__current, self.__next)


def run_version_test():
    lines = ['4.5.9', '4.5.340', '4.5.30', '4.5.378', '4.9.901', '9.9.9', '6.0.0', '4.5.001']
    for i in range(len(lines)):
        print(Version(lines[i], False))
        print(Version(lines[i], True))


def run_num_test():
    print(int('-19'))
    try:
        print(int('-19.0'))
    except ValueError as e:
        print(e)


if __name__ == '__main__':
    run_version_test()
    # run_num_test()

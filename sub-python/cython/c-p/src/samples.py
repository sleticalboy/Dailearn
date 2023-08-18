class User(object):
    def __init__(self, name):
        self.name = name

    def say_hello(self):
        print(f"hey, my name is {self.name}")


def hello():
    print("hello() func called")


def add(a, b):
    return a + b


def print_list(li):
    print(f"list is {li}, len: {len(li)}")


def get_list() -> list[str]:
    return ['30', '99']


def get_dict():
    return {
        'name': 'tom',
        'age': 30,
    }

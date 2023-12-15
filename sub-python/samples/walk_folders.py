import logging
import os
import threading
import time
import traceback

from concurrent import futures

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s %(message)s',
                    filename='a.log', filemode='w')

logging.debug('starting....')

logging.disable(logging.DEBUG)


thread_executor = futures.ThreadPoolExecutor(max_workers=5, thread_name_prefix="Test-")


def walk_dir(path):
    logging.debug(path)
    for root, dirs, files in os.walk(path):
        logging.debug(f'current dir: {root}')
        for sd in dirs:
            logging.info(f'sub dir {sd} in {root}')
            pass
        for f in files:
            logging.warning(f'file {f} in {root}')
            pass
    pass


def main():
    cwd = os.getcwd()
    # args 是传给 walk_dir 的参数列表
    t = threading.Thread(target=walk_dir, args=[os.path.dirname(cwd)])
    t.start()
    t.join()

    logging.debug("another walk dir")
    thread_executor.submit(walk_dir, os.path.dirname(os.path.dirname(cwd)))
    thread_executor.shutdown()

    time.sleep(1)
    try:
        raise Exception(f'main() run')
    except:
        logging.error(traceback.format_exc())
    time.sleep(0.5)

    for i in range(100, 0, -2):
        if i % 16 == 0:
            logging.debug(f'num is {i}')
        elif i % 14 == 0:
            logging.info(f'num is {i}')
        elif i % 12 == 0:
            logging.warning(f'num is {i}')
        elif i % 10 == 0:
            logging.error(f'num is {i}')
        elif i % 6 == 0:
            logging.critical(f'num is {i}')

    pass


if __name__ == '__main__':
    main()
    logging.debug('ending...')

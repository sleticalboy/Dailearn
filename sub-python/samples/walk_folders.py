import logging
import os
import time
import traceback

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s %(message)s',
                    filename='a.log', filemode='w')

logging.debug('starting....')

logging.disable(logging.WARNING)


def main():
    cwd = os.getcwd()
    logging.debug(cwd)
    for root, dirs, files in os.walk(os.path.dirname(cwd)):
        logging.debug(f'current dir: {root}')
        for sd in dirs:
            logging.info(f'sub dir {sd} in {root}')
            pass
        for f in files:
            logging.warning(f'file {f} in {root}')
            pass
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

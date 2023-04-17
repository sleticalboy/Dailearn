import logging
import subprocess

import httpx


def run_http2_test():
    # url = 'https://up.enterdesk.com/edpic/35/b9/c0/35b9c067ff54d43a1956c3e0661f22f2.jpg'
    # url = 'http://pic1.win4000.com/mobile/2019-09-17/5d80a795d62a2.jpg'
    url = 'https://up.enterdesk.com/edpic_source/99/d8/7d/99d87d992ff02a72e2fdda69d18d6dd0.jpg'

    # *  CAfile: /etc/ssl/certs/ca-certificates.crt
    # *  CApath: /etc/ssl/certs
    user_agent = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36'
    with subprocess.Popen(f"curl -A '{user_agent}' -v '{url}' -o /tmp/aaa.jpg",
                          text=True, shell=True, stdout=subprocess.PIPE) as p:
        p.wait()
        for line in p.stdout.readlines():
            print(line)

    print("\n" * 3)
    logging.basicConfig(
        format="%(levelname)s [%(asctime)s] %(name)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
        level=logging.FATAL
    )
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/112.0.0.0 Safari/537.36",
    }
    with httpx.Client(http1=True, http2=True, headers=headers) as client:
        # DEBUG [2023-04-17 12:48:26] httpx - load_ssl_context verify=True cert=None trust_env=True http2=False
        # DEBUG [2023-04-17 12:48:26] httpx - load_verify_locations cafile='/etc/ssl/certs/ca-certificates.crt'
        r = client.get(url)
        print(r.status_code)
        print(r.http_version)
        print(r.headers)


if __name__ == '__main__':
    run_http2_test()

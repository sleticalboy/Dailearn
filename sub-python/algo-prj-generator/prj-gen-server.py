import json
import os
import subprocess
import uuid
from http.server import HTTPServer, BaseHTTPRequestHandler

# 工作目录
work_dir = './tmp'


# This class will handles any incoming request from the browser
class HttpHandler(BaseHTTPRequestHandler):

    # Handler for the GET requests
    def do_GET(self):
        print(self.headers)
        print(f'request path: {self.path}, pwd: {os.getcwd()}')
        if '/' == self.path or '/favicon.ico' == self.path:
            self.send_response(200, 'OK')
            self.send_header('Content-Type', 'text/html')
            self.end_headers()
            with open('index.html', mode='rb') as fp:
                self.wfile.write(fp.read())
        elif '/jquery-3.6.0.js' == self.path:
            self.send_response(200, 'OK')
            self.send_header('Content-Type', 'text/javascript')
            self.end_headers()
            with open('jquery-3.6.0.js', mode='rb') as fp:
                self.wfile.write(fp.read())
            pass
        else:
            self.send_error(404, 'Not Found')

    def do_POST(self):
        print(f'{self.command} {self.path}\n------\n{self.headers}------')
        if '/api/v1/generate' in self.path:
            content_len = int(self.headers['Content-Length'])
            body = self.rfile.read(content_len).decode()
            prj_type = body.split('&')[0].split('=')[1]
            prj_name = body.split('&')[1].split('=')[1]
            print(f'request body: {body}, prj type: {prj_type}, prj name: {prj_name}')

            from prj_generator import PrjGenerator
            generator = PrjGenerator(prj_type, prj_name, work_dir)
            prj_file, msg = generator.gen_prj()
            data = {'url': None, 'code': 500, 'msg': msg}
            if prj_file != "":
                data['url'] = prj_file
                data['code'] = 200

            self.send_response(code=data['code'], message='OK')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(data, ensure_ascii=True).encode())
            self.wfile.flush()
        else:
            self.send_error(404, 'Not Found')


def get_host_ip() -> str:
    output = subprocess.check_output(['ifconfig'])
    for line in output.decode('utf-8').split('\n'):
        if 'inet ' in line and '255.0.0.0' not in line:
            # print(line)
            return line.split()[1]
    return ''


def run_main_flow():
    port_number = 18100
    ip = get_host_ip()
    # ip = '172.16.3.9'
    # Create a web server and define the handler to manage the incoming request
    server_ = HTTPServer((ip, port_number), RequestHandlerClass=HttpHandler)
    try:
        addr = f'http://{ip}:{port_number}'
        print(f'Started httpserver on {addr}')
        print(f'update[POST]: {addr}/api/v1/gen')
        # Wait forever for incoming htto requests
        server_.serve_forever()
    except OSError or KeyboardInterrupt:
        server_.shutdown()
        server_.server_close()


if __name__ == '__main__':
    id_ = uuid.uuid4()
    print(f'id: {id_.__str__()}, type: {type(id_)}')
    run_main_flow()
    pass

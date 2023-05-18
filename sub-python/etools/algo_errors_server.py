import json
import subprocess
import urllib.parse
from http.server import HTTPServer, BaseHTTPRequestHandler


def formtted_err(raw: str) -> str:
    if raw == '':
        return ''
    base = 10
    if raw.lower().startswith('0x'):
        base = 16
    elif raw.lower().startswith('0b'):
        base = 2
    hex_num = hex(int(raw, base))
    return hex_num[:2] + hex_num[2:].zfill(8)


class ErrorMap:
    def __init__(self):
        self.__load__()

    def __load__(self):
        with open('../out/errors.json', mode='r') as f:
            self.error_map = json.load(f)

    def reload(self):
        self.__load__()

    def detail(self, key: str) -> list[any]:
        try:
            return self.error_map[formtted_err(key)]
        except KeyError as e:
            raise e


server_: HTTPServer
error_map = ErrorMap()


# This class will handles any incoming request from the browser
class HttpHandler(BaseHTTPRequestHandler):

    # Handler for the GET requests
    def do_GET(self):
        # print(self.requestline)
        # print(self.command)
        # print(self.headers)
        print(f'request path: {self.path}')
        if '/' == self.path or '/favicon.ico' == self.path:
            self.send_response(200, 'OK')
            self.send_header('Content-Type', 'text/html')
            self.end_headers()
            self.wfile.write('<h1>Hello Python Server</h1>'.encode('utf-8'))
        elif '/api/v1/query_err' in self.path:
            r = urllib.parse.urlparse(self.path)
            # response format: {data:, status:, message:}
            response = {}
            if r.query != '':
                try:
                    response['status'] = 200
                    response['message'] = f"成功查询到 '{r.query}' 对应以下错误："
                    response['data'] = error_map.detail(r.query)
                except KeyError:
                    response['status'] = 404
                    response['message'] = f"没有找到 '{r.query}' 对应的错误，或许你可以问下算法的同学哦"
                    response['data'] = []
            else:
                response['status'] = 400
                response['message'] = '查询格式错误：xxx/query_err?{二/十/十六进制错误码}'
                response['data'] = []
            print(f'parsed path: {r}')
            print(f'msg: {response}')
            self.send_response(code=200, message='OK')
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        else:
            self.send_error(404, 'Not Found')

    def do_POST(self):
        if '/api/v1/update_err' in self.path:
            content_length = int(self.headers['Content-Length'])
            # 读取响应内容并处理（一定要按照长度读取，否则会阻塞客户端）
            request_body = self.rfile.read(content_length).decode('utf-8')
            print(f'update errors... {request_body}')
            response = {'status': 200, 'message': '更新成功', 'data': []}
            self.send_response(code=200, message='OK')
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
            self.wfile.flush()
            # 更新之后重新加载 error_map
            error_map.reload()
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
    global server_
    port_number = 8099
    ip = get_host_ip()
    try:
        # Create a web server and define the handler to manage the incoming request
        server_ = HTTPServer((ip, port_number), RequestHandlerClass=HttpHandler)
        print(f'Started httpserver on http://{ip}:{port_number}')
        # Wait forever for incoming htto requests
        server_.serve_forever()
    except OSError or KeyboardInterrupt:
        server_.shutdown()
        server_.server_close()


if __name__ == '__main__':
    run_main_flow()

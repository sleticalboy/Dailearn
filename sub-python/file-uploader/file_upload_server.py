import json
import os
from http.server import HTTPServer, BaseHTTPRequestHandler

# 工作目录
work_dir = './tmp'


# This class will handles any incoming request from the browser
class HttpHandler(BaseHTTPRequestHandler):

    # Handler for the GET requests
    def do_GET(self):
        print(f'request path: {self.path}, pwd: {os.getcwd()}')
        if '/' == self.path or '/favicon.ico' == self.path or '/index.html' == self.path:
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

    def do_OPTIONS(self):
        print(self.headers)
        if '/api/v1/upload' in self.path:
            self.send_response(code=200, message='OK')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.flush()
        else:
            self.send_error(404, 'Not Found')
        pass

    def do_POST(self):
        print(f'headers: {self.headers}, path: {self.path}')
        if '/api/v1/upload' in self.path:
            content_length = int(self.headers['Content-Length'])
            # 读取响应内容并处理（一定要按照长度读取，否则会阻塞客户端）
            request_body = self.rfile.read(content_length)
            save_path = work_dir + "/" + self.path.split('=')[1]
            print(f'save path: {save_path}, file len: {content_length}')
            status_code = 200
            status_msg = "OK"
            url = ""
            try:
                with open(save_path, 'wb') as fp:
                    fp.write(request_body)
                    url = save_path
            except Exception as e:
                status_code = 500
                status_msg = f'{e}'
                pass
            response = {'status': status_code, 'msg': status_msg, 'url': url}
            self.send_response(code=status_code, message=status_msg)
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
            self.wfile.flush()
        else:
            self.send_error(404, 'Not Found')


def run_main_flow():
    port_number = 18100
    # Create a web server and define the handler to manage the incoming request
    server_ = HTTPServer(('0.0.0.0', port_number), RequestHandlerClass=HttpHandler)
    try:
        addr = f'http://{server_.server_address[0]}:{server_.server_address[1]}'
        print(f'Started httpserver on {addr}')
        print(f'update[POST]: {addr}/api/v1/upload')
        # Wait forever for incoming htto requests
        server_.serve_forever()
    except OSError or KeyboardInterrupt:
        server_.shutdown()
        server_.server_close()


if __name__ == '__main__':
    run_main_flow()

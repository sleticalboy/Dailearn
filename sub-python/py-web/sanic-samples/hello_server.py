import os

from sanic import Sanic, html

app = Sanic(__name__)


@app.get('/')
def index(request):
    return html("Welcome Here")


# 上传文件
@app.route('/upload', methods=['POST'])
def upload_file(request):
    uploaded_file = request.files['file']
    if uploaded_file:
        file_path = os.path.join('uploads', uploaded_file.filename)
        uploaded_file.save(file_path)
        return f"File '{uploaded_file.filename}' uploaded successfully."


# 列出所有文件
@app.route('/list')
def list_files(request):
    files = os.listdir('uploads')
    return {'files': files}


# 删除文件
@app.route('/delete/<filename>')
def delete_file(request, filename):
    file_path = os.path.join('uploads', filename)
    if os.path.exists(file_path):
        os.remove(file_path)
        return f"File '{filename}' deleted successfully."
    else:
        return f"File '{filename}' not found."


if __name__ == '__main__':
    # 创建上传文件夹
    if not os.path.exists('uploads'):
        os.makedirs('uploads')

    # 运行 Sonic 应用
    app.run(debug=True)

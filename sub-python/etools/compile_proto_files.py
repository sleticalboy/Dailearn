import os


def compile_proto_files(common_files: list[str], is_rpc: bool = False):
    cmd = './deps/protobuf/ubuntu/bin/protoc -I ./idl --cpp_out=./algo_cc --plugin=./bin/protoc-gen-go'
    cmd += f" --go_out={'plugins=grpc:' if is_rpc else ''}/tmp"
    for item in common_files:
        os.system(f'{cmd} {item}')


def run_main_flow():
    if os.path.exists("/tmp/gitlab.quvideo.com/"):
        os.system('rm -r /tmp/gitlab.quvideo.com/')
    cwd = './idl'
    common_files: list[str] = []
    rpc_files: list[str] = []
    for item in os.listdir(cwd):
        with open(f'{cwd}/{item}', mode='r') as f:
            if 'rpc ' in ''.join(f.readlines()):
                rpc_files.append(f'{cwd}/{item}')
            else:
                common_files.append(f'{cwd}/{item}')
    # print(f'skip rpc files: {rpc_files}')
    compile_proto_files(rpc_files, True)
    compile_proto_files(common_files, False)
    # copy 文件
    os.system('cp -rv /tmp/gitlab.quvideo.com/algo/grpc.git/** ./')
    # 删除不需要的文件
    os.system("""rm -rv algo_cc/callback.pb.* algo_cc/gateway.pb.* algo_cc/sync.pb.* algo_cc/algo_progress.pb*;
     rm -rv video_landmarkspb/video_landmarks_data.pb.*""")


if __name__ == '__main__':
    run_main_flow()

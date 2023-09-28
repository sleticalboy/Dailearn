import math
import os

import ffmpeg


def run_main_flow():
    # 删除临时文件
    os.system('rm -rfv /tmp/*.mp4')

    input_file = '/home/binlee/Downloads/audio/dance-4k.mp4'
    input_file = '/home/binlee/Downloads/audio/10s-timer.mp4'

    # 获取视频时长并计算文件分片数
    probe = ffmpeg.probe(input_file)
    video_stream = next((stream for stream in probe['streams'] if stream['codec_type'] == 'video'), None)
    duration = int(float(video_stream['duration']) * 1000)
    print(f"duration: {duration}ms, w: {video_stream['width']}, h: {video_stream['height']}")
    part_fils = []
    part_rfils = []
    for i in range(math.ceil(duration / 5000)):
        part_fils.append(f'/tmp/part-{i}.mp4')
        part_rfils.append(f'/tmp/part-r-{i}.mp4')
    # 1、切割视频
    # ffmpeg -i input.mp4 -c copy -map 0 -segment_time 300 -f segment output%d.mp4
    os.system(f'ffmpeg -i {input_file} -c copy -map 0 -segment_time 5 -f segment -y /tmp/part-%d.mp4 >/dev/null')
    # 2、分段倒放
    for i in range(len(part_fils)):
        # ffmpeg -i output1.mp4 -vf reverse reversed_output1.mp4
        os.system(f'ffmpeg -i {part_fils[i]} -vf reverse -y {part_rfils[i]} >/dev/null')

    # 逆序后写入文件，后续合并视频时使用
    part_rfils.reverse()
    with open('/tmp/all-r-files.txt', 'wt') as f:
        for prf in part_rfils:
            f.write(f"file '{prf}'\n")
    # 3、合并视频
    # ffmpeg -i "concat:reversed_output1.mp4|reversed_output2.mp4|reversed_output3.mp4" -c copy reversed_full.mp4
    os.system(f'ffmpeg -f concat -safe 0 -i /tmp/all-r-files.txt -c copy -y /tmp/full_reversed.mp4 >/dev/null')
    os.system(f'ffplay /tmp/full_reversed.mp4 >/dev/null')


if __name__ == '__main__':
    run_main_flow()

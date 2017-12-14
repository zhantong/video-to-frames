# VideoToFrames [![Build Status](https://travis-ci.org/zhantong/video-to-frames.svg?branch=master)](https://travis-ci.org/zhantong/video-to-frames)

## 特性

- 需要Android API 21
- 直接将视频解码为YUV格式帧，不经过OpenGL，不转换为RGB
- 对绝大多数设备和绝大多数视频编码格式，都可以解码得到NV21或I420格式帧数据
- 30ms内获得NV21或I420格式帧数据
- 10ms内将NV21或I420格式帧数据写入到文件
- 对得到的NV21格式帧数据，在110ms内完成JPEG格式的转换和写入到文件

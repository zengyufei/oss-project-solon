# pip3 install pycryptodome
import base64
from Crypto.Cipher import PKCS1_v1_5
from Crypto import Random
from Crypto.PublicKey import RSA
import requests

import os
import json
import time
import sys
import urllib.parse

import datetime
import getopt
from urllib import parse


def downloadFile(name, url, headers={}, interval=0.5):
    def MB(byte):
        return byte / 1024 / 1024

    print(name)
    res = requests.get(url, stream=True, headers={**headers, 'Proxy-Connection': 'keep-alive'})
    fileLen = res.headers.get('content-length')
    file_size = 0
    if not fileLen == None:
        file_size = int(fileLen)  # 文件大小 Byte
    f = open(name, 'wb')
    down_size = 0  # 已下载字节数
    old_down_size = 0  # 上一次已下载字节数
    time_ = time.time()
    for chunk in res.iter_content(chunk_size=512):
        if chunk:
            f.write(chunk)
            down_size += len(chunk)
            if time.time() - time_ > interval:
                # rate = down_size / file_size * 100  # 进度  0.01%
                speed = (down_size - old_down_size) / interval  # 速率 0.01B/s

                old_down_size = down_size
                time_ = time.time()

                print_params = [MB(speed), MB(down_size), MB(file_size), (file_size - down_size) / speed]
                print('\r{:.1f}MB/s - {:.1f}MB，共 {:.1f}MB，还剩 {:.0f} 秒   '.format(*print_params), end='')

    f.close()
    print('\r下载成功' + ' ' * 50)



class Downloader(object):
    def __init__(self, url, file_path):
        self.url = url
        self.file_path = file_path

    def start(self):
        res_length = requests.get(self.url, stream=True)
        print(res_length.headers)
        total_size = int(res_length.headers['Content-Length'])
        print(res_length)
        if os.path.exists(self.file_path):
            temp_size = os.path.getsize(self.file_path)
            print("当前：%d 字节， 总：%d 字节， 已下载：%2.2f%% " % (temp_size, total_size, 100 * temp_size / total_size))
        else:
            temp_size = 0
            print("总：%d 字节，开始下载..." % (total_size,))

        headers = {'Range': 'bytes=%d-' % temp_size,
                   "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0"}
        res_left = requests.get(self.url, stream=True, headers=headers)

        with open(self.file_path, "ab") as f:
            for chunk in res_left.iter_content(chunk_size=1024):
                temp_size += len(chunk)
                f.write(chunk)
                f.flush()

                done = int(50 * temp_size / total_size)
                sys.stdout.write("\r[%s%s] %d%%" % ('█' * done, ' ' * (50 - done), 100 * temp_size / total_size))
                sys.stdout.flush()



# 加密
def encryption(text: str, public_key: bytes):
    # 字符串指定编码（转为bytes）
    text = text.encode('utf-8')
    #print(text)
    # 构建公钥对象
    cipher_public = PKCS1_v1_5.new(RSA.importKey(public_key))
    # 加密（bytes）
    text_encrypted = cipher_public.encrypt(text)
    #print(text_encrypted)
    # base64编码，并转为字符串
    text_encrypted_base64 = base64.b64encode(text_encrypted).decode()
    return text_encrypted_base64


def decryption(text_encrypted_base64: bytes, private_key: bytes):
    # 字符串指定编码（转为bytes）
    text_encrypted_base64 = text_encrypted_base64.encode('utf-8')
    # base64解码
    text_encrypted = base64.b64decode(text_encrypted_base64)
    # 构建私钥对象
    cipher_private = PKCS1_v1_5.new(RSA.importKey(private_key))
    # 解密（bytes）
    text_decrypted = cipher_private.decrypt(text_encrypted, Random.new().read)
    # 解码为字符串
    text_decrypted = text_decrypted.decode()
    return text_decrypted

def goToDownload(host, port, uri, file, output, isUri):

    public_key = open("public.key").read()

    # 格式化成2016-03-20 11:45:39形式
    strftime = (datetime.datetime.now() + datetime.timedelta(seconds=360)).strftime("%Y-%m-%d %H:%M:%S")
    print('密文有效时间：', strftime)
    # 2018-05-09 16:56:07

    # 加密
    text = '{"name": "mybatisplus","time": "'+strftime+'"}'
    text_encrypted_base64 = encryption(text, public_key)
    print('密文：', text_encrypted_base64)

    param = urllib.parse.quote(text_encrypted_base64)
    list_url = f'http://{host}:{port}/api/file/list?key={param}'
    res = requests.get(list_url, stream=True)
    print('请求成功返回值：', res.text)

    result = json.loads(res.content)
    list = result['data']
    for r in list:
        nameStr = r['fileFullName']
        if nameStr == file:
            dateStr = r['lastModified']
            urlStr = r['url']
            size = r['size']
            txtFilePath = os.path.join(output, f'{nameStr}.txt')

            lastDate = ''
            if os.path.exists(txtFilePath):
                lastDate = open(txtFilePath, 'r').read()

            if lastDate == '' or dateStr != lastDate:
                print(nameStr, dateStr, urlStr)

                if isUri:
                    url = f'{uri}&key={param}'
                else:
                    url = f'http://{host}:{port}/{urlStr}&size={size}&key={param}'
                print('完整的下载链接：', url)
                # 下载文件
                downloadFile( os.path.join(output, file), url)
                print("下载完成.")

                f = open(txtFilePath, 'w+')
                f.write(dateStr)
                f.close()
                print("保存文件完成.")
            else:
                print(nameStr, dateStr, lastDate, '无变化，无需下载')

    # downloader = Downloader(url, os.path.join(output, file))
    # downloader.start()



if __name__ == '__main__':
    isUri =False
    uri = '/api/v2/file/download'
    host = '127.0.0.1'
    port = '8081'
    file = None
    output = os.getcwd()

    argv = sys.argv[1:]

    try:
        opts, arg = getopt.getopt(argv, "h:p:u:f:o:", ["uri", "host", "port", "file", "output"])

        for o, value in opts:
            if o in ("-h", "--host"):
                host = value
            if o in ("-p", "--port"):
                port = value
            if o in ("-u", "--uri"):
                uri = value
                isUri = True
            if o in ("-f", "--file"):
                file = value
            if o in ("-o", "--output"):
                output = value

        print('目标域名：', host)
        print('目标端口：', port)
        print('下载地址链接：', uri)
        print('下载的文件名：', file)
        print('存放下载文件夹：', output)

        goToDownload(host, port, uri, file, output, isUri)
    except Exception as error:
        print(error)

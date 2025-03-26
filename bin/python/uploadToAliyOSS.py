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

def send(host, port, uri, file):
    if not os.path.exists(file):
        raise Exception('路径不存在')

    public_key = open("public.key").read()

    # 格式化成2016-03-20 11:45:39形式
    strftime = (datetime.datetime.now() + datetime.timedelta(seconds=360)).strftime("%Y-%m-%d %H:%M:%S")
    print('密文有效时间：', strftime)
    # 2018-05-09 16:56:07

    # 加密
    text = '{"name": "mybatisplus","time": "'+strftime+'"}'
    text_encrypted_base64 = encryption(text, public_key)
    print('密文：', text_encrypted_base64)

    files = {'file': open(file, 'rb')}
    param = urllib.parse.quote(text_encrypted_base64)
    url = f'http://{host}:{port}{uri}?key={param}'
    print('完整的上传链接：', url)
    response = requests.post(url, files=files)
    print('请求成功返回值：', response.text)

    jsonStr = json.loads(response.text)
    data = jsonStr.get('data')
    if data:
        contents = data[1]
        print('文件的新下载地址：', contents)

def testSend():
    uri = '/api/file/upload'
    host = '127.0.0.1'
    port = '8081'
    file = "d:/run.log"
    send(host, port, uri, file)

def 生产发送():
    uri = '/api/file/upload'
    host = '127.0.0.1'
    port = '8081'
    file = None

    argv = sys.argv[1:]

    try:
        opts, arg = getopt.getopt(argv, "h:p:u:f:", ["uri", "host", "port", "file"])

        for o, value in opts:
            if o in ("-h", "--host"):
                host = value
            if o in ("-p", "--port"):
                port = value
            if o in ("-u", "--uri"):
                uri = value
            if o in ("-f", "--file"):
                file = value

        print('目标域名：', host)
        print('目标端口：', port)
        print('上传地址链接：', uri)
        print('上传的文件路径：', file)
    except Exception as error:
        print(error)

    send(host, port, uri, file)

if __name__ == '__main__':
	生产发送()
    #testSend()

package com.zyf.test;

import com.zyf.utils.RsaUtil;

import java.util.Map;

public class 公私钥生成 {

    public static void main(String[] args) throws Exception {
        run();
    }

    private static void run() throws Exception {
        // 生成密钥对
        Map<String, String> map = RsaUtil.genKeyPair();
        final String publicBase64Str = map.get(RsaUtil.PUBLIC_KEY);
        final String privateBase64Str = map.get(RsaUtil.PRIVATE_KEY);
        System.out.println("---------- ------------------- ----------");
        // 公钥
        System.out.println(publicBase64Str);
        System.out.println("---------- ------------------- ----------");
        // 私钥
        System.out.println(privateBase64Str);
        System.out.println("---------- ------------------- ----------");

        {

            final String testStr = "你好";
            System.out.println("你好");
            System.out.println("---------- ------------------- ----------");

            // RSA加密
            final String encrypt = RsaUtil.encodePublicKey(testStr, publicBase64Str);
            System.out.println(encrypt);
            System.out.println("---------- ------------------- ----------");

            // RSA签名
            // final String signStr = RsaUtil.signPrivateKey(encrypt, privateBase64Str);
            // System.out.println("sign: "+ signStr);
            // System.out.println("---------- ------------------- ----------");

            // RSA解密
            final String result = RsaUtil.decodePrivateKey(encrypt, privateBase64Str);
            System.out.println(result);
            System.out.println("---------- ------------------- ----------");

            // RSA验签
            // System.out.println("verify: "+RsaUtil.verifyPublicKey(encrypt, publicBase64Str, signStr));
            // System.out.println("---------- ------------------- ----------");
        }
    }




}

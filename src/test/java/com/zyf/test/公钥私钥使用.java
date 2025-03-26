package com.zyf.test;

import cn.hutool.core.io.resource.ClassPathResource;
import com.zyf.utils.RsaUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class 公钥私钥使用 {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("private.key");
        final String privateKey = classPathResource.readUtf8Str();
        classPathResource = new ClassPathResource("public.key");
        final String publicKey = classPathResource.readUtf8Str();


        {
            final String encodeData = RsaUtil.encodePublicKey("aaa", publicKey);
            System.out.println(encodeData);
            final String decodeData = RsaUtil.decodePrivateKey(encodeData, privateKey);
            System.out.println(decodeData);
        }

        {
            final String encodeData = RsaUtil.encodePrivateKey("bbb", privateKey);
            System.out.println(encodeData);
            final String decodeData = RsaUtil.decodePublicKey(encodeData, publicKey);
            System.out.println(decodeData);
        }

    }

}

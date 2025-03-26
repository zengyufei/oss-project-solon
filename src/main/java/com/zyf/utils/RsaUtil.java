package com.zyf.utils;


import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.crypto.SecureUtil;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RsaUtil {


    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * 签名算法
     */
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 获取公钥的key
     */
    public static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    public static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 加密的块大小
     */
    private static int encryptBlockSize = -1;
    /**
     * 解密的块大小
     */
    private static int decryptBlockSize = -1;


    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    /**
     * 锁
     */
    private static final KeyFactory keyFactory;
    private static final Lock lock = new ReentrantLock();
    private static final ConcurrentHashMap<Integer, Cipher> cipherMap = new ConcurrentHashMap<>();
    private static final Cipher cipher;

    static {
        SecureUtil.addProvider(provider);
        try {
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, provider);
            cipher = Cipher.getInstance(CIPHER_ALGORITHM, provider);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     *
     * @return
     * @throws Exception
     */
    public static Map<String, String> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();


        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, String> keyMap = new HashMap<>(2);
        final String publicBase64Str = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        final String privateBase64Str = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        StringJoiner sj = new StringJoiner("\n");
        sj.add("-----BEGIN PUBLIC KEY-----");
        sj.add(
                formatKey(
                        publicBase64Str
                                .replace("_", "/")
                                .replace("-", "+")
                )
        );
        sj.add("-----END PUBLIC KEY-----");

        keyMap.put(PUBLIC_KEY, sj.toString());
        keyMap.put(PRIVATE_KEY, privatePem(privateBase64Str));
        return keyMap;
    }


    private static String signPrivateKey(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64Utils.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return Base64Utils.encode(signature.sign());
    }

    public static String signPrivateKey(String dataStr, String privateKey) throws Exception {
        return signPrivateKey(dataStr.getBytes(StandardCharsets.UTF_8), privateKey .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", ""));
    }

    public static boolean verifyPublicKey(String dataStr, String publicKey, String sign) throws Exception {
        return verifyPublicKey(dataStr.getBytes(StandardCharsets.UTF_8), publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", ""), sign);
    }

    private static boolean verifyPublicKey(byte[] data, String publicKey, String sign) throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign));
    }


    public static String decodePrivateKey(String encryptedData, String privateKey) throws Exception {
        final byte[] bytes = decrypt(Base64Utils.decode(encryptedData), privateKey, false);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String decodePublicKey(String encryptedData, String privateKey) throws Exception {
        final byte[] bytes = decrypt(Base64Utils.decode(encryptedData), privateKey, true);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] decrypt(byte[] encryptedData, String keyStr, boolean isPulic) throws Exception {
        byte[] keyBytes;
        if (isPulic) {
            keyBytes = Base64Utils.decode(keyStr
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", ""));
        } else {
            keyBytes = Base64Utils.decode(keyStr
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", ""));
        }
        return decrypt(encryptedData, keyBytes, isPulic);
    }

    private static byte[] decrypt(byte[] data, byte[] keyBytes, boolean isPulic) throws Exception {
        final Key key;
        if (isPulic) {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            key = keyFactory.generatePublic(keySpec);
        } else {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            key = keyFactory.generatePrivate(keySpec);
        }

        lock.lock();
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);

            if (decryptBlockSize < 0) {
                // 在引入BC库情况下，自动获取块大小
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    decryptBlockSize = blockSize;
                }
            }
            // 模长
            final int dataLength = data.length;

            final int maxBlockSize = decryptBlockSize < 0 ? dataLength : decryptBlockSize;

            // 不足分段
            if (dataLength <= maxBlockSize) {
                return cipher.doFinal(data, 0, dataLength);
            }

            // 分段解密
            try (final FastByteArrayOutputStream out = new FastByteArrayOutputStream()) {

                int offSet = 0;
                // 剩余长度
                int remainLength = dataLength;
                int blockSize;
                // 对数据分段处理
                while (remainLength > 0) {
                    blockSize = Math.min(remainLength, maxBlockSize);
                    out.write(cipher.doFinal(data, offSet, blockSize));

                    offSet += blockSize;
                    remainLength = dataLength - offSet;
                }

                return out.toByteArray();
            }
        } finally {
            lock.unlock();
        }
    }

    public static String encodePublicKey(String data, String keyStr) throws Exception {
        final byte[] bytes = encrypt(data.getBytes(StandardCharsets.UTF_8), keyStr, true);
        return Base64Utils.encode(bytes);
    }

    public static String encodePrivateKey(String data, String keyStr) throws Exception {
        final byte[] bytes = encrypt(data.getBytes(StandardCharsets.UTF_8), keyStr, false);
        return Base64Utils.encode(bytes);
    }

    private static byte[] encrypt(byte[] data, String keyStr, boolean isPulic) throws Exception {
        byte[] keyBytes;
        if (isPulic) {
            keyBytes = Base64Utils.decode(keyStr
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", ""));
        } else {
            keyBytes = Base64Utils.decode(keyStr
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", ""));
        }
        return encrypt(data, keyBytes, isPulic);
    }

    private static byte[] encrypt(byte[] data, byte[] keyBytes, boolean isPulic) throws Exception {
        final Key key;
        if (isPulic) {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            key = keyFactory.generatePublic(keySpec);
        } else {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            key = keyFactory.generatePrivate(keySpec);
        }

        lock.lock();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);

            final int maxBlockSize = encryptBlockSize < 0 ? data.length : encryptBlockSize;

            if (encryptBlockSize < 0) {
                // 在引入BC库情况下，自动获取块大小
                final int blockSize = cipher.getBlockSize();
                if (blockSize > 0) {
                    encryptBlockSize = blockSize;
                }
            }

            // 模长
            final int dataLength = data.length;

            // 不足分段
            if (dataLength <= maxBlockSize) {
                return cipher.doFinal(data, 0, dataLength);
            }

            try (final FastByteArrayOutputStream out = new FastByteArrayOutputStream()) {
                int offSet = 0;
                // 剩余长度
                int remainLength = dataLength;
                int blockSize;
                // 对数据分段处理
                while (remainLength > 0) {
                    blockSize = Math.min(remainLength, maxBlockSize);
                    out.write(cipher.doFinal(data, offSet, blockSize));

                    offSet += blockSize;
                    remainLength = dataLength - offSet;
                }

                return out.toByteArray();
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * 格式化java生成的key，一行长的，不适合pem中的-----BEGIN PUBLIC KEY-----，pem已经有换行了
     *
     * @param key
     */
    private static String formatKey(String key) {
        if (key == null) return null;

        StringJoiner sj = new StringJoiner("\n");

        key = key.replace("\n", "");

        int count = (key.length() - 1) / 64 + 1;
        for (int i = 0; i < count; i++) {
            if (i + 1 == count) {
                // 循环的最后一次
                sj.add(key.substring(i * 64));
                // System.out.println(key.substring(i*64));
            } else {
                sj.add(key.substring(i * 64, i * 64 + 64));
                // System.out.println(key.substring(i*64,i*64+64));
            }
        }
        return sj.toString();
    }

    private static String privatePem(String privateKey) throws Exception {
        byte[] privBytes = Base64.getDecoder().decode(privateKey);

        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
        ASN1Encodable encodable = pkInfo.parsePrivateKey();
        ASN1Primitive primitive = encodable.toASN1Primitive();
        byte[] privateKeyPKCS1 = primitive.getEncoded();

        return pkcs1ToPem(privateKeyPKCS1, false);
    }

    private static String pkcs1ToPem(byte[] pcks1KeyBytes, boolean isPublic) throws Exception {
        String type;
        if (isPublic) {
            type = "RSA PUBLIC KEY";
        } else {
            type = "RSA PRIVATE KEY";
        }

        PemObject pemObject = new PemObject(type, pcks1KeyBytes);
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();

        return stringWriter.toString();
    }
}

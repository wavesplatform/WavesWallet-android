package com.wavesplatform.sdk.crypto;

import org.apache.commons.codec.binary.Base64;
import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.modes.OFBBlockCipher;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.ISO10126d2Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

public class AESUtil {

//    private static Logger mLogger = LoggerFactory.getLogger(AESUtil.class);

    public static final int DEFAULT_PBKDF2_ITERATIONS_V2 = 5000;
    public static final int QR_CODE_PBKDF_2ITERATIONS = 10;

    public static final int MODE_CBC = 0;
    public static final int MODE_OFB = 1;

    private static final int AESBlockSize = 4;
    private static final int KEY_BIT_LEN = 256;

    private static byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);
        return range;
    }

    // AES 256 PBKDF2 CBC iso10126 decryption
    // 16 byte IV must be prepended to ciphertext - Compatible with crypto-js
    public static String decrypt(String ciphertext, String password, int iterations)
            throws UnsupportedEncodingException, InvalidCipherTextException, DecryptionException {

        return decryptWithSetMode(ciphertext, password, iterations, MODE_CBC, new ISO10126d2Padding());
    }

    public static String decryptWithSetMode(String ciphertext, String password, int iterations,
                                            int mode, BlockCipherPadding padding)
            throws InvalidCipherTextException, UnsupportedEncodingException, DecryptionException {

        byte[] cipherdata = Base64.decodeBase64(ciphertext.getBytes());

        //Separate the IV and cipher data
        byte[] iv = copyOfRange(cipherdata, 0, AESBlockSize * 4);
        byte[] input = copyOfRange(cipherdata, AESBlockSize * 4, cipherdata.length);

        PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(
                password.toCharArray()), iv, iterations);
        KeyParameter keyParam = (KeyParameter) generator.generateDerivedParameters(256);

        CipherParameters params = new ParametersWithIV(keyParam, iv);

        BlockCipher cipherMode;
        if (mode == MODE_CBC) {
            cipherMode = new CBCBlockCipher(new AESEngine());

        } else {
            //mode == MODE_OFB
            cipherMode = new OFBBlockCipher(new AESEngine(), 128);
        }

        BufferedBlockCipher cipher;
        if (padding != null) {
            cipher = new PaddedBufferedBlockCipher(cipherMode, padding);
        } else {
            cipher = new BufferedBlockCipher(cipherMode);
        }

        cipher.reset();
        cipher.init(false, params);

        // create a temporary buffer to decode into (includes padding)
        byte[] buf = new byte[cipher.getOutputSize(input.length)];
        int len = cipher.processBytes(input, 0, input.length, buf, 0);
        len += cipher.doFinal(buf, len);

        // remove padding
        byte[] out = new byte[len];
        System.arraycopy(buf, 0, out, 0, len);

        // return string representation of decoded bytes
        String result = new String(out, "UTF-8");
        if (result.isEmpty()) {
            throw new DecryptionException("Decrypted string is empty.");
        }

        return result;
    }

    // AES 256 PBKDF2 CBC iso10126 encryption
    public static String encrypt(String cleartext, String password, int iterations) throws Exception {

        return encryptWithSetMode(cleartext, password, iterations, MODE_CBC, new ISO10126d2Padding());
    }

    private static String encryptWithSetMode(String cleartext, String password, int iterations,
                                             int mode, BlockCipherPadding padding) throws Exception {

        if (password == null) {
            throw  new Exception("Password null");
        }

        // Use secure random to generate a 16 byte iv
        SecureRandom random = new SecureRandom();
        byte iv[] = new byte[AESBlockSize * 4];
        random.nextBytes(iv);

        byte[] clearbytes = cleartext.getBytes("UTF-8");

        PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(
                password.toCharArray()), iv, iterations);
        KeyParameter keyParam = (KeyParameter) generator.generateDerivedParameters(256);

        CipherParameters params = new ParametersWithIV(keyParam, iv);

        BlockCipher cipherMode;
        if (mode == MODE_CBC) {
            cipherMode = new CBCBlockCipher(new AESEngine());

        } else {
            //mode == MODE_OFB
            cipherMode = new OFBBlockCipher(new AESEngine(), 128);
        }

        BufferedBlockCipher cipher;
        if (padding != null) {
            cipher = new PaddedBufferedBlockCipher(cipherMode, padding);
        } else {
            cipher = new BufferedBlockCipher(cipherMode);
        }

        cipher.reset();
        cipher.init(true, params);

        byte[] outBuf = cipherData(cipher, clearbytes);

        // Append to IV to the output
        int len1 = iv.length;
        int len2 = outBuf.length;
        byte[] ivAppended = new byte[len1 + len2];
        System.arraycopy(iv, 0, ivAppended, 0, len1);
        System.arraycopy(outBuf, 0, ivAppended, len1, len2);

//      String ret = Base64.encodeBase64String(ivAppended);
        byte[] raw = Base64.encodeBase64(ivAppended);
        return new String(raw);
    }

    private static byte[] cipherData(BufferedBlockCipher cipher, byte[] data) {
        int minSize = cipher.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int len1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
        int len2 = -1;
        try {
            len2 = cipher.doFinal(outBuf, len1);
        } catch (InvalidCipherTextException icte) {
            icte.printStackTrace();
        }

        int actualLength = len1 + len2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

    /**
     * Use secure random to generate a 16 byte iv
     * @return
     */
    private static byte[] getSalt() {

        SecureRandom random = new SecureRandom();
        byte iv[] = new byte[AESBlockSize * 4];
        random.nextBytes(iv);

        return iv;
    }

    /**
     *
     * @param key AES key (256 bit Buffer)
     * @param data e.g. "{'aaa':'bbb'}"
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] encryptWithKey(byte[] key, String data) throws UnsupportedEncodingException {

        byte[] iv = getSalt();
        byte[] dataBytes = data.getBytes("utf-8");

        KeyParameter keyParam = new KeyParameter(key);
        CipherParameters params = new ParametersWithIV(keyParam, iv);

        BlockCipher cipherMode = new CBCBlockCipher(new AESFastEngine());
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cipherMode, new ISO10126d2Padding());
        cipher.reset();
        cipher.init(true, params);

        byte[] outBuf = cipherData(cipher, dataBytes);

        // Concatenate iv
        int len1 = iv.length;
        int len2 = outBuf.length;
        byte[] ivAppended = new byte[len1 + len2];
        System.arraycopy(iv, 0, ivAppended, 0, len1);
        System.arraycopy(outBuf, 0, ivAppended, len1, len2);

        return Base64.encodeBase64(ivAppended);
    }

    /**
     *
     * @param key AES key (256 bit Buffer)
     * @param ciphertext Base64 encoded concatenated payload + iv
     * @return
     * @throws InvalidCipherTextException
     * @throws UnsupportedEncodingException
     */
    public static String decryptWithKey(byte[] key, String ciphertext)
            throws InvalidCipherTextException, UnsupportedEncodingException {

        byte[] dataBytesB64 = Base64.decodeBase64(ciphertext.getBytes("utf-8"));

        //Separate the IV and cipher data
        byte[] iv = copyOfRange(dataBytesB64, 0, AESBlockSize * 4);
        byte[] dataBytes = copyOfRange(dataBytesB64, AESBlockSize * 4, dataBytesB64.length);

        KeyParameter keyParam = new KeyParameter(key);
        CipherParameters params = new ParametersWithIV(keyParam, iv);

        BlockCipher cipherMode = new CBCBlockCipher(new AESFastEngine());
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cipherMode, new ISO10126d2Padding());
        cipher.reset();
        cipher.init(false, params);

        //Create a temporary buffer to decode into (includes padding)
        byte[] buf = new byte[cipher.getOutputSize(dataBytes.length)];
        int len = cipher.processBytes(dataBytes, 0, dataBytes.length, buf, 0);
        len += cipher.doFinal(buf, len);

        //Remove padding
        byte[] out = new byte[len];
        System.arraycopy(buf, 0, out, 0, len);

        return new String(out, "UTF-8");
    }

    public static byte[] stringToKey(String string, int iterations) throws UnsupportedEncodingException {

        byte[] salt = new String("salt").getBytes("utf-8");

        PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(
                string.toCharArray()), salt, iterations);
        KeyParameter keyParam = (KeyParameter) generator.generateDerivedParameters(KEY_BIT_LEN);

        return  keyParam.getKey();
    }

    public static class DecryptionException extends Exception {
        public DecryptionException() {
        }

        public DecryptionException(String message) {
            super(message);
        }
    }
}
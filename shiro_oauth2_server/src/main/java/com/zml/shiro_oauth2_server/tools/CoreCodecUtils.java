package com.zml.shiro_oauth2_server.tools;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.zip.CRC32;

/**
 * 算法、摘要、签名、验签、加密、解密
 *
 * @author user
 * @since 2015-07-15 10:58
 */
public class CoreCodecUtils {

	protected static final org.apache.logging.log4j.Logger logger                  = LogManager.getLogger(CoreCodecUtils.class);
	public static final    String                          KEY_SHA1                = "SHA1";
	public static final    String                          KEY_MD5                 = "MD5";
	public static final    String                          KEY_RSA                 = "RSA";
	public static final    String                          KEY_DESEDE              = "DESede";
	public static final    String                          KEY_AES                 = "AES";
	private static final   String                          SIGNATURE_ALGORITHM_SHA = "SHA1WithRSA";
	private static final   String                          SIGNATURE_ALGORITHM_MD5 = "MD5WithRSA";
	/**
	 * 加密/解密算法/工作模式/填充方式
	 */
	public static final    String                          CIPHER_ALGORITHM_DESEDE = "DESede/ECB/PKCS5Padding";
	public static final    String                          CIPHER_ALGORITHM_AES    = "AES/CBC/PKCS5PADDING";
	public static final    String                          CIPHER_ALGORITHM_RSA    = "RSA/ECB/PKCS1Padding";
	private static final   byte[]                          AES_CBC_INIT_VECTOR     = "RandomInitVector".getBytes();
	public static final    String                          AES_DEFAULT_SEED        = "lmhd";

	/**
	 * CRC32加密
	 *
	 * @param source   源数据
	 * @param encoding 编码
	 * @return crc32摘要
	 */
	public static Long encryptCRC32(String source, String encoding) throws RuntimeException {
		try {
			CRC32 crc32 = new CRC32();
			crc32.update(source.getBytes(encoding));
			return crc32.getValue();
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * CRC32加密
	 *
	 * @param source 源数据
	 * @return crc32摘要
	 */
	public static Long encryptCRC32(String source) throws RuntimeException {
		return CoreCodecUtils.encryptCRC32(source, Charsets.UTF_8.name());
	}

	/**
	 * MD5加密
	 *
	 * @param source   源数据
	 * @param encoding 编码
	 * @return md5摘要
	 */
	public static String encryptMD5(String source, String encoding) throws RuntimeException {
		try {
			return DigestUtils.md5Hex(source.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * MD5加密
	 *
	 * @param source 源数据
	 * @return 加密结果
	 */
	public static String encryptMD5(String source) throws RuntimeException {
		return CoreCodecUtils.encryptMD5(source, Charsets.UTF_8.name());
	}

	/**
	 * SHA1加密
	 *
	 * @param source   源数据
	 * @param encoding 编码
	 * @return sha1摘要
	 */
	public static String encryptSHA1(String source, String encoding) throws RuntimeException {
		try {
			return DigestUtils.sha1Hex(source.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * SHA1加密
	 *
	 * @param source 源数据
	 * @return 加密结果
	 */
	public static String encryptSHA1(String source) throws RuntimeException {
		return CoreCodecUtils.encryptSHA1(source, Charsets.UTF_8.name());
	}

	/**
	 * BASE64加密
	 *
	 * @param source 源数据
	 * @return 加密结果
	 */
	public static String encryptBASE64(byte[] source) {
		return Base64.getEncoder().encodeToString(source);
	}

	/**
	 * BASE64解密
	 *
	 * @param source 源数据
	 * @return 解密结果
	 */
	public static byte[] decryptBASE64(String source) {
		return Base64.getDecoder().decode(source);
	}

	/**
	 * HEX加密
	 *
	 * @param source 源数据
	 * @return 加密结果
	 */
	public static String encryptHEX(byte[] source) {
		Hex hex = new Hex();
		return new String(hex.encode(source));
	}

	/**
	 * HEX解密
	 *
	 * @param source 源数据
	 * @return 解密结果
	 */
	public static byte[] decryptHEX(String source) throws RuntimeException {
		try {
			Hex hex = new Hex();
			return hex.decode(source.getBytes());
		} catch (DecoderException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * RSA用私钥对信息生成数字签名
	 *
	 * @param source     源数据
	 * @param privateKey 私钥
	 * @return 签名结果
	 */
	public static byte[] signWithRSAByPrivateKey(byte[] source, byte[] privateKey) throws RuntimeException {
		try {
			//构造PKCS8EncodedKeySpec对象
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
			//指定加密算法
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
			//取私钥匙对象
			PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
			//用私钥对信息生成数字签名
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA);
			signature.initSign(privateKey2);
			signature.update(source);
			return signature.sign();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * RSA用公钥验证数字签名
	 *
	 * @param source    源数据
	 * @param publicKey 公钥
	 * @param sign      签名
	 * @return 签名结果
	 */
	public static boolean verifyWithRSAByPublicKey(byte[] source, byte[] publicKey, byte[] sign) {
		try {
			//构造X509EncodedKeySpec对象
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
			//指定加密算法
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
			//取公钥匙对象
			PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);
			//用公钥对信息生成数字签名
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA);
			signature.initVerify(publicKey2);
			signature.update(source);
			//验证签名是否正常
			return signature.verify(sign);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * RSA公钥加密
	 *
	 * @param source    源数据
	 * @param publicKey 公钥
	 * @return 加密后的结果
	 */
	public static byte[] encryptWithRSAByPublicKey(byte[] source, byte[] publicKey) throws RuntimeException {
		try {
			//构造X509EncodedKeySpec对象
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
			//指定加密算法
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
			//取公钥匙对象
			PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);
			//加密
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_RSA);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey2);
			return cipher.doFinal(source);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * RSA私钥解密
	 *
	 * @param source     源数据
	 * @param privateKey 私钥 使用base64编码之后的key
	 * @return 解密后的结果
	 */
	public static byte[] decryptWithRSAByPrivateKey(byte[] source, byte[] privateKey) throws RuntimeException {
		try {
			//构造PKCS8EncodedKeySpec对象
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
			//指定加密算法
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_RSA);
			//取公钥匙对象
			PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
			//解密
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_RSA);
			cipher.init(Cipher.DECRYPT_MODE, privateKey2);
			return cipher.doFinal(source);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 3des加密
	 *
	 * @param source 源数据
	 * @param key    私钥 使用base64编码之后的key
	 * @return 加密后的结果
	 */
	public static byte[] encryptWithDESedeByKey(byte[] source, byte[] key) throws RuntimeException {
		try {
			//构造DESedeKeySpec对象
			DESedeKeySpec deSedeKeySpec = new DESedeKeySpec(key);
			//指定加密算法
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_DESEDE);
			//取公钥匙对象
			SecretKey secretKey = secretKeyFactory.generateSecret(deSedeKeySpec);
			//加密
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_DESEDE);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(source);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 3des解密
	 *
	 * @param source 源数据
	 * @param key    私钥 使用base64编码之后的key
	 * @return 解密后的结果
	 */
	public static byte[] decryptWithDESedeByKey(byte[] source, byte[] key) throws RuntimeException {
		try {
			//构造DESedeKeySpec对象
			DESedeKeySpec deSedeKeySpec = new DESedeKeySpec(key);
			//指定加密算法
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_DESEDE);
			//取公钥匙对象
			SecretKey secretKey = secretKeyFactory.generateSecret(deSedeKeySpec);
			//解密
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_DESEDE);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(source);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private static byte[] getHash(final String algorithm, final byte[] data) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(data);
			return digest.digest();
		} catch (final Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	/**
	 * AES加密
	 *
	 * @param source 源数据
	 * @param seed   最终加密使用的key的生成种子
	 * @return 加密后的结果
	 * @throws RuntimeException
	 */
	public static byte[] encryptWithAESByKey(byte[] source, byte[] seed) throws RuntimeException {
		return encryptWithAESByKey(source, seed, AES_CBC_INIT_VECTOR);
	}

	/**
	 * AES解密
	 *
	 * @param source 源数据
	 * @param seed   最终加密使用的key的生成种子
	 * @return 解密后的结果
	 * @throws RuntimeException
	 */
	public static byte[] decryptWithAESByKey(byte[] source, byte[] seed) throws RuntimeException {
		return decryptWithAESByKey(source, seed, AES_CBC_INIT_VECTOR);
	}

	/**
	 * AES加密
	 *
	 * @param source     源数据
	 * @param seed       最终加密使用的key的生成种子
	 * @param initVector 16字节/128bit
	 * @return 加密后的结果
	 */
	public static byte[] encryptWithAESByKey(byte[] source, byte[] seed, byte[] initVector) throws RuntimeException {
		if (initVector == null || initVector.length != 16) {
			logger.error("Wrong IV length: must be 16 bytes long");
			throw new RuntimeException("Wrong IV length: must be 16 bytes long");
		}
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector);
			SecretKeySpec keySpec = new SecretKeySpec(getHash(KEY_MD5, seed), KEY_AES);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_AES);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
			return cipher.doFinal(source);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * AES解密
	 *
	 * @param source     源数据
	 * @param seed       最终解密使用的key的生成种子
	 * @param initVector 16字节/128bit
	 * @return 解密后的结果
	 * @throws RuntimeException
	 */
	public static byte[] decryptWithAESByKey(byte[] source, byte[] seed, byte[] initVector) throws RuntimeException {
		if (initVector == null || initVector.length != 16) {
			logger.error("Wrong IV length: must be 16 bytes long");
			throw new RuntimeException("Wrong IV length: must be 16 bytes long");
		}
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector);
			SecretKeySpec keySpec = new SecretKeySpec(getHash(KEY_MD5, seed), KEY_AES);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_AES);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
			return cipher.doFinal(source);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 加密
	 * 1.构造密钥生成器
	 * 2.根据ecnodeRules规则初始化密钥生成器
	 * 3.产生密钥
	 * 4.创建和初始化密码器
	 * 5.内容加密
	 * 6.返回字符串
	 */
	public static String AESEncode(String content, String encykey) {
		try {
			//1.构造密钥生成器，指定为AES算法,不区分大小写
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			//2.根据ecnodeRules规则初始化密钥生成器
			//生成一个128位的随机源,根据传入的字节数组
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(encykey.getBytes(Charsets.UTF_8.name()));
			keygen.init(128, random);
			//3.产生原始对称密钥
			SecretKey original_key = keygen.generateKey();
			//4.获得原始对称密钥的字节数组
			byte[] raw = original_key.getEncoded();
			//5.根据字节数组生成AES密钥
			SecretKey key = new SecretKeySpec(raw, "AES");
			//6.根据指定算法AES自成密码器
			Cipher cipher = Cipher.getInstance("AES");
			//7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.ENCRYPT_MODE, key);
			//8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
			byte[] byte_encode = content.getBytes("utf-8");
			//9.根据密码器的初始化方式--加密：将数据加密
			byte[] byte_AES = cipher.doFinal(byte_encode);
			//10.将加密后的数据转换为字符串
			//这里用Base64Encoder中会找不到包
			//解决办法：
			//在项目的Build path中先移除JRE System Library，再添加库JRE System Library，重新编译后就一切正常了。
			String AES_encode = Base64.getEncoder().encodeToString(byte_AES);
			//11.将字符串返回
			return AES_encode;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//如果有错就返加nulll
		return null;
	}

	/**
	 * 解密
	 * 解密过程：
	 * 1.同加密1-4步
	 * 2.将加密后的字符串反纺成byte[]数组
	 * 3.将加密内容解密
	 */
	public static String AESDecode(String content, String encykey) {
		try {
			//1.构造密钥生成器，指定为AES算法,不区分大小写
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			//2.根据ecnodeRules规则初始化密钥生成器
			//生成一个128位的随机源,根据传入的字节数组
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(encykey.getBytes(Charsets.UTF_8.name()));
			keygen.init(128, random);
			//3.产生原始对称密钥
			SecretKey original_key = keygen.generateKey();
			//4.获得原始对称密钥的字节数组
			byte[] raw = original_key.getEncoded();
			//5.根据字节数组生成AES密钥
			SecretKey key = new SecretKeySpec(raw, "AES");
			//6.根据指定算法AES自成密码器
			Cipher cipher = Cipher.getInstance("AES");
			//7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.DECRYPT_MODE, key);
			//8.将加密并编码后的内容解码成字节数组
			byte[] byte_content = Base64.getDecoder().decode(content);
			/*
			 * 解密
			 */
			byte[] byte_decode = cipher.doFinal(byte_content);
			String AES_decode = new String(byte_decode, "utf-8");
			return AES_decode;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("兄弟，配置文件中的密码需要使用AES加密，请使用com.lmhd.common.util.AESUtil工具类修改这些值！" + content);
			//e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		//如果有错就返加nulll
		return null;
	}

	public static String getSign(String... params) {
		List<String> srcList = new ArrayList<String>();
		for (String param : params) {
			srcList.add(param);
		}
		// 按照字典序逆序拼接参数
		Arrays.sort(params);
		Collections.sort(srcList, String.CASE_INSENSITIVE_ORDER);
		Collections.reverse(srcList);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < srcList.size(); i++) {
			sb.append(srcList.get(i));
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String s = AESEncode("Sowhat?", AES_DEFAULT_SEED);
		//root：RzihMfaGmVi1rl8utKRrLA==
		//
		String s1 = AESDecode("RzihMfaGmVi1rl8utKRrLA==", AES_DEFAULT_SEED);
		System.out.println(s);
	}
}
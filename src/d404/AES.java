package d404;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

public class AES {

	public String Encrypt(String sSrc, String sKey) throws Exception {
		try {			
			if (sKey == null) {
				System.out.println("Key is null");
				return null;
			}
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//kgen.init(128, new SecureRandom(sKey.getBytes()));
			
			SecureRandom sRnd = SecureRandom.getInstance("SHA1PRNG");
			sRnd.setSeed(sKey.getBytes());
			kgen.init(256, sRnd);
			
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec sKeySpec = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = sSrc.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
			byte[] result = cipher.doFinal(byteContent);
			
			return byte2hex(result);
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch(NoSuchPaddingException e) {
			e.printStackTrace();
		} catch(InvalidKeyException e) {
			e.printStackTrace();
		} catch(IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch(BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String Decrypt(String sSrc, String sKey)  throws Exception {
		try {
			byte[] deSrc = hex2byte(sSrc);
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//kgen.init(128, new SecureRandom(sKey.getBytes()));
			
			SecureRandom sRnd = SecureRandom.getInstance("SHA1PRNG");
			sRnd.setSeed(sKey.getBytes());
			kgen.init(256, sRnd);
			
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec sKeySpec = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
			byte[] result = cipher.doFinal(deSrc);
		
			return new String(result);
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
		}
		return null;
	}
	
	public String byte2hex(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0XFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}
	
	public byte[] hex2byte (String hexStr) {
		if (hexStr.length() < 1) {
			return null;
		}
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result [i] = (byte) (high * 16 + low);
		}
		return result;
	}
}

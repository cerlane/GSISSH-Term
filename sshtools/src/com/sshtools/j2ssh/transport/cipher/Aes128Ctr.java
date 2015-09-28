package com.sshtools.j2ssh.transport.cipher;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.transport.AlgorithmOperationException;

public class Aes128Ctr 
	 extends SshCipher {
		  private static Log log = LogFactory.getLog(Aes128Ctr.class);

		  /**  */
		  protected static String algorithmName = "aes128-ctr";
		  Cipher cipher;

		  /**
		   * Creates a new AesCtr object.
		   */
		  public Aes128Ctr() {
		  }

		  /**
		   *
		   *
		   * @return
		   */
		  public int getBlockSize() {
		    return cipher.getBlockSize();
		  }

		  /**
		   *
		   *
		   * @param mode
		   * @param iv
		   * @param keydata
		   *
		   * @throws AlgorithmOperationException
		   */
		  public void init(int mode, byte[] iv, byte[] keydata) throws
		      AlgorithmOperationException {
		    try {
		      cipher = Cipher.getInstance("AES/CTR/NoPadding");

		      // Create a 16 byte key
		      byte[] actualKey = new byte[16];
		      System.arraycopy(keydata, 0, actualKey, 0, actualKey.length);

		      SecretKeySpec keyspec = new SecretKeySpec(actualKey, "AES");//"Blowfish");

		      //KeyGenerator kg = KeyGenerator.getInstance("AES");
		      // Create the cipher according to its algorithm
		      //cipher.init(Cipher.ENCRYPT_MODE, kg.generateKey());
		      cipher.init( ( (mode == ENCRYPT_MODE) ? Cipher.ENCRYPT_MODE
	                    : Cipher.DECRYPT_MODE),
	                  keyspec, new IvParameterSpec(iv, 0, cipher.getBlockSize()));
		    }
		    catch (NoSuchPaddingException nspe) {
		      log.error("Blowfish initialization failed", nspe);
		      throw new AlgorithmOperationException("No Padding not supported");
		    }
		    catch (NoSuchAlgorithmException nsae) {
		      log.error("Blowfish initialization failed", nsae);
		      throw new AlgorithmOperationException("Algorithm not supported");
		    }
		    catch (InvalidKeyException ike) {
		      log.error("Blowfish initialization failed", ike);
		      throw new AlgorithmOperationException("Invalid encryption key");
	
		      /*} catch (InvalidKeySpecException ispe) {
		           throw new AlgorithmOperationException("Invalid encryption key specification");*/
		    }
		    catch (InvalidAlgorithmParameterException ape) {
		      log.error("Blowfish initialization failed", ape);
		      throw new AlgorithmOperationException("Invalid algorithm parameter");
		    }
		  }

		  /**
		   *
		   *
		   * @param data
		   * @param offset
		   * @param len
		   *
		   * @return
		   *
		   * @throws AlgorithmOperationException
		   */
		  public byte[] transform(byte[] data, int offset, int len) throws
		      AlgorithmOperationException {
		    return cipher.update(data, offset, len);
		  }
}

/*
 *  Browser Certificate Inteface
 *
 *  Copyright (C) 2005-7 CCLRC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */

package uk.ac.rl.esc.browser;
import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.common.CoGProperties;
import org.globus.gsi.*;
import org.globus.gsi.GSIConstants.CertificateType;
import org.globus.gsi.gssapi.*;
import org.globus.gsi.util.CertificateLoadUtil;
import org.ietf.jgss.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.cert.*;
import java.security.PrivateKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.Provider;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.*;
import java.security.Security;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import javax.security.auth.callback.*;
import javax.swing.JOptionPane;

import java.util.*;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.bouncycastle.util.encoders.Base64;
import org.globus.util.PEMUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import org.bouncycastle.asn1.*;

import sun.security.pkcs11.SunPKCS11;

/**
 * Provides a simple to use interface to a selection of Browser certificate stores.  These include
 *   <ul><li><strong>Windows</strong>(Really need Windows 2000+)
 *      <ul><li>Internet Explorer</li>
 *          <li>Firefox</li>
 *          <li>Mozilla</li>
 *      </ul></li>
 *    <li><strong>Linux/UNIX</strong>
 *      <ul><li>Firefox</li>
 *          <li>Mozilla</li>
 *      </ul></li>
 *  </ul>
 * <p>In particular you need to have Java 1.5+ to be able to use this API (because we need the Sun 
 * PKCS11 module).  Mac support for Firefox/Mozilla could easily be added if the JDK for Mac
 * included this module (maybe in JDK 1.6?).  Also support for other PKCS11 modules could be
 * added to support smart cards, etc.  but this has not been done because they are not browsers.</p>
 *
 * <p>There is some strange issues with .so/.dlls which means that the Browser class will only let
 * you choose which browser once in the life of a JVM (because the PKCS11 module/init settings
 * cannot be changed).  In applets this means that a new browser executable is needed, not just a
 * reload of the page. This is because the JVM persists for the life of the brwoser.</p>
 *
 * <p>The following gives an overview of how you would use this class:</p>
<pre>
   private static class PasswordPrompt implements Browser.PasswordCallback {
	public char [] prompt(String promptString) {
	    return passwordDialogBox(promptString);
	}
    }

    private static GSSCredential chooseCert() throws IOException, IllegalArgumentException, IllegalStateException, GeneralSecurityException, GlobusCredentialException, GSSException {
	String profile = Browser.getCurrentBrowser();
	if(profile==null) {
	    String profiles[] = Browser.getBrowserList();
	    if(profiles==null) return null; // there are no profiles!
	    String choice = chooseDialog("Please choose browser to use:", profiles); //user chooses profile.
	    Browser.setBrowser(choice); 
	}
	String dnList[]=null;
	try {
	    dnList = Browser.getDNlist(new PasswordPrompt());
	} catch(javax.security.auth.login.FailedLoginException e) { 
	    wrongPasswordDialog();
	    return null;
	} 
	if(dnList==null) return null;  // No valid DNs found

	String dnChoice = chooseDialog("Please choose certificate to use:", dnList);
	return Browser.getGridProxy(dnChoice, type, lifetime, strength);
    } 
</pre>

 * @author David Spence (d.r.spence@rl.ac.uk)
 */

public class Browser {

	private Browser() {}

	private static abstract class CertInfo {
		String profileDescriptor;

		abstract X509Certificate loadCert(String DN) throws IOException, GeneralSecurityException;
		abstract GSSCredential loadProxy(String DN, int proxyType, int lifetimeHours, int proxyStrength) throws IOException, GeneralSecurityException, GlobusCredentialException, GSSException;
		abstract byte[] loadPKCS12(String DN, char password[]) throws IOException, GeneralSecurityException;
		abstract Set getDNs(PasswordCallback pass) throws IOException, GeneralSecurityException; 
		abstract void importPKCS12(byte data[], char password[]) throws IOException, GeneralSecurityException;
	}
	
	private static class MacCertInfo extends CertInfo{
		String profileFile=null;
		protected static Provider p = null;
		protected static KeyStore keyStore = null;		
		private static HashMap hashmapKeys = new HashMap();
		
		MacCertInfo() {
			profileDescriptor="Mac Keychain Access: Safari/Chrome (default)";		
		}
		
		GSSCredential loadProxy(String DN, int proxyType, int lifetimeHours, int proxyStrength) throws IOException, GeneralSecurityException, GlobusCredentialException, GSSException { 
			GSSCredential gsscredential = null;
			CoGProperties cogproperties = CoGProperties.getDefault();
			
			HashMap dnmap = readDNs(null);
			String alias = (String) dnmap.get(DN);
			X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
			PrivateKey key = (PrivateKey) hashmapKeys.get(alias);
		
			BouncyCastleCertProcessingFactory factory =
				BouncyCastleCertProcessingFactory.getDefault();

			X509Credential proxy =
                factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
                                         key,
                                         2048,
                                         lifetimeHours * 3600,
                                         CertificateType.get(proxyType));

			gsscredential = new GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_ONLY);
		
			return gsscredential;
		}
		
		X509Certificate loadCert(String DN) throws IOException, GeneralSecurityException {
			HashMap dnmap = readDNs(null);
			String alias = (String) dnmap.get(DN);
			X509Certificate c = (X509Certificate)keyStore.getCertificate(alias);
			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			c = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(c.getEncoded()));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertificateLoadUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray()));
			return cert;
		}
		
		void importPKCS12(byte blob[], char password[]) throws IOException, GeneralSecurityException { 
			
			
			/*HashMap dnmap = readDNs(null); // check that the store is open
			KeyStore store = KeyStore.getInstance("PKCS12", "BC");
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			store.load(bais, password);
			Enumeration e = store.aliases();
			if(!e.hasMoreElements()) {
				throw new GeneralSecurityException("Could not find any certificates/keys in PKCS#12 blob.");
			}

			String alias = (String)e.nextElement();
			java.security.cert.Certificate cert = store.getCertificate(alias);
			Key key = store.getKey(alias,password);

			if(!(cert instanceof X509Certificate)) {
				throw new GeneralSecurityException("Could not find certificate in PKCS#12 blob.");
			}
			if(!(key instanceof PrivateKey)) {
				throw new GeneralSecurityException("Could not find key in PKCS#12 blob.");
			}
			keyStore.setKeyEntry("user", key, null, new Certificate[] {cert});
			bais.close();
			*/
			
			CoGProperties cogproperties = CoGProperties.getDefault();
			
			KeyStore keystore = KeyStore.getInstance("KeychainStore", "Apple");
			keystore.load(null, null);
			Enumeration<String> e = keystore.aliases();
			Key key = null;
			java.security.cert.Certificate cert = null;

			// filter the store's contents and select only valid user
			// x509 certs that have an associated key !                     
			List<String> allValidX509CertAliases = new java.util.ArrayList<String>(0);
			while (e.hasMoreElements()){
				String alias = (String)e.nextElement();
				// get the associated key
				key = keystore.getKey(alias, password);
				if (key != null && (key instanceof PrivateKey)) {
					allValidX509CertAliases.add(alias);
				}
			}
			// use the first alias as the defauilt.
			String selected = allValidX509CertAliases.get(0);
			
			// ok, now have the selected x509 cert alias that also
			// has an accompanying private key.
			cert = keyStore.getCertificate(selected);
			key = keystore.getKey(selected, password);
	
			
			if(!(cert instanceof X509Certificate)) {
				throw new GeneralSecurityException("Could not find certificate in PKCS#12 blob.");
			}
			if(!(key instanceof PrivateKey)) {
				throw new GeneralSecurityException("Could not find key in PKCS#12 blob.");
			}
			//Convert to bouncycastle certificate
			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
			
			keyStore.setKeyEntry("user", key, null, new Certificate[] {cert});
		}
		
		byte[] loadPKCS12(String DN, char password[]) throws IOException, GeneralSecurityException {
			HashMap dnmap = readDNs(null);
			String alias = (String) dnmap.get(DN);
			X509Certificate c = (X509Certificate)keyStore.getCertificate(alias);
			Key key = keyStore.getKey(alias, password);		
			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			c = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(c.getEncoded()));
			
			BouncyCastleCertProcessingFactoryProvider factory =
				BouncyCastleCertProcessingFactoryProvider.getDefault();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray())); 

			KeyStore outputStore = KeyStore.getInstance("PKCS12", "BC");

			outputStore.load(null, null);

			outputStore.setKeyEntry("user", key, password, new X509Certificate[] {cert});
			baos = new ByteArrayOutputStream();
			outputStore.store(baos, password);
			return baos.toByteArray();
		}
		
		protected MacCertInfo(String profileDescriptor) {
			this.profileDescriptor =  profileDescriptor;
		}


		private HashMap readDNs(PasswordCallback pass) throws IOException, GeneralSecurityException {		
			if(keyStore==null) {				
				if(pass==null) throw new IllegalStateException("MacCertInfo.readDNs must be called with a PasswordCallback first time.");
				try {
					char [] password = "a".toCharArray(); //pass.prompt("Mac Keychain access (Safari/Chrome) User Keystore Passphrase");
					if(password==null) throw new GeneralSecurityException("No Mac Keychain access (Safari/Chrome) User Keystore Passphrase returned.");
														
					Provider pr = Security.getProvider("Apple");// (Provider)con.newInstance(new Object[] {fileStr});
					KeyStore ks = KeyStore.getInstance("KeychainStore", pr);
					ks.load(null, null);
					
					Enumeration<String> e = ks.aliases();
					List<String> allValidX509CertAliases = new java.util.ArrayList<String>(0);
					while (e.hasMoreElements()){
						String alias = (String)e.nextElement();
						// get the associated key
						Key tempKey = ks.getKey(alias, password);
						if (tempKey != null && (tempKey instanceof PrivateKey)) {	
							hashmapKeys.put(alias, tempKey);
							keyStore = ks;					
						}
					}
					p=pr;
					
					
				} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				/*Date now=new Date();
				KeyStore keystore = KeyStore.getInstance("KeychainStore", "Apple");
				keystore.load(null, null);
				Enumeration<String> e = keystore.aliases();
				Key key = null;
				java.security.cert.Certificate cert = null;

				// filter the store's contents and select only valid user
				// x509 certs that have an associated key !                     
				List<String> allValidX509CertAliases = new java.util.ArrayList<String>(0);
				while (e.hasMoreElements()){
					String alias = (String)e.nextElement();
					// get the associated key
					key = keystore.getKey(alias, password);
					if (key != null && (key instanceof PrivateKey)) {						
						keyStore = keystore.;					
					}
				}*/
				
			
			}
			HashMap hm = new HashMap();
			HashMap ll = new HashMap();
			Date now=new Date();
			for (Enumeration e = keyStore.aliases(); e.hasMoreElements();) {
				String a = (String) e.nextElement();
				Certificate c = keyStore.getCertificate(a);	
				Key key = (Key) hashmapKeys.get(a);
				if (key != null && (key instanceof PrivateKey)) {
					if(c instanceof X509Certificate) {
						X509Certificate x509 = (X509Certificate) c;
						if(x509.getNotBefore().before(now) && x509.getNotAfter().after(now)) {
							hm.put(x509.getSubjectX500Principal().getName(), a);
						}
					}
				}
			} 
			
			return hm;
		}

		Set getDNs(PasswordCallback pass) throws IOException, GeneralSecurityException {
			return readDNs(pass).keySet();
		}

	
	}
	private static class IECertInfo extends CertInfo {
		IECertInfo() {
			profileDescriptor="Internet Explorer";
		}

		X509Certificate loadCert(String DN) throws IOException, GeneralSecurityException {
			X509Certificate c = IECertificateInterface.getX509Certificate(DN);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray()));
			return cert;
		}

		GSSCredential loadProxy(String DN, int proxyType, int lifetimeHours, int proxyStrength) throws IOException, GeneralSecurityException, GlobusCredentialException, GSSException {
			GSSCredential gsscredential = null;
			CoGProperties cogproperties = CoGProperties.getDefault();
			X509Certificate c = IECertificateInterface.getX509Certificate(DN);
			PrivateKey k = (PrivateKey) IECertificateInterface.getKey(DN);
			Provider p = IECertificateInterface.getProvider();
			BouncyCastleCertProcessingFactoryProvider factory =
				BouncyCastleCertProcessingFactoryProvider.getDefault();
			Security.addProvider(p);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray()));
			X509Credential x509credential = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
					(PrivateKey)k,
					proxyStrength, 
					lifetimeHours * 3600,
					proxyType,
					(X509ExtensionSet)null, p);
			if(x509credential!=null) {
				x509credential.verify();
				gsscredential = new GlobusGSSCredentialImpl(x509credential, GSSCredential.INITIATE_ONLY);
				return gsscredential ;
			}

			return gsscredential;
		}

		byte[] loadPKCS12(String DN, char password[]) throws IOException, GeneralSecurityException { 
			return IECertificateInterface.getPKCS12(DN, password);
		}

		void importPKCS12(byte blob[], char password[]) throws IOException, GeneralSecurityException { 
			IECertificateInterface.importPKCS12(blob, password);
		}

		Set getDNs(PasswordCallback pass) throws IOException, GeneralSecurityException {
			String dns[] = IECertificateInterface.listDNS();
			return new TreeSet(Arrays.asList(dns));
		}
	}

	abstract private static class MozillaCertInfo extends CertInfo {
		String profileFile=null;
		protected static KeyStore keyStore = null;
		protected static Provider p = null;
		protected static boolean haveUnlockedMozilla=false;
		static File configDir;
		private static byte keyDB[]=null;
		private static byte HP[] = null; 
		private static boolean gotKeyDB = false;
		private static String keyDBerror = null;

		X509Certificate loadCert(String DN) throws IOException, GeneralSecurityException {
			HashMap dnmap = readDNs(null);
			String alias = (String) dnmap.get(DN);
			X509Certificate c = (X509Certificate)keyStore.getCertificate(alias);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray()));
			return cert;
		}

		void importPKCS12(byte blob[], char password[]) throws IOException, GeneralSecurityException { 
			HashMap dnmap = readDNs(null); // check that the store is open
			KeyStore store = KeyStore.getInstance("PKCS12", "BC");
			ByteArrayInputStream bais = new ByteArrayInputStream(blob);
			store.load(bais, password);
			Enumeration e = store.aliases();
			if(!e.hasMoreElements()) {
				throw new GeneralSecurityException("Could not find any certificates/keys in PKCS#12 blob.");
			}

			String alias = (String)e.nextElement();
			java.security.cert.Certificate cert = store.getCertificate(alias);
			Key key = store.getKey(alias,password);

			if(!(cert instanceof X509Certificate)) {
				throw new GeneralSecurityException("Could not find certificate in PKCS#12 blob.");
			}
			if(!(key instanceof PrivateKey)) {
				throw new GeneralSecurityException("Could not find key in PKCS#12 blob.");
			}
			keyStore.setKeyEntry("user", key, null, new Certificate[] {cert});
			bais.close();
		}

		GSSCredential loadProxy(String DN, int proxyType, int lifetimeHours, int proxyStrength) throws IOException, GeneralSecurityException, GlobusCredentialException, GSSException { 
			GSSCredential gsscredential = null;
			CoGProperties cogproperties = CoGProperties.getDefault();
			HashMap dnmap = readDNs(null);
			String alias = (String) dnmap.get(DN);
			X509Certificate c = (X509Certificate)keyStore.getCertificate(alias);
			PrivateKey k = (PrivateKey) keyStore.getKey(alias, null);
			BouncyCastleCertProcessingFactoryProvider factory =
				BouncyCastleCertProcessingFactoryProvider.getDefault();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray()));
			X509Credential x509credential = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
					(PrivateKey)k,
					proxyStrength, 
					lifetimeHours * 3600,
					proxyType,
					(X509ExtensionSet)null, p);
			if(x509credential!=null) {
				x509credential.verify();
				gsscredential = new GlobusGSSCredentialImpl(x509credential, GSSCredential.INITIATE_ONLY);
				return gsscredential ;
			}

			return gsscredential;
		}

		byte[] loadPKCS12(String DN, char password[]) throws IOException, GeneralSecurityException {
			HashMap dnmap = readDNs(null);
			String alias = (String) dnmap.get(DN);
			X509Certificate c = (X509Certificate)keyStore.getCertificate(alias);

			PrivateKey key = null;

			if(gotKeyDB) {
				reloadKeyDB(null);
				int index = find(keyDB, alias);
				int begin;
				int keystart = index+alias.length()+1;
				boolean first = true;
				if(index==-1) {
					begin = find2(keyDB, 0);
					index = begin+3+16;
					keystart = index+keyDB[begin+2];
					if(begin==-1) {
						throw new GeneralSecurityException("Key not found in key database.");
					}
					first = false;
				} else {
					begin = index - 16 -3;
				}

				while(begin!=-1) {
					try {
						if(keyDB[begin]!=3 || keyDB[begin+1]!=16) throw new GeneralSecurityException("Bad Key format in key database.");

						byte ES[] = new byte[16];
						System.arraycopy(keyDB, begin+3, ES, 0, 16);

						byte PES[] = new byte[20];
						for(int i=0;i<PES.length;i++) {
							if(i<ES.length) PES[i]=ES[i]; else PES[i]=0;
						}
						MessageDigest md = MessageDigest.getInstance("SHA");
						md.update(HP);
						md.update(ES);
						byte CHP[] = md.digest();
						Mac mac = Mac.getInstance("HmacSHA1");
						mac.init(new SecretKeySpec(CHP, "HmacSHA1"));
						mac.update(PES);
						mac.update(ES);
						byte k1[] = mac.doFinal();
						mac.reset();
						mac.update(PES);
						byte kt[] = mac.doFinal();
						mac.reset();
						mac.update(kt);
						mac.update(ES);
						byte k2[] = mac.doFinal();

						byte desKey[] = new byte[24];
						for(int i=0;i<24;i++) {
							if(i<20) desKey[i]=k1[i]; else desKey[i]=k2[i-20];
						}
						byte IV[] = new byte[8];
						for(int i=0;i<8;i++) {
							IV[i]=k2[i+12];
						}

						int byte1 = keyDB[keystart+1];
						long length = 0;
						int header = 2;
						if(byte1>0) {
							length = byte1;
						} else {
							int octets = 128 + byte1;

							for(int i=0;i<octets;i++) {
								header++;
								length*=256;
								long b = keyDB[keystart+2+i];
								if(b<0) b+=256;
								length+=b;
							}
						}
						if((length+header)>(1024*16) || (length+header)<0) throw new GeneralSecurityException("Not a key.");
						byte encrypted[] = new byte[(int)length+header];
						System.arraycopy(keyDB, keystart, encrypted, 0, (int)length+header);

						ByteArrayInputStream bais = new ByteArrayInputStream(encrypted);

						ASN1InputStream asn1is = new ASN1InputStream(bais);
						DERSequence ders = (DERSequence) asn1is.readObject();
						DEREncodable der = ders.getObjectAt(1);
						DEROctetString dero = (DEROctetString) der;
						encrypted = dero.getOctets();
						Cipher ci = Cipher.getInstance("DESede/CBC/PKCS5Padding");
						ci.init(ci.DECRYPT_MODE, new SecretKeySpec(desKey, "DESede"), new IvParameterSpec(IV));

						byte data[] = ci.doFinal(encrypted);

						bais = new ByteArrayInputStream(data);

						asn1is = new ASN1InputStream(bais);
						ders = (DERSequence) asn1is.readObject();
						der = ders.getObjectAt(2);
						dero = (DEROctetString) der;
						data = dero.getOctets();
						key = new org.globus.gsi.bc.BouncyCastleOpenSSLKey("RSA", data).getPrivateKey();
						Signature sig = Signature.getInstance("SHA1withRSA");
						Signature sig1 = Signature.getInstance("SHA1withRSA");
						sig.initSign(key);
						sig.update(new byte[] {1, 2, 3, 4});
						byte b[] = sig.sign();
						sig1.initVerify(c);
						sig1.update(new byte[] {1, 2, 3, 4});
						if(!sig1.verify(b)) throw new GeneralSecurityException("Key does not match");
						break;
					} catch(GeneralSecurityException e) {
						if(first==true) begin = -1;
						begin = find2(keyDB, begin+1);
						index = begin+3+16;
						keystart = index+keyDB[begin+2];
						if(begin==-1) {
							throw e;
						}
					} catch(ClassCastException e) {
						if(first==true) begin = -1;
						begin = find2(keyDB, begin+1);
						index = begin+3+16;
						keystart = index+keyDB[begin+2];
						if(begin==-1) {
							throw e;
						}
					}
				}
			} else {

				throw new GeneralSecurityException("Cannot access key database, problem: "+keyDBerror);
			}


			BouncyCastleCertProcessingFactoryProvider factory =
				BouncyCastleCertProcessingFactoryProvider.getDefault();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c.getEncoded()),"-----END CERTIFICATE-----");
			X509Certificate cert = CertUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray())); 

			KeyStore outputStore = KeyStore.getInstance("PKCS12", "BC");

			outputStore.load(null, null);

			outputStore.setKeyEntry("user", key, password, new X509Certificate[] {cert});
			baos = new ByteArrayOutputStream();
			outputStore.store(baos, password);
			return baos.toByteArray();
		}

		private static int find(byte array[], String tofind) {
			byte key[] = tofind.getBytes();
			/* KMP implementation */
			int next[] = new int[key.length+2];
			int j=-1;
			next[0] = -1;
			for(int i=0; i < key.length; i++) {
				while(j > -1 && key[i] != key[j]) j = next[j];
				j++;
				if((i+1)!=key.length && key[i+1] == key[j]) next[i+1] = next[j]; else next[i+1] = j;
			}
			j = 0;
			for(int i=0; i < array.length; i++) {
				while (j > -1 && key[j] != array[i]) j = next[j];
				j++;
				if (j >= key.length) {
					return i+1 - j;
				}
			}
			return -1;
		}

		private void reloadKeyDB(char[] password) {
			try {
				File f = new File(configDir, "key3.db");
				if(f.exists() && f.isFile()) {

					long length = f.length();
					keyDB = new byte[(int)length];
					FileInputStream fis = new FileInputStream(f);
					fis.read(keyDB);
					fis.close();

					String PASSWORD_CHECK = "password-check";
					String VERSION = "Version";
					String GLOBAL_SALT = "global-salt";

					int ind = find(keyDB, VERSION);
					if(ind==-1 || keyDB[ind-1]!=3) {
						keyDBerror = "Wrong Version: "+ind; 
					} else {
						if(password!=null) {
							byte GS[] = new byte[16];
							ind = find(keyDB, GLOBAL_SALT);
							if(ind==-1) {
								keyDBerror = "No Global salt entry in key DB.";
							} else {
								System.arraycopy(keyDB, ind-16, GS, 0, 16);
								byte p[] = (new String(password)).getBytes();

								MessageDigest md = MessageDigest.getInstance("SHA");
								md.update(GS);
								md.update(p);
								HP = md.digest();

								byte ES[] = new byte[16];
								ind = find(keyDB, PASSWORD_CHECK);
								int bpwd = ind-48;
								if(ind==-1 || keyDB[bpwd]!=3 || keyDB[bpwd+1]!=16) {
									keyDBerror = "Bad password-check entry in key DB";
								} else {
									System.arraycopy(keyDB, bpwd+3, ES, 0, 16);

									byte PES[] = new byte[20];
									for(int i=0;i<PES.length;i++) {
										if(i<ES.length) PES[i]=ES[i]; else PES[i]=0;
									}
									md.reset();
									md.update(HP);
									md.update(ES);
									byte CHP[] = md.digest();
									Mac mac = Mac.getInstance("HmacSHA1");
									mac.init(new SecretKeySpec(CHP, "HmacSHA1"));
									mac.update(PES);
									mac.update(ES);
									byte k1[] = mac.doFinal();
									mac.reset();
									mac.update(PES);
									byte kt[] = mac.doFinal();
									mac.reset();
									mac.update(kt);
									mac.update(ES);
									byte k2[] = mac.doFinal();

									byte desKey[] = new byte[24];
									for(int i=0;i<24;i++) {
										if(i<20) desKey[i]=k1[i]; else desKey[i]=k2[i-20];
									}
									byte IV[] = new byte[8];
									for(int i=0;i<8;i++) {
										IV[i]=k2[i+12];
									}

									byte encrypted[] = new byte[16];
									System.arraycopy(keyDB, ind-16, encrypted, 0, 16);

									Cipher ci = Cipher.getInstance("DESede/CBC/PKCS5Padding");
									ci.init(ci.DECRYPT_MODE, new SecretKeySpec(desKey, "DESede"), new IvParameterSpec(IV));

									byte data[] = ci.doFinal(encrypted);
									if((new String(data)).equals(PASSWORD_CHECK)) {		
										keyDBerror = "OK";
									} else {
										keyDBerror = "Local password check failed.";
									}
									gotKeyDB = true;
								}
							}
						}
					}
				}

			} catch(Exception e) {
				keyDBerror = "Caught an Exception: "+e.getMessage();
			}

		}

		private static int find2(byte array[], int start) {
			byte key[] = new byte[] {3, 16};
			/* KMP implementation */
			int next[] = new int[key.length+2];
			int j=-1;
			next[0] = -1;
			for(int i=0; i < key.length; i++) {
				while(j > -1 && key[i] != key[j]) j = next[j];
				j++;
				if((i+1)!=key.length && key[i+1] == key[j]) next[i+1] = next[j]; else next[i+1] = j;
			}
			j = 0;
			for(int i=start; i < array.length; i++) {
				while (j > -1 && key[j] != array[i]) j = next[j];
				j++;
				if (j >= key.length) {
					return i+1 - j;
				}
			}
			return -1;
		}

		private HashMap readDNs(PasswordCallback pass) throws IOException, GeneralSecurityException {
		
			if(keyStore==null) {
				if(pass==null) throw new IllegalStateException("MozillaCertInfo.readDNs must be called with a PasswordCallback first time.");
				try {
					char password[] = pass.prompt("Firefox/Mozilla Master Certificate Store Passphrase");
					if(password==null) throw new GeneralSecurityException("No Firefox/Mozilla Master Certificate Store Passphrase returned.");
									
					File confFile=File.createTempFile("ssh-term", "pkcs11.cfg");

					PrintWriter pw = new PrintWriter(new FileWriter(confFile));					
					pw.println(profileFile);					
					pw.close();
					String fileStr=confFile.getAbsolutePath();
										
					Class c = ClassLoader.getSystemClassLoader().loadClass("sun.security.pkcs11.SunPKCS11");
					Constructor con = c.getConstructor(new Class[] {String.class});
					Provider pr = (Provider)con.newInstance(new Object[] {fileStr});
					KeyStore ks = KeyStore.getInstance("PKCS11", pr);
					ks.load(null, password);
					Security.addProvider(pr);
					
					/**The new NSS way to define if ever required**/
					//profileFile = "name = NSS\nnssLibraryDirectory = /usr/lib64\nnssSecmodDirectory = "+System.getProperty("user.home")+"/.mozilla/firefox/75ym5da6.default\nnssDbMode = readOnly\nnssModule = keystore\nattributes = compatibility";
					/*Provider nss = new SunPKCS11(fileStr);
					Security.insertProviderAt(nss, 1);
					KeyStore ks = KeyStore.getInstance("PKCS11", nss);
					ks.load(null, password);
					Provider pr=nss;*/
					
					p=pr;haveUnlockedMozilla=true;keyStore=ks;
					confFile.delete();
					reloadKeyDB(password);
				} catch(ClassNotFoundException e) {
					throw new GeneralSecurityException("Error (ClassNotFoundException) accessing Firefox/Mozilla certificate store; this probably means that you are not running Java SDK version>=1.5 which is needed to access Mozilla/Firefox Certificates.", e);
				} catch(NoSuchMethodException e) {
					throw new GeneralSecurityException("Error (NoSuchMethodException) accessing Firefox/Mozilla certificate store; this probably means that you are not running Java SDK version>=1.5 which is needed to access Mozilla/Firefox Certificates.", e);
				} catch(InstantiationException e) {
					throw new GeneralSecurityException("Error (InstantiationException) accessing Firefox/Mozilla certificate store; this probably means that you are not running Java SDK version>=1.5 which is needed to access Mozilla/Firefox Certificates.", e);
				} catch(IllegalAccessException e) {
					throw new GeneralSecurityException("Error (IllegalAccessException) accessing Firefox/Mozilla certificate store; this probably means that you are not running Java SDK version>=1.5 which is needed to access Mozilla/Firefox Certificates.", e);
				} catch(InvocationTargetException e) {
					String errorMsg = e.toString();
					System.out.println(errorMsg);
					throw new GeneralSecurityException("Error (InvocationTargetException) accessing Firefox/Mozilla certificate store; this probably means that you are not running Java SDK version>=1.5 which is needed to access Mozilla/Firefox Certificates.", e);
				}
			}
			HashMap hm = new HashMap();
			HashMap ll = new HashMap();
			Date now=new Date();
			for (Enumeration e = keyStore.aliases(); e.hasMoreElements();) {
				String a = (String) e.nextElement();
				Certificate c = keyStore.getCertificate(a);
				if(c instanceof X509Certificate) {
					X509Certificate x509 = (X509Certificate) c;
					if(x509.getNotBefore().before(now) && x509.getNotAfter().after(now)) {
						hm.put(x509.getSubjectX500Principal().getName(), a);
					}
				}
			} 
			return hm;
		}

		Set getDNs(PasswordCallback pass) throws IOException, GeneralSecurityException {
			return readDNs(pass).keySet();
		}

		protected MozillaCertInfo(String profileDescriptor) {
			this.profileDescriptor =  profileDescriptor;
		}

	}

	private static int idNumPKCS11=0;

	private static class MozillaLinuxCertInfo extends MozillaCertInfo {

		public MozillaLinuxCertInfo(File library, File config, String profileDescriptor) throws IOException {
			super(profileDescriptor);
			profileFile=createMozillaConfigLinux(config, library);
		}

		private static String createMozillaConfigLinux(File configDir, File libDir) throws IOException {
			MozillaCertInfo.configDir = configDir;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println("name=NSSSofToken"+(idNumPKCS11++));
			pw.println("library="+libDir.getAbsolutePath());
			pw.println("slot=2"); 
			pw.println("nssArgs = \"configdir='"+configDir.getAbsolutePath()+"' certPrefix='' keyPrefix='' secmod='secmod.db'\"");
			pw.close();
			return sw.toString();
		}
	}

	private static class MozillaWindowsCertInfo extends MozillaCertInfo {

		protected MozillaWindowsCertInfo(File library, File config, String profileDescriptor) throws IOException {
			super(profileDescriptor);
			profileFile=createMozillaConfigWindows(config, library);
		}

		private static boolean loaded = false;
		public static void init(File libDir) {
			if(!loaded) try {
				System.load(libDir.getParent()+"\\nspr4.dll");
				System.load(libDir.getParent()+"\\plds4.dll");
				System.load(libDir.getParent()+"\\plc4.dll");
				loaded=true;
			} catch(Throwable t) {
				t.printStackTrace();//may still work!
			}
		}


		private static String createMozillaConfigWindows(File configDir, File libDir) throws IOException {
			MozillaCertInfo.configDir = configDir;
			System.gc();
			init(libDir);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println("name=NSSSofToken"+(idNumPKCS11++));
			pw.println("library="+libDir.getAbsolutePath().replace("\\","/")+"");
			//pw.println("library=softokn3.dll");
			pw.println("slot=2"); 
			pw.println("nssArgs = \"configdir='"+configDir.getAbsolutePath().replace("\\","//")+"' certPrefix='' keyPrefix='' secmod='secmod.db'\"");
			pw.close();
			return sw.toString();
		}

	}

	private static CertInfo theProfile = null;

	private static CertInfo[] getProfiles() throws IOException, IllegalStateException {
		if(theProfile!=null) throw new IllegalStateException("You cannot change the browser profile once you have set it (within the life of the JVM).");
		boolean windows = System.getProperty("os.name").startsWith("Windows");
		boolean linux = System.getProperty("os.name").startsWith("Linux");
		boolean mac = System.getProperty("os.name").startsWith("Mac OS");
		String homedir = System.getProperty("user.home");
		LinkedList profiles = new LinkedList();
		if (mac) {
			profiles.addFirst(new MacCertInfo());
		}
		if(linux) {
			// try firefox and mozilla
			File libraryFirefox=null;
			File libraryMozilla=null;
			String firefoxPath=exec("which firefox");
			String mozillaPath=exec("which mozilla");
			//Cerlane to path 32 or 64 bit firefox/mozilla
			String libBitwise = "lib";
			if(mozillaPath!=null) {
				//Cerlane to path 32 or 64 bit firefox/mozilla
				String realPath=exec("readlink /usr/bin/firefox");				
				if (realPath.contains("lib64")){
					libBitwise = "lib64";
				}
				
				File mozillaBin=(new File(mozillaPath));
				if(mozillaBin!=null) {
					mozillaBin = mozillaBin.getParentFile();
					if(mozillaBin!=null && mozillaBin.exists() && mozillaBin.getName().equals("bin")) {
						
						//File mozillaLib = new File(mozillaBin.getParent(),"lib");
						//Cerlane to path 32 or 64 bit firefox/mozilla
						File mozillaLib = new File(mozillaBin.getParent(),libBitwise);
						if(mozillaLib!=null && mozillaLib.exists()) {
							//Cerlane: additional checks for Ubuntu where this file is in nss folder
							File tmp = new File(mozillaLib, "libsoftokn3.so");
							if(tmp!=null && tmp.exists()) 
								libraryMozilla=tmp;
							//Cerlane: adds this because e.g. on Ubuntu file is at /usr/lib/nss
							else{
								mozillaLib = new File(mozillaBin.getParent(),libBitwise + "/nss");
								tmp = new File(mozillaLib, "libsoftokn3.so");
								if(tmp!=null && tmp.exists()) 
									libraryMozilla=tmp;
							}
							
						}
					}
				}
			}
			FilenameFilter fnf = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("firefox");
				}
			};
			if(firefoxPath!=null) {
				//Cerlane to path 32 or 64 bit firefox/mozilla
				String realPath=exec("readlink /usr/bin/firefox");				
				if (realPath.contains("lib64")){
					libBitwise = "lib64";
				}
				File firefoxBin=(new File(firefoxPath));
				if(firefoxBin!=null) {
					firefoxBin = firefoxBin.getParentFile();
					if(firefoxBin!=null && firefoxBin.exists() && firefoxBin.getName().equals("bin")) {
						//Cerlane to path 32 or 64 bit firefox/mozilla
						File firefoxLib = new File(firefoxBin.getParent(),libBitwise);
						//File firefoxLib = new File(firefoxBin.getParent(),"lib/nss");
						if(firefoxLib!=null && firefoxLib.exists()) {
							File tmp = new File(firefoxLib, "libsoftokn3.so");
							if(tmp!=null && tmp.exists()) 
								libraryFirefox=tmp;
							//Cerlane: adds this because e.g. on Ubuntu file is at /usr/lib/nss
							else{
								firefoxLib = new File(firefoxBin.getParent(),libBitwise + "/nss");
								tmp = new File(firefoxLib, "libsoftokn3.so");
								if(tmp!=null && tmp.exists()) 
									libraryFirefox=tmp;
							}
							File tmps[] = firefoxLib.listFiles(fnf);
							if(tmps!=null) {
								for(int i=0;i<tmps.length;i++) {
									File tmp2 = new File(tmps[i], "libsoftokn3.so");
									if(tmp2!=null && tmp2.exists()) libraryFirefox=tmp2;
								}
							}
						}
					}
				}
			}
			StringBuffer passwordstringbuffer = null;

			if(libraryFirefox==null && libraryMozilla==null) return null;
			if(libraryFirefox==null) libraryFirefox=libraryMozilla; /// try to keep respective versions, else may need to use each others
			if(libraryMozilla==null) libraryMozilla=libraryFirefox;
			File mozillaConfigDir = new File(homedir, ".mozilla");
			if(mozillaConfigDir ==null || !mozillaConfigDir.exists() || !mozillaConfigDir.isDirectory()) return null;
			File[] files = mozillaConfigDir.listFiles();
			if(files!=null) {
				File firefoxConfigDir = null;
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						String name = files[i].getName();
						if(name.equals("firefox")) {
							firefoxConfigDir = files[i];
						} else if(name.equals("plugins") || name.equals("searchplugins")) {
						} else {
							File inner[] = files[i].listFiles();
							if(inner!=null) {
								if(inner.length==1 && libraryMozilla!=null) {
									try {
										if(name.equals("default")) {
											profiles.addFirst(new MozillaLinuxCertInfo(libraryMozilla, inner[0], "Mozilla ("+name+")"));
										} else {
											profiles.add(new MozillaLinuxCertInfo(libraryMozilla, inner[0], "Mozilla ("+name+")"));
										}
									} catch(IOException e) {
										log.debug("Could not access keystore in profile: Firefox: "+name+" : "+e);
									}
								}
							}
						}
					}
				}

				if(firefoxConfigDir.exists() && firefoxConfigDir.isDirectory()) {
					File firefoxConfigFile = new File(firefoxConfigDir, "profiles.ini");
					if(firefoxConfigFile.exists() && firefoxConfigFile.isFile()) {
						if(libraryFirefox!=null) processINI(profiles, firefoxConfigFile, firefoxConfigDir, libraryFirefox, false);
					}
				}
				// files = firefoxConfigDir.listFiles();
				// 		boolean addedDefault = false;
				// 		if(files!=null) {
				// 		    for(int i=0; i<files.length; i++) {
				// 			if(files[i].isDirectory()) {
				// 			    String name = files[i].getName();
				// 			    if(name.charAt(8)=='.' && libraryFirefox!=null) {
				// 				String realname = name.substring(9);
				// 				try {
				// 				    if(realname.equals("default")|| !addedDefault) {
				// 					profiles.addFirst(new MozillaLinuxCertInfo(libraryFirefox, files[i], "Firefox ("+realname+")"));
				// 					addedDefault=true;
				// 				    } else {
				// 					profiles.add(1, new MozillaLinuxCertInfo(libraryFirefox, files[i], "Firefox ("+realname+")"));
				// 				    }
				// 				} catch(IOException e) {
				// 				    log.debug("Could not access keystore in profile: Firefox: "+realname+" : "+e);
				// 				} 
				// 			    }
				// 			}
				// 		    }
				// 		}
			}

		} 
		if(windows) {
			// try firefox and IE
			String firefoxGetPathCommand = "REG QUERY \"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\firefox.exe\" /v Path";
			String mozillaGetPathCommand = "REG QUERY \"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\Mozilla.exe\" /v Path";
			String output = exec(firefoxGetPathCommand);
			int ind =output.indexOf("REG_SZ");
			File tmp = null;
			if(ind>=0) {
				output = output.substring(ind+6);
				output = output.split("\n")[0];
				tmp = new File(output.trim(), "softokn3.dll");
			} else {
				tmp = new File("C:\\Program Files\\Mozilla Firefox\\softokn3.dll");
			}
			File libraryFirefox=null;
			if(tmp!=null && tmp.exists()) libraryFirefox=tmp;

			output = exec(mozillaGetPathCommand);
			ind =output.indexOf("REG_SZ");
			tmp = null;
			if(ind>=0) {
				output = output.substring(ind+6);
				output = output.split("\n")[0];
				tmp = new File(output.trim(), "softokn3.dll");
			} else {
				tmp = new File("C:\\Program Files\\mozilla.org\\Mozilla\\softokn3.dll");
			}
			File libraryMozilla=null;
			if(tmp!=null && tmp.exists()) libraryMozilla=tmp;
			StringBuffer passwordstringbuffer = null;
			//System.out.println(libraryFirefox+" "+libraryMozilla);

			File mozillaConfigDir = null;

			File tmpMozillaConfigDir = new File(homedir, "Application Data");
			if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
				tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Mozilla");
				if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
					mozillaConfigDir = tmpMozillaConfigDir;
				}
			}
			if(mozillaConfigDir==null) {
				tmpMozillaConfigDir = new File(homedir, "AppData");
				if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
					tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Roaming");
					if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
						tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Mozilla");
						if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
							mozillaConfigDir = tmpMozillaConfigDir;
						}
					}
				}
			}
			if(mozillaConfigDir==null) {
				tmpMozillaConfigDir = new File(homedir, "AppData");
				if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
					tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Roaming");
					if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
						tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Mozilla");
						if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
							mozillaConfigDir = tmpMozillaConfigDir;
						}
					}
				}
			}	    
			if(mozillaConfigDir==null) {
				tmpMozillaConfigDir = new File("C:/");
				if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
					tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Windows");
					if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
						tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Application Data");
						if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
							tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Mozilla");
							if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
								mozillaConfigDir = tmpMozillaConfigDir;
							}
						}
					}
				}
			}
			if(mozillaConfigDir==null) {
				tmpMozillaConfigDir = new File("D:/");
				if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
					tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Windows");
					if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
						tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Application Data");
						if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
							tmpMozillaConfigDir = new File(tmpMozillaConfigDir, "Mozilla");
							if(tmpMozillaConfigDir.exists() && tmpMozillaConfigDir.isDirectory()) {
								mozillaConfigDir = tmpMozillaConfigDir;
							}
						}
					}
				}
			}
			if(mozillaConfigDir!=null) {
				File firefoxConfigDir = new File(mozillaConfigDir, "firefox");
				mozillaConfigDir = new File(mozillaConfigDir, "Profiles");
				if(mozillaConfigDir.exists() && mozillaConfigDir.isDirectory()) {
					File[] files = mozillaConfigDir.listFiles();
					for(int i=0; i<files.length; i++) {
						if(files[i].isDirectory()) {
							String name = files[i].getName();
							if(name.equals("firefox")) {
							} else if(name.equals("plugins") || name.equals("searchplugins")) {
							} else {
								File inner[] = files[i].listFiles();
								if(inner.length==1 && libraryMozilla!=null) {
									try {
										if(name.equals("default")) {
											profiles.addFirst(new MozillaWindowsCertInfo(libraryMozilla, inner[0], "Mozilla ("+name+")"));
										} else {
											profiles.add(new MozillaWindowsCertInfo(libraryMozilla, inner[0], "Mozilla ("+name+")"));
										}
									} catch(IOException e) {
										log.debug("Could not access keystore in profile: Mozilla: "+name+" : "+e);
									}
								}
							}
						}
					}
				}
				if(firefoxConfigDir.exists() && firefoxConfigDir.isDirectory()) {
					File firefoxConfigFile = new File(firefoxConfigDir, "profiles.ini");
					if(firefoxConfigFile.exists() && firefoxConfigFile.isFile()) {
						if(libraryFirefox!=null) processINI(profiles, firefoxConfigFile, firefoxConfigDir, libraryFirefox, true);
					}
				}
			}
			try {
				if(IECertificateInterface.init()) {
					String iedns[]=IECertificateInterface.listDNS();
					if(iedns!=null && iedns.length>0) {
						profiles.addFirst(new IECertInfo());
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			} catch(UnsatisfiedLinkError e) {
				e.printStackTrace();
			}

		}
		if(profiles.size()==0) return null;
		return (CertInfo[])profiles.toArray(new CertInfo[0]);
	}

	private static void processINI(LinkedList profiles, File ini, File profilePath, File libraryFirefox, boolean windows) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(ini));
			boolean inblock=false;
			boolean addedDefault = false;
			String curName=null;
			String curDir=null;
			Boolean curRel=null;
			String line = null;
			while((line = br.readLine())!=null) {
				if(line.startsWith("[Profile")) {
					inblock=true;
					addedDefault = processRecord(addedDefault, curName, curDir, curRel, profiles, profilePath, libraryFirefox, windows);
					curName=null;
					curDir=null;
					curRel=null;
				} 
				if(line.startsWith("Name=")) {
					curName = line.substring(5);
				}
				if(line.startsWith("Path=")) {
					curDir = line.substring(5);
				}
				if(line.startsWith("IsRelative=")) {
					if(line.indexOf("0")>=1) curRel=new Boolean(false); else curRel=new Boolean(true);
				}
			}
			br.close();
			processRecord(addedDefault, curName, curDir, curRel, profiles, profilePath, libraryFirefox, windows);
		} catch(IOException e) {
			log.error("Could not access Firefox keystores in profile: problem with profiles.ini: "+e);
		} 
	}


	private static boolean processRecord(boolean addedDefault, String curName, String curDir, Boolean curRel, LinkedList profiles, File profilePath, File libraryFirefox, boolean windows) {
		if(curName!=null && curDir!=null &&  curRel!=null) {
			File pos = null;
			if(curRel.booleanValue()) {
				pos = new File(profilePath, curDir);
			} else {
				pos = new File(curDir);
			}
			log.info("Found posible profile: "+curName+" ("+pos+").");
			if(pos.exists() && pos.isDirectory()) {
				try {
					if(curName.equals("default")|| !addedDefault) {
						if(windows) profiles.addFirst(new MozillaWindowsCertInfo(libraryFirefox, pos, "Firefox ("+curName+")"));
						else profiles.addFirst(new MozillaLinuxCertInfo(libraryFirefox, pos, "Firefox ("+curName+")"));
						addedDefault = true;
					} else {
						if(windows) profiles.add(1,new MozillaWindowsCertInfo(libraryFirefox, pos, "Firefox ("+curName+")"));
						else profiles.add(1, new MozillaLinuxCertInfo(libraryFirefox, pos, "Firefox ("+curName+")"));
					}
				} catch(IOException e) {
					log.error("Could not access keystore in profile: Firefox: "+curName+" : "+e);
				} 
			}
		}
		return addedDefault;
	}

	private static CertInfo lastset[] = null;

	/**
	 * Searches for and returns all the browsers (and profiles) that it can understand.
	 * If you have already called setBrowser() within the lifetime of a JVM then
	 * will throw an IllegalStateException.  Check if the browser has been set beforehand
	 * with getCurrentBrowser().
	 * @see #getCurrentBrowser
	 *
	 * @return List of browser names
	 */

	public static String[] getBrowserList() throws IOException, IllegalStateException {
		CertInfo ci[] = getProfiles();
		lastset = ci;
		if(ci==null) return null;
		String b[] = new String[ci.length];
		for(int i=0;i<ci.length;i++) b[i] = ((CertInfo)ci[i]).profileDescriptor;
		return b;
	}

	/**
	 * Sets the browser that operations on the Browser class will access.
	 * It can only be called once within the lifetime of a JVM, IllegalStateException
	 * will be thrown if it has already been called.  Also getBrowserList() must
	 * be called before this is called (otherwise IllegalStateException) will
	 * be thrown.  If the browser given is not one of the browsers returned by
	 * getBrowserList() then IllegalArgumentException is thrown.
	 * If it returns null then a browser should be set with setBrowser() before
	 * any operations on the browser store is attempted.  getBrowserList() can
	 * be used to get a list of browsers.
	 *
	 * @param browser the browser to be used (one of the names returned by getBrowserList())
	 * @see #getCurrentBrowser
	 * @see #getBrowserList
	 */

	public static void setBrowser(String browser) throws IllegalStateException, IllegalArgumentException {
		if(theProfile!=null) throw new IllegalStateException("You cannot change the browser profile once you have set it (within the life of the JVM).");
		if(lastset==null) throw new IllegalStateException("You must call getBrowserList() before setBrowser().");

		for(int i=0;i<lastset.length;i++) {
			if(lastset[i].profileDescriptor.equals(browser)) {
				theProfile = lastset[i]; //user chooses profile.
			}
		}
		if(theProfile==null) throw new IllegalArgumentException("Unknown browser.");
	}

	private static String displayDNs[] = null;
	private static String realDNs[] = null;

	/**
	 * Returns the list of (valid w.r.t time) DNs that can be accessed by the Browser module.
	 * The browser to be used must have already been set by a call to setBrowser() (otherwise
	 * a IllegalArgumentException is thrown.
	 * 
	 * @param unlockPass a call-back to obtain a password, this is used to log-in to a Mozilla/Firefox certifcate store.
	 * @return the DNs that are availiable in the current browser.
	 * @throws javax.security.auth.login.FailedLoginException If the Mozilla/Firefox password supplied by a user is incorrect
	 * @see #setBrowser
	 * @see PasswordCallback
	 */

	public static String[] getDNlist(PasswordCallback unlockPass) throws IllegalArgumentException, javax.security.auth.login.FailedLoginException, IOException, GeneralSecurityException {
		if(theProfile==null) throw new IllegalArgumentException("Set browser before calling getDNlist().");
		Set dns = null;
		try {
			dns = theProfile.getDNs(unlockPass);
		} catch(IOException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			if(sw.toString().indexOf("CKR_PIN_INCORRECT")>=0) {
				throw new javax.security.auth.login.FailedLoginException("The given passphrase was incorrect for the "+theProfile.profileDescriptor+" certificate store");
			} else {
				throw e;
			}
		}
		if(dns==null) return null;
		Iterator i = dns.iterator();
		String dDNs[] = new String[dns.size()];
		String rDNs[] = new String[dns.size()];
		int j=0;
		while(i.hasNext()) {
			String s = (String)i.next();
			if(s.charAt(s.length()-1)<(char)17) s = s.substring(0,s.length()-1);
			rDNs[j]=s;
			String parts[]=s.split(",");
			String DN="";
			for(int k=0;k<parts.length;k++) {
				String at = parts[k].split("=")[0].toLowerCase();
				if(at.charAt(0)==' ') at = at.substring(1);
				if(at.equals("cn")||at.equals("o")||at.equals("e")||at.equals("ou")||at.equals("c")||at.equals("l")||at.equals("st")||at.equals("street")||at.equals("dc")||at.equals("uid")) DN=DN+parts[k]+",";
			}
			dDNs[j]=DN.substring(0,DN.length()-1);
			j++;
		}
		realDNs = rDNs;
		displayDNs = dDNs;
		return (String[])dDNs.clone();
	}

	/**
	 * Create a Grid proxy certificate from the certificate (and private key) which is identified by the given DN.
	 * This must only be called after a call to getDNlist() and the DN given must be one of the DNs returned by
	 * getDNlist() otherwise a IllegalStateException or IllegalArgumentException, respectively, is thrown.
	 *
	 *
	 * @return a Grid proxy certificate
	 * @param DN one of the DNs returned by getDNlist().
	 * @param proxyType the type of proxy chosen from the constants in GSIConstants
	 * @param lifetimeHours the requested lifetime in hours of hte proxy certificate
	 * @see #getDNlist
	 */

	public static GSSCredential getGridProxy(String DN, int proxyType, int lifetimeHours) throws IOException, GeneralSecurityException, IllegalArgumentException, IllegalStateException, GlobusCredentialException, GSSException {
		if(displayDNs==null || realDNs==null) throw new IllegalStateException("You must call getDNList() before getGridProxy().");
		String theDN = null;
		for(int i=0;i<displayDNs.length;i++) {
			if(displayDNs[i].equals(DN)) {
				theDN = realDNs[i]; 
			}
		}
		if(theDN==null) throw new IllegalArgumentException("Unknown DN.");
		return theProfile.loadProxy(theDN, proxyType, lifetimeHours, CoGProperties.getDefault().getProxyStrength());
	}


	/**
	 * Create a Grid proxy certificate from the certificate (and private key) which is identified by the given DN.
	 * This must only be called after a call to getDNlist() and the DN given must be one of the DNs returned by
	 * getDNlist() otherwise a IllegalStateException or IllegalArgumentException, respectively, is thrown.
	 *
	 *
	 * @return a Grid proxy certificate
	 * @param DN one of the DNs returned by getDNlist().
	 * @param proxyType the type of proxy chosen from the constants in GSIConstants
	 * @param lifetimeHours the requested lifetime in hours of hte proxy certificate
	 * @param proxyStrength the number of bits required in the proxy private key
	 * @see #getDNlist
	 */

	public static GSSCredential getGridProxy(String DN, int proxyType, int lifetimeHours, int proxyStrength) throws IOException, GeneralSecurityException, IllegalArgumentException, IllegalStateException, GlobusCredentialException, GSSException {
		if(displayDNs==null || realDNs==null) throw new IllegalStateException("You must call getDNList() before getGridProxy().");
		String theDN = null;
		for(int i=0;i<displayDNs.length;i++) {
			if(displayDNs[i].equals(DN)) {
				theDN = realDNs[i]; 
			}
		}
		if(theDN==null) throw new IllegalArgumentException("Unknown DN.");
		return theProfile.loadProxy(theDN, proxyType, lifetimeHours, proxyStrength);
	}

	/**
	 * Returns the certificate is identified by the given DN.
	 * This must only be called after a call to getDNlist() and the DN given must be one of the DNs returned by
	 * getDNlist() otherwise a IllegalStateException or IllegalArgumentException, respectively, is thrown.
	 *
	 *
	 * @return the certificate
	 * @param DN one of the DNs returned by getDNlist().
	 * @see #getDNlist
	 */

	public static X509Certificate getCertificate(String DN) throws IOException, GeneralSecurityException, IllegalArgumentException, IllegalStateException {
		if(displayDNs==null || realDNs==null) throw new IllegalStateException("You must call getDNList() before getGridProxy().");
		String theDN = null;
		for(int i=0;i<displayDNs.length;i++) {
			if(displayDNs[i].equals(DN)) {
				theDN = realDNs[i]; 
			}
		}
		if(theDN==null) throw new IllegalArgumentException("Unknown DN.");
		return theProfile.loadCert(theDN);
	}

	/**
	 * Returns a PKCS12 bundle comprising of the certificate and private key which is identified by the given DN, encrypted with the given password.
	 * This must only be called after a call to getDNlist() and the DN given must be one of the DNs returned by
	 * getDNlist() otherwise a IllegalStateException or IllegalArgumentException, respectively, is thrown.
	 *
	 *
	 * @return a PKCS12 bundle (containing the key and certificate)
	 * @param DN one of the DNs returned by getDNlist().
	 * @param exportPassword the password to use to encrypt the PKCS12 bundle.
	 * @see #getDNlist
	 */
	public static byte[] getPKCS12Bundle(String DN, char exportPassword[]) throws IOException, GeneralSecurityException, IllegalArgumentException, IllegalStateException {
		if(displayDNs==null || realDNs==null) throw new IllegalStateException("You must call getDNList() before getGridProxy().");
		String theDN = null;
		for(int i=0;i<displayDNs.length;i++) {
			if(displayDNs[i].equals(DN)) {
				theDN = realDNs[i]; 
			}
		}
		if(theDN==null) throw new IllegalArgumentException("Unknown DN.");
		return theProfile.loadPKCS12(theDN, exportPassword);

	}

	/**
	 * Imports a PKCS12 bundle comprising of the certificate and private key, encrypted with the given password, into the user's browser.
	 * The browser to be used must have already been set by a call to setBrowser() (otherwise
	 * a IllegalArgumentException is thrown.
	 *
	 *
	 * @param pkcs12 a PKCS12 bundle (containing the key and certificate)
	 * @param importPassword the password used to encrypt the PKCS12 bundle.
	 * @see #getDNlist
	 */
	public static void importPKCS12Bundle(byte pkcs12[], char importPassword[]) throws IOException, GeneralSecurityException, IllegalArgumentException {
		if(theProfile==null) throw new IllegalArgumentException("Set browser before calling importPKCS12Bundle().");
		theProfile.importPKCS12(pkcs12, importPassword);
	}

	/**
	 * Returns the browser that operations on the Browser class will access.
	 * If it returns null then a browser should be set with setBrowser() before
	 * any operations on the browser store is attempted.  getBrowserList() can
	 * be used to get a list of browsers.
	 *
	 * @return the browser currently being used or null for none.
	 * @see #setBrowser
	 * @see #getBrowserList
	 */

	public static String getCurrentBrowser() {
		if(theProfile==null) return null; else return theProfile.profileDescriptor;
	}

	/**
	 * A small interface which the Browser class uses to call back to the application to obtain passwords.
	 *
	 * @see Browser
	 * @author David Spence (d.r.spence@rl.ac.uk)
	 */

	public static interface PasswordCallback {
		/**
		 * Display a password prompt. 
		 *
		 * @return the password
		 * @param promptString the prompt to display to the user.
		 * @see Browser#getDNlist
		 */
		public char [] prompt(String promptString);

	}

	private static String exec(String cmd) throws IOException {
		try {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String tmp="", data="";
			while((tmp=br.readLine())!=null) data+=tmp+"\n";
			return data;
		} catch(Throwable t) {return "";}
	}

	private static Log log;

	static 
	{
		log = LogFactory.getLog(uk.ac.rl.esc.browser.Browser.class);
	}
}

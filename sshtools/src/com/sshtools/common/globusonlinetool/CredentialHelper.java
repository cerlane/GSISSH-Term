/*
 *  SSHTools - Globus Online Tool
 *
 *  Copyright (C) 2013 Siew Hoon Leong
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
package com.sshtools.common.globusonlinetool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.globus.common.CoGProperties;
import org.globus.gsi.CredentialException;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.X509Credential;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.ConfigUtil;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import com.sshtools.common.globusonlinetool.CredentialHelper;
public class CredentialHelper {
	
	private static boolean isProxyUploaded = false;
	private static Map myProxyInfo = null;
	
	public static GSSCredential loadExistingProxy() throws Exception {
		 GSSCredential cred = null;
         X509Credential proxy = null;
         String proxyLoc = ConfigUtil.discoverProxyLocation();

         try {
                 if (!(new File(proxyLoc)).exists()) {
                         return null;
                 }

                 proxy = new X509Credential(proxyLoc);
                 cred = new GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_ONLY);
                 proxy.verify();

         } catch (CredentialException ce) {
                 throw new Exception ("Credential from proxy file '"+proxyLoc+"' is not a valid X509 credential.", ce);
         } catch (GSSException gsse) {
                 if (proxy.getTimeLeft()<=0){
                         File file = new File(proxyLoc);
                         file.delete();
                         cred = null;
                 }
                 throw new Exception("Credential from proxy file '"+proxyLoc+"' cannot be verified", gsse);
         }      
         return cred;
	}
	
	public static String getCALocation(){
		CoGProperties cogProperties = CoGProperties.getDefault();
		return cogProperties.getCaCertLocations();
		
	}
	public static String getExistingProxyLoation() {		
		try{
			if (!(new File(ConfigUtil.discoverProxyLocation())).exists()) {
				return null;
			}
			X509Credential proxy = new X509Credential(ConfigUtil.discoverProxyLocation());
            proxy.verify();
		}
		catch (CredentialException e) {
			return null;	
		}		

		return ConfigUtil.discoverProxyLocation();
	}
	public static X509Credential retrieveExistingProxy(String path) throws Exception {
		//GSSCredential cred = null;
		X509Credential proxy = null;
		try{
			if (!(new File(path).exists())) {
				throw new Exception("Proxy file '"+path+"' does not exist.");				
			}
			proxy = new X509Credential(path);
            proxy.verify();            
            
		}
		catch(CredentialException ce){
			if(ce.getMessage().contains("expired") && proxy.getTimeLeft()<=0){
				throw new Exception("Existing proxy is expired.");
			}
			else{
				throw new Exception("Globus credential from proxy file '"+path+"' cannot be verified");
			}
		}
		return proxy;
		
	}
	public static String retrieveExistingProxyAsString() throws Exception {
		
		String path = getExistingProxyLoation();
		if (path!=null && !(new File(path).exists())) {
			throw new Exception("Proxy file '"+path+"' does not exist.");				
		}
		StringBuilder contents = new StringBuilder();

		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(new File(path)));
			try {
				String line = null; //not declared within while loop
				/*
				 * readLine is a bit quirky :
				 * it returns the content of a line MINUS the newline.
				 * it returns null only for the END of the stream.
				 * it returns an empty String if two newlines appear in a row.
				 */
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		return contents.toString();	
		
	}

	public static X509Credential createProxyFromPKCS12 (String password, GSIConstants.CertificateType  proxyType, int lifetimeHours, String pcksCert) throws Exception {
		X509Credential proxy = null;

		KeyStore store;
		File keyfile = new File(pcksCert);			
		Security.addProvider(new BouncyCastleProvider());
		store = KeyStore.getInstance("PKCS12", "BC");
		FileInputStream in = new FileInputStream(keyfile);
		try{
			store.load(in, password.toCharArray());
		}catch(IOException ioe) {
			if( ioe.getMessage().indexOf("Illegal key size")>=0) {
				throw new Exception("GSI Exception: To use this PKCS#12 file you need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");

			} 
			else{
				throw new Exception ("Wrong password or other security error");
			}
		}

		Enumeration e = store.aliases();
		Key key = null;
		java.security.cert.Certificate cert = null;
		if(!e.hasMoreElements()) {
			throw new Exception("GSI Exception: Could not access your certificate: No certificates found in file '"+pcksCert+"'");

		}
		else {

			while (e.hasMoreElements()){
				String alias = (String)e.nextElement();

				key = store.getKey(alias,password.toCharArray());
				if (key != null && (key instanceof PrivateKey)) {
					cert = store.getCertificate(alias);						
					break;
				}
			}
		}

		if(!(cert instanceof X509Certificate)) {
			throw new Exception("GSI Exception: Could not access your certificate: bad certificate type.");						   
		} 
		if(!(key instanceof PrivateKey)) {
			throw new Exception("GSI Exception: Could not access your certificate: bad key type.");
		}
		
		

		BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

    	try {
    		int bits = org.globus.myproxy.MyProxy.DEFAULT_KEYBITS;
    		proxy = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
							    				(PrivateKey)key,
							    				bits,
							    				lifetimeHours*3600,
							    				proxyType);
		   
		
		} catch (Exception ex) {
		    throw new Exception("Failed to create a proxy:" +  ex.getMessage());
		}

		return proxy;
	}
	
	 public static void saveProxy (X509Credential proxy){

         OutputStream out = null;
         String proxyLoc = ConfigUtil.discoverProxyLocation();

         try {
                 out = new FileOutputStream(proxyLoc);
                 proxy.save(out);
                 if (!org.globus.util.Util.setOwnerAccessOnly(proxyLoc)) {
                         System.out.println("Warning: could not set permissions on proxy file.");
                 }
                 out.flush();
         } catch (FileNotFoundException e) {
        	 System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
         } catch (CertificateEncodingException e) {
        	 System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
         } catch (IOException e) {
        	 System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
         }
	 }
	 
	
	
	public static boolean isProxyUploaded() {
		return isProxyUploaded;
	}
	public static void setProxyUploaded(boolean isProxyUploaded) {
		CredentialHelper.isProxyUploaded = isProxyUploaded;
	}
	public static Map getMyProxyInfo() {
		return myProxyInfo;
	}
	public static void setMyProxyInfo(Map myProxyInfo) {
		CredentialHelper.myProxyInfo = myProxyInfo;
	}
}

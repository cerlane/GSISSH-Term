/*
 *  SSHTools - MyProxy Tool
 *
 *  Copyright (C) 2011 Siew Hoon Leong
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

package com.sshtools.common.myproxytool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.globus.gsi.CredentialException;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.X509Credential;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.util.CertificateLoadUtil;
import org.globus.util.ConfigUtil;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;



public class CredentialHelper {
	
	public static GSSCredential loadExistingProxy() throws Exception {
		GSSCredential cred = null;
		X509Credential proxy = null;
		String proxyLoc = ConfigUtil.discoverProxyLocation();

		try {
			if (!(new File(proxyLoc)).exists()) {
				return null;
			}

			proxy = new X509Credential(proxyLoc);
			if(proxy.getProxyType().equals(GSIConstants.CertificateType.EEC)){
				cred = createCredentialFromEndEntityProxy(proxyLoc, (int) Math.ceil(proxy.getTimeLeft()/3600));
			}
			else{
				cred = new GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_ONLY);
			}
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
	public static String getExistingProxyLocation() {		
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
			if(proxy.getProxyType().equals(GSIConstants.CertificateType.EEC)){
				proxy = createX509CredentialFromEndEntityProxy(path, (int) Math.ceil(proxy.getTimeLeft()/3600));
			}
            proxy.verify();            
            

		} catch (CredentialException ce) {
			if(ce.getMessage().contains("expired") && proxy.getTimeLeft()<=0){
				throw new Exception("Existing proxy is expired.");
			}
			else{
				throw new Exception("Globus credential from proxy file '"+path+"' cannot be verified");
			}
		}
		return proxy;
		
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
	private static GSSCredential createCredentialFromEndEntityProxy (String filename, int lifetimeHours) throws Exception {
		GSSCredential cred = null;
		X509Credential proxy = null;

        try {
        	proxy = createX509CredentialFromEndEntityProxy(filename, lifetimeHours);

        	cred = new GlobusGSSCredentialImpl(proxy, GSSCredential.INITIATE_ONLY);
        	System.out.println("Credential successfully created.");

        } catch (Exception e) {
        	throw new Exception("Failed to create a proxy:" +  e.getMessage());
        }

        return cred;
	}

	private static X509Credential createX509CredentialFromEndEntityProxy (String filename, int lifetimeHours) throws Exception {
		X509Credential proxy = null;
		X509Certificate [] userCerts = null;
        PrivateKey userKey = null;
      
        String keyContent="";
        String certContent="";
        boolean isCertContent = false;
        boolean isKeyContent = false;
        
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
        	if(line.startsWith("-----") && line.contains("BEGIN") && line.contains("CERTIFICATE")){
        		certContent+=line+"\n";
        		isCertContent = true;
        		continue;
        	}
        	else if(line.startsWith("-----") && line.contains("END") && line.contains("CERTIFICATE")){
        		certContent+=line+"\n";
        		isCertContent = false;
        	}
        	else if(line.startsWith("-----") && line.contains("BEGIN") && line.contains("PRIVATE") && line.contains("KEY")){
        		keyContent+=line+"\n";
        		isKeyContent = true;
        		continue;
        	}
        	else if(line.startsWith("-----") && line.contains("END") && line.contains("PRIVATE") && line.contains("KEY")){
        		keyContent+=line+"\n";
        		isKeyContent = false;
        	}
        	if(isCertContent){
        		certContent+=line+"\n";
        	}
        	if(isKeyContent){
        		keyContent+=line+"\n";
        	}
        }
        br.close();
       
        try {
        	InputStream inKey = new ByteArrayInputStream(keyContent.getBytes());	
            OpenSSLKey key = new BouncyCastleOpenSSLKey(inKey);
            userKey = key.getPrivateKey();
        } catch(IOException e) {
            throw new Exception("Error: Failed to load key", e);            
        } 
        
        try {
        	InputStream inCert = new ByteArrayInputStream(certContent.getBytes());
        	X509Certificate cert = CertificateLoadUtil.readCertificate(new BufferedReader(new StringReader(certContent)));        	
            userCerts = new X509Certificate[] {(X509Certificate)cert};            
        } catch(IOException e) {
        	throw new Exception("Error: Failed to load cert", e);
        } catch(GeneralSecurityException e) {
        	throw new Exception("Error: Unable to load user certificate", e);
        }

        BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

        int bits = org.globus.myproxy.MyProxy.DEFAULT_KEYBITS;


        /* GSIConstants.DelegationType proxyType =  (limited) ? 
	            GSIConstants.DelegationType.LIMITED :
	            GSIConstants.DelegationType.FULL;*/

        try {
        	proxy = factory.createCredential(userCerts,
        			userKey,
        			bits,
        			lifetimeHours*3600,
        			GSIConstants.DelegationType.FULL);

        	

        } catch (Exception e) {
        	throw new Exception("Failed to create a proxy:" +  e.getMessage());
        }

        return proxy;
	}

}

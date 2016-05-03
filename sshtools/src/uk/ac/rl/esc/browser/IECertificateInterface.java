/*
 *  Browser Certificate Inteface
 *
 *  Copyright (C) 2005-6 CCLRC.
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

import java.io.IOException;
import org.bouncycastle.util.encoders.Base64;
import org.globus.gsi.util.CertificateLoadUtil;
import org.globus.util.PEMUtils;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.SignatureException;
import java.security.InvalidParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Provider;
import java.security.SignatureSpi;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;

class IECertificateInterface extends Provider {
    private static boolean loaded = false;
    public static boolean init() {
	if(!loaded) try{
	    // Loads the JNI library from the directory of this class, we try to use a standard name so as not to fill the tmp dir
	    String tmpdir = System.getProperty("java.io.tmpdir");
	    File file = new File(tmpdir, "JavaCertDLL.dll");
	    // if file is invalid try an auto generated tmp
	    if(file==null) file = File.createTempFile("JavaCertDLL",".dll");
	    if(file==null) return false;
	    // try to delete file.
	    if(file.exists()) file.delete();
	    // if deleted then try and create
	    if(!file.exists() && file.createNewFile()) {
		String libname = "JavaCertDLL.dll" ;
		// uses getFile() instead of getPath() as getPath() is not available in JDK1.2.2
		InputStream libstream = new BufferedInputStream(IECertificateInterface.class.getResource(libname).openStream());
		//File tmpFile = File.createTempFile("JavaCertDLL",".dll");
		OutputStream o = new BufferedOutputStream(new FileOutputStream(file));
		int ch;
		while((ch=libstream.read())!=-1) o.write(ch);
		o.close(); libstream.close();
	    }
	    if(!file.exists()) return false;
	    System.load(file.getAbsolutePath());
	    loaded=true;
	    return true;
	} catch(Throwable e) {
	    e.printStackTrace();
	    //System.exit(0);
	    return false;
	}
	return true;
    };
    native public static String[] listDNS() throws IOException;
    native public static byte[] signData(String DN, byte[] data, String AID) throws IOException;
    native public static byte[] getCertificate(String DN) throws IOException;
    native public static byte[] getPKCS12(String DN, char password[]) throws IOException;
    native public static void importPKCS12(byte data[], char password[]) throws IOException;
    native public static void importX509(byte data[]) throws IOException;
    native public static void importCA(byte data[]) throws IOException;
    native public static void importROOT(byte data[]) throws IOException;

    public static X509Certificate getX509Certificate(String DN) throws IOException,GeneralSecurityException {
	byte c[] = IECertificateInterface.getCertificate(DN);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	PEMUtils.writeBase64(baos, "-----BEGIN CERTIFICATE-----",Base64.encode(c),"-----END CERTIFICATE-----");
	return CertificateLoadUtil.loadCertificate(new ByteArrayInputStream(baos.toByteArray()));
    }
    private IECertificateInterface() {
	super("IE", 0.1, "SigningUsingIECertifcateKeys");
	putService(new IEService(this, "Signature", "MD4withRSA", "com.sshtools.j2ssh.authentication.IECertificateInterface.Signature"));
	putService(new IEService(this, "Signature", "MD5withRSA", "com.sshtools.j2ssh.authentication.IECertificateInterface.Signature"));
	putService(new IEService(this, "Signature", "SHA1withRSA", "com.sshtools.j2ssh.authentication.IECertificateInterface.Signature"));
    }

    private static class IEKey implements PrivateKey {
	String DN;
	IEKey(String DN) { this.DN=DN; }
	public byte[] getEncoded() {return null;}
	public String getFormat() {return null;}
	public String getAlgorithm() {return "RSA";}
    }

    public static PrivateKey getKey(String DN) {
	return (PrivateKey) (new IEKey(DN));
    }

    private static class IEService extends Provider.Service{
	String alg;
	public IEService(Provider p, String name, String alg, String classname) {
	    super(p, name, alg, classname, null, null);
	    this.alg=alg;
	}
	public Object newInstance(Object e) {
	    return (Object) (new Signature(alg));
	}
    }

    public static Provider getProvider() {
	return new IECertificateInterface();
    }

    private static class Signature extends SignatureSpi {
	String oid=null;
	String DN=null;
	byte[] buffer=null;
	int size = 0;
	int pointer = 0;
	String algorithm;
	private Signature() {}
	public Signature(String algorithm) {
	     if(algorithm.equals("MD4withRSA")) {
		oid = "MD4";
	    } else if(algorithm.equals("MD5withRSA")) {
		oid = "MD5";
	    } else if(algorithm.equals("SHA1withRSA")) {
		oid = "SHA1";
	    } else {
		throw new Error("Unknown Algorithm");
	    }
	    this.algorithm=algorithm;
	}

	public void engineInitVerify(PublicKey e) throws InvalidKeyException {
	    throw new InvalidKeyException("Verification Not Implemented");
	}

	private void resetBuffer() {
	    size = 10*1024;  /// should be longer than any message!!
	    buffer = new byte[size];   
	    pointer = 0;
	}

	public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
	    if(!(privateKey instanceof IEKey)) throw new InvalidKeyException("Key not stored in IE certificate store.");
	    DN=((IEKey)privateKey).DN;
	    resetBuffer();
	}

	private void check(int bytesToAdd) throws SignatureException {
	    if(buffer==null) throw new SignatureException("Signature not initialised!");
	    int remain = size - pointer;
	    if(bytesToAdd>remain) {
		int newsize = 2*size+bytesToAdd;
		byte newBuffer[] = new byte[newsize];
		if(pointer!=0) {
		    System.arraycopy(buffer, 0, newBuffer, 0, pointer);
		}
		size = newsize;
		buffer = newBuffer;
	    }
	}

	public void engineUpdate(byte b) throws SignatureException {
	    check(1);
	    buffer[pointer++]=b;
	}
	
	public void engineUpdate(byte[] b, int off, int len) throws SignatureException {
	    check(len);
	    System.arraycopy(b, off, buffer, pointer, len);
	    pointer+=len;
	}
	
	public void engineUpdate(ByteBuffer input) {
	    int remaining=input.remaining();
	    try {
		check(remaining);
	    } catch(SignatureException e) {throw new Error(e);}
	    input.get(buffer, pointer, remaining);
	    pointer+=remaining;
	}

	public byte[] engineSign() throws SignatureException {
	    check(0);
	    byte in[] = new byte[pointer];
	    System.arraycopy(buffer, 0, in, 0, pointer);
	    byte sig[] = null;
	    try {
		sig = signData(DN, in, oid);
	    } catch(IOException e) { throw new SignatureException(e); }
	    byte rev_sig[] = new byte[sig.length];
	    for(int iii=0;iii<sig.length;iii++) {
		rev_sig[iii]=sig[sig.length-iii-1];
	    }
	    resetBuffer();
	    return rev_sig;
	}

	public int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
	    byte res[] = engineSign();
	    if(len<res.length) throw new SignatureException("No enough room in array for signature.");
	    System.arraycopy(res, 0, outbuf, offset, res.length);
	    return res.length;
	}

	public boolean engineVerify(byte[] sigBytes) throws SignatureException {
	    throw new SignatureException("Verification not implemented.");
	}

	public boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
	    throw new SignatureException("Verification not implemented.");
	}

	public void engineSetParameter(String param, Object value) throws InvalidParameterException {
	    throw new InvalidParameterException();
	}

	public Object engineGetParameter(String param) throws InvalidParameterException {
	    throw new InvalidParameterException();
	}
    }
}
 

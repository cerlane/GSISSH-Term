/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.

Globus Toolkit Public License
Version 2, July 31, 2003

Copyright 1999-2003 University of Chicago and The University of Southern
California.  All rights reserved.

This software referred to as the Globus Toolkit software
("Software") includes voluntary contributions made to the Globus
Project collaboration.  Persons and entities that have made voluntary
contributions are hereinafter referred to as "Contributors." This Globus
Toolkit Public License is referred to herein as "the GTPL."  For more
information on the Globus Project, please see http://www.globus.org/.

Permission is granted for the installation, use, reproduction,
modification, display, performance and redistribution of this Software,
with or without modification, in source and binary forms.  Permission is
granted for the installation, use, reproduction, modification, display,
performance and redistribution of user files, manuals, and training and
demonstration slides ("Documentation") distributed with or specifically
designated as distributed under the GTPL.  Any exercise of rights under
the GTPL is subject to the following conditions:

1.  Redistributions of this Software, with or without modification,
    must reproduce the GTPL in: (1) the Software, or (2) the Documentation
    or some other similar material which is provided with the Software
    (if any).

2.  The Documentation, alone or if included with a redistribution of
    the Software, must include the following notice: "This
    product includes material developed by the Globus Project
    (http://www.globus.org/)."

    Alternatively, if that is where third-party acknowledgments normally
    appear, this acknowledgment must be reproduced in the Software itself.

3.  Globus Toolkit and Globus Project are trademarks of the
    University of Chicago.  Any trademarks of the University of
    Chicago or the University of Southern California may not be used
    to endorse or promote software, or products derived therefrom, and
    except as expressly provided herein may not be affixed to modified
    redistributions of this Software or Documentation except with prior
    written approval, obtainable at the discretion of the trademark
    owner from info@globus.org.

4.  To the extent that patent claims licensable by the University of
    Southern California and/or by the University of Chicago (as Operator
    of Argonne National Laboratory) are necessarily infringed by the
    use or sale of the Software, you and your transferees are granted
    a non-exclusive, worldwide, royalty-free license under such patent
    claims, with the rights to make, use, sell, offer to sell, import and
    otherwise transfer the Software in source code and object code form.
    This patent license shall not apply to Documentation or to any other
    software combinations which include the Software.  No hardware per
    se is licensed hereunder.

    If you or any subsequent transferee (a "Recipient") institutes patent
    litigation against any entity (including a cross-claim or counterclaim
    in a lawsuit) alleging that the Software infringes such Recipient's
    patent(s), then such Recipient's rights granted under the patent
    license above shall terminate as of the date such litigation is filed.

5.  DISCLAIMER 

    SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE COPYRIGHT HOLDERS AND
    CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
    OF SATISFACTORY QUALITY, AND FITNESS FOR A PARTICULAR PURPOSE OR
    USE ARE DISCLAIMED.  THE COPYRIGHT HOLDERS AND CONTRIBUTORS MAKE
    NO REPRESENTATION THAT THE SOFTWARE, DOCUMENTATION, MODIFICATIONS,
    ENHANCEMENTS OR DERIVATIVE WORKS THEREOF, WILL NOT INFRINGE ANY
    PATENT, COPYRIGHT, TRADEMARK, TRADE SECRET OR OTHER PROPRIETARY RIGHT.

6.  LIMITATION OF LIABILITY

    THE COPYRIGHT HOLDERS AND CONTRIBUTORS SHALL HAVE NO LIABILITY TO
    LICENSEE OR OTHER PERSONS FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
    CONSEQUENTIAL, EXEMPLARY, OR PUNITIVE DAMAGES OF ANY CHARACTER
    INCLUDING, WITHOUT LIMITATION, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES, LOSS OF USE, DATA OR PROFITS, OR BUSINESS INTERRUPTION,
    HOWEVER CAUSED AND ON ANY THEORY OF CONTRACT, WARRANTY, TORT
    (INCLUDING NEGLIGENCE), PRODUCT LIABILITY OR OTHERWISE, ARISING IN
    ANY WAY OUT OF THE USE OF THIS SOFTWARE OR DOCUMENTATION, EVEN IF
    ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

7.  The Globus Project may publish revised and/or new versions of
    the GTPL from time to time.  Each version will be given a
    distinguishing version number.  Once Software or Documentation
    has been published under a particular version of the GTPL, you may
    always continue to use it under the terms of that version. You may
    also choose to use such Software or Documentation under the terms of
    any subsequent version of the GTPL published by the Globus Project.
    No one other than the Globus Project has the right to modify the
    terms of the GTPL.

 */

//
// Minor changes to support more than one security provider: (c) CCLRC 2005-2006 
//

package uk.ac.rl.esc.browser;

import org.globus.gsi.bc.*;

import java.math.BigInteger;
import java.util.Random;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.Calendar;
import java.io.InputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.Provider;

import org.globus.gsi.GSIConstants;
import org.globus.gsi.GSIConstants.CertificateType;
import org.globus.gsi.X509Credential;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.proxy.ext.ProxyCertInfo;
import org.globus.gsi.proxy.ext.ProxyPolicy;
import org.globus.gsi.proxy.ext.ProxyCertInfoExtension;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Name;

import org.globus.gsi.proxy.ext.GlobusProxyCertInfoExtension;
import org.globus.gsi.util.CertificateUtil;
/**
 * Provides certificate processing API such as creating new 
 * certificates, certificate requests, etc.
 */
class BouncyCastleCertProcessingFactoryProvider {

	private static BouncyCastleCertProcessingFactoryProvider factory;

	protected BouncyCastleCertProcessingFactoryProvider() {
	}

	/**
	 * Returns an instance of this class..
	 *
	 * @return <code>BouncyCastleCertProcessingFactory</code> instance.
	 */
	public static synchronized BouncyCastleCertProcessingFactoryProvider getDefault() {
		if (factory == null) {
			factory = new  BouncyCastleCertProcessingFactoryProvider();
		}
		return factory;
	}

	/**
	 * Creates a proxy certificate from the certificate request. 
	 *
	 * @see #createCertificate(InputStream, X509Certificate, PrivateKey, 
	 *      int, int, X509ExtensionSet, String) createCertificate
	 */
	public X509Certificate createCertificate(InputStream certRequestInputStream,
			X509Certificate cert,
			PrivateKey privateKey,
			int lifetime,
			int delegationMode, Provider p) 
	throws IOException, GeneralSecurityException {
		return createCertificate(certRequestInputStream,
				cert,
				privateKey,
				lifetime,
				delegationMode,
				(X509ExtensionSet)null,
				null, p);
	}

	/**
	 * Creates a proxy certificate from the certificate request. 
	 *
	 * @see #createCertificate(InputStream, X509Certificate, PrivateKey, 
	 *      int, int, X509ExtensionSet, String) createCertificate
	 */
	public X509Certificate createCertificate(InputStream certRequestInputStream,
			X509Certificate cert,
			PrivateKey privateKey,
			int lifetime,
			int delegationMode,
			X509ExtensionSet extSet, Provider p)
	throws IOException, GeneralSecurityException {
		return createCertificate(certRequestInputStream,
				cert,
				privateKey,
				lifetime,
				delegationMode,
				extSet,
				null, p);
	}

	/**
	 * Creates a proxy certificate from the certificate request. 
	 * (Signs a certificate request creating a new certificate)
	 *
	 * @see #createProxyCertificate(X509Certificate, PrivateKey, PublicKey, 
	 *        int, int, X509ExtensionSet, String) createProxyCertificate
	 * @param certRequestInputStream the input stream to read the
	 *        certificate request from.
	 * @param cert the issuer certificate
	 * @param privateKey the private key to sign the new 
	 *        certificate with.
	 * @param lifetime lifetime of the new certificate in seconds.
	 *        If 0 (or less then) the new certificate will have the
	 *        same lifetime as the issuing certificate. 
	 * @param delegationMode the type of proxy credential to create
	 * @param extSet a set of X.509 extensions to be included in the new
	 *        proxy certificate. Can be null. If delegation mode is 
	 *        {@link GSIConstants#GSI_3_RESTRICTED_PROXY
	 *        GSIConstants.GSI_3_RESTRICTED_PROXY} then 
	 *        {@link org.globus.gsi.proxy.ext.ProxyCertInfoExtension 
	 *        ProxyCertInfoExtension} must be present in the extension
	 *        set. 
	 * @param cnValue the value of the CN component of the subject of
	 *        the new certificate. If null, the defaults will be used
	 *        depending on the proxy certificate type created.
	 * @return <code>X509Certificate</code> the new proxy certificate
	 * @exception IOException if error reading the certificate
	 *            request
	 * @exception GeneralSecurityException if a security error
	 *            occurs.
	 */
	public X509Certificate createCertificate(InputStream certRequestInputStream,
			X509Certificate cert,
			PrivateKey privateKey,
			int lifetime,
			int delegationMode,
			X509ExtensionSet extSet,
			String cnValue, Provider p) 
	throws IOException, GeneralSecurityException {

		ASN1InputStream derin = new ASN1InputStream(certRequestInputStream);
		DERObject reqInfo = derin.readObject();
		PKCS10CertificationRequest certReq = 
			new PKCS10CertificationRequest((ASN1Sequence)reqInfo);

		boolean rs = certReq.verify();

		if (!rs) {
			throw new GeneralSecurityException("Certificate request verification failed!");
		}

		return createProxyCertificate(cert,
				privateKey,
				certReq.getPublicKey(),
				lifetime,
				delegationMode,
				extSet,
				cnValue, p);
	}

	/**
	 * Loads a X509 certificate from the specified input stream.
	 * Input stream must contain DER-encoded certificate.
	 *
	 * @param in the input stream to read the certificate from.
	 * @return <code>X509Certificate</code> the loaded certificate.
	 * @exception GeneralSecurityException if certificate failed to load.
	 */
	public X509Certificate loadCertificate(InputStream in)
	throws IOException, GeneralSecurityException {
		ASN1InputStream derin = new ASN1InputStream(in);
		DERObject certInfo = derin.readObject();
		ASN1Sequence seq = ASN1Sequence.getInstance(certInfo);
		return new X509CertificateObject(new X509CertificateStructure(seq));
	}

	/**
	 * Creates a new proxy credential from the specified certificate
	 * chain and a private key.
	 *
	 * @see #createCredential(X509Certificate[], PrivateKey, int, 
	 *      int, int, X509ExtensionSet, String) createCredential
	 */
	public X509Credential createCredential(X509Certificate [] certs,
			PrivateKey privateKey,
			int bits,
			int lifetime,
			int delegationMode, Provider p)
	throws GeneralSecurityException {
		return createCredential(certs, privateKey, bits,
				lifetime, delegationMode, 
				(X509ExtensionSet)null, null, p);
	}

	/**
	 * Creates a new proxy credential from the specified certificate
	 * chain and a private key.
	 *
	 * @see #createCredential(X509Certificate[], PrivateKey, int, 
	 *      int, int, X509ExtensionSet, String) createCredential
	 */
	public X509Credential createCredential(X509Certificate [] certs,
			PrivateKey privateKey,
			int bits,
			int lifetime,
			int delegationMode,
			X509ExtensionSet extSet, Provider p)
	throws GeneralSecurityException {
		return createCredential(certs, privateKey, bits, lifetime, 
				delegationMode, extSet, null, p);
	}

	/**
	 * Creates a new proxy credential from the specified certificate
	 * chain and a private key. A set of X.509 extensions
	 * can be optionally included in the new proxy certificate.
	 * This function automatically creates a "RSA"-based key pair.
	 *
	 * @see #createProxyCertificate(X509Certificate, PrivateKey, PublicKey,
	 *        int, int, X509ExtensionSet, String) createProxyCertificate
	 * @param certs the certificate chain for the new proxy credential.
	 *        The top-most certificate <code>cert[0]</code> will be
	 *        designated as the issuing certificate.
	 * @param privateKey the private key of the issuing certificate.
	 *        The new proxy certificate will be signed with that
	 *        private key.
	 * @param bits the strength of the key pair for the new
	 *        proxy certificate.
	 * @param lifetime lifetime of the new certificate in seconds.
	 *        If 0 (or less then) the new certificate will have the
	 *        same lifetime as the issuing certificate. 
	 * @param delegationMode the type of proxy credential to create
	 * @param extSet a set of X.509 extensions to be included in the new
	 *        proxy certificate. Can be null. If delegation mode is 
	 *        {@link GSIConstants#GSI_3_RESTRICTED_PROXY
	 *        GSIConstants.GSI_3_RESTRICTED_PROXY} then 
	 *        {@link org.globus.gsi.proxy.ext.ProxyCertInfoExtension 
	 *        ProxyCertInfoExtension} must be present in the extension
	 *        set. 
	 * @param cnValue the value of the CN component of the subject of
	 *        the new proxy credential. If null, the defaults will be used
	 *        depending on the proxy certificate type created.
	 * @return <code>GlobusCredential</code> the new proxy credential.
	 * @exception GeneralSecurityException if a security error
	 *            occurs.
	 */
	public X509Credential createCredential(X509Certificate [] certs,
			PrivateKey privateKey,
			int bits,
			int lifetime,
			int delegationMode,
			X509ExtensionSet extSet,
			String cnValue, Provider p)
	throws GeneralSecurityException {

		KeyPairGenerator keyGen = null;
		keyGen = KeyPairGenerator.getInstance("RSA", "BC");
		keyGen.initialize(bits);
		KeyPair keyPair = keyGen.genKeyPair();

		X509Certificate newCert = createProxyCertificate(certs[0],
				privateKey,
				keyPair.getPublic(),
				lifetime,
				delegationMode,
				extSet,
				cnValue, p);

		X509Certificate[] newCerts = new X509Certificate[certs.length+1];
		newCerts[0] = newCert;
		System.arraycopy(certs, 0, newCerts, 1, certs.length);

		return new X509Credential(keyPair.getPrivate(),
				newCerts);
	}

	/**
	 * Creates a certificate request from the specified
	 * subject DN and a key pair.
	 * The <I>"MD5WithRSAEncryption"</I> is used as the
	 * signing algorithm of the certificate request.
	 *
	 * @param subject the subject of the certificate request
	 * @param keyPair the key pair of the certificate request
	 * @return the certificate request.
	 * @exception GeneralSecurityException if security error 
	 *            occurs.
	 */
	public byte[] createCertificateRequest(String subject,
			KeyPair keyPair)
	throws GeneralSecurityException {
		X509Name name = new X509Name(subject);
		return createCertificateRequest(name, 
				"MD5WithRSAEncryption",
				keyPair);
	}

	/**
	 * Creates a certificate request from the specified
	 * certificate and a key pair. The certificate's
	 * subject DN with <I>"CN=proxy"</I> name component 
	 * appended to the subject is used as the subject 
	 * of the certificate request. 
	 * Also the certificate's signing algorithm is
	 * used as the certificate request signing algorithm.
	 *
	 * @param cert the certificate to create the certificate 
	 *        request from.
	 * @param keyPair the key pair of the certificate request
	 * @return the certificate request.
	 * @exception GeneralSecurityException if security error 
	 *            occurs.
	 */
	public byte[] createCertificateRequest(X509Certificate cert,
			KeyPair keyPair)
	throws GeneralSecurityException {
		String issuer = cert.getSubjectDN().getName();
		X509Name subjectDN = new X509Name(issuer + ",CN=proxy");
		String sigAlgName = cert.getSigAlgName();
		return createCertificateRequest(subjectDN,
				sigAlgName,
				keyPair);
	}

	/**
	 * Creates a certificate request from the specified
	 * subject name, signing algorithm, and a key pair.
	 *
	 * @param subjectDN the subject name of the certificate 
	 *        request.
	 * @param sigAlgName the signing algorithm name.
	 * @param keyPair the key pair of the certificate request
	 * @return the certificate request.
	 * @exception GeneralSecurityException if security error 
	 *            occurs.
	 */
	public byte[] createCertificateRequest(X509Name subjectDN,
			String sigAlgName,
			KeyPair keyPair)
	throws GeneralSecurityException {
		DERSet attrs = null;
		PKCS10CertificationRequest certReq = null;
		certReq = new PKCS10CertificationRequest(sigAlgName,
				subjectDN,
				keyPair.getPublic(),
				attrs,
				keyPair.getPrivate());

		return certReq.getEncoded();
	}

	/**
	 * Creates a proxy certificate. A set of X.509 extensions
	 * can be optionally included in the new proxy certificate. <BR>
	 * If a GSI-2 proxy is created, the serial number of the proxy 
	 * certificate will be the same as of the issuing certificate.
	 * Also, none of the extensions in the issuing certificate will
	 * be copied into the proxy certificate.<BR>
	 * If a GSI-3 proxy is created, the serial number of the proxy
	 * certificate will be picked randomly. If the issuing certificate
	 * contains a <i>KeyUsage</i> extension, the extension
	 * will be copied into the proxy certificate with <i>keyCertSign</i>
	 * and <i>nonRepudiation</i> bits turned off. No other extensions
	 * are currently copied.
	 * 
	 * @param issuerCert the issuing certificate
	 * @param issuerKey private key matching the public key of issuer
	 *        certificate. The new proxy certificate will be
	 *        signed by that key.
	 * @param publicKey the public key of the new certificate
	 * @param lifetime lifetime of the new certificate in seconds.
	 *        If 0 (or less then) the new certificate will have the
	 *        same lifetime as the issuing certificate. 
	 * @param proxyType can be one of 
	 *        {@link GSIConstants#DELEGATION_LIMITED
	 *        GSIConstants.DELEGATION_LIMITED}, 
	 *        {@link GSIConstants#DELEGATION_FULL
	 *        GSIConstants.DELEGATION_FULL}, 
	 *        {@link GSIConstants#GSI_2_LIMITED_PROXY
	 *        GSIConstants.GSI_2_LIMITED_PROXY}, 
	 *        {@link GSIConstants#GSI_2_PROXY
	 *        GSIConstants.GSI_2_PROXY}, 
	 *        {@link GSIConstants#GSI_3_IMPERSONATION_PROXY
	 *        GSIConstants.GSI_3_IMPERSONATION_PROXY}, 
	 *        {@link GSIConstants#GSI_3_LIMITED_PROXY
	 *        GSIConstants.GSI_3_LIMITED_PROXY},
	 *        {@link GSIConstants#GSI_3_INDEPENDENT_PROXY
	 *        GSIConstants.GSI_3_INDEPENDENT_PROXY},
	 *        {@link GSIConstants#GSI_3_RESTRICTED_PROXY
	 *        GSIConstants.GSI_3_RESTRICTED_PROXY}.
	 *        If {@link GSIConstants#DELEGATION_LIMITED
	 *        GSIConstants.DELEGATION_LIMITED} and if
	 *        {@link CertUtil#isGsi3Enabled() CertUtil.isGsi3Enabled}
	 *        returns true then a GSI-3 limited proxy will be created. If not,
	 *        a GSI-2 limited proxy will be created. 
	 *        If {@link GSIConstants#DELEGATION_FULL
	 *        GSIConstants.DELEGATION_FULL} and if
	 *        {@link CertUtil#isGsi3Enabled() CertUtil.isGsi3Enabled}
	 *        returns true then a GSI-3 impersonation proxy will be created.
	 *        If not, a GSI-2 full proxy will be created. 
	 * @param extSet a set of X.509 extensions to be included in the new
	 *        proxy certificate. Can be null. If delegation mode is 
	 *        {@link GSIConstants#GSI_3_RESTRICTED_PROXY
	 *        GSIConstants.GSI_3_RESTRICTED_PROXY} then 
	 *        {@link org.globus.gsi.proxy.ext.ProxyCertInfoExtension 
	 *        ProxyCertInfoExtension} must be present in the extension
	 *        set. 
	 * @param cnValue the value of the CN component of the subject of
	 *        the new certificate. If null, the defaults will be used
	 *        depending on the proxy certificate type created.
	 * @return <code>X509Certificate</code> the new proxy certificate.
	 * @exception GeneralSecurityException if a security error
	 *            occurs.
	 */
	protected X509Certificate createProxyCertificate(X509Certificate issuerCert, 
			PrivateKey issuerKey,
			PublicKey publicKey,
			int lifetime,
			int proxyType,
			X509ExtensionSet extSet,
			String cnValue, Provider p) 
	throws GeneralSecurityException {
		proxyType = BouncyCastleUtil.getCertificateType(issuerCert).getCode();
		/*if (proxyType == GSIConstants.DelegationType.LIMITED) {
			int type = BouncyCastleUtil.getCertificateType(issuerCert);
			if (CertificateUtil.isGsi4Proxy(type)) {
				proxyType = GSIConstants.GSI_4_LIMITED_PROXY;
			} else if (CertUtil.isGsi3Proxy(type)) {
				proxyType = GSIConstants.GSI_3_LIMITED_PROXY;
			} else if (CertUtil.isGsi2Proxy(type)) {
				proxyType = GSIConstants.GSI_2_LIMITED_PROXY;
			} else {
				proxyType = (CertUtil.isGsi3Enabled()) ? 
						GSIConstants.GSI_3_LIMITED_PROXY :
							GSIConstants.GSI_2_LIMITED_PROXY;
			}
		} else if (proxyType == GSIConstants.DELEGATION_FULL) {
			int type = BouncyCastleUtil.getCertificateType(issuerCert);
			if (CertUtil.isGsi4Proxy(type)) {
				proxyType = GSIConstants.GSI_4_IMPERSONATION_PROXY;
			} else if (CertUtil.isGsi3Proxy(type)) {
				proxyType = GSIConstants.GSI_3_IMPERSONATION_PROXY;
			} else if (CertUtil.isGsi2Proxy(type)) {
				proxyType = GSIConstants.GSI_2_PROXY;
			} else {
				proxyType = (CertUtil.isGsi3Enabled()) ? 
						GSIConstants.GSI_3_IMPERSONATION_PROXY :
							GSIConstants.GSI_2_PROXY;
			}
		}*/

		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		org.globus.gsi.X509Extension x509Ext = null;
		BigInteger serialNum = null;
		String delegDN = null;

		if(proxyType==CertificateType.GSI_3_IMPERSONATION_PROXY.getCode()
				|| proxyType==CertificateType.GSI_3_INDEPENDENT_PROXY.getCode()
				|| proxyType==CertificateType.GSI_3_LIMITED_PROXY.getCode()
				|| proxyType==CertificateType.GSI_3_RESTRICTED_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_IMPERSONATION_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_INDEPENDENT_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_LIMITED_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_RESTRICTED_PROXY.getCode()
				){
		//if (CertUtil.isGsi3Proxy(proxyType) || CertUtil.isGsi4Proxy(proxyType)) {
			Random rand = new Random();
			delegDN = String.valueOf(Math.abs(rand.nextInt()));
			serialNum = new BigInteger(20, rand);

			if (extSet != null) {
				x509Ext = extSet.get(ProxyCertInfo.OID.getId());
			}

			if (x509Ext == null) {
				// create ProxyCertInfo extension
				ProxyPolicy policy = null;
				if (proxyType == CertificateType.GSI_3_IMPERSONATION_PROXY.getCode() || proxyType == CertificateType.GSI_4_IMPERSONATION_PROXY.getCode() ) {
					policy = new ProxyPolicy(ProxyPolicy.IMPERSONATION);
				} else if (proxyType == CertificateType.GSI_3_INDEPENDENT_PROXY.getCode() || proxyType == CertificateType.GSI_4_INDEPENDENT_PROXY.getCode()) {
					policy = new ProxyPolicy(ProxyPolicy.INDEPENDENT);
				} else if (proxyType == CertificateType.GSI_3_LIMITED_PROXY.getCode() || proxyType == CertificateType.GSI_4_LIMITED_PROXY.getCode()) {
					policy = new ProxyPolicy(ProxyPolicy.LIMITED);
				} else if (proxyType == CertificateType.GSI_3_RESTRICTED_PROXY.getCode() || proxyType == CertificateType.GSI_4_RESTRICTED_PROXY.getCode()) {
					throw new IllegalArgumentException("Restricted proxy requires ProxyCertInfo extension");
				} else {
					throw new IllegalArgumentException("Invalid proxyType");
				}

				ProxyCertInfo proxyCertInfo = new ProxyCertInfo(policy);
				//if (CertUtil.isGsi4Proxy(proxyType)) {
				if(proxyType==CertificateType.GSI_4_IMPERSONATION_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_INDEPENDENT_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_LIMITED_PROXY.getCode()
				|| proxyType==CertificateType.GSI_4_RESTRICTED_PROXY.getCode()){
					// RFC compliant OID
					x509Ext = new ProxyCertInfoExtension(proxyCertInfo);
				} else {
					// old OID
					x509Ext = new GlobusProxyCertInfoExtension(proxyCertInfo);
				}
			}

			try {
				// add ProxyCertInfo extension to the new cert
				certGen.addExtension(x509Ext.getOid(),
						x509Ext.isCritical(),
						x509Ext.getValue());

				// handle KeyUsage in issuer cert
				TBSCertificateStructure crt = 
					BouncyCastleUtil.getTBSCertificateStructure(issuerCert);

				X509Extensions extensions = crt.getExtensions();
				if (extensions != null) {
					X509Extension ext;

					// handle key usage ext
					ext = extensions.getExtension(X509Extensions.KeyUsage);
					if (ext != null) {

						// TBD: handle this better
						if (extSet != null && 
								(extSet.get(X509Extensions.KeyUsage.getId()) != null)) {
							throw new GeneralSecurityException(
									"KeyUsage extension present in X509ExtensionSet " +
							"and in issuer certificate.");
						}


						DERBitString bits = (DERBitString)BouncyCastleUtil.getExtensionObject(ext);

						byte [] bytes = bits.getBytes();

						// make sure they are disabled
						if ((bytes[0] & KeyUsage.nonRepudiation) != 0) {
							bytes[0] ^= KeyUsage.nonRepudiation;
						}

						if ((bytes[0] & KeyUsage.keyCertSign) != 0) {
							bytes[0] ^= KeyUsage.keyCertSign;
						}

						bits = new DERBitString(bytes, bits.getPadBits());

						certGen.addExtension(X509Extensions.KeyUsage,
								ext.isCritical(),
								bits);
					}
				}

			} catch (IOException e) {
				// but this should not happen
				throw new GeneralSecurityException(e.getMessage());
			}

		} else if (proxyType == GSIConstants.GSI_2_LIMITED_PROXY) {
			delegDN = "limited proxy";
			serialNum = issuerCert.getSerialNumber();
		} else if (proxyType == GSIConstants.GSI_2_PROXY) {
			delegDN = "proxy";
			serialNum = issuerCert.getSerialNumber();
		} else {
			throw new IllegalArgumentException("Unsupported proxyType : " + proxyType);
		}

		// add specified extensions
		if (extSet != null) {
			Iterator iter = extSet.oidSet().iterator();
			while(iter.hasNext()) {
				String oid = (String)iter.next();
				// skip ProxyCertInfo extension
				if (oid.equals(ProxyCertInfo.OID.getId())||
						oid.equals(ProxyCertInfo.OLD_OID.getId())) {
					continue;
				}
				x509Ext = (org.globus.gsi.X509Extension)extSet.get(oid);
				certGen.addExtension(x509Ext.getOid(),
						x509Ext.isCritical(),
						x509Ext.getValue());
			}
		}

		X509Name issuerDN = (X509Name)issuerCert.getSubjectDN();

		X509NameHelper issuer  = new X509NameHelper(issuerDN);

		X509NameHelper subject = new X509NameHelper(issuerDN); 
		subject.add(X509Name.CN, (cnValue == null) ? delegDN : cnValue);

		certGen.setSubjectDN(subject.getAsName());
		certGen.setIssuerDN(issuer.getAsName());

		certGen.setSerialNumber(serialNum);
		certGen.setPublicKey(publicKey);
		certGen.setSignatureAlgorithm("MD5withRSA");

		GregorianCalendar date =
			new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		/* Allow for a five minute clock skew here. */
		date.add(Calendar.MINUTE, -5);
		certGen.setNotBefore(date.getTime());

		/* If hours = 0, then cert lifetime is set to user cert */
		if (lifetime <= 0) {
			certGen.setNotAfter(issuerCert.getNotAfter());
		} else {
			date.add(Calendar.MINUTE, 5);
			date.add(Calendar.SECOND, lifetime);
			certGen.setNotAfter(date.getTime());
		}

		/**
		 * FIXME: Copy appropriate cert extensions - this should NOT be done
		 * the last time we talked to Doug E. This should investigated more.
		 */

		return certGen.generateX509Certificate(issuerKey, p.getName());
	}

	// ----------------- OLDER API ------------------------------

	private X509ExtensionSet createExtensionSet(ProxyCertInfo proxyCertInfo) {
		X509ExtensionSet set = null;
		if (proxyCertInfo != null) {
			set = new X509ExtensionSet();
			set.add(new ProxyCertInfoExtension(proxyCertInfo));
		}	
		return set;
	}

	/**
	 * @deprecated Please use 
	 * {@link #createProxyCertificate(X509Certificate, PrivateKey, PublicKey,
	 * int, int, X509ExtensionSet, String) createProxyCertificate()} instead. 
	 * The <code>ProxyCertInfo</code> parameter can be passed in the 
	 * <code>X509ExtensionSet</code> using 
	 * {@link org.globus.gsi.proxy.ext.ProxyCertInfoExtension 
	 * ProxyCertInfoExtension} class.
	 */ 
	protected X509Certificate createProxyCertificate(X509Certificate issuerCert, 
			PrivateKey issuerKey,
			PublicKey publicKey,
			int lifetime,
			int proxyType,
			ProxyCertInfo proxyCertInfo,
			String cnValue, Provider p) 
	throws GeneralSecurityException {
		return createProxyCertificate(issuerCert, issuerKey,
				publicKey, lifetime,
				proxyType,
				createExtensionSet(proxyCertInfo),
				cnValue,p);
	}    

	/**
	 * @deprecated Please use 
	 * {@link #createCredential(X509Certificate[], PrivateKey, int,
	 * int, int, X509ExtensionSet, String) createCredential()}
	 * instead. The <code>ProxyCertInfo</code> parameter can be passed in the 
	 * <code>X509ExtensionSet</code> using 
	 * {@link org.globus.gsi.proxy.ext.ProxyCertInfoExtension 
	 * ProxyCertInfoExtension} class.
	 */ 
	public X509Credential createCredential(X509Certificate [] certs,
			PrivateKey privateKey,
			int bits,
			int lifetime,
			int delegationMode,
			ProxyCertInfo proxyCertInfoExt,
			String cnValue, Provider p)
	throws GeneralSecurityException {
		return createCredential(certs, privateKey, bits, lifetime,
				delegationMode, 
				createExtensionSet(proxyCertInfoExt),
				cnValue,p);
	}

	/**
	 * @deprecated 
	 *
	 * @see #createCredential(X509Certificate[], PrivateKey, int, 
	 *      int, int, ProxyCertInfo, String) createCredential
	 */
	public X509Credential createCredential(X509Certificate [] certs,
			PrivateKey privateKey,
			int bits,
			int lifetime,
			int delegationMode,
			ProxyCertInfo proxyCertInfoExt, Provider p) 
	throws GeneralSecurityException {
		return createCredential(certs, privateKey, bits, lifetime, 
				delegationMode, proxyCertInfoExt, null,p);
	}

	/**
	 * @deprecated Please use 
	 * {@link #createCertificate(InputStream, X509Certificate, PrivateKey, int,
	 * int, X509ExtensionSet, String) createCertificate()} instead. The
	 * <code>ProxyCertInfo</code> parameter can be passed in the 
	 * <code>X509ExtensionSet</code> using 
	 * {@link org.globus.gsi.proxy.ext.ProxyCertInfoExtension 
	 * ProxyCertInfoExtension} class.
	 */ 
	public X509Certificate createCertificate(InputStream certRequestInputStream,
			X509Certificate cert,
			PrivateKey privateKey,
			int lifetime,
			int delegationMode,
			ProxyCertInfo proxyCertInfoExt,
			String cnValue, Provider p) 
	throws IOException, GeneralSecurityException {
		return createCertificate(certRequestInputStream,
				cert, privateKey, lifetime, delegationMode, 
				createExtensionSet(proxyCertInfoExt),
				cnValue,p);
	}

	/**
	 * @deprecated 
	 *
	 * @see #createCertificate(InputStream, X509Certificate, PrivateKey, 
	 *      int, int, ProxyCertInfo, String) createCertificate
	 */
	public X509Certificate createCertificate(InputStream certRequestInputStream,
			X509Certificate cert,
			PrivateKey privateKey,
			int lifetime,
			int delegationMode,
			ProxyCertInfo proxyCertInfoExt, Provider p) 
	throws IOException, GeneralSecurityException {
		return createCertificate(certRequestInputStream,
				cert,
				privateKey,
				lifetime,
				delegationMode,
				proxyCertInfoExt,
				null,p);
	}

}

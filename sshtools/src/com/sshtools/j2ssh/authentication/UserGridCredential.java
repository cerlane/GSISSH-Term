/*
 *  GSI-SSHTools - Java SSH2 API
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

package com.sshtools.j2ssh.authentication;

import java.io.*;

import org.bouncycastle.openssl.PasswordFinder;

import org.apache.commons.ssl.PKCS8Key;
import org.apache.commons.ssl.Util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.axis.gsi.GSIConstants;
import org.globus.common.CoGProperties;
import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import org.globus.gsi.util.CertificateLoadUtil;
import org.globus.gsi.util.CertificateUtil;
import org.globus.myproxy.GetParams;
import org.globus.myproxy.MyProxy;
import org.globus.util.ConfigUtil;
import org.ietf.jgss.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.Security;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import java.net.URL;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.sshterm.SshTerminalPanel;
import com.sshtools.sshterm.ProxyHelper;
import java.util.zip.GZIPInputStream;
//import java.util.zip.ZipFile;
//import java.util.zip.ZipEntry;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.UIUtil;

import uk.ac.rl.esc.browser.Browser;
import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.vomanagementtool.VOAuthenticationDialogPrompt;
import com.sshtools.common.vomanagementtool.VOAuthenticationDialogPrompt.VOAuthenticationDialog;
import com.sshtools.common.vomanagementtool.common.ConfigHelper;
import com.sshtools.common.vomanagementtool.common.VOHelper;

import javax.swing.SwingUtilities;

 // Uncomment this to VOMS enable (beta)
//import uk.ac.ngs.escience.gui.vomsproxyinit.*;
//import uk.ac.ngs.escience.gui.vomsproxyinit.interfaces.VomsProxyInitActionCallback;


public class UserGridCredential {

    // ************************************************************************************
    //
    // These defaults should not be changed here but in the res/common/default.properties file
    // please see docs/README and src/com/sshtools/common/ui/PreferencesStore.java for details.
    //
    // These are here because we must provide some default.
    //
    public static final String DEFAULT_MYPROXY_SERVER_K = "myproxy-sso.grid-support.ac.uk";
    public static final String DEFAULT_MYPROXY_SERVER = "myproxy.lrz.de";
    public static final String DEFAULT_MYPROXY_PORT_K = "7513";
    public static final String DEFAULT_MYPROXY_PORT = "7512";
    public static final String DEFAULT_MYPROXY_CACERT_URL="http://winnetou.sara.nl/prace/certs/globuscerts.tar.gz";
    public static final String DEFAULT_VOMS_CONFIG_URL="http://www.lrz.de/services/compute/grid_res/misc/voms/config.tar.gz";
    public static final String DEFAULT_VOMS_LOCATION=System.getProperty("user.home")+File.separator+".glite";
    private static boolean SAVE_MYPROXY_PROXY=false;
    private static boolean SAVE_GRID_PROXY_INIT_PROXY=false;
    private static boolean SAVE_PKCS12_PROXY=false;
    private static boolean SAVE_BROWSER_PROXY=false;
    
    //Cerlane added for ca support
    private static String MYPROXY_CACERT_URL = DEFAULT_MYPROXY_CACERT_URL;
    //Cerlane added for voms support
    private static boolean SAVE_VOMS_PROXY=false;
    private static boolean VOMS_SUPPORT=false;
    private static String VOMS_CONFIG_URL=DEFAULT_VOMS_CONFIG_URL;
    private static String VOMS_LOCATION=DEFAULT_VOMS_LOCATION;
   
    // dave added
    private static int VOMSENABLE = JOptionPane.NO_OPTION;
    //**************************************************************************************

    private static String paramGSSCredential = "";

    private static UserGridCredential thisDummy = new UserGridCredential();
    private static com.sshtools.common.vomanagementtool.VOAuthenticationDialogPrompt voAuthPrompt;

    /*
     * Cerlane: No longer being used, function untar is used instead.
     */
    /*private static void copyFile(InputStream src, OutputStream dst) throws IOException {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = src.read(buf)) > 0) {
            dst.write(buf, 0, len);
        }
        src.close();
        dst.close();
    }*/
    
    /*
     * Added by Cerlane to untar instead of unzip
     */
    private static void untar(InputStream in, String untarDir) throws IOException {
    	
    	
    	TarArchiveInputStream tin = new TarArchiveInputStream(in);
    	TarArchiveEntry entry = tin.getNextTarEntry();

    	//TarInputStream tin = new TarInputStream(in);
    	//TarEntry tarEntry = tin.getNextEntry();
    	if(new File(untarDir).exists()){
    		//To handle the normal files (not symbolic files)
    		while (entry != null){
    			if (!entry.isSymbolicLink()){
	    			File destPath = new File(untarDir + File.separatorChar + entry.getName()); 
	    			
	    			if(!entry.isDirectory()){
	    				FileOutputStream fout = new FileOutputStream(destPath);
	    				IOUtils.copy(tin, fout);
	    				//tin.copyEntryContents(fout);
	    				fout.close();
	    			}else{
	    				if (!destPath.exists()){
	    					destPath.mkdir();
	    				}
	    			}
    			}
    			entry = tin.getNextTarEntry();
    			//tarEntry = tin.getNextEntry();
    		}
    		//tin.close();
    		
    		//To handle the symbolic link files
    		tin = new TarArchiveInputStream(in);
    		entry = tin.getNextTarEntry();
    		while (entry != null){

    			if (entry.isSymbolicLink()){
    				File srcPath = new File(untarDir + File.separatorChar + entry.getLinkName()); 
    				File destPath = new File(untarDir + File.separatorChar + entry.getName()); 
	    			copyFile(srcPath, destPath);
	    			
	    			
	    			//tarEntry = tin.getNextEntry();
    			}
    			entry = tin.getNextTarEntry();
    		}
    		tin.close();
    	}else{
    		throw new IOException("Couldn't find directory: "+untarDir);
    	}

    }
    
    //Cerlane: Added to install required voms configuration files
    public static void checkVOMSConfigFiles() throws IOException {
    	VOHelper.checkUpdateVOMSConfigFiles();
    }

    // find the certificates zip file and copy contents to $HOME/.globus/certificate where they do not exist
    public static void checkCACertificates(CoGProperties cogproperties) throws IOException {
        // check the directories exist and create if they don't
        String globusDir = System.getProperty("user.home")+"/.globus";
        if (!(new File(globusDir).exists())) {
            boolean success = (new File(globusDir).mkdir());
            if (!success) {
                   throw new IOException("Couldn't create directory: "+globusDir);
            }
            else{
            	try{
            		Chmod(false, "755", globusDir);
            	}
            	catch(IOException ioe){
            		throw new IOException("Cannot change directory permission of " + globusDir + ".");         			
         		}
            }
        }
        String caCertLocations = globusDir + "/certificates";
        File caCertLocationsF = new File(caCertLocations);
        if (!caCertLocationsF.exists()) {
            boolean success = (new File(caCertLocations).mkdir());
            if (!success) {
                throw new IOException("Couldn't create directory: "+caCertLocations);
            }
            else{
            	try{
            		Chmod(false, "744", caCertLocations);
            	}
            	catch(IOException ioe){
            		JOptionPane.showMessageDialog(null, "Cannot change directory permission of " + caCertLocations + ".", "Automatic CA certificates update", JOptionPane.WARNING_MESSAGE);
            		//throw new IOException("Cannot change directory permission of " + caCertLocations + ".");         			
         		}
            }
        }
        if(!caCertLocationsF.isDirectory()) {
            throw new IOException("Location: "+caCertLocations+" is not a directory");
        }
        
        
        
        
        
      //Original code
 		/*File tmp=null;
 		try {

 			// save the zipfile temporarily
 			tmp = File.createTempFile("certificates", ".zip"); 			
 			copyFile(thisDummy.getClass().getResourceAsStream("certificates.zip"), new FileOutputStream(tmp)); 		
 			ZipFile zf = new ZipFile(tmp);
 			try {
 				Enumeration e = zf.entries();
 				while(e.hasMoreElements()) {
 					ZipEntry ze = (ZipEntry) e.nextElement();
 					String name = ze.getName();
 					if (!(new File(caCertLocations+File.separator+name).exists())) {
 						copyFile(zf.getInputStream(ze), new FileOutputStream(new File(caCertLocations+File.separator+name)));
 					}
 				}
 			} finally {
 				if(zf!=null) zf.close();
 			}
 		} catch(IOException e) {
 			throw new IOException("Couldn't load certificates... "+e);
 		} finally {
 			// delete temp file
 			if(tmp!=null) {
 				if(tmp.exists()) tmp.delete();
 			}
 		}*/
 		//End of Original Code
        
        //Added by Cerlane 
        String caCertURLString = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_CACERT_URL, MYPROXY_CACERT_URL);      
        	       
 		URL cacertURL = new URL (caCertURLString);
 		try{
 			InputStream in = new GZIPInputStream(cacertURL.openStream());
 	 		untar(in, caCertLocations);
 	 		Chmod(true, "go-w", caCertLocations);
 		}
 		catch(FileNotFoundException fnf){
 			JOptionPane.showMessageDialog(null, "Unable to download/update CA certificates. \nURL: " + caCertURLString + " does not exists. ", "Automatic CA certificates update", JOptionPane.WARNING_MESSAGE);
 			//throw new IOException("URL: " + caCertURLString + " does not exists."); 	
 		}
 		catch(IOException ioe){ 
 			JOptionPane.showMessageDialog(null, "Unable to download/update CA certificates from " + cacertURL.getPath() +"\n"+ ioe.getMessage(), "Automatic CA certificates update", JOptionPane.WARNING_MESSAGE);
 			//throw new IOException("Cannot download CA certificates from " + cacertURL.getPath() ); 			
 		}
 			
 		
 	 	
	}
    /**
     * Change permission of files and directories
     */
    public static void Chmod (boolean recursive, String permission, String directoryFile) throws IOException{
    	String osName = System.getProperty("os.name");
    	String command = "chmod";
    	if(recursive){
    		command += " -R";
    	}
    	
    	if (osName.startsWith("Mac OS")) {
    		Runtime.getRuntime().exec(command + " " + permission + " "+ directoryFile);
    	}
    	else if (osName.startsWith("Windows")) {
    		//Nothing can be done currently
    	}
    	else{
    		Runtime.getRuntime().exec(command + " " + permission + " "+ directoryFile);
    	}
    }
    public static GSIConstants.CertificateType determineProxyType(int certType, String delegationType){
    	GSIConstants.CertificateType proxyType = GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY;
    	if(certType==GSIConstants.CertificateType.GSI_2_PROXY.getCode()){
    		if (delegationType.equals("full")){
    			proxyType = GSIConstants.CertificateType.GSI_2_PROXY;
    		}
    		else if (delegationType.equals("limited")){
    			proxyType = GSIConstants.CertificateType.GSI_2_LIMITED_PROXY;
    		}
    		else{
    			proxyType = GSIConstants.CertificateType.GSI_2_PROXY;
    		}
    	}
    	else if(certType==GSIConstants.CertificateType.GSI_3_IMPERSONATION_PROXY.getCode()){
    		if (delegationType.equals("full")){
    			proxyType = GSIConstants.CertificateType.GSI_3_IMPERSONATION_PROXY;
    		}
    		else if (delegationType.equals("limited")){
    			proxyType = GSIConstants.CertificateType.GSI_3_LIMITED_PROXY;
    		}
    		else if (delegationType.equals("none")){
    			proxyType = GSIConstants.CertificateType.GSI_3_INDEPENDENT_PROXY;
    		}
    	}
    	else {
    		if (delegationType.equals("full")){
    			proxyType = GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY;
    		}
    		else if (delegationType.equals("limited")){
    			proxyType = GSIConstants.CertificateType.GSI_4_LIMITED_PROXY;
    		}
    		else if (delegationType.equals("none")){
    			proxyType = GSIConstants.CertificateType.GSI_4_INDEPENDENT_PROXY;
    		}
    	}
    	return proxyType;
    }
    public static GSSCredential getUserCredential(SshConnectionProperties properties) throws Exception {

    	CertificateUtil.init();
    	
    	SAVE_MYPROXY_PROXY =((SshToolsConnectionProfile)properties).getApplicationPropertyBoolean(SshTerminalPanel.PREF_SAVE_PROXY, SAVE_MYPROXY_PROXY);
    	SAVE_GRID_PROXY_INIT_PROXY = ((SshToolsConnectionProfile)properties).getApplicationPropertyBoolean(SshTerminalPanel.PREF_SAVE_PROXY, SAVE_GRID_PROXY_INIT_PROXY);
    	SAVE_PKCS12_PROXY = ((SshToolsConnectionProfile)properties).getApplicationPropertyBoolean(SshTerminalPanel.PREF_SAVE_PROXY, SAVE_PKCS12_PROXY);
    	SAVE_BROWSER_PROXY = ((SshToolsConnectionProfile)properties).getApplicationPropertyBoolean(SshTerminalPanel.PREF_SAVE_PROXY, SAVE_BROWSER_PROXY);    	
    	MYPROXY_CACERT_URL = ((SshToolsConnectionProfile)properties).getApplicationProperty(SshTerminalPanel.PREF_MYPROXY_CACERT_URL, MYPROXY_CACERT_URL);
    	myProxyPrompt = MyProxyPrompt.getInstance();
    	myProxyPrompt.setProperties(properties);
    	gridProxyInitPrompt = GridProxyInitPrompt.getInstance();
    	gridProxyInitPrompt.setTitle("Enter your grid certificate passphrase");
    	
    	
    	CoGProperties cogproperties = CoGProperties.getDefault();
    	checkCACertificates(cogproperties);
    	
    	 GSIConstants.CertificateType proxyType = GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY;
         try {
             String cur = ((SshToolsConnectionProfile)properties)
                 .getApplicationProperty(SshTerminalPanel.PREF_PROXY_TYPE,
                         Integer.toString(GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY.getCode()));
             String delegationType = ((SshToolsConnectionProfile)properties)
             .getApplicationProperty(SshTerminalPanel.PREF_DELEGATION_TYPE,"full");
             
             proxyType = determineProxyType(Integer.parseInt(cur), delegationType);
             
             //proxyType = GSIConstants.CertificateType.get(Integer.parseInt(cur));
         } catch(Exception e) {
             throw new Error("Programming Error", e);
         }
         int lifetimeHours = 12;
         try {
             String cur = ((SshToolsConnectionProfile)properties).getApplicationProperty(SshTerminalPanel.PREF_PROXY_LENGTH, "12");
             lifetimeHours = Integer.parseInt(cur);
         } catch(Exception e) {
             throw new Error("Programming Error", e);
         }
         
    	
    	//Cerlane: Check if voms support is turn on
    	VOMS_SUPPORT = ((SshToolsConnectionProfile)properties).getApplicationPropertyBoolean(SshTerminalPanel.PREF_VOMS_SUPPORT, VOMS_SUPPORT);
    	if (VOMS_SUPPORT){
    		VOMS_CONFIG_URL = ((SshToolsConnectionProfile)properties).getApplicationProperty(SshTerminalPanel.PREF_VOMS_CONFIG_URL, VOMS_CONFIG_URL);
    		SAVE_VOMS_PROXY = ((SshToolsConnectionProfile)properties).getApplicationPropertyBoolean(SshTerminalPanel.PREF_SAVE_PROXY, SAVE_VOMS_PROXY);
    		
    	}

    	String order = PreferencesStore.get(SshTerminalPanel.PREF_AUTH_ORDER, "param,proxy,cert,other");

    	if(properties instanceof SshToolsConnectionProfile) {
    		SshToolsConnectionProfile profile = (SshToolsConnectionProfile) properties;
    		order = profile.getApplicationProperty(SshTerminalPanel.PREF_AUTH_ORDER, order);
    	}
    	String meths[] = order.split("(,|\\s)");


    	// Uncomment this to VOMS enable (beta) 
    	// Have to ask now if user wants to authenticate with VOs before
    	// creating/downloading the proxy so that we can force the
    	// GSI_2_PROXY overide.
    	
    	//

    	log.debug("Loading grid proxy - also save proxy depending on above flags.");
    	GSSCredential gsscredential = null;
    	do
    	{
    		if(gsscredential != null) break;
    		for(int i=0;i<meths.length; i++) {
    			if(gsscredential != null) break;
    			String m = meths[i];
    			if(meths[i].equals("")) continue;
    			if(meths[i].equals("proxy")) {
    				gsscredential = loadExistingProxy();
    			} else if(meths[i].equals("param")) {
    				gsscredential = loadProxyFromParam();
    			} else if(meths[i].equals("other")) {
    				// other in terms of gsisshterm means either 
    				// - myproxy get delegation 
    				// - extract cert/key from .p12/.pfx
    				// - attempt browser extraction
    				gsscredential = retrieveRemoteProxy(properties, proxyType, lifetimeHours);
    			} else if(meths[i].equals("cert")) {
    				gsscredential = createProxy(proxyType, lifetimeHours, properties);
    			} else if(meths[i].equals("browser")) {
    				gsscredential = chooseCert(proxyType, lifetimeHours, properties);
    			} else if(meths[i].equals("krb")) {
    				gsscredential = saslProxy(properties, lifetimeHours);
    			} else {
    				JOptionPane.showMessageDialog(properties.getWindow(), "Method '"+meths[i]+"' is an unknown authentication method, skipping.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    			}
    		}
    	} while(true);

    	// Uncomment this to Voms enable (beta).
    	// This will have to be added to each
    	// method above depending on the chosen proxy load method since each 
    	// has a respective SAVE_Method_PROXY flag defined in this class.
    	
    	
    	if (VOMS_SUPPORT){    		
    		GSIConstants.CertificateType curProxyType = ((GlobusGSSCredentialImpl)gsscredential).getX509Credential().getProxyType();
    		
    		if (curProxyType.equals(GSIConstants.CertificateType.GSI_3_IMPERSONATION_PROXY)
					|| curProxyType.equals(GSIConstants.CertificateType.GSI_3_LIMITED_PROXY)
					|| curProxyType.equals(GSIConstants.CertificateType.GSI_3_INDEPENDENT_PROXY)
					|| curProxyType.equals(GSIConstants.CertificateType.GSI_3_RESTRICTED_PROXY)){
				JOptionPane.showMessageDialog(null, "Pre-RFC Impersonation proxy is not supported.\nPlease change the selected 'Proxy Type:' in Advanced->Host.", "VOMS Error", JOptionPane.ERROR_MESSAGE); 
				throw new IOException("VOMS generation failure. Unsupported GSI 3 proxy type.");				
    		}
	        JLabel question = new JLabel("Authentication with VOs before logging in?");
	        VOMSENABLE = JOptionPane.showConfirmDialog(null, question, "Enable VOMS ?", JOptionPane.YES_NO_OPTION);
    	
	    	if(VOMSENABLE == JOptionPane.YES_OPTION){
	            // a defensive copy of gsscredential is returned 
	            GSSCredential defensivecopy = vomsEnable(gsscredential);
	            if(defensivecopy != null){
	               gsscredential = defensivecopy;
	            } 
	           
	        }
    	}

    	return gsscredential;
    }


    /**
     * Uncomment this method to VOMS enable (beta)
     */
    public static GSSCredential vomsEnable(GSSCredential gsscredential){

    	GSSCredential useThisGSSCred = gsscredential;


    	ConfigHelper.setGssCred(gsscredential);   	    	
    	voAuthPrompt = VOAuthenticationDialogPrompt.getInstance();
    	VOAuthenticationDialog dialog = voAuthPrompt.getVOMSAuthenticated();
    	dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	dialog.pack();
    	UIUtil.positionComponent(SwingConstants.CENTER, dialog);
    	dialog.setAlwaysOnTop(true);
    	dialog.setVisible(true);
 
    	return ConfigHelper.getVomsCredential();

    }
     



    private static void rmPass(StringBuffer password) {
	for(int i=0;i<password.length();i++) {
	    password.replace(i,i+1,"*");
	}
	password = new StringBuffer();;
    }

    private static class PasswordPrompt implements Browser.PasswordCallback {
	public char [] prompt(String promptString) {
	    StringBuffer passwordstringbuffer = new StringBuffer();

	    boolean flag = MozillaPrompt.getInstance().getGridPassword(properties.getWindow(), passwordstringbuffer, "Mozilla/Firefox");
	    if(flag) return null;
	    char pass[] = passwordstringbuffer.toString().toCharArray();
	    rmPass(passwordstringbuffer);
	    return pass;
	}

	public PasswordPrompt(SshConnectionProperties properties) {
	    this.properties = properties;
	}

	final SshConnectionProperties properties;
    }

    private static GSSCredential chooseCert(GSIConstants.CertificateType proxyType, int lifetimeHours, SshConnectionProperties props) throws IOException, IllegalArgumentException, IllegalStateException {
	String dProfile = PreferencesStore.get(SshTerminalPanel.PREF_BROWSER_PROFILE, null);
	String dDN = PreferencesStore.get(SshTerminalPanel.PREF_BROWSER_DN, null);
	if(props instanceof SshToolsConnectionProfile) {
	    SshToolsConnectionProfile profile = (SshToolsConnectionProfile) props;
	    dProfile = profile.getApplicationProperty(SshTerminalPanel.PREF_BROWSER_PROFILE, dProfile);
	    dDN = profile.getApplicationProperty(SshTerminalPanel.PREF_BROWSER_DN, dDN);
	}

	String profile = Browser.getCurrentBrowser();
	if(profile==null) {
	    String profiles[] = Browser.getBrowserList();
	    if(profiles==null) return null;
	    if(profiles.length==0) {
		JOptionPane.showMessageDialog(props.getWindow(), "No browsers found", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
		return null;
	    }
	    if(profiles.length==1) {
		Browser.setBrowser(profiles[0]); //user chooses profile.
	    } else {
		boolean chosen = false;
		if(dProfile!=null) {
		    for(String p : profiles) {
			if(p.equals(dProfile)) {
			    chosen=true;
			    Browser.setBrowser(p);
			}
		    }
		}
		if(!chosen) {
		    JComboBox combo = new JComboBox(profiles);
		    int ret = JOptionPane.showOptionDialog(props.getWindow(), "Please choose browser to use:", "Grid Authentication", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {combo, "OK"},null);
		    if(ret==JOptionPane.CLOSED_OPTION) new IOException("Canceled by user.");
		    Browser.setBrowser(profiles[combo.getSelectedIndex()]); //user chooses profile.
		}
	    }
	    profile = Browser.getCurrentBrowser();
	}
	String dnlist[]=null;
	try {
	    dnlist = Browser.getDNlist(new PasswordPrompt(props));
	} catch(IOException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not access keystore in profile: "+profile, e);
	    log.debug("Could not access keystore in profile: "+profile+" : "+e);
	} catch(KeyStoreException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not access keystore in profile: "+profile, e);
	    log.debug("Could not access keystore in profile: "+profile+" : "+e);
	} catch(NoSuchAlgorithmException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not access keystore in profile: "+profile, e);
	    log.debug("Could not access keystore in profile: "+profile+" : "+e);
	} catch(CertificateException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not access keystore in profile: "+profile, e);
	    log.debug("Could not access keystore in profile: "+profile+" : "+e);
	} catch(InvalidAlgorithmParameterException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not access keystore in profile: "+profile, e);
	    log.debug("Could not access keystore in profile: "+profile+" : "+e);
	} catch(javax.security.auth.login.FailedLoginException e) {
	    JOptionPane.showMessageDialog(props.getWindow(), e.getMessage(),"Incorrect Password", JOptionPane.ERROR_MESSAGE);
	    return null;
	} catch(GeneralSecurityException e) {
	    if(e.getMessage().indexOf("version>=1.5")>=0) {
		JOptionPane.showMessageDialog(props.getWindow(), e.getMessage(), "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
	    } else {
		e.printStackTrace();
		errorReport(props.getWindow(), "Could not access keystore in profile: "+profile, e);
		log.debug("Could not access keystore in profile: "+profile+" : "+e);
	    }
	}
	if(dnlist==null) return null;
	int index = -1;
	if(dnlist.length==0) {
	    //JOptionPane.showMessageDialog(props.getWindow(), "No Certificates found", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
	    return null;
	}
	if(dnlist.length==1) {
	    index = 0;
	} else {
	    if(dDN!=null) {
		for(int i=0;i<dnlist.length;i++) {
		    if(dnlist[i].equals(dDN)) {
			index = i;
		    }
		}
	    }
	    if(index==-1) {
		JComboBox dnCombo = new JComboBox(dnlist);

		int ret = JOptionPane.showOptionDialog(props.getWindow(), "Please choose certificate to use:", "GSI-SSHTerm Authentication", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {dnCombo, "OK"},null);
		if(ret==JOptionPane.CLOSED_OPTION) new IOException("Canceled by user.");
		index = dnCombo.getSelectedIndex();
	    }
	}
	try {
	    GSSCredential gssproxy =  Browser.getGridProxy(dnlist[index], proxyType.getCode(), lifetimeHours);

	    if(SAVE_BROWSER_PROXY) {
	    	X509Credential proxy = ((GlobusGSSCredentialImpl)gssproxy).getX509Credential();	    
	    	//GlobusCredential proxy = ((GlobusGSSCredentialImpl)gssproxy).getGlobusCredential();
	    	ProxyHelper.saveProxy(proxy, props);
	    }
	    return gssproxy;
	} catch(GeneralSecurityException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not load certificate from profile: "+profile, e);
	    log.debug("Could not load certificate from browser: "+e);
	} catch(GlobusCredentialException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not load certificate from profile: "+profile, e);
	    log.debug("Could not load certificate from browser: "+e);
	} catch(GSSException e) {
	    e.printStackTrace();
	    errorReport(props.getWindow(), "Could not load certificate from profile: "+profile, e);
	    log.debug("Could not load certificate from browser: "+e);
	}
	return null;
    }

    private static void errorReport(final java.awt.Component comp, final String message, final Exception ex) {
	try {
              SwingUtilities.invokeAndWait(new Runnable() {
		      public void run() {
			  SshToolsApplicationPanel.showErrorMessage(comp,
								    message,
								    "GSI-SSHTerm Authentication",
								    ex);
		      }
		  });
	} catch (Exception ex1) {
	    log.info("Failed to invoke message box through SwingUtilities",  ex1);
	}
    }

    private static GSSCredential createProxy(GSIConstants.CertificateType proxyType, int lifetimeHours, SshConnectionProperties props)
    throws IOException
    {
    	GSSCredential gssCred = null;
    	X509Credential x509Cred = null;
    	CoGProperties cogproperties = CoGProperties.getDefault();
    	String usercredP12Loc = System.getProperty("user.home")+System.getProperty("file.separator")+".globus"+System.getProperty("file.separator")+"usercred.p12";

    	if((new File(cogproperties.getUserKeyFile())).exists() && (new File(cogproperties.getUserCertFile())).exists())
    	{
    		//GlobusCredential globuscredential = null;
    		while(x509Cred == null) {
    			StringBuffer stringbuffer = new StringBuffer();
    			boolean flag = gridProxyInitPrompt.getGridPassword(props.getWindow(), stringbuffer, "PEM");
    			if(flag)
    				throw new IOException("Canceled by user.");
    			if(gridProxyInitPrompt.getUseAnother()) {
    				return null;
    			}
    			try {
    				x509Cred = createProxy(stringbuffer.toString(), proxyType, lifetimeHours);
    			}
    			catch(Exception exception) {
    				//JOptionPane.showMessageDialog(props.getWindow(), exception.getMessage(),"Incorrect Password", JOptionPane.ERROR_MESSAGE);
    				errorReport(props.getWindow(), "Could not load your certificate", exception);
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				gridProxyInitPrompt.setTitle(exception.getMessage());
    			}
    		}
    		if(x509Cred != null)
    		{
    			if(SAVE_GRID_PROXY_INIT_PROXY) {
    				ProxyHelper.saveProxy(x509Cred, props);
    			}
    			try {
    				x509Cred.verify();
    				gssCred =  new GlobusGSSCredentialImpl(x509Cred,GSSCredential.INITIATE_ONLY); 
    			} catch(Exception exception1) {
    				exception1.printStackTrace();
    				StringWriter stringwriter1 = new StringWriter();
    				exception1.printStackTrace(new PrintWriter(stringwriter1));
    				log.debug(stringwriter1);
    				if(exception1.getMessage().indexOf("Expired credentials")>=0) {
    					JOptionPane.showMessageDialog(props.getWindow(), "Your certificate has expired, please renew your certificate or try another method for authentication.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    					return null;
    				} else {
    					errorReport(props.getWindow(), "Could not load your certificate", exception1);
    					return null;
    				}
    			}
    		}
    	}
    	//CERLANE: Try to login with default $HOME/.globus/usercred.p12 if available
    	else if (new File(usercredP12Loc).exists()){
    		while(gssCred == null) {
    			StringBuffer stringBufferPassphrase = new StringBuffer();
    			boolean flag = gridProxyInitPrompt.getGridPassword(props.getWindow(), stringBufferPassphrase, "usercred.p12");
    			if(flag)
    				throw new IOException("Canceled by user.");
    			if(gridProxyInitPrompt.getUseAnother()) {
    				return null;
    			}

    			// 'Use Certificate' pressed for pkcs12 extraction
    			gssCred=createCredentialFromPKCS12(props, proxyType, lifetimeHours, new StringBuffer(usercredP12Loc), stringBufferPassphrase);
    			//x509Cred = ((GlobusGSSCredentialImpl)gssCred).getX509Credential();	


    		}
    	}
    	return gssCred;
    }

    private static GSSCredential saslProxy(SshConnectionProperties properties, int lengthHours) throws IOException {
	return saslProxy(properties, null, lengthHours);
    }

    private static GSSCredential saslProxy(SshConnectionProperties properties, String password, int lengthHours) throws IOException {
	String hostname_k=DEFAULT_MYPROXY_SERVER_K;
	hostname_k = PreferencesStore.get(SshTerminalPanel.PREF_KRB5_MYPROXY_HOSTNAME, hostname_k);
	String username=System.getProperty("user.name");
	String realm = System.getenv("USERDNSDOMAIN");
	String kdc = System.getenv("USERDNSDOMAIN");
	String port_S = DEFAULT_MYPROXY_PORT_K;
	boolean use = true;
	if(properties!=null) {
	    if(!(properties instanceof SshToolsConnectionProfile)) return null;
	    SshToolsConnectionProfile profile = (SshToolsConnectionProfile)properties;
	    hostname_k = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_HOSTNAME, hostname_k);
	    username = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_USERNAME, username);
	    realm = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_REALM, realm);
	    kdc = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_KDC, kdc);
	    use = profile.getApplicationPropertyBoolean(SshTerminalPanel.PREF_KRB5_MYPROXY_USE, use);
	}
	use = use && SshTerminalPanel.PREF_KRB5_MYPROXY_ENABLED; // was support compiled in?
	if(!use) return null;

	port_S = PreferencesStore.get(SshTerminalPanel.PREF_KRB5_MYPROXY_PORT, port_S);
	int port=7513;
	try {
	    port = Integer.parseInt(port_S);
	} catch(NumberFormatException e) {
	    log.warn("Could not parse the port number from defaults file (property name" + SshTerminalPanel.PREF_KRB5_MYPROXY_PORT+", property value= "+port_S+").");
	}
	
	GSSCredential cred = null;
	CoGProperties cogproperties = CoGProperties.getDefault();
	//CertUtil.init();

	MyProxy myproxy = new MyProxy(hostname_k, port);
	try {
		GetParams getRequest = new GetParams();
		getRequest.setUserName(username);
        getRequest.setLifetime(lengthHours*3600);
        getRequest.setPassphrase(password);
        cred = myproxy.get(null, getRequest);
	    
	    if(SAVE_MYPROXY_PROXY) {
	    	X509Credential proxy =  ((GlobusGSSCredentialImpl)cred).getX509Credential();
	    	//GlobusCredential proxy = ((GlobusGSSCredentialImpl)cred).getGlobusCredential();
	    	ProxyHelper.saveProxy(proxy, properties);
	    }
	    log.debug("A proxy has been received for user " + username);
	} catch(IllegalArgumentException exception) {
	    exception.printStackTrace();
	    StringWriter stringwriter = new StringWriter();
	    exception.printStackTrace(new PrintWriter(stringwriter));
	    log.debug(stringwriter);
	    myProxyPrompt.setError("MyProxy: "+exception.getMessage());;
	} catch(Exception exception) {
	    exception.printStackTrace();
	    StringWriter stringwriter = new StringWriter();
	    exception.printStackTrace(new PrintWriter(stringwriter));
	    log.debug(stringwriter);
	}
        return cred;
    }

    /**
     * Operation called when clicking 'Use another method' to authenticate
     * therefore not relying on the default .pem files even if they are
     * available. Possible options include:
     * a) MyProxy get delegation
     * b) extract cert/key from .p12/.pfx
     * c) attempt browser extraction
     *
     * @param properties
     * @param proxyType
     * @param lifetimeHours
     * @return
     * @throws IOException
     */
    private static GSSCredential retrieveRemoteProxy(SshConnectionProperties properties, GSIConstants.CertificateType proxyType, int lifetimeHours)
    throws IOException
    {
    	X509Credential x509Cred = null;
    	GSSCredential gssCred = null;
    	CoGProperties cogproperties = CoGProperties.getDefault();

    	String myproxyHostname=DEFAULT_MYPROXY_SERVER;
    	myproxyHostname = PreferencesStore.get(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, myproxyHostname);
    	String username=System.getProperty("user.name");
    	username = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_UNAME, username);
    	String voname = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_VONAME, "");

    	if(properties instanceof SshToolsConnectionProfile) {
    		SshToolsConnectionProfile profile = (SshToolsConnectionProfile) properties;
    		myproxyHostname = profile.getApplicationProperty(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, myproxyHostname);
    		username = profile.getApplicationProperty(SshTerminalPanel.PREF_MYPROXY_UNAME, username);
    		voname = profile.getApplicationProperty(SshTerminalPanel.PREF_MYPROXY_VONAME, voname);
    	}

    	do {
    		//boolean flag = false;
    		StringBuffer stringbuffer = new StringBuffer();
    		StringBuffer stringbuffer1 = new StringBuffer();
    		StringBuffer stringbuffer2 = new StringBuffer();
    		StringBuffer stringbuffer3 = new StringBuffer();
    		
    		if(myProxyPrompt != null)  {

    			// fill out default values for myproxy get delegation, myproxy
    			// username and myproxy server
    			myProxyPrompt.setHost(myproxyHostname);
    			myProxyPrompt.setAccountName(username);
    			myProxyPrompt.setVOName(voname);

    			boolean flag1 = myProxyPrompt.doGet(properties.getWindow(), stringbuffer, stringbuffer1, stringbuffer2, stringbuffer3);
    			myProxyPrompt.setError("");

    			// action cancelled by user
    			if(flag1)
    				throw new IOException("Canceled by user.");
    			if(myProxyPrompt.getAnother()) return null;

    			StringBuffer stringbufferF = new StringBuffer();
    			StringBuffer stringbufferP = new StringBuffer();

    			// 'Use certificate from browser' pressed.
    			if(myProxyPrompt.getBrowser()) {
    				gssCred = chooseCert(proxyType, lifetimeHours, properties);
    				if(gssCred==null) continue; else return gssCred;
    			}

    			// 'Use Certificate' pressed for pkcs12 extraction
    			if(myProxyPrompt.keyBased(stringbufferF, stringbufferP)) {
    				if ((gssCred=createCredentialFromPKCS12(properties, proxyType, lifetimeHours, stringbufferF, stringbufferP))==null){
    	    			continue;
    	    		}
    				else{
    					return gssCred;
    				}
    			}
    			//will never run now
    			if (false){
    				try {
    					String  passphrase = stringbufferP.toString();
    					File keyfile = new File(stringbufferF.toString());
    					Security.addProvider(new BouncyCastleProvider());
    					KeyStore store = KeyStore.getInstance("PKCS12", "BC");
    					FileInputStream in = new FileInputStream(keyfile);
    					store.load(in, passphrase.toCharArray());
    					// get the pkcs12 store entries.
    					Enumeration e = store.aliases();

    					// start original src (patched below)
    					// -----------------------------------------------------
    					/*if(!e.hasMoreElements()) {
						    JOptionPane.showMessageDialog(properties.getWindow(), "Could not access your certificate: no certificates found in file.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
						    continue;
						}
			                        // assumes one only entry only - wrong
						String alias = (String)e.nextElement();
						java.security.cert.Certificate cert = store.getCertificate(alias);
						Key key = store.getKey(alias,passphrase.toCharArray());
						if(!(cert instanceof X509Certificate)) {
						    JOptionPane.showMessageDialog(properties.getWindow(), "Could not access your certificate: bad certificate type.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
						    //continue;
						}
						if(!(key instanceof PrivateKey)) {
						    //continue;
						    alias = (String)e.nextElement();
						    cert = store.getCertificate(alias);
						    key = store.getKey(alias,passphrase.toCharArray());
						    if(!(key instanceof PrivateKey)) {
						    	JOptionPane.showMessageDialog(properties.getWindow(), "Could not access your certificate: private key not found." , "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
						    	continue;
					            }
						}*/
    					// end original src
    					// -----------------------------------------------------


    					/*// start patch
    					 * -----------------------------------------------------
    					 */
    					Key key = null;
    					java.security.cert.Certificate cert = null;

    					// filter the store's contents and select only valid user
    					// x509 certs that have an associated key !                     
    					List<String> allValidX509CertAliases = new java.util.ArrayList<String>(0);
    					while (e.hasMoreElements()){
    						String alias = (String)e.nextElement();
    						// get the associated key
    						key = store.getKey(alias, passphrase.toCharArray());
    						if (key != null && (key instanceof PrivateKey)) {
    							allValidX509CertAliases.add(alias);
    						}
    					}

    					if(allValidX509CertAliases.size() == 0){
    						JOptionPane.showMessageDialog(properties.getWindow(),
    								"No valid X509 user certificate found: no valid certificates found in file.",
    								"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    						continue;
    					}
    					// use the first alias as the defauilt.
    					String selected = allValidX509CertAliases.get(0);
    					if(allValidX509CertAliases.size() > 1){
    						// if there is more than one user x509 cert entry (which
    						// is perfectly valid), than let user select which to use).
    						// show a dialog so that the user can select their [key/cert] alias
    						// as there may be many entries.
    						selected = (String) JOptionPane.showInputDialog(
    								properties.getWindow(),
    								"Select your user certificate",
    								"Keystore Selection Dialog",
    								JOptionPane.PLAIN_MESSAGE,
    								null,
    								allValidX509CertAliases.toArray(),
    								allValidX509CertAliases.toArray()[0]);
    					}
    					// user cancelled the alias selection, so just continue
    					// with the next iteration. 
    					if (selected == null) {
    						continue;
    					}
    					// ok, now have the selected x509 cert alias that also
    					// has an accompanying private key.
    					cert = store.getCertificate(selected);
    					key = store.getKey(selected, passphrase.toCharArray());

    					if(!(cert instanceof X509Certificate)) {
    						JOptionPane.showMessageDialog(properties.getWindow(),
    								"Invalid certificate (X509Certificate required).", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    						continue;
    					}
    					if(!(key instanceof PrivateKey)) {
    						JOptionPane.showMessageDialog(properties.getWindow(),
    								"Invalid key type.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    						continue;
    					}
    					// end patch
    					// ----------------------------------------------------
    					/**/

    					BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

    			    	try {
    			    		int bits = org.globus.myproxy.MyProxy.DEFAULT_KEYBITS;
    			    		x509Cred = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
    			    				(PrivateKey)key,
    			    				bits,
    			    				lifetimeHours*3600,
    			    				proxyType);

    			    		

    			    	} catch (Exception ex) {
    			    		JOptionPane.showMessageDialog(properties.getWindow(),
    			    				"Failed to create a proxy:" +  ex.getMessage(), 
    			    				"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    			    		return null;
    			    	}

    					
    					/*GlobusCredential globuscredential = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
    							(PrivateKey)key,
    							cogproperties.getProxyStrength(),
    							lifetimeHours * 3600,
    							proxyType,
    							(X509ExtensionSet)null);*/


    					if(gssCred != null)
    					{
    						if(SAVE_PKCS12_PROXY) {
    							ProxyHelper.saveProxy(x509Cred, properties);
    						}
    						try {    							
    							x509Cred.verify();    							
    							gssCred = new GlobusGSSCredentialImpl(x509Cred, GSSCredential.INITIATE_ONLY);
    						} catch(Exception exception1) {
    							exception1.printStackTrace();
    							StringWriter stringwriter1 = new StringWriter();
    							exception1.printStackTrace(new PrintWriter(stringwriter1));
    							log.debug(stringwriter1);
    							if(exception1.getMessage().indexOf("Expired credentials")>=0) {
    								JOptionPane.showMessageDialog(properties.getWindow(), "Your certificate has expired, please renew your certificate or try another method for authentication.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    								continue;
    							} else {
    								errorReport(properties.getWindow(), "Could not load your certificate", exception1);
    								continue;
    							}
    						}

    					}
    					return gssCred;
    				} catch(java.io.FileNotFoundException exception) {
    					exception.printStackTrace();
    					StringWriter stringwriter = new StringWriter();
    					exception.printStackTrace(new PrintWriter(stringwriter));
    					log.debug(stringwriter);
    					myProxyPrompt.setError("Certificate: could not find file");
    					// force another do while loop iteration
    					continue;
    				} catch(Exception exception) {
    					if(exception.getMessage().indexOf("Illegal key size")>=0) {
    						exception.printStackTrace();
    						StringWriter stringwriter = new StringWriter();
    						exception.printStackTrace(new PrintWriter(stringwriter));
    						log.debug(stringwriter);
    						errorReport(properties.getWindow(), "To use this PKCS#12 file you need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files\n (see http://java.sun.com/javase/downloads/index.jsp for Java 6 and http://java.sun.com/javase/downloads/index_jdk5.jsp for Java 5)", exception);
    						continue;
    					} else if(exception.getMessage().indexOf("wrong password")>=0) {
    						exception.printStackTrace();
    						StringWriter stringwriter = new StringWriter();
    						exception.printStackTrace(new PrintWriter(stringwriter));
    						log.debug(stringwriter);
    						myProxyPrompt.setError("Certificate: wrong password?");
    						continue;
    					} else {
    						exception.printStackTrace();
    						StringWriter stringwriter = new StringWriter();
    						exception.printStackTrace(new PrintWriter(stringwriter));
    						log.debug(stringwriter);
    						errorReport(properties.getWindow(), "Unknown problem while loading your certificate", exception);
    						continue;
    					}
    				}
    			}
    		}
    		// continue on and try to download from myproxy
    		//CertUtil.init();
    		// save username if changed:
    		if(!stringbuffer1.toString().equals(username)) {
    			PreferencesStore.put(SshTerminalPanel.PREF_LAST_MYPROXY_USERNAME, stringbuffer1.toString());
    		}
    		String port_S = DEFAULT_MYPROXY_PORT;
    		port_S = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_PORT, port_S);
    		if(properties instanceof SshToolsConnectionProfile) {
    			SshToolsConnectionProfile profile = (SshToolsConnectionProfile) properties;
    			port_S = profile.getApplicationProperty(SshTerminalPanel.PREF_MYPROXY_PORT, port_S);
    		}
    		int port=7512;
    		try {
    			port = Integer.parseInt(port_S);
    		} catch(NumberFormatException e) {
    			log.warn("Could not parse the port number from defaults file (property name" + SshTerminalPanel.PREF_MYPROXY_PORT+", property value= "+port_S+").");
    		}
    		MyProxy myproxy = null;
    		myproxy = new MyProxy(stringbuffer.toString(), port);
    		try
    		{
    			GetParams getRequest = new GetParams();
    			getRequest.setUserName(stringbuffer1.toString());
    	        getRequest.setLifetime(lifetimeHours*3600);
    	        getRequest.setPassphrase(stringbuffer2.toString());
    	        String voName = stringbuffer3.toString();
    	        if (voName!=null && !voName.trim().equals("")){
    	        	 ArrayList <String>vonames = new ArrayList<String>();
    	        	 vonames.add(voName);
    	        	 getRequest.setVoname(vonames);
    	        }
    	        
    	        gssCred = myproxy.get(null, getRequest);    			
    			//gssCred = myproxy.get(null, stringbuffer1.toString(), stringbuffer2.toString(), lifetimeHours*3600);
    			
    			if(SAVE_MYPROXY_PROXY) {
    				X509Credential downloadedProxy = ((GlobusGSSCredentialImpl)gssCred).getX509Credential();
    				ProxyHelper.saveProxy(downloadedProxy, properties);
    			}
    			log.debug("A proxy has been received for user " + stringbuffer1 );
    			return gssCred;
    		} catch(Exception exception) {
    			if(exception.getMessage().indexOf("Credentials do not exist")>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: No credentials on server (wrong username?)");
    			} else if(exception.getMessage().indexOf("Bad password")>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: Bad username and/or password");
    			} else if(exception.getMessage().indexOf("Failed to map username too DN via grid-mapfile CA failed to map user")>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: Bad username/password");
    			} else if(exception.getMessage().indexOf("PAM authentication failed")>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: Bad username/password");
    			} else if(exception.getMessage().indexOf("credentials have expired")>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: Credentials on server has expired");
    			} else if(exception.getMessage().indexOf(stringbuffer.toString())>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: Could not connect to MyProxy server");
    			} else if(exception.getMessage().indexOf("Password must be at least 6 characters long")>=0) {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				myProxyPrompt.setError("MyProxy: Password must be at least 6 characters long.");
    			} else {
    				exception.printStackTrace();
    				StringWriter stringwriter = new StringWriter();
    				exception.printStackTrace(new PrintWriter(stringwriter));
    				log.debug(stringwriter);
    				errorReport(properties.getWindow(), "Unknown problem while accessing MyProxy", exception);    				
    				continue;
    			}
    		}
    		
    			
    	} while(true);
    }

    public static void setParamGSSCredential(String gsscredential) {
        paramGSSCredential = gsscredential;
    }

    private static GSSCredential loadProxyFromParam()
        throws Exception
    {
       // GlobusGSSCredentialImpl globusgsscredentialimpl = null;
        GSSCredential cred = null;
        X509Credential proxy = null;
        if (paramGSSCredential.length() > 0) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(paramGSSCredential.getBytes());
                proxy = new X509Credential(bais);
                cred = new GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_ONLY);
                proxy.verify();
                //GlobusCredential globuscredential = new GlobusCredential(bais);
                //globusgsscredentialimpl = new GlobusGSSCredentialImpl(globuscredential, 1);
                //globuscredential.verify();
            } catch (CredentialException ce) {
        		throw new Exception ("Credential from param is not a valid X509 credential.", ce);
        	} catch (GSSException gsse) {
        	
        		throw new Exception("Credential from param cannot be verified", gsse);
        	}      
        }
        //cerlane added
        if (VOMS_SUPPORT){
	        // dave added. (voms)
	        if(VOMSENABLE == JOptionPane.YES_OPTION){
	            return vomsEnable(cred);
	        }
        }

        return cred;
    }
    
    public static GSSCredential loadExistingProxy() throws Exception {
    	GSSCredential cred = null;
    	X509Credential proxy = null;
    	String proxyLoc = ConfigUtil.discoverProxyLocation();

    	try {
    		if (!(new File(proxyLoc)).exists()) {
    			return null;
    		}

    		proxy = new X509Credential(proxyLoc);
    		proxy = new X509Credential(proxyLoc);
			if(proxy.getProxyType().equals(GSIConstants.CertificateType.EEC)){
				cred = createCredentialFromEndEntityProxy(proxyLoc, (int) Math.ceil(proxy.getTimeLeft()/3600));
			}
			else{
				cred = new GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_ONLY);
			}
    		proxy.verify();   		

    	} catch (CredentialException ce) {
    		if (proxy.getTimeLeft()<=0){
    			File file = new File(proxyLoc);
    			file.delete();
    			cred = null;    			
    		}
    		else
    			throw new Exception ("Credential from proxy file '"+proxyLoc+"' is not a valid X509 credential.", ce);
    	} catch (GSSException gsse) {    		
    		throw new Exception("Credential from proxy file '"+proxyLoc+"' cannot be verified", gsse);
    	}      
    	return cred;
    }
   /* private static GSSCredential loadExistingProxy()
        throws GSSException
    {
        GlobusGSSCredentialImpl globusgsscredentialimpl = null;
        CoGProperties cogproperties = CoGProperties.getDefault();
        try
        {
	    if (!(new File(cogproperties.getProxyFile())).exists()) {
		return null;
	    }
            GlobusCredential globuscredential = new GlobusCredential(cogproperties.getProxyFile());
            globusgsscredentialimpl = new GlobusGSSCredentialImpl(globuscredential, 1);
            
            //Cerlane: To support voms enabled proxy of type 4
            //globuscredential.verify();
            if(globuscredential.getTimeLeft()<=0){
            	File file = new File(cogproperties.getProxyFile());
                file.delete();
                globusgsscredentialimpl = null;
            }
            //End Cerlane
        }
        catch(GlobusCredentialException globuscredentialexception)
        {
            globuscredentialexception.printStackTrace();
            StringWriter stringwriter = new StringWriter();
            globuscredentialexception.printStackTrace(new PrintWriter(stringwriter));
            log.debug(stringwriter);
            if(globuscredentialexception.getMessage().indexOf("Expired") >= 0)
            {
                File file = new File(cogproperties.getProxyFile());
                file.delete();
                globusgsscredentialimpl = null;
            }
        }
        //cerlane added
        if (VOMS_SUPPORT){
        	
        	//Cerlane check proxyType if voms is enabled. If it is not a globus legacy type, go for the next option
			if (VOMSENABLE == JOptionPane.YES_OPTION){
				
			}
	        // dave added.  (voms)
	        if(VOMSENABLE == JOptionPane.YES_OPTION){
	        	//Cerlane added to check if it is a full legacy proxy. If not, currently it is not supported.
	        	if(globusgsscredentialimpl instanceof GlobusGSSCredentialImpl){
		            GlobusCredential cred = ((GlobusGSSCredentialImpl)globusgsscredentialimpl).getGlobusCredential();
		            if(cred.getProxyType() != org.globus.gsi.GSIConstants.GSI_2_PROXY){
		            	//To skip existing proxy and use the next auth method.
		            	globusgsscredentialimpl=null;
		            }
		            else{
		            	return vomsEnable(globusgsscredentialimpl);
		            }
		       } 
	        }
        }

        return globusgsscredentialimpl;
    }*/
    
    public static void copyFile(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
    
    public static void convertPEMTOPKCS12(String passphrase, String usercredP12Loc)
    throws Exception {


        CoGProperties props = CoGProperties.getDefault();

        X509Certificate userCert =  CertificateLoadUtil.loadCertificate(props.getUserCertFile());

        /** CERLANE: To support Openssl 1.0.0 encrypted pem keys **/
        FileInputStream in = new FileInputStream(props.getUserKeyFile());
        byte[] bytes = Util.streamToBytes(in);
        PKCS8Key key = new PKCS8Key(bytes, passphrase.toCharArray());
        byte[] decrypted = key.getDecryptedBytes();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec( decrypted );

        PrivateKey userKey = null;
        if ( key.isDSA() ){
                userKey = KeyFactory.getInstance( "DSA" ).generatePrivate( spec );
        }
        else if ( key.isRSA() ){
                userKey = KeyFactory.getInstance( "RSA" ).generatePrivate( spec );
        }
        /** CERLANE: End of Openssl support **/
        
        // Put them into a PKCS12 keystore and write it to a byte[]
        
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, passphrase.toCharArray());
        
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(passphrase.toCharArray());
        Certificate[] certChain = new Certificate[]{ userCert };
        KeyStore.PrivateKeyEntry pkEntry = new KeyStore.PrivateKeyEntry(userKey, certChain);
        ks.setEntry("keypair", pkEntry, protParam);
        FileOutputStream fos = new FileOutputStream(usercredP12Loc);
        ks.store(fos, passphrase.toCharArray());
        fos.close();
        Chmod(false, "600", usercredP12Loc);
    }
    
    public static X509Credential createProxy(String passphrase, GSIConstants.CertificateType proxyType, int lifetimeHours)
    throws Exception {


        CoGProperties props = CoGProperties.getDefault();

        X509Certificate userCert =  CertificateLoadUtil.loadCertificate(props.getUserCertFile());

        /** CERLANE: To support Openssl 1.0.0 encrypted pem keys **/
        /*X509Certificate userCert = CertUtil.loadCertificate(props.getUserCertFile());

             OpenSSLKey key =
                 new BouncyCastleOpenSSLKey(props.getUserKeyFile());

             if (key.isEncrypted()) {
                 try {
                     key.decrypt(pwd);
                 } catch(GeneralSecurityException e) {
                     throw new Exception("Wrong password or other security error");
                 }
             }

             PrivateKey userKey = key.getPrivateKey();*/

        FileInputStream in = new FileInputStream(props.getUserKeyFile());
        byte[] bytes = Util.streamToBytes(in);
        PKCS8Key key = new PKCS8Key(bytes, passphrase.toCharArray());
        byte[] decrypted = key.getDecryptedBytes();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec( decrypted );

        PrivateKey userKey = null;
        if ( key.isDSA() ){
                userKey = KeyFactory.getInstance( "DSA" ).generatePrivate( spec );
        }
        else if ( key.isRSA() ){
                userKey = KeyFactory.getInstance( "RSA" ).generatePrivate( spec );
        }
        /** CERLANE: End of Openssl support **/
    	BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

    	int bits = org.globus.myproxy.MyProxy.DEFAULT_KEYBITS;//props.getProxyStrength();

    	return factory.createCredential(new X509Certificate[] {userCert},
    				userKey,
    				bits,
    				lifetimeHours*3600,
    				proxyType);
    	
    }

   /*public static GlobusCredential createProxy(String pwd, int proxyType, int lifetimeHours)
	throws Exception {

	   CoGProperties props = CoGProperties.getDefault();

	   X509Certificate userCert = CertUtil.loadCertificate(props.getUserCertFile());

	   /** CERLANE: To support Openssl 1.0.0 encrypted pem keys **/
	   /*X509Certificate userCert = CertUtil.loadCertificate(props.getUserCertFile());

		OpenSSLKey key =
		    new BouncyCastleOpenSSLKey(props.getUserKeyFile());

		if (key.isEncrypted()) {
		    try {
			key.decrypt(pwd);
		    } catch(GeneralSecurityException e) {
			throw new Exception("Wrong password or other security error");
		    }
		}

		PrivateKey userKey = key.getPrivateKey();*/

	  /* FileInputStream in = new FileInputStream(props.getUserKeyFile());
	   byte[] bytes = Util.streamToBytes(in);
	   PKCS8Key key = new PKCS8Key(bytes, pwd.toCharArray());
	   byte[] decrypted = key.getDecryptedBytes();

	   PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec( decrypted );

	   PrivateKey userKey = null;
	   if ( key.isDSA() ){
		   userKey = KeyFactory.getInstance( "DSA" ).generatePrivate( spec );
	   }
	   else if ( key.isRSA() ){
		   userKey = KeyFactory.getInstance( "RSA" ).generatePrivate( spec );
	   }
	   /** CERLANE: End of Openssl support **/

	   /*BouncyCastleCertProcessingFactory factory =
		   BouncyCastleCertProcessingFactory.getDefault();

	   return factory.createCredential(new X509Certificate[] {userCert},
			   userKey,
			   props.getProxyStrength(),
			   lifetimeHours * 3600,
			   proxyType,
			   (X509ExtensionSet)null);
   }*/

    public static GSSCredential createCredentialFromPKCS12(SshConnectionProperties properties, GSIConstants.CertificateType proxyType, int lifetimeHours, StringBuffer stringbufferCredentialFile, StringBuffer stringbufferPassphrase){
    	GSSCredential cred = null;
    	X509Credential proxy = null;
    	String  passphrase = stringbufferPassphrase.toString();
    	
    	KeyStore store;
    	File keyfile = new File(stringbufferCredentialFile.toString());
    	Security.addProvider(new BouncyCastleProvider());
    	try {
			store = KeyStore.getInstance("PKCS12", "BC");
			FileInputStream in = new FileInputStream(keyfile);
			store.load(in, passphrase.toCharArray());
			
			Enumeration <String>enumeration = store.aliases();
	    	Key key = null;
	    	java.security.cert.Certificate cert = null;
	    	if(!enumeration.hasMoreElements()) {    		
	    		JOptionPane.showMessageDialog(properties.getWindow(),
	    				"GSI Exception: Could not access your certificate: No certificates found in file '"+stringbufferCredentialFile.toString()+"'", 
	    				"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
	    		return null;
	    	}
	    	else {

	    		while (enumeration.hasMoreElements()){
	    			String alias = (String)enumeration.nextElement();

	    			key = store.getKey(alias,passphrase.toCharArray());
	    			if (key != null && (key instanceof PrivateKey)) {
	    				cert = store.getCertificate(alias);
	    				break;
	    			}
	    		}
	    	}

	    	if(!(cert instanceof X509Certificate)) {
	    		JOptionPane.showMessageDialog(properties.getWindow(),
	    				"GSI Exception: Could not access your certificate: bad certificate type", 
	    				"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
	    		return null;
	    	}
	    	if(!(key instanceof PrivateKey)) {
	    		JOptionPane.showMessageDialog(properties.getWindow(),
	    				"GSI Exception: Could not access your certificate: bad key type", 
	    				"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
	    		return null;
	    	}

	    	BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

	    	try {
	    		int bits = org.globus.myproxy.MyProxy.DEFAULT_KEYBITS;
	    		proxy = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
	    				(PrivateKey)key,
	    				bits,
	    				lifetimeHours*3600,
	    				proxyType);

	    		cred = new GlobusGSSCredentialImpl(proxy, GSSCredential.INITIATE_ONLY);

	    	} catch (Exception ex) {
	    		JOptionPane.showMessageDialog(properties.getWindow(),
	    				"Failed to create a proxy:" +  ex.getMessage(), 
	    				"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
	    		return null;
	    	}
	    	in.close();
		} catch (KeyStoreException e) {
			JOptionPane.showMessageDialog(properties.getWindow(),
					"Unable to get an instance type \"PKCS12\" in BouncyCastleProvider.\n"+ e.getMessage(), 
					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (NoSuchProviderException e) {
			JOptionPane.showMessageDialog(properties.getWindow(),
					"No such provider \"PKCS12\".\n"+ e.getMessage(), 
					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			return null;			
    	}catch(IOException ioe) {
    		if( ioe.getMessage().indexOf("Illegal key size")>=0) {
    			JOptionPane.showMessageDialog(properties.getWindow(),
    					"GSI Exception: To use this PKCS#12 file you need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files.",
    					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    			return null;   		
    		}
    		else{
    			JOptionPane.showMessageDialog(properties.getWindow(),
    					"Wrong password or other security error", 
    					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
    			return null;
    		}
    	} catch (NoSuchAlgorithmException e) {
    		JOptionPane.showMessageDialog(properties.getWindow(),
					"No Such Algorithm Error. \n" + e.getMessage(), 
					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (CertificateException e) {
			JOptionPane.showMessageDialog(properties.getWindow(),
					"Certificate Exception in PKCS12 keystore. \n" + e.getMessage(), 
					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (UnrecoverableKeyException e) {
			JOptionPane.showMessageDialog(properties.getWindow(),
					"Unable to recover key with passphrase from PKCS12 keystore.\n" + e.getMessage(), 
					"GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			return null;
		}

    	
    	return cred;

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
  /* public static GSSCredential createCredentialFromPKCS12(SshConnectionProperties properties, int proxyType, int lifetimeHours, StringBuffer stringbufferCredentialFile, StringBuffer stringbufferPassphrase){
	   GSSCredential gsscredential = null;
	   CoGProperties cogproperties = CoGProperties.getDefault();
	   
	   try {
		   String  passphrase = stringbufferPassphrase.toString();
		   File keyfile = new File(stringbufferCredentialFile.toString());
		   Security.addProvider(new BouncyCastleProvider());
		   KeyStore store = KeyStore.getInstance("PKCS12", "BC");
		   FileInputStream in = new FileInputStream(keyfile);
		   store.load(in, passphrase.toCharArray());
		   // get the pkcs12 store entries.
		   Enumeration e = store.aliases();

		
		   Key key = null;
		   java.security.cert.Certificate cert = null;
		   

		   // filter the store's contents and select only valid user
		   // x509 certs that have an associated key !                     
		   List<String> allValidX509CertAliases = new java.util.ArrayList<String>(0);
		   while (e.hasMoreElements()){
			   String alias = (String)e.nextElement();
			   // get the associated key
			   key = store.getKey(alias, passphrase.toCharArray());
			   if (key != null && (key instanceof PrivateKey)) {
				   allValidX509CertAliases.add(alias);
			   }
		   }

		   if(allValidX509CertAliases.size() == 0){
			   JOptionPane.showMessageDialog(properties.getWindow(),
					   "No valid X509 user certificate found: no valid certificates found in file.",
					   "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			   return null;
		   }
		   // use the first alias as the defauilt.
		   String selected = allValidX509CertAliases.get(0);
		   if(allValidX509CertAliases.size() > 1){
			   // if there is more than one user x509 cert entry (which
			   // is perfectly valid), than let user select which to use).
			   // show a dialog so that the user can select their [key/cert] alias
			   // as there may be many entries.
			   selected = (String) JOptionPane.showInputDialog(
					   properties.getWindow(),
					   "Select your user certificate",
					   "Keystore Selection Dialog",
					   JOptionPane.PLAIN_MESSAGE,
					   null,
					   allValidX509CertAliases.toArray(),
					   allValidX509CertAliases.toArray()[0]);
		   }
		   // user cancelled the alias selection, so just continue
		   // with the next iteration. 
		   if (selected == null) {
			   return null;
		   }
		   // ok, now have the selected x509 cert alias that also
		   // has an accompanying private key.
		   cert = store.getCertificate(selected);
		   key = store.getKey(selected, passphrase.toCharArray());

		   if(!(cert instanceof X509Certificate)) {
			   JOptionPane.showMessageDialog(properties.getWindow(),
					   "Invalid certificate (X509Certificate required).", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			   return null;
		   }
		   if(!(key instanceof PrivateKey)) {
			   JOptionPane.showMessageDialog(properties.getWindow(),
					   "Invalid key type.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
			   return null;
		   }

		   BouncyCastleCertProcessingFactory factory =
			   BouncyCastleCertProcessingFactory.getDefault();


		   GlobusCredential globuscredential = factory.createCredential(new X509Certificate[] {(X509Certificate)cert},
				   (PrivateKey)key,
				   cogproperties.getProxyStrength(),
				   lifetimeHours * 3600,
				   proxyType,
				   (X509ExtensionSet)null);


		   if(globuscredential != null)
		   {
			   if(SAVE_PKCS12_PROXY) {
				   ProxyHelper.saveProxy(globuscredential, properties);
			   }
			   try {
				   globuscredential.verify();
				   gsscredential = new GlobusGSSCredentialImpl(globuscredential, 1);
			   } catch(Exception exception1) {
				   exception1.printStackTrace();
				   StringWriter stringwriter1 = new StringWriter();
				   exception1.printStackTrace(new PrintWriter(stringwriter1));
				   log.debug(stringwriter1);
				   if(exception1.getMessage().indexOf("Expired credentials")>=0) {
					   JOptionPane.showMessageDialog(properties.getWindow(), "Your certificate has expired, please renew your certificate or try another method for authentication.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
					   return null;
				   } else {
					   errorReport(properties.getWindow(), "Could not load your certificate", exception1);
					   return null;
				   }
			   }

		   }
		   return gsscredential;
	   } catch(java.io.FileNotFoundException exception) {
		   //exception.printStackTrace();
		   StringWriter stringwriter = new StringWriter();
		   exception.printStackTrace(new PrintWriter(stringwriter));
		   log.debug(stringwriter);
		
		   myProxyPrompt.setError("Certificate: could not find file");
		   errorReport(properties.getWindow(), "", exception);
		
		   // force another do while loop iteration
		   return null;
	   } catch(Exception exception) {
		   if(exception.getMessage().indexOf("Illegal key size")>=0) {
			   exception.printStackTrace();
			   StringWriter stringwriter = new StringWriter();
			   exception.printStackTrace(new PrintWriter(stringwriter));
			   log.debug(stringwriter);
			   errorReport(properties.getWindow(), "To use this PKCS#12 file you need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files\n (see http://java.sun.com/javase/downloads/index.jsp for Java 6 and http://java.sun.com/javase/downloads/index_jdk5.jsp for Java 5)", exception);
			   return null;
		   } else if(exception.getMessage().indexOf("wrong password")>=0) {
			   //exception.printStackTrace();
			   StringWriter stringwriter = new StringWriter();
			   exception.printStackTrace(new PrintWriter(stringwriter));
			   log.debug(stringwriter);
			   
			   myProxyPrompt.setError("Certificate: wrong password?");
			   errorReport(properties.getWindow(), "", exception);
			   return null;
		   } else {
			   exception.printStackTrace();
			   StringWriter stringwriter = new StringWriter();
			   exception.printStackTrace(new PrintWriter(stringwriter));
			   log.debug(stringwriter);
			   errorReport(properties.getWindow(), "Unknown problem while loading your certificate", exception);
			   return null;
		   }
	   } 
	   

   }*/

    private static Log log;
    private static GridProxyInitPrompt gridProxyInitPrompt;
    private static MyProxyPrompt myProxyPrompt;

    static
    {
        log = LogFactory.getLog(com.sshtools.j2ssh.authentication.UserGridCredential.class);
    }
    private static class DefaultPasswordFinder implements PasswordFinder {

        private final char [] password;

        private DefaultPasswordFinder(char [] password) {
            this.password = password;
        }

        @Override
        public char[] getPassword() {
            return Arrays.copyOf(password, password.length);
        }
    } 
  
}

/*
 *  SSHTools - Globus Online Tool
 *
 *  Copyright (C) 2013 Siew Hoon Leong (Cerlane)
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

import java.util.HashMap;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.globus.common.CoGProperties;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.myproxy.DestroyParams;
import org.ietf.jgss.GSSCredential;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.j2ssh.authentication.UserGridCredential;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.sshterm.SshTerminalPanel;
import com.sshtools.common.globusonlinetool.SetupPanel.GOSetupPanel;
import com.sshtools.common.globusonlinetool.CredentialHelper;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class Main
extends JFrame
implements ActionListener {
	//  Statics
	final static String ICON =
		"/com/sshtools/common/globusonlinetool/largego.png";

	/**  */
	final static int LOGIN_SETUP = 0;

	/**  */
	final static int BEGIN_TRANSFER = 1;

	/**  */
	final static int CHECK_TRANSFERS = 2;

	String userName = "";
	String hostName = "";
	int port = 0;
	int lifetime = 0;
	String certType= null;
	String dn = null;
	boolean voms = false;
	boolean autoProxyUpload = false;
	String uploadedproxyPassphrase = "";


	JButton close;
	JButton generate;
	GlobusOnlineToolPanel gotoolpanel;
	GOTaskPanel gotaskpanel;
	JComboBox action;

	private String passphrase = null; //new String(myproxypanel.getPassphrase()).trim();
	private String confirmPassphrase = null; //new String(myproxypanel.getConfirmPassphrase()).trim();

	private boolean toContinue = false;

	/**
	 * Creates a new Main object.
	 */
	public Main() {
		super("Globus Online Tool - Cloud GridFTP service");
		try {
			ConfigurationLoader.initialize(false);
		}
		catch (ConfigurationException ex) {
		}
		//Set the frame icon
		setIconImage(new ResourceIcon(this.getClass(), ICON).getImage());
		
		addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(java.awt.event.WindowEvent e) {
	        	//on exit, save files
	        	if (GOHelper.isAuthenticated()){
	        		GOHelper.writeGOLogFile();	 
		        	//Remove proxy from server if it is uploaded
		        	if (CredentialHelper.isProxyUploaded()){
		        		removeCredentialFromMyProxy();
		        	}
	        	}
		        if (GOHelper.getGoUserName()!=null && !GOHelper.getGoUserName().trim().equals("")){
					PreferencesStore.put(SshTerminalPanel.PREF_GO_USERNAME, GOHelper.getGoUserName());
				}
		        //System.exit(0);
	        }
	    });
		
		//Check if a proxy already exists and if the the proxy is valid.
		String existingProxyCertStr = "";
		boolean proxyValid = false;
		if (CredentialHelper.getExistingProxyLoation()!=null){
			existingProxyCertStr = CredentialHelper.getExistingProxyLoation();
			if (existingProxyCertStr!=null && !existingProxyCertStr.equals("")){
				proxyValid = true;
			}
		}
		
		//Go Username dialog	
		JLabel goUsernameLabel = new JLabel("Globus Online Username:");	
		String goUsernameStr = PreferencesStore.get(SshTerminalPanel.PREF_GO_USERNAME, ""); 
		XTextField goUsernameTextBox;
		if (goUsernameStr!=null){
			goUsernameTextBox = new XTextField(goUsernameStr, 20);
		}
		else{
			goUsernameTextBox = new XTextField(20);
		}
		
		if (proxyValid){			
			Object[] obj = {goUsernameLabel, goUsernameTextBox};
			int counter = 0;
			while(counter<3){
				int result = JOptionPane.showConfirmDialog(this, obj, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ICON) );
				if (result == JOptionPane.OK_OPTION) {	
					String goUserName = goUsernameTextBox.getText().trim();
					if (goUserName.equals("")){
						JOptionPane.showMessageDialog(this, "Cannot login to Globus Online without a valid username.", "Error: Globus Online username", JOptionPane.ERROR_MESSAGE);
					}
					else {
						GOHelper.setGoUserName(goUserName);
						if(GOHelper.credentialChecker()){
							toContinue = true;
							break;
						}
						else{
							JOptionPane.showMessageDialog(this, "Cannot login to Globus Online without a valid username.", "Error: Globus Online username", JOptionPane.ERROR_MESSAGE);
						}
					}
					counter--;
				}
				else{
					toContinue = false;
					break;
				}
			}
		}
		else{
			
			JPanel goSetupPanel = new JPanel();
			GOSetupPanel goLoginPanel = new GOSetupPanel();
			//loginPanel.add(goUsernameLabel);
			//loginPanel.add(goUsernameTextBox);
			
			goSetupPanel.add(goLoginPanel);
			JScrollPane scrollPane = new JScrollPane(goLoginPanel); 
			
			
			int counter = 0;
			while(counter<3){			
				int result = JOptionPane.showConfirmDialog(this,scrollPane,"Setting up for Globus Online",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
				if (result == JOptionPane.OK_OPTION) {	
					String goUserName = goLoginPanel.getGOUsername();
					if (goUserName.equals("")){
						JOptionPane.showMessageDialog(this, "Cannot login to Globus Online without a valid username.", "Error: Globus Online username", JOptionPane.ERROR_MESSAGE);
					}
					else {
						//Now check the authentication 
						if(authenticationHelper(goLoginPanel)){
							GOHelper.setGoUserName(goUserName);
							if(GOHelper.credentialChecker()){
								toContinue = true;
								break;
							}
							else{
								JOptionPane.showMessageDialog(this, "Cannot login to Globus Online without a valid username.", "Error: Globus Online username", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
				else{
					toContinue = false;
					break;
				}
			}
			
		}
		
		if (toContinue){
					
			//Just to update CA files
			try {
				String globusCertDir = System.getProperty("user.home")+File.separator+".globus"+File.separator+"certificates";
				if (!new File(globusCertDir).exists())
					UserGridCredential.checkCACertificates(CoGProperties.getDefault());
			} catch (IOException e1) {
				System.out.println("Failed to update CA certificates");
			}
			
			//Action ComboBox
			/*JPanel selectActionPanel = new JPanel(new GridBagLayout());    
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(0, 2, 4, 2);
			gbc.weightx = 0.0;
			action = new JComboBox(new String[] {
					"Login and Setup", "Begin Transfer", "Manage Transfers"                          
			});
			action.addActionListener(this);
			UIUtil.jGridBagAdd(selectActionPanel, action, gbc, GridBagConstraints.RELATIVE);
			gbc.weightx = 1.0;
			UIUtil.jGridBagAdd(selectActionPanel, new JLabel(), gbc,
					GridBagConstraints.REMAINDER);*/
			
			JPanel iconPanel = new JPanel(new GridBagLayout());    
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 2, 4, 2);
			gbc.weightx = 0.0;
			goUsernameLabel = new JLabel("GO Username: " + GOHelper.getGoUserName());
			goUsernameLabel.setFont(new Font("Serif", Font.BOLD, 15));
			UIUtil.jGridBagAdd(iconPanel, goUsernameLabel, gbc, GridBagConstraints.RELATIVE);
			gbc.weightx = 1.0;
			IconWrapperPanel northPanel = new IconWrapperPanel(new ResourceIcon(this.getClass(), ICON), iconPanel);
			northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			
				
			//globusonline panel
			gotoolpanel = new GlobusOnlineToolPanel();
	
			JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			centerPanel.add(gotoolpanel);
	
			//Task panel
			gotaskpanel = new GOTaskPanel();
			JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			southPanel.add(gotaskpanel);
	

	
			//  Wrap the whole thing in an empty border
			JPanel mainPanel = new JPanel(new BorderLayout());
			JScrollPane mainScroller = new JScrollPane(mainPanel);  
			mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));    
			mainPanel.add(northPanel, BorderLayout.NORTH);
			mainPanel.add(centerPanel, BorderLayout.CENTER);
			mainPanel.add(southPanel, BorderLayout.SOUTH);
	
			//  Build the main panel
			getContentPane().setLayout(new GridLayout(1, 1));
			getContentPane().add(mainScroller);
			
		}
		
		

	}
	
	
	public boolean authenticationHelper(GOSetupPanel goSetupPanel){
		final String localPassphrase = new String(goSetupPanel.getLocalPassphrase()).trim();
		userName = new String(goSetupPanel.getMyProxyUsername());
		hostName = new String(goSetupPanel.getMyProxyServer());
		port = goSetupPanel.getMyProxyPort();
		lifetime = goSetupPanel.getMyProxyLifetime();
		uploadedproxyPassphrase = new String(goSetupPanel.getUploadedMyProxyPassphrase());
		certType = goSetupPanel.getCertType();
		voms = goSetupPanel.getVOMSSupport();
		autoProxyUpload = goSetupPanel.getAutoProxyUpload();

		String pkcs12cert = goSetupPanel.getPKCS12Cert();
		String existingProxyCert = goSetupPanel.getExistingProxyCert();
		
		//MyProxy
		String myProxyServer = goSetupPanel.getMyProxyServer();
		int myProxyPort = goSetupPanel.getMyProxyPort();
		int myProxyLifetime = goSetupPanel.getMyProxyLifetime();
		String myProxyUsername = goSetupPanel.getMyProxyUsername();
		
		try {
			GSSCredential gsscredential = null;
			GSIConstants.CertificateType  proxyType = GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY; 
			GSSCredential vomscredential = null;
			
			if(certType.equals("Local PEM") || certType.equals("Local PKCS12")){
				gsscredential = createCredential(localPassphrase, proxyType, lifetime , pkcs12cert, existingProxyCert, false);
				if (voms && gsscredential!=null){
					vomscredential= createCredential(localPassphrase, proxyType, lifetime , pkcs12cert, existingProxyCert, true);
				}
			}
			else if (certType.equals("Retrieve from MyProxy Server")){
			    org.globus.myproxy.MyProxy myProxy = getMyProxy();
		        gsscredential = myProxy.get(null, userName, uploadedproxyPassphrase, lifetime*3600);
		        /*if (((GlobusGSSCredentialImpl)gsscredential).getGlobusCredential().getProxyType()==GSIConstants.GSI_2_PROXY){
		        	gsscredential = null;
		        	JOptionPane.showMessageDialog(this, "Your credential on MyProxy Server is a globus legacy proxy. Globus Online login service does not support this type proxy."+
		        			"\nPlease create a RFC compliant proxy using your \"Local PEM\" or \"Local PKCS12\" certificate."+
		        			"\nThe legacy proxy on MyProxy server can still be used by Globus Online to activate and manage transfer on resources that support legacy proxy, e.g. EGI resources.", "Error: Globus Online", JOptionPane.ERROR_MESSAGE);
				
		        	
		        }*/
		        //Save a copy
		        Map info = new HashMap();
				info.put("username", userName);
				info.put("passphrase", uploadedproxyPassphrase);
				info.put("server", hostName);
				CredentialHelper.setMyProxyInfo(info);
				
				if (gsscredential==null){
					JOptionPane.showMessageDialog(this, "Cannot retrieve valid/required credential from MyProxy Server", "Error: MyProxy Server", JOptionPane.ERROR_MESSAGE);
				}
			}
			if (gsscredential!=null){
				CredentialHelper.saveProxy(((GlobusGSSCredentialImpl)gsscredential).getX509Credential());
				if(autoProxyUpload){
					boolean ok = createConfirmPassphraseDialog();

					if (ok){
						
						/*InitParams initRequest = new InitParams();
						initRequest.setUserName(userName);
						initRequest.setLifetime(lifetime*3600);
						initRequest.setCredentialName(null);
						initRequest.setCredentialDescription(null);
						initRequest.setRenewer(null);
						initRequest.setRetriever(null);
						initRequest.setPassphrase(passphrase);	 */

						org.globus.myproxy.MyProxy myProxy = getMyProxy();
						if (vomscredential!=null)
							myProxy.put(vomscredential, userName, passphrase, lifetime*3600);
							//myProxy.put(vomscredential, initRequest);
						else
							myProxy.put(gsscredential, userName, passphrase, lifetime*3600);
							//myProxy.put(gsscredential, initRequest);
						
						JOptionPane.showMessageDialog(this, "Your credential has been uploaded to "+ hostName +" under \nUsername: "+ userName + "\nWhen you exit normally from Globus Online Tool, this proxy will automatically be removed from the server.",
								"MyProxy status", JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
						
						Map info = new HashMap();
						info.put("username", userName);
						info.put("passphrase", passphrase);
						info.put("server", hostName);
						CredentialHelper.setMyProxyInfo(info);
						CredentialHelper.setProxyUploaded(true);
					}
				}
				
				return true;
			}
		}catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"MyProxy Error", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
		
		
	public void removeCredentialFromMyProxy(){
		
		
		GSSCredential gsscredential = null;
		int proxyType = GSIConstants.GSI_4_IMPERSONATION_PROXY; 
		
		try {
			gsscredential = CredentialHelper.loadExistingProxy();
		
			if (gsscredential!=null ){
				
				DestroyParams destroyRequest = new DestroyParams();
				destroyRequest.setUserName(userName);
				destroyRequest.setCredentialName(null);
				destroyRequest.setPassphrase("DUMMY-PASSPHRASE");
		
				try {
					org.globus.myproxy.MyProxy myProxy = getMyProxy();
					myProxy.destroy(gsscredential, destroyRequest);
					JOptionPane.showMessageDialog(this, "Proxy is successfully removed/destroyed.",
							"MyProxy status", JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
				} catch(Exception e) {
					if (e.getMessage().contains("Credentials do not exist")){
						JOptionPane.showMessageDialog(this,"No credential found for user '" + userName + "'",
								"MyProxy Error", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(this, e.getMessage(),
								"MyProxy Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"MyProxy Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 *
	 *
	 * @param evt
	 */
	public void actionPerformed(ActionEvent evt) {
	

	}
	/**
	 *
	 *
	 * @return
	 */
	public int getAction() {
		return action.getSelectedIndex();
	}
	private GSSCredential createCredential(String localPassphrase, GSIConstants.CertificateType  proxyType, int lifetime, String pkcs12cert, String existingProxyCert, boolean voms) throws Exception{

		GSSCredential gsscredential = null;
		X509Credential proxy = null;
		boolean error = false;
		
		//To check if voms is turned on. 
    	if (voms){    		
	    
	        JLabel question = new JLabel("Authentication with VOs before logging in?");
	        JLabel proxyTypeLB = new JLabel("Proxy Type: ");
	        JComboBox proxyTypeCB = new JComboBox(new String[] {
	        		"Legacy", "RFC Impersonation"                         
	    			});	
	        String cur = PreferencesStore.get(SshTerminalPanel.PREF_VOMS_PROXYTYPE, Integer.toString(GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY.getCode()));
	        
	    	if (cur.equals(Integer.toString(GSIConstants.CertificateType.GSI_2_PROXY.getCode())))
	    		proxyTypeCB.setSelectedIndex(0);
	    	else{
	    		proxyTypeCB.setSelectedIndex(1);
	    	}
	    	
	    	Object [] obj = {question, proxyTypeLB, proxyTypeCB};
	    	int result = JOptionPane.showConfirmDialog(null, obj, "Enable VOMS ?", JOptionPane.YES_NO_OPTION);
	    	if(result == JOptionPane.YES_OPTION){
	    		int index = proxyTypeCB.getSelectedIndex();
	    		if (index == 0){
	    			proxyType = GSIConstants.CertificateType.GSI_2_PROXY;
	    		}
	    		else if (index == 1){
	    			proxyType = GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY;
	    		}
	    	}
	    	
    	}
		
		if (certType.equals("Local PEM")){
			try{
				proxy = UserGridCredential.createProxy(localPassphrase, proxyType, lifetime);		
				
			}catch(Exception ex) {
				error = true;
				JOptionPane.showMessageDialog(this, "Cannot find your PEM certificates in {home directory}/.globus/ or incorrect passphrase.",
						"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
			}
		}
		else if (certType.equals("Local PKCS12")){
			try{
				proxy = CredentialHelper.createProxyFromPKCS12(localPassphrase, proxyType, lifetime, pkcs12cert);
			}catch(Exception ex) {
				error = true;
				JOptionPane.showMessageDialog(null, ex.getMessage(),
						"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
			}
		}
		else if (certType.equals("Existing proxy")){
			try{
				proxy = CredentialHelper.retrieveExistingProxy(existingProxyCert);
				/*if (voms){
					if (globuscred.getProxyType() != org.globus.gsi.GSIConstants.GSI_2_PROXY){
						error = true;
						JOptionPane.showMessageDialog(null, "VOMS enabled proxy can only be created from a Globus full Legacy proxy. Please choose another 'Certificate Format'.",
						"MyProxy Error", JOptionPane.ERROR_MESSAGE);
						globuscred = null;
					}
				}*/
				
			}catch(Exception ex) {
				error = true;
				JOptionPane.showMessageDialog(null, ex.getMessage(),
						"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
			}
			
		}
		if (proxy!=null){
			try {
				
					//Cerlane: To support voms enabled proxy of type 4
		            //globuscred.verify();
		            if(proxy.getTimeLeft()<=0){
		            	JOptionPane.showMessageDialog(this, "Credentials have expired.",
								"MyProxy Error", JOptionPane.ERROR_MESSAGE);
		            }
		            //End Cerlane
						
				gsscredential = new GlobusGSSCredentialImpl(proxy, 1);
			} catch(Exception exception1) {		
				error = true;
				if(exception1.getMessage().indexOf("Expired credentials")>=0) {
					JOptionPane.showMessageDialog(this, "Credentials have expired.",
							"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
				} else {
					JOptionPane.showMessageDialog(this, "Could not load your certificate.",
							"MyProxy Error", JOptionPane.ERROR_MESSAGE); 

				}
			}
		}
		
		
		if (!error && voms){
	    	
	            // a defensive copy of gsscredential is returned 
	            GSSCredential defensivecopy = UserGridCredential.vomsEnable(gsscredential);
	            if(defensivecopy != null){
	               gsscredential = defensivecopy;
	            } 
	        
    	}
		return gsscredential;
	}
	private boolean createConfirmPassphraseDialog (){
		int counter = 0;
		while(counter<3){
			try{
				JLabel serverInfoLabel = new JLabel("MyProxy server: myproxy.lrz.de");	
				JLabel lmyProxyPassphrase = new JLabel("Upload Passphrase:");		        
				JPasswordField myProxyPassphrase =  new JPasswordField(20);
				JLabel lmyProxyPassphraseConfirm = new JLabel("Comfirm Upload Passphrase:");
				JPasswordField myProxyPassphraseConfirm =  new JPasswordField(20);
				Object[] obj = {serverInfoLabel, lmyProxyPassphrase, myProxyPassphrase, lmyProxyPassphraseConfirm, myProxyPassphraseConfirm};


				int result = JOptionPane.showConfirmDialog(this, obj, "Upload proxy to MyProxy Server", JOptionPane.OK_CANCEL_OPTION);

				if (result == JOptionPane.OK_OPTION) {
					passphrase = new String(myProxyPassphrase.getPassword()).trim();
					confirmPassphrase = new String(myProxyPassphraseConfirm.getPassword()).trim();
					if (!passphrase.equals(confirmPassphrase)){
						throw new Exception("Passphrase and Confirm Passphrase do not match. Please try again.");
					}
					else{
						return true;
					}
				}
				else{				 
					break;
				}

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(),
						"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
			}
			counter++;
		}
		return false;
	}

	
	private org.globus.myproxy.MyProxy getMyProxy() {
		org.globus.myproxy.MyProxy myProxy = new org.globus.myproxy.MyProxy(this.hostName, 
				this.port); 
		if (this.dn != null) {
			myProxy.setAuthorization(new IdentityAuthorization(this.dn));
		}

		return myProxy;
	}

	/**
	 *
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		}
		
		Main main = new Main();
		/*main.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});*/
		main.pack();
		UIUtil.positionComponent(SwingConstants.CENTER, main);
		main.setVisible(true);
	}

	public boolean isToContinue() {
		return this.toContinue;
	}




}

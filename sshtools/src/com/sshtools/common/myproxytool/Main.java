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

import java.util.ArrayList;
import java.util.Date;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.globus.common.CoGProperties;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.DestroyParams;
import org.globus.myproxy.GetParams;
import org.globus.myproxy.InfoParams;
import org.globus.myproxy.InitParams;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.j2ssh.authentication.UserGridCredential;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.sshterm.SshTerminalPanel;

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
		"/com/sshtools/common/myproxytool/largemyproxy.png";

	/**  */
	final static int CREATE_STORE_PROXY = 0;

	/**  */
	final static int REMOVE_DESTROY_PROXY = 1;

	/**  */
	final static int PROXY_INFORMATION = 2;
	
	/**  */
	//final static int DOWNLOAD_PROXY = 3;
	
	String userName = "";
	String hostName = "";
	int port = 0;
	int lifetime = 0;
	String certType= null;
	String dn = null;
	boolean voms = false;


	JButton close;
	JButton generate;
	MyProxyToolPanel myproxypanel;
	JComboBox action;

	private String passphrase = null; //new String(myproxypanel.getPassphrase()).trim();
	private String confirmPassphrase = null; //new String(myproxypanel.getConfirmPassphrase()).trim();


	/**
	 * Creates a new Main object.
	 */
	public Main() {
		super("MyProxy Tool");
		try {
			ConfigurationLoader.initialize(false);
		}
		catch (ConfigurationException ex) {
		}

		//  Set the frame icon
		setIconImage(new ResourceIcon(this.getClass(), ICON).getImage());
		
		//Things to do when window is closed
		addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(java.awt.event.WindowEvent e) {
	        	if (myproxypanel.getPKCS12Cert()!=null && !myproxypanel.getPKCS12Cert().trim().equals("")){
					PreferencesStore.put(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, myproxypanel.getPKCS12Cert().trim());
				}
				if (myproxypanel.getMyProxyServer()!=null && !myproxypanel.getMyProxyServer().trim().equals("")){
					PreferencesStore.put(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, myproxypanel.getMyProxyServer().trim());
				}
				if (myproxypanel.getPort()!=0){
					PreferencesStore.put(SshTerminalPanel.PREF_MYPROXY_PORT, myproxypanel.getPort()+"");
				}
				if (myproxypanel.getUsername()!=null && !myproxypanel.getUsername().trim().equals("")){
					PreferencesStore.put(SshTerminalPanel.PREF_MYPROXY_UNAME, myproxypanel.getUsername().trim());
				}       
				dispose();
	           // System.exit(0);
	        }
	    });

		//Just to update CA files
		try {
			String globusCertDir = System.getProperty("user.home")+File.separator+".globus"+File.separator+"certificates";
			if (!new File(globusCertDir).exists())
				UserGridCredential.checkCACertificates(CoGProperties.getDefault());
		} catch (IOException e1) {
			System.out.println("Failed to update CA certificates");
		}
		
		//Action ComboBox
		JPanel selectActionPanel = new JPanel(new GridBagLayout());    
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 2, 4, 2);
		gbc.weightx = 0.0;
		action = new JComboBox(new String[] {
				"Create and upload proxy", "Remove or destroy proxy", "Retrieve proxy information"/*, "Download proxy"   */                       
		});
		action.addActionListener(this);
		UIUtil.jGridBagAdd(selectActionPanel, action, gbc, GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(selectActionPanel, new JLabel(), gbc,
				GridBagConstraints.REMAINDER);

		/* JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    northPanel.add(selectActionPanel);*/
		IconWrapperPanel northPanel = new IconWrapperPanel(new ResourceIcon(this.getClass(), ICON), selectActionPanel);
		northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		//myproxy panel
		myproxypanel = new MyProxyToolPanel();

		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		centerPanel.add(myproxypanel);
		/*IconWrapperPanel centerPanel = new IconWrapperPanel(new ResourceIcon(this.getClass(), ICON), myproxypanel);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));*/

		//  Button panel
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		//GridBagConstraints 
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(6, 6, 0, 0);
		gbc.weighty = 1.0;
		generate = new JButton("Create and Store");
		generate.addActionListener(this);
		generate.setMnemonic('u');
		this.getRootPane().setDefaultButton(generate);
		UIUtil.jGridBagAdd(buttonPanel, generate, gbc,
				GridBagConstraints.RELATIVE);
		close = new JButton("Close");
		close.addActionListener(this);
		close.setMnemonic('c');
		UIUtil.jGridBagAdd(buttonPanel, close, gbc, GridBagConstraints.REMAINDER);

		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		southPanel.add(buttonPanel);

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

	/**
	 *
	 *
	 * @param evt
	 */
	public void actionPerformed(ActionEvent evt) {
		//  Close
		if (evt.getSource() == close) {
			if (myproxypanel.getPKCS12Cert()!=null && !myproxypanel.getPKCS12Cert().trim().equals("")){
				PreferencesStore.put(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, myproxypanel.getPKCS12Cert().trim());
			}
			if (myproxypanel.getMyProxyServer()!=null && !myproxypanel.getMyProxyServer().trim().equals("")){
				PreferencesStore.put(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, myproxypanel.getMyProxyServer().trim());
			}
			if (myproxypanel.getPort()!=0){
				PreferencesStore.put(SshTerminalPanel.PREF_MYPROXY_PORT, myproxypanel.getPort()+"");
			}
			if (myproxypanel.getUsername()!=null && !myproxypanel.getUsername().trim().equals("")){
				PreferencesStore.put(SshTerminalPanel.PREF_MYPROXY_UNAME, myproxypanel.getUsername().trim());
			}
			dispose();

			return;
		}


		final String localPassphrase = new String(myproxypanel.getLocalPassphrase()).trim();
		userName = new String(myproxypanel.getUsername());
		hostName = new String(myproxypanel.getMyProxyServer());
		port = myproxypanel.getPort();
		lifetime = myproxypanel.getLifetime();
		certType = myproxypanel.getCertType();
		voms = myproxypanel.getVOMSSupport();

		String pkcs12cert = myproxypanel.getPKCS12Cert();
		String existingProxyCert = myproxypanel.getExistingProxyCert();

		try {
			GSSCredential gsscredential = null;
			//int proxyType = GSIConstants.GSI_4_IMPERSONATION_PROXY;  
			GSIConstants.CertificateType  proxyType = GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY; 
			
			if (getAction() == CREATE_STORE_PROXY){
				myproxypanel.triggerLocalCertificatePanel(true);
				if (!certType.equals("Existing proxy")){
					myproxypanel.triggerLifetime(true);
				}
				generate.setText("Create and Store");
				if (evt.getSource() == generate){	 
					gsscredential = createCredential(localPassphrase, proxyType, lifetime , pkcs12cert, existingProxyCert);
					if (gsscredential!=null){					        
						boolean ok = createConfirmPassphraseDialog();
						MyProxy myProxy = getMyProxy();
						if (ok){							
							myProxy.put(gsscredential, userName, passphrase, lifetime*3600);
													
							//This is to support getting VOMS extension from myproxy server and then reloading
							if (myproxypanel.getVOMSRemoteSupport()){
								String voName = createSimpleVOMSDialog();
								if (voName!=null){
									try {
										GetParams getRequest = new GetParams();
										getRequest.setUserName(userName);
										getRequest.setLifetime(lifetime*3600);
										getRequest.setPassphrase(passphrase);
										ArrayList <String>vonameList = new ArrayList<String>();
										vonameList.add(voName);
										getRequest.setVoname(vonameList);
									
										GSSCredential vomGsscredential = myProxy.get(null, getRequest);	 
										//System.out.println("[MyProxy Tool] Successfully download a voms enabled proxy");
										myProxy.put(vomGsscredential, userName, passphrase, lifetime*3600);
										
									} catch (MyProxyException e) {
										e.printStackTrace();
									}
								}
							}
							JOptionPane.showMessageDialog(this, "Proxy is successfully created and stored.",
									"MyProxy status", JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
						}
					}
				}
				
			}
			else if (getAction() == REMOVE_DESTROY_PROXY){
				generate.setText("Destroy");
				myproxypanel.triggerLifetime(false);
				gsscredential = CredentialHelper.loadExistingProxy();
				if (gsscredential!=null){
					myproxypanel.triggerLocalCertificatePanel(false);
				}
				else{
					myproxypanel.triggerLocalCertificatePanel(true);
				}
				if (evt.getSource() == generate){
					if (gsscredential==null){
						gsscredential = createCredential(localPassphrase, proxyType, lifetime , pkcs12cert, existingProxyCert);
					}
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
			}
			else if (getAction() == PROXY_INFORMATION){
				generate.setText("Retrieve");
				myproxypanel.triggerLifetime(false);
				gsscredential = CredentialHelper.loadExistingProxy();
				if (gsscredential!=null){
					myproxypanel.triggerLocalCertificatePanel(false);
				}
				else{
					myproxypanel.triggerLocalCertificatePanel(true);
				}
				if (evt.getSource() == generate){
					if (gsscredential==null){
						gsscredential = createCredential(localPassphrase, proxyType, lifetime , pkcs12cert, existingProxyCert);
					}

					InfoParams infoRequest = new InfoParams();
					infoRequest.setUserName(userName);
					infoRequest.setPassphrase("DUMMY-PASSPHRASE");

					String tmp;
					String proxyInfo = "";
					try{
						org.globus.myproxy.MyProxy myProxy = getMyProxy();
						CredentialInfo[] info = myProxy.info(gsscredential, infoRequest);
						proxyInfo += "From MyProxy server: " + myProxy.getHost() + "\n";
						proxyInfo += "Owner: " + info[0].getOwner() + "\n";
						proxyInfo += "Username: " + userName + "\n";
						for (int i=0;i<info.length;i++) {
							tmp = info[i].getName();
							proxyInfo += (tmp == null) ? "default:" : tmp +":\n";    						
							proxyInfo += "\tStart Time  : " + 
							new Date(info[i].getStartTime()) + "\n";
							proxyInfo += "\tEnd Time    : " + 
							new Date(info[i].getEndTime()) + "\n";

							long now = System.currentTimeMillis();
							if (info[i].getEndTime() > now) {
								proxyInfo += "\tTime left   : " +
								Util.formatTimeSec((info[i].getEndTime() - now)/1000) + "\n";
							} else {
								proxyInfo += "\tTime left   : expired\n";
							}

							tmp = info[i].getRetrievers();
							if (tmp != null) {
								proxyInfo += "\tRetrievers  : "+tmp + "\n";
							}
							tmp = info[i].getRenewers();
							if (tmp != null) {
								proxyInfo += "\tRenewers    : "+tmp + "\n";
							}
							tmp = info[i].getDescription();
							if (tmp != null) {
								proxyInfo += "\tDescription : "+tmp + "\n";
							}
							
						}

						JLabel lproxyInfo = new JLabel("Credential Information:");	
						JTextArea existingproxyInfo = new JTextArea(5, 20);
						existingproxyInfo.append(proxyInfo);
						JScrollPane scrollPane = new JScrollPane(existingproxyInfo); 
						existingproxyInfo.setEditable(false);
											
						
						Object [] obj = {lproxyInfo, existingproxyInfo};
						JOptionPane.showMessageDialog(this, obj,
								"MyProxy credential information", JOptionPane.INFORMATION_MESSAGE);

					}catch (Exception e) {
						JOptionPane.showMessageDialog(this, e.getMessage(),
								"MyProxy Error", JOptionPane.ERROR_MESSAGE);
					}
				}

			} /*else if (getAction() == DOWNLOAD_PROXY){
				generate.setText("Download");
				myproxypanel.triggerLocalCertificatePanel(false);
				
			}*/
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
		}


	}
	/**
	 *
	 *
	 * @return
	 */
	public int getAction() {
		return action.getSelectedIndex();
	}
	private GSSCredential createCredential(String localPassphrase, GSIConstants.CertificateType proxyType, int lifetime, String pkcs12cert, String existingProxyCert) throws Exception{

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
		
		if (certType.equals("PEM")){
			try{
				proxy = UserGridCredential.createProxy(localPassphrase, proxyType, lifetime);			
			}catch(Exception ex) {
				error = true;
				JOptionPane.showMessageDialog(this, "Cannot find your PEM certificates in {home directory}/.globus/ or incorrect passphrase.",
						"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
			}
		}
		else if (certType.equals("PKCS12")){
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
				proxy.verify();
				gsscredential =  new GlobusGSSCredentialImpl(proxy, GSSCredential.INITIATE_ONLY);
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
				JLabel lmyProxyPassphrase = new JLabel("Upload Passphrase:");		        
				JPasswordField myProxyPassphrase =  new JPasswordField(20);
				JLabel lmyProxyPassphraseConfirm = new JLabel("Comfirm Upload Passphrase:");
				JPasswordField myProxyPassphraseConfirm =  new JPasswordField(20);
				Object[] obj = {lmyProxyPassphrase, myProxyPassphrase, lmyProxyPassphraseConfirm, myProxyPassphraseConfirm};


				int result = JOptionPane.showConfirmDialog(this, obj, "Please input passphrase for storing your proxy", JOptionPane.OK_CANCEL_OPTION);

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

	private String createSimpleVOMSDialog(){
		int counter = 0;
		while(counter<3){
			JLabel lvoms = new JLabel("VO:");
			JTextField tbVOName = new JTextField();
			Object[] obj = {lvoms, tbVOName};
			int result = JOptionPane.showConfirmDialog(this, obj, "Please enter VO name", JOptionPane.OK_CANCEL_OPTION);
			
			if (result == JOptionPane.OK_OPTION) {
				String voName = tbVOName.getText().trim();
				if (voName!=null && !voName.equals("")){
					return voName;
				}
			}
			counter ++;
		}
		return null;
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


}

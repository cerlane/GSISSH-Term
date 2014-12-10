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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.globus.gsi.X509Credential;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.j2ssh.authentication.ExampleFileFilter;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.sshterm.SshTerminalPanel;

public class SetupPanel {
	public static class GOSetupPanel extends JPanel implements DocumentListener, ActionListener {
		
	
		private JPanel createProxyCertPanel;
		private JButton browsePKCS12Cert;
		private JButton browseExistingProxyCert;
		private XTextField pkcs12cert;
		private XTextField existingProxyCert;
		private XTextField goUsername;
		private JComboBox certType;
		private JLabel certInfoLabel;
		private JLabel pkcs12CertLabel;
		private JLabel existingProxyCertLabel;
		private JLabel localPassphraseLabel;	
		private JLabel certTypeLabel;
		private JLabel vomsLabel;
		private JCheckBox vomsSupport;
		private JPasswordField localPassphrase;
		private JPasswordField passphrase;
		private JPasswordField confirmPassphrase;
		private JPasswordField myproxyUploadedPassphrase;
		
		
		
		private JPanel myproxyPanel;
		private JLabel myproxyServerLabel;
		private JLabel myproxyServerPortLabel;
		private JLabel myproxyServerLifetimeLabel;
		private JLabel myproxyUsernameLabel;
		private JLabel myproxyPassphraseLabel;
		private XTextField myproxyUsername;
		private JComboBox myproxyServer;
		private NumericTextField myproxyPort;
		private NumericTextField myproxyLifetime;
		
		private JPanel optionalPanel;
		private JLabel autoProxyUploadLabel;
		private JCheckBox autoProxyUpload;
	
		private static int defaultLifetime = 168;
		private static Map myProxyInfoMap = null;
	
	
		private String [] infoText = {"Assumes the PEM certficates 'usercert.pem' and 'userkey.pem' are in {home.directory}/.globus directory.",
		"Assumes the lifetime of uploaded proxy is the same as the life of the existing local proxy certificate."};
		
		/**
		 * Creates a new GOTaskPanel object.
		 * @throws Exception 
		 */
		public GOSetupPanel()  {
			super();
			
			/*
			 * Local Certificate
			 */
	
			//Check if a proxy already exists and if the the proxy is valid.
			String existingProxyCertStr = "";
			boolean proxyValid = false;
			if (CredentialHelper.getExistingProxyLoation()!=null){
				existingProxyCertStr = CredentialHelper.getExistingProxyLoation();
				if (existingProxyCertStr!=null && !existingProxyCertStr.equals("")){
					proxyValid = true;
				}
			}
			
			//  Create the main panel
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.WEST;
	
			Insets normalInsets = new Insets(0, 2, 4, 2);
			Insets indentedInsets = new Insets(0, 26, 4, 2);
			gbc.insets = normalInsets;
			gbc.weightx = 1.0;
	
			//  Build this panel
			setLayout(new BorderLayout());  
	
			createProxyCertPanel = new JPanel (new GridBagLayout());
			createProxyCertPanel.setBorder(BorderFactory.createTitledBorder("Login to Globus Online"));
	
			//GO username
			gbc.insets = indentedInsets;
			UIUtil.jGridBagAdd(createProxyCertPanel, new JLabel("GO Username:"), gbc, 1);   
	
			
			gbc.insets = normalInsets;
			gbc.weightx = 2.0;
			String goUsernameStr = PreferencesStore.get(SshTerminalPanel.PREF_GO_USERNAME, ""); 
			if (goUsernameStr!=null){
				goUsername = new XTextField(goUsernameStr, 20);
			}
			else{
				goUsername = new XTextField(20);
			}			
			UIUtil.jGridBagAdd(createProxyCertPanel, goUsername, gbc, GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(createProxyCertPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
	
			//add(createProxyCertPanel, BorderLayout.NORTH);
	
			//  Certificate type
			gbc.insets = indentedInsets;
			certTypeLabel = new JLabel("Certificate");
			UIUtil.jGridBagAdd(createProxyCertPanel, certTypeLabel, gbc, 1);
			gbc.insets = normalInsets;
			gbc.weightx = 2.0;
			certType = new JComboBox(new String[] {
					"Local PEM", "Local PKCS12", "Existing proxy", "Retrieve from MyProxy Server"                          
			});
			certType.addActionListener(this);
			certType.setFont(certType.getFont());
			certTypeLabel.setLabelFor(certType);
			UIUtil.jGridBagAdd(createProxyCertPanel, certType, gbc, 
					GridBagConstraints.RELATIVE);   
			UIUtil.jGridBagAdd(createProxyCertPanel, new JLabel(), gbc, 
					GridBagConstraints.REMAINDER);
	
			//PEM certificate
			gbc.insets = indentedInsets;
			gbc.weightx = 0.0;
			certInfoLabel = new JLabel(infoText[0]);
			certInfoLabel.setFont(new Font("Serif", Font.ITALIC, 14));
			UIUtil.jGridBagAdd(createProxyCertPanel, certInfoLabel, gbc, GridBagConstraints.REMAINDER);
	
	
			//  PKCS12 certificate
			String recordedPKCS12File =  PreferencesStore.get(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, "").trim().toLowerCase();
			String typicalPKCS12FIle = ConfigurationLoader.checkAndGetProperty("user.home", null) + File.separator + ".globus" + File.separator + "usercred.p12";
			gbc.insets = indentedInsets;
			pkcs12CertLabel = new JLabel("PKCS12 certificate location");
			UIUtil.jGridBagAdd(createProxyCertPanel, pkcs12CertLabel, gbc, 1);
			gbc.insets = normalInsets;
			gbc.weightx = 1.0;
			if (recordedPKCS12File!=null && !recordedPKCS12File.equals("")){
				pkcs12cert = new XTextField(recordedPKCS12File, 25);
			}
			else if(new File(typicalPKCS12FIle).exists()){
				pkcs12cert = new XTextField(typicalPKCS12FIle, 25);
			}
			else{
				pkcs12cert = new XTextField(25);
			}
			UIUtil.jGridBagAdd(createProxyCertPanel, pkcs12cert, gbc, GridBagConstraints.RELATIVE);
			pkcs12CertLabel.setLabelFor(pkcs12cert);
			gbc.weightx = 0.0;
			browsePKCS12Cert = new JButton("Browse");
			browsePKCS12Cert.setMnemonic('b');
			browsePKCS12Cert.addActionListener(this);
			UIUtil.jGridBagAdd(createProxyCertPanel, browsePKCS12Cert, gbc, GridBagConstraints.REMAINDER);
	
			//  Existing proxy certificate
			gbc.insets = indentedInsets;
			existingProxyCertLabel = new JLabel("Existing proxy location");
			UIUtil.jGridBagAdd(createProxyCertPanel, existingProxyCertLabel, gbc, 1);
			gbc.insets = normalInsets;
			gbc.weightx = 1.0;
	
			existingProxyCertStr = "";
			if (CredentialHelper.getExistingProxyLoation()!=null){
				existingProxyCertStr = CredentialHelper.getExistingProxyLoation();
			}
			existingProxyCert = new XTextField(existingProxyCertStr, 25);
	
			UIUtil.jGridBagAdd(createProxyCertPanel, existingProxyCert, gbc, GridBagConstraints.RELATIVE);
			existingProxyCertLabel.setLabelFor(existingProxyCert);
			gbc.weightx = 0.0;
			browseExistingProxyCert = new JButton("Browse");
			browseExistingProxyCert.setMnemonic('b');
			browseExistingProxyCert.addActionListener(this);
			UIUtil.jGridBagAdd(createProxyCertPanel, browseExistingProxyCert, gbc, GridBagConstraints.REMAINDER);
	
	
			//local passphrase
			gbc.insets = indentedInsets;
			localPassphraseLabel = new JLabel("Passphrase");
			UIUtil.jGridBagAdd(createProxyCertPanel, localPassphraseLabel, gbc, 1);
			gbc.insets = normalInsets;
			gbc.weightx = 2.0;
			localPassphrase = new JPasswordField(20);
			localPassphrase.setBackground(Color.white);
			localPassphrase.getDocument().addDocumentListener(this);
			localPassphraseLabel.setLabelFor(localPassphrase);
			UIUtil.jGridBagAdd(createProxyCertPanel, localPassphrase, gbc,
					GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(createProxyCertPanel, new JLabel(), gbc,
					GridBagConstraints.REMAINDER);
			
			add(createProxyCertPanel, BorderLayout.NORTH); 
	
			
			/*
			 * MyProxy Panel
			 */
			myproxyPanel = new JPanel(new GridBagLayout());
			myproxyPanel.setBorder(BorderFactory.createTitledBorder("MyProxy Server"));
	
			
		    gbc.insets = indentedInsets;
		    UIUtil.jGridBagAdd(myproxyPanel, new JLabel("MyProxy Server"), gbc, 1);
		    
		    String recordedMyproxyHostname = PreferencesStore.get(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, "").trim().toLowerCase();    
		    String [] myProxyHostNames = GOHelper.getMyProxyHostNames(recordedMyproxyHostname);
		    myproxyServer = new JComboBox(myProxyHostNames);
		    myproxyServer.setEditable(true);
		    myproxyServer.addActionListener(this);
		    gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyServer, gbc, GridBagConstraints.RELATIVE);   
		    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
		    
		      
		    //Port
		    int recordedMyproxyPort = 0;
		    try{
		    	recordedMyproxyPort = Integer.getInteger(PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_PORT, ""));
		    }catch(Exception e){    	
		    }
		    if (recordedMyproxyPort==0){
		    	recordedMyproxyPort = 7512;
		    }    
		    gbc.insets = indentedInsets;
		    myproxyServerPortLabel = new JLabel("Port");
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyServerPortLabel, gbc, 1);
		    gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
		    myproxyPort = new NumericTextField(new Integer(1), new Integer(50000), recordedMyproxyPort);
		    myproxyServerPortLabel.setLabelFor(myproxyPort);
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyPort, gbc, GridBagConstraints.RELATIVE);
		    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
		    
		    
		    //Lifetime
		    gbc.insets = indentedInsets;
		    myproxyServerLifetimeLabel = new JLabel("Lifetime (hours)");
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyServerLifetimeLabel, gbc, 1);
		    gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
		    myproxyLifetime = new NumericTextField(new Integer(0), new Integer(8760),
		            new Integer(defaultLifetime));
		    myproxyServerLifetimeLabel.setLabelFor(myproxyLifetime);
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyLifetime, gbc, GridBagConstraints.RELATIVE);    
		    //gbc.weightx = 0.0;
		    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(""), gbc, GridBagConstraints.REMAINDER);
		    
		    
		    //Username    
		    gbc.insets = indentedInsets;
		    myproxyUsernameLabel = new JLabel("Username");
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyUsernameLabel, gbc, 1);
		    gbc.insets = normalInsets;
		    gbc.weightx = 1.0;
		    String usernameStr = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_UNAME, "");    	
		    	
		    if(usernameStr==null || usernameStr.equals("")){
		    	usernameStr = System.getProperty("user.name");
		    }
		    
		    if (usernameStr!=null){
		    	myproxyUsername = new XTextField(usernameStr, 20);
		    }
		    else{
		    	myproxyUsername = new XTextField(20);
		    }
		    myproxyUsernameLabel.setLabelFor(myproxyUsername);    
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyUsername, gbc, GridBagConstraints.RELATIVE);
		    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
		    
		    
		    //Uploaded Myproxy passphrase (for retrieval
		    gbc.insets = indentedInsets;
		    myproxyPassphraseLabel = new JLabel("Passphrase");
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyPassphraseLabel, gbc, 1);
		    gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
		    myproxyUploadedPassphrase = new JPasswordField(20);
		    myproxyUploadedPassphrase.setBackground(Color.white);
		    myproxyUploadedPassphrase.getDocument().addDocumentListener(this);
		    myproxyPassphraseLabel.setLabelFor(myproxyUploadedPassphrase);
		    UIUtil.jGridBagAdd(myproxyPanel, myproxyUploadedPassphrase, gbc,
		                       GridBagConstraints.RELATIVE);
		    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(), gbc,
		                       GridBagConstraints.REMAINDER);
		    add(myproxyPanel, BorderLayout.CENTER);
		    
		    /*
		     * Optional Panel for voms and proxy auto upload
		     */
	
		    optionalPanel = new JPanel(new GridBagLayout());
		    optionalPanel.setBorder(BorderFactory.createTitledBorder("Optional"));
		    
			//Generate VOMS Proxy
			gbc.insets = indentedInsets;
			vomsSupport = new JCheckBox("  VOMS Enabled Proxy");
			UIUtil.jGridBagAdd(optionalPanel, vomsSupport, gbc, 1);
			gbc.insets = normalInsets;
			gbc.weightx = 1.0;
			vomsLabel = new JLabel("");
			//vomsLabel.setFont(new Font("Serif", Font.ITALIC, 14));
			UIUtil.jGridBagAdd(optionalPanel, vomsLabel, gbc, GridBagConstraints.REMAINDER);
			
			//Auto upload proxy to MyProxy server
			gbc.insets = indentedInsets;
			autoProxyUpload = new JCheckBox("  Upload credential to MyProxy server for Globus Online Endpoint activation");
			UIUtil.jGridBagAdd(optionalPanel, autoProxyUpload, gbc, 1);
			gbc.insets = normalInsets;
			gbc.weightx = 1.0;
			
			
			add(optionalPanel, BorderLayout.SOUTH);
	
			//Disable pkcs12 related ui
			triggerCertificateFormat("Local PEM");
	
			
			
			
	
		}
		
		public void triggerCertificateFormat(String certFormat) {
			localPassphrase.setText("");  
			if (certFormat.equals("Local PEM")){
				pkcs12CertLabel.setVisible(false);
				pkcs12cert.setVisible(false);
				browsePKCS12Cert.setVisible(false);
				existingProxyCertLabel.setVisible(false);
				existingProxyCert.setVisible(false);
				browseExistingProxyCert.setVisible(false);
				certInfoLabel.setVisible(true);
				certInfoLabel.setText(infoText[0]);
				localPassphraseLabel.setVisible(true);
				localPassphrase.setVisible(true);
				
				optionalPanel.setVisible(true);
				myproxyPanel.setVisible(false);
	
				//lifetime.setEnabled(true);
			}
			else if (certFormat.equals("Local PKCS12")){
				pkcs12CertLabel.setVisible(true);
				pkcs12cert.setVisible(true);
				browsePKCS12Cert.setVisible(true);
				existingProxyCertLabel.setVisible(false);
				existingProxyCert.setVisible(false);
				browseExistingProxyCert.setVisible(false);
				certInfoLabel.setVisible(false);
				localPassphraseLabel.setVisible(true);
				localPassphrase.setVisible(true);
	
				optionalPanel.setVisible(true);
				myproxyPanel.setVisible(false);
				//lifetime.setEnabled(true);
			}
			else if (certFormat.equals("Existing proxy")){	
	
				pkcs12CertLabel.setVisible(false);
				pkcs12cert.setVisible(false);
				browsePKCS12Cert.setVisible(false);
				existingProxyCertLabel.setVisible(true);
				existingProxyCert.setVisible(true);
				browseExistingProxyCert.setVisible(true);
				certInfoLabel.setVisible(true);
				certInfoLabel.setText(infoText[1]);
				localPassphraseLabel.setVisible(false);
				localPassphrase.setVisible(false);
				
				optionalPanel.setVisible(true);
				myproxyPanel.setVisible(false);
				
	
				//lifetime.setEnabled(false);
				if (existingProxyCert.getText().trim().equals("")){
					if (CredentialHelper.getExistingProxyLoation()!=null){
						existingProxyCert.setText(CredentialHelper.getExistingProxyLoation());
					}
				}
			}
			else if (certFormat.equals("Retrieve from MyProxy Server")){
				pkcs12CertLabel.setVisible(false);
				pkcs12cert.setVisible(false);
				browsePKCS12Cert.setVisible(false);
				existingProxyCertLabel.setVisible(false);
				existingProxyCert.setVisible(false);
				browseExistingProxyCert.setVisible(false);
				certInfoLabel.setVisible(false);
				localPassphraseLabel.setVisible(false);
				localPassphrase.setVisible(false);
				
				optionalPanel.setVisible(false);
				myproxyPanel.setVisible(true);
				
			}
		}
		
		
		public void triggerLocalCertificatePanel(boolean isEnabled){
			createProxyCertPanel.setVisible(isEnabled);
		}
	
		@Override
		public void actionPerformed(ActionEvent evt) {
			//Check for which certificate type to generate proxy from
			if (evt.getSource() == certType){
				triggerCertificateFormat(getCertType());			
			}
			if (evt.getSource() == browsePKCS12Cert) {      
				JFileChooser chooser = new JFileChooser();
				ExampleFileFilter filter = new ExampleFileFilter();
				filter.addExtension("pfx");
				filter.addExtension("p12");
				filter.setDescription("pfx and p12 files");
				if (getPKCS12Cert()!=null && !getPKCS12Cert().equals(""))
					chooser.setSelectedFile(new File(getPKCS12Cert()));
				chooser.setFileFilter(filter);
				chooser.setFileHidingEnabled(false);
				chooser.setDialogTitle("Select PKCS12 Certificate File");
	
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					pkcs12cert.setText(chooser.getSelectedFile().getPath());
				}
			}  
			if (evt.getSource() == browseExistingProxyCert) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileHidingEnabled(false);
				if (getExistingProxyCert()!=null && !getExistingProxyCert().equals("")){
					chooser.setSelectedFile(new File(getExistingProxyCert()));
				}
				chooser.setDialogTitle("Select Existing Proxy File");
	
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {			
					existingProxyCert.setText(chooser.getSelectedFile().getPath());			
					try{
						X509Credential proxy = CredentialHelper.retrieveExistingProxy(getExistingProxyCert());
						if (proxy!=null){
							int timeleft = (int) proxy.getTimeLeft()/3600;
							myproxyLifetime.setValue(timeleft);
						}
	
					}catch(Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage(),
								"Globus Online Tool Error", JOptionPane.ERROR_MESSAGE); 
					}
				}
			}
			
		}
	
		@Override
		public void changedUpdate(DocumentEvent evt) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void insertUpdate(DocumentEvent evt) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void removeUpdate(DocumentEvent evt) {
			// TODO Auto-generated method stub
			
		}
		
		/**
		 *
		 *
		 * @return
		 */ 
		public String getGOUsername() {
			return (String) goUsername.getText().trim();
		}
		  /**
		  *
		  *
		  * @return
		  */
		 public int getMyProxyPort() {
		   return ( (Integer) myproxyPort.getValue()).intValue();
		 }
		  /**
		   *
		   *
		   * @return
		   */
		  public String getMyProxyServer() {
			  return (String) myproxyServer.getSelectedItem();
		  }
		  /**
		  *
		  *
		  * @return
		  */
		 public int getMyProxyLifetime() {
		   return ( (Integer) myproxyLifetime.getValue()).intValue();
		 }		  
		 
		 /**
		  *
		  *
		  * @return
		  */ 
		  public String getMyProxyUsername() {
			  return (String) myproxyUsername.getText().trim();
		  }

		/**
		 *
		 *
		 * @return
		 */
		public String getCertType() {
			return (String) certType.getSelectedItem();
		}
	
		/**
		 *
		 *
		 * @return
		 */
		public String getPKCS12Cert() {
			return pkcs12cert.getText();
		}
	
		/**
		 *
		 *
		 * @return
		 */
		public String getExistingProxyCert() {
			return existingProxyCert.getText();
		}
		
		/**
		 *
		 *
		 * @return
		 */
		public boolean getVOMSSupport() {
			return vomsSupport.isSelected();
		}
		/**
		 *
		 *
		 * @return
		 */
		public boolean getAutoProxyUpload() {
			return autoProxyUpload.isSelected();
		}
		/**
		 *
		 *
		 * @return
		 */
		public char[] getLocalPassphrase() {
			return localPassphrase.getPassword();
		}
		/**
		 *
		 *
		 * @return
		 */
		public char[] getUploadedMyProxyPassphrase() {
			return myproxyUploadedPassphrase.getPassword();
		}
	}

}

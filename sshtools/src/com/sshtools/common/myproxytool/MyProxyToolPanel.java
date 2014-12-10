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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.globus.gsi.X509Credential;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.j2ssh.authentication.ExampleFileFilter;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.sshterm.SshTerminalPanel;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class MyProxyToolPanel
    extends JPanel
    implements DocumentListener,
    ActionListener {  

  //  Private instance variables
  private JButton browseInput;

  //  Private instance variables
  private JPanel localCertificatePanel;
  private JButton browsePKCS12Cert;
  private JButton browseExistingProxyCert;
  private JComboBox certType;
  private JComboBox myproxyServer;
  private JLabel certTypeLabel;
  private JLabel myproxyServerLabel;
  private JLabel myproxyServerPortLabel;
  private JLabel myproxyServerLifetimeLabel;
  private JLabel usernameLabel;
  private JLabel vonameInfoText;
  private JLabel vonameLabel;
  private JLabel certInfoLabel;
  private JLabel pkcs12CertLabel;
  private JLabel existingProxyCertLabel;
  private JLabel localPassphraseLabel;
  private JLabel typeLabel;
  private JLabel vomsLabel;
  private JPasswordField passphrase;
  private JPasswordField confirmPassphrase;
  private JProgressBar strength;
  private XTextField username;
  private XTextField voname;
  private XTextField pkcs12cert;
  private XTextField existingProxyCert;
  private JPasswordField localPassphrase;
  private JCheckBox vomsSupport;
  private NumericTextField port;
  private NumericTextField lifetime;
  
  private String [] infoText = {"Assumes the PEM certficates 'usercert.pem' and 'userkey.pem' are in {home.directory}/.globus directory.",
  								"Assumes the lifetime of uploaded proxy is the same as the life of the existing local proxy certificate."};
  private static int defaultLifetime = 168;
  
  
    
  /**
   * Creates a new MyProxyToolPanel object.
   */
  public MyProxyToolPanel() {
    super();
    
    String val = ConfigurationLoader.checkAndGetProperty("user.home", null);
    File userPrefDir = null;
    if (val != null) {
      userPrefDir = new File(val + File.separator + ".sshterm");
    }

    if (userPrefDir != null) {
      PreferencesStore.init(new File(userPrefDir,
    		  						"@APPLICATION_NAME@"+ ".properties"));
    }

    JPanel myproxyPanel = new JPanel(new GridBagLayout());
    myproxyPanel.setBorder(BorderFactory.createTitledBorder("MyProxy Server Information"));

    //  Create the main panel
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    Insets normalInsets = new Insets(0, 2, 4, 2);
    Insets indentedInsets = new Insets(0, 26, 4, 2);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    

    //  myProxy Server
    gbc.insets = indentedInsets;
    UIUtil.jGridBagAdd(myproxyPanel, new JLabel("Server"), gbc, 1);
    
    String recordedMyproxyHostname = PreferencesStore.get(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, "").trim().toLowerCase();    
    String [] myProxyHostNames = getMyProxyHostNames(recordedMyproxyHostname);
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
    port = new NumericTextField(new Integer(1), new Integer(50000), recordedMyproxyPort);
    myproxyServerPortLabel.setLabelFor(port);
    UIUtil.jGridBagAdd(myproxyPanel, port, gbc, GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
    
    
    //Lifetime
    gbc.insets = indentedInsets;
    myproxyServerLifetimeLabel = new JLabel("Lifetime (hours)");
    UIUtil.jGridBagAdd(myproxyPanel, myproxyServerLifetimeLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 2.0;
    lifetime = new NumericTextField(new Integer(0), new Integer(8760),
            new Integer(defaultLifetime));
    myproxyServerLifetimeLabel.setLabelFor(lifetime);
    UIUtil.jGridBagAdd(myproxyPanel, lifetime, gbc, GridBagConstraints.RELATIVE);    
    //gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(""), gbc, GridBagConstraints.REMAINDER);
    
    
    //Username    
    gbc.insets = indentedInsets;
    usernameLabel = new JLabel("Username");
    UIUtil.jGridBagAdd(myproxyPanel, usernameLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    String usernameStr = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_UNAME, "");    	
    	
    if(usernameStr==null || usernameStr.equals("")){
    	usernameStr = System.getProperty("user.name");
    }
    
    if (usernameStr!=null){
    	username = new XTextField(usernameStr, 20);
    }
    else{
    	username = new XTextField(20);
    }
    usernameLabel.setLabelFor(username);    
    UIUtil.jGridBagAdd(myproxyPanel, username, gbc, GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(myproxyPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
      
    
    /*
     * Local Certificate
     */
    localCertificatePanel = new JPanel (new GridBagLayout());
    localCertificatePanel.setBorder(BorderFactory.createTitledBorder("Local Certificate"));
          
    //  Certificate type
    gbc.insets = indentedInsets;
    certTypeLabel = new JLabel("Certificate Format");
    UIUtil.jGridBagAdd(localCertificatePanel, certTypeLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 2.0;
    certType = new JComboBox(new String[] {
            				"PEM", "PKCS12", "Existing proxy"                          
							});
    certType.addActionListener(this);
    certType.setFont(certType.getFont());
    certTypeLabel.setLabelFor(certType);
    UIUtil.jGridBagAdd(localCertificatePanel, certType, gbc, 
    					GridBagConstraints.RELATIVE);   
    UIUtil.jGridBagAdd(localCertificatePanel, new JLabel(), gbc, 
    					GridBagConstraints.REMAINDER);
      
    //PEM certificate
    gbc.insets = indentedInsets;
    gbc.weightx = 0.0;
    certInfoLabel = new JLabel(infoText[0]);
    certInfoLabel.setFont(new Font("Serif", Font.ITALIC, 14));
    UIUtil.jGridBagAdd(localCertificatePanel, certInfoLabel, gbc, GridBagConstraints.REMAINDER);
    
    
    //  PKCS12 certificate
    String recordedPKCS12File =  PreferencesStore.get(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, "").trim().toLowerCase();
    gbc.insets = indentedInsets;
    pkcs12CertLabel = new JLabel("PKCS12 certificate location");
    UIUtil.jGridBagAdd(localCertificatePanel, pkcs12CertLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    if (recordedPKCS12File!=null && !recordedPKCS12File.equals("")){
    	pkcs12cert = new XTextField(recordedPKCS12File, 25);
    }
    else{
    	pkcs12cert = new XTextField(25);
    }
    UIUtil.jGridBagAdd(localCertificatePanel, pkcs12cert, gbc, GridBagConstraints.RELATIVE);
    pkcs12CertLabel.setLabelFor(pkcs12cert);
    gbc.weightx = 0.0;
    browsePKCS12Cert = new JButton("Browse");
    browsePKCS12Cert.setMnemonic('b');
    browsePKCS12Cert.addActionListener(this);
    UIUtil.jGridBagAdd(localCertificatePanel, browsePKCS12Cert, gbc, GridBagConstraints.REMAINDER);
    
    //  Existing proxy certificate
    gbc.insets = indentedInsets;
    existingProxyCertLabel = new JLabel("Existing proxy location");
    UIUtil.jGridBagAdd(localCertificatePanel, existingProxyCertLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    
    String existingProxyCertStr = "";
    if (CredentialHelper.getExistingProxyLocation()!=null){
		 existingProxyCertStr = CredentialHelper.getExistingProxyLocation();
	 }
    existingProxyCert = new XTextField(existingProxyCertStr, 25);
    
    UIUtil.jGridBagAdd(localCertificatePanel, existingProxyCert, gbc, GridBagConstraints.RELATIVE);
    existingProxyCertLabel.setLabelFor(existingProxyCert);
    gbc.weightx = 0.0;
    browseExistingProxyCert = new JButton("Browse");
    browseExistingProxyCert.setMnemonic('b');
    browseExistingProxyCert.addActionListener(this);
    UIUtil.jGridBagAdd(localCertificatePanel, browseExistingProxyCert, gbc, GridBagConstraints.REMAINDER);
    
    
    //local passphrase
    gbc.insets = indentedInsets;
    localPassphraseLabel = new JLabel("Passphrase");
    UIUtil.jGridBagAdd(localCertificatePanel, localPassphraseLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 2.0;
    localPassphrase = new JPasswordField(20);
    localPassphrase.setBackground(Color.white);
    localPassphrase.getDocument().addDocumentListener(this);
    localPassphraseLabel.setLabelFor(localPassphrase);
    UIUtil.jGridBagAdd(localCertificatePanel, localPassphrase, gbc,
                       GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(localCertificatePanel, new JLabel(), gbc,
                       GridBagConstraints.REMAINDER);
    
    //Generate VOMS Proxy
    gbc.insets = indentedInsets;
    vomsSupport = new JCheckBox("  VOMS Enabled Proxy");
    UIUtil.jGridBagAdd(localCertificatePanel, vomsSupport, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 3.0;
    vomsLabel = new JLabel(" (Generates voms-enabled proxy and upload it)");
    vomsLabel.setFont(new Font("Serif", Font.ITALIC, 14));
    UIUtil.jGridBagAdd(localCertificatePanel, vomsLabel, gbc, GridBagConstraints.REMAINDER);
    

    //Disable pkcs12 related ui
    triggerCertificateFormat("PEM");

    //  Build this panel
    setLayout(new BorderLayout());
    add(myproxyPanel, BorderLayout.CENTER);
    add(localCertificatePanel,BorderLayout.SOUTH);

  }
  
  public void triggerLifetime(boolean isEnabled){
	  lifetime.setEnabled(isEnabled);
	  myproxyServerLifetimeLabel.setEnabled(isEnabled);
  }
  
  public void triggerCertificateFormat(String certFormat) {
	  localPassphrase.setText("");  
	  if (certFormat.equals("PEM")){
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
		  
		  lifetime.setEnabled(true);
	  }
	  else if (certFormat.equals("PKCS12")){
		  pkcs12CertLabel.setVisible(true);
		  pkcs12cert.setVisible(true);
		  browsePKCS12Cert.setVisible(true);
		  existingProxyCertLabel.setVisible(false);
		  existingProxyCert.setVisible(false);
		  browseExistingProxyCert.setVisible(false);
		  certInfoLabel.setVisible(false);
		  localPassphraseLabel.setVisible(true);
		  localPassphrase.setVisible(true);
		  
		  lifetime.setEnabled(true);
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
		  
		  lifetime.setEnabled(false);
		  if (existingProxyCert.getText().trim().equals("")){
			  if (CredentialHelper.getExistingProxyLocation()!=null){
				  existingProxyCert.setText(CredentialHelper.getExistingProxyLocation());
			  }
		  }
	  }
  }

  public void triggerLocalCertificatePanel(boolean isEnabled){
	  localCertificatePanel.setVisible(isEnabled);
  }
  

  
  public String[] getMyProxyHostNames(String recordedHostName){
	  LinkedHashSet<String> myProxyHostNames = new LinkedHashSet<String>();
	  myProxyHostNames.add("myproxy.lrz.de");
	  myProxyHostNames.add("px.grid.sara.nl");
	  myProxyHostNames.add("myproxy.egi.eu");
	  if (recordedHostName!=null && !recordedHostName.equals(""))
		  myProxyHostNames.add(recordedHostName);
	  
	  String [] hostnames = new String [myProxyHostNames.size()];
	  hostnames = myProxyHostNames.toArray(hostnames);
	  return hostnames;
  }
 
  /**
  *
  *
  * @return
  */
 public int getPort() {
   return ( (Integer) port.getValue()).intValue();
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
 public int getLifetime() {
   return ( (Integer) lifetime.getValue()).intValue();
 }
 /**
  *
  *
  * @return
  */ 
  public String getUsername() {
	  return (String) username.getText().trim();
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
  public char[] getLocalPassphrase() {
    return localPassphrase.getPassword();
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
   * @param evt
   */
  public void actionPerformed(ActionEvent evt) {	
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
					lifetime.setValue(timeleft);
				}
				
			}catch(Exception ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(),
						"MyProxy Error", JOptionPane.ERROR_MESSAGE); 
			}
		}
    }
  }

  /**
  *
  *
  * @param e
  */
 public void changedUpdate(DocumentEvent e) {   
 }

 /**
  *
  *
  * @param e
  */
 public void insertUpdate(DocumentEvent e) {
 }

 /**
  *
  *
  * @param e
  */
 public void removeUpdate(DocumentEvent e) {
 }

  
}

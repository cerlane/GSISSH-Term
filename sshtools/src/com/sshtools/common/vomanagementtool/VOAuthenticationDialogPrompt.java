/*
 *  SSHTools - VO Management Tool
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

package com.sshtools.common.vomanagementtool;

import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.glite.voms.contact.UserCredentials;
import org.glite.voms.contact.VOMSProxyInit;
import org.glite.voms.contact.VOMSRequestOptions;
import org.globus.axis.gsi.GSIConstants;
import org.globus.common.CoGProperties;
import org.globus.gsi.CredentialException;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.ConfigUtil;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.vomanagementtool.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.vomanagementtool.common.ConfigHelper;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.sshterm.ProxyHelper;
import com.sshtools.sshterm.SshTerminalPanel;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class VOAuthenticationDialogPrompt{

	public class VOAuthenticationDialog
					extends JDialog
					implements ActionListener {

		String userName = "";
		String hostName = "";
		int port = 0;
		int lifetime = 0;
		String certType= null;
		String dn = null;
		boolean voms = false;
		
		JLabel promptLabel;
	
	
		VOManagementToolPanel vopanel;
		JButton vomsproxyinit;
		int proxyType = 10;
	
		/**
		 * Creates a new Main object.
		 */
		
		
		void init() {		
			try {
				ConfigurationLoader.initialize(false);
			}
			catch (ConfigurationException ex) {
			}
	
			//  Set the frame icon
			setIconImage(new ResourceIcon(this.getClass(), ConfigHelper.ICON).getImage());
	
			//Things to do when window is closed
			addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					PreferencesStore.put(SshTerminalPanel.PREF_VOMS_PROXYTYPE, proxyType + "");
					//System.exit(0);
				}
			});
	
			//Check if voms dir is ok for user
			//VOHelper.createVOMSLOCATIONDialog();
	
			try {
				VOHelper.checkUpdateVOMSConfigFiles();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error: Cannot fetch latest VOs setup", JOptionPane.ERROR_MESSAGE);
			}
	
			if (VOHelper.retrieveSetup()){
				//Check if Favourites is changed
				VOHelper.checkFavouritesChanges();
			}
			else{
				System.err.println("Retrieve setup failed!");
			}
	
			ConfigHelper.setAuthentication(true);
	
			JPanel iconPanel = new JPanel(new GridBagLayout());    
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(0, 2, 4, 2);
			gbc.weightx = 0.0;
			JLabel VOTitleLabel = new JLabel("Create voms enabled proxy");
			VOTitleLabel.setFont(new Font("Serif", Font.BOLD, 16));
			UIUtil.jGridBagAdd(iconPanel, VOTitleLabel, gbc, GridBagConstraints.RELATIVE);
			gbc.weightx = 1.0;
			IconWrapperPanel northPanel = new IconWrapperPanel(new ResourceIcon(this.getClass(), ConfigHelper.ICON), iconPanel);
			northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	
			//Change size of tree
			ConfigHelper.setVisibleRowCount(10);
			VOManagementToolPanel voPanel = new VOManagementToolPanel();
			JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			centerPanel.add(voPanel);
	
			//Change size of tree		
			VOSelectedPanel voSelectedPanel = new VOSelectedPanel();
			JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			eastPanel.add(voSelectedPanel);
	
			//  Wrap the whole thing in an empty border
			JPanel mainPanel = new JPanel(new BorderLayout());
			JScrollPane mainScroller = new JScrollPane(mainPanel);  
			mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));    
			mainPanel.add(northPanel, BorderLayout.NORTH);
			mainPanel.add(centerPanel, BorderLayout.CENTER);
			mainPanel.add(eastPanel, BorderLayout.EAST);
	
			vomsproxyinit = new JButton("voms-proxy-init");	
			vomsproxyinit.addActionListener(this);
			mainPanel.add(vomsproxyinit, BorderLayout.SOUTH);
	
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
	
			if (evt.getSource() == vomsproxyinit){
				boolean toContinue = true;
				if (ConfigHelper.selectedRoot.getChildCount()==0){
					int result = JOptionPane.showConfirmDialog(this, "No VOs are selected", "Warning", JOptionPane.OK_OPTION, 
							JOptionPane.WARNING_MESSAGE,  new ResourceIcon(VOHelper.class, ConfigHelper.ICON));	
					if(result==JOptionPane.YES_OPTION){
						dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
					}
				}
				else{
					ArrayList<String> selectedVOs = new ArrayList<String>();
					for (int i=0; i<ConfigHelper.selectedRoot.getChildCount(); i++){
						MyTreeNode temp = (MyTreeNode) ConfigHelper.selectedRoot.getChildAt(i);			            				
						selectedVOs.add(temp.getUserObject().toString());
					}
					X509Credential x509Cred = initProxy( (String[])selectedVOs.toArray(new String[selectedVOs.size()]));
					if(x509Cred!=null){
						//Save it!
						try {
								x509Cred.verify();
								GSSCredential vomsCredential = new GlobusGSSCredentialImpl(x509Cred, GSSCredential.INITIATE_ONLY);
								//TODO Check voms attribute and thus lifetime to ensure that lifetime is not shorter than main proxy
								String warningMsg = "";
								Vector voAttributes = VOMSValidator.parse(x509Cred.getCertificateChain());
								if (voAttributes!=null && voAttributes.size()>0){
									for (int i=0;i<voAttributes.size();i++){
										VOMSAttribute att = (VOMSAttribute) voAttributes.get(i);
										if(att!=null){
											try {
												long vomsSecLeft = (att.getNotAfter().getTime() - System.currentTimeMillis()) / 1000L;
												long proxySecLeft = x509Cred.getTimeLeft(); 
												if (proxySecLeft>vomsSecLeft){
													long days = vomsSecLeft /(60*60*24);
													vomsSecLeft = (vomsSecLeft - (days*60*60*24));
													long hours = vomsSecLeft / (60*60);
													vomsSecLeft = (vomsSecLeft - (hours*60*60));
													long mins = vomsSecLeft / 60;
													vomsSecLeft = (vomsSecLeft - (mins*60));
												
													warningMsg += "Lifetime for VOMS extension has been reduced to \n" +
													 days+" days "+hours+" hours "+mins+" minutes "+vomsSecLeft+" seconds\n"+ 
													 " for VO: " + att.getVO() +"\n\n"; 
												}
											} catch (ParseException e) {
												e.printStackTrace();
											}
											
										}
									}
									if (!warningMsg.trim().equals("")){
										warningMsg += "Please note that this is a VOMS server restriction.\nPlease contact your VO admin for more information.";
										JOptionPane.showMessageDialog(this, warningMsg, "Warning: VOMS extension lifetime",
												JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ConfigHelper.ICON));
									}
								}
								
								saveProxy(x509Cred);
								ConfigHelper.setVomsCredential(vomsCredential);	
								dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
							}
	
						/* catch (GlobusCredentialException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} */catch (GSSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (CredentialException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
	
	
		}
	
		private X509Credential initProxy(String [] voNames){
	
			X509Credential x509Cred = null;
			X509Credential vomsCred = null;
			int proxyTypeInt = 4;
	
			if (ConfigHelper.getGssCred() instanceof GlobusGSSCredentialImpl){
				x509Cred = ((GlobusGSSCredentialImpl)ConfigHelper.getGssCred()).getX509Credential();
				
				int proxyCode = x509Cred.getProxyType().getCode();
				if (proxyCode==GSIConstants.CertificateType.GSI_2_PROXY.getCode()
						|| proxyCode==GSIConstants.CertificateType.GSI_2_LIMITED_PROXY.getCode()){
					proxyTypeInt = 2;
				}
				
			}
	
			if (x509Cred !=null){
	
				//Setting up VOMSDIR and CADIR and VOMSESDIR
				VOHelper.setupVOMSProperties();
				System.setProperty("org.globus.gsi.version", proxyTypeInt+"");
				HashMap options = getVOMSRequestOptions(voNames);			
				try{
					System.out.println("vomsdir: " + System.getProperty("VOMSDIR"));
					System.out.println("vomsesdir: " + System.getProperty("VOMSES_LOCATION"));
					System.out.println("cadir: " + System.getProperty("CADIR"));
	
					UserCredentials userCreds = UserCredentials.instance(x509Cred.getPrivateKey(), x509Cred.getCertificateChain());
					VOMSProxyInit vomsProxyInit = VOMSProxyInit.instance(userCreds);
					vomsProxyInit.setProxyLifetime((int) x509Cred.getTimeLeft());				
					vomsProxyInit.setProxyType(proxyTypeInt);
					UserCredentials vomsUserCred = vomsProxyInit.getVomsProxy(options.values());
					
					vomsCred = new X509Credential(vomsUserCred.getUserKey(), vomsUserCred.getUserChain());
					saveVOMSProxy(vomsCred);
					
					JOptionPane.showMessageDialog(this, "Successfully created VOMS enabled proxy.", "GSI-SSHTerm Authentication",
												JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ConfigHelper.ICON));
				}
				catch (Exception e){
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Failed to create VOMS enabled proxy.", "GSI-SSHTerm Authentication", JOptionPane.ERROR_MESSAGE);
					return null;
	
				}
			}
			return vomsCred;
	
		}
		private void saveVOMSProxy(X509Credential theProxy) {
			String proxyFile = CoGProperties.getDefault().getProxyFile();	

			OutputStream out = null;
			String proxyLoc = ConfigUtil.discoverProxyLocation();

			try {
				out = new FileOutputStream(proxyLoc);
				theProxy.save(out);
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
			}finally {
				if (out != null) {
					try { 
						out.close(); 
					} catch(Exception e) {}
				}
			}
	    }
		private HashMap getVOMSRequestOptions(String [] fqans){
			HashMap options = new HashMap();
			for (int i = 0; i < fqans.length; i++){
				String[] opts = fqans[i].split(":");
	
				String voname = opts[0];
				VOMSRequestOptions voOpt;
				if (options.containsKey(voname)) {
					voOpt = (VOMSRequestOptions)options.get(voname);
				}
				else {
					voOpt = new VOMSRequestOptions();
					voOpt.setVoName(voname);
					options.put(voname, voOpt);
				}
	
				if(opts.length==2){
					voOpt.addFQAN(opts[1]);
				}
			}
			return options;
		}
		private void saveProxy (X509Credential proxy){

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
	
		
	
	
		/**
		 *
		 *
		 * @param args
		 */
		/*public static void main(String[] args) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) {
			}
	
			VOAuthenticationDialog main = new VOAuthenticationDialog();
			/*main.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					System.exit(0);
				}
			});*/
	
			/*main.pack();
			UIUtil.positionComponent(SwingConstants.CENTER, main);
			main.setVisible(true);
	
		}*/
		
		VOAuthenticationDialog(){
            super((Frame)null, title, true);
            promptLabel = new JLabel();
            init();
        }

		VOAuthenticationDialog(Frame frame){
            super(frame, title, true);
            promptLabel = new JLabel();
            init();
        }

		VOAuthenticationDialog(Dialog dialog){
            super(dialog, title, true);
            promptLabel = new JLabel();
            init();
        }
	}

	public VOAuthenticationDialog getVOMSAuthenticated(){
		Window window = parent != null ? (Window)SwingUtilities.getAncestorOfClass(java.awt.Window.class, parent) : null;
		VOAuthenticationDialog dialog = null;
		if(window instanceof Frame)
            dialog = new VOAuthenticationDialog((Frame)window);
        else
        if(window instanceof Dialog)
            dialog = new VOAuthenticationDialog((Dialog)window);
        else
            dialog = new VOAuthenticationDialog();
		
		return dialog;
        
	}
	
	private VOAuthenticationDialogPrompt()
	{
	    title = "VOMS-PROXY-INIT";
	}
	
	public void setParentComponent(Component component)
	{
	    parent = component;
	}
	public static VOAuthenticationDialogPrompt getInstance(){
	    if(instance == null)
	        instance = new VOAuthenticationDialogPrompt();
	    return instance;
	}
	
	public void setTitle(String s)
	{
	    title = s;
	}

	private static VOAuthenticationDialogPrompt instance;
	private Component parent;
	private String title;
}

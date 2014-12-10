/*
 *  GSI-SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2007 STFC/CCLRC.
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

/**
 *
 * This class provides some helpful routines for proxy handling
 */

package com.sshtools.sshterm;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.globus.common.CoGProperties;
import org.globus.gsi.CredentialException;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.X509Credential;
import org.globus.util.ConfigUtil;
import org.globus.util.Util;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.util.CertificateUtil;
import org.globus.gsi.util.ProxyCertificateUtil;

import com.sshtools.j2ssh.configuration.SshConnectionProperties;

public class ProxyHelper implements Runnable {


    public static boolean proxyExists() {
	String filename = CoGProperties.getDefault().getProxyFile();
	if(filename==null) return false;
	File file = new File(filename);
	return file.exists() && file.isFile();
    }

    /*public static void saveProxy(GlobusCredential theProxy, SshConnectionProperties props) {
	String proxyFile = CoGProperties.getDefault().getProxyFile();			 
        OutputStream out = null;
        try {
            File file = Util.createFile(proxyFile);
            if (!Util.setOwnerAccessOnly(proxyFile)) {
                System.out.println("Warning: could not set permissions on proxy file.");
		//message("Warning: could not set permissions on proxy file.");
            }
            out = new FileOutputStream(file);
            theProxy.save(out);
	    com.sshtools.common.util.ShutdownHooks.runOnExit(new ProxyHelper());
       } catch (SecurityException e) {
            System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
	    message(props, "Failed to save proxy to proxy file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
	    message(props, "Failed to save proxy to proxy file: " + e.getMessage());
        } finally {
            if (out != null) {
                try { 
		    out.close(); 
		} catch(Exception e) {}
            }
        }
    }*/
    
    
    public static void saveProxy(X509Credential theProxy, SshConnectionProperties props) {
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
    	    message(props, "Failed to save proxy to proxy file: " + e.getMessage());
        } catch (CertificateEncodingException e) {
        	System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
    	    message(props, "Failed to save proxy to proxy file: " + e.getMessage());
        } catch (IOException e) {
        	 System.out.println("Failed to save proxy to proxy file: " + e.getMessage());
        	 message(props, "Failed to save proxy to proxy file: " + e.getMessage());
        }finally {
            if (out != null) {
                try { 
		    out.close(); 
		} catch(Exception e) {}
            }
        }
    }

    public void run() {
	if(proxyExists()) {
	    int ret = JOptionPane.showConfirmDialog(null, "You have a proxy certificate stored on a disk which may have been created by this terminal, do you want to delete it?", "GSI-SSHTerm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	    if(ret==JOptionPane.YES_OPTION) destroyProxy();
	}
    }

    private static void message(SshConnectionProperties props, String s) {
	JOptionPane.showMessageDialog(props.getWindow(), s, "Problem saving proxy", JOptionPane.ERROR_MESSAGE);
		
    }

    public static void showProxyInfo(java.awt.Component parent) {
	String identity="", subject="", issuer="", lifetime="", storage="", type="";
	Vector voAttributes = null;
	CoGProperties cogproperties = CoGProperties.getDefault();

	try {
	    if (!(new File(cogproperties.getProxyFile())).exists()) {
		storage = "No local proxy.";
	    } else {
		X509Credential x509credential = new X509Credential(cogproperties.getProxyFile());
		//Cerlane: To support voms enabled proxy of type 4
        //globuscredential.verify();
        if(x509credential.getTimeLeft()>0){		
        	storage = "Local File: "+cogproperties.getProxyFile();
        	subject = CertificateUtil.toGlobusID(x509credential.getSubject(),true);
        	issuer = CertificateUtil.toGlobusID(x509credential.getIssuer(),true);
        	identity = CertificateUtil.toGlobusID(x509credential.getIdentityCertificate().getSubjectDN().toString(),true);
        	long seconds = x509credential.getTimeLeft();
        	long days = seconds /(60*60*24);
        	seconds = (seconds - (days*60*60*24));
        	long hours = seconds / (60*60);
        	seconds = (seconds - (hours*60*60));
        	long mins = seconds / 60;
        	seconds = (seconds - (mins*60));
			lifetime = days+" days "+hours+" hours "+mins+" minutes "+seconds+" seconds.";
			GSIConstants.CertificateType certType = x509credential.getProxyType();
		    type = ProxyCertificateUtil.getProxyTypeAsString(certType);
		    if(certType == GSIConstants.CertificateType.EEC) type = "end entity certificate";
		    } 
        
        	//Cerlane
        	//Check if it is a voms proxy
        	voAttributes = VOMSValidator.parse(x509credential.getCertificateChain());
        	
	    }
	} catch(CredentialException credentialexception) {
	    if(credentialexception.getMessage().indexOf("Expired") >= 0) {
		File file = new File(cogproperties.getProxyFile());
		file.delete();
		storage = "Expired local proxy.";
	    } else {
		storage = "No local proxy (Error).";			
	    }
	}
	JComponent panel = getInfoPanel(identity, subject, issuer, lifetime, storage, type, voAttributes);
	JOptionPane.showMessageDialog(parent, panel, "Disk Proxy Information", JOptionPane.PLAIN_MESSAGE);
    }

    public static void destroyProxy() {
	String filename = CoGProperties.getDefault().getProxyFile();
	if(filename!=null) {
	    Util.destroy(new File(filename));
	}
    }


    private static JComponent getInfoPanel(String identity, String subject, String issuer, String lifetime, String storage, String type, Vector voAttributes) {
	JPanel infoResultPanel = new JPanel();
	infoResultPanel.setLayout(new GridBagLayout());
	GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
	gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.insets = new Insets(20, 20, 0, 0);
	JLabel infoResulTitleLabel = new JLabel();
	infoResulTitleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
	infoResulTitleLabel.setText("Proxy Information:");
	GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	gridBagConstraints2.gridy = 1;
	infoResultPanel.setLayout(new GridBagLayout());
	infoResultPanel.add(getInfoResultMainPanel(identity, subject, issuer, lifetime, storage, type, voAttributes), gridBagConstraints2);
	infoResultPanel.add(infoResulTitleLabel, gridBagConstraints1);
	return infoResultPanel;
    }



    private static JPanel getInfoResultMainPanel(String identity, String subject, String issuer, String lifetime, String storage, String type, Vector voAttributes) {
	JPanel infoResultMainPanel = new JPanel();
	infoResultMainPanel.setAutoscrolls(true);
	GridBagConstraints gridBagConstraintsA = new GridBagConstraints();
	gridBagConstraintsA.fill = GridBagConstraints.VERTICAL;
	gridBagConstraintsA.weightx = 1.0;
	gridBagConstraintsA.insets = new Insets(3, 3, 3, 3);
	gridBagConstraintsA.ipadx = 0;
	gridBagConstraintsA.gridx = 2;
	gridBagConstraintsA.gridy = 1;
	GridBagConstraints gridBagConstraintsB = new GridBagConstraints();
	gridBagConstraintsB.insets = new Insets(3, 3, 3, 3);
	gridBagConstraintsB.anchor = GridBagConstraints.WEST;
	gridBagConstraintsB.gridx = 1;
	gridBagConstraintsB.gridy = 1;

	JLabel infoResultTypeLabel = new JLabel();
	infoResultTypeLabel.setText("Type:");
	JLabel infoResultIdentityLabel = new JLabel();
	infoResultIdentityLabel.setText("Identity: ");
	JLabel infoResultLifetimeLabel = new JLabel();
	infoResultLifetimeLabel.setText("Lifetime: ");
	JLabel infoResultIssuerLabel = new JLabel();
	infoResultIssuerLabel.setText("Issuer: ");
	JLabel infoResultSubjectLabel = new JLabel();
	infoResultSubjectLabel.setText("Subject: ");
	JLabel infoResultStorageLabel = new JLabel();
	infoResultStorageLabel.setText("Storage: ");
	
	JTextField infoResultIdentityTextField = new JTextField(identity);
	infoResultIdentityTextField.setEditable(false);
	infoResultIdentityTextField.setColumns(50);
	JTextField infoResultSubjectTextField = new JTextField(subject);
	infoResultSubjectTextField.setEditable(false);
	infoResultSubjectTextField.setColumns(50);
	JTextField infoResultStorageTextField = new JTextField(storage);
	infoResultStorageTextField.setEditable(false);
	infoResultStorageTextField.setColumns(50);
	JTextField infoResultIssuerTextField = new JTextField(issuer);
	infoResultIssuerTextField.setEditable(false);
	infoResultIssuerTextField.setColumns(50);
	JTextField infoResultTypeTextField = new JTextField(type);
	infoResultTypeTextField.setEditable(false);
	infoResultTypeTextField.setColumns(50);
	JTextField infoResultLifetimeTextField = new JTextField(lifetime);
	infoResultLifetimeTextField.setEditable(false);
	infoResultLifetimeTextField.setColumns(50);

	infoResultMainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	infoResultMainPanel.setLayout(new GridBagLayout());

	infoResultMainPanel.add(infoResultStorageLabel, gridBagConstraintsB);
	infoResultMainPanel.add(infoResultStorageTextField, gridBagConstraintsA);
	gridBagConstraintsB.gridy = 2;
	gridBagConstraintsA.gridy = 2;
	
	infoResultMainPanel.add(infoResultIdentityLabel, gridBagConstraintsB);
	infoResultMainPanel.add(infoResultIdentityTextField, gridBagConstraintsA);

	gridBagConstraintsB.gridy = 3;
	gridBagConstraintsA.gridy = 3;
	infoResultMainPanel.add(infoResultSubjectLabel, gridBagConstraintsB);
	infoResultMainPanel.add(infoResultSubjectTextField, gridBagConstraintsA);

	gridBagConstraintsB.gridy = 4;
	gridBagConstraintsA.gridy = 4;
	infoResultMainPanel.add(infoResultIssuerLabel, gridBagConstraintsB);
	infoResultMainPanel.add(infoResultIssuerTextField, gridBagConstraintsA);

	gridBagConstraintsB.gridy = 5;
	gridBagConstraintsA.gridy = 5;
	infoResultMainPanel.add(infoResultLifetimeLabel, gridBagConstraintsB);
	infoResultMainPanel.add(infoResultLifetimeTextField, gridBagConstraintsA);

	gridBagConstraintsB.gridy = 6;
	gridBagConstraintsA.gridy = 6;
	infoResultMainPanel.add(infoResultTypeLabel, gridBagConstraintsB);
	infoResultMainPanel.add(infoResultTypeTextField, gridBagConstraintsA);
	
	
	//Cerlane 
	//For vos
	int j=7;
	if (voAttributes!=null && voAttributes.size()>0){
		JTextArea voInfo = new JTextArea(5,50);
		JScrollPane voScrollPane = new JScrollPane(voInfo);
		String info = "";
		for (int i=0;i<voAttributes.size();i++){
			VOMSAttribute att = (VOMSAttribute) voAttributes.get(i);
			if(att!=null){
				if(i==0){
					gridBagConstraintsB.gridy = j;
					gridBagConstraintsA.gridy = j;
					infoResultMainPanel.add(new JLabel("VO Information:"), gridBagConstraintsB);
					
				}
				info += "VO: " + att.getVO() + "\n";
				info += "Issuer: " + att.getIssuer() + "\n";
				info += "URI: " + att.getHost() + ":" + att.getPort() + "\n";	
				try {
					long seconds = (att.getNotAfter().getTime() - System.currentTimeMillis()) / 1000L;
					long days = seconds /(60*60*24);
					seconds = (seconds - (days*60*60*24));
					long hours = seconds / (60*60);
					seconds = (seconds - (hours*60*60));
					long mins = seconds / 60;
					seconds = (seconds - (mins*60));
					info += "Lifetime: " + days+" days "+hours+" hours "+mins+" minutes "+seconds+" seconds\n";
				} catch (ParseException e) {
					e.printStackTrace();
				}
				info += "\n";
				
			}
		}
		gridBagConstraintsB.gridy = j;
		gridBagConstraintsA.gridy = j;
		voInfo.append(info);
		infoResultMainPanel.add(voScrollPane, gridBagConstraintsA);
		j++;
	}
	
	return infoResultMainPanel;
    }
}

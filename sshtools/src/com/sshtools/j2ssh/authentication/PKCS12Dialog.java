/*
 *  GSI-SSHTools - Java SSH2 API
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

package com.sshtools.j2ssh.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.sshtools.j2ssh.authentication.AuthenticationProtocolException;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationPrompt;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.globus.gsi.*;
import java.util.*;
import com.sshtools.common.authentication.*;
import java.security.cert.*;

public class PKCS12Dialog {
  private Component parent;

  public PKCS12Dialog(Component parent) {
    this.parent = parent;
  }

  public GlobusCredential showPrompt() throws AuthenticationProtocolException {

      Security.addProvider(new BouncyCastleProvider());
      File keyfile =null;

      String passphrase = null;

      if (keyfile == null || !keyfile.exists()) {
	  JFileChooser chooser = new JFileChooser();
	  chooser.setFileHidingEnabled(false);
	  chooser.setDialogTitle("Select Certificate File For Authentication");
	  
	  if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
	      keyfile = chooser.getSelectedFile();
	  }
	  else {
	      return null;
	  }
      }
      
      Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, parent);
      PassphraseDialog dialog = null;
      
      if (w instanceof Frame) {
	  dialog = new PassphraseDialog( (Frame) w);
      }
      else if (w instanceof Dialog) {
	  dialog = new PassphraseDialog( (Dialog) w);
      }
      else {
	  dialog = new PassphraseDialog();
      }
      KeyStore store = null;
      do {
	  dialog.setVisible(true);
	  
	  if (dialog.isCancelled()) {
	      return null;
	  }
	  
	  passphrase = new String(dialog.getPassphrase());
	  
	  try {
	      store = KeyStore.getInstance("PKCS12", "BC");
	      FileInputStream in = new FileInputStream(keyfile);
	      store.load(in, passphrase.toCharArray());
	      break;
	  }
	  catch (Exception ihke) {
	      dialog.setMessage("Had a problem: "+ihke);
	      dialog.setMessageForeground(Color.red);
	  }
      } while (true);
      try {

	  Enumeration e = store.aliases();
	  if(!e.hasMoreElements()) return null;
	  String alias = (String)e.nextElement();
	  java.security.cert.Certificate cert = store.getCertificate(alias);
	  Key key = store.getKey(alias,passphrase.toCharArray());
	  // System.out.println("Y "+cert[i].getType()+" "+cert[i].getClass().getName()+" "+key.getClass().getName());
	  
	  if(!(cert instanceof X509Certificate)) return null;
	  if(!(key instanceof PrivateKey)) return null;
	  return new GlobusCredential((PrivateKey)key, new X509Certificate[] {(X509Certificate) cert});
      } catch(Exception ihke) {
	  throw new AuthenticationProtocolException("Had a problem: "+ihke);
      }
  }
}

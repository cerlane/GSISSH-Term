/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
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

package com.sshtools.common.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.j2ssh.authentication.UserGridCredential;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClientFactory;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;
import com.sshtools.sshterm.SshTerminalPanel;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.4 $
 */
public class SshToolsConnectionKerberosTab
    extends JPanel
    implements SshToolsConnectionTab {
  //


  //

  /**  */
  public final static String CONNECT_ICON = "largeserveridentity.png";

  /**  */
  public final static String AUTH_ICON = "largelock.png";

  /**  */
  public final static String SHOW_AVAILABLE = "<Show available methods>";

  //

  /**  */
  protected XTextField jTextHostname = new XTextField();

  /**  */
  protected XTextField jTextUsername = new XTextField();

  /**  */
  protected XTextField jTextRealm = new XTextField();

  /**  */
  protected XTextField jTextKDC = new XTextField();


  /**  */
  protected SshToolsConnectionProfile profile;

  /**  */
  protected JCheckBox useKerberos;


  /**  */
  protected Log log = LogFactory.getLog(SshToolsConnectionHostTab.class);

  /**
   * Creates a new SshToolsConnectionKerberosTab object.
   */
  public SshToolsConnectionKerberosTab() {
    super();

    //  Create the main connection details panel
    JPanel mainConnectionDetailsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(0, 2, 2, 2);
    //  enabled option
    //gbc.fill = GridBagConstraints.NONE;
    useKerberos = new JCheckBox("Use MyProxy Kerberos support");
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, useKerberos, gbc,
                       GridBagConstraints.REMAINDER);
    //  Host name
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, new JLabel("Hostname"),
                       gbc, GridBagConstraints.REMAINDER);
    //gbc.fill = GridBagConstraints.HORIZONTAL;
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, jTextHostname, gbc,
                       GridBagConstraints.REMAINDER);
    //gbc.fill = GridBagConstraints.NONE;

    //  Username
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, new JLabel("Username"),
                       gbc, GridBagConstraints.REMAINDER);
    //gbc.fill = GridBagConstraints.HORIZONTAL;
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, jTextUsername, gbc,
                       GridBagConstraints.REMAINDER);

    JPanel settingsPanel = new JPanel(new GridBagLayout());
    settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings if krb5.conf or krb5.ini file not found: "));
    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    gbc2.anchor = GridBagConstraints.NORTHWEST;
    gbc2.insets = new Insets(0, 2, 2, 2);
    gbc2.weightx = 1.0;

    //  realm
    UIUtil.jGridBagAdd(settingsPanel, new JLabel("Realm"),
                       gbc2, GridBagConstraints.REMAINDER);
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    UIUtil.jGridBagAdd(settingsPanel, jTextRealm, gbc2,
                       GridBagConstraints.REMAINDER);
    gbc2.fill = GridBagConstraints.NONE;

    //  kdc
    UIUtil.jGridBagAdd(settingsPanel, new JLabel("KDC"),
                       gbc2, GridBagConstraints.REMAINDER);
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    gbc2.weighty = 1.0;
    UIUtil.jGridBagAdd(settingsPanel, jTextKDC, gbc2,
                       GridBagConstraints.REMAINDER);
    gbc2.fill = GridBagConstraints.NONE;

    //
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.insets = new Insets(4, 2, 2, 2);
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, settingsPanel, gbc,
                       GridBagConstraints.REMAINDER);

    IconWrapperPanel iconMainConnectionDetailsPanel = new IconWrapperPanel(new
        ResourceIcon(
        SshToolsConnectionHostTab.class, AUTH_ICON),
        mainConnectionDetailsPanel);

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.weightx = 1.0;    
    gbc.weighty = 1.0;
    UIUtil.jGridBagAdd(this, iconMainConnectionDetailsPanel, gbc,
                       GridBagConstraints.REMAINDER);

  }

  /**
   *
   *
   * @param profile
   */
  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;
    String hostname=UserGridCredential.DEFAULT_MYPROXY_SERVER_K;
    String username=System.getProperty("user.name");
    String realm = System.getenv("USERDNSDOMAIN");
    String kdc = System.getenv("USERDNSDOMAIN");
    boolean use = true;
    
    // Load defaults from ~/.sshterm/GSI-SSHTerm.properties if present.
    hostname = PreferencesStore.get(SshTerminalPanel.PREF_KRB5_MYPROXY_HOSTNAME, hostname);
    username = PreferencesStore.get(SshTerminalPanel.PREF_KRB5_MYPROXY_USERNAME, username);
    realm = PreferencesStore.get(SshTerminalPanel.PREF_KRB5_MYPROXY_REALM, realm);
    kdc = PreferencesStore.get(SshTerminalPanel.PREF_KRB5_MYPROXY_KDC, kdc);


    if(profile!=null) {
	hostname = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_HOSTNAME, hostname);
	username = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_USERNAME, username);
	realm = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_REALM, realm);
	kdc = profile.getApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_KDC, kdc);
	use = profile.getApplicationPropertyBoolean(SshTerminalPanel.PREF_KRB5_MYPROXY_USE, use);
    }
    jTextHostname.setText(hostname);
    jTextUsername.setText(username);
    jTextRealm.setText(realm);
    jTextKDC.setText(kdc);
    useKerberos.setSelected(use);
   
  }

  /**
   *
   *
   * @return
   */
  public SshToolsConnectionProfile getConnectionProfile() {
    return profile;
  }

  /**
   *
   *
   * @return
   */
  public String getTabContext() {
    return "Connection";
  }

  /**
   *
   *
   * @return
   */
  public Icon getTabIcon() {
    return null;
  }

  /**
   *
   *
   * @return
   */
  public String getTabTitle() {
    return "Kerberos";
  }

  /**
   *
   *
   * @return
   */
  public String getTabToolTipText() {
    return "Settings for using Kerberos enabled MyProxy servers.";
  }

  /**
   *
   *
   * @return
   */
  public int getTabMnemonic() {
    return 'k';
  }

  /**
   *
   *
   * @return
   */
  public Component getTabComponent() {
    return this;
  }

  /**
   *
   *
   * @return
   */
  public boolean validateTab() {
      return true;
  }


  /**
   *
   */
  public void applyTab() {

    if(profile!=null) {
	profile.setApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_HOSTNAME, jTextHostname.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_USERNAME, jTextUsername.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_REALM, jTextRealm.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_KDC, jTextKDC.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_KRB5_MYPROXY_USE, useKerberos.isSelected());
    }
  }

  /**
   *
   */
  public void tabSelected() {
  }
}

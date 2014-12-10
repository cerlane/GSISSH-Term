//Changes (c) STFC/CCLRC 2006-2007
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
import java.awt.BorderLayout;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClientFactory;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;
import com.sshtools.sshterm.SshTerminalPanel;

import org.globus.gsi.GSIConstants;
/**
 *
 *
 * @author $author$
 * @version $Revision: 1.12 $
 */
public class SshToolsConnectionHostTab
    extends JPanel
    implements SshToolsConnectionTab {
  //

  // ************************************************************************************
  //
  // These defaults should not be changed here but in the res/common/default.properties file
  // please see docs/README and src/com/sshtools/common/ui/PreferencesStore.java for details.
  //
  // These are here because we must provide some default.
  //
  public final static int DEFAULT_PORT = 2222;
  //**************************************************************************************

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
  protected NumericTextField jTextPort = new NumericTextField(new Integer(0),
      new Integer(65535), new Integer(DEFAULT_PORT));

  /**  */
  protected XTextField jTextUsername = new XTextField();

  /**  */
  protected JList jListAuths = new JList();

  /**  */
  protected java.util.List methods = new ArrayList();

  /**  */
  protected SshToolsConnectionProfile profile;

  /**  */
  protected JCheckBox allowAgentForwarding;

  /**  */
  protected JComboBox delegationOption;

  /**  */
  protected JComboBox proxyOption;

  /**  */
  protected NumericTextField proxyLength  = new NumericTextField(new Integer(1),
      new Integer(240), new Integer(12));

  /**  */
  protected JCheckBox proxySave;
  
  /**  Cerlane: added this for voms support*/
  protected JCheckBox vomsSupport;

  /**  */
  protected Log log = LogFactory.getLog(SshToolsConnectionHostTab.class);

  /**
   * Creates a new SshToolsConnectionHostTab object.
   */
  public SshToolsConnectionHostTab() {
    super();

    //  Create the main connection details panel
    JPanel mainConnectionDetailsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(0, 2, 2, 2);
    gbc.weightx = 1.0;

    //  Host name
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, new JLabel("Hostname"),
                       gbc, GridBagConstraints.REMAINDER);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, jTextHostname, gbc,
                       GridBagConstraints.REMAINDER);
    gbc.fill = GridBagConstraints.NONE;

    //  Port
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, new JLabel("Port"), gbc,
                       GridBagConstraints.REMAINDER);
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, jTextPort, gbc,
                       GridBagConstraints.REMAINDER);

    //  Username
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, new JLabel("Username"),
                       gbc, GridBagConstraints.REMAINDER);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 1.0;
    UIUtil.jGridBagAdd(mainConnectionDetailsPanel, jTextUsername, gbc,
                       GridBagConstraints.REMAINDER);
    gbc.fill = GridBagConstraints.NONE;

    //
    IconWrapperPanel iconMainConnectionDetailsPanel = new IconWrapperPanel(new
        ResourceIcon(
        SshToolsConnectionHostTab.class, CONNECT_ICON),
        mainConnectionDetailsPanel);

    //  Authentication methods panel
    JPanel authMethodsPanel = new JPanel(new GridBagLayout());
    authMethodsPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.gridx=0;
    gbc.gridy=0;
    gbc.gridwidth=2;

    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    gbc2.anchor = GridBagConstraints.NORTHWEST;
    gbc2.insets = new Insets(2, 2, 2, 2);
    gbc2.weightx = 1.0;
    gbc2.weighty = 0.0;
    gbc2.gridx=0;
    gbc2.gridy=1;
    gbc2.gridwidth=2;

    GridBagConstraints gbc3 = new GridBagConstraints();
    gbc3.fill = GridBagConstraints.HORIZONTAL;
    gbc3.anchor = GridBagConstraints.NORTHWEST;
    gbc3.insets = new Insets(2, 2, 2, 2);
    gbc3.weightx = 1.0;
    gbc3.weighty = 0.0;
    gbc3.gridx=0;
    gbc3.gridy=2;
    gbc3.gridwidth=2;

    GridBagConstraints gbc4 = new GridBagConstraints();
    gbc4.fill = GridBagConstraints.HORIZONTAL;
    gbc4.anchor = GridBagConstraints.NORTHWEST;
    gbc4.insets = new Insets(26, 2, 2, 2);
    gbc4.weightx = 0.0;
    gbc4.weighty = 0.0;
    gbc4.gridx=0;
    gbc4.gridy=3;
    gbc4.gridwidth=1;

    GridBagConstraints gbc5 = new GridBagConstraints();
    gbc5.fill = GridBagConstraints.NONE;
    gbc5.anchor = GridBagConstraints.WEST;
    gbc5.insets = new Insets(26, 2, 2, 2);
    gbc5.weightx = 1.0;
    gbc5.weighty = 0.0;
    gbc5.gridx=1;
    gbc5.gridy=3;
    gbc5.gridwidth=1;

    GridBagConstraints gbc6 = new GridBagConstraints();
    gbc6.fill = GridBagConstraints.HORIZONTAL;
    gbc6.anchor = GridBagConstraints.NORTHWEST;
    gbc6.insets = new Insets(12, 2, 2, 2);
    gbc6.weightx = 0.0;
    gbc6.weighty = 0.0;
    gbc6.gridx=0;
    gbc6.gridy=4;
    gbc6.gridwidth=1;

    GridBagConstraints gbc7 = new GridBagConstraints();
    gbc7.fill = GridBagConstraints.NONE;
    gbc7.anchor = GridBagConstraints.WEST;
    gbc7.insets = new Insets(12, 2, 2, 2);
    gbc7.weightx = 1.0;
    gbc7.weighty = 0.0;
    gbc7.gridx=1;
    gbc7.gridy=4;
    gbc7.gridwidth=1;

    GridBagConstraints gbc8 = new GridBagConstraints();
    gbc8.fill = GridBagConstraints.HORIZONTAL;
    gbc8.anchor = GridBagConstraints.NORTHWEST;
    gbc8.insets = new Insets(12, 2, 2, 2);
    gbc8.weightx = 0.0;
    gbc8.weighty = 0.0;
    gbc8.gridx=0;
    gbc8.gridy=5;
    gbc8.gridwidth=1;

    GridBagConstraints gbc9 = new GridBagConstraints();
    gbc9.fill = GridBagConstraints.NONE;
    gbc9.anchor = GridBagConstraints.WEST;
    gbc9.insets = new Insets(12, 2, 2, 2);
    gbc9.weightx = 1.0;
    gbc9.weighty = 0.0;
    gbc9.gridx=1;
    gbc9.gridy=5;
    gbc9.gridwidth=1;

    GridBagConstraints gbc10 = new GridBagConstraints();
    gbc10.fill = GridBagConstraints.HORIZONTAL;
    gbc10.anchor = GridBagConstraints.NORTHWEST;
    gbc10.insets = new Insets(26, 2, 2, 2);
    gbc10.weightx = 1.0;
    gbc10.weighty = 0.0;
    gbc10.gridx=0;
    gbc10.gridy=6;
    gbc10.gridwidth=2;
    
    //Cerlane added for voms support
    GridBagConstraints gbc11 = new GridBagConstraints();
    gbc11.fill = GridBagConstraints.HORIZONTAL;
    gbc11.anchor = GridBagConstraints.NORTHWEST;
    gbc11.insets = new Insets(26, 2, 2, 2);
    gbc11.weightx = 1.0;
    gbc11.weighty = 0.0;
    gbc11.gridx=0;
    gbc11.gridy=7;
    gbc11.gridwidth=2;


    //  Authentication methods
    authMethodsPanel.add(new JLabel("Authentication Methods"), gbc);
    jListAuths.setVisibleRowCount(5);
    authMethodsPanel.add(new JScrollPane(jListAuths), gbc2);

    allowAgentForwarding = new JCheckBox("Allow agent forwarding");
    authMethodsPanel.add(allowAgentForwarding, gbc3);

    String options[] = {"Full", "Limited", "None"};
    delegationOption = new JComboBox(options);
    delegationOption.setSelectedIndex(0);
    authMethodsPanel.add(new JLabel("Delegation Type:"), gbc4);
    authMethodsPanel.add(delegationOption, gbc5);

    String optionsP[]={"Pre-RFC Impersonation", "RFC Impersonation", "Legacy"};
    proxyOption = new JComboBox(optionsP);
    //Cerlane: Default proxy type to RFC
    proxyOption.setSelectedIndex(0);
    authMethodsPanel.add(new JLabel("Proxy Type:"), gbc6);
    authMethodsPanel.add(proxyOption, gbc7);

    authMethodsPanel.add(new JLabel("Proxy Lifetime (hours):"), gbc8);
    proxyLength.setColumns(5);
    authMethodsPanel.add(proxyLength, gbc9);
    gbc.gridx=0;
    proxySave = new JCheckBox("Save Grid Proxies to Disk");
    authMethodsPanel.add(proxySave, gbc10);
    
    //Cerlane: Add option to enable VOMS support
    vomsSupport = new JCheckBox("Enable VOMS Support");
    authMethodsPanel.add(vomsSupport, gbc11);
    
    //
    IconWrapperPanel iconAuthMethodsPanel = new IconWrapperPanel(new
        ResourceIcon(
        SshToolsConnectionHostTab.class, AUTH_ICON),
        authMethodsPanel);

    //  This panel
    JPanel mine = new JPanel();
    mine.setLayout(new GridBagLayout());
    mine.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.gridx=0;
    gbc.gridy=0;
    mine.add(iconMainConnectionDetailsPanel, gbc);
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.gridx=0;
    gbc.gridy=1;
    gbc.insets = new Insets(20, 2, 2, 2);
    mine.add(iconAuthMethodsPanel, gbc);
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    this.add(mine, BorderLayout.NORTH);
    //  Set up the values in the various components
    addAuthenticationMethods();
  }

  /**
   *
   *
   * @param profile
   */
  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;
    jTextHostname.setText(profile == null ? "" : profile.getHost());
    jTextUsername.setText(profile == null ? "" : profile.getUsername());
    //jTextPort.setValue(new Integer(profile == null ? DEFAULT_PORT : profile.getPort()));
    //Attempted to try to change default port
    int port = PreferencesStore.getInt(SshToolsApplication.PREF_CONNECTION_LAST_PORT, DEFAULT_PORT);
    port = profile.getApplicationPropertyInt(SshToolsApplication.PREF_CONNECTION_LAST_PORT, port);
    jTextPort.setValue(port);
 
    if (System.getProperty("sshtools.agent") == null) {
      allowAgentForwarding.setSelected(false);
      allowAgentForwarding.setEnabled(false);
    }
    else {
      allowAgentForwarding.setEnabled(true);
      allowAgentForwarding.setSelected(profile != null && profile.getAllowAgentForwarding());
    }
    
    //CERLANE: Change default proxy type to rfc compliant
    String cur = PreferencesStore.get(SshTerminalPanel.PREF_PROXY_TYPE, Integer.toString(GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY.getCode()));
    cur = profile.getApplicationProperty(SshTerminalPanel.PREF_PROXY_TYPE, cur);
    if(cur.equals(Integer.toString(GSIConstants.CertificateType.GSI_2_PROXY.getCode())) || cur.equals("prerfc")) {
    	proxyOption.setSelectedIndex(0);
    }  else if(cur.equals(Integer.toString(GSIConstants.CertificateType.GSI_4_IMPERSONATION_PROXY.getCode())) || cur.equals("rfc")) {
    	proxyOption.setSelectedIndex(1);
    } if(cur.equals(Integer.toString(GSIConstants.CertificateType.GSI_2_PROXY.getCode())) || cur.equals("legacy")) {
    	proxyOption.setSelectedIndex(2);
    } 

    cur = PreferencesStore.get(SshTerminalPanel.PREF_DELEGATION_TYPE, "full");
    cur = profile.getApplicationProperty(SshTerminalPanel.PREF_DELEGATION_TYPE, cur);
    if(cur.equals("full")) {
    	delegationOption.setSelectedIndex(0);
    } else if(cur.equals("limited")) {
    	delegationOption.setSelectedIndex(1);
    } else if(cur.equals("none")) {
    	delegationOption.setSelectedIndex(2);
    }
    cur = PreferencesStore.get(SshTerminalPanel.PREF_PROXY_LENGTH, "12");
    cur = profile.getApplicationProperty(SshTerminalPanel.PREF_PROXY_LENGTH, cur);
    Integer t = new Integer(12);
    try {
	Integer tt = Integer.parseInt(cur);
	if(tt<=240 && tt>=1) {
	    t = tt;
	}
    } catch(NumberFormatException e) {}
    proxyLength.setValue(t);

    boolean saveProxy = PreferencesStore.getBoolean(SshTerminalPanel.PREF_SAVE_PROXY, false);
    saveProxy = profile.getApplicationPropertyBoolean(SshTerminalPanel.PREF_SAVE_PROXY, saveProxy);
    proxySave.setSelected(saveProxy);
    
    //Cerlane added this for voms support -> To get information from properties file
    boolean supportVOMS = PreferencesStore.getBoolean(SshTerminalPanel.PREF_VOMS_SUPPORT, false);
    supportVOMS = profile.getApplicationPropertyBoolean(SshTerminalPanel.PREF_VOMS_SUPPORT, supportVOMS);
    vomsSupport.setSelected(supportVOMS);
   
    // Match the authentication methods
    Map auths = profile == null ? new HashMap() : profile.getAuthenticationMethods();
    Iterator it = auths.entrySet().iterator();
    Map.Entry entry;
    String authmethod;
    int[] selectionarray = new int[auths.values().size()];
    int count = 0;

    ListModel model = jListAuths.getModel();

    while (it.hasNext()) {
      entry = (Map.Entry) it.next();
      authmethod = (String) entry.getKey();

      for (int i = 0; i < model.getSize(); i++) {
        if (model.getElementAt(i).equals(authmethod)) {
          selectionarray[count++] = i;
          break;
        }
      }
          /*if (jListAuths.getNextMatch(authmethod, 0, Position.Bias.Forward) > -1) {
               selectionarray[count] = jListAuths.getNextMatch(authmethod, 0,
        Position.Bias.Forward);
               count++;
         }*/

      jListAuths.clearSelection();
      jListAuths.setSelectedIndices(selectionarray);
    }
  }

  /**
   *
   *
   * @return
   */
  public SshToolsConnectionProfile getConnectionProfile() {
    return profile;
  }

  private void addAuthenticationMethods() {
    java.util.List methods = new ArrayList();
    methods.add(SHOW_AVAILABLE);
    methods.addAll(SshAuthenticationClientFactory.getSupportedMethods());
    jListAuths.setListData(methods.toArray());
    jListAuths.setSelectedIndex(2);
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
    return "Host";
  }

  /**
   *
   *
   * @return
   */
  public String getTabToolTipText() {
    return "The main host connection details.";
  }

  /**
   *
   *
   * @return
   */
  public int getTabMnemonic() {
    return 'h';
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
    // Validate that we have enough information
    if (jTextHostname.getText().equals("")
        || jTextPort.getText().equals("")
	/* || jTextUsername.getText().equals("")*/) {
      JOptionPane.showMessageDialog(this, "Please enter all details!",
                                    "Connect", JOptionPane.OK_OPTION);

      return false;
    }

    // Setup the authentications selected
    java.util.List chosen = getChosenAuth();

    if (chosen != null) {
      Iterator it = chosen.iterator();

      while (it.hasNext()) {
        String method = (String) it.next();

        try {
          SshAuthenticationClient auth = SshAuthenticationClientFactory
              .newInstance(method, profile);
        }
        catch (AlgorithmNotSupportedException anse) {
          JOptionPane.showMessageDialog(this,
                                        method + " is not supported!");

          return false;
        }
      }
    }

    return true;
  }

  private java.util.List getChosenAuth() {
    // Determine whether any authenticaiton methods we selected
    Object[] auths = jListAuths.getSelectedValues();
    String a;
    java.util.List l = new java.util.ArrayList();

    if (auths != null) {
      for (int i = 0; i < auths.length; i++) {
        a = (String) auths[i];

        if (a.equals(SHOW_AVAILABLE)) {
          return null;
        }
        else {
          l.add(a);
        }
      }
    }
    else {
      return null;
    }

    return l;
  }

  /**
   *
   */
  public void applyTab() {
    profile.setHost(jTextHostname.getText());
    profile.setPort(Integer.valueOf(jTextPort.getText()).intValue());   
    profile.setUsername(jTextUsername.getText());
    profile.setAllowAgentForwarding(allowAgentForwarding.getModel()
                                    .isSelected());

    profile.setApplicationProperty(SshTerminalPanel.PREF_DELEGATION_TYPE, 
                                   ((String)delegationOption.getSelectedItem()).toLowerCase());

    String proxy=Integer.toString(GSIConstants.GSI_3_IMPERSONATION_PROXY);
    switch (proxyOption.getSelectedIndex()) {
    case 0: proxy = Integer.toString(GSIConstants.GSI_3_IMPERSONATION_PROXY); break;
    case 1: proxy = Integer.toString(GSIConstants.GSI_4_IMPERSONATION_PROXY); break;
    case 2: proxy = Integer.toString(GSIConstants.GSI_2_PROXY); break;
    }

    profile.setApplicationProperty(SshTerminalPanel.PREF_PROXY_TYPE, proxy);

    proxy = proxyLength.getText();
    profile.setApplicationProperty(SshTerminalPanel.PREF_PROXY_LENGTH, proxy);

    profile.setApplicationProperty(SshTerminalPanel.PREF_SAVE_PROXY, proxySave.isSelected());
    
    //Cerlane added for voms support
    profile.setApplicationProperty(SshTerminalPanel.PREF_VOMS_SUPPORT, vomsSupport.isSelected());
    
    java.util.List chosen = getChosenAuth();

    // Remove the authentication methods and re-apply them
    profile.removeAuthenticationMethods();

    if (chosen != null) {
      Iterator it = chosen.iterator();

      while (it.hasNext()) {
        String method = (String) it.next();

        try {
          SshAuthenticationClient auth = SshAuthenticationClientFactory
              .newInstance(method, profile);
          auth.setUsername(jTextUsername.getText());
          profile.addAuthenticationMethod(auth);
        }
        catch (AlgorithmNotSupportedException anse) {
          log.error("This should have been caught by validateTab()?",
                    anse);
        }
      }
    }
  }

  /**
   *
   */
  public void tabSelected() {
  }
}

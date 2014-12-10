/*
 *  GSI-SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2005-7 STFC/CCLRC.
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
package com.sshtools.sshterm;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import uk.ac.rl.esc.browser.Browser;

public class GSIAuthTab extends JPanel implements SshToolsConnectionTab,  ActionListener {

    protected SshToolsConnectionProfile profile;
    private JPanel jContentPane = null;
    private JPanel usePanel = null;
    private JList useList = null;
    private JList dontUseList = null;
    private JLabel dontUseLabel = null;
    private JLabel useLabel = null;
    private JButton upButton = null;
    private JButton downButton = null;
    private JButton stopUseButton = null;
    private JButton useButton = null;
    private JPanel mpPanel = null;
    private JLabel myProxyLabel = null;
    private JLabel mpHostLabel = null;
    private JLabel mpPortLabel = null;
    private JLabel mpUsernameLabel = null;
    private JLabel mpVOnameLabel = null;
    private JTextField mpHostTextField = null;
    private JTextField mpPortTextField = null;
    private JTextField mpUsernameTextField = null;
    private JTextField mpVOnameTextField = null;
    private JPanel authsPanel = null;
    private JPanel browserPanel = null;
    private JLabel browserLabel = null;
    private JComboBox bBrowserComboBox = null;
    private JLabel browserBrowserLabel = null;
    private JLabel dNLabel = null;
    private JTextField bDNTextField = null;
    private JPanel pkcs12Panel = null;
    private JLabel pkcs12Label = null;
    private JLabel pkcs12FileLabel = null;
    private JTextField pkcs12FileTextField = null;
    private JButton pkcs12Button = null;

    private String ALL_AUTHS[] = {
	"param",
	"proxy", 
	"other",
	"cert",
	"browser",
	"krb"};

    private String AUTHNAMES[] = {
	"Applet Param",
	"Disk Proxy",
	"Other Methods",
	".pem/usercred.p12",
	"Browser",
	"SSO"};

    private String DEFAULT_ORDER = "param,proxy,cert,other";

    public GSIAuthTab() {
	super();
	add(getJContentPane());
	setAvailableActions();
    }

    private void setAvailableActions() {
    }

    public void applyTab() {
	profile.setApplicationProperty(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, mpHostTextField.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_MYPROXY_UNAME, mpUsernameTextField.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_MYPROXY_PORT, mpPortTextField.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_MYPROXY_VONAME, mpVOnameTextField.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_BROWSER_DN, bDNTextField.getText());
	profile.setApplicationProperty(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, pkcs12FileTextField.getText());
	
	String bprofile = Browser.getCurrentBrowser();
	String profiles[] = null;
	try {
	    profiles = uk.ac.rl.esc.browser.Browser.getBrowserList();
	} catch(java.io.IOException e) {
	} catch(IllegalStateException e) {}
	if(bprofile==null && (profiles!=null && profiles.length>0)) {
	    profile.setApplicationProperty(SshTerminalPanel.PREF_BROWSER_PROFILE, (String) bBrowserComboBox.getSelectedItem());
	}
	DefaultListModel lm = (DefaultListModel) useList.getModel();
	String auths = "";
	for(int i=0;i<lm.getSize();i++) {
	    String item = (String)lm.get(i);
	    for(int j=0;j<ALL_AUTHS.length;j++) {
		if(AUTHNAMES[j].equals(item)) auths+=ALL_AUTHS[j]+",";
	    }
	}
	if(auths.charAt(auths.length()-1)==',') auths=auths.substring(0, auths.length()-1);
	profile.setApplicationProperty(SshTerminalPanel.PREF_AUTH_ORDER, auths);
    }

    public void setConnectionProfile(SshToolsConnectionProfile profile) {
	this.profile = profile;
	// Authentication Order
	String order = PreferencesStore.get(SshTerminalPanel.PREF_AUTH_ORDER, DEFAULT_ORDER);
	order = profile.getApplicationProperty(SshTerminalPanel.PREF_AUTH_ORDER, order);
	String meths[] = order.split("(,|\\s)");
	LinkedList luse = new LinkedList();
	LinkedList lnouse = new LinkedList();
	for(int i=0;i<meths.length;i++) {
	    if(meths[i]!=null && (!meths[i].equals("")) && (!luse.contains(meths[i]))) luse.add(meths[i]);
	}
	for(int i=0;i<ALL_AUTHS.length;i++) {
	    if(!luse.contains(ALL_AUTHS[i])) lnouse.add(ALL_AUTHS[i]);
	}
	DefaultListModel suse = new DefaultListModel();
	for(int i=0;i<luse.size();i++) {
	    String res = null;
	    for(int j=0;j<ALL_AUTHS.length;j++) {
		if(ALL_AUTHS[j].equals((String)luse.get(i))) res = AUTHNAMES[j];
	    }
	    suse.addElement(res);
	}
	DefaultListModel snouse = new DefaultListModel();
	for(int i=0;i<lnouse.size();i++) {
	    String res = null;
	    for(int j=0;j<ALL_AUTHS.length;j++) {
		if(ALL_AUTHS[j].equals((String)lnouse.get(i))) res = AUTHNAMES[j];
	    }
	    snouse.addElement(res);
	}

	useList.setModel(suse);
	dontUseList.setModel(snouse);
	mpUsernameTextField.setText(profile.getApplicationProperty(SshTerminalPanel.PREF_MYPROXY_UNAME, PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_UNAME, System.getProperty("user.name"))));
	//mpHostTextField.setText(profile.getApplicationProperty(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, PreferencesStore.get(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, "myproxy.grid-support.ac.uk")));
    mpHostTextField.setText(profile.getApplicationProperty(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, PreferencesStore.get(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, "myproxy.ngs.ac.uk")));
	mpPortTextField.setText(profile.getApplicationProperty(SshTerminalPanel.PREF_MYPROXY_PORT, PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_PORT, "7513")));	
	bDNTextField.setText(profile.getApplicationProperty(SshTerminalPanel.PREF_BROWSER_DN, PreferencesStore.get(SshTerminalPanel.PREF_BROWSER_DN, "")));
	pkcs12FileTextField.setText(profile.getApplicationProperty(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, PreferencesStore.get(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, "")));
	
	String prefProfile = profile.getApplicationProperty(SshTerminalPanel.PREF_BROWSER_PROFILE, PreferencesStore.get(SshTerminalPanel.PREF_BROWSER_PROFILE, ""));
	String bprofile = Browser.getCurrentBrowser();
	bBrowserComboBox.setEnabled(true);
	bDNTextField.setEnabled(true);
	boolean ok = true;
	if(bprofile==null) {
	    try {
		String profiles[] = uk.ac.rl.esc.browser.Browser.getBrowserList();
		if(profiles==null || profiles.length==0) {
		    bBrowserComboBox.removeAllItems();
		    bBrowserComboBox.addItem("No Browsers found");
		    bBrowserComboBox.setSelectedIndex(0);
		    bBrowserComboBox.setEnabled(false);
		    bDNTextField.setEnabled(false);
		    ok = false;
		} else {
		    bBrowserComboBox.removeAllItems();
		    for(String i : profiles) {
			bBrowserComboBox.addItem(i);
		    }
		}
	    } catch(java.io.IOException e) {
	    } catch(IllegalStateException e) {}
	    if(ok) {
		bBrowserComboBox.insertItemAt(prefProfile, 0);
		bBrowserComboBox.setSelectedIndex(0);
	    }
	} else {
	    bBrowserComboBox.removeAllItems();
	    bBrowserComboBox.addItem("Browser Set for session ("+bprofile+")");
	    bBrowserComboBox.setSelectedIndex(0);
	    bBrowserComboBox.setEnabled(false);
	}
    }

    public void actionPerformed(ActionEvent evt) {
	setAvailableActions();
    }

    public SshToolsConnectionProfile getConnectionProfile() {
	return profile;
    }

    public String getTabContext() {
	return "Connection";
    }

    public Icon getTabIcon() {
	return null;
    }

    public String getTabTitle() {
	return "GSI Defaults";
    }

    public String getTabToolTipText() {
	return "GSI Authentication Defaults Configuration.";
    }

    public int getTabMnemonic() {
	return 'g';
    }

    public Component getTabComponent() {
	return this;
    }

    public boolean validateTab() {
	return true;
    }

    public void tabSelected() {
    }


    private JPanel getJContentPane() {
	if (jContentPane == null) {
	    GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
	    gridBagConstraints15.gridx = 0;
	    gridBagConstraints15.insets = new Insets(10, 0, 0, 0);
	    gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
	    gridBagConstraints15.gridy = 7;
	    mpHostLabel = new JLabel();
	    mpHostLabel.setText("Host:");
	    GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	    gridBagConstraints2.fill = GridBagConstraints.BOTH;
	    gridBagConstraints2.gridy = 4;
	    gridBagConstraints2.weightx = 1.0;
	    gridBagConstraints2.weighty = 1.0;
	    gridBagConstraints2.gridheight = 4;
	    gridBagConstraints2.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints2.gridx = 3;
	    GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
	    gridBagConstraints1.fill = GridBagConstraints.BOTH;
	    gridBagConstraints1.gridy = 4;
	    gridBagConstraints1.weightx = 1.0;
	    gridBagConstraints1.weighty = 1.0;
	    gridBagConstraints1.gridheight = 4;
	    gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints1.gridx = 0;
	    useLabel = new JLabel();
	    useLabel.setText("Use:");
	    dontUseLabel = new JLabel();
	    dontUseLabel.setText("Dont Use:");
	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.gridy = 4;
	    jContentPane = new JPanel();
	    jContentPane.setLayout(new GridBagLayout());
	    jContentPane.add(getUsePanel(), gridBagConstraints);
	    jContentPane.add(getAuthsPanel(), gridBagConstraints15);
	}
	return jContentPane;
    }

    private JPanel getUsePanel() {
	if (usePanel == null) {
	    GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
	    gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints1.gridy = 1;
	    gridBagConstraints1.ipadx = 4;
	    gridBagConstraints1.ipady = 2;
	    gridBagConstraints1.weightx = 0.0D;
	    gridBagConstraints1.gridheight = 7;
	    gridBagConstraints1.fill = GridBagConstraints.BOTH;
	    gridBagConstraints1.gridx = 0;
	    GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
	    gridBagConstraints2.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints2.gridy = 1;
	    gridBagConstraints2.ipadx = 0;
	    gridBagConstraints2.ipady = 0;
	    gridBagConstraints2.gridheight = 7;
	    gridBagConstraints2.fill = GridBagConstraints.BOTH;
	    gridBagConstraints2.gridx = 3;
	    GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
	    gridBagConstraints3.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints3.gridy = 0;
	    gridBagConstraints3.ipadx = 4;
	    gridBagConstraints3.ipady = 2;
	    gridBagConstraints3.gridx = 3;
	    GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
	    gridBagConstraints4.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints4.gridy = 0;
	    gridBagConstraints4.ipadx = 4;
	    gridBagConstraints4.ipady = 2;
	    gridBagConstraints4.gridx = 0;
	    GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
	    gridBagConstraints8.gridx = 1;
	    gridBagConstraints8.insets = new Insets(26, 3, 20, 3);
	    gridBagConstraints8.gridy = 5;
	    GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
	    gridBagConstraints7.gridx = 2;
	    gridBagConstraints7.insets = new Insets(26, 3, 20, 3);
	    gridBagConstraints7.gridy = 5;
	    GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
	    gridBagConstraints6.gridx = 1;
	    gridBagConstraints6.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints6.anchor = GridBagConstraints.WEST;
	    gridBagConstraints6.gridy = 7;
	    GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
	    gridBagConstraints5.gridx = 1;
	    gridBagConstraints5.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints5.anchor = GridBagConstraints.WEST;
	    gridBagConstraints5.gridy = 4;
	    usePanel = new JPanel();
	    usePanel.setLayout(new GridBagLayout());
	    usePanel.setBorder(BorderFactory.createTitledBorder("Authentication Order: "));
	    //BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Authentication Order", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
	    usePanel.add(getUseList(), gridBagConstraints1);
	    usePanel.add(getDontUseList(), gridBagConstraints2);
	    usePanel.add(getUpButton(), gridBagConstraints5);
	    usePanel.add(getDownButton(), gridBagConstraints6);
	    usePanel.add(getStopUseButton(), gridBagConstraints7);
	    usePanel.add(getUseButton(), gridBagConstraints8);
	    usePanel.add(useLabel, gridBagConstraints4);
	    usePanel.add(dontUseLabel, gridBagConstraints3);
	}
	return usePanel;
    }

    private JList getUseList() {
	if (useList == null) {
	    useList = new JList();
	    useList.setPreferredSize(new Dimension(120, 100));
	    useList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    useList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	return useList;
    }

    private JList getDontUseList() {
	if (dontUseList == null) {
	    dontUseList = new JList();
	    dontUseList.setPreferredSize(new Dimension(120, 100));
	    dontUseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    dontUseList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	return dontUseList;
    }

    private JButton getUpButton() {
	if (upButton == null) {
	    upButton = new JButton();
	    //upButton.setText("/\\");
	    upButton.setIcon(new ResourceIcon("/com/sshtools/sshterm/up.png"));
	    upButton.setMargin(new Insets(3,3,3,3));
	    upButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			int i = useList.getSelectedIndex();
			if(i!=-1 && i!=0) {
			    DefaultListModel lm = (DefaultListModel) useList.getModel();
			    Object o = lm.get(i);
			    lm.set(i, lm.get(i-1));
			    lm.set(i-1, o);
			    useList.setSelectedIndex(i-1);
			}
		    }
		});
	}
	return upButton;
    }

    private JButton getDownButton() {
	if (downButton == null) {
	    downButton = new JButton();
	    //downButton.setText("\\/");
	    downButton.setMargin(new Insets(3,3,3,3));
	    downButton.setIcon(new ResourceIcon("/com/sshtools/sshterm/down.png"));
	    downButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			int i = useList.getSelectedIndex();
			DefaultListModel lm = (DefaultListModel) useList.getModel();
			if(i!=-1 && i!=lm.size()-1) {
			    Object o = lm.get(i);
			    lm.set(i, lm.get(i+1));
			    lm.set(i+1, o);
			    useList.setSelectedIndex(i+1);
			}
		    }
		});
	}
	return downButton;
    }

    private JButton getStopUseButton() {
	if (stopUseButton == null) {
	    stopUseButton = new JButton();
	    //stopUseButton.setText("->");
	    stopUseButton.setMargin(new Insets(3,3,3,3));
	    stopUseButton.setIcon(new ResourceIcon("/com/sshtools/sshterm/right.png"));
	    stopUseButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			int i = useList.getSelectedIndex();
			if(i!=-1) {
			    DefaultListModel lmu = (DefaultListModel) useList.getModel();
			    DefaultListModel lmd = (DefaultListModel) dontUseList.getModel();
			    lmd.addElement(lmu.get(i));
			    lmu.remove(i);
			}
		    }
		});
	}
	return stopUseButton;
    }

    private JButton getUseButton() {
	if (useButton == null) {
	    useButton = new JButton();
	    //useButton.setText("<-");
	    useButton.setMargin(new Insets(3,3,3,3));
	    useButton.setIcon(new ResourceIcon("/com/sshtools/sshterm/left.png"));
	    useButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			int i = dontUseList.getSelectedIndex();
			if(i!=-1) {
			    DefaultListModel lmu = (DefaultListModel) useList.getModel();
			    DefaultListModel lmd = (DefaultListModel) dontUseList.getModel();
			    lmu.addElement(lmd.get(i));
			    lmd.remove(i);
			}
		    }
		});
	}
	return useButton;
    }

    private JPanel getMpPanel() {
	if (mpPanel == null) {
	    GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
	    gridBagConstraints14.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints14.gridy = 3;
	    gridBagConstraints14.weightx = 1.0;
	    gridBagConstraints14.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints14.anchor = GridBagConstraints.WEST;
	    gridBagConstraints14.gridx = 2;
	    GridBagConstraints gridBagConstraints14b = new GridBagConstraints();
	    gridBagConstraints14b.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints14b.gridy = 4;
	    gridBagConstraints14b.weightx = 1.0;
	    gridBagConstraints14.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints14b.anchor = GridBagConstraints.WEST;
	    gridBagConstraints14b.gridx = 2;
	    GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
	    gridBagConstraints13.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints13.gridy = 2;
	    gridBagConstraints13.weightx = 1.0;
	    gridBagConstraints13.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints13.anchor = GridBagConstraints.WEST;
	    gridBagConstraints13.gridx = 2;
	    GridBagConstraints gridBagConstraints13b = new GridBagConstraints();
	    gridBagConstraints13b.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints13b.gridy = 1;
	    gridBagConstraints13b.weightx = 1.0;
	    gridBagConstraints13b.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints13b.anchor = GridBagConstraints.WEST;
	    gridBagConstraints13b.gridx = 2;
	    GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
	    gridBagConstraints12.gridx = 1;
	    gridBagConstraints12.insets = new Insets(3, 20, 3, 3);
	    gridBagConstraints12.ipadx = 4;
	    gridBagConstraints12.ipady = 2;
	    gridBagConstraints12.anchor = GridBagConstraints.EAST;
	    gridBagConstraints12.gridy = 3;
	    GridBagConstraints gridBagConstraints12b = new GridBagConstraints();
	    gridBagConstraints12b.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints12b.gridy = 4;
	    gridBagConstraints12b.ipadx = 4;
	    gridBagConstraints12b.ipady = 2;
	    gridBagConstraints12b.weightx = 1.0;
	    gridBagConstraints12b.anchor = GridBagConstraints.EAST;
	    gridBagConstraints12b.gridx = 1;
	    mpPortLabel = new JLabel();
	    mpPortLabel.setText("Port:");
	    GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
	    gridBagConstraints10.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints10.gridy = 2;
	    gridBagConstraints10.ipadx = 4;
	    gridBagConstraints10.ipady = 2;
	    gridBagConstraints10.anchor = GridBagConstraints.EAST;
	    gridBagConstraints10.gridx = 1;
	    GridBagConstraints gridBagConstraints10b = new GridBagConstraints();
	    gridBagConstraints10b.insets = new Insets(3, 20, 3, 3);
	    gridBagConstraints10b.gridy = 1;
	    gridBagConstraints10b.ipadx = 4;
	    gridBagConstraints10b.ipady = 2;
	    gridBagConstraints10b.anchor = GridBagConstraints.EAST;
	    gridBagConstraints10b.gridx = 1;
	    GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
	    gridBagConstraints11.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints11.gridy = 0;
	    gridBagConstraints11.gridwidth = 3;
	    gridBagConstraints11.anchor = GridBagConstraints.WEST;
	    gridBagConstraints11.ipadx = 4;
	    gridBagConstraints11.ipady = 2;
	    gridBagConstraints11.gridx = 0;
	    myProxyLabel = new JLabel();
	    myProxyLabel.setText("MyProxy:");
	    mpUsernameLabel = new JLabel();
	    mpUsernameLabel.setText("Username:");
	    mpVOnameLabel = new JLabel();
	    mpVOnameLabel.setText("VO name:");
	    mpPanel = new JPanel();
	    mpPanel.setLayout(new GridBagLayout());
	    mpPanel.add(myProxyLabel, gridBagConstraints11);
	    mpPanel.add(mpHostLabel, gridBagConstraints10);
	    mpPanel.add(mpUsernameLabel, gridBagConstraints10b);
	    mpPanel.add(mpPortLabel, gridBagConstraints12);
	    mpPanel.add(mpVOnameLabel, gridBagConstraints12b);
	    mpPanel.add(getMpVOnameTextField(), gridBagConstraints14b);
	    mpPanel.add(getMpHostTextField(), gridBagConstraints13);
	    mpPanel.add(getMpPortTextField(), gridBagConstraints14);
	    mpPanel.add(getMpUsernameTextField(), gridBagConstraints13b);
	}
	return mpPanel;
    }

    private JTextField getMpHostTextField() {
	if (mpHostTextField == null) {
	    mpHostTextField = new JTextField();
	    mpHostTextField.setPreferredSize(new Dimension(240, 20));
	}
	return mpHostTextField;
    }

    private JTextField getMpPortTextField() {
	if (mpPortTextField == null) {
	    mpPortTextField = new NumericTextField(new Integer(0), new Integer(65535), new Integer(7513));
	    mpPortTextField.setPreferredSize(new Dimension(60, 20));
	}
	return mpPortTextField;
    }

    private JTextField getMpUsernameTextField() {
	if (mpUsernameTextField == null) {
	    mpUsernameTextField = new JTextField();
	    //CERLANE: Increase with of username textbox
	    mpUsernameTextField.setPreferredSize(new Dimension(100, 20));
	}
	return mpUsernameTextField;
    }
    
    private JTextField getMpVOnameTextField() {
    	if (mpVOnameTextField == null) {
    		mpVOnameTextField = new JTextField();
    		mpVOnameTextField.setPreferredSize(new Dimension(100, 20));
    	}
    	return mpVOnameTextField;
   }

    private JPanel getAuthsPanel() {
	if (authsPanel == null) {
	    GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
	    gridBagConstraints22.gridx = 0;
	    gridBagConstraints22.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints22.gridy = 3;
	    GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
	    gridBagConstraints17.gridx = 0;
	    gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
	    gridBagConstraints17.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints17.gridy = 2;
	    GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
	    gridBagConstraints9.gridx = -1;
	    gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
	    gridBagConstraints9.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints9.gridy = -1;
	    authsPanel = new JPanel();
	    authsPanel.setLayout(new GridBagLayout());
	    authsPanel.setBorder(BorderFactory.createTitledBorder("Authentication Defaults: "));
	    //BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Authentication Defaults", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
	    authsPanel.add(getMpPanel(), gridBagConstraints9);
	    authsPanel.add(getBrowserPanel(), gridBagConstraints17);
	    authsPanel.add(getPkcs12Panel(), gridBagConstraints22);
	}
	return authsPanel;
    }

    private JPanel getBrowserPanel() {
	if (browserPanel == null) {
	    GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
	    gridBagConstraints21.fill = GridBagConstraints.BOTH;
	    gridBagConstraints21.gridy = 2;
	    gridBagConstraints21.weightx = 1.0;
	    gridBagConstraints21.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints21.anchor = GridBagConstraints.WEST;
	    gridBagConstraints21.gridx = 1;
	    GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
	    gridBagConstraints20.gridx = 0;
	    gridBagConstraints20.anchor = GridBagConstraints.EAST;
	    gridBagConstraints20.insets = new Insets(3, 20, 3, 3);
	    gridBagConstraints20.ipadx = 4;
	    gridBagConstraints20.ipady = 2;
	    gridBagConstraints20.gridy = 2;
	    dNLabel = new JLabel();
	    dNLabel.setText("DN:");
	    GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
	    gridBagConstraints19.gridx = 0;
	    gridBagConstraints19.insets = new Insets(3, 28, 3, 3);
	    gridBagConstraints19.ipadx = 4;
	    gridBagConstraints19.ipady = 2;
	    gridBagConstraints19.anchor = GridBagConstraints.EAST;
	    gridBagConstraints19.gridy = 1;
	    browserBrowserLabel = new JLabel();
	    browserBrowserLabel.setText("Browser:");
	    GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
	    gridBagConstraints18.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints18.gridy = 1;
	    gridBagConstraints18.weightx = 1.0;
	    gridBagConstraints18.anchor = GridBagConstraints.WEST;
	    gridBagConstraints18.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints18.gridx = 1;
	    GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
	    gridBagConstraints16.gridx = 0;
	    gridBagConstraints16.ipadx = 4;
	    gridBagConstraints16.ipady = 2;
	    gridBagConstraints16.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints16.gridwidth = 2;
	    gridBagConstraints16.anchor = GridBagConstraints.WEST;
	    gridBagConstraints16.gridy = 0;
	    browserLabel = new JLabel();
	    browserLabel.setText("Browser:");
	    browserPanel = new JPanel();
	    browserPanel.setLayout(new GridBagLayout());
	    browserPanel.add(getBBrowserComboBox(), gridBagConstraints18);
	    browserPanel.add(browserLabel, gridBagConstraints16);
	    browserPanel.add(browserBrowserLabel, gridBagConstraints19);
	    browserPanel.add(dNLabel, gridBagConstraints20);
	    browserPanel.add(getBDNTextField(), gridBagConstraints21);
	}
	return browserPanel;
    }

    private JComboBox getBBrowserComboBox() {
	if (bBrowserComboBox == null) {
	    bBrowserComboBox = new JComboBox();
	    bBrowserComboBox.setPreferredSize(new Dimension(240, 25));
	}
	return bBrowserComboBox;
    }

    private JTextField getBDNTextField() {
	if (bDNTextField == null) {
	    bDNTextField = new JTextField();
	    bDNTextField.setPreferredSize(new Dimension(240, 20));
	}
	return bDNTextField;
    }

    private JPanel getPkcs12Panel() {
    /**
     * This method initializes pkcs12FileTextField	
     * 	
     * @return javax.swing.JTextField	
     */
	if (pkcs12Panel == null) {
	    GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
	    gridBagConstraints26.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints26.gridy = 1;
	    gridBagConstraints26.weightx = 1.0;
	    gridBagConstraints26.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints26.gridx = 4;
	    GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
	    gridBagConstraints25.fill = GridBagConstraints.VERTICAL;
	    gridBagConstraints25.gridy = 1;
	    gridBagConstraints25.weightx = 1.0;
	    gridBagConstraints25.insets = new Insets(3, 3, 3, 3);
	    gridBagConstraints25.gridx = 3;
	    GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
	    gridBagConstraints24.gridx = 2;
	    gridBagConstraints24.anchor = GridBagConstraints.WEST;
	    gridBagConstraints24.insets = new Insets(3, 50, 3, 3);
	    gridBagConstraints24.ipadx = 4;
	    gridBagConstraints24.ipady = 2;
	    gridBagConstraints24.gridy = 1;
	    pkcs12FileLabel = new JLabel();
	    pkcs12FileLabel.setText("File:");
	    GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
	    gridBagConstraints23.anchor = GridBagConstraints.WEST;
	    gridBagConstraints23.gridx = 0;
	    gridBagConstraints23.gridy = 0;
	    gridBagConstraints23.gridwidth = 3;
	    gridBagConstraints23.ipadx = 4;
	    gridBagConstraints23.ipady = 2;
	    gridBagConstraints23.insets = new Insets(3, 3, 3, 3);
	    pkcs12Label = new JLabel();
	    pkcs12Label.setText("PKCS12:");
	    pkcs12Panel = new JPanel();
	    pkcs12Panel.setLayout(new GridBagLayout());
	    pkcs12Panel.add(pkcs12Label, gridBagConstraints23);
	    pkcs12Panel.add(pkcs12FileLabel, gridBagConstraints24);
	    pkcs12Panel.add(getPkcs12FileTextField(), gridBagConstraints25);
	    pkcs12Panel.add(getPkcs12Button(), gridBagConstraints26);
	}
	return pkcs12Panel;
    }

    private JTextField getPkcs12FileTextField() {
	if (pkcs12FileTextField == null) {
	    pkcs12FileTextField = new JTextField();
	    pkcs12FileTextField.setPreferredSize(new Dimension(150, 20));
	}
	return pkcs12FileTextField;
    }

    private JButton getPkcs12Button() {
	if (pkcs12Button == null) {
	    pkcs12Button = new JButton();
	    pkcs12Button.setText("Browse...");
            pkcs12Button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
		    {
			JFileChooser chooser = new JFileChooser();
			com.sshtools.j2ssh.authentication.ExampleFileFilter filter = new com.sshtools.j2ssh.authentication.ExampleFileFilter();
			filter.addExtension("pfx");
			filter.addExtension("p12");
			filter.setDescription("pfx and p12 files");
			chooser.setFileFilter(filter);
			chooser.setFileHidingEnabled(false);
			chooser.setDialogTitle("Select Certificate File For Authentication");
			
			if (chooser.showOpenDialog(GSIAuthTab.this) == JFileChooser.APPROVE_OPTION) {
			    pkcs12FileTextField.setText( chooser.getSelectedFile().getAbsolutePath());
			}
		    }
		});
	}
	return pkcs12Button;
    }


}

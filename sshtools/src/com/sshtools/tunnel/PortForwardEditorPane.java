//Changes (c) STFC/CCLRC 2007
/*
 *  Sshtools - SSHTerm
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.tunnel;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;

public class PortForwardEditorPane
    extends JPanel
    implements ActionListener {
  private static int id = 1000;
  //  Private instance variables
  private NumericTextField listenPortI;
  private NumericTextField destPortI;
  private NumericTextField listenPortO;
  private NumericTextField destPortO;
  private XTextField destHostI;
  private XTextField destHostO;
  private XTextField name;
  private JTabbedPane tabbedPane;
  private ForwardingConfiguration config;

  public PortForwardEditorPane() {
    super(new GridBagLayout());
    init();
  }

  public PortForwardEditorPane(ForwardingConfiguration config) {
    super(new GridBagLayout());
    this.config = config;
    init();

    // Populate the textfields from the ForwardingConfiguration object
    name.setText(config.getName());
    if(config instanceof com.sshtools.j2ssh.forwarding.ForwardingClient.ClientForwardingListener) {
        tabbedPane.setSelectedIndex(1); // incoming
    } else {
        tabbedPane.setSelectedIndex(0); // outgoing
    }
    listenPortI.setValue(new Integer(config.getPortToBind()));
    listenPortO.setValue(new Integer(config.getPortToBind()));
    destPortI.setValue(new Integer(config.getPortToConnect()));
    destPortO.setValue(new Integer(config.getPortToConnect()));
    destHostI.setText(config.getHostToConnect());
    destHostO.setText(config.getHostToConnect());
  }

  void init() {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.WEST;
      Insets normal = new Insets(2, 2, 2, 2);
      Insets indented = new Insets(2, 26, 2, 2);
      gbc.insets = normal;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(this, new JLabel("Name "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(this, name = new XTextField(getNextAutoId(), 10),
			 gbc, GridBagConstraints.REMAINDER);
      tabbedPane = new JTabbedPane();
      
      //////// Incoming

      JPanel pIncoming = new JPanel(new GridBagLayout());

      // port to listen on, on remote server
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(pIncoming, new JLabel("Listening Port (remote server): "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(pIncoming,
			 listenPortI = new NumericTextField(new Integer(0),
							   new Integer(65535), new Integer(0)), gbc,
			 GridBagConstraints.REMAINDER);

      // where to connect to locally
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(pIncoming, new JLabel("Destination Host: "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(pIncoming, destHostI = new XTextField("localhost", 10), gbc,
			 GridBagConstraints.REMAINDER);
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(pIncoming, new JLabel("Destination Port: "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(pIncoming,
			 destPortI = new NumericTextField(new Integer(0),
							  new Integer(65535), new Integer(0)), gbc,
			 GridBagConstraints.REMAINDER);
      tabbedPane.addTab("Incoming", null, pIncoming, "Tunnel from remote host to local host");
      tabbedPane.setMnemonicAt(0, KeyEvent.VK_I);

      ///////// Outgoing

      JPanel pOutgoing = new JPanel(new GridBagLayout());

      // port to listen on, on local host
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(pOutgoing, new JLabel("Listening Port (local): "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(pOutgoing,
			 listenPortO = new NumericTextField(new Integer(0),
							   new Integer(65535), new Integer(0)), gbc,
			 GridBagConstraints.REMAINDER);


      // where to connect to locally
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(pOutgoing, new JLabel("Destination Host: "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(pOutgoing, destHostO = new XTextField("localhost", 10), gbc,
			 GridBagConstraints.REMAINDER);
      gbc.weightx = 1.0;
      UIUtil.jGridBagAdd(pOutgoing, new JLabel("Destination Port: "), gbc,
			 GridBagConstraints.RELATIVE);
      gbc.weightx = 0.0;
      UIUtil.jGridBagAdd(pOutgoing,
			 destPortO = new NumericTextField(new Integer(0),
							  new Integer(65535), new Integer(0)), gbc,
			 GridBagConstraints.REMAINDER);
      tabbedPane.addTab("Outgoing", null, pOutgoing, "Tunnel from local host to remote host");
      tabbedPane.setMnemonicAt(1, KeyEvent.VK_O);
      
      gbc.gridwidth = 2;
      UIUtil.jGridBagAdd(this, tabbedPane, gbc, GridBagConstraints.REMAINDER);
  }

  public void actionPerformed(ActionEvent evt) {
    
  }

  protected String getNextAutoId() {
      return "#".concat(String.valueOf(++id));
  }

  public boolean isLocal() {
      return tabbedPane.getSelectedIndex()==1; // incoming
  }

  public boolean isRemote() {
      return tabbedPane.getSelectedIndex()==0; // outgoing
  }

  public String getForwardName() {
      return name.getText();
  }

  public int getLocalPort() {
      if(tabbedPane.getSelectedIndex()==0) {
	  return ( (Integer) listenPortI.getValue()).intValue();
      } else {
	  return ( (Integer) listenPortO.getValue()).intValue();
      }
  }

  public String getBindAddress() {
      return "127.0.0.1";
  }

  public int getRemotePort() {
      if(tabbedPane.getSelectedIndex()==0) {
	  return ( (Integer) destPortI.getValue()).intValue();
      } else {
	  return ( (Integer) destPortO.getValue()).intValue();
      }
  }

  public String getHost() {
      if(tabbedPane.getSelectedIndex()==0) {
	  return destHostI.getText();
      } else {
	  return destHostO.getText();
      }
  }

}

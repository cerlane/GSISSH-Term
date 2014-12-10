/*
 *  SSHTools - VO Management Tool
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

package com.sshtools.common.vomanagementtool;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.common.vomanagementtool.common.VOHelper;

public class ExtraPanel {
	public static class VOMSLocationPanel extends JPanel implements DocumentListener, ActionListener {	
	
		private JPanel vomsSetupPanel;
		private JButton browse;
		private XTextField vomslocation;
	
		/**
		 * Creates a new GOTaskPanel object.
		 * @throws Exception 
		 */
		public VOMSLocationPanel()  {
			super();
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
	
			vomsSetupPanel = new JPanel (new GridBagLayout());
			vomsSetupPanel.setBorder(BorderFactory.createTitledBorder("Preferred VOMS Location"));
	
			//VOMS Location
			gbc.insets = indentedInsets;
			UIUtil.jGridBagAdd(vomsSetupPanel, new JLabel("VOMS Location:"), gbc, 1);   
	
			gbc.insets = normalInsets;
			gbc.weightx = 2.0;
			String vomsDir = VOHelper.getVomslocation();
			vomslocation =  new XTextField(vomsDir, 25);	
			UIUtil.jGridBagAdd(vomsSetupPanel, vomslocation, gbc, GridBagConstraints.RELATIVE);
			
			
			gbc.weightx = 0.0;
			browse = new JButton("Browse");
			browse.addActionListener(this);
			UIUtil.jGridBagAdd(vomsSetupPanel, browse, gbc, GridBagConstraints.REMAINDER);
			
			gbc.insets = indentedInsets;
		    gbc.weightx = 0.0;
		    JLabel infoLabel = new JLabel("'vomsdir' dirctory will be at {VOMS Location}" +
		    		File.separator + "voms. 'vomses' directory will be at {VOMS Location}" +
		    		File.separator + "vomses.");
		    infoLabel.setFont(new Font("Serif", Font.ITALIC, 14));
		    UIUtil.jGridBagAdd(vomsSetupPanel, infoLabel, gbc, GridBagConstraints.REMAINDER);
			
			add(vomsSetupPanel, BorderLayout.SOUTH);
		}

	
		@Override
		public void actionPerformed(ActionEvent evt) {
	  
			if (evt.getSource() == browse) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileHidingEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (getVOMSLocation()!=null && !getVOMSLocation().equals("")){
					chooser.setSelectedFile(new File(getVOMSLocation()));
				}
				chooser.setDialogTitle("Select VOMS location:");
	
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {			
					vomslocation.setText(chooser.getSelectedFile().getPath());			
					
				}
			}
			
		}
	

	
		/**
		 *
		 *
		 * @return
		 */
		public String getVOMSLocation() {
			return vomslocation.getText().trim();
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	
	}
	
	public static class NewVOPanel extends JPanel implements DocumentListener, ActionListener {	
		
		private JPanel voPanel;
		private XTextField voName;
		private XTextField server;
		private NumericTextField port;
		private XTextField serverDN;
		private XTextField serverIssuerDN;

		private JButton ok;
		private JButton cancel;
		
		private boolean status = false;
	
		/**
		 * Creates a new GOTaskPanel object.
		 * @throws Exception 
		 */
		public NewVOPanel()  {
			super();
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
	
			voPanel = new JPanel (new GridBagLayout());
			voPanel.setBorder(BorderFactory.createTitledBorder("New VO"));
	
			//VO Name
			gbc.insets = indentedInsets;
			UIUtil.jGridBagAdd(voPanel, new JLabel("VO name:"), gbc, 1);
			gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
			voName =  new XTextField(20);	
			UIUtil.jGridBagAdd(voPanel, voName, gbc, GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(voPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
						
			//Server
			gbc.insets = indentedInsets;
		    UIUtil.jGridBagAdd(voPanel, new JLabel("Server:"), gbc, 1);
		    
		    gbc.insets = normalInsets;
		    gbc.weightx = 1.0;
			server =  new XTextField(20);	
			UIUtil.jGridBagAdd(voPanel, server, gbc, GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(voPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
			
			//Port
			gbc.insets = indentedInsets;
		    UIUtil.jGridBagAdd(voPanel, new JLabel("Port:"), gbc, 1);
		    
		    gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
			port = new NumericTextField(new Integer(0), new Integer(100000));			
			UIUtil.jGridBagAdd(voPanel, port, gbc, GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(voPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
			
			//Server DN
			gbc.insets = indentedInsets;
		    UIUtil.jGridBagAdd(voPanel, new JLabel("Server DN:"), gbc, 1);
		    
		    gbc.insets = normalInsets;
		    gbc.weightx = 2.0;
			serverDN =  new XTextField(20);	
			UIUtil.jGridBagAdd(voPanel, serverDN, gbc, GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(voPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);;
			
			//Server Issuer DN
			gbc.insets = indentedInsets;
			gbc.weightx = 2.0;
		    UIUtil.jGridBagAdd(voPanel, new JLabel("Server Certificate Issuer DN:"), gbc, 1);
		    
		    gbc.insets = normalInsets;
			serverIssuerDN =  new XTextField(20);	
			UIUtil.jGridBagAdd(voPanel, serverIssuerDN, gbc, GridBagConstraints.RELATIVE);
			UIUtil.jGridBagAdd(voPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
			
			//Info
			gbc.insets = indentedInsets;
		    JLabel infoLabel = new JLabel("Please refer to your respective VO configuration webpage or contact your VO administrator for the above information.");
		    infoLabel.setFont(new Font("Serif", Font.ITALIC, 14));
		    UIUtil.jGridBagAdd(voPanel, infoLabel, gbc, GridBagConstraints.REMAINDER);
		 
		    
			add(voPanel, BorderLayout.SOUTH);
		}

	
		@Override
		public void actionPerformed(ActionEvent evt) {
	  
			
			
		}
		/**
		 *
		 *
		 * @return
		 */
		public String getServerIssuerDN() {
			return serverIssuerDN.getText().trim();
		}
		
		/**
		 *
		 *
		 * @return
		 */
		public String getServerDN() {
			return serverDN.getText().trim();
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
		public String getServer() {
			return server.getText().trim();
		}
	
		/**
		 *
		 *
		 * @return
		 */
		public String getVOName() {
			return voName.getText().trim();
		}

		


		public boolean isStatus() {
			return status;
		}


		@Override
		public void changedUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void insertUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void removeUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	
	}

}

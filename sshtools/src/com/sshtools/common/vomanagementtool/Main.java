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

import java.util.Date;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.vomanagementtool.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.vomanagementtool.common.ConfigHelper;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.sshterm.SshTerminalPanel;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class Main
extends JFrame
implements ActionListener {
	//  Statics

	String userName = "";
	String hostName = "";
	int port = 0;
	int lifetime = 0;
	String certType= null;
	String dn = null;
	boolean voms = false;


	JButton close;
	JButton generate;
	VOManagementToolPanel vopanel;
	JComboBox action;
	
	
	
	/**
	 * Creates a new Main object.
	 */
	public Main() {
		super("VO Management Tool");
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
	        	PreferencesStore.put(SshTerminalPanel.PREF_VOMS_LOCATION, VOHelper.getVomslocation());
	        	//System.exit(0);
	        }
	    });
		
		ConfigHelper.setAuthentication(false);
		ConfigHelper.setVisibleRowCount(20);
		
		//Check if voms dir is ok for user
		VOHelper.createVOMSLOCATIONDialog();
		
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
		
		JPanel iconPanel = new JPanel(new GridBagLayout());    
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 2, 4, 2);
		gbc.weightx = 0.0;
		JLabel VOTitleLabel = new JLabel("Manage your VO");
		VOTitleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		UIUtil.jGridBagAdd(iconPanel, VOTitleLabel, gbc, GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		IconWrapperPanel northPanel = new IconWrapperPanel(new ResourceIcon(this.getClass(), ConfigHelper.ICON), iconPanel);
		northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		VOManagementToolPanel voPanel = new VOManagementToolPanel();
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		centerPanel.add(voPanel);
		
		//  Wrap the whole thing in an empty border
		JPanel mainPanel = new JPanel(new BorderLayout());
		JScrollPane mainScroller = new JScrollPane(mainPanel);  
		mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));    
		mainPanel.add(northPanel, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);

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
		
		
	}
	/**
	 *
	 *
	 * @return
	 */
	public int getAction() {
		return action.getSelectedIndex();
	}
	
	
	/**
	 *
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		}

		Main main = new Main();
		main.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});

		main.pack();
		UIUtil.positionComponent(SwingConstants.CENTER, main);
		main.setVisible(true);

	}



}

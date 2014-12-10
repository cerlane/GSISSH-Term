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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;

import org.globus.gsi.GSIConstants;

import com.sshtools.common.vomanagementtool.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.vomanagementtool.common.ConfigHelper;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.sshterm.SshTerminalPanel;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class VOSelectedPanel
    extends JPanel
    implements DocumentListener,
    ActionListener {  
	
    
  /**
   * Creates a new VOSelectedPanel object.
   */
  public VOSelectedPanel() {
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


	JPanel selectedPanel = new JPanel(new GridBagLayout());
	selectedPanel.setBorder(BorderFactory.createTitledBorder("Selected VOs"));

	gbc.insets = indentedInsets;
	ConfigHelper.selectedTree = new MyTree(false);
	ConfigHelper.selectedRoot = new MyTreeNode(1, 0, "Selected" , false);
	ConfigHelper.selectedTreeModel = new DefaultTreeModel(ConfigHelper.selectedRoot);
	ConfigHelper.selectedTree.setRootVisible(false);
	ConfigHelper.selectedTree.setModel(ConfigHelper.selectedTreeModel);
	ConfigHelper.selectedTree.setVisibleRowCount(ConfigHelper.getVisibleRowCount());
	JScrollPane selectedScrollPane = new JScrollPane(ConfigHelper.selectedTree);
	selectedScrollPane.setWheelScrollingEnabled(true);
	
	UIUtil.jGridBagAdd(selectedPanel, selectedScrollPane, gbc, 1);
	UIUtil.jGridBagAdd(selectedPanel, new JLabel(), gbc, GridBagConstraints.RELATIVE);
	
	gbc.gridx = 0;
	JLabel text = new JLabel("Right-click and select 'Clear' to remove.");		
	UIUtil.jGridBagAdd(selectedPanel, text, gbc, 1);
	
	
	/*JPanel advancePanel = new JPanel(new GridBagLayout());
	advancePanel.setBorder(BorderFactory.createTitledBorder("Advanced"));
	gbc.insets = indentedInsets;	
	UIUtil.jGridBagAdd(advancePanel, new JLabel("Proxy Type: "), gbc, 1);
	JComboBox proxyType = new JComboBox(new String[] {
			"RFC Impersonation", "Pre-RFC Impersonation", "Legacy"                          
			});	
	String cur = PreferencesStore.get(SshTerminalPanel.PREF_PROXY_TYPE, Integer.toString(GSIConstants.GSI_2_PROXY));
	if (cur.equals(GSIConstants.GSI_2_PROXY))
		proxyType.setSelectedIndex(2);
	else if (cur.equals(GSIConstants.GSI_3_IMPERSONATION_PROXY))
		proxyType.setSelectedIndex(1);				
	UIUtil.jGridBagAdd(advancePanel, proxyType, gbc, GridBagConstraints.REMAINDER);*/
	
	
	add(selectedPanel, BorderLayout.NORTH);
	//add (advancePanel, BorderLayout.CENTER);

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

@Override
public void actionPerformed(ActionEvent arg0) {
	// TODO Auto-generated method stub
	
}

  
}

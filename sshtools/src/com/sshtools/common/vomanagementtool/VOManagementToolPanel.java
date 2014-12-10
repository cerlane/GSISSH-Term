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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;

import com.sshtools.common.vomanagementtool.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.vomanagementtool.common.ConfigHelper;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.common.globusonlinetool.myswingutils.DragDropTree;
import com.sshtools.common.ui.UIUtil;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class VOManagementToolPanel
    extends JPanel
    implements DocumentListener,
    ActionListener {  
	
    
  /**
   * Creates a new VOManagementToolPanel object.
   */
  public VOManagementToolPanel() {
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
	
	
	if (ConfigHelper.isAuthentication()){
		JPanel allPanel = new JPanel(new GridBagLayout());
		allPanel.setBorder(BorderFactory.createTitledBorder("VOs"));
		gbc.insets = indentedInsets;
		
		MyTree rootTree = new MyTree(false);
		MyTreeNode allRoot = new MyTreeNode(1, 0, "VOs" , false);
		DefaultTreeModel allTreeModel = new DefaultTreeModel(allRoot);
		rootTree.setRootVisible(false);
		rootTree.setVisibleRowCount(ConfigHelper.getVisibleRowCount());
		rootTree.setModel(allTreeModel);
		
		ConfigHelper.favRoot = new MyTreeNode(1, 1, VOHelper.FAVOURITES, false);
		allTreeModel.insertNodeInto(ConfigHelper.favRoot, allRoot, 0);
		ConfigHelper.favRoot.loadChildren(allTreeModel, VOHelper.FAVOURITES, true);	
		
				
		MyTreeNode egiRoot = new MyTreeNode(1, 0, VOHelper.EGI, false);
		allTreeModel.insertNodeInto(egiRoot, allRoot, 1);	
		egiRoot.loadChildren(allTreeModel, VOHelper.EGI , true);
		
		MyTreeNode igeRoot = new MyTreeNode(1, 0, VOHelper.OTHERS , false);
		allTreeModel.insertNodeInto(igeRoot, allRoot, 2);
		igeRoot.loadChildren(allTreeModel, VOHelper.OTHERS , true);
		
		rootTree.expandRow(0);
		
		JScrollPane allScrollPane = new JScrollPane(rootTree);
		allScrollPane.setWheelScrollingEnabled(true);
		UIUtil.jGridBagAdd(allPanel, allScrollPane, gbc, 1);
		UIUtil.jGridBagAdd(allPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
		

		JLabel text = new JLabel("Double-click to add to 'Selected VOs'.");		
		UIUtil.jGridBagAdd(allPanel, text, gbc, 1);
		
		add(allPanel,BorderLayout.NORTH);
	}
	else{
		/*
		 * Favourites
		 */
		JPanel favouritesPanel = new JPanel(new GridBagLayout());
		favouritesPanel.setBorder(BorderFactory.createTitledBorder(VOHelper.FAVOURITES + " (customisable)"));
		
		gbc.insets = indentedInsets;
		
		//Favourite tree
		ConfigHelper.favTree = new MyTree(true);	
		
		ConfigHelper.favRoot = new MyTreeNode(1, 0, VOHelper.FAVOURITES, false);
		DefaultTreeModel favTreeModel = new DefaultTreeModel(ConfigHelper.favRoot);
		ConfigHelper.favTree.setModel(favTreeModel);
		ConfigHelper.favTree.setVisibleRowCount(ConfigHelper.getVisibleRowCount());
		ConfigHelper.favRoot.loadChildren(favTreeModel, VOHelper.FAVOURITES, true);	
		JScrollPane favScrollPane = new JScrollPane(ConfigHelper.favTree);
		favScrollPane.setWheelScrollingEnabled(true);
		UIUtil.jGridBagAdd(favouritesPanel, favScrollPane, gbc, GridBagConstraints.RELATIVE);
		add(favouritesPanel,BorderLayout.WEST);
		
		
		/*
		 * EGI
		 */
		JPanel egiPanel = new JPanel(new GridBagLayout());
		egiPanel.setBorder(BorderFactory.createTitledBorder(VOHelper.EGI + " (automatically downloaded)"));
		
		gbc.insets = indentedInsets;
		//EGI tree
		MyTree egiTree = new MyTree(false);	
		
		MyTreeNode egiRoot = new MyTreeNode(1, 0, VOHelper.EGI, false);
		DefaultTreeModel egiTreeModel = new DefaultTreeModel(egiRoot);
		egiTree.setModel(egiTreeModel);
		egiTree.setVisibleRowCount(ConfigHelper.getVisibleRowCount());
		egiRoot.loadChildren(egiTreeModel, VOHelper.EGI , true);	
		JScrollPane egiScrollPane = new JScrollPane(egiTree);
		egiScrollPane.setWheelScrollingEnabled(true);
		UIUtil.jGridBagAdd(egiPanel, egiScrollPane, gbc, GridBagConstraints.RELATIVE);
		add(egiPanel,BorderLayout.CENTER);
		
		
		/*
		 * IGE
		 */
		JPanel igePanel = new JPanel(new GridBagLayout());
		igePanel.setBorder(BorderFactory.createTitledBorder(VOHelper.OTHERS+" (automatically downloaded)"));
		
		gbc.insets = indentedInsets;
		//EGI tree
		MyTree igeTree = new MyTree(false);	
		
		MyTreeNode igeRoot = new MyTreeNode(1, 0, VOHelper.OTHERS , false);
		DefaultTreeModel igeTreeModel = new DefaultTreeModel(igeRoot);
		igeTree.setModel(igeTreeModel);
		igeTree.setVisibleRowCount(ConfigHelper.getVisibleRowCount());
		igeRoot.loadChildren(igeTreeModel, VOHelper.OTHERS, true);	
		JScrollPane igeScrollPane = new JScrollPane(igeTree);
		igeScrollPane.setWheelScrollingEnabled(true);
		UIUtil.jGridBagAdd(igePanel, igeScrollPane, gbc, GridBagConstraints.RELATIVE);
		add(igePanel,BorderLayout.EAST);
	}
	
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

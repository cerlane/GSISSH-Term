/*
 *  SSHTools - Globus Online Tool
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

package com.sshtools.common.globusonlinetool;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.sshtools.common.globusonlinetool.myswingutils.DragDropTree;
import com.sshtools.common.globusonlinetool.myswingutils.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.globusonlinetool.myswingutils.Java2sAutoComboBox;
import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class GlobusOnlineToolPanel
extends JPanel
implements ItemListener,
ActionListener {  

	//  Private instance variables
	private Java2sAutoComboBox endpoint1;
	private Java2sAutoComboBox endpoint2;
	private JLabel myproxyServerLifetimeLabel;
	private JLabel directory1Label;
	private JLabel directory2Label;
	private DragDropTree fileDir1;
	private DragDropTree fileDir2;	
	private JScrollPane fileDir1ScrollPane;
	private JScrollPane fileDir2ScrollPane;
	private JButton go1;
	private JButton go2;
	private XTextField directory1;
	private XTextField directory2;
	private JPasswordField localPassphrase;
	private NumericTextField port;
	private NumericTextField lifetime;
	private DefaultTreeModel treeModel1;
	private DefaultTreeModel treeModel2;	

	private List <String> allEndPts=null;
	private static String ICON = Main.ICON;



	/**
	 * Creates a new GlobusOnlineToolPanel object.
	 * @throws Exception 
	 */
	public GlobusOnlineToolPanel()  {
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
		
		
		/*
		 * GO Required information Panel
		 */
		JPanel goUsernamePanel = new JPanel(new GridBagLayout());
		goUsernamePanel.setBorder(BorderFactory.createTitledBorder("Globus Online Transfer"));



		/*
		 * firstEndptPanel
		 */
		JPanel firstEndptPanel = new JPanel(new GridBagLayout());
		firstEndptPanel.setBorder(BorderFactory.createTitledBorder("First Endpoint"));

		gbc.insets = indentedInsets;
		UIUtil.jGridBagAdd(firstEndptPanel, new JLabel("Endpoint 1:"), gbc, 1);


		try {
			allEndPts = GOHelper.getAllEndpoints();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		endpoint1 = new Java2sAutoComboBox(allEndPts);
		endpoint1.addItemListener(this);


		gbc.insets = normalInsets;
		gbc.weightx = 2.0;
		//UIUtil.jGridBagAdd(firstEndptPanel, endpoint1CB, gbc, GridBagConstraints.RELATIVE); 
		UIUtil.jGridBagAdd(firstEndptPanel, endpoint1, gbc, GridBagConstraints.RELATIVE);
		UIUtil.jGridBagAdd(firstEndptPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);

		//Initial Directory   
		gbc.insets = indentedInsets;
		directory1Label = new JLabel("Directory:");
		UIUtil.jGridBagAdd(firstEndptPanel, directory1Label, gbc, 1);
		gbc.insets = normalInsets;
		gbc.weightx = 1.0;
		String defaultDir = GOHelper.retrieveEndPointDefaultDirectory(allEndPts.get(0));
		directory1 = new XTextField(defaultDir, 30);	
		directory1Label.setLabelFor(directory1);    
		UIUtil.jGridBagAdd(firstEndptPanel, directory1, gbc, GridBagConstraints.RELATIVE);
		// UIUtil.jGridBagAdd(firstEndptPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);

		go1 = new JButton("GO");
		go1.addActionListener(this);
		UIUtil.jGridBagAdd(firstEndptPanel, go1, gbc, GridBagConstraints.REMAINDER);


		//File browser
		gbc.insets = indentedInsets;	 
		fileDir1 = new DragDropTree(treeModel1);

		fileDir1.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				TreePath path = event.getPath();
				if (path.getLastPathComponent() instanceof MyTreeNode) {
					MyTreeNode node = (MyTreeNode) path.getLastPathComponent();
					String currentDir = node.getFullPath();
					node.loadChildren(treeModel1, getEndPoint1(), currentDir);
				}
			}
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

			}
		});
		fileDir1.setModel(treeModel1); 

		fileDir1ScrollPane = new JScrollPane(fileDir1);
		fileDir1ScrollPane.setWheelScrollingEnabled(true);
		UIUtil.jGridBagAdd(firstEndptPanel, fileDir1ScrollPane, gbc, GridBagConstraints.RELATIVE);

		add(firstEndptPanel,BorderLayout.CENTER);


		/*
		 * 2ndEndptPanel
		 */
		JPanel secondEndptPanel = new JPanel(new GridBagLayout());
		secondEndptPanel.setBorder(BorderFactory.createTitledBorder("Second Endpoint"));

		gbc.insets = indentedInsets;
		UIUtil.jGridBagAdd(secondEndptPanel, new JLabel("Endpoint 2:"), gbc, 1);


		endpoint2 = new Java2sAutoComboBox(allEndPts);
		endpoint2.addItemListener(this);

		gbc.insets = normalInsets;
		gbc.weightx = 2.0;
		UIUtil.jGridBagAdd(secondEndptPanel, endpoint2, gbc, GridBagConstraints.RELATIVE);
		UIUtil.jGridBagAdd(secondEndptPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);

		//Initial Directory   
		gbc.insets = indentedInsets;
		directory2Label = new JLabel("Directory:");
		UIUtil.jGridBagAdd(secondEndptPanel, directory2Label, gbc, 1);
		gbc.insets = normalInsets;
		gbc.weightx = 1.0;  	
		directory2 = new XTextField(defaultDir, 30);
		directory2Label.setLabelFor(directory1);    
		UIUtil.jGridBagAdd(secondEndptPanel, directory2, gbc, GridBagConstraints.RELATIVE);
		// UIUtil.jGridBagAdd(secondEndptPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);


		//GO button
		go2 = new JButton("GO");
		go2.addActionListener(this);
		UIUtil.jGridBagAdd(secondEndptPanel, go2, gbc, GridBagConstraints.REMAINDER);


		//File browser
		gbc.insets = indentedInsets; 
		fileDir2 = new DragDropTree(treeModel2);			 
		fileDir2.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				TreePath path = event.getPath();
				if (path.getLastPathComponent() instanceof MyTreeNode && !((MyTreeNode)path.getLastPathComponent()).isLeaf()) {
					MyTreeNode node = (MyTreeNode) path.getLastPathComponent();
					String currentDir = node.getFullPath();
					node.loadChildren(treeModel2, getEndPoint2(), currentDir);
				}
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

			}
		});	 
		fileDir2.setModel(treeModel2);  

		fileDir2ScrollPane = new JScrollPane(fileDir2);
		fileDir2ScrollPane.setWheelScrollingEnabled(true);
		UIUtil.jGridBagAdd(secondEndptPanel, fileDir2ScrollPane, gbc, GridBagConstraints.RELATIVE);

		add(secondEndptPanel,BorderLayout.EAST);

		

		/* //  Build this panel
    	setLayout(new BorderLayout());    
	    add(localCertificatePanel, BorderLayout.CENTER);
	    add(myproxyPanel,BorderLayout.SOUTH);*/
	}



	public DefaultMutableTreeNode addNode(DefaultMutableTreeNode curTop, String endpoint, String dir) {
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir);
		if (curTop != null) { // should only be null at root
			curTop.add(curDir);
		}
		Vector ol = new Vector();

		try {
			List<Map> allDir = GOHelper.displayLs(endpoint, dir);
			for(int i=0; i<allDir.size(); i++){
				ol.addElement(allDir.get(i).get("name"));
			}
			Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);

			Vector files = new Vector();
			for (int i = 0; i < ol.size(); i++) {
				String thisObject = (String) ol.elementAt(i);
				String newPath = thisObject;

				if (allDir.get(i).get("type").equals("dir"))
					addNode(curDir, endpoint, dir+ File.separator + allDir.get(i).get("name"));
				else
					files.addElement(thisObject);
			}

			// Pass two: for files.
			for (int fnum = 0; fnum < files.size(); fnum++){
				curDir.add(new DefaultMutableTreeNode(files.elementAt(fnum)));
			}



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return curDir;
	}

	public void triggerLifetime(boolean isEnabled){
		lifetime.setEnabled(isEnabled);
		myproxyServerLifetimeLabel.setEnabled(isEnabled);
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
	public String getEndPoint1() {
		return (String) endpoint1.getSelectedItem();
	}

	/**
	 *
	 *
	 * @return
	 */
	public String getEndPoint2() {
		return (String) endpoint2.getSelectedItem();
	}
	/**
	 *
	 *
	 * @return
	 */
	public int getLifetime() {
		return ( (Integer) lifetime.getValue()).intValue();
	}
	/**
	 *
	 *
	 * @return
	 */ 
	public String getDirectory1() {
		return (String) directory1.getText().trim();
	}
	/**
	 *
	 *
	 * @return
	 */ 
	public String getDirectory2() {
		return (String) directory2.getText().trim();
	}

	public void setDirectory1(String directory) {
		this.directory1.setText(directory);
	}



	public void setDirectory2(String directory) {
		this.directory2.setText(directory);
	}



	/**
	 *
	 *
	 * @return
	 */
	public char[] getLocalPassphrase() {
		return localPassphrase.getPassword();
	}


	/**
	 *
	 *
	 * @param evt
	 */
	public void actionPerformed(ActionEvent evt) {	
		
		//Load GO File Browser  
		if (evt.getActionCommand().equals("GO")){		
			String endpoint = "";
			String directory = "";
			if (evt.getSource() == go1 || evt.getSource().equals(directory1)){
				endpoint = getEndPoint1();
				directory = getDirectory1();
				if (directory.equals(""))
					directory = GOHelper.retrieveEndPointDefaultDirectory(endpoint);
				try{
					//Activate endpoint if it is not activated
					if (!GOHelper.autoActivate(endpoint)) {
						JOptionPane.showMessageDialog(null, "Cannot activate endpoint \"" + endpoint + "\". Please check if you have the permission to access this resource with the resource administrator.", 
								"Globus Online Tool Error", JOptionPane.ERROR_MESSAGE);	
					}
					else{
						MyTreeNode root = new MyTreeNode(1, 0, getDirectory1(), getEndPoint1(), true);
						treeModel1 = new DefaultTreeModel(root);
						/*bar1 = new JProgressBar();
						progressListener1 = new PropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								bar1.setValue((Integer) evt.getNewValue());
							}
						};*/
						root.loadChildren(treeModel1, getEndPoint1(), getDirectory1());
						fileDir1.setModel(treeModel1);
					}
				}catch(Exception e){
					JOptionPane.showMessageDialog(null, "Cannot activate the endpoint. You might not have the required permission. Please check your configuration." + "\nDetail message:\n" + e.getMessage(), "Globus Online Tool Error", JOptionPane.ERROR_MESSAGE); 
				} 


			}
			else {
				endpoint = getEndPoint2();
				directory = getDirectory2();
				if (directory.equals(""))
					directory = GOHelper.retrieveEndPointDefaultDirectory(endpoint);
				try{
					//Activate endpoint if it is not activated
					if (!GOHelper.autoActivate(endpoint)) {
						JOptionPane.showMessageDialog(null, "Cannot activate endpoint \"" + endpoint + "\". Please check if you have the permission to access this resource with the resource administrator.", 
								"Globus Online Tool Error", JOptionPane.ERROR_MESSAGE);	
					}
					else{
						MyTreeNode root = new MyTreeNode(1, 0, getDirectory2(), getEndPoint2(), true);
						treeModel2 = new DefaultTreeModel(root);
						/*bar1 = new JProgressBar();
						progressListener1 = new PropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								bar2.setValue((Integer) evt.getNewValue());
							}
						};*/
						root.loadChildren(treeModel2, getEndPoint2(), getDirectory2());
						fileDir2.setModel(treeModel2);
					}
				}catch(Exception e){
					String msg = "Cannot activate the endpoint. You might not have the required permission. Please check your configuration.";
					if (e.getMessage()!=null && !e.getMessage().trim().equals("null")){
						msg += "\nDetail message:\n" + e.getMessage();
					}
					JOptionPane.showMessageDialog(null, msg, "Globus Online Tool Error", JOptionPane.ERROR_MESSAGE); 
				} 


			}


		}

	}


	@Override
	public void itemStateChanged(ItemEvent evt) {
		String endpoint;
		if (evt.getSource() == endpoint1 || evt.getSource() == endpoint2){	
			if (evt.getSource() == endpoint1){
				endpoint = getEndPoint1();				
			}
			else{
				endpoint = getEndPoint2();
			}
			
			String defaultDir = GOHelper.retrieveEndPointDefaultDirectory(endpoint);
			if (evt.getSource() == endpoint1){
				setDirectory1(defaultDir);
			}
			else{
				setDirectory2(defaultDir);
			}
				
		}
		
	}



}

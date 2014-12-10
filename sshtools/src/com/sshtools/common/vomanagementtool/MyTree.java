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

import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


import com.sshtools.common.vomanagementtool.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.vomanagementtool.common.ConfigHelper;
import com.sshtools.common.vomanagementtool.common.VOHelper;
import com.sshtools.common.ui.ResourceIcon;

import java.awt.Dimension;


public class MyTree extends JTree implements Autoscroll, ActionListener{
	private Insets insets;
	private int top = 0, bottom = 0, topRow = 0, bottomRow = 0;
	private JPopupMenu popupMenu;
	
	public MyTree (final boolean isFavourite){
		
		this.setModel(this.treeModel); 
		this.setShowsRootHandles(true);
		this.setEditable(true);
		this.setDragEnabled(true);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		//Right Click Menu
		final MyTree thisTree = this;
		addMouseListener(new java.awt.event.MouseAdapter(){
			@Override
	        public void mouseClicked(java.awt.event.MouseEvent evt) {
	            try {
	            	TreePath tp = thisTree.getClosestPathForLocation(evt.getX(), evt.getY());
	            	if (thisTree.getSelectionPath()!=null && tp.equals(thisTree.getSelectionPath())){
			            if(evt.getButton() == evt.BUTTON1) {
			            	MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
			            	if(evt.getClickCount() == 2) {
			            		if(ConfigHelper.isAuthentication()){
			            			if(node.isLeaf()){	
				            			//Check if already added
				            			if (ConfigHelper.selectedRoot.getChildCount() == 0){
				            				ConfigHelper.selectedTreeModel.insertNodeInto(node, ConfigHelper.selectedRoot, 0);
				            			}
				            			else{
				            				boolean isAdded = false;
					            			for (int i=0; i<ConfigHelper.selectedRoot.getChildCount(); i++){
					            				MyTreeNode temp = (MyTreeNode) ConfigHelper.selectedRoot.getChildAt(i);			            				
					            				if (temp.getUserObject().toString().equals(node.getUserObject().toString())){
					            					isAdded = true;
					            				}
					            			}
					            			if (!isAdded){
					            				ConfigHelper.selectedTreeModel.insertNodeInto(node, ConfigHelper.selectedRoot, ConfigHelper.selectedRoot.getChildCount());
					            			}
				            			}
			            			}
			            		}
			            	}
			            }
			            else if (evt.getButton() == evt.BUTTON3) {
			            	MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
			            	popupMenu = new JPopupMenu();
			            	JMenuItem item;
			            	if (evt.getSource() == ConfigHelper.selectedTree){
			            		item = new JMenuItem("Clear"); 
				        		item.setActionCommand("clear");
				        		item.addActionListener(thisTree);
				        		popupMenu.add(item);
			            	}
			            	if(!ConfigHelper.isAuthentication()){
				            	if (isFavourite){
				            		if(!node.isLeaf()){
						            	item = new JMenuItem("Add VO"); 
						        		item.setActionCommand("addVo");
						        		item.addActionListener(thisTree);
						        		popupMenu.add(item);	
				            		}
				            		else{
						        		item = new JMenuItem("Remove from Favourites"); 
						        		item.setActionCommand("removeFav");
						        		item.addActionListener(thisTree);
						        		popupMenu.add(item);
				            		}
				            	}
				            	else {
				            		if(node.isLeaf()){
						        		item = new JMenuItem("Add to Favourites"); 
						        		item.setActionCommand("addFav");
						        		item.addActionListener(thisTree);
						        		popupMenu.add(item);
				            		}
				        		}
			            	}
			            	if(node.isLeaf()){			        		
				        		item = new JMenuItem("Show details"); 
				        		item.setActionCommand("show");
				        		item.addActionListener(thisTree);
				        		popupMenu.add(item);
			            	}	
			            	popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			            }
		            }
	            }catch (Exception e) {
	            	e.printStackTrace();
	            }
	        }
		});
	}

	
	 /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MyTree.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

	
	@Override
	public void actionPerformed(ActionEvent evt) {
		TreePath tp = this.getSelectionPath();	
		if (evt.getActionCommand().equals("addVo")){			
			if (VOHelper.createAddVODialog()){
				JOptionPane.showMessageDialog(null, "Successfully created new VO.",
						"Add new VO:", JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ConfigHelper.ICON));
			}	
			MyTreeNode myNode = (MyTreeNode) tp.getLastPathComponent();
			DefaultTreeModel model = (DefaultTreeModel) (MyTree.this.getModel());
			//Get updated setup
			if (VOHelper.retrieveSetup()){
				myNode.loadChildren(model, VOHelper.FAVOURITES, true);	
			}
					
		}
		else if (evt.getActionCommand().equals("removeFav")){
			String voname = tp.getLastPathComponent().toString();
			int result = JOptionPane.showConfirmDialog(null, "Are sure you want to remove '"+ voname
					+"' from "+VOHelper.FAVOURITES+"?", "Remove VO from " + VOHelper.FAVOURITES, JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ConfigHelper.ICON));
			if (result==JOptionPane.OK_OPTION){
				if (VOHelper.removeVOFromFavourites(voname)){
					MyTreeNode parentNode = ((MyTreeNode) tp.getParentPath().getLastPathComponent());
					DefaultTreeModel model = (DefaultTreeModel) (MyTree.this.getModel());
					//Get updated setup
					if (VOHelper.retrieveSetup()){
						parentNode.loadChildren(model, VOHelper.FAVOURITES, true);	
					}
				}
			}
		}
		else if (evt.getActionCommand().equals("addFav")){
			String voname = tp.getLastPathComponent().toString();
			int result = JOptionPane.showConfirmDialog(null, "Are sure you want to add existing VO '"+ voname
					+"' to "+VOHelper.FAVOURITES+"?", "Add existing VO to " + VOHelper.FAVOURITES, JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ConfigHelper.ICON));
			if (result==JOptionPane.OK_OPTION){
				if (VOHelper.addVOToFavourites(voname, tp.getParentPath().getLastPathComponent().toString())){
					MyTreeNode parentNode = ((MyTreeNode) tp.getParentPath().getLastPathComponent());
					DefaultTreeModel model = (DefaultTreeModel) ConfigHelper.favTree.getModel();
					//Get updated setup
					if (VOHelper.retrieveSetup()){
						ConfigHelper.favRoot.loadChildren(model, VOHelper.FAVOURITES, true);	
					}
				}
			}
		}
		else if (evt.getActionCommand().equals("show")){
			if (tp.getLastPathComponent() instanceof MyTreeNode) {
				MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
				List voDetails = node.getNodeVODetails();
				String msg = "Local VO name: " + tp.getLastPathComponent().toString()+ "\n";
				for (int i=0; i<voDetails.size(); i++){
					TreeMap details = (TreeMap) voDetails.get(i);
					if(voDetails.size()>1){
						msg += "	(" + (i+1) +")\n" +
								"			Server: "+ details.get("server") +"\n" +
								"			Port: "+ details.get("port") + "\n" +
								"			DN: " + details.get("dn")+ "\n" +
								"			Server VO name: " + details.get("servervoname") + "\n";
					}
					else{
						msg += "		Server: "+ details.get("server") +"\n" +
								"		Port: "+ details.get("port") + "\n" +
								"		DN: " + details.get("dn")+ "\n" +
								"		Server VO name: " + details.get("servervoname") + "\n";
					}
				}
				JOptionPane.showMessageDialog(null, msg, "VO Information:", JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ConfigHelper.ICON));
			}
		}
		else if (evt.getActionCommand().equals("clear")){
			if (tp.getLastPathComponent() instanceof MyTreeNode) {
				MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
				ConfigHelper.selectedTreeModel.removeNodeFromParent(node);
			
			}
		}
	
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.width = Math.max(ConfigHelper.getPreferredWidth(), preferredSize.width);
		preferredSize.height = Math.max(ConfigHelper.getPreferredHeight(), preferredSize.height);
		return preferredSize;
	}
	
	public Insets getAutoscrollInsets() {
		return insets;
	}
	@Override
	public void autoscroll(Point p) {
		// Only support up/down scrolling
		top = Math.abs(getLocation().y) + 10;
		bottom = top + getParent().getHeight() - 20;
		int next;
		if (p.y < top) {
			next = topRow--;
			bottomRow++;
			scrollRowToVisible(next);
		} else if (p.y > bottom) {
			next = bottomRow++;
			topRow--;
			scrollRowToVisible(next);
		}

	}


}




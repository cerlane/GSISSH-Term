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
package com.sshtools.common.globusonlinetool.myswingutils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sshtools.common.globusonlinetool.GOHelper;
import com.sshtools.common.globusonlinetool.Main;
import com.sshtools.common.globusonlinetool.myswingutils.JTreeNodeDynamic.MyTreeNode;
import com.sshtools.common.ui.ResourceIcon;

import java.awt.Dimension;


public class DragDropTree extends JTree implements Autoscroll, ActionListener{
	private Insets insets;
	private int top = 0, bottom = 0, topRow = 0, bottomRow = 0;
	private JPopupMenu popupMenu;
	private DefaultTreeModel treeModel;
	private ImageIcon toRefreshIcon;
	final static String ICON = "/com/sshtools/common/globusonlinetool/largego.png";
	
	public DragDropTree (){
		
		this.setModel(this.treeModel); 
		this.setShowsRootHandles(true);
		this.setEditable(true);
		//this.setDragEnabled(true);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, new TreeDragGestureListener());
		DropTarget dropTarget = new DropTarget(this, new TreeDropTargetListener());
		
		//Right Click Menu
		final DragDropTree thisTree = this;
		addMouseListener(new java.awt.event.MouseAdapter(){
			@Override
	        public void mouseClicked(java.awt.event.MouseEvent evt) {
	            try {
	            	TreePath tp = thisTree.getClosestPathForLocation(evt.getX(), evt.getY());
	            	if (thisTree.getSelectionPath()!=null && tp.equals(thisTree.getSelectionPath())){
			            if(evt.getButton() == evt.BUTTON1) {
			            }
			            else if (evt.getButton() == evt.BUTTON3) {
			            	
			            	MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
			            	popupMenu = new JPopupMenu();
			            	JMenuItem item;
			            	if(!node.isUncertain()){
				            	if(node.getParent()!=null){
					            	item = new JMenuItem("Delete"); 
					        		item.setActionCommand("delete");
					        		item.addActionListener(thisTree);
					        		popupMenu.add(item);	
				            	}		
				            	if(!node.isLeaf()){
					            	item = new JMenuItem("New Directory"); 
					        		item.setActionCommand("newDir");
					        		item.addActionListener(thisTree);
					        		popupMenu.add(item);
				            	}
				        		
				            	if(node.getParent()!=null){		        		
					        		item = new JMenuItem("Show details"); 
					        		item.setActionCommand("show");
					        		item.addActionListener(thisTree);
					        		popupMenu.add(item);
				            	}	
			            	}
			            	if(node.getParent()!=null){	
				            	item = new JMenuItem("Refresh"); 
				        		item.setActionCommand("refresh");
				        		item.addActionListener(thisTree);
				        		popupMenu.add(item);
			            	}
			            	popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			            }
		            }
	            }catch (Exception e) {}
	        }
		});
		
		//Set the icon for incomplete nodes.
        toRefreshIcon = createImageIcon("../refresh.png");
        if(toRefreshIcon!=null){
        	this.setCellRenderer(new MyRenderer(toRefreshIcon));
        }
			
	}
	public DragDropTree (DefaultTreeModel model){
		this();
		this.setModel(model);
		this.treeModel = model;	
	}
	 /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = DragDropTree.class.getResource(path);
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
		// TODO Auto-generated method stub
		if (evt.getActionCommand().equals("delete")){
			if (tp.getLastPathComponent() instanceof MyTreeNode) {
				MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
				String file = node.getFullPath();
				String endpoint = node.getEndpoint();				
				int reply;
				if (node.isLeaf()){
					
					reply = JOptionPane.showConfirmDialog(null, "Do you want to delete the file \n"+
							endpoint + ":" + file+ " ?", "Delete Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
				}
				else{
					reply = JOptionPane.showConfirmDialog(null, "Do you want to delete the directory \n"+
							endpoint + ":" + file+ "\n and all the files/directories in it recursively?", "Delete Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ICON));	
				}
				
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				if (reply == JOptionPane.YES_NO_OPTION){
					int status = GOHelper.deleteFiles(endpoint, file);
					if ( status == GOHelper.SUCCESS){
						/*DefaultTreeModel model = (DefaultTreeModel) (DragDropTree.this.getModel());
						model.removeNodeFromParent(node);*/
						
						MyTreeNode parentNode = (MyTreeNode) node.getParent();
						parentNode.setLoaded(false);
						parentNode.loadChildren((DefaultTreeModel) this.getModel(), parentNode.getEndpoint(), parentNode.getFullPath());
					}
					//Longer than 15 seconds
					else if (status == GOHelper.IN_PROGRESS){
						node.setUncertain(true);
						
					}
					else if (status == GOHelper.FAIL){
						JOptionPane.showMessageDialog(null, "Cannot delete file/directory.\n" + endpoint+":"+file, "Error: Delete file/directory", JOptionPane.ERROR_MESSAGE);
					
					}
					
				}
				this.setCursor(Cursor.getDefaultCursor());
				
			}
		} else if (evt.getActionCommand().equals("refresh")){
			if (tp.getLastPathComponent() instanceof MyTreeNode) {
				MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//If node is uncertain, reload parent instead of itself
				if(node.isUncertain() || node.isLeaf()){
					MyTreeNode parentNode = (MyTreeNode) node.getParent();
					parentNode.setLoaded(false);
					parentNode.loadChildren((DefaultTreeModel) this.getModel(), parentNode.getEndpoint(), parentNode.getFullPath());
				}
				else{
					node.setLoaded(false);
					node.loadChildren((DefaultTreeModel) this.getModel(), node.getEndpoint(), node.getFullPath());
				}
				this.setCursor(Cursor.getDefaultCursor());
			}
		
		} else if (evt.getActionCommand().equals("show")){
			if (tp.getLastPathComponent() instanceof MyTreeNode) {
				MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
				Map details = node.getNodeFileDetails();
				String fileSize = GOHelper.humanReadableByteCount(Long.valueOf(details.get("size")+"").longValue(), true);
				JOptionPane.showMessageDialog(null, "Endpoint: " + node.getEndpoint() + "\n" +
													"File: "	+ node.getFullPath()+ "\n" +
													"		User: "+ details.get("user") +"\n" +
													"		Group: "+ details.get("group") + "\n" +
													"		Size: " + fileSize + " or " +details.get("size")+ " B\n" +
													"		Permissions: " + details.get("permissions") + "\n" +
													"		Last Modified: " + GOHelper.formatDateTime(details.get("last_modified")+"")
									, "File Information:", JOptionPane.INFORMATION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
			}
		} else if (evt.getActionCommand().equals("newDir")){
			if (tp.getLastPathComponent() instanceof MyTreeNode) {
				MyTreeNode node = (MyTreeNode) tp.getLastPathComponent();
				if(!node.isLeaf()){
					String directory = GOHelper.createMkDirDialog();
					if (!directory.equals("")){
						if(GOHelper.mkEndpointDir(node.getEndpoint(), node.getFullPath()+"/"+directory)){
							node.setLoaded(false);
							node.loadChildren((DefaultTreeModel) this.getModel(), node.getEndpoint(), node.getFullPath());
						}
						
					} 
				}
				else{
					JOptionPane.showMessageDialog(null, "Cannot create directory in a file!", "Error: Create New Directory", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.width = Math.max(400, preferredSize.width);
		preferredSize.height = Math.max(500, preferredSize.height);
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
	private class MyRenderer extends DefaultTreeCellRenderer {
	    Icon toRefreshIcon;

	    public MyRenderer(Icon icon) {
	        toRefreshIcon = icon;
	    }

	    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
	                        								int row, boolean hasFocus) {

	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);	       
	        if (IsIncomplete(value)) {
	            setIcon(toRefreshIcon);
	            setToolTipText("Please refresh parent node to check if the task is completed.");
	        } else {
	            setToolTipText(null); //no tool tip
	        }

	        return this;
	    }

	    protected boolean IsIncomplete(Object value) {
	    	if ((value != null) && (value instanceof MyTreeNode)){
	    		MyTreeNode node = (MyTreeNode) value;
	    		if (node.isUncertain()){
	    			return true;
	    		}
	    	}	   
	    	return false;
	    }
	}

	private static class TreeDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
			// Can only drag leafs
			JTree tree = (JTree) dragGestureEvent.getComponent();
			TreePath path = tree.getSelectionPath();
			MyTreeNode selectedNode = (MyTreeNode) path.getLastPathComponent();
			
			if (path ==null || selectedNode.isUncertain()) {
				// Nothing selected, nothing to drag
				if (selectedNode.isUncertain()){
					JOptionPane.showMessageDialog(null, "You are not allowed to transfer \"work in progress\" file/directory. Please refresh the file/directory before trying again.", "Warning", JOptionPane.WARNING_MESSAGE, new ResourceIcon(this.getClass(), ICON));
				}
			} else {
				DefaultMutableTreeNode selection = (DefaultMutableTreeNode) path.getLastPathComponent();
				//if (selection.isLeaf()) {
					TransferableTreeNode node = new TransferableTreeNode(selection);
					dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, node, new MyDragSourceListener());
				/*} else {
					System.out.println("Not a leaf - beep");
					tree.getToolkit().beep();
				}*/
			}
		}
	}

	private class TreeDropTargetListener implements DropTargetListener {
		private int highlightedRow = -1; 
		private Rectangle dirtyRegion = null; 
		private Color highlightColor = new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), 100); 
		
		public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
			// Setup positioning info for auto-scrolling
			top = Math.abs(getLocation().y);
			bottom = top + getParent().getHeight();
			topRow = getClosestRowForLocation(0, top);
			bottomRow = getClosestRowForLocation(0, bottom);
			insets = new Insets(top + 10, 0, bottom - 10, getWidth());
		}

		public void dragExit(DropTargetEvent dropTargetEvent) {
			if (null != dirtyRegion) { 
				paintImmediately(dirtyRegion); 
			} 
		}

		public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
			JTree tree = (JTree) dropTargetDragEvent.getDropTargetContext().getComponent();
			int closestRow = -1;
			Point location = dropTargetDragEvent.getLocation();
			TreePath path = getPathForLocation(location.x, location.y);
			try{
				DefaultMutableTreeNode selection = (DefaultMutableTreeNode) path.getLastPathComponent();
				
				if (selection!=null && selection.isLeaf()) {
					closestRow = tree.getRowForPath(path.getParentPath());
					
				} else {
					if (path!=null && !path.toString().trim().equals("null"))
						closestRow = tree.getClosestRowForLocation((int) location.getX(), (int) location.getY()); 
				}
			}catch (NullPointerException e){
				if (path!=null && !path.toString().trim().equals("null"))
					closestRow = tree.getClosestRowForLocation((int) location.getX(), (int) location.getY()); 
			}
			
			boolean highlighted = false; 
			Graphics g = getGraphics(); 

			// row changed 
			if (closestRow!=-1 && highlightedRow != closestRow) { 
				if (null != dirtyRegion) { 
					paintImmediately(dirtyRegion); 
				} 
	
				for (int j = 0; j <tree.getRowCount(); j++){
					
					if (closestRow == j) { 
						Rectangle firstRowRect = getRowBounds(closestRow); 
						this.dirtyRegion = firstRowRect; 
						g.setColor(highlightColor); 
			
						g.fillRect((int) dirtyRegion.getX(), (int) dirtyRegion.getY(), (int) dirtyRegion.getWidth(), (int) dirtyRegion.getHeight()); 
						highlightedRow = closestRow; 
					} 
				} 
			} 
			//To "un" highlight a row if curser moved to an area not on the node/leaf text/icon
			if(closestRow==-1) {
				if (null != dirtyRegion) { 
					paintImmediately(dirtyRegion); 
				} 
			}
			
		}

		public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
		}

		public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
			try{	
				Point dropLocation = dropTargetDropEvent.getLocation();
				TreePath dropPath = getPathForLocation(dropLocation.x, dropLocation.y);
				Object destinationNode = dropPath.getLastPathComponent();
				
				//If a leaf is chosen, then the parent node is assumed to be chosen.
				if (((TreeNode) destinationNode).isLeaf()){
					dropPath = dropPath.getParentPath();
					destinationNode = dropPath.getLastPathComponent();
				}
				
				if ((destinationNode != null) && (destinationNode instanceof TreeNode) && (!((TreeNode) destinationNode).isLeaf())) {
					try {		
						Transferable tr = dropTargetDropEvent.getTransferable();
					
						if (tr.isDataFlavorSupported(TransferableTreeNode.DEFAULT_MUTABLE_TREENODE_FLAVOR)) {							
							dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
							MyTreeNode sourceTreeNode = (MyTreeNode)tr.getTransferData(TransferableTreeNode.DEFAULT_MUTABLE_TREENODE_FLAVOR);
							dropTargetDropEvent.getDropTargetContext().getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							addElement(dropPath, sourceTreeNode, dropTargetDropEvent.getDropTargetContext().getComponent());
							dropTargetDropEvent.getDropTargetContext().getComponent().setCursor(Cursor.getDefaultCursor());
							dropTargetDropEvent.dropComplete(true);
							
						} else {
							System.out.println("Rejected");
							dropTargetDropEvent.rejectDrop();
						}
						
						
						
						
					} catch (IOException io) {
						io.printStackTrace();
						dropTargetDropEvent.rejectDrop();
					} catch (UnsupportedFlavorException ufe) {
						ufe.printStackTrace();
						dropTargetDropEvent.rejectDrop();
					}
				} else {
					//try {
						/*Transferable tr = dropTargetDropEvent.getTransferable();					
						if (tr.isDataFlavorSupported(TransferableTreeNode.DEFAULT_MUTABLE_TREENODE_FLAVOR)) {
							dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
							MyTreeNode sourceTreeNode = (MyTreeNode)tr.getTransferData(TransferableTreeNode.DEFAULT_MUTABLE_TREENODE_FLAVOR);
							//System.out.println(sourceTreeNode.getFirstChild());
						}*/
					
						//Dropping a file on a leaf. Ignore.
						dropTargetDropEvent.rejectDrop();
					/*} catch (IOException io) {
						io.printStackTrace();
						dropTargetDropEvent.rejectDrop();
					} catch (UnsupportedFlavorException ufe) {
						ufe.printStackTrace();
						dropTargetDropEvent.rejectDrop();
					}*/
				}
			}catch (NullPointerException e){
				// Probably no node is selected during a drop
			}
		}

		private void addElement(TreePath path, Object element, final Component component) {		
						
			MyTreeNode destinationNode = (MyTreeNode) path.getLastPathComponent();
			MyTreeNode sourceNode = (MyTreeNode)element;
			String destPath = destinationNode.getFullPath() + "/" + nodePathChecker(sourceNode.getNodeName());
			int reply = JOptionPane.showConfirmDialog(null, "Are you show you want to transfer files/directories from \n"+
					sourceNode.getEndpoint()+":"+sourceNode.getFullPath()+ "\nto\n"+
					destinationNode.getEndpoint() +":" + destPath +" ?", "Transfer Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(this.getClass(), ICON));
			
			
			
			if (reply == JOptionPane.YES_NO_OPTION){
				int status = GOHelper.transferFiles(sourceNode.getEndpoint(), destinationNode.getEndpoint(), sourceNode.getFullPath(), destPath, !sourceNode.isLeaf());
				
				if ( status == GOHelper.SUCCESS){
					destinationNode.setLoaded(false);
					destinationNode.loadChildren((DefaultTreeModel) (DragDropTree.this.getModel()), destinationNode.getEndpoint(), destinationNode.getFullPath());
					
					/*DefaultTreeModel model = (DefaultTreeModel) (DragDropTree.this.getModel());
					model.insertNodeInto(sourceNode, destinationNode, destinationNode.getChildCount());*/
				}
				//Longer than 15 seconds
				else if (status == GOHelper.IN_PROGRESS){
					sourceNode.setUncertain(true);
					DefaultTreeModel model = (DefaultTreeModel) (DragDropTree.this.getModel());
					model.insertNodeInto(sourceNode, destinationNode, destinationNode.getChildCount());
					
				}
				else if (status == GOHelper.FAIL){
					JOptionPane.showMessageDialog(null, "Cannot transfer file/directory from\n" + sourceNode.getEndpoint()+":"+sourceNode.getFullPath()
													+ "\nto\n" +destinationNode.getEndpoint()+":" +destinationNode.getFullPath() , "Error: transfer file/directory", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		}
		private String nodePathChecker(String nodePath){
			if (nodePath.contains("/")){
				String [] temp = nodePath.split("/");
				nodePath = temp[temp.length-1];
				
			}
			return nodePath;
		}
	}
	

	private static class MyDragSourceListener implements DragSourceListener {
		public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) {
			if (dragSourceDropEvent.getDropSuccess()) {
				int dropAction = dragSourceDropEvent.getDropAction();
				if (dropAction == DnDConstants.ACTION_MOVE) {
					System.out.println("MOVE: remove node");
				}
			}
		}

		public void dragEnter(DragSourceDragEvent dragSourceDragEvent) {
			DragSourceContext context = dragSourceDragEvent.getDragSourceContext();
			int dropAction = dragSourceDragEvent.getDropAction();
			if ((dropAction & DnDConstants.ACTION_COPY) != 0) {
				context.setCursor(DragSource.DefaultCopyDrop);
			} else if ((dropAction & DnDConstants.ACTION_MOVE) != 0) {
				context.setCursor(DragSource.DefaultMoveDrop);
			} else {
				context.setCursor(DragSource.DefaultCopyNoDrop);
			}
		}

		public void dragExit(DragSourceEvent dragSourceEvent) {
		}

		public void dragOver(DragSourceDragEvent dragSourceDragEvent) {
		}

		public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) {
		}
	}

	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}
	public void setTreeModel(DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}


	

	
}

class TransferableTreeNode extends DefaultMutableTreeNode implements
Transferable {
	final static int TREE = 0;

	final static int STRING = 1;

	final static int PLAIN_TEXT = 1;

	final public static DataFlavor DEFAULT_MUTABLE_TREENODE_FLAVOR = new DataFlavor(
			DefaultMutableTreeNode.class, "Default Mutable Tree Node");

	static DataFlavor flavors[] = { DEFAULT_MUTABLE_TREENODE_FLAVOR/*,
		DataFlavor.stringFlavor, DataFlavor.plainTextFlavor */};

	private DefaultMutableTreeNode data;

	public TransferableTreeNode(DefaultMutableTreeNode data) {
		this.data = data;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		Object returnObject;
		if (flavor.equals(flavors[TREE])) {
			Object userObject = data;
			if (userObject == null) {
				returnObject = data;
			} else {
				returnObject = userObject;
			}
		} /*else if (flavor.equals(flavors[STRING])) {
			Object userObject = data.getUserObject();
			if (userObject == null) {
				returnObject = data.toString();
			} else {
				returnObject = userObject.toString();
			}
		} else if (flavor.equals(flavors[PLAIN_TEXT])) {
			Object userObject = data.getUserObject();
			String string;
			if (userObject == null) {
				string = data.toString();
			} else {
				string = userObject.toString();
			}
			returnObject = new ByteArrayInputStream(string.getBytes("Unicode"));
		}*/ else {
			throw new UnsupportedFlavorException(flavor);
		}
		return returnObject;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		boolean returnValue = false;
		for (int i = 0, n = flavors.length; i < n; i++) {
			if (flavor.equals(flavors[i])) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
	
	


}




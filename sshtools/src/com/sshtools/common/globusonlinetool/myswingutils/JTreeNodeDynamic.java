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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.sshtools.common.globusonlinetool.GOHelper;

public class JTreeNodeDynamic {

    public static class MyTreeNode extends DefaultMutableTreeNode implements PropertyChangeListener {

        private boolean loaded = false;
        private int depth;
        private boolean isUncertain = false;

        private final int index;        
        private String endpoint;
        private boolean isLeaf;
        private Map fileDetails;

    	private PropertyChangeListener progressListener;
    	private JProgressBar bar;
    	
        
        /*private DefaultTreeModel treeModel;
        private JProgressBar bar;
        private PropertyChangeListener progressListener;*/

        public MyTreeNode(int index, int depth, String dir, String endpoint, boolean allowChildren) { 
            this.index = index;
            this.depth = depth;
            this.endpoint = endpoint;
            this.isLeaf = !allowChildren;
            add(new MyTreeNode("Loading...", false));
            setAllowsChildren(allowChildren);
            setUserObject(dir);
            
            bar = new JProgressBar();
        }
        
        public MyTreeNode(Object userObject, boolean allowsChildren){
        	super(new DefaultMutableTreeNode(userObject, allowsChildren));
        	this.isLeaf = !allowsChildren;
        	this.index = 0;
        }
        
        public MyTreeNode(int index, int depth, String dir, String endpoint, Map fileDetails, boolean allowChildren) {
        	/*super(new MyTreeNode(index, depth, dir, endpoint, allowChildren));
        	this.index = index;
        	this.fileDetails = fileDetails;*/
        	
           this.index = index;
            this.depth = depth;
            this.endpoint = endpoint;
            this.fileDetails = fileDetails;
            this.isLeaf = !allowChildren;
            add(new MyTreeNode("Loading...", false));
            setAllowsChildren(allowChildren);
            setUserObject(dir);
            
            bar = new JProgressBar();
        }
         

        private void setChildren(List<DefaultMutableTreeNode> children) {
            removeAllChildren();
            setAllowsChildren(children.size() > 0);
            for (MutableTreeNode node : children) {
                add(node);
            }
            loaded = true;
        }
        public String getNodeName(){
        	String name= "";
        	TreePath path = new TreePath(this.getPath());
        	name += path.getLastPathComponent();
        	return name;
        }
        
        public String getFullPath(){
        	String fullPath= "";
        	TreePath path = new TreePath(this.getPath());
        	for (int i=0; i<path.getPathCount(); i++){
				 if (i!=0)	
					 fullPath += "/";
				 fullPath += path.getPathComponent(i);
			 }
        	
        	return fullPath;
        }
        public Map getNodeFileDetails(){
        	return this.fileDetails;
        }
        
        public String getEndpoint(){
        	return this.endpoint;
        }
        
        @Override
        public boolean isLeaf() {
            return this.isLeaf;
        }
        

        public void loadChildren(final DefaultTreeModel model, final String endpoint, final String dir) {
            if (loaded) {
                return;
            }
            SwingWorker<List<DefaultMutableTreeNode>, Void> worker = new SwingWorker<List<DefaultMutableTreeNode>, Void>() {
                @Override
                protected List<DefaultMutableTreeNode> doInBackground() throws Exception {
                    // Here access database if needed
                    setProgress(0);
                    List<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>();
                    
                    List<Map> allDir = GOHelper.displayLs(endpoint, dir);
                    for(int i=0; i<allDir.size(); i++){
                    	Map filedetails = new HashMap();
                		filedetails.put("size", allDir.get(i).get("size").toString());
                		filedetails.put("group", allDir.get(i).get("group").toString());
                		filedetails.put("last_modified", allDir.get(i).get("last_modified").toString());
                		filedetails.put("permissions", allDir.get(i).get("permissions").toString());
                		filedetails.put("type", allDir.get(i).get("type").toString());
                		filedetails.put("user", allDir.get(i).get("user").toString());
                		filedetails.put("link_target", allDir.get(i).get("link_target").toString());
                		
                    	if (allDir.get(i).get("type").toString().equals("dir")){                    		
                    		children.add(new MyTreeNode(i + 1, depth + 1, allDir.get(i).get("name").toString(), endpoint, filedetails, true));                      		
                    	}
                    	else{
                    		children.add(new MyTreeNode(i + 1, depth + 1, allDir.get(i).get("name").toString(), endpoint, filedetails, false));
                    		//children.add(new DefaultMutableTreeNode(allDir.get(i).get("name").toString(), false));
                    	}
                    		
                    	setProgress((i+1)*(100/allDir.size()));
                    	
        			}
                    setProgress(0);
                    return children;
                }

                @Override
                protected void done() {
                    try {
                        setChildren(get());
                        model.nodeStructureChanged(MyTreeNode.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Notify user of error.
                    }
                    super.done();
                }
            };
            if (progressListener != null) {
                worker.getPropertyChangeSupport().addPropertyChangeListener("progress", progressListener);
            }
            worker.execute();
        }

		@Override
		public void propertyChange(PropertyChangeEvent evt) {			
			bar.setValue((Integer) evt.getNewValue());
			
		}

		public void setLoaded(boolean loaded) {
			this.loaded = loaded;
		}
		public boolean isLoaded() {
			return this.loaded;
		}

		public boolean isUncertain() {
			return isUncertain;
		}

		public void setUncertain(boolean isUncertain) {
			this.isUncertain = isUncertain;
		}

       
    }
    

}
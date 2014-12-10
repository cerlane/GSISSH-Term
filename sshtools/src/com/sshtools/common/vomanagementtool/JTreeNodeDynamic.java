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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.sshtools.common.vomanagementtool.common.VOHelper;


public class JTreeNodeDynamic {

    public static class MyTreeNode extends DefaultMutableTreeNode implements PropertyChangeListener {

        private int depth;
        private final int index;     
        private boolean isLeaf;
        
        private PropertyChangeListener progressListener;
    	private JProgressBar bar;
    	private List voDetails;

        public MyTreeNode(int index, int depth, String voName, boolean isLeaf) { 
            this.index = index;
            this.depth = depth;
            this.isLeaf = isLeaf;
           
            //add(new MyTreeNode("Loading..."));
            setAllowsChildren(!isLeaf);
            setUserObject(voName);
            bar = new JProgressBar();
        }
        
        public MyTreeNode(Object userObject, boolean isLeaf){
        	super(new DefaultMutableTreeNode(userObject, false));
        	this.isLeaf = isLeaf;
        	this.index = 0;
        }
        
        public MyTreeNode(int index, int depth, String voName, List voDetails) { 
            this.index = index;
            this.depth = depth;
            this.voDetails = voDetails;
            this.isLeaf = true;
            
            //add(new MyTreeNode("Loading..."));
            setAllowsChildren(false);
            setUserObject(voName);
            bar = new JProgressBar();
        }
    

        private void setChildren(List<DefaultMutableTreeNode> children) {
            removeAllChildren();
            setAllowsChildren(children.size() > 0);
            for (MutableTreeNode node : children) {
                add(node);
            }
        }
       
   
        @Override
        public boolean isLeaf() {
            return this.isLeaf;
        }
        

        public void loadChildren(final DefaultTreeModel model, final String voGroup, boolean isLeaf) {
       
            SwingWorker<List<DefaultMutableTreeNode>, Void> worker = new SwingWorker<List<DefaultMutableTreeNode>, Void>() {
                @Override
                protected List<DefaultMutableTreeNode> doInBackground() throws Exception {
                    // Here access database if needed
                    setProgress(0);
                    List<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>();
                    
                    Map groupVOs = (TreeMap)VOHelper.getVOGroup(voGroup);
                    if (groupVOs!=null && groupVOs.size()>0){
                    	Iterator<Map.Entry<String,TreeMap>> entries = groupVOs.entrySet().iterator();
                    
	                    int counter =0;
	                    while (entries.hasNext()) {
	                        Map.Entry<String,TreeMap> entry = entries.next();
	                        String voname = entry.getKey();
	                        List voDetails = (ArrayList) groupVOs.get(voname);
	                    	if (voname!=null && !voname.trim().equals("")){
	                    		children.add(new MyTreeNode(counter + 1, depth + 1, voname.trim(), voDetails)); 
	                    	}
	                        setProgress((counter+1)*(100/groupVOs.size()));
	                    	counter ++;
	                    }
                    }
                   
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
        
        public List getNodeVODetails(){
        	return this.voDetails;
        }

       
    }
    

}
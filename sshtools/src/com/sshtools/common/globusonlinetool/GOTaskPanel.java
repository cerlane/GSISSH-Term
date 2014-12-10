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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.sshtools.common.globusonlinetool.myswingutils.TextAreaRenderer;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

public class GOTaskPanel extends JPanel
implements DocumentListener,
ActionListener {
	public JTable taskTable;
	private JScrollPane taskListScrollPane;
	private DefaultTableModel tableModel;
	private TableColumnModel colTableModel;
	private String [] columnNames = {"Task Id", "Description", "Creation", "Last Check", "Status"};
	
	/**
	 * Creates a new GOTaskPanel object.
	 * @throws Exception 
	 */
	public GOTaskPanel()  {
		super();

		String val = ConfigurationLoader.checkAndGetProperty("user.home", null);
		File userPrefDir = null;
		if (val != null) {
			userPrefDir = new File(val + File.separator + ".sshterm");
		}

		if (userPrefDir != null) {
			//
			PreferencesStore.init(new File(userPrefDir,
					"@APPLICATION_NAME@"+ ".properties"));
		}



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
		 * TaskListPanel
		 */
		JPanel taskListPanel = new JPanel(new GridBagLayout());
		taskListPanel.setBorder(BorderFactory.createTitledBorder("Task Management"));

		gbc.insets = normalInsets;
		Object [][] data = GOHelper.getGOLogRecords();
		if (data!=null){
			tableModel = new DefaultTableModel();
			tableModel.setDataVector(data, columnNames);
			taskTable = new JTable(tableModel);//new JTable(data, columnNames);
			taskTable.setModel(tableModel);
		}
		else{
			tableModel = new DefaultTableModel(0, columnNames.length) ;
			tableModel.setColumnIdentifiers(columnNames);
			taskTable = new JTable(tableModel);
			taskTable.setModel(tableModel);
		}
		taskTable.setAutoCreateRowSorter(true);
		//taskTable.setFillsViewportHeight(true);
		taskTable.setRowHeight(20);
		//taskTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		taskTable.setAlignmentY(JTable.TOP_ALIGNMENT);

		colTableModel = taskTable.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer(); 

		TableColumn column = null;
		for (int i = 0; i < columnNames.length; i++) {
			colTableModel.getColumn(i).setCellRenderer(textAreaRenderer); 
			column = taskTable.getColumnModel().getColumn(i);
		    if (i == 0) {
		        column.setPreferredWidth(300); 
		    } else if (i==1){
		        column.setPreferredWidth(380);
		    } else if (i==2){
		        column.setPreferredWidth(160);
		    } else if (i==3){
		        column.setPreferredWidth(160);
		    } else if (i==4){
		        column.setPreferredWidth(100);
		    }
		}
		
		taskListScrollPane = new JScrollPane(taskTable);
		taskListScrollPane.setWheelScrollingEnabled(true);
		taskListScrollPane.setPreferredSize(new Dimension(1100,200));
		UIUtil.jGridBagAdd(taskListPanel, taskListScrollPane, gbc, GridBagConstraints.RELATIVE);

		add(taskListPanel,BorderLayout.CENTER);
		
		Timer timer = new Timer(0, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					if (GOHelper.updateTasksStatus() || GOHelper.isGOTaskTableToBeUpdated()){
						Object [][] data = GOHelper.getTasksObject();
						if (data!=null){
							tableModel.setDataVector(data, columnNames);
							
							colTableModel = taskTable.getColumnModel(); 
							TextAreaRenderer textAreaRenderer = new TextAreaRenderer(); 

							TableColumn column = null;
							for (int i = 0; i < columnNames.length; i++) {
								colTableModel.getColumn(i).setCellRenderer(textAreaRenderer); 
								column = taskTable.getColumnModel().getColumn(i);
							    if (i == 0) {
							        column.setPreferredWidth(300); 
							    } else if (i==1){
							        column.setPreferredWidth(380);
							    } else if (i==2){
							        column.setPreferredWidth(160);
							    } else if (i==3){
							        column.setPreferredWidth(160);
							    } else if (i==4){
							        column.setPreferredWidth(100);
							    }
							}
						}
						else{
							tableModel.setColumnIdentifiers(columnNames);
							taskTable = new JTable(tableModel);
							taskTable.setColumnModel(colTableModel);
						}
						if (GOHelper.isGOTaskTableToBeUpdated()){
							GOHelper.setGOTaskTableToBeUpdated(false);
						}
						
					}
					//System.out.println("Pinging....");
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
			
		});

		timer.setDelay(5000); // delay for 5 seconds
		timer.start();
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
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
	public String[] getColumnNames() {
		return columnNames;
	} 

}

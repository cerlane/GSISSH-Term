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


import javax.swing.JTable; 
import javax.swing.JTextArea; 
import javax.swing.table.DefaultTableCellRenderer; 
import javax.swing.table.TableCellRenderer; 
import javax.swing.table.TableColumn; 
import javax.swing.table.TableColumnModel; 

import java.awt.Color;
import java.awt.Component; 
import java.awt.Font;
import java.util.Enumeration; 
import java.util.HashMap; 
import java.util.Map; 

/** 
* The standard class for rendering (displaying) individual cells in a JTable. 
* This class inherits from JTextArea, a standard component class. 
* However JTextArea is a multi-line area that displays plain text. 
* 
* This class implements TableCellRenderer , i.e. interface. 
* This interface defines the method required by any object that 
* would like to be a renderer for cells in a JTable. 
* 
* @author Manivel 
* @see JTable 
* @see JTextArea 
*/ 

public class TextAreaRenderer extends JTextArea implements TableCellRenderer { 
   private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer(); 

   // Column heights are placed in this Map 
   private final Map<JTable, Map<Object, Map<Object, Integer>>> tablecellSizes = new HashMap<JTable, Map<Object, Map<Object, Integer>>>(); 

   /** 
    * Creates a text area renderer. 
    */ 
   public TextAreaRenderer() { 
       setLineWrap(true); 
       setWrapStyleWord(true); 
   } 

   /** 
    * Returns the component used for drawing the cell.  This method is 
    * used to configure the renderer appropriately before drawing. 
    * 
    * @param table      - JTable object 
    * @param value      - the value of the cell to be rendered. 
    * @param isSelected - isSelected   true if the cell is to be rendered with the selection highlighted; 
    *                   otherwise false. 
    * @param hasFocus   - if true, render cell appropriately. 
    * @param row        - The row index of the cell being drawn. 
    * @param column     - The column index of the cell being drawn. 
    * @return - Returns the component used for drawing the cell. 
    */ 
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                  boolean hasFocus, int row, int column) { 
       // set the Font, Color, etc. 
       renderer.getTableCellRendererComponent(table, value, 
               isSelected, hasFocus, row, column); 
       
       if(column==4){
    	   Font statusFont = new Font("Verdana", Font.BOLD, 12);
    	   if(renderer.getText().equals("SUCCEEDED")){
    		   setBackground(Color.GREEN);
    		   setForeground(Color.WHITE); 
        	   setFont(statusFont); 
    	   }
    	   else if(renderer.getText().equals("FAILED")){
    		   setBackground(Color.RED);
    		   setForeground(Color.WHITE); 
        	   setFont(statusFont); 
    	   }
    	   else if(renderer.getText().equals("ACTIVE")){
    		   setBackground(Color.BLUE);
    		   setForeground(Color.WHITE); 
        	   setFont(statusFont); 
    	   }
    	   
       }
       else{
    	   setBackground(renderer.getBackground());
    	   setForeground(renderer.getForeground()); 
    	   setFont(renderer.getFont()); 
       }
       setBorder(renderer.getBorder());        
       setText(renderer.getText()); 
       
       TableColumnModel columnModel = table.getColumnModel(); 
       setSize(columnModel.getColumn(column).getWidth(), 0); 
       int height_wanted = (int) getPreferredSize().getHeight(); 
       addSize(table, row, column, height_wanted); 
       height_wanted = 50;//findTotalMaximumRowSize(table, row); 
       if (height_wanted != table.getRowHeight(row)) { 
           table.setRowHeight(row, height_wanted); 
       } 
       return this; 
   } 

   /** 
    * @param table  - JTable object 
    * @param row    - The row index of the cell being drawn. 
    * @param column - The column index of the cell being drawn. 
    * @param height - Row cell height as int value 
    *               This method will add size to cell based on row and column number 
    */ 
   private void addSize(JTable table, int row, int column, int height) { 
       Map<Object, Map<Object, Integer>> rowsMap = tablecellSizes.get(table); 
       if (rowsMap == null) { 
           tablecellSizes.put(table, rowsMap = new HashMap<Object, Map<Object, Integer>>()); 
       } 
       Map<Object, Integer> rowheightsMap = rowsMap.get(row); 
       if (rowheightsMap == null) { 
           rowsMap.put(row, rowheightsMap = new HashMap<Object, Integer>()); 
       } 
       rowheightsMap.put(column, height); 
   } 

   /** 
    * Look through all columns and get the renderer.  If it is 
    * also a TextAreaRenderer, we look at the maximum height in 
    * its hash table for this row. 
    * 
    * @param table -JTable object 
    * @param row   - The row index of the cell being drawn. 
    * @return row maximum height as integer value 
    */ 
   private int findTotalMaximumRowSize(JTable table, int row) { 
       int maximum_height = 0; 
       Enumeration<TableColumn> columns = table.getColumnModel().getColumns(); 
       while (columns.hasMoreElements()) { 
           TableColumn tc = columns.nextElement(); 
           TableCellRenderer cellRenderer = tc.getCellRenderer(); 
           if (cellRenderer instanceof TextAreaRenderer) { 
               TextAreaRenderer tar = (TextAreaRenderer) cellRenderer; 
               maximum_height = Math.max(maximum_height, 
                       tar.findMaximumRowSize(table, row)); 
           } 
       } 
       return maximum_height; 
   } 

   /** 
    * This will find the maximum row size 
    * 
    * @param table - JTable object 
    * @param row   - The row index of the cell being drawn. 
    * @return row maximum height as integer value 
    */ 
   private int findMaximumRowSize(JTable table, int row) { 
       Map<Object, Map<Object, Integer>> rows = tablecellSizes.get(table); 
       if (rows == null) return 0; 
       Map<Object, Integer> rowheights = rows.get(row); 
       if (rowheights == null) return 0; 
       int maximum_height = 0; 
       for (Map.Entry<Object, Integer> entry : rowheights.entrySet()) { 
           int cellHeight = entry.getValue(); 
           maximum_height = Math.max(maximum_height, cellHeight); 
       } 
       return maximum_height; 
   } 
}
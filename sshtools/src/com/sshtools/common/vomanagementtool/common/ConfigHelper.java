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

package com.sshtools.common.vomanagementtool.common;

import javax.swing.tree.DefaultTreeModel;

import org.ietf.jgss.GSSCredential;


import com.sshtools.common.vomanagementtool.MyTree;
import com.sshtools.common.vomanagementtool.JTreeNodeDynamic.MyTreeNode;


public class ConfigHelper {
	private static int preferredWidth = 250;
	private static int preferredHeight = 250;
	private static int visibleRowCount = 20;
	private static boolean isAuthentication = false;
	
	private static GSSCredential gssCred= null;
	private static GSSCredential vomsCredential= null;
	
	public final static String ICON = "/com/sshtools/common/vomanagementtool/largeVO.png";
	public static MyTreeNode favRoot ;
	public static MyTree favTree;
	public static DefaultTreeModel selectedTreeModel;
	public static MyTreeNode selectedRoot;
	public static MyTree selectedTree;
	
	
	public static int getPreferredWidth() {
		return preferredWidth;
	}
	public static int getPreferredHeight() {
		return preferredHeight;
	}
	public static void setPreferredWidth(int width) {
		preferredWidth = width;
	}
	public static void setPreferredHeight(int height) {
		preferredHeight = height;
	}
	public static int getVisibleRowCount() {
		return visibleRowCount;
	}
	public static void setVisibleRowCount(int visibleRowCount) {
		ConfigHelper.visibleRowCount = visibleRowCount;
	}
	public static boolean isAuthentication() {
		return isAuthentication;
	}
	public static void setAuthentication(boolean isAuthentication) {
		ConfigHelper.isAuthentication = isAuthentication;
	}
	public static GSSCredential getGssCred() {
		return gssCred;
	}
	public static void setGssCred(GSSCredential gssCred) {
		ConfigHelper.gssCred = gssCred;
	}
	public static GSSCredential getVomsCredential() {
		return vomsCredential;
	}
	public static void setVomsCredential(GSSCredential vomsCredential) {
		ConfigHelper.vomsCredential = vomsCredential;
	}
	

}

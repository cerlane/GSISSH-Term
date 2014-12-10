package com.sshtools.tightvnc;

import java.io.IOException;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsApplicationSessionPanel;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.j2ssh.connection.ChannelEventListener;


public class Main
extends SshToolsApplicationSessionPanel {
	public static final ResourceIcon TIGHTVNC_ICON = new ResourceIcon(
		      Main.class,
		      "tightvnc.png");
	@Override
	public SshToolsConnectionTab[] getAdditionalConnectionTabs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEventListener(ChannelEventListener eventListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean requiresConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onOpenSession() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canClose() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAvailableActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResourceIcon getIcon() {
		return TIGHTVNC_ICON;
	}

}

package com.sshtools.sshterm;

import java.awt.Container;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.sshtools.common.myproxytool.Main;

public class MyProxyClientApplet extends JApplet{
	Dimension size = new Dimension(700, 450);
	@Override
	public void init() {
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					JFrame myFrame = new Main();	               
					Container contentPane = myFrame.getContentPane();        
					setContentPane(contentPane);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		this.setSize(size);
	}
}

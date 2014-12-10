/*
 *  GSI-SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2005-6 CCLRC.
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

package com.sshtools.common.ui;

import com.sshtools.common.ui.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SshToolsSimpleConnectionPrompt
{
    class SshToolsSimpleConnectionDialog extends JDialog
    {

        String getHostname()
        {
            return !cancelled ? hostname.getText() : null;
        }

        boolean getCancelled()
        {
            return cancelled;
        }
        boolean getAdvanced()
        {
            return advanced;
        }

        void init()
        {
        	//setAlwaysOnTop(true);
            setDefaultCloseOperation(2);
            JPanel jpanel = new JPanel(new GridBagLayout());
            GridBagConstraints gridbagconstraints = new GridBagConstraints();
            gridbagconstraints.insets = new Insets(0, 0, 2, 2);
            gridbagconstraints.anchor = 17;
            gridbagconstraints.fill = 2;
            gridbagconstraints.weightx = 0.0D;
            UIUtil.jGridBagAdd(jpanel, new JLabel("Host to Connect to: "), gridbagconstraints, -1);
            gridbagconstraints.weightx = 1.0D;
            UIUtil.jGridBagAdd(jpanel, hostname, gridbagconstraints, 0);
            promptLabel.setHorizontalAlignment(0);
            JPanel jpanel1 = new JPanel(new BorderLayout());
            jpanel1.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            jpanel1.add(promptLabel, "North");
            jpanel1.add(jpanel, "Center");

            JButton jbutton = new JButton("Ok");
            jbutton.setMnemonic('o');
            jbutton.setDefaultCapable(true);
            jbutton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
                {
                    cancelled = false;
		    advanced = false;
                    hide();
                }

            });
            getRootPane().setDefaultButton(jbutton);

            JButton jbutton1 = new JButton("Cancel");
            jbutton1.setMnemonic('c');
            jbutton1.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
                {
                    cancelled = true;
		    advanced = false;
                    hide();
                }

            });

            JButton jbutton3 = new JButton("Advanced");
            jbutton3.setMnemonic('a');
            jbutton3.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
                {
                    cancelled = false;
		    advanced = true;
                    hide();
                }

            });

            JPanel jpanel2 = new JPanel(new FlowLayout(2, 0, 0));
            jpanel2.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            jpanel2.add(jbutton3);
            jpanel2.add(jbutton1);
            jpanel2.add(jbutton);
            IconWrapperPanel iconwrapperpanel = new IconWrapperPanel(new ResourceIcon("largeserveridentity.png"), jpanel1);
            iconwrapperpanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JPanel jpanel3 = new JPanel(new BorderLayout());
            jpanel3.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            jpanel3.add(iconwrapperpanel, "Center");
            jpanel3.add(jpanel2, "South");
            getContentPane().setLayout(new GridLayout(1, 1));
            getContentPane().add(jpanel3);
            pack();
            toFront();
            UIUtil.positionComponent(0, this);
            setVisible(true);
        }

        JLabel promptLabel;
        JTextField hostname;
        boolean cancelled=true;
        boolean advanced= false;

        SshToolsSimpleConnectionDialog()
        {
            super((Frame)null, title, true);
            promptLabel = new JLabel();
            hostname = new JTextField(lastHost, 15);
            init();
        }

        SshToolsSimpleConnectionDialog(Frame frame)
        {
            super(frame, title, true);
            promptLabel = new JLabel();
            hostname = new JTextField(lastHost, 15);
            init();
        }

        SshToolsSimpleConnectionDialog(Dialog dialog)
        {
            super(dialog, title, true);
            promptLabel = new JLabel();
            hostname = new JTextField(lastHost, 15);
            init();
        }
    }


    private SshToolsSimpleConnectionPrompt()
    {
        title = "Connect to host...";
    }

    public void setParentComponent(Component component)
    {
        parent = component;
    }

    public boolean getHostname(StringBuffer stringbuffer, String lastHost)
    {
	this.lastHost = lastHost;
	advanced = false;
        Window window = parent != null ? (Window)SwingUtilities.getAncestorOfClass(java.awt.Window.class, parent) : null;
	SshToolsSimpleConnectionDialog dialog = null;
        if(window instanceof Frame)
            dialog = new SshToolsSimpleConnectionDialog((Frame)window);
        else
        if(window instanceof Dialog)
            dialog = new SshToolsSimpleConnectionDialog((Dialog)window);
        else
            dialog = new SshToolsSimpleConnectionDialog();
        String ac = dialog.getHostname();
        if(ac != null)
            stringbuffer.append(ac);
	advanced = dialog.getAdvanced();
        return dialog.getCancelled();
    }

    public static SshToolsSimpleConnectionPrompt getInstance()
    {
        if(instance == null)
            instance = new SshToolsSimpleConnectionPrompt();
        return instance;
    }

    public void setTitle(String s)
    {
        title = s;
    }
    public boolean getAdvanced()
    {
	return advanced;
    }

    private static SshToolsSimpleConnectionPrompt instance;
    private Component parent;
    private String title;
    private boolean advanced;
    private String lastHost;

}

/*
 *  GSI-SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2005-7 STFC/CCLRC.
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



package com.sshtools.j2ssh.authentication;

import com.sshtools.common.ui.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Component;

public class MozillaPrompt
{
    class MozillaDialog extends JDialog
    {

        char[] getPassword()
        {
            return !cancelled ? password.getPassword() : null;
        }

        boolean getCancelled()
        {
            return cancelled;
        }

        void init()
        {
            setDefaultCloseOperation(2);
	    JLabel jlab = new JLabel("Your master certificate store passphrase is needed to access "+theDesc+" certificate store");
            JPanel jpanel = new JPanel(new GridBagLayout());
            GridBagConstraints gridbagconstraints = new GridBagConstraints();
            gridbagconstraints.insets = new Insets(0, 0, 2, 2);
            gridbagconstraints.anchor = 17;
            gridbagconstraints.fill = 2;
            gridbagconstraints.weightx = 0.0D;
            UIUtil.jGridBagAdd(jpanel, new JLabel("Certificate Store Passphrase: "), gridbagconstraints, -1);
            gridbagconstraints.weightx = 1.0D;
            UIUtil.jGridBagAdd(jpanel, password, gridbagconstraints, 0);
            promptLabel.setHorizontalAlignment(0);
            JPanel jpanel1 = new JPanel(new BorderLayout());
            jpanel1.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            jpanel1.add(jlab, "North");
            jpanel1.add(jpanel, "Center");
            JButton jbutton = new JButton("Ok");
            jbutton.setMnemonic('o');
            jbutton.setDefaultCapable(true);
            jbutton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
                {
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
                    hide();
                }

            });
            JPanel jpanel2 = new JPanel(new FlowLayout(2, 0, 0));
            jpanel2.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            jpanel2.add(jbutton1);
            jpanel2.add(jbutton);
            IconWrapperPanel iconwrapperpanel = new IconWrapperPanel(new ResourceIcon("largelock.png"), jpanel1);
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
        JPasswordField password;
        boolean cancelled;

        MozillaDialog()
        {
            super((Frame)null, title, true);
            promptLabel = new JLabel();
            password = new JPasswordField(15);
            init();
        }

        MozillaDialog(Frame frame)
        {
            super(frame, title, true);
            promptLabel = new JLabel();
            password = new JPasswordField(15);
            init();
        }

        MozillaDialog(Dialog dialog)
        {
            super(dialog, title, true);
            promptLabel = new JLabel();
            password = new JPasswordField(15);
            init();
        }
    }


    private MozillaPrompt()
    {
    }

    public void setParentComponent(Component component)
    {
        parent = component;
    }

    public boolean getGridPassword(Component component, StringBuffer stringbuffer, String descrip)
    {
	theDesc=descrip;
	title = theDesc+" Certificate Store";
	if(component==null) component=parent;
        Window window = component != null ? (Window)SwingUtilities.getAncestorOfClass(java.awt.Window.class, component) : null;
        MozillaDialog gridproxyinitdialog = null;
        if(window instanceof Frame)
            gridproxyinitdialog = new  MozillaDialog((Frame)window);
        else
        if(window instanceof Dialog)
            gridproxyinitdialog = new  MozillaDialog((Dialog)window);
        else
            gridproxyinitdialog = new  MozillaDialog();
        char ac[] = gridproxyinitdialog.getPassword();
        if(ac != null)
            stringbuffer.append(new String(ac));
        return gridproxyinitdialog.getCancelled();
    }

    public static MozillaPrompt getInstance()
    {
        if(instance == null)
            instance = new MozillaPrompt();
        return instance;
    }

    public void setTitle(String s)
    {
        title = s;
    }

    public static final String PASSWORD_ICON = "/com/sshtools/common/authentication/largepassword.png";
    private static MozillaPrompt instance;
    private Component parent;
    private String title="Firefox Certificate Store";
    private String theDesc;

}

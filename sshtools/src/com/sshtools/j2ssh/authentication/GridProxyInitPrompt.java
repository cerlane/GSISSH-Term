/**
 * Copyright (c) 2004, National Research Council of Canada
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice(s) and this licence appear in all copies of the Software or 
 * substantial portions of the Software, and that both the above copyright notice(s) and this 
 * license appear in supporting documentation.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE BE LIABLE 
 * FOR ANY CLAIM, OR ANY DIRECT, INDIRECT, SPECIAL OR CONSEQUENTIAL 
 * DAMAGES, OR ANY DAMAGES WHATSOEVER (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWSOEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OF THE SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Except as contained in this notice, the name of a copyright holder shall NOT be used in 
 * advertising or otherwise to promote the sale, use or other dealings in this Software 
 * without specific prior written authorization.  Title to copyright in this software and any 
 * associated documentation will at all times remain with copyright holders.
 */
 // (Changes (c) STFC/CCLRC 2006-7)


package com.sshtools.j2ssh.authentication;

import com.sshtools.common.ui.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Component;

public class GridProxyInitPrompt
{
    class GridProxyInitDialog extends JDialog
    {

        char[] getPassword()
        {
            return (!cancelled && !useanother) ? password.getPassword() : null;
        }

        boolean getCancelled()
        {
            return cancelled;
        }

        boolean getUseAnother()
        {
            return useanother;
        }
        //CERLANE: Added certType
        void init(String certType)
        {
            setDefaultCloseOperation(2);
            JPanel jpanel = new JPanel(new GridBagLayout());
            GridBagConstraints gridbagconstraints = new GridBagConstraints();
            gridbagconstraints.insets = new Insets(0, 0, 2, 2);
            gridbagconstraints.anchor = 17;
            gridbagconstraints.fill = 2;
            gridbagconstraints.weightx = 0.0D;
            UIUtil.jGridBagAdd(jpanel, new JLabel("Grid Certificate ("+certType+") Passphrase: "), gridbagconstraints, -1);
            gridbagconstraints.weightx = 1.0D;
            UIUtil.jGridBagAdd(jpanel, password, gridbagconstraints, 0);
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
		    useanother = false;
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
		    useanother = false;
                    hide();
                }

            });
            JButton jbutton2 = new JButton("Use Another Method");
            jbutton2.setMnemonic('a');
            jbutton2.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
                {
                    cancelled = false;
		    useanother = true;
                    hide();
                }

            });
            JPanel jpanel2 = new JPanel(new FlowLayout(2, 0, 0));
            jpanel2.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            jpanel2.add(jbutton2);
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
        boolean cancelled = true;
        boolean useanother;
        
        //CERLANE: Added certType
        GridProxyInitDialog(String certType)
        {
            super((Frame)null, title, true);
            promptLabel = new JLabel();
            password = new JPasswordField(15);
            init(certType);
        }

        GridProxyInitDialog(Frame frame, String certType)
        {
            super(frame, title, true);
            promptLabel = new JLabel();
            password = new JPasswordField(15);
            init(certType);
        }

        GridProxyInitDialog(Dialog dialog, String certType)
        {
            super(dialog, title, true);
            promptLabel = new JLabel();
            password = new JPasswordField(15);
            init(certType);
        }
    }


    private GridProxyInitPrompt()
    {
        title = "  ";
    }

    public void setParentComponent(Component component)
    {
        parent = component;
    }
    
    //CERLANE: added certType
    public boolean getGridPassword(Component component, StringBuffer stringbuffer, String certType)
    {
	if(component==null) component=parent;
        Window window = component != null ? (Window)SwingUtilities.getAncestorOfClass(java.awt.Window.class, component) : null;
        GridProxyInitDialog gridproxyinitdialog = null;
        if(window instanceof Frame)
            gridproxyinitdialog = new GridProxyInitDialog((Frame)window, certType);
        else
        if(window instanceof Dialog)
            gridproxyinitdialog = new GridProxyInitDialog((Dialog)window, certType);
        else
            gridproxyinitdialog = new GridProxyInitDialog(certType);
        char ac[] = gridproxyinitdialog.getPassword();
        if(ac != null)
            stringbuffer.append(new String(ac));
	last = gridproxyinitdialog;
        return gridproxyinitdialog.getCancelled();
    }

    public boolean getUseAnother() {
	return last.getUseAnother();
    }

    public static GridProxyInitPrompt getInstance()
    {
        if(instance == null)
            instance = new GridProxyInitPrompt();
        return instance;
    }

    public void setTitle(String s)
    {
        title = s;
    }

    public static final String PASSWORD_ICON = "/com/sshtools/common/authentication/largepassword.png";
    private static GridProxyInitPrompt instance;
    GridProxyInitDialog last = null;
    private Component parent;
    private String title;

}

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
import com.sshtools.sshterm.SshTerminalPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import java.io.File;
import java.awt.Color;
import java.awt.Component;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;

public class MyProxyPrompt
{
	class MyProxyDialog extends JDialog
	{

		String getPassword()
		{
			if(cancelled || !myproxy)
				return null;
			else
				return new String(password.getPassword());
		}
		String getCPassword()
		{
			if(cancelled || myproxy || browser)
				return null;
			else
				return new String(cpassword.getPassword());
		}
		String getHost()
		{
			return (!cancelled && myproxy) ? host.getText() : null;
		}

		String getAccountName()
		{
			return (!cancelled && myproxy) ? name.getText() : null;
		}
		String getVOName()
		{
			return (!cancelled && myproxy) ? voname.getText() : null;
		}
		String getCFile()
		{
			return (!cancelled && !myproxy && !browser) ? cfile.getText() : null;
		}

		String getPKCSFile() {
			return cfile.getText();
		}


		boolean getCanceled()
		{
			return cancelled;
		}


		void setFile(File f) { cfile.setText( f.getAbsolutePath()); }

		void init()
		{
			// MyProxy Icon Panel!

			setDefaultCloseOperation(2);
			JPanel pMyproxy = new JPanel(new GridBagLayout());
			//setAlwaysOnTop(true);
			GridBagConstraints gridbagconstraints = new GridBagConstraints();
			gridbagconstraints.insets = new Insets(0, 0, 2, 2);
			gridbagconstraints.anchor = 17;
			gridbagconstraints.fill = 2;
			gridbagconstraints.weightx = 0.0D;
			UIUtil.jGridBagAdd(pMyproxy, new JLabel("Host: "), gridbagconstraints, -1);
			gridbagconstraints.weightx = 1.0D;
			UIUtil.jGridBagAdd(pMyproxy, host, gridbagconstraints, 0);
			gridbagconstraints.weightx = 0.0D;
			UIUtil.jGridBagAdd(pMyproxy, new JLabel("Account Name: "), gridbagconstraints, -1);
			gridbagconstraints.weightx = 1.0D;
			UIUtil.jGridBagAdd(pMyproxy, name, gridbagconstraints, 0);
			gridbagconstraints.weightx = 0.0D;
			UIUtil.jGridBagAdd(pMyproxy, new JLabel("Passphrase: "), gridbagconstraints, -1);
			gridbagconstraints.weightx = 1.0D;
			UIUtil.jGridBagAdd(pMyproxy, password, gridbagconstraints, 0);            
			gridbagconstraints.weightx = 0.0D;
			UIUtil.jGridBagAdd(pMyproxy, new JLabel("VO Name: "), gridbagconstraints, -1);
			gridbagconstraints.weightx = 1.0D;
			UIUtil.jGridBagAdd(pMyproxy, voname, gridbagconstraints, 0);
			promptLabel.setHorizontalAlignment(0);

			JPanel pOuterMP = new JPanel(new BorderLayout());
			pOuterMP.add(promptLabel, "North");
			pOuterMP.add(pMyproxy, "Center");

			JButton jbutton = new JButton("Use MyProxy");
			jbutton.setMnemonic('m');
			jbutton.setDefaultCapable(true);
			jbutton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent actionevent)
				{
					another = false;
					cancelled = false;
					browser = false;
					myproxy=true;
					cancelled = false;
					hide();
				}

			});
			getRootPane().setDefaultButton(jbutton);

			JPanel jpanel2 = new JPanel(new FlowLayout(2, 0, 0));
			jpanel2.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
			jpanel2.add(jbutton);
			pOuterMP.add(jpanel2, "South");

			IconWrapperPanel iconwrapperpanel = new IconWrapperPanel(new ResourceIcon("largelock.png"), pOuterMP);
			iconwrapperpanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			// PKCS12 iconpanel

			JButton jbuttonB = new JButton("Browse...");
			jbuttonB.setMnemonic('b');
			jbuttonB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent actionevent)
				{
					JFileChooser chooser = new JFileChooser();
					ExampleFileFilter filter = new ExampleFileFilter();
					filter.addExtension("pfx");
					filter.addExtension("p12");
					filter.setDescription("pfx and p12 files");
					chooser.setFileFilter(filter);
					chooser.setFileHidingEnabled(false);
					chooser.setDialogTitle("Select Certificate File For Authentication");

					if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
						setFile( chooser.getSelectedFile());
					}
				}
			});

			cpassword.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int k = e.getKeyCode();
					if(k==e.VK_ENTER) {
						another = false;
						cancelled = false;
						browser = false;
						myproxy=false;
						hide();
						e.consume();
					}
				}
			});

			JPanel filepan = new JPanel(new BorderLayout());
			filepan.add(cfile, BorderLayout.CENTER);
			filepan.add(jbuttonB, BorderLayout.EAST);

			JPanel jpanelC = new JPanel(new GridBagLayout());
			gridbagconstraints = new GridBagConstraints();
			gridbagconstraints.insets = new Insets(0, 0, 2, 2);
			gridbagconstraints.anchor = 17;
			gridbagconstraints.fill = 2;
			gridbagconstraints.weightx = 0.0D;
			UIUtil.jGridBagAdd(jpanelC, new JLabel("Filename: "), gridbagconstraints, -1);
			gridbagconstraints.weightx = 1.0D;
			UIUtil.jGridBagAdd(jpanelC, filepan, gridbagconstraints, 0);
			gridbagconstraints.weightx = 0.0D;
			UIUtil.jGridBagAdd(jpanelC, new JLabel("Passphrase: "), gridbagconstraints, -1);
			gridbagconstraints.weightx = 1.0D;
			UIUtil.jGridBagAdd(jpanelC, cpassword, gridbagconstraints, 0);
			JLabel promptLabelC = new JLabel("Use a Grid certificate in pkcs12 format:");
			promptLabelC.setHorizontalAlignment(0);

			JButton jbuttonC = new JButton("Use Certificate");
			jbuttonC.setMnemonic('t');
			jbuttonC.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent actionevent)
				{
					another = false;
					cancelled = false;
					browser = false;
					myproxy=false;
					hide();
				}

			});

			JPanel jpanelC2 = new JPanel(new BorderLayout());
			jpanelC2.add(jbuttonC, BorderLayout.EAST);

			JPanel jpanelC1 = new JPanel(new BorderLayout());
			jpanelC1.setBorder(BorderFactory.createEmptyBorder(0, 0,0, 0));
			jpanelC1.add(promptLabelC, "North");
			jpanelC1.add(jpanelC, "Center");
			jpanelC1.add(jpanelC2, "South");
			IconWrapperPanel iconwrapperpanelC = new IconWrapperPanel(new ResourceIcon("/com/sshtools/common/authentication/largepassphrase.png"), jpanelC1);            
			iconwrapperpanelC.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			// Certificate from brower Iconpanel

			JButton jbuttonB2 = new JButton("Use Certificate from Browser");
			jbuttonB2.setMnemonic('b');
			jbuttonB2.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent actionevent)
				{
					another = false;
					cancelled = false;
					myproxy=false;
					browser = true;
					hide();
				}

			});
			JPanel jpanelB = new JPanel(new BorderLayout());
			JLabel info = new JLabel("Search for certificates in Internet Explorer or Firefox:");
			JPanel jpanelB2=new JPanel(new BorderLayout());

			JPanel jpanelB3=new JPanel(new BorderLayout());
			jpanelB3.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));
			jpanelB3.add(jbuttonB2, BorderLayout.NORTH);
			jpanelB2.add(jpanelB3, BorderLayout.EAST);
			jpanelB2.add(info, BorderLayout.NORTH);
			jpanelB.add(jpanelB2, BorderLayout.NORTH);
			//jpanelB.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			IconWrapperPanel iconwrapperpanelB = new IconWrapperPanel(new ResourceIcon("/com/sshtools/common/ui/proxy.png"), jpanelB);
			iconwrapperpanelB.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


			JLabel warn=new JLabel(lastError);
			warn.setForeground(Color.red);
			warn.setHorizontalAlignment(JLabel.CENTER);


			JButton jbutton1 = new JButton("Cancel");
			jbutton1.setMnemonic('c');
			jbutton1.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent actionevent)
				{
					another = false;
					cancelled = true;
					browser = false;
					myproxy=false;
					hide();
				}

			});

			JButton jbuttonAnother = new JButton("Try Another Method");
			jbuttonAnother.setMnemonic('a');
			jbuttonAnother.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent actionevent)
				{
					another = true;
					cancelled = false;
					browser = false;
					myproxy=false;
					hide();
				}

			});
			JPanel jpanelEND = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			jpanelEND.add(jbutton1);
			jpanelEND.add(jbuttonAnother);
			jpanelEND.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));



			JPanel jpanelW = new JPanel(new BorderLayout());
			jpanelW.add(warn, BorderLayout.NORTH);

			getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
			getContentPane().add(jpanelW);
			getContentPane().add(iconwrapperpanel);
			getContentPane().add(iconwrapperpanelC);
			getContentPane().add(iconwrapperpanelB);
			getContentPane().add(jpanelEND);
			pack();
			setResizable(false);
			toFront();
			UIUtil.positionComponent(0, this);
			setVisible(true);
			password.requestFocus();
		}

		JLabel promptLabel;
		JTextField host;
		JTextField name;
		JTextField cfile;
		JPasswordField password;
		JPasswordField cpassword;
		JTextField voname;
		boolean cancelled=true;
		boolean myproxy=false;
		boolean browser=false;
		boolean another=false;

		boolean getKeybased() {
			return !cancelled && !another && !browser && !myproxy;
		}

		boolean getAnother() {
			return another;
		}


		boolean getBrowser() {
			return browser;
		}
		MyProxyDialog()
		{
			super((Frame)null, "Grid Certificate/Proxy needed for Authentication", true);
			promptLabel = new JLabel(title);
			host = new JTextField(15);
			name = new JTextField(15);
			voname = new JTextField(15);
			host.setText(default_host);
			name.setText(default_name);
			voname.setText(default_voname);
			password = new JPasswordField(15);

			cpassword = new JPasswordField(15);
			cfile = new JTextField(10);
			if(lastFILE==null) {
				cfile.setText(PreferencesStore.get(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, ""));
			} else {
				cfile.setText(lastFILE);
			}
			init();
		}

		MyProxyDialog(Frame frame)
		{
			super(frame, "Grid Certificate/Proxy needed for Authentication", true);
			promptLabel = new JLabel(title);
			host = new JTextField(15);
			name = new JTextField(15);
			host.setText(default_host);
			name.setText(default_name);
			password = new JPasswordField(15);
			cpassword = new JPasswordField(15);
			cfile = new JTextField(10);
			if(lastFILE==null) {
				cfile.setText(PreferencesStore.get(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, ""));
			} else {
				cfile.setText(lastFILE);
			}
			init();
		}

		MyProxyDialog(Dialog dialog)
		{
			super(dialog, "Grid Certificate/Proxy needed for Authentication", true);
			promptLabel = new JLabel(title);
			host = new JTextField(15);
			name = new JTextField(15);
			host.setText(default_host);
			name.setText(default_name);
			password = new JPasswordField(15);
			cpassword = new JPasswordField(15);
			cfile = new JTextField(10);
			if(lastFILE==null) {
				cfile.setText(PreferencesStore.get(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, ""));
			} else {
				cfile.setText(lastFILE);
			}
			init();
		}
	}


	private MyProxyPrompt()
	{
		title = "Retrieve Credentials from MyProxy:";
	}

	public void setParentComponent(Component component)
	{
		parent = component;
	}

	public boolean doGet(Component component, StringBuffer stringbuffer, StringBuffer stringbuffer1, StringBuffer stringbuffer2, StringBuffer stringbuffer3)
	{
		if(component==null) component=parent;
		Window window = component != null ? (Window)SwingUtilities.getAncestorOfClass(java.awt.Window.class, component) : null;
		MyProxyDialog myproxydialog = null;
		if(window instanceof Frame)
			myproxydialog = new MyProxyDialog((Frame)window);
		else
			if(window instanceof Dialog)
				myproxydialog = new MyProxyDialog((Dialog)window);
			else
				myproxydialog = new MyProxyDialog();
		stringbuffer.append(myproxydialog.getHost());
		stringbuffer1.append(myproxydialog.getAccountName());
		stringbuffer2.append(myproxydialog.getPassword());
		stringbuffer3.append(myproxydialog.getVOName());
		last = myproxydialog;
		lastFILE= last.getPKCSFile();
		return myproxydialog.getCanceled();
	}

	public boolean keyBased(StringBuffer bufferf, StringBuffer bufferp) {
		bufferp.append(last.getCPassword());
		bufferf.append(last.getCFile());
		return last.getKeybased();
	}

	public boolean getBrowser() {
		return last.getBrowser();
	}

	public boolean getAnother() {
		return last.getAnother();
	}

	public static MyProxyPrompt getInstance()
	{
		if(instance == null)
			instance = new MyProxyPrompt();
		return instance;
	}

	public void setTitle(String s)
	{
		title = s;
	}

	public static final String PASSWORD_ICON = "/com/sshtools/common/authentication/largepassword.png";
	private static MyProxyPrompt instance;
	private Component parent;
	private String title;
	private String default_name;
	private String default_voname;
	private String default_host;
	private String lastError="";
	private MyProxyDialog last;
	private String lastFILE = null;

	public void setHost(String newhost) {
		default_host = newhost;
	}

	public void setAccountName(String newname) {
		default_name = newname;
	}

	public void setVOName(String newvoname) {
		default_voname = newvoname;
	}


	public void setError(String s) {
		lastError = s;
	}

	public void setProperties(SshConnectionProperties properties) {
		if(properties instanceof SshToolsConnectionProfile) {
			this.lastFILE = ((SshToolsConnectionProfile) properties).getApplicationProperty(SshTerminalPanel.PREF_PKCS12_DEFAULT_FILE, null);
		}
	}
}

//Changes (c) CCLRC 2006
/*
 *  Sshtools - SSHTerm
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.sshterm;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.File;

import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.SshToolsApplicationApplet;
import com.sshtools.common.ui.SshToolsApplicationClientApplet;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.j2ssh.io.IOUtil;
import com.sshtools.common.configuration.SshToolsConnectionProfile;

public class SshTermApplet
    extends SshToolsApplicationClientApplet {


  //     eurgghh!
  public final static String[][] SSHTERM_PARAMETER_INFO = {
      {"sshterm.ui.scrollBar", "boolean", "Enable / Disable the menu bar"}, 
      {"sshterm.ui.autoHide", "boolean", "Enable / Disable auto-hiding of the tool bar, menu bar, status bar and scroll bar"} ,
      {"sshterm.gsscredential", "string", "String consisting of a proxy credential" },
      {"sshterm.autoconnect.host", "string", "Connect to this host..."},
      {"sshterm.autoconnect.port", "integer", "...on this port..."},
      {"sshterm.autoconnect.username", "string", "...with this username."}
  };
  private boolean scrollBar;
  private boolean autoHide;
  public void initApplet() throws IOException {
    super.initApplet();
    scrollBar = getParameter("sshterm.ui.scrollBar", "true").equals("true");
    autoHide = getParameter("sshterm.ui.autoHide", "false").equals("true");
    readGSSCredentialFromParam();
  }

 /** Read in a GSSCredential from the Java applet's &lt;param&gt; tag.
   * The applet param name is "sshterm.gsscredential" and the
   * value is the proxy credential string such as one would find in
   * a "/tmp/x509u_*" proxy file.
   */

    private void readGSSCredentialFromParam() {
	String gsscredential = getParameter("sshterm.gsscredential");
	if ((gsscredential != null) && 
	    (gsscredential.length() > 0)) {
	    /* The call to getParameter removes all newlines.  So we need
	     * to scan the gsscredential string and add back newlines
	     * for the -----BEGIN----- and -----END----- tags.  Otherwise,
	     * the base64 decoder cannot read in the credential.
	     */
	    StringBuffer sb = new StringBuffer(gsscredential);
	    String dashes = "-----";
	    String newline = System.getProperty("line.separator");
	    boolean foundOpeningDashes = false;
	    int fromIndex = 0;
	    while (fromIndex >= 0) {
		fromIndex = sb.indexOf(dashes,fromIndex);
		if (fromIndex >= 0) {
		    if (foundOpeningDashes) {
			// Add newline after closing dashes
			sb.insert(fromIndex+dashes.length(),newline);
		    } else {
                       // Add newline before opening dashes, except for first
			if (fromIndex > 0) {
			    sb.insert(fromIndex,newline);
			}
		    }
		    fromIndex += dashes.length();  // Skip past found dashes
		    foundOpeningDashes = !foundOpeningDashes;
		}
	    }
  
	    com.sshtools.j2ssh.authentication.UserGridCredential.setParamGSSCredential(sb.toString());
	}
    }


    public void stop() {
	com.sshtools.common.util.ShutdownHooks.exit(false);
	applicationPanel.getApplication().exit(false);
    }

  public String[][] getParameterInfo() {
    String[][] s = super.getParameterInfo();
    String[][] x = new String[s.length + SSHTERM_PARAMETER_INFO.length][];
    System.arraycopy(s, 0, x, 0, s.length);
    System.arraycopy(SSHTERM_PARAMETER_INFO, 0, x, s.length,
                     SSHTERM_PARAMETER_INFO.length);
    return x;
  }

  public String getAppletInfo() {
    return "SSHTerm";
  }

  public SshToolsApplicationPanel createApplicationPanel() throws
      SshToolsApplicationException {
      SshTerm term = null;
      try {
	  term = new SshTerm();
	  term.init(new String[] {});
      } catch(Exception e) {
	  e.printStackTrace();
      }
    SshToolsApplicationClientApplet.SshToolsApplicationAppletContainer
        container =
        new SshToolsApplicationClientApplet.SshToolsApplicationAppletContainer();
    term.newContainer(container);
    SshTerminalPanel panel = (SshTerminalPanel) container
        .getApplicationPanel();
    panel.setScrollBarVisible(scrollBar);
    panel.setAutoHideTools(autoHide);
    panel.setToolsVisible(true);
    autoConnect(panel);
    return panel;
  } 

    public void autoConnect(SshTerminalPanel sshTP){
	
        String host = getParameter("sshterm.autoconnect.host");
        String port = getParameter("sshterm.autoconnect.port");
        String username = getParameter("sshterm.autoconnect.username");

        if ( host == null || port == null || username == null) {
            return;
        } else {
            SshToolsConnectionProfile p = new SshToolsConnectionProfile();

            p.setHost(host);
            p.setPort(Integer.valueOf(port));
            p.setUsername(username);

            sshTP.connect(p, true);
        }
    }
 
}

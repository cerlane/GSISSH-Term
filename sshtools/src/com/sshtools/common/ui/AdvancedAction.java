//Duplication & modifications (c) STFC 2007
/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Action;
//import com.sshtools.common.util.BrowserLauncher;
import com.sshtools.common.util.BareBonesBrowserLaunch;


/**
 *
 *
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class AdvancedAction
    extends StandardAction {
  private final static String ACTION_COMMAND_KEY_ABOUT = "Advanced-command";
  private final static String NAME_ABOUT = "Advanced Help";
    private final static String SMALL_ICON_ABOUT = 
     "/com/sshtools/common/ui/help.png";
  private final static String LARGE_ICON_ABOUT = "";
  private final static int MNEMONIC_KEY_ABOUT = ' ';

  /**
   * Creates a new AdvancedAction object.
   *
   * @param parent
   * @param application
   */
  public AdvancedAction() {
    putValue(Action.NAME, NAME_ABOUT);
    putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
    putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
    putValue(Action.SHORT_DESCRIPTION,
             "Advanced Help");
    putValue(Action.LONG_DESCRIPTION,
             "Show the Advanced Help page");
    //putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "Help");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(90));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(65));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(false));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(90));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(5));
  }

  /**
   *
   *
   * @param evt
   */
  public void actionPerformed(ActionEvent evt) {
      /* Patch contributed by Jon Siwek. 
       * Use the bare bones browser launcher rather than BrowserLauncher. 
       * Several Help menu actions attempt to launch a browser and point it 
       * at a URL. The old BrowserLauncher class contained some deprecated 
       * elements and no longer worked on Mac OS X or Linux. 
       * This patch updates to BareBonesBrowserLauncher which, at the time of 
       * implementation (Nov, 09), works on all platforms.
       * URL should not be hard-wired.
       */
      BareBonesBrowserLaunch.openURL("http://www.lrz.de/services/compute/grid_en/software_en/gsisshterm_en/");

      /* Original src
       try {
	  BrowserLauncher.openURL("http://wiki.ngs.ac.uk/index.php?title=GSI-SSHTerm");
      } catch (java.io.IOException ioe) {
          ioe.printStackTrace();
      }
       */
  }
}

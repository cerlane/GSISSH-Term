/*
 *  GSI-SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2005-6 CCLRC.
 *
 *  Based on NewAction (c) 2002 Lee David Painter.
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

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.StandardAction;

class ProxyDestroyAction
    extends StandardAction {
  private final static String ACTION_COMMAND_KEY_NEW = "proxydestroy-command";
  private final static String NAME_NEW = "Destroy Proxy";
  private final static String SMALL_ICON_NEW = "";
  private final static String LARGE_ICON_NEW = "";
  private final static String SHORT_DESCRIPTION_NEW = "Destroy Grid Proxy";
  private final static String LONG_DESCRIPTION_NEW =
      "Destroy your Grid proxy stored on disk";
  private final static int MNEMONIC_KEY_NEW = 'D';
  public ProxyDestroyAction() {
    putValue(Action.NAME, NAME_NEW);
    putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_NEW));
    putValue(LARGE_ICON, getIcon(LARGE_ICON_NEW));
    putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_NEW);
    putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_NEW);
  //Cerlane (ALT to CTRL)
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_D, Keyboard_Modifier));
    putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_NEW));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_NEW);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "Proxy");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(0));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(2));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(false));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(0));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(0));
  }
}

/*
 *  Sshtools - SSHTerm
 *
 *  The contents of this package have been derived from the Java
 *  Telnet/SSH Applet from http://javassh.org. The files have been
 *  modified and are supplied under the terms of the original license.
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
package com.sshtools.sshterm.emulation;

import java.io.OutputStream;
import java.util.Properties;

public interface VDUInput {
  public final static int KEY_CONTROL = 0x01;
  public final static int KEY_SHIFT = 0x02;
  public final static int KEY_ALT = 0x04;
  public final static int KEY_ACTION = 0x08;
  //Cerlane adds Mac Command key
  public final static int KEY_META = 0x100;
  
  public OutputStream getOutputStream();

  void mousePressed(int x, int y, int modifiers);

  void mouseReleased(int x, int y, int modifiers);

  void setKeyCodes(Properties codes);

  void keyPressed(int keyCode, char keyChar, int modifiers);

  void keyTyped(int keyCode, char keyChar, int modifiers);
}

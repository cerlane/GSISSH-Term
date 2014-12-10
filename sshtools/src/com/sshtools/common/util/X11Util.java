//Changes (c) CCLRC 2006
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

package com.sshtools.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sshtools.j2ssh.io.IOUtil;
import com.sshtools.j2ssh.io.IOStreamConnector;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.forwarding.XDisplay;
/**
 *
 *
 * @author $author$
 * @version $Revision: 1.5 $
 */
public class X11Util {
  // Logger

  /**  */
    protected static Log log = LogFactory.getLog(X11Util.class);
    static byte[] table = {
	0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62,
	0x63, 0x64, 0x65, 0x66
    };

    public static X11Cookie getCookie(XDisplay xdisplay) {
	if(System.getProperty("os.name").startsWith("Windows")) return getRNDCookie();
	log.debug("Getting X11 cookie using xauth");
	try {
	    String host = xdisplay.getHost();
	    String display=host+":"+xdisplay.getDisplay();
	    String cmd="xauth list "+display+" 2>/dev/null";
	    if(host==null || host.equals("localhost") || host.equals("unix")) {
		cmd = "xauth list :"+xdisplay.getDisplay()+" 2>/dev/null";
	    }

	    Process process = null;
	    InputStream in = null;
	    OutputStream out = null;  
	    try {
		log.debug("Executing " + cmd);
		process = Runtime.getRuntime().exec(cmd);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in = process.getInputStream()));
		out = process.getOutputStream();
		String line = null;
		String cookie = null;
		while( ( line = reader.readLine() ) != null) {
		    log.debug(line);
		    StringTokenizer t = new StringTokenizer(line);
		    try {
			String fhost = t.nextToken();
			String type = t.nextToken();
			String value = t.nextToken();
			if(cookie == null) {
			    cookie = value;
			    log.debug("Using cookie " + cookie);
			}
			return expand(type, cookie);
		    }
		    catch(Exception e) {
			log.error("Unexpected response from xauth.", e);
			log.warn("Trying random data.");
			return getRNDCookie();
		    }
		}
	    } finally {
		IOUtil.closeStream(in);
		IOUtil.closeStream(out);
	    }
	    return getRNDCookie();
	} catch(Exception e) {
	    log.warn("Had problem finding xauth data ("+e+") trying random data.");
	    return getRNDCookie();
	}
    }


  /**
   *
   *
   * @param displayNumber
   *
   * @return
   */
  private static String createCookie() {
    log.warn("Creating fake cookie");

    StringBuffer b = new StringBuffer();

    for (int i = 0; i < 16; i++) {
      int r = (int) (Math.random() * 256);
      String h = Integer.toHexString(r);

      if (h.length() == 1) {
        b.append(0);
      }

      b.append(h);
    }

    log.debug("Fake cookie is " + b.toString());

    return b.toString();
  }

  private static String createMatchingCookie(int length) {
    log.debug("Creating fake cookie");

    StringBuffer b = new StringBuffer();

    length = length / 2;

    for (int i = 0; i < 16; i++) {
      int r = (int) (Math.random() * 256);
      String h = Integer.toHexString(r);

      if (h.length() == 1) {
        b.append(0);
      }

      b.append(h);
    }

    log.debug("Fake cookie is " + b.toString());

    return b.toString();
  }

private static X11Cookie expand(String authType, String realAuthData) {
    return new X11Cookie(authType, realAuthData, createMatchingCookie(realAuthData.length()));
}

private static X11Cookie getRNDCookie() {
    String auth = createCookie();
    return new X11Cookie("MIT-MAGIC-COOKIE-1", auth, auth);
}

    public static class X11Cookie {
	String authType;
	String realAuthData;
	String fakeAuthData;

	public X11Cookie(String authType, String realAuthData, String fakeAuthData) {
	    this.authType = authType;
	    this.realAuthData = realAuthData;
	    this.fakeAuthData = fakeAuthData;
	}

	public String getAuthType() { return authType; }
	public String getRealAuthData() { return realAuthData; }
	public String getFakeAuthData() { return fakeAuthData; }
	
    }
}

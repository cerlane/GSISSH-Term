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

package com.sshtools.j2ssh.forwarding;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.connection.SocketChannel;
import com.sshtools.j2ssh.connection.SshMsgChannelData;

public class ForwardingSocketX11Channel extends ForwardingSocketChannel {
    private static Log log = LogFactory.getLog(ForwardingSocketX11Channel.class);

    private String authType;
    private String fakeAuthData;
    private String realAuthData;

    public ForwardingSocketX11Channel(String forwardType,
				      String name, 
				      String hostToConnectOrBind,
				      int portToConnectOrBind,
				      String originatingHost, int originatingPort,
				      String authType, String fakeAuthData, String realAuthData) throws
					  ForwardingConfigurationException {
      super(forwardType, name, hostToConnectOrBind, portToConnectOrBind, originatingHost, originatingPort);
      
      this.authType = authType;
      this.fakeAuthData = fakeAuthData;
      this.realAuthData = realAuthData;    
  }

    private boolean firstPacket = true;
    private byte[] dataSoFar = null;

    // if this returns true then it has substitued the data.
    // if this returns false then new data required
    // otherwise it throws an error

    private boolean allDataCheckSubst() throws IOException {
	int plen,dlen;
	//check if all of the X11 session opening packet is in the buffer
	if(dataSoFar.length<12) return false; // not got fixed length part
	if(dataSoFar[0]==0x42) { //MSB first
	    plen = 256*dataSoFar[6]+dataSoFar[7];
	    dlen = 256*dataSoFar[8]+dataSoFar[9];
	} else if(dataSoFar[0]==0x6C) {//LSB first
	    plen = 256*dataSoFar[7]+dataSoFar[6];
	    dlen = 256*dataSoFar[9]+dataSoFar[8];
	} else {
	    throw new IOException("Bad initial X11 packet: bad byte order byte: "+dataSoFar[0]);
	}


	if(dataSoFar.length < (12+ ((plen+3) & ~3)+((dlen+3) & ~3))) return false; // not all packet
	if(plen!=authType.length()) {
	    throw new IOException("X11 connection uses different authentication protocol.");
	} else {
	    if(!authType.equals(new String(dataSoFar, 12, plen, "US-ASCII"))) {
		throw new IOException("X11 connection uses different authentication protocol.");
	    }
	}
	if(fakeAuthData.length()!=realAuthData.length()) throw new IOException("fake and real X11 authentication data differ in length.");
	int len = fakeAuthData.length()/2;
	byte newdata[] = new byte[len];
	if(dlen!=len) {
	    throw new IOException("X11 connection used wrong authentication data.");
	} else {
	    for(int i=0;i<len;i++) {
		byte data = (byte) Integer.parseInt(fakeAuthData.substring(i*2,i*2+2), 16);
		if(data!=dataSoFar[i+(12+((plen+3) & ~3))]) throw new IOException("X11 connection used wrong authentication data.");
		newdata[i] = (byte) Integer.parseInt(realAuthData.substring(i*2,i*2+2), 16);
	    }
	}
	System.arraycopy(newdata, 0, dataSoFar, 12+((plen+3) & ~3), dlen);
	return true;

    }


  /**
   * Redefine onChannelData... for first packet check that there is the correct fake authentication data and then
   * substitute the real data.
   *
   */
  protected void onChannelData(SshMsgChannelData msg) throws IOException {
      if(firstPacket) {
	  if(dataSoFar==null) {
	      dataSoFar = msg.getChannelData();
	  } else {
	      byte newData[] = msg.getChannelData();
	      byte data[] = new byte[dataSoFar.length+newData.length];
	      System.arraycopy(dataSoFar, 0, data, 0, dataSoFar.length);
	      System.arraycopy(newData, 0, data, dataSoFar.length, newData.length);
	      dataSoFar = data;
	  }
	  if(allDataCheckSubst()) {
	      firstPacket = false;
	      socket.getOutputStream().write(dataSoFar);
	  }
      } else {
	  try {
	      socket.getOutputStream().write(msg.getChannelData());
	  }
	  catch (IOException ex) {
	  }
      }
  }
}

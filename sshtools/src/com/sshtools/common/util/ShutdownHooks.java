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


package com.sshtools.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;

// Provides registration of shutdown code and files to delete.  This relies on either being called from applet.stop() or being registered as a 
// shutdownhook by an application.
 
public class ShutdownHooks extends Thread {

    protected static Log log = LogFactory.getLog(ShutdownHooks.class);
    
    private static LinkedList files = new LinkedList();
    private static LinkedList codes = new LinkedList();

    public static synchronized void deleteOnExit(String filename) {
	files.addFirst(filename);
    }

    public static synchronized void runOnExit(Runnable codeblob) {
	codes.addFirst(codeblob);
    }

    private static class Helper extends Thread {
	public Helper(Runnable r, Notif n) {
	    task = r;
	    notif = n;
	}

	Runnable task;
	Notif notif;
	public void run() {
	    task.run();
	    notif.done();
	}
    }

    private static class Notif extends Thread {
	public Notif(int n) {
	    count = n;
	}
	volatile int count;
	private Object lock = new Object();

	public void done() {
	    synchronized(lock) {
		count--;
		lock.notify();
	    }
	}

	public void run() {
	    synchronized(lock) {
		while(count>0) {
		    try {
			lock.wait();
		    } catch(java.lang.InterruptedException i) {}
		}
	    }
	    System.exit(0);
	}
	
    }

    public static synchronized void exit(final boolean exit) {
	Iterator ifiles = files.iterator();
	while(ifiles.hasNext()) {
	    String file = (String)ifiles.next();
	    File f = new File(file);
	    if(f!=null & f.exists()) {
		f.delete();
	    }
	}
	Iterator icodes = codes.iterator();
	Notif n = new Notif(codes.size());
	while(icodes.hasNext()) {
	    Runnable code = (Runnable)icodes.next();
	    try {
		Thread t = new Helper(code, n);
		t.start();
		if(!exit) t.join();
	    } catch(IllegalThreadStateException e) {
		log.error("Exception", e);
	    } catch(InterruptedException e) {
	       	log.error("Exception", e);
	    }
	}
      
	files = new LinkedList();
	codes = new LinkedList();
	
	if(exit) n.start();
    }

    public void run() {
	exit(false);
    }
}

/*
 *  SSHTools - VO Management Tool
 *
 *  Copyright (C) 2013 Siew Hoon Leong
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

package com.sshtools.common.vomanagementtool.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;


import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.j2ssh.authentication.UserGridCredential;
import com.sshtools.sshterm.SshTerminalPanel;
import com.sshtools.common.vomanagementtool.ExtraPanel.NewVOPanel;
import com.sshtools.common.vomanagementtool.ExtraPanel.VOMSLocationPanel;



public class VOHelper {
	private static TreeMap allVOs = null;
	private static String vomsConfigURL = PreferencesStore.get(SshTerminalPanel.PREF_VOMS_CONFIG_URL, UserGridCredential.DEFAULT_VOMS_CONFIG_URL);
	private static String vomslocation = PreferencesStore.get(SshTerminalPanel.PREF_VOMS_LOCATION, UserGridCredential.DEFAULT_VOMS_LOCATION);
	private static String vomses = vomslocation + File.separator  + "vomses";
	private static String vomsdir = vomslocation + File.separator  + "voms";
	private static String favouritesFile = vomslocation + File.separator  + "vomses" + File.separator + "Favourites";
	private static String[] listeInfra;
	
	public static final String FAVOURITES = "Favourites";
	//TODO 
	public static final String EGI = "egi";
	public static final String OTHERS = "ige";
	
	public static void refreshVOMSConfig(){
		vomses =  vomslocation + File.separator  + "vomses";
		vomsdir = vomslocation + File.separator  + "voms";
		favouritesFile = vomses + File.separator + "Favourites";
		
	}
	
	public static void setupVOMSProperties(){
		System.setProperty("VOMSES_LOCATION", vomses);
		System.setProperty("VOMSDIR", vomsdir);
		System.setProperty("CADIR", System.getProperty("user.home")+ File.separator+ ".globus" + File.separator + "certificates");
	}
	public static boolean checkVOExists(String voName, String server){
		boolean isExist = false;
		String voDir = vomsdir + File.separator + voName;
		String voFilelsc = voDir + File.separator + server + ".lsc";
		if (new File(voDir).isDirectory()){
			if (new File(voFilelsc).isFile()){	
				isExist = true;
				JOptionPane.showMessageDialog(null, "This VO '"+voName+"' with server '+ server +' already exists.", "Error: Add new VO", JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
		return isExist;
	}
	
	public static boolean retrieveSetup(){
		boolean isSuccessful = false;
		//getVOMConfig();
		allVOs = new TreeMap<String, TreeMap>();
		
		File folder = new File(vomses);
		if (folder.isDirectory()){
			File[] listOfFiles = folder.listFiles();
			if(listOfFiles!=null){
				listeInfra = new String[listOfFiles.length];				
			}
			for (int i=0; i<listOfFiles.length; i++){
				if(listOfFiles[i].isFile()){
					String group=listOfFiles[i].getName();
					TreeMap vos = readVomsesFile(listOfFiles[i]);					
					allVOs.put(group, vos);					
					listeInfra[i] = group;
					isSuccessful=true;
				}
			}
		}
		return isSuccessful;
	}
	 public static void checkUpdateVOMSConfigFiles() throws IOException {
	    	refreshVOMSConfig();
	        if (!(new File(vomslocation).exists())) {
	            boolean success = (new File(vomslocation).mkdir());
	            if (!success) {
	                   throw new IOException("Couldn't create directory for VOMS support: "+vomslocation);
	            }
	        }
	        if(!new File(vomslocation).isDirectory()) {
	        	throw new IOException("Location: "+vomslocation+" is not a directory");
	        }
	        else{
		        
		        URL vomsURL = new URL (vomsConfigURL);
		        InputStream in;
		        try{
		        	in = new GZIPInputStream(vomsURL.openStream());
		        	
		        }
		        catch(FileNotFoundException fnf){
		        	throw new IOException("URL: " + vomslocation + " does not exists."); 	
		        }
		        catch(IOException ioe){
		        	throw new IOException("Failed to download VOMS config '"+vomsConfigURL+"'.\n" + ioe.getMessage()); 			
		        }
		        
		        try{
		        	untar(in, vomslocation);
		        }
		        catch(IOException ioe){		        	
		        	throw new IOException("Untarring VOMS config into '"+vomslocation+"'.\n" + ioe.getMessage()); 			
		        }
	        }
	        
	        //Favourites
	        if (!(new File(favouritesFile).exists())) {
	            boolean success = (new File(favouritesFile).createNewFile());
	            if (!success) {
	                   throw new IOException("Couldn't create 'Favourites' directory to allow you to store your VOs: "+favouritesFile);
	            }	            
	        }
	        try{
        		Chmod(true, "755", vomslocation);
        	}
        	catch(IOException ioe){
        		throw new IOException("Cannot change directory permission of " + vomslocation + ".");         			
     		}
	        
	    }
	    
	public static boolean addNewVO(TreeMap info) throws IOException{
		boolean isSuccessful = false;
		String voname = (String) info.get("localvoname");
		String server = (String) info.get("server");
		int port = (Integer) info.get("port");
		String serverdn = (String) info.get("serverdn");
		String serverissuerdn = (String) info.get("serverissuerdn");
		//Make this directory in voms
		String path = vomsdir+File.separator+voname;
		
		if (!new File(path).isDirectory()){
			if (new File(path).mkdir()){
				try {
					Chmod(true, "go-w", path);
				} catch (IOException e) {
					throw new IOException("Cannot change directory permission of " + vomslocation + ".");
				}
			}
		}
		//successfully created the directory
		//Now need to add a lsc file
		String lscfile = path + File.separator + server +".lsc";
		if (!new File(lscfile).isFile()){
			String content = serverdn + "\n" + serverissuerdn;
			try {
				writeToFile(lscfile, content);
	
				//Now add line to vomses file
				String lscString = "\""+voname+"\"" +
				" \""+server+"\"" +
				" \""+port+"\"" +
				" \""+serverdn+"\"" +
				" \""+voname+"\"" ;	
				try{
					appendLineToFavourites(lscString);						
					isSuccessful = true;
				} catch (IOException e) {
					throw new IOException("Failed to append line\n"+lscString+"\nto " + voname + ".lsc file.");
				}
			} catch (IOException e) {
				throw new IOException("Failed to create " + voname + ".lsc file.");				
			}
		}
		else{
			throw new IOException("VO '" + voname + "' with server '"+server+"' already exists.");	
		}

		return isSuccessful;

	}
	public static boolean addVOToFavourites(String voName, String fromGroup){
		boolean isAdded = false;
		//First check if VO is already in Favourites
		TreeMap allFavVOs = getVOGroup(FAVOURITES);
		if(allFavVOs.containsKey(voName)){
			JOptionPane.showMessageDialog(null, "VO '"+voName+"' is already in " + FAVOURITES + ".", "Error: Add VO to "+ FAVOURITES, JOptionPane.ERROR_MESSAGE);
		}
		else{
			TreeMap groupVOs = getVOGroup(fromGroup);
			if (groupVOs.containsKey(voName)){
				List voDetails = (List) groupVOs.get(voName);
				allFavVOs.put(voName, voDetails);
				String newContent = createStringFromTreeMap(allFavVOs);
				try {
					writeToFile(favouritesFile, newContent);
					isAdded = true;
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Cannot add VO '"+voName+"' to "+FAVOURITES+". Write to file '"+favouritesFile+"' failed", "Error: Add VO to "+FAVOURITES, JOptionPane.ERROR_MESSAGE);
				}
				
			}
		}
		return isAdded;
	}
	public static boolean removeVOFromFavourites(String voName){
		boolean isRemoved = false;
		TreeMap allFavVOs = getVOGroup(FAVOURITES);
		Iterator<Map.Entry<String,TreeMap>> entries = allFavVOs.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String,TreeMap> entry = entries.next();
			String vonameinMap = entry.getKey();
			if (voName.equals(vonameinMap)){
				//Check also if it is a self-VO, if yes, need to remove folders too
				TreeMap allEGIVOs = getVOGroup(EGI);
				TreeMap allOTHERSVO = getVOGroup(OTHERS);
				if (!allEGIVOs.containsKey(voName) && !allOTHERSVO.containsKey(voName)){
					try {
						delete(new File(vomsdir+File.separator+voName));
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Cannot remove dirctory '"+vomsdir+File.separator+voName+"'.", "Error: Remove VO from "+FAVOURITES, JOptionPane.ERROR_MESSAGE);
					}
				}
				allFavVOs.remove(vonameinMap);
				isRemoved = true;
				break;
			}
		}
		if (isRemoved){
			String newContent = createStringFromTreeMap(allFavVOs);
			try {
				writeToFile(favouritesFile, newContent);
			} catch (IOException e) {
				isRemoved = false;
				JOptionPane.showMessageDialog(null, "Failed to remove VO from Favourites. Cannot write to file '"+favouritesFile+"'", "Error: Remove VO", JOptionPane.ERROR_MESSAGE);
			}
		}

		return isRemoved;
	}
	
	public static boolean checkFavouritesChanges(){
		boolean isChanged = false;
		Map favourites = (TreeMap)VOHelper.getVOGroup(FAVOURITES);
		Map egis = (TreeMap)VOHelper.getVOGroup(EGI);
		Map others = (TreeMap)VOHelper.getVOGroup(OTHERS);
        Iterator<Map.Entry<String,List>> entries = favourites.entrySet().iterator();

        while (entries.hasNext()) {
            Entry<String, List> favourite = entries.next();
            String favVOName = favourite.getKey();
            List voDetails = favourite.getValue();
            for (int i=0; i<voDetails.size(); i++){
            	TreeMap favInfo = (TreeMap)voDetails.get(i);
            	
            	boolean isSame = false;
            	String group = "";
            	if(egis.containsKey(favVOName)){
            		List egiVODetails = (List) egis.get(favVOName);
            		isSame = compareTreeMapInList(favVOName, favInfo, egiVODetails);
            		group = EGI;            
            		
            	}
            	else if(others.containsKey(favVOName)){
            		List othersVODetails = (List) others.get(favVOName);
            		isSame = compareTreeMapInList(favVOName, favInfo, othersVODetails);
            		group = OTHERS;
            		
            	}
            	
            	if(!isSame && !group.equals("")){
        			int reply = JOptionPane.showConfirmDialog(null, "VO '"+favVOName+"' setup has been modified in " +group+
							" setup. Your "+FAVOURITES+" configuration might not work.\n Do you want to update Favourites with the updated configurations?", 
							"Update "+FAVOURITES+" configuration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(VOHelper.class, ConfigHelper.ICON));
				
        			if (reply== JOptionPane.OK_OPTION){
        				if (group.equals(EGI)){
        					favourites.put(favVOName, egis.get(favVOName));
        					isChanged = true;
        				}
        				else if (group.equals(OTHERS)){
        					favourites.put(favVOName, others.get(favVOName));
        					isChanged = true;
        				}
        				break;
        			}
        		}
            }
        }
        if (isChanged){
        	String newContent = createStringFromTreeMap((TreeMap)favourites);
			try {
				writeToFile(favouritesFile, newContent);
			} catch (IOException e) {
				isChanged = false;
				JOptionPane.showMessageDialog(null, "Failed to remove VO from Favourites. Cannot write to file '"+favouritesFile+"'", "Error: Remove VO", JOptionPane.ERROR_MESSAGE);
			}
        }
		return isChanged;
	}
	
	public static boolean compareTreeMapInList(String voName, TreeMap favInfo, List list){
		boolean isSame = false;
		String favServer = (String)favInfo.get("server");
    	String favPort = (String)favInfo.get("port");
    	String favDN = (String)favInfo.get("dn");
		for(int j=0; j<list.size(); j++){
			TreeMap info = (TreeMap)list.get(j);
			String egiServer = (String) info.get("server");
			if(favServer.equals(egiServer)){
				//Now check also the port and dn
				String egiPort = (String)info.get("port");
            	String egiDN = (String)info.get("dn");
            	if (egiPort.equals(favPort) && egiDN.equals(favDN)){
            		isSame = true;
            		break;
            	}
			}
		}
		return isSame;
		
	}
	
	public static TreeMap getVOGroup(String group){
		return (TreeMap) allVOs.get(group);
	}
	
	private static TreeMap readVomsesFile(File file){
		TreeMap vosInfo = new TreeMap<String, List>();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			
			String line;
			//int counter=0;
			while ((line = br.readLine()) != null) {	
				if (!line.trim().equals("")){
					String [] info = line.split("\" \"");
					TreeMap temp = null;
					
					String voname = "";
					for (int i=0; i<info.length; i++){
						if (i==0){
							temp = new TreeMap<String, String>();
							voname = info[i].substring(1);
						}
						else if (i==4){
							temp.put("servervoname", info[i].substring(0, info[i].length()-1));
							//Find if the same voname already exists
							if (vosInfo.containsKey(voname)){
								List multiValue = (List) vosInfo.get(voname);
								multiValue.add(temp);
								vosInfo.put(voname, multiValue);
							}
							else{
								List singleValue = new ArrayList();
								singleValue.add(temp);
								vosInfo.put(voname, singleValue);
							}
						}
						else{
							if(i==1){
								temp.put("server", info[i]);
							}
							else if(i==2){
								temp.put("port", info[i]);
							}
							else if(i==3){
								temp.put("dn", info[i]);
							}
						}
					}
					//counter++;
				}
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return vosInfo;
	}
	
   
    /*
     * To peform tar czf vomslocation/
     */
    private static void untar(InputStream in, String untarDir) throws IOException {
    	
    	
    	TarArchiveInputStream tin = new TarArchiveInputStream(in);
    	TarArchiveEntry entry = tin.getNextTarEntry();

    	//TarInputStream tin = new TarInputStream(in);
    	//TarEntry tarEntry = tin.getNextEntry();
    	if(new File(untarDir).exists()){
    		//To handle the normal files (not symbolic files)
    		while (entry != null){
    			if (!entry.isSymbolicLink()){
    				String filename = untarDir + File.separatorChar + entry.getName();
	    			File destPath = new File(filename); 
	    			
	    			if(!entry.isDirectory()){
	    				FileOutputStream fout = new FileOutputStream(destPath);
	    				IOUtils.copy(tin, fout);
	    				//tin.copyEntryContents(fout);
	    				fout.close();
	    			}else{	
	    				if (!destPath.exists()){
		    				boolean success = destPath.mkdir();
		    				if (!success) {
		 	                   throw new IOException("Couldn't create directory for VOMS support: "+destPath.getAbsolutePath());
		    				}
	    				}
	    			}
    			}
    			entry = tin.getNextTarEntry();
    			//tarEntry = tin.getNextEntry();
    		}
    		//tin.close();
    		
    		//To handle the symbolic link files
    		tin = new TarArchiveInputStream(in);
    		entry = tin.getNextTarEntry();
    		while (entry != null){

    			if (entry.isSymbolicLink()){
    				File srcPath = new File(untarDir + File.separatorChar + entry.getLinkName()); 
    				File destPath = new File(untarDir + File.separatorChar + entry.getName()); 
	    			copyFile(srcPath, destPath);
	    			
	    			
	    			//tarEntry = tin.getNextEntry();
    			}
    			entry = tin.getNextTarEntry();
    		}
    		tin.close();
    	}else{
    		throw new IOException("Couldn't find directory: "+untarDir);
    	}

    }
    private static void copyFile(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
    /**
     * Change permission of files and directories
     */
    private static void Chmod (boolean recursive, String permission, String directoryFile) throws IOException{
    	String osName = System.getProperty("os.name");
    	String command = "chmod";
    	if(recursive){
    		command += " -R";
    	}
    	
    	if (osName.startsWith("Mac OS")) {
    		Runtime.getRuntime().exec(command + " " + permission + " "+ directoryFile);
    	}
    	else if (osName.startsWith("Windows")) {
    		//Nothing can be done currently
    	}
    	else{
    		Runtime.getRuntime().exec(command + " " + permission + " "+ directoryFile);
    	}
    }
    
    public static boolean createVOMSLOCATIONDialog(){
    	boolean isSuccessful = false;
    	VOMSLocationPanel vomsPanel = new VOMSLocationPanel();
    	
    	int result = JOptionPane.showConfirmDialog(null,vomsPanel,"VOMS Location",JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(vomsPanel.getClass(), ConfigHelper.ICON));
		if (result == JOptionPane.OK_OPTION) {	
			if (vomsPanel.getVOMSLocation()!=null && !vomsPanel.getVOMSLocation().equals("") ){
				vomslocation = vomsPanel.getVOMSLocation();
				refreshVOMSConfig();
				PreferencesStore.put(SshTerminalPanel.PREF_VOMS_LOCATION, vomslocation);
			}
		}
			
		return isSuccessful;
	}
    
    public static boolean createAddVODialog(){
    	boolean isSuccessful = false;
    	NewVOPanel newVOPanel = new NewVOPanel();
    	
		while(true){
			int result = JOptionPane.showConfirmDialog(null,newVOPanel,"New VO",JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ResourceIcon(newVOPanel.getClass(), ConfigHelper.ICON));
			
	    	if (result == JOptionPane.OK_OPTION) {
	    		
				
				String msg = "";
				if (newVOPanel.getServerIssuerDN().equals("")){
					msg = "Field 'Server Certificate Issuer DN' cannot be empty.";
				}
				if (newVOPanel.getServerDN().equals("")){
					msg =  "Field 'Server DN' cannot be empty.\n" +  msg;		
				}
				if( newVOPanel.getPort()==0){
					msg =  "Field 'Port' cannot be empty.\n" +  msg;
				}
				if (newVOPanel.getServer().equals("")){
					msg =  "Field 'Server' cannot be empty.\n" +  msg;
				}
				if (newVOPanel.getVOName().equals("")){
					msg =  "Field 'VO name' cannot be empty.\n" +  msg;
				}				
				
				if(!msg.equals("")){
					JOptionPane.showMessageDialog(null, msg, "Error: Add new VO", JOptionPane.ERROR_MESSAGE);				
				}
				else{
					TreeMap vo = new TreeMap();
					vo.put("localvoname", newVOPanel.getVOName());
					vo.put("servervoname", newVOPanel.getVOName());
					vo.put("server", newVOPanel.getServer());
					vo.put("port", newVOPanel.getPort());
					vo.put("serverdn", newVOPanel.getServerDN());
					vo.put("serverissuerdn", newVOPanel.getServerIssuerDN());
					try {
						isSuccessful = addNewVO(vo);
						break;
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error: Add new VO", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
	    	else{	    		
	    		break;
	    	}
		}
				
		return isSuccessful;
	}
    
  
    
    private static String createStringFromTreeMap(TreeMap treemap){
    	String content = "";
    	Iterator<Map.Entry<String,TreeMap>> entries = treemap.entrySet().iterator();
        while (entries.hasNext()) {
        	Map.Entry<String,TreeMap> entry = entries.next();
            String vonameinMap = entry.getKey();
            List voDetails = (List) entry.getValue();
            for (int i=0; i<voDetails.size(); i++){
				TreeMap details = (TreeMap) voDetails.get(i);
				String server = (String) details.get("server");
				String port = (String) details.get("port");
				String dn = (String) details.get("dn");
				String servervoname = (String) details.get("servervoname");
				String lscString = "\""+vonameinMap+"\"" +
									" \""+server+"\"" +
									" \""+port+"\"" +
									" \""+dn+"\"" +
									" \""+servervoname+"\"" ;	
				content += lscString + "\n";
            }
        }
        return content;
    }
    
    private static void writeToFile(String filename, String content) throws IOException {
    	File newFile = new File(filename);
    	if (!newFile.isFile()){
    		newFile.createNewFile();
    	}
    	PrintWriter out = new PrintWriter(newFile.getAbsolutePath());
		out.println(content);
		out.close();		
    }
    
    private static void appendLineToFavourites(String line) throws IOException{
    	Writer output;
    	output = new BufferedWriter(new FileWriter(favouritesFile, true));
    	output.append(line+"\n");
    	output.close();
    }

    private static void delete(File file) throws IOException{
    	if(file.isDirectory()){
    		if(file.list().length==0){
    		   file.delete();
    		}else{
        	   String files[] = file.list(); 
        	   for (String temp : files) {
        	      File fileDelete = new File(file, temp);
        	      delete(fileDelete);
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	   }
    		}
 
    	}else{
    		//if file, then delete it
    		file.delete();
    	}
    }
	public String getVomses() {
		return vomses;
	}

	public String getVomsdir() {
		return vomsdir;
	}

	public void setVomses(String vomses) {
		this.vomses = vomses;
	}

	public void setVomsdir(String vomsdir) {
		this.vomsdir = vomsdir;
	}

	public static String[] getListeInfra() {
		return listeInfra;
	}

	public static void setListeInfra(String[] listeInfra) {
		VOHelper.listeInfra = listeInfra;
	}

	public static TreeMap getAllVOs() {
		return allVOs;
	}

	public static void setAllVOs(TreeMap allVOs) {
		VOHelper.allVOs = allVOs;
	}

	public static String getVomslocation() {
		return vomslocation;
	}
	
	

}

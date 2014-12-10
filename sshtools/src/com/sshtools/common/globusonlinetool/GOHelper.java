/*
 *  SSHTools - Globus Online Tool
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
package com.sshtools.common.globusonlinetool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;

import org.globusonline.transfer.APIError;
import org.globusonline.transfer.BaseTransferAPIClient;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.XTextField;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.sshterm.SshTerminalPanel;

public class GOHelper {
	public static JSONTransferAPIClient client;
	private static int defaultLifetime = 168;
	public static int IN_PROGRESS = 2;
	public static int SUCCESS = 1;
	public static int FAIL = 0;
	
	private static int timeout = 15;
	public static JSONObject tasks = new JSONObject();
	private static File goLogFile;
	private static String myGOTaskSimpleDateFormat = "dd/MM/yyyy h:mm aaa";
	private static boolean isGOTaskTableToBeUpdated = false;	
	private static String goUserName = "";
	private static boolean authenticated = false;
	private static HashMap defaultDirectories = new HashMap();
	
	static {
	    String val = ConfigurationLoader.checkAndGetProperty("user.home", null);

	    if (val != null) {
	    	goLogFile = new File(val + File.separator + ".sshterm" + File.separator + "gotaskslog.json");
	    }
	}
	
	public static boolean init(){

		try {			
			client = new JSONTransferAPIClient(goUserName,null, CredentialHelper.getExistingProxyLoation(), CredentialHelper.getExistingProxyLoation(), null);
			return true;
		} catch (KeyManagementException e) {
			JOptionPane.showMessageDialog(null,"Cannot connect to GO with your credential.\nPlease check that you have add your X.509 certificate to your GO account and your account is correct.\nMore information:\nhttps://support.globusonline.org/entries/20999758-How-do-I-add-an-X-509-certificate-to-my-Globus-Online-account-","Globus Online Tool-  Error", JOptionPane.ERROR_MESSAGE); 
			
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(null,"Cannot connect to GO with your credential. The algorithm used is not supported.\nDetail Error:\n" + e.getMessage(), "Globus Online Tool-  Error",JOptionPane.ERROR_MESSAGE); 
			
		}
		return false;
	}



	public static boolean autoActivate(String endpointName) throws Exception{		 
		try {
			init();
			String resource= BaseTransferAPIClient.endpointPath(endpointName)+ "/autoactivate";
		
			JSONTransferAPIClient.Result r = client.postResult(resource, null,null);
			String code = r.document.getString("code");
			
			if (code.startsWith("AutoActivationFailed")) {
				Map myProxyInfo = null;
				boolean usedMemorySetting = false;
				int counter = 0;
				while (counter<2){
					counter ++;
					//System.out.println("counter: " + counter + " usedMemorySetting: " + usedMemorySetting);
					if (CredentialHelper.getMyProxyInfo()!=null && !usedMemorySetting){
						myProxyInfo = CredentialHelper.getMyProxyInfo();
						usedMemorySetting = true;
					}
					//Get myProxy information from user
					else {
						myProxyInfo = createRetrieveProxyDialog();
					}
					
					JSONObject proxy = new JSONObject();
					proxy.put("DATA_TYPE", "activation_requirements");
					proxy.put("length", myProxyInfo.get("lifetime"));
		
					
					if (myProxyInfo!=null){
						JSONArray reqsArray = r.document.getJSONArray("DATA");
						for (int i=0; i < reqsArray.length(); i++) {
							resource = BaseTransferAPIClient.endpointPath(endpointName) + "/activation_requirements";
							JSONObject reqObject = reqsArray.getJSONObject(i);
							if (reqObject.getString("ui_name").equals("Username")) {
								reqsArray.getJSONObject(i).remove("value");						
								reqsArray.getJSONObject(i).put("value", myProxyInfo.get("username"));                    	
							}
							if (reqObject.getString("ui_name").equals("Passphrase")) {
								reqsArray.getJSONObject(i).remove("value");
								reqsArray.getJSONObject(i).put("value",  myProxyInfo.get("passphrase")); 
							}
							if (reqObject.getString("ui_name").equals("MyProxy Server")) {
								reqsArray.getJSONObject(i).remove("value");
								reqsArray.getJSONObject(i).put("value",  myProxyInfo.get("server")); 
							}
							/*if(reqObject.getString("ui_name").equals("Credential Lifetime (hours)")) {
								reqsArray.getJSONObject(i).remove("value");
								reqsArray.getJSONObject(i).put("value",  myProxyInfo.get("lifetime")); 
							}*/
						}
						proxy.put("DATA",reqsArray);
						resource = BaseTransferAPIClient.endpointPath(endpointName) + "/activate";
			
						r = client.postResult(resource, proxy);
						code = r.document.getString("code");
			
						if (code.startsWith("AutoActivationFailed")) {
							if (counter ==2){
								return false;
							}
							else if (counter==1 && !usedMemorySetting){
								return false;
							}
							
						}
						else{
							break;
						}
					}
					else{
						if (counter ==2){
							return false;
						}
						else if (counter==1 && !usedMemorySetting){
							return false;
						}
					}
					
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e.getMessage());
		}
		return true;
	}

	public static List<Map> displayLs(String endpointName, String path){
		List <Map> allDir = new ArrayList<Map>();
		try {
			init();  	
			Map<String, String> params = new HashMap<String, String>();
			if (path != null) {
				params.put("path", path);
			}
			String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/ls";
			
			JSONTransferAPIClient.Result r = client.getResult(resource, params);
			JSONArray fileArray = r.document.getJSONArray("DATA");
			for (int i=0; i < fileArray.length(); i++) {
				Map temp = new HashMap<String, String>();
				JSONObject fileObject = fileArray.getJSONObject(i);
				//Name of file or Folder
				temp.put("name", fileObject.getString("name"));
	
				Iterator keysIter = fileObject.sortedKeys();
				while (keysIter.hasNext()) {
					String key = (String)keysIter.next();
					if (!key.equals("DATA_TYPE") && !key.equals("LINKS")
							&& !key.endsWith("_link") && !key.equals("name")) {
						temp.put(key, fileObject.getString(key));                   
					}
	
				}
				allDir.add(temp);        
			}
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(null, "Cannot list the directory.\n1) You might not have the required permission to access the directory or \n2) The directory does not exist. \nDetail Message:\n" + e.toString(),"Globus Online Tool-  Error", JOptionPane.ERROR_MESSAGE); 

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Cannot list the directory.\n1) You might not have the required permission to access the directory or \n2) The directory does not exist. \n3) Your proxy is not voms enabled.\nDetail Message:\n" + e.toString(),"Globus Online Tool-  Error", JOptionPane.ERROR_MESSAGE); 
	
		}
		return allDir;
	}

	public static int transferFiles(String sourceEndpoint, String destEndpoint, String source, String dest, boolean recursiveFlag ){

		init();
		JSONTransferAPIClient.Result r;
		String status = "";
		try {
			r = client.getResult("/transfer/submission_id");
	        String submissionId = r.document.getString("value");
	        JSONObject transfer = new JSONObject();
	        transfer.put("DATA_TYPE", "transfer");
	        transfer.put("submission_id", submissionId);
	        JSONObject item = new JSONObject();
	        item.put("DATA_TYPE", "transfer_item");
	        item.put("source_endpoint", sourceEndpoint);
	        item.put("source_path", source);
	        item.put("destination_endpoint", destEndpoint);
	        item.put("destination_path", dest);
	        item.put("recursive", recursiveFlag);
	        transfer.append("DATA", item);

	        r = client.postResult("/transfer", transfer, null);
	        String taskId = r.document.getString("task_id");
	        
	        //Logging
	        JSONObject cTask = new JSONObject();
	        cTask.put("taskid", taskId);	
	        String msg = "TRANSFER ";
	        if(recursiveFlag){
	        	msg += "recursively ";
	        }
	        msg += "from " + sourceEndpoint + ":" + source + " to " + destEndpoint + ":" + dest;
	        cTask.put("description", msg );
	        cTask.put("creation", formatSimpleDateTime(new Date()));
	        cTask.put("lastcheck", formatSimpleDateTime(new Date()));
	        status = waitAndReturnTaskStatus(taskId, timeout);
	        cTask.put("status",status);
	        tasks.append("TASK", cTask);     
	        
	        isGOTaskTableToBeUpdated = true;
	        
	        //System.out.println(tasks.toString());
		} catch (Exception e) {
			return FAIL;
		}    	
		
		//progressBar.setVisible(false);
		if (status.equals("ACTIVE")){
            return IN_PROGRESS;
        }
        else if (status.equals("SUCCEEDED")){
        	return SUCCESS;
        }
        else 
        	return FAIL;
	}
	
	
	public static int deleteFiles(String endpoint, String fileDir){
	
			init();
			JSONTransferAPIClient.Result r;
			String status = "";
			try {
				r = client.getResult("/transfer/submission_id");
			
		        String submissionId = r.document.getString("value");
		        JSONObject delete = new JSONObject();
		        delete.put("DATA_TYPE", "delete");
		        delete.put("submission_id", submissionId);
		        delete.put("endpoint", endpoint);
		        delete.put("recursive", true);
		        JSONObject item = new JSONObject();
		        item.put("DATA_TYPE", "delete_item");
		        item.put("path", fileDir);        
		        delete.append("DATA", item);
	
		        r = client.postResult("/delete", delete, null);
		        String taskId = r.document.getString("task_id");
		        
		        //Logging
		        JSONObject cTask = new JSONObject();
		        cTask.put("taskid", taskId);	
		        cTask.put("description", "DELETE " + endpoint + ":" + fileDir);
		        cTask.put("creation", formatSimpleDateTime(new Date()));
		        cTask.put("lastcheck", formatSimpleDateTime(new Date()));
		        status = waitAndReturnTaskStatus(taskId, timeout);
		        cTask.put("status",status);
		        tasks.append("TASK", cTask);
		        
		        isGOTaskTableToBeUpdated = true;
		        
	        	//System.out.println(tasks.toString());
			} catch (Exception e) {
				e.printStackTrace();
				return FAIL;
			} 
			if (status.equals("ACTIVE")){
	            return IN_PROGRESS;
	        }
	        else if (status.equals("SUCCEEDED")){
	        	return SUCCESS;
	        }
	        else 
	        	return FAIL;
	        
	}
	
	public static String waitAndReturnTaskStatus(String taskId, int timeout)
	throws IOException, JSONException, GeneralSecurityException, APIError {
		JSONTransferAPIClient.Result r;
		String status = "ACTIVE";
        String resource = "/task/" +  taskId;
        Map<String, String> params = new HashMap<String, String>();
        params.put("fields", "status");
        while (timeout > 0 && status.equals("ACTIVE")) {
            r = client.getResult(resource, params);
            status = r.document.getString("status");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return status;
            }
            timeout -= 5;
         
        }
        //System.out.println(status);
        return status;
        
	}
	public static boolean waitForTask(String taskId, int timeout)
    throws IOException, JSONException, GeneralSecurityException, APIError {
        String status = "ACTIVE";
        JSONTransferAPIClient.Result r;

        String resource = "/task/" +  taskId;
        Map<String, String> params = new HashMap<String, String>();
        params.put("fields", "status");

        while (timeout > 0 && status.equals("ACTIVE")) {
            r = client.getResult(resource, params);
            status = r.document.getString("status");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return false;
            }
            timeout -= 5;
        }

        if (status.equals("ACTIVE"))
            return false;
        return true;
    }
	
	public static String getTaskStatus(String taskId)
    throws IOException, JSONException, GeneralSecurityException, APIError {
        String status = "";
        JSONTransferAPIClient.Result r;

        String resource = "/task/" +  taskId;
        Map<String, String> params = new HashMap<String, String>();
        params.put("fields", "status");
        r = client.getResult(resource, params);
        status = r.document.getString("status");
        return status;
    }

	public static List<String> getAllEndpoints() throws Exception {
		List<String> allEndpts = new ArrayList<String>();
		int counter = 0;
		int offset = 0;
		try{
			while(true){
				init();  
				offset = counter*1000;
				JSONTransferAPIClient.Result r = client.getResult("/endpoint_list?offset="+offset+"&limit=1000&fields=canonical_name,default_directory");
	
				Iterator keysIter = r.document.sortedKeys();
				
				while (keysIter.hasNext()) {
					String key = (String)keysIter.next();
					if (key.equals("DATA")){
						JSONArray jArray = new JSONArray(r.document.getString(key));
	
						for (int i=0; i<jArray.length(); i++){
							JSONObject jObj = new JSONObject(jArray.get(i).toString());
							allEndpts.add(jObj.getString("canonical_name"));
							defaultDirectories.put(jObj.getString("canonical_name"), jObj.getString("default_directory"));
						}
					}
				}
				counter ++;
				if (allEndpts.size()<counter*1000){
					break;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		} catch (APIError e1) {
			e1.printStackTrace();
		}
		return allEndpts;
	}

	public static List<String> filterList(List myList, String filteredText){

		List tempList = new ArrayList();
		for (int i=0; i<myList.size(); i++){
			if(myList.get(i)!=null){
				if (myList.get(i).toString().toLowerCase().contains(filteredText.toLowerCase())){
					tempList.add(myList.get(i).toString());
				}
			}
		}

		return tempList;
	}
	public static boolean mkEndpointDir (String endpointName, String directoryName) {		
		init();
		try {
			String resource = BaseTransferAPIClient.endpointPath(endpointName);
			JSONObject o = new JSONObject();
	        o.put("DATA_TYPE", "mkdir");
	        o.put("path", directoryName);
	        JSONTransferAPIClient.Result r = client.postResult(resource +"/mkdir", o, null);
	 	    JOptionPane.showMessageDialog(null, r.document.getString("message")+ "\n" + endpointName+":"+directoryName, "GO Error: Create New Directory", JOptionPane.INFORMATION_MESSAGE);
	        /*String taskId = r.document.getString("task_id");
	        if (!waitForTask(taskId, 120)) {
	            System.out.println(
	                "Transfer not complete after 2 minutes, exiting");
	            return false;
	        }*/
	    
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(null, "Cannot create new directory. Directory might already exist.\nDetail Error:\n" +e.getMessage() , "Globus Online Tool- Error", JOptionPane.ERROR_MESSAGE);
			return false;			
		} catch (JSONException e) {
			JOptionPane.showMessageDialog(null, "Cannot create new directory. Directory might already exist.\nDetail Error:\n" +e.getMessage() , "Globus Online Tool- Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null, "Cannot create new directory. Directory might already exist.\nDetail Error:\n" +e.getMessage() , "Globus Online Tool- Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Cannot create new directory. Directory might already exist.\nDetail Error:\n" +e.getMessage() , "Globus Online Tool- Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (GeneralSecurityException e) {
			JOptionPane.showMessageDialog(null, "Cannot create new directory. Directory might already exist.\nDetail Error:\n" +e.getMessage() , "Globus Online Tool- Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (APIError e) { 
			JOptionPane.showMessageDialog(null, "Cannot create new directory. Directory might already exist.\nDetail Error:\n" +e.getMessage() , "Globus Online Tool- Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	   
	    return true;
    	
    }
	public static String createMkDirDialog(){

		String directory = "";
	
		try{
			init();  
			JLabel directoryLabel = new JLabel("Name of new directory:");			    
			XTextField directoryTextBox = new XTextField("", 20);
			directoryTextBox.hasFocus();
			Object[] obj = {directoryLabel, directoryTextBox};
			int result = JOptionPane.showConfirmDialog(null, obj, "Create new directory", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (result == JOptionPane.OK_OPTION) {	
				directory = directoryTextBox.getText().trim();
				if (directory.equals("")){
					JOptionPane.showMessageDialog(null, "Cannot create directory without an empty name", "Error: Create New Directory", JOptionPane.ERROR_MESSAGE);
				}
			}
	
		} catch (Exception e){
			e.printStackTrace();
		}
			
		return directory;
	}
	
	private static Map createRetrieveProxyDialog (){
		int counter = 0;
		Map myProxyInfo = null;
		while(counter<3){
			try{
				//Server			    
			    JLabel myProxyServerLabel = new JLabel("Server:");			    
			    String recordedMyproxyHostname = PreferencesStore.get(SshTerminalPanel.PREF_DEFAULT_MYPROXY_HOSTNAME, "").trim().toLowerCase();    
			    String [] myProxyHostNames = getMyProxyHostNames(recordedMyproxyHostname);
			    JComboBox myproxyServer = new JComboBox(myProxyHostNames);
			    myproxyServer.setEditable(true);
			    
			    
			   /* NumericTextField port;
			    NumericTextField lifetime;
			    if (extraParam){
				    //Port
				    int recordedMyproxyPort = 0;
				    try{
				    	recordedMyproxyPort = Integer.getInteger(PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_PORT, ""));
				    }catch(Exception e){    	
				    }
				    if (recordedMyproxyPort==0){
				    	recordedMyproxyPort = 7512;
				    }    
				    JLabel myproxyServerPortLabel = new JLabel("Port");
				    NumericTextField port = new NumericTextField(new Integer(1), new Integer(50000), recordedMyproxyPort);
				    
				    //Lifetime
				    JLabel myproxyServerLifetimeLabel = new JLabel("Lifetime (hours)");
				    NumericTextField lifetime = new NumericTextField(new Integer(0), new Integer(8760),
				            new Integer(defaultLifetime));
			    }*/
			    
			    //Username
			    JLabel usernameLabel = new JLabel("Username:");		 
				XTextField username;
				String usernameStr = PreferencesStore.get(SshTerminalPanel.PREF_MYPROXY_UNAME, "");    	
		    	
			    if(usernameStr==null || usernameStr.equals("")){
			    	usernameStr = System.getProperty("user.name");
			    }
			    
			    if (usernameStr!=null){
			    	username = new XTextField(usernameStr, 20);
			    }
			    else{
			    	username = new XTextField(20);
			    }
			    
			    //Passphrase
			    JLabel myProxyPassphraseLabel = new JLabel("Passphrase:");			    
				JPasswordField passphrase =  new JPasswordField(20);
				passphrase.hasFocus();
				Object[] obj =  {myProxyServerLabel, myproxyServer, usernameLabel, username, myProxyPassphraseLabel, passphrase};


				int result = JOptionPane.showConfirmDialog(null, obj, "Retrieve credential to activate this endpoint", JOptionPane.OK_CANCEL_OPTION);
				
				if (result == JOptionPane.OK_OPTION) {
					
					String cServer = myproxyServer.getSelectedItem().toString().trim();
					//int cPort = (Integer) port.getValue();
					//int cLifetime = (Integer) lifetime.getValue();
					String cUsername = username.getText().trim();
					String cPassphrase = new String(passphrase.getPassword()).trim();
				
					if(!cServer.equals("") && !cUsername.equals("") && !cPassphrase.equals("")){
						myProxyInfo = new HashMap();
						myProxyInfo.put("server", cServer);
						myProxyInfo.put("username", cUsername);
						myProxyInfo.put("passphrase", cPassphrase);
						break;
					}
					else{
						JOptionPane.showMessageDialog(null, "All fields must not be empty.","Globus Online Tool- MyProxy Retrieve Warning", JOptionPane.ERROR_MESSAGE);
					}
				}
				else{				 
					break;
				}

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Fail to get myProxy information:\n" + e.getMessage(),
						"Globus Online Tool- MyProxy Retrieve Error", JOptionPane.ERROR_MESSAGE); 
			}
			counter++;
		}
		return myProxyInfo;
	}
	public static String[] getMyProxyHostNames(String recordedHostName){
		  LinkedHashSet<String> myProxyHostNames = new LinkedHashSet<String>();
		  myProxyHostNames.add("myproxy.lrz.de");
		  myProxyHostNames.add("px.grid.sara.nl");
		  myProxyHostNames.add("myproxy.egi.eu");
		  if (recordedHostName!=null && !recordedHostName.equals(""))
			  myProxyHostNames.add(recordedHostName);
		  
		  String [] hostnames = new String [myProxyHostNames.size()];
		  hostnames = myProxyHostNames.toArray(hostnames);
		  return hostnames;
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static String formatDateTime(String dateTime){
		try {
			dateTime = dateTime.substring(0, 22) + dateTime.substring(23, 25);
			String pattern = "yyyy-MM-dd HH:mm:ssZ";
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			Date date = sdf.parse(dateTime);
			String newPattern = "dd MMM yyyy h:mm aaa zzz";
			SimpleDateFormat myFormat = new SimpleDateFormat(newPattern);
			return myFormat.format(date);
			
		} catch (ParseException e) {
		}
		
		return dateTime;
	}
	
	public static String formatSimpleDateTime(Date date){	
		SimpleDateFormat myFormat = new SimpleDateFormat(myGOTaskSimpleDateFormat);
		return myFormat.format(date);
					
	}
	
	public static Object [][] getGOLogRecords(){
		Object [][] data = null;
		try {
			String content = readFileContent(goLogFile);
			if (content.equals("")){
				return null;
			}
			else{
				tasks = new JSONObject(content);
				//Check and update status
				if(tasks!=null && tasks.length()!=0){
					updateTasksStatus();
			
					JSONArray tasksArray = tasks.getJSONArray("TASK");
					if(tasksArray!=null && tasksArray.length()>0){
						data = getTasksObject();
					}
				}
			}
			
		} catch (JSONException e) {
			//No task
			e.printStackTrace();
		}
		
		return data;
	}
	
	public static Object[][] getTasksObject(){

		Object [][] data = null;
		try{
			//Check and update status
			updateTasksStatus();
	
			JSONArray tasksArray = tasks.getJSONArray("TASK");
			if(tasksArray!=null && tasksArray.length()!=0){
				data = new Object[tasksArray.length()][5];
				for (int i=0; i<tasksArray.length(); i++){
					Object [] temp = new Object[5];
					JSONObject stask = (JSONObject) tasksArray.get(i);
					temp[0] = stask.get("taskid");
					temp[1] = stask.get("description");
					temp[2] = stask.get("creation");
					temp[3] = stask.get("lastcheck");
					temp[4] = stask.get("status");
					data[tasksArray.length()-1-i] = temp;
				}
			}
		} catch (JSONException e) {
			//No task
			e.printStackTrace();
		}
		
		return data;
	}
	public static String readFileContent(File file) {
		
		if (!(file.exists())) {
			return "";			
		}
		StringBuilder contents = new StringBuilder();

		try {
			BufferedReader input =  new BufferedReader(new FileReader(file));
			try {
				String line = null; 
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		return contents.toString();	
		
	}
	
	public static boolean writeGOLogFile(){
		PrintWriter out;
		try {
			out = new PrintWriter(goLogFile.getAbsolutePath());
			out.println(tasks.toString());
			out.close();
			return true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	public static String retrieveEndPointDefaultDirectory(String endpoint) {
		String defaultDir = (String) defaultDirectories.get(endpoint);
		if (defaultDir.equals("null") || defaultDir.trim().equals("")){
			defaultDir = "~";
		}
		return defaultDir;

	}
	public static boolean updateTasksStatus(){
		boolean isUpdated = false;
		try {
			if (tasks!=null && tasks.length()!=0){
				JSONArray taskArray = tasks.getJSONArray("TASK");
				if (taskArray!=null){
					ArrayList<Integer> toBeRemoved = new ArrayList();
					for (int i=0; i<taskArray.length(); i++){
						JSONObject ctask = taskArray.getJSONObject(i);
						if(ctask.get("status").equals("ACTIVE")){
							String updatedStatus = getTaskStatus(ctask.getString("taskid"));
							if(!updatedStatus.equals("ACTIVE")){
								ctask.put("status", updatedStatus);
								isUpdated = true;
							}
						}
						else{
							String lastCheck = ctask.getString("lastcheck");
							SimpleDateFormat sdf = new SimpleDateFormat(myGOTaskSimpleDateFormat);
							Date lastCheckDate = sdf.parse(lastCheck);
							Date todayDate = new Date();
							//More than 7 days, can remove records
							if (((todayDate.getTime()-lastCheckDate.getTime())/(1000*60*60*24))>7){
								toBeRemoved.add(0, i);
							}
						}
					}
				
				
					//Remove longer than 7 days records
					for (Iterator<Integer> iter = toBeRemoved.iterator(); iter.hasNext(); ) {
						taskArray.remove(iter.next());
						isUpdated = true;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isUpdated;
		
	}

	public static boolean credentialChecker() {
		if (!init())
			return false;
		
        try {
			JSONTransferAPIClient.Result r = client.getResult("/tasksummary");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			return false;
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (APIError e) {
			e.printStackTrace();
			return false;
		}
		authenticated = true;
		return true;
        
    }


	public static boolean isGOTaskTableToBeUpdated() {
		return isGOTaskTableToBeUpdated;
	}



	public static void setGOTaskTableToBeUpdated(boolean changedValue) {
		isGOTaskTableToBeUpdated = changedValue;
	}



	public static String getGoUserName() {
		return goUserName;
	}



	public static void setGoUserName(String goUserName) {
		GOHelper.goUserName = goUserName.trim();
	}



	public static boolean isAuthenticated() {
		return authenticated;
	}
}

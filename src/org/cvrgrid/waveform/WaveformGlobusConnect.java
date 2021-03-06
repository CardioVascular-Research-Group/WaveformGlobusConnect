/* Copyright 2013, 2014 Cardiovascular Research Grid
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 *	All rights reserved
 * 	
 * 	@author Stephen J Granite (Email: sgranite@jhu.edu)
 * 	@author Dina Sulakhe (Email: sulakhe@mcs.anl.gov)
 * 	@author Christian Jurado (Email: cjurado2@jhu.edu)
 * 	@author Josh Bryan (Email: josh.bryan@gmail.com)
 */

package org.cvrgrid.waveform;
import java.io.File;
import java.io.IOException;

//import org.apache.commons.io;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cvrgrid.waveform.backing.GlobusUploadBacking;
import org.cvrgrid.waveform.model.GlobusConnectConfiguration;
import org.cvrgrid.waveform.model.GlobusEndpoint;
import org.cvrgrid.waveform.model.GlobusServer;
import org.globusonline.nexus.GoauthClient;
import org.globusonline.nexus.exception.NexusClientException;
import org.globusonline.transfer.APIError;
import org.globusonline.transfer.Authenticator;
import org.globusonline.transfer.Example;
import org.globusonline.transfer.GoauthAuthenticator;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rsna.ctp.pipeline.Status;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * This is the main class to transfer local Waveform files using Globus Connect.
 * It is a variation of the CTP-GlobusExportService written by Dina Sulakhe, coupling it with
 * the Java Nexus Client written by Chris Jurado and extended by Josh Bryan.
 * Those software can be found here: https://github.com/sulakhe/CTP-GlobusExportService 
 * and https://github.com/globusonline/java-nexus-client.
 * You should read the details on the CTP-GlobusExportService site to understand the /resources/server.properties.
 * This command line tool looks for 7 parameters,
 * that can have defaults in the /resources/server.properties file.
 * The purpose for allowing entry of these parameters is to make the tool flexible in use, 
 * while not storing these parameters in the example /resources/server.properties.
 * 
 * @param sourceRoot - root folder on the source file system where the files are located
 * @param destinationRoot - root folder on the destination file system where the files will be placed
 * @param globusOnlineUsername - username of the Globus Online account that will be used
 * @param globusOnlinePassword - password of the Globus Online account that will be used
 * @param destinationUsername - username of the destination file system account that will be used
 * @param destinationPassword - password of the destination file system account that will be used
 * @param destinationMyProxy - myProxy server of the destination file system account that will be used
 * 
 * Once the code has those parameters, the code will check to see if the Globus Endpoints have been 
 * activated.  While the destination endpoint is likely to be a Globus Connect Server that can be 
 * activated remotely, the source is likely to be a Globus Connect Personal.  The Globus Connect Personal 
 * must be running in order for this code to work.  After both checks have passed, the code will 
 * transfer the data from the source to the destination, echoing the results in the terminal window.
 * 
 * The tool requires the bcprov_ext_jdk16_146, log4j, TransferAPIClient and java-nexus-client libraries and 
 * the org.rsna.ctp.pipeline.Status class (included) to function properly.

 */

public class WaveformGlobusConnect {


	static final Logger logger = Logger.getLogger(WaveformGlobusConnect.class);
	private String configFilename = "/resources/server.properties";
	private GlobusConnectConfiguration globusConnectConfiguration = new GlobusConnectConfiguration();
	private boolean onlineGC, onlineGCMU;

	/**
	 * Constructor for this code intended to set all the variables based upon the properties file.
	 */
	public WaveformGlobusConnect(){		 

		try {
			logger.setLevel(Level.INFO);
			GlobusConnectConfiguration globusConnectConfiguration = new GlobusConnectConfiguration();
			Properties serverProperties = new Properties();
			InputStream stream = WaveformGlobusConnect.class.getResourceAsStream(this.getConfigFilename());
			serverProperties.load(stream);
			globusConnectConfiguration.setGlobusOnlineUsername(serverProperties.getProperty("globusOnlineUsername"));
			globusConnectConfiguration.setGlobusOnlinePassword(serverProperties.getProperty("globusOnlinePassword"));
			globusConnectConfiguration.setSourceUsername(serverProperties.getProperty("sourceUsername"));
			globusConnectConfiguration.setSourcePassword(serverProperties.getProperty("sourcePassword"));
			globusConnectConfiguration.setSourceEP(serverProperties.getProperty("sourceEP"));
			globusConnectConfiguration.setSourceRoot(serverProperties.getProperty("sourceRoot"));
			globusConnectConfiguration.setDestinationEP(serverProperties.getProperty("destinationEP"));
			globusConnectConfiguration.setDestinationRoot(serverProperties.getProperty("destinationRoot"));
			globusConnectConfiguration.setDestinationUsername(serverProperties.getProperty("destinationUsername"));
			globusConnectConfiguration.setDestinationPassword(serverProperties.getProperty("destinationPassword"));			
			globusConnectConfiguration.setDestinationMyProxy(serverProperties.getProperty("destinationMyProxy"));			
			globusConnectConfiguration.setWindowsGCVersion("");

			this.setGlobusConnectConfiguration(globusConnectConfiguration);

		} catch (IOException e) {

			e.printStackTrace();

		}

	}


	/**
	 * An alternative constructor for this code intended to set all the variables 
	 * based upon the properties file.
	 */
	public WaveformGlobusConnect(GlobusConnectConfiguration globusConnectConfiguration){

		this.setGlobusConnectConfiguration(globusConnectConfiguration);

	}

	/**
	 * Main class meant to invoke the code from the command line.
	 * The 7 input parameters should be: 
	 * 	SourceRoot - Absolute directory of the Globus Connect Personal on this machine. e.g. "/home/WIN/mshipwa1/Downloads/samples/ECGtestData"
	 * 	DestinationRoot - (Relative or Absolute) directory on the Globus Server that the files will be sent to. e.g. "~/wftest/" or "/home/WIN/cvrgglobusproc/wftest/"
	 * 	GlobusOnlineUsername - Globus usename as you'd used to log into the Globus Tranfer web page. e.g. "wftest" 
	 * 	GlobusOnlinePassword - Globus password as you'd used to log into the Globus Tranfer web page.
	 * 	DestinationUsername  - Username on the Globus Server that the files will be sent to. e.g. "cvrgglobusproc" 
	 * 	DestinationPassword - Password on the Globus Server that the files will be sent to. 
	 * 	DestinationMyProxy - The destination proxy used to activate the destination endpoint. e.g. "gcmu-02.cvrgrid.org"
	 */
	public static void main(String[] args) {
				
		long tTotalStart = System.nanoTime();
		
		WaveformGlobusConnect waveformGlobusConnect = new WaveformGlobusConnect();
		GlobusConnectConfiguration globusConnectConfiguration = waveformGlobusConnect.getGlobusConnectConfiguration();
		try{ 
			globusConnectConfiguration.setSourceRoot(args[0]); 				// "/home/WIN/mshipwa1/Downloads/samples/ECGtestData/ECG 2 leads/" or "/media/Hitachi/physionet.org/physiobank/database/mimic2wdb/30/3000003"
			globusConnectConfiguration.setDestinationRoot(args[1]); 		// "/home/WIN/cvrgglobusproc/wftest/" 
			globusConnectConfiguration.setGlobusOnlineUsername(args[2]);	// "wftest" 
			globusConnectConfiguration.setGlobusOnlinePassword(args[3]);	// "MaryHad1Lamb" 
			globusConnectConfiguration.setDestinationUsername(args[4]);		// "cvrgglobusproc" 
			globusConnectConfiguration.setDestinationPassword(args[5]);		// "#Cft6Vgy7!"
			globusConnectConfiguration.setDestinationMyProxy(args[6]);		// "gcmu-02.cvrgrid.org"
		}catch(Exception arrayex){
			arrayex.printStackTrace();
			System.out.println("input parameters should be: SourceRoot DestinationRoot GlobusOnlineUsername GlobusOnlinePassword DestinationUsername DestinationPassword DestinationMyProxy");
			System.exit(0);
		}
		String[] headerFileExtensions = {"hea"}; // list of extensions which the record header files might have (must be "hea" for WFDB)
		List<String> headerPaths = waveformGlobusConnect.transferFolder(globusConnectConfiguration, headerFileExtensions);
		
		
		GoauthClient cli = new GoauthClient("nexus.api.globusonline.org", "https://www.globusonline.org", args[2], args[3]);
		cli.setIgnoreCertErrors(true);
		String accessToken = null;
		try {

			JSONObject accessTokenJSON = cli.getClientOnlyAccessToken();
			accessToken = accessTokenJSON.getString("access_token");
			cli.validateAccessToken(accessToken);

		} catch (NexusClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Authenticator authenticator = new GoauthAuthenticator(accessToken);
		try {
			JSONTransferAPIClient client = new JSONTransferAPIClient(args[2], null, null);
			client.setAuthenticator(authenticator);
			globusConnectConfiguration.setClient(client);
		} catch (Exception e) {
			System.err.println("Got an exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());

			e.printStackTrace();
		}
		waveformGlobusConnect.setGlobusConnectConfiguration(globusConnectConfiguration);
		GlobusUploadBacking globusUploadBacking = new GlobusUploadBacking(globusConnectConfiguration);
		if (waveformGlobusConnect.isOnlineGC(globusConnectConfiguration, globusUploadBacking)) {
			if(!waveformGlobusConnect.isOnlineGCMU(globusConnectConfiguration, globusUploadBacking)) {
				System.out.println(globusConnectConfiguration.getDestinationEP() + " needs to be activated for data to transfer from " + globusConnectConfiguration.getSourceEP());
				waveformGlobusConnect.activateEP(globusConnectConfiguration, globusConnectConfiguration.getDestinationUsername(), globusConnectConfiguration.getDestinationPassword(), globusConnectConfiguration.getDestinationEP());
			}
			System.out.println("Transferring data from " + globusConnectConfiguration.getSourceEP() + " to " + globusConnectConfiguration.getDestinationEP());
			System.out.println(waveformGlobusConnect.export(globusConnectConfiguration));
		} else {
			System.out.println(globusConnectConfiguration.getSourceEP() + " needs to be connected for data to transfer to " + globusConnectConfiguration.getDestinationEP());	
		}

		//********************* Galaxy test section **********************
		long tGalaxyStart = System.nanoTime();
		final String instanceUrl = "http://ec2-54-211-1-192.compute-1.amazonaws.com:8081/";
		final String apiKey = "ed6d88d896b8d770254c6ab8e6abe201";
//	    String globusEndpoint  = globusConnectConfiguration.getDestinationEP(); //  "cvrgglobusproc#gcmu-02";
		String workFlowName = "Copy of CVRG - sqrs with data transfer";
		String historyName = "wftest533";
//		String fileName = "3793588_0001";
//		String filePath = "~/matched/s00402/"; 
		int i=0;
		for(String header:headerPaths){
			if(header.contains("_")){ // only analyze mimic2 data in the form "3793588_XXXX"
				if(!header.contains("_layout")){// where "_XXXX" is not "_layout", e.g. is a number
					String relativePath = header.replace("~/", globusConnectConfiguration.getDestinationRoot()); //e.g.changes "~/3000003_0008.hea" to "/home/WIN/cvrgglobusproc/wftest500/3000003_0008.hea"
					galaxyParameter[] paramArray = waveformGlobusConnect.buildGalaxyParameter(relativePath);
					RunGalaxyWorkflow rGW = new RunGalaxyWorkflow();
		//			rGW.runGalaxyWF(instanceUrl, apiKey, workFlowName, historyName, false, globusEndpoint, paramArray);
					rGW.runGalaxyWF(instanceUrl, apiKey, workFlowName, historyName, true, paramArray);
				}
			}
		}
		
		long tEnd = System.nanoTime();
		long tTotalRes = tEnd - tTotalStart; // total time in nanoseconds
		long tGalaxyRes = tEnd - tGalaxyStart; // time in nanoseconds of Galaxy API section
		long tGlobusRes = tGalaxyStart - tTotalStart; // Time in nanosecond of Globus section
		System.out.println("Globus Time: " + tGlobusRes/1000000000 + " sec");
		System.out.println("Galaxy Time: " + tGalaxyRes/1000000000 + " sec");
		System.out.println("Total Time: " + tTotalRes/1000000000 + " sec");
		//********************* Old Galaxy test section **********************
//		final String instanceUrl = "http://ec2-54-211-1-192.compute-1.amazonaws.com:8081/";
//		final String apiKey = "ed6d88d896b8d770254c6ab8e6abe201";
//		final String workFlowTemplate = "ZZZZZZZZZZ";
//		String historyName = "test3";
//	    String fileName = "3793588_0035";
//	    String filePath = globusConnectConfiguration.getDestinationRoot(); // "~/matched/s00402/";
//	    String globusEndpoint  = globusConnectConfiguration.getDestinationEP(); //  "cvrgglobusproc#gcmu-02";

		//runGalaxyWFHack(instanceUrl, apiKey, workFlowTemplate, historyName, fileName, filePath, globusEndpoint);

	}

	/** Builds the Galaxy Parameter array needed by the workflow "Copy of CVRG - sqrs with data transfer".
	 * 
	 * @param headerPathS
	 * @return
	 */
	private galaxyParameter[] buildGalaxyParameter(String headerPath){
		headerPath = headerPath.substring(0, headerPath.length()-4); // trim last 4 characters off (".hea")
		
		galaxyParameter[] paramArray = new galaxyParameter[4];

		// input, WFDB header file
		paramArray[0] = new galaxyParameter();
		paramArray[0].setStepIdNumber(487);
		paramArray[0].setStepParameterName("globus_get_data");
		paramArray[0].setValue(headerPath + ".hea");
		paramArray[0].setParamName("from_path");
		// input, WFDB header file
		paramArray[1] = new galaxyParameter();
		paramArray[1].setStepIdNumber(486);
		paramArray[1].setStepParameterName("globus_get_data");
		paramArray[1].setValue(headerPath + ".dat");
		paramArray[1].setParamName("from_path");
		// output, WFDB header file
		paramArray[2] = new galaxyParameter();
		paramArray[2].setStepIdNumber(495);
		paramArray[2].setStepParameterName("globus_send_data");
		paramArray[2].setValue(headerPath + "_sqrs.txt");
		paramArray[2].setParamName("to_path");
		// output, WFDB header file
		paramArray[3] = new galaxyParameter();
		paramArray[3].setStepIdNumber(494);
		paramArray[3].setStepParameterName("globus_send_data");
		paramArray[3].setValue(headerPath + "_wqrs.txt");
		paramArray[3].setParamName("to_path");
		
		return paramArray;
	}
	
	/** Transfers the contents of a folder (all files and subfolders) from the local source endpoint to the remote endpoint. Returns a list of records sent.
	 * 
	 * @param globusConnectConfiguration - has the connection criteria needed to make a Globus transfer <BR>
	 * e.g. SourceRoot, DestinationRoot, GlobusOnlineUsername, GlobusOnlinePassword, DestinationUsername, DestinationPassword, DestinationMyProxy
	 * @param extensions - list of extensions which the record header files might have (must be "hea" for WFDB). Used to create return list.
	 * 
	 * @return - arrayList of header files for all records found, includes the relative path and extension. e.g. "~/30/3000003_0001.hea"
	 */
	public ArrayList<String> transferFolder(GlobusConnectConfiguration globusConnectConfiguration, String[] extensions){
		/*
		GoauthClient cli = new GoauthClient("nexus.api.globusonline.org", "https://www.globusonline.org",
				globusConnectConfiguration.getGlobusOnlineUsername(), globusConnectConfiguration.getGlobusOnlinePassword());
		cli.setIgnoreCertErrors(true);
		String accessToken = null;
		try {

			JSONObject accessTokenJSON = cli.getClientOnlyAccessToken();
			accessToken = accessTokenJSON.getString("access_token");
			cli.validateAccessToken(accessToken);

		} catch (NexusClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Authenticator authenticator = new GoauthAuthenticator(accessToken);
		try {
			JSONTransferAPIClient client = new JSONTransferAPIClient(globusConnectConfiguration.getGlobusOnlineUsername(), null, null);
			client.setAuthenticator(authenticator);
			globusConnectConfiguration.setClient(client);
		} catch (Exception e) {
			System.err.println("Got an exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());

			e.printStackTrace();
		}
		setGlobusConnectConfiguration(globusConnectConfiguration);
		GlobusUploadBacking globusUploadBacking = new GlobusUploadBacking(globusConnectConfiguration);
		if (isOnlineGC(globusConnectConfiguration, globusUploadBacking)) {
			if(!isOnlineGCMU(globusConnectConfiguration, globusUploadBacking)) {
				System.out.println(globusConnectConfiguration.getDestinationEP() + " needs to be activated for data to transfer from " + globusConnectConfiguration.getSourceEP());
				activateEP(globusConnectConfiguration, globusConnectConfiguration.getDestinationUsername(), globusConnectConfiguration.getDestinationPassword(), globusConnectConfiguration.getDestinationEP());
			}
			System.out.println("Transferring data from " + globusConnectConfiguration.getSourceEP() + " to " + globusConnectConfiguration.getDestinationEP());
			System.out.println(export(globusConnectConfiguration));
		} else {
			System.out.println(globusConnectConfiguration.getSourceEP() + " needs to be connected for data to transfer to " + globusConnectConfiguration.getDestinationEP());	
		}
		*/
		File rootDir = new File(globusConnectConfiguration.getSourceRoot());
		
		Collection<File> headerFiles = FileUtils.listFiles(rootDir, extensions, true); // Finds files within a given directory and its subdirectories, which match the extension "hea".
		String sourceRoot = globusConnectConfiguration.getSourceRoot();
		ArrayList<String> headerList = new ArrayList<String>();
		for(File file:headerFiles){
			String headerPath = file.getPath().replace(sourceRoot, "~/"); // change absolute local path to path relative to local Globus root.
			headerList.add(headerPath);
			System.err.println(headerPath);
		}

		return headerList;
	}
	
	/**
	 * Public API intended for invocation of the GlobusExportService written by Dina
	 */
	public Status export(GlobusConnectConfiguration globusConnectConfiguration) {
		try{
			File directoryToSend = new File(globusConnectConfiguration.getSourceRoot());
			System.out.println("Absolute souce path: " + directoryToSend.getAbsolutePath());
			List<File> files = new ArrayList<File>(Arrays.asList(directoryToSend.listFiles()));
			return this.export(globusConnectConfiguration, files);
		}catch(NullPointerException npex){
			System.out.println("export() - No files found in source root: " + globusConnectConfiguration.getSourceRoot());
			return null;
		}
	}

	/**
	 * Private API that does the actual export.
	 */
	private Status export(GlobusConnectConfiguration globusConnectConfiguration, List<File> files) {

		try {

			String sourceEP = globusConnectConfiguration.getSourceEP();
			String sourceUsername = globusConnectConfiguration.getSourceUsername();
			String sourcePassword = globusConnectConfiguration.getSourcePassword();	
			String destinationEP = globusConnectConfiguration.getDestinationEP();
			String destinationUsername = globusConnectConfiguration.getDestinationUsername();
			String destinationPassword = globusConnectConfiguration.getDestinationPassword();
			String destinationRoot = globusConnectConfiguration.getDestinationRoot();
			JSONTransferAPIClient client = globusConnectConfiguration.getClient();

			//Activate source endpoint
			if(this.activateEP(globusConnectConfiguration, sourceUsername, sourcePassword, sourceEP).equals(Status.FAIL)){
				System.out.println("Source EP Activation failed..");
				return Status.FAIL;
			}


			//Activate destination endpoint
			if(this.activateEP(globusConnectConfiguration, destinationUsername, destinationPassword, destinationEP).equals(Status.FAIL)) {
				System.out.println("Destination EP Activation failed..");
				return Status.FAIL;
			}

			System.out.println("GO Endpoints Activation was successful..");


			JSONTransferAPIClient.Result result = client.getResult("/transfer/submission_id");
			String submissionId = result.document.getString("value");

			JSONObject transfer = new JSONObject();
			transfer.put("DATA_TYPE", "transfer");
			transfer.put("submission_id", submissionId);

			//Check if the OS is Windows and translate the Path to GO Compliant 			
			String os = System.getProperty("os.name").toLowerCase();			
			//Uncomment the following line if you want to check the version of Globus Connect on Windows before every transfer!! 
			//windowsGCVersion = "";


			// Iterate through the files and create a JSON object to be sent to the GO REST API.			
			Iterator<File> iterator = files.iterator();
			while(iterator.hasNext()){

				File file = iterator.next();
				String sPath = file.getAbsolutePath();
				if(os.indexOf("win")>= 0){

					//Preparing the source path by removing : on Windows. 
					String[] sPathSplit = sPath.split(":");
					sPath = "/"+ sPathSplit[0].toLowerCase() + sPathSplit[1].replace("\\","/");

					//Check if this source path is accessible on GO, if not assume it is cygdrive
					// version of the GC and append /cygdrive to path.
					// The rest of the code in this if-loop should be deleted once GC bug is fixed.
					if(this.checkIfOldWindowsGC(globusConnectConfiguration, file).equals("old")){
						//System.out.println("It might be an older version of GC, Trying cygdrive based configuration..");
						sPath = "/cygdrive" + sPath;
					}

				}

				//System.out.println("File to be transferred: " + sPath); 
				JSONObject item = new JSONObject();
				item.put("DATA_TYPE", "transfer_item");
				item.put("source_endpoint", sourceEP);
				item.put("source_path", sPath);
				item.put("destination_endpoint", destinationEP);
				item.put("destination_path", destinationRoot + file.getName());

				transfer.append("DATA", item);

			}


			result = client.postResult("/transfer", transfer, null);

			System.out.println("Initiating Globus Online Transfer..");

		} catch (Exception e) {
			System.err.println("Got an exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());

			e.printStackTrace();
			return Status.FAIL;
		}
		System.out.println("Transfer complete..");
		return Status.OK;

	}

	/**
	 * Private API that activates an endpoint for the transfer.
	 */
	private Status activateEP(GlobusConnectConfiguration globusConnectConfiguration, String EpUsername, String EpPassword, String endpoint){

		String myProxy = globusConnectConfiguration.getDestinationMyProxy();

		System.out.println("Activating Endpoint: " + endpoint);
		try {

			JSONTransferAPIClient client = globusConnectConfiguration.getClient();  
			org.globusonline.transfer.Example GOClient = new Example(client);

			if(EpUsername == null || EpUsername == ""){
				System.out.println("EP username and password is null. Attempting Autoactivations without username/passwd.");
				if (!GOClient.autoActivate(endpoint)) {
					System.err.println("Unable to auto activate GO endpoint : " + endpoint);                               
					return Status.FAIL;
				}
			}else{
				if (myproxyActivateEP(client, EpUsername, EpPassword, endpoint, myProxy).equals(Status.FAIL)){
					System.err.println("Unable to activate GO endpoint : " + endpoint);                               
					return Status.FAIL;					
				}
			}
			return Status.OK;

		} catch (IOException e) {
			System.err.println("Got an IO exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());

			e.printStackTrace();
			return Status.FAIL;
		} catch (JSONException e) {
			System.err.println("Got an JSON exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());
			e.printStackTrace();
			return Status.FAIL;
		} catch (GeneralSecurityException e) {
			System.err.println("Got an Security exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());
			e.printStackTrace();
			return Status.FAIL;
		} catch (APIError e) {
			System.err.println("Got an APIError exception..\n");
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace().toString());
			e.printStackTrace();
			return Status.FAIL;
		}				

	}

		private Status myproxyActivateEP(JSONTransferAPIClient client, String myproxyUsername,
				String myproxyPassphrase,
				String endpointName,
				String myproxyHostname)
		{
	
			System.out.println("Activating Endpoint using myproxy: " + endpointName);
	
			try {
				String url = client.endpointPath(endpointName) + "/activation_requirements";
				JSONTransferAPIClient.Result r = client.getResult(url);
	
				// Go through requirements and find the myproxy type, then fill
				// in with the values from the function^Wmethod parameters.
				System.out.println("Endpoint activated:" + r.document.getString("activated") );
				JSONArray reqsArray = r.document.getJSONArray("DATA");
				for (int i=0; i < reqsArray.length(); i++) {
					JSONObject reqObject = reqsArray.getJSONObject(i);
					if (reqObject.getString("type").equals("myproxy")) {
						String name = reqObject.getString("name");
						if (name.equals("hostname")) {
							reqObject.put("value", myproxyHostname);
						} else if (name.equals("username")) {
							reqObject.put("value", myproxyUsername);
						} else if (name.equals("passphrase")) {
							reqObject.put("value", myproxyPassphrase);
						}
						// optional arguments are 'server_dn', required if the hostname
						// does not match the DN in the server's certificate, and
						// 'lifetime_in_hours', to ask for a specific lifetime rather
						// than accepting the server default.
						// See also:
						//  https://transfer.api.globusonline.org/v0.10/document_type/activation_requirements/example?format=json
					}
				}
	
				url = client.endpointPath(endpointName) + "/activate";
				r = client.postResult(url, r.document);
	
				// return r; yge: should check on r to determine what to return
				if (r.statusCode >= 400) {
					
					System.err.println("Returned statusCode >=400 : " + r.statusCode + "\n");
					return Status.FAIL;
				}
				return Status.OK;
	
			} catch (IOException e) {
				System.err.println("Got an IO exception..\n");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace().toString());
	
				e.printStackTrace();
				return Status.FAIL;
			} catch (JSONException e) {
				System.err.println("Got an JSON exception..\n");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace().toString());
				e.printStackTrace();
				return Status.FAIL;
			} catch (GeneralSecurityException e) {
				System.err.println("Got an Security exception..\n");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace().toString());
				e.printStackTrace();
				return Status.FAIL;
			} catch (APIError e) {
				System.err.println("Got an APIError exception..\n");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace().toString());
				e.printStackTrace();
				return Status.FAIL;
			}			
	
		}


	/**
	 * Private API written to check the version of Globus Connect due to issues found with
	 * native file system access in versions greater than 1.33, as of 10/31/2013.
	 * This may not be needed if the Globus Connect problem on Windows is fixed. 
	 */
	private String checkIfOldWindowsGC(GlobusConnectConfiguration globusConnectConfiguration, File file){ //return "new" or "old"

		if (globusConnectConfiguration.getWindowsGCVersion().equals("")){			

			String sourceEP = globusConnectConfiguration.getSourceEP();

			try{

				JSONTransferAPIClient client = globusConnectConfiguration.getClient();  
				Map<String, String> pathMap = new HashMap<String, String>();				
				String[] parentDirSplit = file.getAbsoluteFile().getParent().split(":");
				String parentDir = "/" + parentDirSplit[0].toLowerCase() + parentDirSplit[1].replace("\\", "/");
				pathMap.put("path", parentDir);								
				//				JSONTransferAPIClient.Result listing = client.requestDirListing("GET", "/endpoint/" 
				JSONTransferAPIClient.Result listing = client.requestResult("GET", "/endpoint/" 
						+ (sourceEP.contains("#") ? sourceEP.split("#")[1] : sourceEP)+ "/ls", null, pathMap);

				if(listing.statusCode == 400){
					System.out.println("It might be an older version of GC, Trying cygdrive based configuration..");
					globusConnectConfiguration.setWindowsGCVersion("old");
				}else{
					System.out.println("It seems to be a newer version of GC..");
					globusConnectConfiguration.setWindowsGCVersion("new");
				}

			} catch (Exception e) {
				System.err.println("Got an exception..\n");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace().toString());
			}
		}

		return globusConnectConfiguration.getWindowsGCVersion();

	}


	/**
	 * @return the configFilename
	 */
	public String getConfigFilename() {
		return configFilename;
	}

	/**
	 * @return the globusConnectConfiguration
	 */
	public GlobusConnectConfiguration getGlobusConnectConfiguration() {
		return globusConnectConfiguration;
	}

	/**
	 * @param globusConnectConfiguration the globusConnectConfiguration to set
	 */
	public void setGlobusConnectConfiguration(
			GlobusConnectConfiguration globusConnectConfiguration) {
		this.globusConnectConfiguration = globusConnectConfiguration;
	}
	/**
	 * @return the onlineGC
	 */
	public boolean isOnlineGC(GlobusConnectConfiguration globusConnectConfiguration, GlobusUploadBacking globusUploadBacking) {
		onlineGC = false;
		String usedGC = globusConnectConfiguration.getSourceEP().split("#")[1];
		for (GlobusEndpoint e : globusUploadBacking.getGlobusEndpointLists().get("GC").getGlobusEndpoints()) {
			if (e.getName().equalsIgnoreCase(usedGC))
				for (GlobusServer s : e.getGlobusServers()) {
					onlineGC = s.getIsConnected();
				}
		}
		return onlineGC;
	}

	/**
	 * @return the onlineGCMU
	 */
	public boolean isOnlineGCMU(GlobusConnectConfiguration globusConnectConfiguration, GlobusUploadBacking globusUploadBacking) {
		onlineGCMU = false;
		String usedGC = globusConnectConfiguration.getDestinationEP().split("#")[1];
		for (GlobusEndpoint e : globusUploadBacking.getGlobusEndpointLists().get("GCMU").getGlobusEndpoints()) {
			if (e.getName().equalsIgnoreCase(usedGC))
				onlineGCMU = e.getActivated();
		}
		return onlineGCMU;
	}
	
	  /** Creates and runs a Galaxy Workflow based on the workFlowTemplate.<BR>
	   * Replaces the placeholder text (XXXXXXXXXX and YYYYYYYYYY) in the template workflow with fileName and FilePath.<BR>
	   * This is a kludge to the go with the hack required to execute a workflow using blend4j library which doesn't accept parameters.<BR>
	   * 
	   * @param instanceURL - URL of the Galaxy instance run the workflow on.
	   * @param apiKey - the Galaxy API key for a user who can access the templateWorkflow.
	   * @param workFlowTemplate - the name of workflow template e.g. "ZZZZZZZZZZ"
	   * @param workflowJSON - JSON representation of the (sqrs) template workflow.
	   * @param historyName - name of an existing history to record the execution of new workflow in.
	   * @param recordName - WFDB record name (e.g. "3793588_0035") assumed to be the filename of the ".hea" file and of a single ".dat" file.
	   * @param filePath - Relative location of the WFDB record on Globus Server (e.g. "~/matched/s00402/").
	   * @param globusEndpoint - name of the Globus endpoint containing the WFDB record files (e.g. "cvrgglobusproc#gcmu-02")
	   * @return
	   */
/*	  public static void runGalaxyWF(final String instanceURL, final String apiKey, final String workFlowTemplate, String historyName,
			  String fileName, String filePath, String globusEndpoint) {
		    final GalaxyInstance instance = GalaxyInstanceFactory.get(instanceURL, apiKey);
		    final WorkflowsClient workflowsClient = instance.getWorkflowsClient();

		    // Find history
		    System.out.println("Find history: ");
		    final HistoriesClient historyClient = instance.getHistoriesClient();
		    History matchingHistory = null;
		    for(final History history : historyClient.getHistories()) {
		      if(history.getName().equals(historyName)) { // blend4j Test History // TestHistory1 
		        matchingHistory = history;
		        System.out.println(history.getId() + ") " + history.getName());
		      }
		    }

		    System.out.println("Find workflow: ");
		    Workflow matchingWorkflow = null;
		    for(Workflow workflow : workflowsClient.getWorkflows()) {
		      if(workflow.getName().equals(workFlowTemplate)) {
		        matchingWorkflow = workflow;
		        System.out.println(workflow.getId() + ") " + workflow.getName());
		      }
		    }

		    String workflowJSON = workflowsClient.exportWorkflow(matchingWorkflow.getId());	    
			 // modify workflow json
		    workflowJSON = replaceFileNameJSON(workflowJSON, fileName, filePath, globusEndpoint);
//		    System.out.println("JSON-encoded representation of the Workflow:\n" + workflowJSON);
		    Workflow importedWorkflow = workflowsClient.importWorkflow(workflowJSON);
		    String importedWorkflowId = importedWorkflow.getId();
		    
		    final WorkflowInputs inputs = new WorkflowInputs();
		    inputs.setDestination(new WorkflowInputs.ExistingHistory(matchingHistory.getId()));
		    inputs.setWorkflowId(importedWorkflowId);
		   
		    final WorkflowOutputs output = workflowsClient.runWorkflow(inputs);
		    System.out.println("Running workflow in history " + output.getHistoryId());
		    for(String outputId : output.getOutputIds()) {
		      System.out.println("  Workflow writing to output id " + outputId);
		    }
		  }
	  
	  
	  *//** Replaces the placeholder text (XXXXXXXXXX and YYYYYYYYYY) in the template workflow ZZZZZZZZZZ with fileName and FilePath.<BR>
	   * This is a kludge to the go with the hack required to execute a workflow using blend4j library which doesn't accept parameters.<BR>
	   * It is not efficient, and will probably still run faster than the workflow itself.
	   * 
	   * @param workflowJSON - JSON representation of the (sqrs) template workflow.
	   * @param recordName - WFDB record name (e.g. "3793588_0035") assumed to be the filename of the ".hea" file and of a single ".dat" file.
	   * @param filePath - Relative location of the WFDB record on Globus Server (e.g. "~/matched/s00402/").
	   * @param globusEndpoint - name of the Globus endpoint containing the WFDB record files (e.g. "cvrgglobusproc#gcmu-02")
	   * @return
	   *//*
	  private static String replaceFileNameJSON(String workflowJSON, String recordName, String filePath, String globusEndpoint){
		  Date date = new Date();
	      SimpleDateFormat ft = new SimpleDateFormat ("'SQRS_" + recordName + "' yyyy.MM.dd  hh:mm:ss a");
		  String workflowName = ft.format(date);
		  
		  workflowJSON = workflowJSON.replace("WWWWWWWWWW", globusEndpoint); // e.g. "cvrgglobusproc#gcmu-02"
		  workflowJSON = workflowJSON.replace("XXXXXXXXXX", recordName); // e.g. "3793588_0035"
		  workflowJSON = workflowJSON.replace("YYYYYYYYYY", filePath);	// e.g. "~/matched/s00402/"
		  workflowJSON = workflowJSON.replace("ZZZZZZZZZZ", workflowName);	  
		  
		  return workflowJSON;	  
	  }
*/
}
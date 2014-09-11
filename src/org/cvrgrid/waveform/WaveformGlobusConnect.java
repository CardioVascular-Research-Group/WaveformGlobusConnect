/* Copyright 2013 Cardiovascular Research Grid
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
 */

package org.cvrgrid.waveform;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.cvrgrid.waveform.backing.GlobusUploadBacking;
import org.cvrgrid.waveform.model.GlobusConnectConfiguration;
import org.cvrgrid.waveform.model.GlobusEndpoint;
import org.cvrgrid.waveform.model.GlobusServer;
import org.globusonline.transfer.APIError;
import org.globusonline.transfer.Example;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.rsna.ctp.pipeline.Status;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * This is the main class to transfer local Waveform files using Globus Connect.
 * It is a variation of the CTP-GlobusExportService written by Dina Sulakhe.
 * That software can be found here: https://github.com/sulakhe/CTP-GlobusExportService.
 * You should read the details on that site to understand the /resources/server.properties.
 * This command line tool looks for 6 parameters,
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
 * 
 * Once the code has those parameters, the code will check to see if the Globus Endpoints have been 
 * activated.  While the destination endpoint is likely to be a Globus Connect Multi-User that can be 
 * activated remotely, the source is likely to be a Globus Connect Client.  The Globus Connect Client 
 * must be running in order for this code to work.  After both checks have passed, the code will 
 * transfer the data from the source to the destination, echoing the results in the terminal window.
 * 
 * The tool requires the bcprov_ext_jdk16_146, log4j and TransferAPIClient libraries and 
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

			GlobusConnectConfiguration globusConnectConfiguration = new GlobusConnectConfiguration();
			Properties serverProperties = new Properties();
			InputStream stream = WaveformGlobusConnect.class.getResourceAsStream(this.getConfigFilename());
			serverProperties.load(stream);
			globusConnectConfiguration.setGlobusOnlineUsername(serverProperties.getProperty("globusOnlineUsername"));
			globusConnectConfiguration.setGlobusOnlinePassword(serverProperties.getProperty("globusOnlinePassword"));
			globusConnectConfiguration.setCertFile(serverProperties.getProperty("certFile"));
			globusConnectConfiguration.setKeyFile(serverProperties.getProperty("keyFile"));
			globusConnectConfiguration.setCaFile(serverProperties.getProperty("caFile"));
			globusConnectConfiguration.setSourceEP(serverProperties.getProperty("sourceEP"));
			globusConnectConfiguration.setSourceRoot(serverProperties.getProperty("sourceRoot"));
			globusConnectConfiguration.setSourceUsername(serverProperties.getProperty("sourceUsername"));
			globusConnectConfiguration.setSourcePassword(serverProperties.getProperty("sourcePassword"));	
			globusConnectConfiguration.setDestinationEP(serverProperties.getProperty("destinationEP"));
			globusConnectConfiguration.setDestinationRoot(serverProperties.getProperty("destinationRoot"));
			globusConnectConfiguration.setDestinationUsername(serverProperties.getProperty("destinationUsername"));
			globusConnectConfiguration.setDestinationPassword(serverProperties.getProperty("destinationPassword"));			
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
	 */
	public static void main(String[] args) {
	
			WaveformGlobusConnect waveformGlobusConnect = new WaveformGlobusConnect();
			GlobusConnectConfiguration globusConnectConfiguration = waveformGlobusConnect.getGlobusConnectConfiguration();
			globusConnectConfiguration.setSourceRoot(args[0]);
			globusConnectConfiguration.setDestinationRoot(args[1]);
			globusConnectConfiguration.setGlobusOnlineUsername(args[2]);
			globusConnectConfiguration.setGlobusOnlinePassword(args[3]);
			globusConnectConfiguration.setDestinationUsername(args[4]);
			globusConnectConfiguration.setDestinationPassword(args[5]);
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
				
	}

	/**
	 * Public API intended for invocation of the GlobusExportService written by Dina
	 */
	public Status export(GlobusConnectConfiguration globusConnectConfiguration) {

		File directoryToSend = new File(globusConnectConfiguration.getSourceRoot());
		List<File> files = new ArrayList<File>(Arrays.asList(directoryToSend.listFiles()));
		return this.export(globusConnectConfiguration, files);
		
	}
	
	/**
	 * Private API that does the actual export.
	 */
	private Status export(GlobusConnectConfiguration globusConnectConfiguration, List<File> files) {

		try {
			
			String globusOnlineUsername = globusConnectConfiguration.getGlobusOnlineUsername();
			String caFile = globusConnectConfiguration.getCaFile();
			String certFile = globusConnectConfiguration.getCertFile();
			String keyFile = globusConnectConfiguration.getKeyFile();
			String sourceEP = globusConnectConfiguration.getSourceEP();
			String sourceUsername = globusConnectConfiguration.getSourceUsername();
			String sourcePassword = globusConnectConfiguration.getSourcePassword();	
			String destinationEP = globusConnectConfiguration.getDestinationEP();
			String destinationUsername = globusConnectConfiguration.getDestinationUsername();
			String destinationPassword = globusConnectConfiguration.getDestinationPassword();
			String destinationRoot = globusConnectConfiguration.getDestinationRoot();
		
			JSONTransferAPIClient client = new JSONTransferAPIClient(globusOnlineUsername, caFile, certFile, keyFile);        

			//Activate source endpoint
			if(this.activateEP(globusConnectConfiguration, sourceUsername, sourcePassword, sourceEP).equals(Status.FAIL)){
				logger.info("Source EP Activation failed..");
				return Status.FAIL;
			}
				
			
			//Activate destination endpoint
			if(this.activateEP(globusConnectConfiguration, destinationUsername, destinationPassword, destinationEP).equals(Status.FAIL)) {
				logger.info("Destination EP Activation failed..");
				return Status.FAIL;
			}
			
			logger.info("GO Endpoints Activation was successful..");
			
			
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
						//logger.info("It might be an older version of GC, Trying cygdrive based configuration..");
						sPath = "/cygdrive" + sPath;
					}
					
				}

				//logger.info("File to be transferred: " + sPath);
				JSONObject item = new JSONObject();
				item.put("DATA_TYPE", "transfer_item");
				item.put("source_endpoint", sourceEP);
				item.put("source_path", sPath);
				item.put("destination_endpoint", destinationEP);
				item.put("destination_path", destinationRoot + file.getName());

				transfer.append("DATA", item);
			
			}

			
			result = client.postResult("/transfer", transfer, null);
		
			logger.info("Initiating Globus Online Transfer..");

		} catch (Exception e) {
			logger.error("Got an exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());
			
			e.printStackTrace();
			return Status.FAIL;
		}
		logger.info("Transfer complete..");
		return Status.OK;

	}
	
	/**
	 * Private API that activates an endpoint for the transfer.
	 */
	private Status activateEP(GlobusConnectConfiguration globusConnectConfiguration, String EpUsername, String EpPassword, String endpoint){
		
		String globusOnlineUsername = globusConnectConfiguration.getGlobusOnlineUsername();
		String caFile = globusConnectConfiguration.getCaFile();
		String certFile = globusConnectConfiguration.getCertFile();
		String keyFile = globusConnectConfiguration.getKeyFile();

		logger.info("Activating Endpoint: " + endpoint);
		try {
			JSONTransferAPIClient client = new JSONTransferAPIClient(globusOnlineUsername, caFile, certFile, keyFile);        
			org.globusonline.transfer.Example GOClient = new Example(client);

			if(EpUsername == null || EpUsername == ""){
				logger.info("EP username and password is null. Attempting Autoactivations without username/passwd.");
				if (!GOClient.autoActivate(endpoint)) {
					logger.error("Unable to auto activate GO endpoint : " + endpoint);                               
					return Status.FAIL;
				}
			}else{
				if(!GOClient.runPasswordActivation(endpoint, EpUsername, EpPassword )){
					logger.error("Unable to activate GO endpoint : " + endpoint);                               
					return Status.FAIL;					
				}
			}
			return Status.OK;

		} catch (IOException e) {
			logger.error("Got an IO exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());

			e.printStackTrace();
			return Status.FAIL;
		} catch (JSONException e) {
			logger.error("Got an JSON exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());
			e.printStackTrace();
			return Status.FAIL;
		} catch (GeneralSecurityException e) {
			logger.error("Got an Security exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());
			e.printStackTrace();
			return Status.FAIL;
		} catch (APIError e) {
			logger.error("Got an APIError exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());
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
		
			String globusOnlineUsername = globusConnectConfiguration.getGlobusOnlineUsername();
			String caFile = globusConnectConfiguration.getCaFile();
			String certFile = globusConnectConfiguration.getCertFile();
			String keyFile = globusConnectConfiguration.getKeyFile();
			String sourceEP = globusConnectConfiguration.getSourceEP();
			
			try{
				JSONTransferAPIClient client = new JSONTransferAPIClient(globusOnlineUsername, caFile, certFile, keyFile);        
				Map<String, String> pathMap = new HashMap<String, String>();				
				String[] parentDirSplit = file.getAbsoluteFile().getParent().split(":");
				String parentDir = "/" + parentDirSplit[0].toLowerCase() + parentDirSplit[1].replace("\\", "/");
				pathMap.put("path", parentDir);								
				JSONTransferAPIClient.Result listing = client.requestDirListing("GET", "/endpoint/" 
						+ (sourceEP.contains("#") ? sourceEP.split("#")[1] : sourceEP)+ "/ls", pathMap);
				
				if(listing.statusCode == 400){
					logger.info("It might be an older version of GC, Trying cygdrive based configuration..");
					globusConnectConfiguration.setWindowsGCVersion("old");
				}else{
					logger.info("It seems to be a newer version of GC..");
					globusConnectConfiguration.setWindowsGCVersion("new");
				}
				
			} catch (Exception e) {
				logger.error("Got an exception..\n");
				logger.error(e.getMessage());
				logger.error(e.getStackTrace().toString());
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

}
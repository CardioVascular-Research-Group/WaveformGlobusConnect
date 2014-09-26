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
 */
package org.cvrgrid.waveform.model;

import java.io.Serializable;
import org.globusonline.transfer.JSONTransferAPIClient;

/*
 * This is a model class for use in the transfer of local Waveform files using Globus Connect.
 * The purpose of this model is to use the properties and store variables 
 * for use in other sections of the main class.
 */

public class GlobusConnectConfiguration implements Serializable {

	private static final long serialVersionUID = 6721093052749142738L;
	private String globusOnlineUsername;
	private String globusOnlinePassword;
	private String sourceEP;
	private String sourceRoot;
	private String sourceUsername;
	private String sourcePassword;	
	private String destinationEP;
	private String destinationRoot;
	private String destinationUsername;
	private String destinationPassword;
	private String destinationMyProxy;
	private String windowsGCVersion;
	private JSONTransferAPIClient client;

	public GlobusConnectConfiguration() {
		
	}
	
	/**
	 * @return the globusOnlineUsername
	 */
	public String getGlobusOnlineUsername() {
		return globusOnlineUsername;
	}
	/**
	 * @param globusOnlineUsername the globusOnlineUsername to set
	 */
	public void setGlobusOnlineUsername(String globusOnlineUsername) {
		this.globusOnlineUsername = globusOnlineUsername;
	}
	/**
	 * @return the globusOnlinePassword
	 */
	public String getGlobusOnlinePassword() {
		return globusOnlinePassword;
	}
	/**
	 * @param globusOnlinePassword the globusOnlinePassword to set
	 */
	public void setGlobusOnlinePassword(String globusOnlinePassword) {
		this.globusOnlinePassword = globusOnlinePassword;
	}
	/**
	 * @return the sourceEP
	 */
	public String getSourceEP() {
		return sourceEP;
	}
	/**
	 * @param sourceEP the sourceEP to set
	 */
	public void setSourceEP(String sourceEP) {
		this.sourceEP = sourceEP;
	}
	/**
	 * @return the sourceRoot
	 */
	public String getSourceRoot() {
		return sourceRoot;
	}
	/**
	 * @param sourceRoot the sourceRoot to set
	 */
	public void setSourceRoot(String sourceRoot) {
		this.sourceRoot = sourceRoot;
	}
	/**
	 * @return the sourceUsername
	 */
	public String getSourceUsername() {
		return sourceUsername;
	}
	/**
	 * @param sourceUsername the sourceUsername to set
	 */
	public void setSourceUsername(String sourceUsername) {
		this.sourceUsername = sourceUsername;
	}
	/**
	 * @return the sourcePassword
	 */
	public String getSourcePassword() {
		return sourcePassword;
	}
	/**
	 * @param sourcePassword the sourcePassword to set
	 */
	public void setSourcePassword(String sourcePassword) {
		this.sourcePassword = sourcePassword;
	}
	/**
	 * @return the destinationEP
	 */
	public String getDestinationEP() {
		return destinationEP;
	}
	/**
	 * @param destinationEP the destinationEP to set
	 */
	public void setDestinationEP(String destinationEP) {
		this.destinationEP = destinationEP;
	}
	/**
	 * @return the destinationRoot
	 */
	public String getDestinationRoot() {
		return destinationRoot;
	}
	/**
	 * @param destinationRoot the destinationRoot to set
	 */
	public void setDestinationRoot(String destinationRoot) {
		this.destinationRoot = destinationRoot;
	}
	/**
	 * @return the destinationUsername
	 */
	public String getDestinationUsername() {
		return destinationUsername;
	}
	/**
	 * @param destinationUsername the destinationUsername to set
	 */
	public void setDestinationUsername(String destinationUsername) {
		this.destinationUsername = destinationUsername;
	}
	/**
	 * @return the destinationPassword
	 */
	public String getDestinationPassword() {
		return destinationPassword;
	}
	/**
	 * @param destinationPassword the destinationPassword to set
	 */
	public void setDestinationPassword(String destinationPassword) {
		this.destinationPassword = destinationPassword;
	}
	/**
	 * @return the destinationMyProxy
	 */
	public String getDestinationMyProxy() {
		return destinationMyProxy;
	}
	/**
	 * @param destinationMyProxy the destinationMyProxy to set
	 */
	public void setDestinationMyProxy(String destinationMyProxy) {
		this.destinationMyProxy = destinationMyProxy;
	}
	/**
	 * @return the windowsGCVersion
	 */
	public String getWindowsGCVersion() {
		return windowsGCVersion;
	}
	/**
	 * @param windowsGCVersion the windowsGCVersion to set
	 */
	public void setWindowsGCVersion(String windowsGCVersion) {
		this.windowsGCVersion = windowsGCVersion;
	}
	/**
	 * @return the client
	 */
	public JSONTransferAPIClient getClient() {
		return client;
	}
	/**
	 * @param client the client to set
	 */
	public void setClient(JSONTransferAPIClient client) {
		this.client = client;
	}

}

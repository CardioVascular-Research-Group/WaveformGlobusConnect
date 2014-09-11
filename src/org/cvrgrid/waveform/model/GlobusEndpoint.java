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
 */
package org.cvrgrid.waveform.model;

import java.util.ArrayList;
import java.util.Date;

public class GlobusEndpoint {

	  String userName; 
	  String globusConnectSetupKey; 
	  String name; 
	  String dataType; 
	  Boolean activated;
	  Boolean isGlobusConnect;
	  GlobusLink lsLink;
	  String canonicalName; 
	  String myProxyServer; 
	  Date expireTime; 
	  ArrayList<GlobusServer> globusServers = new ArrayList<GlobusServer>();
	  Boolean globusPublic; 
	  String description;
	  String sharingTargetEndpoint;
	  String sharingTargetRootPath;
	  String hostPath;
	  String defaultDirectory;
	  Integer expiresIn;
	  String myProxyDomainName;
	  String hostEndpoint;
	  Boolean inUse;
	  String oauthServer;
	  Boolean isGoStorage;
	  
	public GlobusEndpoint() {
		
	}
	/**
	 * @return the username
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param username the username to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the globusConnectSetupKey
	 */
	public String getGlobusConnectSetupKey() {
		return globusConnectSetupKey;
	}
	/**
	 * @param globusConnectSetupKey the globusConnectSetupKey to set
	 */
	public void setGlobusConnectSetupKey(String globusConnectSetupKey) {
		this.globusConnectSetupKey = globusConnectSetupKey;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}
	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return the activated
	 */
	public Boolean getActivated() {
		return activated;
	}
	/**
	 * @param activated the activated to set
	 */
	public void setActivated(Boolean activated) {
		this.activated = activated;
	}
	/**
	 * @return the isGlobusConnect
	 */
	public Boolean getIsGlobusConnect() {
		return isGlobusConnect;
	}
	/**
	 * @param isGlobusConnect the isGlobusConnect to set
	 */
	public void setIsGlobusConnect(Boolean isGlobusConnect) {
		this.isGlobusConnect = isGlobusConnect;
	}
	/**
	 * @return the lsLink
	 */
	public GlobusLink getLsLink() {
		return lsLink;
	}
	/**
	 * @param lsLink the lsLink to set
	 */
	public void setLsLink(GlobusLink lsLink) {
		this.lsLink = lsLink;
	}
	/**
	 * @return the canonicalName
	 */
	public String getCanonicalName() {
		return canonicalName;
	}
	/**
	 * @param canonicalName the canonicalName to set
	 */
	public void setCanonicalName(String canonicalName) {
		this.canonicalName = canonicalName;
	}
	/**
	 * @return the myProxyServer
	 */
	public String getMyProxyServer() {
		return myProxyServer;
	}
	/**
	 * @param myProxyServer the myProxyServer to set
	 */
	public void setMyProxyServer(String myProxyServer) {
		this.myProxyServer = myProxyServer;
	}
	/**
	 * @return the expireTime
	 */
	public Date getExpireTime() {
		return expireTime;
	}
	/**
	 * @param expireTime the expireTime to set
	 */
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	/**
	 * @return the globusServers
	 */
	public ArrayList<GlobusServer> getGlobusServers() {
		return globusServers;
	}
	/**
	 * @param globusServers the globusServers to set
	 */
	public void setGlobusServers(ArrayList<GlobusServer> globusServers) {
		this.globusServers = globusServers;
	}
	/**
	 * @return the globusPublic
	 */
	public Boolean getGlobusPublic() {
		return globusPublic;
	}
	/**
	 * @param globusPublic the globusPublic to set
	 */
	public void setGlobusPublic(Boolean globusPublic) {
		this.globusPublic = globusPublic;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the sharingTargetEndpoint
	 */
	public String getSharingTargetEndpoint() {
		return sharingTargetEndpoint;
	}
	/**
	 * @param sharingTargetEndpoint the sharingTargetEndpoint to set
	 */
	public void setSharingTargetEndpoint(String sharingTargetEndpoint) {
		this.sharingTargetEndpoint = sharingTargetEndpoint;
	}
	/**
	 * @return the sharingTargetRootPath
	 */
	public String getSharingTargetRootPath() {
		return sharingTargetRootPath;
	}
	/**
	 * @param sharingTargetRootPath the sharingTargetRootPath to set
	 */
	public void setSharingTargetRootPath(String sharingTargetRootPath) {
		this.sharingTargetRootPath = sharingTargetRootPath;
	}
	/**
	 * @return the hostPath
	 */
	public String getHostPath() {
		return hostPath;
	}
	/**
	 * @param hostPath the hostPath to set
	 */
	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;
	}
	/**
	 * @return the defaultDirectory
	 */
	public String getDefaultDirectory() {
		return defaultDirectory;
	}
	/**
	 * @param defaultDirectory the defaultDirectory to set
	 */
	public void setDefaultDirectory(String defaultDirectory) {
		this.defaultDirectory = defaultDirectory;
	}
	/**
	 * @return the expiresIn
	 */
	public Integer getExpiresIn() {
		return expiresIn;
	}
	/**
	 * @param expiresIn the expiresIn to set
	 */
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}
	/**
	 * @return the myProxyDomainName
	 */
	public String getMyProxyDomainName() {
		return myProxyDomainName;
	}
	/**
	 * @param myProxyDomainName the myProxyDomainName to set
	 */
	public void setMyProxyDomainName(String myProxyDomainName) {
		this.myProxyDomainName = myProxyDomainName;
	}
	/**
	 * @return the hostEndpoint
	 */
	public String getHostEndpoint() {
		return hostEndpoint;
	}
	/**
	 * @param hostEndpoint the hostEndpoint to set
	 */
	public void setHostEndpoint(String hostEndpoint) {
		this.hostEndpoint = hostEndpoint;
	}
	/**
	 * @return the inUse
	 */
	public Boolean getInUse() {
		return inUse;
	}
	/**
	 * @param inUse the inUse to set
	 */
	public void setInUse(Boolean inUse) {
		this.inUse = inUse;
	}
	/**
	 * @return the oauthServer
	 */
	public String getOauthServer() {
		return oauthServer;
	}
	/**
	 * @param oauthServer the oauthServer to set
	 */
	public void setOauthServer(String oauthServer) {
		this.oauthServer = oauthServer;
	}
	/**
	 * @return the isGoStorage
	 */
	public Boolean getIsGoStorage() {
		return isGoStorage;
	}
	/**
	 * @param isGoStorage the isGoStorage to set
	 */
	public void setIsGoStorage(Boolean isGoStorage) {
		this.isGoStorage = isGoStorage;
	}
	  

}

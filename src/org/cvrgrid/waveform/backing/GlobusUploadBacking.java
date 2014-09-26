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

package org.cvrgrid.waveform.backing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cvrgrid.waveform.WaveformGlobusConnect;
import org.cvrgrid.waveform.model.GlobusConnectConfiguration;
import org.cvrgrid.waveform.model.GlobusEndpoint;
import org.cvrgrid.waveform.model.GlobusEndpointList;
import org.cvrgrid.waveform.model.GlobusServer;
import org.cvrgrid.waveform.model.GlobusLink;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.rsna.ctp.pipeline.Status;

/*
 * This is the backing bean class to transfer local Waveform files using Globus Connect.
 * It is a variation of the CTP-GlobusExportService written by Dina Sulakhe, coupling it with
 * the Java Nexus Client written by Chris Jurado and extended by Josh Bryan.
 * The purpose of this bean is to use the models included in this software package,
 * populating them with information that can be returned from a number of Globus REST services.
 * 
 * The tool requires the bcprov_ext_jdk16_146, log4j, TransferAPIClient and java-nexus-client libraries and 
 * the org.rsna.ctp.pipeline.Status class (included) to function properly.
 */

public class GlobusUploadBacking {

	static final Logger logger = Logger.getLogger(WaveformGlobusConnect.class);
	private Map<String, GlobusEndpointList> globusEndpointLists = new HashMap<String, GlobusEndpointList>();

	/**
	 * Public API that echoes out the Globus Endpoints for a user and their respective statuses.
	 */
	public GlobusUploadBacking(GlobusConnectConfiguration globusConnectConfiguration) {

		try {

			//JSONTransferAPIClient client = new JSONTransferAPIClient(globusConnectConfiguration.getGlobusOnlineUsername(), globusConnectConfiguration.getCaFile(), globusConnectConfiguration.getCertFile(), globusConnectConfiguration.getKeyFile());
			JSONTransferAPIClient client = globusConnectConfiguration.getClient();
			getGlobusConnectEndpoints(client);
			for (GlobusEndpoint e : getGlobusEndpointLists().get("GC").getGlobusEndpoints()) {
				logger.info(e.getName() + " ");
				for (GlobusServer s : e.getGlobusServers()) {
					logger.info("Connected: " + s.getIsConnected());
				}
			}
			getGcmuEndpoints(client);
			for (GlobusEndpoint e : getGlobusEndpointLists().get("GCMU").getGlobusEndpoints()) {
				logger.info(e.getName() + " ");
				logger.info("Activated: " + e.getActivated());
			}


		}	catch (Exception e) {
			logger.error("Got an exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());

			e.printStackTrace();
		}

	}

	/**
	 * Private API that polls the Globus Connect Personal endpoints for a user.
	 */
	private Status getGlobusConnectEndpoints(JSONTransferAPIClient client) {

		String query = "/endpoint_list?";
		query = query + "filter=username:" + client.getUsername();
		query = query + "/is_globus_connect:true";
		String key = "GC";
		GlobusEndpointList globusEndpointList = new GlobusEndpointList();
		this.getGlobusEndpointLists().put(key, globusEndpointList);
		return getEndpoints(client, query, key);

	}

	/**
	 * Private API that polls the Globus Connect Server endpoints for a user.
	 */
	private Status getGcmuEndpoints(JSONTransferAPIClient client) {

		String query = "/endpoint_list?";
		query = query + "filter=username:" + client.getUsername();
		query = query + "/is_globus_connect:false";
		String key = "GCMU";
		GlobusEndpointList globusEndpointList = new GlobusEndpointList();
		this.getGlobusEndpointLists().put(key, globusEndpointList);

		return getEndpoints(client, query, key);

	}

	/**
	 * Private API that polls all the endpoints for a user.
	 */
	private Status getEndpoints(JSONTransferAPIClient client, String query, String endpointType) {

		try {

			JSONTransferAPIClient.Result r = client.getResult(query);
			Map<String, GlobusEndpointList> globusEndpointLists = this.getGlobusEndpointLists();
			GlobusEndpointList globusEndpointList = globusEndpointLists.get(endpointType);
			logger.info("Endpoint Listing " + query + " for " + client.getUsername() + ": ");
			Iterator<?> keys = r.document.keys();
			while(keys.hasNext()) {
				String next = (String) keys.next();
				if(next.equalsIgnoreCase("data_type")) {

					globusEndpointList.setDataType(r.document.getString(next));

				} else if (next.equalsIgnoreCase("length")) {

					globusEndpointList.setLength(new Integer(r.document.getString(next)));

				} else if (next.equalsIgnoreCase("limit")) {

					globusEndpointList.setLimit(r.document.getString(next));

				} else if (next.equalsIgnoreCase("offset")) {

					globusEndpointList.setOffset(r.document.getString(next));

				} else if (next.equalsIgnoreCase("total")) {

					globusEndpointList.setTotal(r.document.getString(next));

				} else if (next.equalsIgnoreCase("data")) {
					JSONArray data = r.document.getJSONArray(next);
					int size = data.length();
					ArrayList<GlobusEndpoint> globusEndpoints = new ArrayList<GlobusEndpoint>();
					for (int j = 0; j < size; j++) {
						GlobusEndpoint globusEndpoint = new GlobusEndpoint();
						JSONObject globusEndpointInfo = data.getJSONObject(j);
						Iterator<?> keys2 = globusEndpointInfo.keys();
						while(keys2.hasNext()) {
							String next2 = (String) keys2.next();
							if(next2.equalsIgnoreCase("data_type")) {

								globusEndpoint.setDataType(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("username")) {

								globusEndpoint.setUserName(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("globus_connect_setup_key")) {

								globusEndpoint.setGlobusConnectSetupKey(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("name")) {

								globusEndpoint.setName(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("activated")) {

								globusEndpoint.setActivated(globusEndpointInfo.getBoolean(next2));

							} else if (next2.equalsIgnoreCase("is_globus_connect")) {

								globusEndpoint.setIsGlobusConnect(globusEndpointInfo.getBoolean(next2));

							} else if (next2.equalsIgnoreCase("ls_link")) {

								JSONObject linkInfo = globusEndpointInfo.getJSONObject(next2);
								GlobusLink lsLink = new GlobusLink();
								Iterator<?> keys3 = linkInfo.keys();
								while(keys3.hasNext()) {

									String next3 = (String) keys3.next();
									if(next3.equalsIgnoreCase("data_type")) {

										lsLink.setDataType(linkInfo.getString(next3));

									} else if (next3.equalsIgnoreCase("href")) {

										lsLink.setHref(linkInfo.getString(next3));

									} else if (next3.equalsIgnoreCase("resource")) {

										lsLink.setResource(linkInfo.getString(next3));

									} else if (next3.equalsIgnoreCase("relationship")) {

										lsLink.setRelationship(linkInfo.getString(next3));

									} else if (next3.equalsIgnoreCase("title")) {

										lsLink.setTitle(linkInfo.getString(next3));

									}

								}
								globusEndpoint.setLsLink(lsLink);

							} else if (next2.equalsIgnoreCase("canonical_name")) {

								globusEndpoint.setCanonicalName(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("myproxy_server")) {

								globusEndpoint.setMyProxyServer(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("expire_time")) {

								//globusEndpoint.setExpireTime(new Date(globusEndpointInfo.getString(next2)));

							} else if (next2.equalsIgnoreCase("public")) {

								globusEndpoint.setGlobusPublic(globusEndpointInfo.getBoolean(next2));

							} else if (next2.equalsIgnoreCase("description")) {

								globusEndpoint.setDescription(globusEndpointInfo.getString(next2));

							} else if (next2.equalsIgnoreCase("data")) {

								JSONArray serverData = globusEndpointInfo.getJSONArray(next2);
								int serverDataSize = serverData.length();
								ArrayList<GlobusServer> globusServers = new ArrayList<GlobusServer>();
								for (int k = 0; k < serverDataSize; k++) {
									GlobusServer globusServer = new GlobusServer();
									JSONObject globusServerInfo = serverData.getJSONObject(k);
									Iterator<?> keys4 = globusServerInfo.keys();
									while(keys4.hasNext()) {
										String next4 = (String) keys4.next();
										if(next4.equalsIgnoreCase("data_type")) {

											globusServer.setDataType(globusServerInfo.getString(next4));

										} else if (next4.equalsIgnoreCase("id")) {

											globusServer.setId(globusServerInfo.getInt(next4));

										} else if (next4.equalsIgnoreCase("hostname")) {

											globusServer.setHostname(globusServerInfo.getString(next4));

										} else if (next4.equalsIgnoreCase("uri")) {

											globusServer.setUri(globusServerInfo.getString(next4));

										} else if (next4.equalsIgnoreCase("scheme")) {

											globusServer.setScheme(globusServerInfo.getString(next4));

										} else if (next4.equalsIgnoreCase("port")) {

											if (globusServerInfo.get("port").toString().equalsIgnoreCase("null")) {

												globusServer.setPort(0);

											} else {

												globusServer.setPort(globusServerInfo.getInt(next4));

											}

										} else if (next4.equalsIgnoreCase("subject")) {

											globusServer.setSubject(globusServerInfo.getString(next4));

										} else if (next4.equalsIgnoreCase("is_connected")) {

											globusServer.setIsConnected(globusServerInfo.getBoolean(next4));

										}

									}
									globusServers.add(globusServer);
								}
								globusEndpoint.setGlobusServers(globusServers);
							}

						}
						globusEndpoints.add(globusEndpoint);
					}
					globusEndpointList.setGlobusEndpoints(globusEndpoints);	
				}

			}

			globusEndpointLists.put(endpointType, globusEndpointList);
			this.setGlobusEndpointLists(globusEndpointLists);
			return Status.OK;

		}	catch (Exception e) {

			logger.error("Got an exception..\n");
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());
			e.printStackTrace();
			return Status.FAIL;

		}


	}

	/**
	 *  @return the globusEndpointLists.
	 */
	public Map<String, GlobusEndpointList> getGlobusEndpointLists() {
		return globusEndpointLists;
	}

	/**
	 * @param globusEndpointLists the globusEndpointLists to set
	 */
	public void setGlobusEndpointLists(Map<String, GlobusEndpointList> globusEndpointLists) {
		this.globusEndpointLists = globusEndpointLists;
	}

}
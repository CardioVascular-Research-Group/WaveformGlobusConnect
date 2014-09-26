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

import java.util.ArrayList;
import org.cvrgrid.waveform.model.GlobusEndpoint;

/*
 * This is a model class for use in the transfer of local Waveform files using Globus Connect.
 * The purpose of this model is to capture results returned from an endpoint_list provided by the 
 * Globus REST API.
 */

public class GlobusEndpointList {

	String dataType;
	Integer length;
	String limit;
	String offset;
	String total;
	ArrayList<GlobusEndpoint> globusEndpoints = new ArrayList<GlobusEndpoint>();
	
	public GlobusEndpointList() {
		
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
	 * @return the length
	 */
	public Integer getLength() {
		return length;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(Integer length) {
		this.length = length;
	}
	/**
	 * @return the limit
	 */
	public String getLimit() {
		return limit;
	}
	/**
	 * @param limit the limit to set
	 */
	public void setLimit(String limit) {
		this.limit = limit;
	}
	/**
	 * @return the offset
	 */
	public String getOffset() {
		return offset;
	}
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(String offset) {
		this.offset = offset;
	}
	/**
	 * @return the total
	 */
	public String getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(String total) {
		this.total = total;
	}
	/**
	 * @return the globusEndpoints
	 */
	public ArrayList<GlobusEndpoint> getGlobusEndpoints() {
		return globusEndpoints;
	}
	/**
	 * @param globusEndpoints the globusEndpoints to set
	 */
	public void setGlobusEndpoints(ArrayList<GlobusEndpoint> globusEndpoints) {
		this.globusEndpoints = globusEndpoints;
	}

}

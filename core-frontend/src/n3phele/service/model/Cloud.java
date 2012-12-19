/**
 * @author Nigel Cook
 *
 * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
 * except in compliance with the License. 
 * 
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 */
package n3phele.service.model;

import java.net.URI;
import java.util.ArrayList;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.Credential;
import n3phele.service.model.core.Entity;
import n3phele.service.model.core.TypedParameter;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Cloud")
@XmlType(name="Cloud", propOrder={"description", "location", "factory", "inputParameters", "outputParameters", "regionPrices"})
@Unindexed
@Cached
public class Cloud extends Entity {
	@Id private Long id;
	private Text description;
	private String location;
	private String factory;
	@Embedded private Credential factoryCredential;
	@Embedded private ArrayList<TypedParameter> inputParameters; 
	@Embedded private ArrayList<TypedParameter> outputParameters;
	@Embedded private Region regionPrices;
	
	public Cloud() {}
	

	public Cloud(String name, String description,
		 URI location, URI factory, Credential factoryCredential, URI owner, boolean isPublic, Region regionPrices) {
		super(name, null, "application/vnd.com.n3phele.Cloud+json", owner, isPublic);
		this.id = null;
		setDescription(description);
		this.location = (location==null)? null : location.toString();
		this.factory = (factory==null)? null : factory.toString();
		this.factoryCredential = factoryCredential;
		this.regionPrices = regionPrices;
	}
	
	/**
	 * @return the id
	 */
	@XmlTransient
	public Long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return (description==null)?null:description.getValue();
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = (description==null)?null:new Text(description);
	}


	/**
	 * @return the location
	 */
	public URI getLocation() {
		URI result = null;
		if(this.location != null)
			result = URI.create(this.location);
		return result;
	}


	/**
	 * @param location the location to set
	 */
	public void setLocation(URI location) {
		this.location = (location == null)? null : location.toString();
	}


	/**
	 * @return the factory
	 */
	public URI getFactory() {
		URI result = null;
		if(this.factory != null) 
			result = URI.create(this.factory);
		return result;
	}


	/**
	 * @param factory the factory to set
	 */
	public void setFactory(URI factory) {
		this.factory = (factory == null)? null : factory.toString();
	}


	/**
	 * @return the factoryCredential
	 */
	@XmlTransient
	public Credential getFactoryCredential() {
		return factoryCredential;
	}


	/**
	 * @param factoryCredential the factoryCredential to set
	 */
	public void setFactoryCredential(Credential factoryCredential) {
		this.factoryCredential = factoryCredential;
	}


	/**
	 * @return the inputParameters
	 */
	public ArrayList<TypedParameter> getInputParameters() {
		return inputParameters;
	}


	/**
	 * @param inputParameters the inputParameters to set
	 */
	public void setInputParameters(ArrayList<TypedParameter> inputParameters) {
		this.inputParameters = inputParameters;
	}


	/**
	 * @return the outputParameters
	 */
	public ArrayList<TypedParameter> getOutputParameters() {
		return outputParameters;
	}


	/**
	 * @param outputParameters the outputParameters to set
	 */
	public void setOutputParameters(ArrayList<TypedParameter> outputParameters) {
		this.outputParameters = outputParameters;
	}


	public static Cloud summary(Cloud c) {
			if(c == null) return null;
			Cloud result = new Cloud(c.name, null, null, null, null, c.getOwner(), c.isPublic, c.getRegionPrices());
			result.uri = c.uri;
			return result;
	}

	public Region getRegionPrices() {
		return this.regionPrices;
	}


	public void setRegionPrices(Region regionPrices) {
		this.regionPrices = regionPrices;
	}
}

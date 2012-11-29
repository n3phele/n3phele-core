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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.Entity;
import n3phele.service.model.core.TypedParameter;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Command")
@XmlSeeAlso({CloudProfile.class})
@XmlType(name="Command", propOrder={"description", "version", "application", "ownerName", "preferred",
		"inputFiles", "executionParameters", 
		"outputFiles", "outputParameters", "cloudProfiles"})
@Unindexed
@Cached
public class Command extends Entity {
	@Id private Long id;
	private Text description;
	private String version;
	private String application;
	@Transient
	private String ownerName;
	@Indexed
	private boolean preferred;
	@Embedded protected ArrayList<FileSpecification> outputFiles;
	@Embedded protected ArrayList<TypedParameter> outputParameters;
	@Embedded protected ArrayList<FileSpecification> inputFiles;
	@Embedded protected ArrayList<TypedParameter> executionParameters;
	@Embedded private ArrayList<CloudProfile> cloudProfiles;
	
	public Command() {super();}
	

	public Command(String name, String description,
			URI user, boolean isPublic, boolean isPreferred,
			String version, URI application, 
			ArrayList<FileSpecification> inputFiles,
			ArrayList<TypedParameter> executionParameters,
			ArrayList<FileSpecification> outputFiles,
			ArrayList<TypedParameter> outputParameters,
			ArrayList<CloudProfile> cloudProfiles) {
		super(name, null, "application/vnd.com.n3phele.Command+json", user, isPublic);
		this.id = null;
		setDescription(description);
		this.version = version;
		this.application = (application != null) ? application.toString():null;
		this.inputFiles = inputFiles;
		this.executionParameters = executionParameters;
		this.outputFiles = outputFiles;
		this.outputParameters = outputParameters;
		this.cloudProfiles = cloudProfiles;
		this.preferred = isPreferred;
	}
	
	public static Command summary(Command c) {
		if(c == null) return null;
		Command result = new Command(c.name, c.description == null? null:c.description.getValue(), c.getOwner(), c.isPublic, c.isPreferred(), c.version, c.getApplication(),
				null,null,null,null,null);
		result.uri = c.uri;
		return result;
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
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @param application the application to set
	 */
	public void setApplication(URI application) {
		if(application != null) {
			this.application = application.toString();
		} else {
			this.application = null;
		}
	}


	/**
	 * @return the application
	 */
	public URI getApplication() {
		URI result = null;
		if(this.application != null) {
			result = URI.create(application);
		}
		return result;
	}
	
	


	/**
	 * @return the outputFiles
	 */
	public ArrayList<FileSpecification> getOutputFiles() {
		return outputFiles;
	}


	/**
	 * @param outputFiles the outputFiles to set
	 */
	public void setOutputFiles(ArrayList<FileSpecification> outputFiles) {
		this.outputFiles = outputFiles;
	}




	/**
	 * @return the inputFiles
	 */
	public ArrayList<FileSpecification> getInputFiles() {
		return inputFiles;
	}


	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputFiles(ArrayList<FileSpecification> inputFiles) {
		this.inputFiles = inputFiles;
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


	/**
	 * @return the executionParameters
	 */
	public ArrayList<TypedParameter> getExecutionParameters() {
		return executionParameters;
	}


	/**
	 * @param executionParameters the executionParameters to set
	 */
	public void setExecutionParameters(ArrayList<TypedParameter> executionParameters) {
		this.executionParameters = executionParameters;
	}


	/**
	 * @return the cloudProfiles
	 */
	public ArrayList<CloudProfile> getCloudProfiles() {
		return cloudProfiles;
	}


	/**
	 * @param cloudProfiles the cloudProfiles to set
	 */
	public void setCloudProfiles(ArrayList<CloudProfile> cloudProfiles) {
		this.cloudProfiles = cloudProfiles;
	}


	/**
	 * @return the ownerName
	 */
	public String getOwnerName() {
		return this.ownerName;
	}


	/**
	 * @param ownerName the ownerName to set
	 */
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	/**
	 * @return the preferred visibility
	 */
	public boolean isPreferred() {
		return preferred;
	}
	
	/**
	 * @return the preferred visibility
	 */
	public boolean getPreferred() {
		return preferred;
	}

	/**
	 * @param preferred the preferred visibility to set
	 */
	public void setPreferred(boolean preferred) {
		this.preferred = preferred;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("Command [id=%s, description=%s, version=%s, application=%s, ownerName=%s, preferred=%s, outputFiles=%s, outputParameters=%s, inputFiles=%s, executionParameters=%s, cloudProfiles=%s]",
						this.id, this.description, this.version,
						this.application, this.ownerName, this.preferred,
						this.outputFiles, this.outputParameters,
						this.inputFiles, this.executionParameters,
						this.cloudProfiles);
	}


}

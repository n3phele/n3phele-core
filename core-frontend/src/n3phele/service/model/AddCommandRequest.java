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
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.TypedParameter;

@XmlRootElement(name="AddCommandRequest")
@XmlType(name="AddCommandRequest", propOrder={"name", "description", "public", "preferred", "version", "application", "inputFiles", "executionParameters", "outputFiles", "outputParameters"})
public class AddCommandRequest {
	private String name;
	private String description;
	private boolean isPublic;
	private boolean isPreferred;
	private String version;
	private URI application;
	private List<FileSpecification> inputFiles;
	private List<TypedParameter> executionParameters;
	private List<FileSpecification> outputFiles;
	private List<TypedParameter> outputParameters;
	
	public AddCommandRequest() {}

	/**
	 * @param name
	 * @param description
	 * @param isPublic
	 * @param isPreferred
	 * @param version
	 * @param application
	 * @param inputFiles
	 * @param executionParameters
	 * @param outputFiles
	 * @param outputParameters
	 */
	public AddCommandRequest(String name, String description, boolean isPublic,
			boolean isPreferred, String version,
			URI application, List<FileSpecification> inputFiles,
			List<TypedParameter> executionParameters,
			List<FileSpecification> outputFiles,
			List<TypedParameter> outputParameters) {
		super();
		this.name = name;
		this.description = description;
		this.isPublic = isPublic;
		this.isPreferred = isPreferred;
		this.version = version;
		this.application = application;
		this.inputFiles = inputFiles;
		this.executionParameters = executionParameters;
		this.outputFiles = outputFiles;
		this.outputParameters = outputParameters;
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
	 * @return the isPublic
	 */
	public boolean getPublic() {
		return isPublic;
	}
	
	/**
	 * @return the isPublic
	 */
	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the isPreferred
	 */
	public boolean isPreferred() {
		return this.isPreferred;
	}
	
	/**
	 * @return the preferred state
	 */
	public boolean getPreferred() {
		return this.isPreferred;
	}

	/**
	 * @param isPreferred the isPreferred to set
	 */
	public void setPreferred(boolean isPreferred) {
		this.isPreferred = isPreferred;
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
	 * @return the application
	 */
	public URI getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(URI application) {
		this.application = application;
	}

	/**
	 * @return the inputFiles
	 */
	public List<FileSpecification> getInputFiles() {
		return inputFiles;
	}

	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputFiles(List<FileSpecification> inputFiles) {
		this.inputFiles = inputFiles;
	}

	/**
	 * @return the executionParameters
	 */
	public List<TypedParameter> getExecutionParameters() {
		return executionParameters;
	}

	/**
	 * @param executionParameters the executionParameters to set
	 */
	public void setExecutionParameters(List<TypedParameter> executionParameters) {
		this.executionParameters = executionParameters;
	}

	/**
	 * @return the outputFiles
	 */
	public List<FileSpecification> getOutputFiles() {
		return outputFiles;
	}

	/**
	 * @param outputFiles the outputFiles to set
	 */
	public void setOutputFiles(List<FileSpecification> outputFiles) {
		this.outputFiles = outputFiles;
	}

	/**
	 * @return the outputParameters
	 */
	public List<TypedParameter> getOutputParameters() {
		return outputParameters;
	}

	/**
	 * @param outputParameters the outputParameters to set
	 */
	public void setOutputParameters(List<TypedParameter> outputParameters) {
		this.outputParameters = outputParameters;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("AddCommandRequest [name=%s, description=%s, isPublic=%s, isPreferred=%s, version=%s, application=%s, inputFiles=%s, executionParameters=%s, outputFiles=%s, outputParameters=%s]",
						this.name, this.description, this.isPublic,
						this.isPreferred, this.version, this.application,
						this.inputFiles, this.executionParameters,
						this.outputFiles, this.outputParameters);
	}

	
}
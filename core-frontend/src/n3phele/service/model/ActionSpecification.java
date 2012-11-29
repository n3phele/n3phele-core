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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.TypedParameter;

@XmlRootElement(name="ActionSpecification")
@XmlType(name="ActionSpecification", propOrder={"name", "description", "workloadKey", "action", "inputFiles", "inputParameters", "outputFiles", "outputParameters"})
public class ActionSpecification implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	private String workloadKey;
	private String action;
	private ArrayList<FileSpecification> inputFiles;
	private ArrayList<TypedParameter> inputParameters;
	private ArrayList<FileSpecification> outputFiles;
	private ArrayList<TypedParameter> outputParameters;

	public ActionSpecification() {}


	/**
	 * @param name
	 * @param description
	 * @param workloadKey
	 * @param action
	 * @param inputFiles
	 * @param inputParameters
	 * @param outputFiles
	 * @param outputParameters
	 */
	public ActionSpecification(String name, String description, String workloadKey, URI action, ArrayList<FileSpecification> inputFiles,
			ArrayList<TypedParameter> inputParameters, ArrayList<FileSpecification> outputFiles,
			ArrayList<TypedParameter> outputParameters) {
		super();
		this.name = name;
		this.description = description;
		this.workloadKey = workloadKey;
		this.action = (action==null)?null:action.toString();
		this.inputFiles = inputFiles;
		this.inputParameters = inputParameters;
		this.outputFiles = outputFiles;
		this.outputParameters = outputParameters;
	}
	
	public ActionSpecification(ActionSpecification deepCopy) {
		super();
		this.name = deepCopy.name;
		this.description = deepCopy.description;
		this.workloadKey = deepCopy.workloadKey;
		this.action = deepCopy.action;
		this.inputFiles = new ArrayList<FileSpecification>(); 
		if(deepCopy.inputFiles != null) {
			for(FileSpecification f : deepCopy.inputFiles) {
				this.inputFiles.add(new FileSpecification(f));
			}
		}
		this.inputParameters = new ArrayList<TypedParameter>(); 
		if(deepCopy.inputParameters != null) {
			for(TypedParameter f : deepCopy.inputParameters) {
				this.inputParameters.add(new TypedParameter(f));
			}
		}
		this.outputFiles = new ArrayList<FileSpecification>(); 
		if(deepCopy.outputFiles != null) {
			for(FileSpecification f : deepCopy.outputFiles) {
				this.outputFiles.add(new FileSpecification(f));
			}
		}
		this.outputParameters = new ArrayList<TypedParameter>();
		if(deepCopy.outputParameters != null) {
			for(TypedParameter f : deepCopy.outputParameters) {
				this.outputParameters.add(new TypedParameter(f));
			}
		}
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
	 * @return the workloadKey
	 */
	public String getWorkloadKey() {
		return workloadKey;
	}


	/**
	 * @param workloadKey the workloadKey to set
	 */
	public void setWorkloadKey(String workloadKey) {
		this.workloadKey = workloadKey;
	}


	/**
	 * @return the action
	 */
	public URI getAction() {
		return (action==null)?null:URI.create(action);
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(URI action) {
		this.action = (action==null)?null:action.toString();
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


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 20;
		return String
				.format("ActionSpecification [name=%s, description=%s, workloadKey=%s, action=%s, inputFiles=%s, inputParameters=%s, outputFiles=%s, outputParameters=%s]",
						name,
						description,
						workloadKey,
						action,
						inputFiles != null ? inputFiles.subList(0,
								Math.min(inputFiles.size(), maxLen)) : null,
						inputParameters != null ? inputParameters.subList(0,
								Math.min(inputParameters.size(), maxLen))
								: null,
						outputFiles != null ? outputFiles.subList(0,
								Math.min(outputFiles.size(), maxLen)) : null,
						outputParameters != null ? outputParameters.subList(0,
								Math.min(outputParameters.size(), maxLen))
								: null);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((inputFiles == null) ? 0 : inputFiles.hashCode());
		result = prime * result
				+ ((inputParameters == null) ? 0 : inputParameters.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((outputFiles == null) ? 0 : outputFiles.hashCode());
		result = prime
				* result
				+ ((outputParameters == null) ? 0 : outputParameters.hashCode());
		result = prime * result
				+ ((workloadKey == null) ? 0 : workloadKey.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionSpecification other = (ActionSpecification) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (inputFiles == null) {
			if (other.inputFiles != null)
				return false;
		} else if (!inputFiles.equals(other.inputFiles))
			return false;
		if (inputParameters == null) {
			if (other.inputParameters != null)
				return false;
		} else if (!inputParameters.equals(other.inputParameters))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (outputFiles == null) {
			if (other.outputFiles != null)
				return false;
		} else if (!outputFiles.equals(other.outputFiles))
			return false;
		if (outputParameters == null) {
			if (other.outputParameters != null)
				return false;
		} else if (!outputParameters.equals(other.outputParameters))
			return false;
		if (workloadKey == null) {
			if (other.workloadKey != null)
				return false;
		} else if (!workloadKey.equals(other.workloadKey))
			return false;
		return true;
	}

	
}

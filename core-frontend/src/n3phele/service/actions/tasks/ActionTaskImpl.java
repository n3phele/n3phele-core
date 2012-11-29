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
package n3phele.service.actions.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

import n3phele.service.core.NotFoundException;
import n3phele.service.model.Action;
import n3phele.service.model.ActionState;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.rest.impl.ActionResource;
import n3phele.service.rest.impl.ActionResource.ActionManager;

@XmlRootElement(name="ActionTaskImpl")
@XmlType(name="ActionTaskImpl", propOrder={"name", "description", "workloadKey", "state", "start", "complete", "parent", "parentAction", "inputFiles", "executionParameters", "outputFiles", "outputParameters",
		"dependentOn", "dependencyFor", "progress", "manifest"})
@XmlSeeAlso({CreateVmTask.class, ExecuteCommandTask.class, FileTransferTask.class })

@Unindexed
@Cached
public class ActionTaskImpl {
	private final static Logger log = Logger.getLogger(ActionTaskImpl.class.getName());
	protected String name;
	protected String description;
	protected Date start;
	protected Date complete;
	protected String parent;
	protected String parentAction;
	@Embedded protected ArrayList<FileRef> outputFiles;
	@Embedded protected ArrayList<TypedParameter> outputParameters;
	@Embedded protected ArrayList<FileRef> inputFiles;
	@Embedded protected ArrayList<TypedParameter> executionParameters;
	protected ActionState state;
	protected ArrayList<String> dependentOn;
	protected ArrayList<String> dependencyFor;
	protected String workloadKey;
	protected String[] manifest;
	
	
	protected String progress;
	
	public ActionTaskImpl() {state = ActionState.INIT; }
	
	public ActionTaskImpl(String name, String description, String workloadKey,
			ArrayList<FileRef> inputFiles, ArrayList<TypedParameter> executionParameters,
			ArrayList<FileRef> outputFiles, ArrayList<TypedParameter> outputParameters) {
		this.state = ActionState.INIT;
		this.name = name;
		this.description = description;
		this.workloadKey = workloadKey;
		this.inputFiles = inputFiles;
		this.executionParameters = executionParameters;
		this.outputFiles = outputFiles;
		this.outputParameters = outputParameters;
	}
	
	static final int MAXSTRING = 1000000;
	public void updateOutputParameter(String name, String value) {
		if(outputParameters != null) {
			for(TypedParameter p : outputParameters) {
				if(p.getName().equals(name)) {
					if(value != null && value.length()> MAXSTRING)
						p.setValue(value.substring(0, MAXSTRING-1));
					else
						p.setValue(value);
					return;
				}
			}
		}
		log.info("Unknown parameter "+name);
	}
	
	protected String getExecutionParameterValue(String name) {
		if(executionParameters != null) {
			for(TypedParameter p : executionParameters) {
				if(p.getName().equals(name)) {
					String value = p.value();
					if(value == null || value == "") {
						value = p.defaultValue();
					}
					if(value == null) value = "";
					return value;
				}
			}
		}
		log.warning("Unknown parameter "+name);
		return null;
	}
	
	public boolean setExecutionParameter(String name, String value) {
		TypedParameter p = getExecutionParameter(name);
		if(p != null) {
			p.setValue(value);
		}
		return p != null;
	}
	
	public TypedParameter getExecutionParameter(String name) {
		if(executionParameters != null) {
			for(TypedParameter p : executionParameters) {
				if(p.getName().equals(name)) {
					return p;
				}
			}
		}
		log.warning("Unknown parameter "+name);
		return null;
	}
	
	public TypedParameter getOutputParameter(String name) {
		if(outputParameters != null) {
			for(TypedParameter p : outputParameters) {
				if(p.getName().equals(name)) {
					return p;
				}
			}
		}
		log.warning("Unknown parameter "+name);
		return null;
	}
	
	protected String getOutputParameterValue(String name) {
		TypedParameter result = getOutputParameter(name);
		if(result != null) {
			return result.value();
		}
		return null;
	}
	
	public void hasDependencyOn(URI actionTaskUri) {
		if(dependentOn == null) dependentOn = new ArrayList<String>();
		if(!dependentOn.contains(actionTaskUri.toString()))
			dependentOn.add(actionTaskUri.toString());
	}
	
	public void dependencyFor(URI actionTaskUri) {
		if(dependencyFor == null) dependencyFor = new ArrayList<String>();
		if(!dependencyFor.contains(actionTaskUri.toString()))
			dependencyFor.add(actionTaskUri.toString());
	}
	
	public boolean isAwaitingDependencies() {
		if(state == ActionState.BLOCKED) {
			if(dependentOn != null) {
				ActionManager actionManager = new ActionResource.ActionManager();
				for(String s : dependentOn) {
					Action a;
					try {
						a = actionManager.load(URI.create(s));
					} catch (NotFoundException e) {
						return true;
					}
					ActionState state = a.getActionTask().getState();
					if(state != ActionState.COMPLETE) {
						return true;
					}
				}
			}
			state = ActionState.PENDING;
			return false;
		}
		return false;
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
	 * @return the start
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
		this.start = start;
	}



	/**
	 * @return the date processing was complete
	 */
	public Date getComplete() {
		return complete;
	}

	/**
	 * @param complete the date processing was complete
	 */
	public void setComplete(Date complete) {
		this.complete = complete;
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
	 * @return the parent
	 */
	public URI getParent() {
		return (this.parent==null)?null:URI.create(this.parent);
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(URI parent) {
		this.parent = (parent==null)?null:parent.toString();
	}

	

	/**
	 * @return the parentAction
	 */
	public URI getParentAction() {
		return (parentAction==null)?null:URI.create(parentAction);
	}

	/**
	 * @param parentAction the parentAction to set
	 */
	public void setParentAction(URI parentAction) {
		this.parentAction = (parentAction==null)?null:parentAction.toString();
	}

	/**
	 * @return the outputFiles
	 */
	public ArrayList<FileRef> getOutputFiles() {
		return outputFiles;
	}

	/**
	 * @param outputFiles the outputFiles to set
	 */
	public void setOutputFiles(ArrayList<FileRef> outputFiles) {
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

	/**
	 * @return the inputFiles
	 */
	public ArrayList<FileRef> getInputFiles() {
		return inputFiles;
	}

	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputFiles(ArrayList<FileRef> inputFiles) {
		this.inputFiles = inputFiles;
	}

	/**
	 * @return the dependentOn
	 */
	public ArrayList<URI> getDependentOn() {
		if(dependentOn == null) {
			return new ArrayList<URI>();
		} else {
			ArrayList<URI> result = new ArrayList<URI>(dependentOn.size());
			for(String s : dependentOn) {
				result.add(URI.create(s));
			}
			return result;
		}
		
	}
	

	/**
	 * @return the progress
	 */
	public URI getProgress() {
		return (progress==null)?null:URI.create(progress);
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(URI progress) {
		this.progress = (progress==null)?null:progress.toString();
	}

	/**
	 * @param dependentOn the dependentOn to set
	 */
	public void setDependentOn(ArrayList<URI> dependentOn) {
		if(dependentOn == null) {
			this.dependentOn = null;
		} else {
			ArrayList<String> result = new ArrayList<String>(dependentOn.size());
			for(URI s : dependentOn) {
				result.add(s.toString());
			}
			this.dependentOn = result;
		}
	}

	/**
	 * @return the dependencyFor
	 */
	public ArrayList<URI> getDependencyFor() {
		if(dependencyFor == null) {
			return new ArrayList<URI>();
		} else {
			ArrayList<URI> result = new ArrayList<URI>(dependencyFor.size());
			for(String s : dependencyFor) {
				result.add(URI.create(s));
			}
			return result;
		}
		
	}

	/**
	 * @param dependencyFor the dependencyFor to set
	 */
	public void setDependencyFor(ArrayList<URI> dependencyFor) {
		if(dependencyFor == null) {
			this.dependencyFor = null;
		} else {
			ArrayList<String> result = new ArrayList<String>(dependentOn.size());
			for(URI s : dependencyFor) {
				result.add(s.toString());
			}
			this.dependencyFor = result;
		}
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
	 * @return the state
	 */
	public ActionState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(ActionState state) {
		this.state = state;
	}

	/**
	 * @return the manifest
	 */
	public String[] getManifest() {
		return this.manifest;
	}

	/**
	 * @param manifest the manifest to set
	 */
	public void setManifest(String[] manifest) {
		this.manifest = manifest;
	}
	
}

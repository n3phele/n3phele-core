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
import java.util.Arrays;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.Entity;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.User;
import n3phele.service.rest.impl.CommandResource;

import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;


@XmlRootElement(name="Activity")
@XmlType(name="Activity", propOrder={"command", "cloudProfileId", "account", "parameters", "inputs", "outputs", "actions", 
		"state", "progress", "fileTable", "context", "notify", "origin"})
@Unindexed
//@Cached
public class Activity extends Entity  {
	private final static Logger log = Logger.getLogger(Activity.class.getName()); 
	@Id private Long id;
	private String command;
	private String account;
	private Long cloudProfileId;
	@Embedded private ArrayList<NameValue> parameters;
	@Embedded private ArrayList<FileSpecification> inputs;
	@Embedded private ArrayList<FileSpecification> outputs;
	@Embedded private ArrayList<FileMonitor> fileTable;
	@Embedded private ArrayList<TypedParameter> context;
	@Indexed private ArrayList<String> origin;
	private String progress;
	private ArrayList<String> actions;
	private boolean notify;
	private ActionState state = ActionState.INIT;
	
	public Activity() {};
	
	/**
	 * @param name activity instance name
	 * @param notify send completion email if true
	 * @param requestor the user executing the command
	 * @param command URI of the command to be performed by the activity
	 * @param account history account to host the command execution
	 * @param cloudProfileId id of the cloudProfile associated with command to be used
	 * @param parameters environmental parameters to pass to the command execution
	 * @param inputs input files to be processed by the command
	 * @param outputs output files locations for information preservation after command execution
	 */
	public Activity(String name, Boolean notify, User requestor, URI command, URI account, long cloudProfileId, NameValue[] parameters, 
			FileSpecification[] inputs, FileSpecification[] outputs) {
		super("activity", null, "application/vnd.com.n3phele.Activity+json", requestor.getUri(), false);
		if(name==null || name.equals("")) {
			super.setName(new CommandResource.CommandManager().load(command, requestor).getName());
		} else {
			super.setName(name);
		}

		this.id = null;
		this.notify = notify;
		this.command = (command==null)?null:command.toString();
		this.account = (account==null)?null:account.toString();
		this.cloudProfileId = cloudProfileId;
		this.parameters = new ArrayList<NameValue>();
		if(parameters != null)
			this.parameters.addAll(Arrays.asList(parameters));
		this.inputs = new ArrayList<FileSpecification>();
		if(inputs != null)
			this.inputs.addAll(Arrays.asList(inputs));
		this.outputs = new ArrayList<FileSpecification>();
		if(outputs != null)
			this.outputs.addAll(Arrays.asList(outputs));
		log.fine(String.format("Created activity %s for %s", this.getName(), requestor.getName()));
	}
	
	

	
	/*
	 * Getters and Setters
	 * -------------------
	 */
	
	
	
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
	 * @return the notify
	 */
	public boolean isNotify() {
		return notify;
	}
	
	/**
	 * @return the notify
	 */
	public boolean getNotify() {
		return notify;
	}

	/**
	 * @param notify the notify to set
	 */
	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	/**
	 * @return the command
	 */
	public URI getCommand() {
		return (command==null)?null:URI.create(command);
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(URI command) {
		this.command = (command==null)?null:command.toString();
	}

	/**
	 * @return the account
	 */
	public URI getAccount() {
		return (account==null)?null:URI.create(account);
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(URI account) {
		this.account = (account==null)?null:account.toString();
	}

	/**
	 * @return the cloudProfileId
	 */
	public Long getCloudProfileId() {
		return cloudProfileId;
	}

	/**
	 * @param cloudProfileId the cloudProfileId to set
	 */
	public void setCloudProfileId(Long cloudProfileId) {
		this.cloudProfileId = cloudProfileId;
	}

	/**
	 * @return the parameters
	 */
	public ArrayList<NameValue> getParameters() {
		if(parameters == null) parameters = new ArrayList<NameValue>();
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(ArrayList<NameValue> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the inputs
	 */
	public ArrayList<FileSpecification> getInputs() {
		if(inputs == null) inputs = new ArrayList<FileSpecification>();
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(ArrayList<FileSpecification> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @return the outputs
	 */
	public ArrayList<FileSpecification> getOutputs() {
		if(outputs == null) outputs = new ArrayList<FileSpecification>();
		return outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(ArrayList<FileSpecification> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @return the fileTable
	 */
	public ArrayList<FileMonitor> getFileTable() {
		if(fileTable == null) fileTable = new ArrayList<FileMonitor>();
		return fileTable;
	}

	/**
	 * @param fileTable the fileTable to set
	 */
	public void setFileTable(ArrayList<FileMonitor> fileTable) {
		this.fileTable = fileTable;
	}

	/**
	 * @return the context
	 */
	public ArrayList<TypedParameter> getContext() {
		if(context == null) context = new ArrayList<TypedParameter>();
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(ArrayList<TypedParameter> context) {
		this.context = context;
	}

	/**
	 * @return the progress
	 */
	public URI getProgress() {
		return (progress==null)?null:URI.create(progress);
	}

	/**
	 * @param (uri the progress to set
	 */
	public void setProgress(URI uri) {
		this.progress = (uri==null)?null:uri.toString();
	}

	/**
	 * @return the actions
	 */
	public ArrayList<URI> getActions() {
		if(actions == null) return new ArrayList<URI>();
		ArrayList<URI> result = new ArrayList<URI>();
		for(String s : actions) {
			result.add(URI.create(s));
		}
		return result;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(ArrayList<URI> actions) {
		this.actions = (actions == null)?null:new ArrayList<String>(actions.size());
		if(actions != null) {
			for(URI u : actions) {
				this.actions.add(u.toString());
			}
		}
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
	 * @return the origin
	 */
	public ArrayList<String> getOrigin() {
		return this.origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(ArrayList<String> origin) {
		this.origin = origin;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("Activity [id=%s, command=%s, account=%s, cloudProfileId=%s, parameters=%s, inputs=%s, outputs=%s, fileTable=%s, context=%s, origin=%s, progress=%s, actions=%s, notify=%s, state=%s]",
						this.id, this.command, this.account,
						this.cloudProfileId, this.parameters, this.inputs,
						this.outputs, this.fileTable, this.context,
						this.origin, this.progress, this.actions, this.notify,
						this.state);
	}	

	
}

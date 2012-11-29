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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import n3phele.service.expression.StringEscapeUtils;
import n3phele.service.model.ActionState;
import n3phele.service.model.core.CommandRequest;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.Task;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.rest.impl.HistoryResource;

@XmlRootElement(name="ExecuteCommandTask")
@XmlType(name="ExecuteCommandTask", propOrder={"started", "duration", "instanceId", "clientUnresponsive" })
@Unindexed
@Cached
public class ExecuteCommandTask extends ActionTaskImpl implements ActionTask {
	private static Logger log = Logger.getLogger(ExecuteCommandTask.class.getName());
	@Id private Long id;
	private Long started = 0L;
	private Long duration = 0L;
	private String instanceId;
	private Long clientUnresponsive = null;
	@Embedded private Credential clientCredential;
		
	
	public ExecuteCommandTask() {
		super();
	}
	
	public ExecuteCommandTask(String name, String description, String workloadKey,
			ArrayList<FileRef> inputFiles, ArrayList<TypedParameter> executionParameters,
			ArrayList<FileRef> outputFiles, ArrayList<TypedParameter> outputParameters) {
		super(name, description, workloadKey, inputFiles, executionParameters, outputFiles, outputParameters);
	}
	

	public ExecuteCommandTask(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean call() throws Exception {
		if(this.state == ActionState.INIT)
			return false;
		if(isAwaitingDependencies())
			return false;
		if(this.clientCredential == null) {
			this.clientCredential = new Credential(
					getExecutionParameterValue("agentUser"), 
					getExecutionParameterValue("agentPassword"));
		}
		Client client = ClientFactory.create();
		if(this.clientCredential == null) {
			log.severe("Null client credential");
		}
		Credential plain = this.clientCredential;
		client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
		client.setReadTimeout(30000);
		client.setConnectTimeout(5000);
		WebResource resource;
		try {
			switch(state) {
			case INIT:
			case BLOCKED:
			case CANCELLED:
			case COMPLETE:
			case FAILED:
			default:
				return false;
	
			case PENDING:
				started = Calendar.getInstance().getTimeInMillis();
				resource = client.resource(getExecutionParameterValue("agentBaseUrl"));
				CommandRequest form = new CommandRequest();
				form.setCmd(StringEscapeUtils.unescapeJavaString(getExecutionParameterValue("command")));
				form.setStdin(StringEscapeUtils.unescapeJavaString(getExecutionParameterValue("stdin")));
				form.setNotification(UriBuilder.fromUri(this.parentAction).scheme("http").path("event").build());
				List<FileRef> outputs = this.getOutputFiles();
				if(outputs != null && outputs.size() != 0)
					form.setFiles(this.getOutputFiles().toArray(new FileRef[outputs.size()]));
				try {
					ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, form);
	
					URI location = response.getLocation();
					if(location != null) {
						this.instanceId = location.toString();
						state = ActionState.RUNNING;
						log.fine(this.name+" command execution started. Factory "+location.toString()+" initiating status "+response.getStatus());
						ActionLog.name(this).info("command execution started.");
	
					} else {
						log.log(Level.SEVERE, this.name+" command execution initiation FAILED with status "+response.getStatus());
						ActionLog.name(this).error("command execution initiation FAILED with status "+response.getStatus());
						state = ActionState.FAILED;
					}	
				} catch (Exception e) {
					log.log(Level.SEVERE, "Command exception", e);
					long now = Calendar.getInstance().getTimeInMillis();
					// Give the agent a 5 minute grace period to respond
					if((now - started) > 5*60*1000L) {
						ActionLog.name(this).error("command execution initiation FAILED with exception "+e.getMessage());
						log.log(Level.SEVERE, this.name+" command execution initiation FAILED with exception ", e);
						state = ActionState.FAILED;
					} 
				} 
				break;
				
			case RUNNING:
	
				resource = client.resource(this.instanceId);
				Task t;
				try {
					t = resource.get(Task.class);
					clientUnresponsive = null;
				} catch (ClientHandlerException e) {
					if(clientUnresponsive == null) {
						clientUnresponsive = Calendar.getInstance().getTimeInMillis();
					} else {
						long now = Calendar.getInstance().getTimeInMillis();
						// Give the agent a 5 minute grace period to respond
						// FIXME ,, extended way out
						if((now - clientUnresponsive) > 30*60*1000L) {
							ActionLog.name(this).error("command execution FAILED with exception "+e.getMessage());
							log.log(Level.SEVERE, this.name+" command execution FAILED with exception ", e);
							state = ActionState.FAILED;
							return true;
						} else {
							log.log(Level.SEVERE, this.name+" command execution has exception.Will retry ", e);
							ActionLog.name(this).error("command execution. Will retry "+e.getMessage());
						}	
					} 
					return false;
				} catch (Exception e) {
					if(clientUnresponsive == null) {
						clientUnresponsive = Calendar.getInstance().getTimeInMillis();
					} else {
						long now = Calendar.getInstance().getTimeInMillis();
						// Give the agent a 5 minute grace period to respond
						// FIXME ,, extended way out
						if((now - clientUnresponsive) > 30*60*1000L) {
							ActionLog.name(this).error("command execution FAILED with exception "+e.getMessage());
							log.log(Level.SEVERE, this.name+" command execution FAILED with exception ", e);
							state = ActionState.FAILED;
							return true;
						} else {
							log.log(Level.SEVERE, this.name+" command execution has exception.Will retry ", e);
							ActionLog.name(this).error("command execution. Will retry "+e.getMessage());
						}	
						
					}
					return false;
				}
				updateOutputParameter("stdout", t.getStdout());
				updateOutputParameter("stderr", t.getStderr());
				if(t.getFinished() != null) {
					updateOutputParameter("exitCode", Integer.toString(t.getExitcode()));
					if(t.getExitcode() == 0) {
						duration = (Calendar.getInstance().getTimeInMillis() - started)/1000;
						addDuration(duration);
						if(t.getStdout() != null && t.getStdout().length() > 0)
							ActionLog.name(this).info("Stdout:"+t.getStdout());
						if(t.getStderr() != null && t.getStderr().length() > 0)
							ActionLog.name(this).warning("Stderr:"+t.getStderr());
						if(t.getManifest() != null)
							this.setOutputFiles(new ArrayList<FileRef>(Arrays.asList(t.getManifest())));
						state = ActionState.COMPLETE;
						log.fine(this.name+" command execution completed successfully. Elapsed time "+duration+" seconds.");
						ActionLog.name(this).info("command execution completed successfully. Elapsed time "+duration+" seconds.");
					} else {
						if(t.getStdout() != null && t.getStdout().length() > 0)
							ActionLog.name(this).info("Stdout:"+t.getStdout());
						if(t.getStderr() != null && t.getStderr().length() > 0)
							ActionLog.name(this).warning("Stderr:"+t.getStderr());
						ActionLog.name(this).error("Command execution "+resource.getURI()+" failed with exit status "+t.getExitcode());
						log.severe(name+" command execution "+resource.getURI()+" failed with exit status "+t.getExitcode());
						log.severe("Stdout: "+t.getStdout());
						log.severe("Stderr: "+t.getStderr());
						state = ActionState.FAILED;
					}
				} else {
					return false;
				}
				break;
			}
			return true;
		} finally {
			ClientFactory.give(client);
		}

	}
	
	

	/* (non-Javadoc)
	 * @see n3phele.service.actions.tasks.ActionTask#cleanup(java.util.Map)
	 */
	@Override
	public void cleanup() throws Exception {
		cancel();
		
	}
	
	@Override
	public void stop() throws Exception {
		cancel();
	}

	@Override
	public int progress() {
		if(this.state == ActionState.CANCELLED ||
				   this.state == ActionState.COMPLETE ||
				   this.state == ActionState.FAILED)
					return 1000;
		if(getDuration() == 0 || this.state == ActionState.INIT || this.state == ActionState.BLOCKED)
			return 0;
		long togo = this.duration - ((Calendar.getInstance().getTimeInMillis()-started)/1000);
		long percentx10 = (togo * 1000) / this.duration;
		if(percentx10 <= 0) percentx10 = 1;
		return (int) (1000L - percentx10);
	}


	@Override
	public void cancel() {
		if(instanceId != null) {
			log.info("Cleanup of "+instanceId);
			Client client = ClientFactory.create();
			try {
				Credential plain = this.clientCredential;
				client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
				WebResource resource = client.resource(instanceId);
				ClientResponse response = resource.delete(ClientResponse.class);
				log.info("Delete status is "+response.getStatus());
			} finally {
				ClientFactory.give(client);
			}
		} else {
			log.info("Cleanup has Instance id that is null");
		}
	}


	/**
	 * @return the duration
	 */
	public long getDuration() {
		if(duration == null || duration == 0){
			String key = makeKey();
			if(workloadKey==null || workloadKey.length()==0)
				this.workloadKey = this.getName();
			duration = new HistoryResource.HistoryManager().getNinetyPercentile(this.workloadKey, this.getName(), key);
			if(duration == 0)
				duration = 90L;
		}

		return duration;
	}
	
	/**
	 * @return the duration
	 */
	public void addDuration(long actual) {
		if(actual == 0)
			actual = 1;
		String key = makeKey();
		if(workloadKey==null || workloadKey.length()==0)
			this.workloadKey = this.getName();

		new HistoryResource.HistoryManager().addSample(actual, this.workloadKey, this.getName(), key);
	}
	
	/*
	 * Getters and Setters
	 * ------------------
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
	 * @return the instanceId
	 */
	public URI getInstanceId() {
		return (this.instanceId==null)?null:URI.create(this.instanceId);
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(URI instanceId) {
		this.instanceId = (instanceId==null)?null:instanceId.toString();
	}

	/**
	 * @return the clientCredential
	 */
	@XmlTransient
	public Credential getClientCredential() {
		return clientCredential;
	}

	/**
	 * @param clientCredential the clientCredential to set
	 */
	public void setClientCredential(Credential clientCredential) {
		this.clientCredential = clientCredential;
	}



	/**
	 * @return the RUNNING
	 */
	public Long getStarted() {
		return started;
	}



	/**
	 * @param RUNNING the RUNNING to set
	 */
	public void setStarted(Long started) {
		this.started = started;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}



	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}


	/**
	 * @return the clientUnresponsive
	 */
	public Long getClientUnresponsive() {
		return clientUnresponsive;
	}

	/**
	 * @param clientUnresponsive the clientUnresponsive to set
	 */
	public void setClientUnresponsive(Long clientUnresponsive) {
		this.clientUnresponsive = clientUnresponsive;
	}

	private String makeKey() {
		List<FileRef> files = getInputFiles();
		long sum = 0;
		if(files != null) {
			for(FileRef f : files) {
				sum += f.getLength();
			}
		}
		if(sum > 0)
			return makeKey(sum);
		else
			return makeKey(null);
	}
	private String makeKey(Long size) {
		if(size == null)
			return "0";
		if(size < 1024*1024) {
			return magnitude(size/1024)+"k";
		}
		if(size < 1024*1024*1024) {
			return magnitude(size/(1024*1024))+"m";
		}
		return magnitude(size/(1024*1024*1024))+"g";
	}

	private long magnitude(long x) {
		int i = 1;
		while(x > 0) {
			if(x < 10) {
				if(x > 2 && x < 7)
					i = i * 5;
				else if(x > 7)
					i = 1 * 10;
				x = 0;
			} else {
				x = x / 10;
				i = i * 10;
			}
		}
		return i;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("ExecuteCommandTask [id=%s, started=%s, duration=%s, instanceId=%s, clientUnresponsive=%s, clientCredential=%s]",
						id, started, duration, instanceId, clientUnresponsive,
						clientCredential);
	}



	public final static TypedParameter[] defaultInputParameter = {
		new TypedParameter("command", "shell command", ParameterType.String, "", "echo no command specified"),
		new TypedParameter("stdin", "task execution stdin", ParameterType.String, "", ""),
		new TypedParameter("agentUser", "username for authenticated access to the vm agent", ParameterType.Secret, "", "n3ph"),
		new TypedParameter("agentPassword", "password for authenticated access to the vm agent", ParameterType.Secret, "", "3l3hp3n"),
		new TypedParameter("agentBaseUrl", "url for access to the vm agent", ParameterType.String, "", ""),
		new TypedParameter("dependency", "other dependency", ParameterType.List, "", "[]"),
	};
	
	public final static TypedParameter[] defaultOutputParameter = {
		new TypedParameter("stdout", "task execution stdout", ParameterType.String, "", ""),
		new TypedParameter("stderr", "task execution stderr", ParameterType.String, "", ""),
		new TypedParameter("exitCode", "operating system process exitcode", ParameterType.Long, "", ""),
	};
}

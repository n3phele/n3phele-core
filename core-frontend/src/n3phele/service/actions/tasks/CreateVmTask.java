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
import java.util.Calendar;
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
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import n3phele.service.core.Resource;
import n3phele.service.model.ActionState;
import n3phele.service.model.Cloud;
import n3phele.service.model.core.CreateVirtualServerResponse;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.ExecutionFactoryCreateRequest;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.VirtualServer;
import n3phele.service.rest.impl.CloudResource;
import n3phele.service.rest.impl.HistoryResource;
@XmlRootElement(name="CreateVmTask")
@XmlType(name="CreateVmTask", propOrder={"accountName", "cloud", "instanceId", "clientInitComplete", "duration", "started", 
		"siblings", "agentAddr", "privateIP", "publicIP"})
@Unindexed
@Cached
public class CreateVmTask extends ActionTaskImpl implements ActionTask {
	private final static Logger log = Logger.getLogger(CreateVmTask.class.getName()); 
	
	@Id private Long id;
	private String accountName;
	private String cloud;
	@Embedded private Credential cloudCredential;
	boolean clientInitComplete = false;
	protected String instanceId;
	private Long started = null;
	private String[] siblings;
	private String[] agentAddr;
	private String[] privateIP;
	private String[] publicIP;
	

	private long duration = 0L;
	
	public CreateVmTask() {};
	
	public CreateVmTask(String name, String description, String workloadKey,
			ArrayList<FileRef> inputFiles, ArrayList<TypedParameter> executionParameters,
			ArrayList<FileRef> outputFiles, ArrayList<TypedParameter> outputParameters) {
		super(name, description, workloadKey, inputFiles, executionParameters, outputFiles, outputParameters);
	}
	

	@Override
	public boolean call() throws Exception {
		if(this.state == ActionState.INIT)
			return false;
		if(isAwaitingDependencies())
			return false;
		Client client = ClientFactory.create();
		Cloud myCloud = new CloudResource.CloudManager().load(URI.create(this.cloud));
		Credential plain = Credential.unencrypted(myCloud.getFactoryCredential());
		ClientFilter factoryAuth = new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret());
		client.addFilter(factoryAuth);
		client.setReadTimeout(30000);
		client.setConnectTimeout(5000);

		WebResource resource;
		boolean forceAgentRestart = false;
		
		try {
		
			switch(state) {
			default:
			case INIT:
			case CANCELLED:
			case BLOCKED:
			case COMPLETE:
			case FAILED:
				return false;
	
			case PENDING:
				resource = client.resource(myCloud.getFactory());
				ExecutionFactoryCreateRequest cr = new ExecutionFactoryCreateRequest();
				cr.name = this.name;
				cr.description = this.description;
				cr.location = myCloud.getLocation();
				TypedParameter keyName = getExecutionParameter("keyName");
				if(keyName != null) {
					keyName.setDefaultValue(accountName);
				} else {
					if(this.executionParameters == null) 
						this.executionParameters = new ArrayList<TypedParameter>();
					this.executionParameters.add(new TypedParameter().withName("keyName")
							.withDescription("SSH Public/Private keypair for interaction with the VM")
							.withValue("n3phele-"+accountName));
				}
				cr.parameters = NameValue.from(this.executionParameters);
				cr.notification = UriBuilder.fromUri(this.parentAction).scheme("http").path("event").build();
				cr.idempotencyKey = this.parentAction;
	
				Credential c = Credential.reencrypt(this.cloudCredential, Credential.unencrypted(myCloud.getFactoryCredential()).getSecret());
				cr.accessKey = c.getAccount(); 
				cr.encryptedSecret = c.getSecret();
				cr.owner = URI.create(this.parent);
	
				try {
					client.setReadTimeout(90000);
					client.setConnectTimeout(5000);
					ClientResponse response = resource.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, cr);
		
					URI location = response.getLocation();
					if(location != null) {
						state = ActionState.RUNNING;
						this.instanceId = location.toString();
						updateOutputParameter("vmURI", location.toString());
						URI[] refs = response.getEntity(CreateVirtualServerResponse.class).vmList;
	
						this.siblings = new String[refs.length];
						this.agentAddr = new String[refs.length];
						this.privateIP = new String[refs.length];
						this.publicIP = new String[refs.length];
	
						for(int i=0; i < refs.length; i++) {
							this.siblings[i] = refs[i].toString();
						}
	
						updateOutputParameter("vmURIs", arrayToString(this.siblings));
	
						
						log.fine(this.name+" "+Integer.toString(refs.length)+" vm(s) creation started. Factory "+location.toString()+" initiating status "+response.getStatus());
						if(refs.length == 1) {
							ActionLog.name(this).info("vm creation started.");
								try {
									resource = client.resource(siblings[0]);
									VirtualServer vs = resource.get(VirtualServer.class);
									log.info("Server status is "+vs.getStatus());
									if(vs.getStatus().equalsIgnoreCase("Running")) {
										forceAgentRestart = true;
										log.info("forcing agent restart");
									} else {
										return false;
									}
								} catch (Exception e) {
									log.log(Level.SEVERE, "VM fetch", e);
								}

						} else {
							ActionLog.name(this).info(Integer.toString(refs.length)+" vm(s) creation started.");
							return false;
						}
					} else {
						log.log(Level.SEVERE, this.name+" vm creation initiation FAILED with status "+response.getStatus());
						ActionLog.name(this).error("vm creation initiation FAILED with status "+response.getStatus());
						state = ActionState.FAILED;
						return true;
					}	
				} catch (Exception e) {
					if(started != null && started != 0) {
						ActionLog.name(this).error("vm creation FAILED with exception "+e.getMessage());
						log.log(Level.SEVERE, "vm creation FAILED with exception ", e);
						state = ActionState.FAILED;
						return true;
					}
				} finally {
					started = Calendar.getInstance().getTimeInMillis();
				}
				//no break;
				// this path will be taken for a single vm creation.
			case RUNNING:
				long timeout = Long.valueOf(Resource.get("vmProvisioningTimeoutInSeconds", "900"))*1000;
				if(started+timeout < Calendar.getInstance().getTimeInMillis()) {
					ActionLog.name(this).error("vm creation FAILED due to timeout");
					log.log(Level.SEVERE, "vm creation FAILED due to timeout");
					state = ActionState.FAILED;
					return true;
				}
					
				int alive = 0;
				client.setReadTimeout(25000);
				client.setConnectTimeout(5000);
				Credential agentCredential =  plain = new Credential(getExecutionParameterValue("agentUser"), 
						 																 getExecutionParameterValue("agentPassword"));
				ClientFilter agentAuth = new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret());
	
				for(int i=0; i < siblings.length; i++) {
					if(agentAddr[i]==null) {
						client.removeAllFilters();
						client.addFilter(factoryAuth);
						resource = client.resource(siblings[i]);
						try {
							VirtualServer vs = resource.get(VirtualServer.class);
							log.info("Server status is "+vs.getStatus());
							if(vs.getStatus().equalsIgnoreCase("Running") && vs.getOutputParameters() != null) {
								for(NameValue p : vs.getOutputParameters()) {
									if(p.getKey().equals("privateIpAddress")) {
										privateIP[i] = p.getValue();
									} else if(p.getKey().equals("publicIpAddress")) {
										publicIP[i] = p.getValue();
									}
								}
								client.removeAllFilters();
								client.addFilter(agentAuth);
		
								String agentURI = String.format(Resource.get("agentPattern", "http://%s:8887/task"),publicIP[i]);
								resource = client.resource(agentURI);
								if(forceAgentRestart) {
									try {
										ClientResponse response = resource.path("terminate").get(ClientResponse.class);
										if(response.getStatus() == 200) {
											Thread.sleep(3000);
										}
										log.info("Agent restart "+response.getStatus());
									} catch (Exception e) {
										log.log(Level.SEVERE, "Agent restart exception", e);
									} finally {
										forceAgentRestart = false;
									}
								}
								resource.path("date").get(String.class);
								alive += 1;
								agentAddr[i] = agentURI;
		
							} else if(vs.getStatus().equalsIgnoreCase("Terminated")) {
								ActionLog.name(this).error("Client "+resource.getURI()+" unexpected death.");
								log.severe("Client "+resource.getURI()+" unexpected death.");
								state = ActionState.FAILED;
								return false;		// FIXME Need to add some recovery processing
							}
						} catch (UniformInterfaceException e) {
							ActionLog.name(this).info("Awaiting host "+resource.getURI().getHost()+" initialization");
							log.info("Waiting for agent "+resource.getURI()+" "+e.getMessage());
						} catch (ClientHandlerException e) {
							ActionLog.name(this).info("Awaiting host "+resource.getURI().getHost()+" initialization");
							log.info("Waiting for agent "+resource.getURI()+" "+e.getMessage());
						} catch (Exception e) {
							log.log(Level.SEVERE, "Unexpected exception", e);
						}
					} else {
						alive += 1;
					}
				}
				clientInitComplete = alive == this.siblings.length;
				if(clientInitComplete) { 
					updateOutputParameter("agentUser", agentCredential.getAccount());
					updateOutputParameter("agentPassword", agentCredential.getSecret());
					updateOutputParameter("agentBaseUrl", agentAddr[0]);
					updateOutputParameter("publicIpAddress", this.publicIP[0]);
					updateOutputParameter("privateIpAddress", this.privateIP[0]);
					updateOutputParameter("publicIpAddressList", arrayToString(this.publicIP));
					updateOutputParameter("privateIpAddressList", arrayToString(this.privateIP));
					updateOutputParameter("agentUrls", arrayToString(this.agentAddr));
					client.removeAllFilters();
					client.addFilter(factoryAuth);
					resource = client.resource(siblings[0]);
					VirtualServer vs = resource.get(VirtualServer.class);
					for(NameValue p : vs.getOutputParameters()) {
						updateOutputParameter(p.getKey(), p.getValue());
					}
						
					duration = (Calendar.getInstance().getTimeInMillis() - started)/1000;
					if(duration == 0)
						duration = 1;
					addDuration(duration);
					state = ActionState.COMPLETE;
					log.fine(this.name+" vm(s) created successfully. Elapsed time "+duration+" seconds.");
					ActionLog.name(this).info("vm(s) created completed successfully. Elapsed time "+duration+" seconds.");
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
	 * @see n3phele.service.actions.tasks.ActionTask#cleanup()
	 */
	@Override
	public void cleanup() throws Exception {

		cancel();
		
	}


	@Override
	public void cancel() {
		if(instanceId != null) {
			log.info("Cleanup of "+instanceId);
			Client client = ClientFactory.create();
			try {
				Cloud myCloud = new CloudResource.CloudManager().load(URI.create(this.cloud));
				Credential plain = Credential.unencrypted(myCloud.getFactoryCredential());
				client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
				client.setReadTimeout(90000);
				client.setConnectTimeout(5000);
				for(String addr : this.siblings){
					WebResource resource = client.resource(addr);
					ClientResponse response = resource.delete(ClientResponse.class);
					log.info("Delete status is "+response.getStatus());
				}
				instanceId = null;
			} finally {
				ClientFactory.give(client);
			}
		} else {
			log.info("Cancel has Instance id that is null");
		}
	}
	
	@Override
	public void stop() throws Exception {
		if(instanceId != null) {
			log.info("Stop of "+instanceId);
			Client client = ClientFactory.create();
			try {
				Cloud myCloud = new CloudResource.CloudManager().load(URI.create(this.cloud));
				Credential plain = Credential.unencrypted(myCloud.getFactoryCredential());
				client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
				client.setReadTimeout(90000);
				client.setConnectTimeout(5000);
				for(String addr : this.siblings){
					WebResource resource = client.resource(addr);
					boolean debug = Resource.get("suppressDeleteVM", false);
					ClientResponse response = resource.queryParam("error", Boolean.toString(true)).queryParam("debug", Boolean.toString(debug)).delete(ClientResponse.class);
					log.info("Delete status is "+response.getStatus());
					if(debug) {
						ActionLog.name(this).warning("Attempting to create a debug VM to allow error inspection. Debug instance will be removed aproximately 55 minutes after inital creation.");
					}
					
				}
				instanceId = null;
			} finally {
				ClientFactory.give(client);
			}
		} else {
			log.info("Stop has Instance id that is null");
		}
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
		if(percentx10 <= 0) percentx10 = 10;
		return (int) (1000L - percentx10);
	}
	
	/**
	 * @return the duration
	 */
	public long getDuration() {
		long estimate = 300L;
		if(duration == 0){
			boolean unresolved = false;
			String type = this.getExecutionParameterValue("instanceType");
			if(type == null || type.equals(""))  {
				type = "vm";
			} else {
				if(type.contains("$<")) {
					type = "vm";
					unresolved = true;
				}
			}
			String kind;
			String spotPrice = this.getExecutionParameterValue("spotPrice");
			try {
				kind = (spotPrice == null || spotPrice.equals("") || Double.valueOf(spotPrice) == 0.0)?"demand":"spot";
			} catch (NumberFormatException e) {
				kind = "spot";
				unresolved = true;
			}

			estimate = new HistoryResource.HistoryManager().getNinetyPercentile("createVM", cloud, kind, type, workloadKey);
			if(!unresolved)
				duration = estimate;
		} else {
			estimate = duration;
		}

		return estimate;
	}
	
	/**
	 * @return the duration
	 */
	public void addDuration(long actual) {
		if(actual == 0)
			actual = 1;
		String kind = this.getExecutionParameterValue("spotPrice")==null?"demand":"spot";
		String type = this.getExecutionParameterValue("instanceType");
		if(type == null) type = "vm";
		new HistoryResource.HistoryManager().addSample(actual, "createVM", cloud, kind, type);
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
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * @return the cloudCredential
	 */
	@XmlTransient
	public Credential getCloudCredential() {
		return cloudCredential;
	}

	/**
	 * @param cloudCredential the cloudCredential to set
	 */
	public void setCloudCredential(Credential cloudCredential) {
		this.cloudCredential = cloudCredential;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the clientInitComplete
	 */
	public boolean isClientInitComplete() {
		return clientInitComplete;
	}

	/**
	 * @param clientInitComplete the clientInitComplete to set
	 */
	public void setClientInitComplete(boolean clientInitComplete) {
		this.clientInitComplete = clientInitComplete;
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
	 * @return the history
	 */
	public URI getCloud() {
		return (cloud==null)?null:URI.create(cloud);
	}

	/**
	 * @param history the history to set
	 */
	public void setCloud(URI cloud) {
		this.cloud = (cloud==null)?null:cloud.toString();
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @return the siblings
	 */
	public String[] getSiblings() {
		return siblings;
	}

	/**
	 * @param siblings the siblings to set
	 */
	public void setSiblings(String[] siblings) {
		this.siblings = siblings;
	}

	/**
	 * @return the agentAddr
	 */
	public String[] getAgentAddr() {
		return agentAddr;
	}

	/**
	 * @param agentAddr the agentAddr to set
	 */
	public void setAgentAddr(String[] agentAddr) {
		this.agentAddr = agentAddr;
	}

	/**
	 * @return the privateIP
	 */
	public String[] getPrivateIP() {
		return privateIP;
	}

	/**
	 * @param privateIP the privateIP to set
	 */
	public void setPrivateIP(String[] privateIP) {
		this.privateIP = privateIP;
	}

	/**
	 * @return the publicIP
	 */
	public String[] getPublicIP() {
		return publicIP;
	}

	/**
	 * @param publicIP the publicIP to set
	 */
	public void setPublicIP(String[] publicIP) {
		this.publicIP = publicIP;
	}

	private String arrayToString(String[] a) {
		StringBuilder list = new StringBuilder();
		list.append("[");
		for(String x : a){
			if(list.length() > 1)
				list.append(",");
			list.append("\"");
			list.append(x);
			list.append("\"");
		}
		list.append("]");
		return list.toString();
	}


	public final static TypedParameter[] defaultInputParameter = {
		new TypedParameter("agentUser", "username for authenticated access to the vm agent", ParameterType.Secret, "", "n3ph"),
		new TypedParameter("agentPassword", "password for authenticated access to the vm agent", ParameterType.Secret, "", "3l3hp3n"),
		new TypedParameter("dependency", "other dependency", ParameterType.List, "", "[]"),
	};
	
	public final static TypedParameter[] defaultOutputParameter = {
		new TypedParameter("agentUser", "username for authenticated access to the vm agent", ParameterType.Secret, "", ""),
		new TypedParameter("agentPassword", "password for authenticated access to the vm agent", ParameterType.Secret, "", ""),
		new TypedParameter("agentBaseUrl", "url for access to the vm agent", ParameterType.String, "", ""),
		new TypedParameter("agentUrls", "url for access to the vm agent", ParameterType.List, "", ""),
		new TypedParameter("vmURI", "URI of the first VM created", ParameterType.String, "", ""),
		new TypedParameter("vmURIs", "List of URIs for VM(s) created", ParameterType.List, "", ""),
		new TypedParameter("publicIpAddress", "public IP address of the created VM", ParameterType.String, "", ""),
		new TypedParameter("publicIpAddressList", "list of public IP addresses of the created VM(s)", ParameterType.List, "", ""),
		new TypedParameter("privateIpAddress", "private IP address of the created VM", ParameterType.String, "", ""),
		new TypedParameter("privateIpAddressList", "private IP addresses of the created VMs", ParameterType.List, "", ""),
	};

}

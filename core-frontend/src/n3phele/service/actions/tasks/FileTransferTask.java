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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.representation.Form;

import n3phele.service.model.ActionState;
import n3phele.service.model.core.BaseEntity;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.Task;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.rest.impl.HistoryResource;

@XmlRootElement(name="FileTransferTask")
@XmlSeeAlso({BaseEntity.class})
@XmlType(name="FileTransferTask", propOrder={"started", "instanceId", "clientUnresponsive", "duration", "size"})
@Unindexed
@Cached
public class FileTransferTask extends ActionTaskImpl implements ActionTask {
	private static Logger log = Logger.getLogger(FileTransferTask.class.getName());
	@Id private Long id;
	private Long started = 0L;
	@Embedded private Credential clientCredential;
	private String instanceId;
	private Long clientUnresponsive = null;
	private long duration = 0L;
	private Long size = null;
	
	public FileTransferTask() {};
	
	
	public FileTransferTask(String name, String workloadKey, FileRef from, FileRef to, URI progress, URI parent, ArrayList<TypedParameter> executionParameters, ArrayList<TypedParameter> outParams) {
		super(name, from.getDescription(), workloadKey, asArrayList(from), executionParameters, asArrayList(to), outParams);

		if(this.description == null || this.description.trim().length() == 0)
			this.description = to.getDescription();
//		this.setSource(from.getSource());

//		this.srcKey = from.getKey();
//		this.srcRoot = from.getRoot();
//		this.srcKind = from.getKind();
//		this.srcCredential = from.getRepoCredential();
//		
//		this.setDestination(to.getSource());
//		this.destKey = to.getKey();
//		this.destRoot = to.getRoot();
//		this.destKind = to.getKind();
//		this.destCredential = to.getRepoCredential();
		
		this.progress = (progress==null)?null:progress.toString();
		this.parent = (parent==null)?null:parent.toString();
		if(from.getLength()!=0)
			this.size = from.getLength();

	}

	private static ArrayList<FileRef> asArrayList(FileRef f) {
		ArrayList<FileRef> result = new ArrayList<FileRef>(1);
		if(f != null) result.add(f);
		return result;
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
					getExecutionParameterValue("agentPassword"));		}
		Client client = ClientFactory.create();
		if(this.clientCredential == null) {
			log.severe("Null client srcCredential");
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
				FileRef src = this.getInputFiles().get(0);
				if(started == 0) {
					started = Calendar.getInstance().getTimeInMillis();
					this.size = src.getLength();
					this.duration =0;
					getDuration();
				}
				resource = client.resource(getExecutionParameterValue("agentBaseUrl"));
				Form form = new Form();

				if(src.getSource() != null) form.add("source", src.getSource());	
				if(src.getRoot() != null) form.add("srcRoot", src.getRoot());
				form.add("srcKey", src.getKey());
				if(src.getRepoCredential() != null && src.getRepoCredential().getAccount() != null) {
					Credential srcCredential1;
					srcCredential1 = Credential.reencrypt(src.getRepoCredential(), plain.getSecret());
					form.add("srcAccount", srcCredential1.getAccount());
					form.add("srcSecret", srcCredential1.getSecret());
				}
				form.add("srcKind", src.getKind());
				FileRef dest = this.getOutputFiles().get(0);
				if(dest.getSource() != null) form.add("destination", dest.getSource());	
				if(dest.getRoot() != null) form.add("destRoot", dest.getRoot());
				form.add("destKey", dest.getKey());
				if(dest.getRepoCredential() != null && dest.getRepoCredential().getAccount() != null) {
					Credential destCredential1;
					destCredential1= Credential.reencrypt(dest.getRepoCredential(), plain.getSecret());
					form.add("destAccount", destCredential1.getAccount());
					form.add("destSecret", destCredential1.getSecret());
				}
				form.add("destKind", dest.getKind());
				form.add("notification", UriBuilder.fromUri(this.parentAction).scheme("http").path("event").build());
				form.add("tag", dest.getName());
				form.add("description", dest.getDescription());
	 
				try {
					ClientResponse response = resource.path("xfer").type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, form);
	
					URI location = response.getLocation();
					if(location != null) {
						this.instanceId = location.toString();
						log.fine(this.name+" file copy started. Factory "+location.toString()+" initiating status "+response.getStatus());
						ActionLog.name(this).info("file copy started.");
	
						state = ActionState.RUNNING;
					} else {
						log.log(Level.SEVERE, this.name+" file copy initiation FAILED with status "+response.getStatus());
						ActionLog.name(this).error("file copy initiation FAILED with status "+response.getStatus());
	
						state = ActionState.FAILED;
					}	
				} catch (Exception e) {
					long now = Calendar.getInstance().getTimeInMillis();
					// Give the agent a 5 minute grace period to respond
					if((now - started) > 5*60*1000L) {
						ActionLog.name(this).error("file copy initiation FAILED with exception "+e.getMessage());
						log.log(Level.SEVERE, this.name+" file copy initiation FAILED with exception ", e);
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
						return false;
					} else {
						long now = Calendar.getInstance().getTimeInMillis();
						// Give the agent a 5 minute grace period to respond
						if((now - clientUnresponsive) > 5*60*1000L) {
							log.log(Level.SEVERE, name+" file copy monitoring "+resource.getURI()+" failed", e);
							ActionLog.name(this).error("Copy monitoring failed with exception "+e.getMessage());
							state = ActionState.FAILED;
						} 
					}
					break;
				} catch (UniformInterfaceException e) {
					if(clientUnresponsive == null) {
						clientUnresponsive = Calendar.getInstance().getTimeInMillis();
						return false;
					} else {
						long now = Calendar.getInstance().getTimeInMillis();
						// Give the agent a 5 minute grace period to respond
						if((now - clientUnresponsive) > 5*60*1000L) {
							log.log(Level.SEVERE, name+" file copy monitoring "+resource.getURI()+" failed", e);
							ActionLog.name(this).error("Copy monitoring failed with exception "+e.getMessage());
							state = ActionState.FAILED;
						} 
					}
					break;
				}
				updateOutputParameter("stdout", t.getStdout());
				updateOutputParameter("stderr", t.getStderr());
				if(t.getFinished() != null) {
					updateOutputParameter("exitCode", Integer.toString(t.getExitcode()));
					if(t.getExitcode() == 0) {
						Calendar time = Calendar.getInstance();
						Calendar begin = Calendar.getInstance();
						time.setTime(t.getFinished());
						begin.setTime(t.getStarted());
						long interval = time.getTimeInMillis() - begin.getTimeInMillis();
						addDuration(this.duration = (Calendar.getInstance().getTimeInMillis()-started)/1000);
						if(t.getStdout() != null && t.getStdout().length() > 0)
							ActionLog.name(this).info("Stdout:"+t.getStdout());
						if(t.getStderr() != null && t.getStderr().length() > 0)
							ActionLog.name(this).warning("Stderr:"+t.getStderr());
						String durationText;
						if(interval < 1000) {
							durationText = Long.toString(interval)+" milliseconds"; 
						} else {
							durationText = Long.toString(interval/1000)+" seconds"; 
						}
						log.fine(this.name+" file copy completed successfully. Elapsed time "+durationText);
						ActionLog.name(this).info("File copy completed successfully. Elapsed time "+durationText);

						if(t.getManifest() != null) {
							log.info("File copy manifest length "+t.getManifest().length);
							int len = t.getManifest().length;
							if(len < 1024) {
								this.setOutputFiles(new ArrayList<FileRef>(Arrays.asList(t.getManifest())));
							} else {
								if(this.getOutputFiles() != null && this.getOutputFiles().size()  > 0) {
									Map<String, FileRef> outputs = new HashMap<String, FileRef>();
					
									for(FileRef f : this.getOutputFiles()) {
										outputs.put(f.getName(), f);
									}
									for(FileRef f : t.getManifest()) {
										if(outputs.containsKey(f.getName())) {
											FileRef out = outputs.get(f.getName());
											out.setLength(f.getLength());
											out.setModified(f.getModified());
										} else if(this.getOutputFiles().size() < 1024) {
											this.getOutputFiles().add(f);
										}
									}
								} else {
									this.setOutputFiles(new ArrayList<FileRef>(Arrays.asList(t.getManifest()).subList(0, 1024)));
								}
							}
						}
						state = ActionState.COMPLETE;
					} else {
						if(t.getStdout() != null && t.getStdout().length() > 0)
							ActionLog.name(this).info("Stdout:"+t.getStdout());
						if(t.getStderr() != null && t.getStderr().length() > 0)
							ActionLog.name(this).warning("Stderr:"+t.getStderr());
						ActionLog.name(this).error("File copy "+resource.getURI()+" failed with exit status "+t.getExitcode());
						log.severe(name+" file copy "+resource.getURI()+" failed with exit status "+t.getExitcode());
						log.severe("Stdout: "+t.getStdout());
						log.severe("Stderr: "+t.getStderr());
	
						state = ActionState.FAILED;
					}
				} else {
					double sofar = (double) (Calendar.getInstance().getTimeInMillis() - started);
					double copyProgress = (double)t.getProgress();
					long oldduration = duration;
					if(sofar > 2000.0 && copyProgress > 0.0) {
						duration = (long) (sofar/copyProgress);
					}
					return Math.abs(duration - oldduration) > 5;
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
		long togo = duration - ((Calendar.getInstance().getTimeInMillis()-started)/1000);
		long percentx10 = (togo * 1000) / duration;
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
		if(duration == 0){
			String key = makeKey(this.size);
			FileRef src = this.getInputFiles().get(0);
			FileRef dest = this.getOutputFiles().get(0);
			String mix = src.getKind()+dest.getKind();
			this.workloadKey = mix;
			duration = new HistoryResource.HistoryManager().getNinetyPercentile(this.workloadKey, key);
			if(duration == 0)
				duration = 5L;
		}
		return duration;
	}
	

	/**
	 * @param actual the actual execution duration
	 */
	protected void addDuration(long actual) {
		if(actual == 0)
			actual = 1;
		String key =  makeKey(this.size);
		if(this.workloadKey == null || this.workloadKey.length() == 0) {
			FileRef src = this.getInputFiles().get(0);
			FileRef dest = this.getOutputFiles().get(0);
			String mix = src.getKind()+dest.getKind();
			this.workloadKey = mix;
		}
		new HistoryResource.HistoryManager().addSample(actual, this.workloadKey, key);
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
	 * @param RUNNING the RUNNING to set
	 */
	public void setStarted(Long started) {
		this.started = started;
	}



//	/**
//	 * @return the source
//	 */
//	public URI getSource() {
//		return (source==null)?null:URI.create(source);
//	}
//
//	/**
//	 * @param source the source to set
//	 */
//	public void setSource(URI source) {
//		this.source = (source==null)?null:source.toString();
//	}
//
//
//	/**
//	 * @return the destination
//	 */
//	public URI getDestination() {
//		return (destination==null)?null:URI.create(destination);
//	}
//
//	/**
//	 * @param destination the destination to set
//	 */
//	public void setDestination(URI destination) {
//		this.destination = (destination==null)?null:destination.toString();
//	}
//
//	/**
//	 * @return the srcCredential
//	 */
//	@XmlTransient
//	public Credential getSrcCredential() {
//		return srcCredential;
//	}
//
//	/**
//	 * @param srcCredential the srcCredential to set
//	 */
//	public void setSrcCredential(Credential srcCredential) {
//		this.srcCredential = srcCredential;
//	}
//
//	/**
//	 * @return the destCredential
//	 */
//	@XmlTransient
//	public Credential getDestCredential() {
//		return destCredential;
//	}
//
//	/**
//	 * @param destCredential the destCredential to set
//	 */
//	public void setDestCredential(Credential destCredential) {
//		this.destCredential = destCredential;
//	}

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


//	/**
//	 * @return the srcKey
//	 */
//	public String getSrcKey() {
//		return srcKey;
//	}
//
//
//	/**
//	 * @param srcKey the srcKey to set
//	 */
//	public void setSrcKey(String srcKey) {
//		this.srcKey = srcKey;
//	}
//
//
//	/**
//	 * @return the srcRoot
//	 */
//	public String getSrcRoot() {
//		return srcRoot;
//	}
//
//
//	/**
//	 * @param srcRoot the srcRoot to set
//	 */
//	public void setSrcRoot(String srcRoot) {
//		this.srcRoot = srcRoot;
//	}
//
//
//	/**
//	 * @return the srcKind
//	 */
//	public String getSrcKind() {
//		return srcKind;
//	}
//
//
//	/**
//	 * @param srcKind the srcKind to set
//	 */
//	public void setSrcKind(String srcKind) {
//		this.srcKind = srcKind;
//	}
//
//
//	/**
//	 * @return the destKey
//	 */
//	public String getDestKey() {
//		return destKey;
//	}
//
//
//	/**
//	 * @param destKey the destKey to set
//	 */
//	public void setDestKey(String destKey) {
//		this.destKey = destKey;
//	}
//
//
//	/**
//	 * @return the destRoot
//	 */
//	public String getDestRoot() {
//		return destRoot;
//	}
//
//
//	/**
//	 * @param destRoot the destRoot to set
//	 */
//	public void setDestRoot(String destRoot) {
//		this.destRoot = destRoot;
//	}
//
//
//	/**
//	 * @return the destKind
//	 */
//	public String getDestKind() {
//		return destKind;
//	}
//
//
//	/**
//	 * @param destKind the destKind to set
//	 */
//	public void setDestKind(String destKind) {
//		this.destKind = destKind;
//	}


	/**
	 * @return the execution start time since the epoch.
	 */
	public Long getStarted() {
		return started;
	}


	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
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




	/**
	 * @return the size
	 */
	public Long getSize() {
		return this.size;
	}


	/**
	 * @param size the size to set
	 */
	public void setSize(Long size) {
		this.size = size;
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

	public final static TypedParameter[] defaultInputParameter = {
		new TypedParameter("agentUser", "username for authenticated access to the vm agent", ParameterType.Secret, "", "n3ph"),
		new TypedParameter("agentPassword", "password for authenticated access to the vm agent", ParameterType.Secret, "", "3l3hp3n"),
		new TypedParameter("agentBaseUrl", "url for access to the vm agent", ParameterType.String, "", ""),
	};
	
	public final static TypedParameter[] defaultOutputParameter = {
		new TypedParameter("stdout", "task execution stdout", ParameterType.String, "", ""),
		new TypedParameter("stderr", "task execution stderr", ParameterType.String, "", ""),
		new TypedParameter("exitCode", "operating system process exitcode", ParameterType.Long, "", ""),
	};

}

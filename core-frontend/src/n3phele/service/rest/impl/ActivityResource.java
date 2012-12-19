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
package n3phele.service.rest.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import n3phele.service.actions.tasks.ActionLog;
import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.Action;
import n3phele.service.model.ActionSpecification;
import n3phele.service.model.ActionState;
import n3phele.service.model.ActionType;
import n3phele.service.model.Activity;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.ChangeManager;
import n3phele.service.model.Cloud;
import n3phele.service.model.CloudProfile;
import n3phele.service.model.Command;
import n3phele.service.model.ExecuteCommandRequest;
import n3phele.service.model.FileMonitor;
import n3phele.service.model.FileSpecification;
import n3phele.service.model.Origin;
import n3phele.service.model.Progress;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.BaseEntity;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.UnprocessableEntityException;
import n3phele.service.model.core.User;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

@Path("/activity")
public class ActivityResource {
	private static Logger log = Logger.getLogger(ActivityResource.class.getName()); 
	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	private final Dao dao;
	public ActivityResource() {
		this(new Dao());
	}

	public ActivityResource(Dao dao) {
		this.dao = dao;
	}

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public Collection<BaseEntity> list(
			@DefaultValue("false") @QueryParam("summary") Boolean summary) throws URISyntaxException, EntityNotFoundException {
		Collection<BaseEntity> result = dao.activity().getCollection(UserResource.toUser(securityContext)).collection(summary);
		return result;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("authenticated")
	public Response execute(ExecuteCommandRequest request) throws Exception {
		if(request.getInputFiles() != null && request.getInputFiles().length == 1) {
			String name = request.getInputFiles()[0].getName();
			if(name == null) {
				log.warning("Scrubbing null input file object");
				request.setInputFiles(null);
			}
		}
		if(request.getOutputFiles() != null && request.getOutputFiles().length == 1) {
			String name = request.getOutputFiles()[0].getName();
			if(name == null) {
				log.warning("Scrubbing null output file object");
				request.setOutputFiles(null);
			}
		}
		Activity activity = executeForUser(request, UserResource.toUser(securityContext));
		return Response.created(activity.getUri()).build();
	}

	@GET
	@RolesAllowed("authenticated")
	// @Produces("application/vnd.com.n3phele.Activity+json")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}") 
	public Activity get( @PathParam ("id") Long id) throws NotFoundException {

		Activity item = dao.activity().load(id, UserResource.toUser(securityContext));
		return item;
	}
	
	@GET
	@RolesAllowed("administrator")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("dump") 
	public Activity[] dump(@QueryParam ("filter") String filter ) {
		if(filter != null && filter.length()==0)
			filter = null;
		Activity[] result = null;
		List<Activity> list = dao.activity().getAll();
		if(filter != null && list != null) {
			List <Activity> filtered = new ArrayList<Activity>();
			for(Activity a: list) {
				if(a.getCommand().toString().equals(filter)) {
					if(a.getState().equals(ActionState.COMPLETE))
						filtered.add(a);
				}
			}
			list = filtered;
		}
		if(list != null) {
			result = list.toArray(new Activity[list.size()]);
		}
		return result;
	}
	


	@DELETE
	@RolesAllowed("authenticated")
	@Path("{id}")
	public void delete(@PathParam ("id") Long id) throws NotFoundException {
		Activity activity = dao.activity().load(id, UserResource.toUser(securityContext));
		kill(id);

		dao.activity().delete(activity);
	} 

	@GET
	@RolesAllowed("authenticated")
	@Path("{id}/kill")
	public Response kill(@PathParam ("id") Long id) throws NotFoundException {
		Activity item = dao.activity().load(id, UserResource.toUser(securityContext));
		
		do_kill(item, "Terminated by user "+UserResource.toUser(securityContext).getName());
		
		return Response.ok().build();
	}
	/*
	 * ------------------------------------------------------------------------------------------ *
	 *                      Google app engine entry points.
	 *  The following entry points are intended to by invoked by internal app engine processes.
	 *  Their invocation is protected by google app engine "admin" security settings.
	 * ------------------------------------------------------------------------------------------ *
	 */
	
	private ActionResource actionResouceInstance;
	private ActionResource getActionResource() {
		if(actionResouceInstance == null)
			actionResouceInstance = new ActionResource(dao);
		return actionResouceInstance;
	}
	
	public void do_kill(Activity item, String reason) {
		ActionState newState = reason == null || reason.startsWith(".") ? ActionState.FAILED : ActionState.CANCELLED;
		if(item.getActions() != null) {
			for(URI u : item.getActions()) {
				getActionResource().terminate(u, newState == ActionState.FAILED);
			}
		}
		
		try {
			URI progressUri = item.getProgress();
			Progress progress = dao.progress().load(progressUri);
			ActionState finalState = progress.getStatus();
			if(finalState != ActionState.FAILED && finalState != ActionState.COMPLETE) {
				if(reason != null && reason.length() > 0 && !reason.startsWith(".")) 
					ActionLog.withProgress(progressUri).withName("Activity").error(reason);
				if(progress.getStarted() == null)
					dao.progress().updateStart(progressUri, Calendar.getInstance().getTime(), ActionState.CANCELLED, 1000);
				item.setState(newState);
				dao.activity().update(item);
				dao.progress().updateEstimates(progressUri, 1000, newState,
						Calendar.getInstance().getTime() );
				if(reason==null) reason="processing failure";
				sendNotificationEmail(item, true, reason.startsWith(".")?reason.substring(1):reason);
			}

		} catch (Exception ignore) {
			log.log(Level.SEVERE, "update to progress failed ", ignore);
		}
		
	}

	public Response do_dispatch(long id, boolean failure, String reason) {
		Activity activity=null;
		try {
			activity = dao.activity().lock(id);
			
			if(failure) {
				do_kill(activity, reason);
			} else {
				switch(activity.getState()) {
				case CANCELLED:
				case COMPLETE:
				case FAILED:
				default:
					break;
				case INIT:
					dao.progress().updateStart(activity.getProgress(), Calendar.getInstance().getTime(), ActionState.PENDING, 0);
					activity.setState(ActionState.PENDING);
					getActionResource().initiate(activity.getActions());
					// no break;
				case BLOCKED:
				case RUNNING:
				case PENDING:
	
					Progress progress = updateProgress(activity);
					if(progress != null) {
						ActionState newState = progress.getStatus();
						activity.setState(newState);
						dao.activity().update(activity);
						if(newState == ActionState.COMPLETE || newState == ActionState.FAILED) {
							/*
							 * Force release of all resources
							 */
							for(URI u : activity.getActions()) {
								getActionResource().terminate(u, newState == ActionState.FAILED);
							}
							if(newState == ActionState.COMPLETE) {
								sendNotificationEmail(activity, false, null);
								updateFileInformation(activity);
							}
						}
					}
					break;
				}
			}

		} catch (ConcurrentModificationException e) {
			// update already in progress
		} catch (Exception e) {
			log.log(Level.WARNING, String.format("processing %d %s %s",id, (activity!=null)?activity.getUri():null,(activity!=null)?activity.getName():null), e);
			try {
				if(activity != null) {
					dao.progress().updateEstimates(activity.getProgress(), 1000, ActionState.FAILED, Calendar.getInstance().getTime());
					activity.setState(ActionState.FAILED);
				}

			} catch (Exception ignore) {
				log.log(Level.SEVERE, "dispatch", ignore);
			} 
		} finally {
			try {
				dao.activity().release(activity);
			} catch (Exception ignore) {
				log.log(Level.SEVERE, "dispatch-finally", ignore);
			}
		}

		return Response.ok().build();
	}
	
	public void sendNotificationEmail(Activity activity,
			boolean failure, String reason) {
		if (activity != null && activity.isNotify()) {
			User user = null;
			try {
				URI owner = activity.getOwner();
				if (owner == null || owner.equals(UserResource.Root.getUri()))
					return;
				user = dao.user().load(owner);
				StringBuilder subject = new StringBuilder();
				StringBuilder body = new StringBuilder();
				Progress progress = dao.progress().load(activity
						.getProgress());
				if (failure) {
					subject.append("FAILURE: ");
					subject.append(activity.getName());
					body.append(user.getFirstName());
					body.append(",\nAn error has occured processing your activity named ");
					body.append(activity.getName());
					body.append(". The problem description is \n\n");
					body.append(reason);
					body.append("\n\nMore details are available at https://n3phele.appspot.com\n\nn3phele");
				} else {
					subject.append(activity.getName());
					subject.append(" has completed.");
					body.append(user.getFirstName());
					body.append(",\n\nYour activity named ");
					body.append(activity.getName());
					body.append(" has completed sucessfully.\n\nTotal elapsed time was ");
					Date start = progress.getStarted();
					Date finished = progress.getCompleted();
					Long duration = finished.getTime() - start.getTime();
					if (duration > 1000 * 60 * 60 * 24) {
						double days =  Math.round(10 * duration / (1000 * 60 * 60 * 24)) / 10.0;
						body.append(Double.toString(days));
						body.append(days > 1 ? " days" : " day");
					} else if (duration > 1000 * 60 * 60) {
						double hours = Math.round(10 * duration
								/ (1000 * 60 * 60.0)) / 10.0;
						body.append(Double.toString(hours));
						body.append(hours > 1.0 ? " hours" : " hour");
					} else if (duration >= 1000 * 60) {
						double minutes = Math.round(10 * duration
								/ (1000 * 60.0)) / 10.0;
						body.append(Double.toString(minutes));
						body.append(minutes > 1.0 ? " minutes" : " minute");

					} else if (duration >= 1000) {
						double seconds = Math.round(10 * duration / (1000.0)) / 10.0;
						body.append(Double.toString(seconds));
						body.append(seconds > 1.0 ? " seconds" : " second");
					} else {
						body.append(Long.toString(duration));
						body.append(duration > 1 ? " milliseconds"
								: " millisecond");
					}
					body.append(".\n\nn3phele\n--\nhttps://n3phele.appspot.com\n\n");
				}

				Properties props = new Properties();
				Session session = Session.getDefaultInstance(props, null);

				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress("n3phele@gmail.com", "n3phele"));
				msg.addRecipient(Message.RecipientType.TO,
						new InternetAddress(user.getName(), user.getFirstName()
								+ " " + user.getLastName()));
				msg.setSubject(subject.toString());
				msg.setText(body.toString());
				Transport.send(msg);
			} catch (AddressException e) {
				log.log(Level.SEVERE,
						"Email to " + user.getName() + " " + user.getUri(), e);
			} catch (MessagingException e) {
				log.log(Level.SEVERE,
						"Email to " + user.getName() + " " + user.getUri(), e);
			} catch (UnsupportedEncodingException e) {
				log.log(Level.SEVERE,
						"Email to " + user.getName() + " " + user.getUri(), e);
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Email for activity " + activity.getUri(), e);
			}

		}
	}
	
	/** Refresh the status of active activities.
	 * This entry point will be protected by the restricted admin interface of google. This interface is invoked by the
	 * cron facility in google app engine.
	 * @param id
	 * @return
	 */

	public NameValue do_refresh() {
		NameValue result = new NameValue("refreshed", "0");
		Collection<Activity> collection = dao.activity().getCollection();
		Long dispatched = 0L;
		if(collection.getTotal() > 0) {
			for(Activity a : collection.getElements()) {
				switch(a.getState()) {
				default:
				case INIT:
					continue;
				case CANCELLED:
				case COMPLETE:
				case FAILED:
					break;
				case RUNNING:
				case PENDING:
				case BLOCKED:
					dispatchActivity(a.getId(), false, null);
					dispatched++;
					break;
				}
			}
		}
		log.info("Dispatched "+dispatched);
		result.setValue(Long.toString(dispatched));
		return result;
	}

	
	/*
	 * ------------------------------------------------------------------------------------------ *
	 *                      	== Private & Internal support functions ==
	 * ------------------------------------------------------------------------------------------ *
	 */
	
	public static void dispatchActivity(Long id, boolean failure, String reason) {
		TaskOptions qItem = Builder.withUrl("/admin/worker/activity/dispatch").retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)).param("id", id.toString()).param("failure", Boolean.toString(failure)).method(Method.GET);
		if(reason != null)
			qItem.param("reason", reason);
		QueueFactory.getDefaultQueue().add(qItem);
	}
	
	public Activity executeForUser(ExecuteCommandRequest request, User owner) throws Exception {
		log.info("Execute command "+request+" for user "+owner);
		if(request.getUser()==null)
			request.setUser(owner.getName());
		if(!owner.getName().equals(request.getUser())) {
			if(owner.isAdmin()) {
				owner = dao.user().load(request.getUser(), UserResource.Root);
			} else {
				throw new UnprocessableEntityException("Command owner is not requestor");
			}
		}
		
		Activity activity = new Activity(request.getName(), request.isNotify(), owner, request.getCommand(), request.getAccount(), request.getProfileId(), 
				request.getParameters(), request.getInputFiles(), request.getOutputFiles());

		realize(activity);
		activity.setState(ActionState.INIT);
		dao.activity().update(activity);
		dispatchActivity(activity.getId(), false, null);
		log.info("Created "+activity);
		return activity;
	}
	
//	private Activity realizev0(Activity activity) {
//		/*
//		 * Step 0. Validate arguments and access
//		 */
//		User user = dao.user().load(activity.getOwner());
//		Account myAccount = dao.account().load(activity.getAccount(), user);
//		Command myCommand = dao.command().load(activity.getCommand(), user);
//		Cloud myCloud = dao.cloud().load(myAccount.getCloud());
//		log.fine(String.format("Realize activity for command %s on cloud %s for user %s account %s",
//				myCommand.getName(), myCloud.getName(), user.getName(), myAccount.getName()));
//
//		CloudProfile profile = null;
//		for(CloudProfile p : myCommand.getCloudProfiles()) {
//			if(p.getId().equals(activity.getCloudProfileId())) {
//				if(p.getCloud().equals(myAccount.getCloud())) {
//					profile = p;
//					break;
//				} else {
//					throw new IllegalArgumentException("CloudProfile not associated with Account");
//				}
//			}
//		}
//		if(profile == null)
//			throw new IllegalArgumentException("Cloud profile Id "+activity.getCloudProfileId()+" not associated with command "+activity.getCommand());
//
//		if(profile.getActions() == null || profile.getActions().size() == 0) {
//			throw new IllegalArgumentException("No defined actions");
//		}
//		
//		/*
//		 * Check the specified files
//		 */
//		/*
//		 * Build maps of the input and output files specified as part of the
//		 * command execution invocation. These files represent the data sources to be used.
//		 */
//		Map<String, FileRef> commandLineSpecifiedInputFiles = new HashMap<String, FileRef>();
//		Map<String, FileRef> commandLineSpecifiedOutputFiles = new HashMap<String, FileRef>();
//		FileSpecification lastRepo = null;
//		try {
//
//			if(activity.getInputs() != null) {
//				for(FileSpecification f : activity.getInputs()) {
//					lastRepo = f;
//					FileRef fileRef = f.toFileRef(dao, user.getUri());
//					getRepositoryResource().updateFileSpecificationInfo(f, user.getUri());
//					fileRef.setLength(f.getSize());
//					fileRef.setModified(f.getModified());
//					commandLineSpecifiedInputFiles.put(f.getName(), fileRef);
//				}
//			}
//
//			if(activity.getOutputs() != null) {
//				for(FileSpecification f : activity.getOutputs()) {
//					lastRepo = f;
//					FileRef fileRef = f.toFileRef(dao, user.getUri());
//					commandLineSpecifiedOutputFiles.put(f.getName(), fileRef);
//				}
//			}
//		} catch (NotFoundException nf) {
//			throw new UnprocessableEntityException("Invalid repository or file specification: "+lastRepo);
//
//		} catch (Exception e) {
//			log.log(Level.WARNING, "Error processing activity "+activity.toString(), e);
//			throw new UnprocessableEntityException("Invalid file specification: "+lastRepo+" cause:"+e.getMessage());
//		}
//		
//		
//		
//		
//		dao.activity().add(activity);
//
//		/*
//		 * Step 1. Merge parameters
//		 * ------------------------
//		 */
//
//		Map<String, TypedParameter> parameterMap = TypedParameter.asMap(myCommand.getExecutionParameters());
//
//		if(activity.getParameters() != null) {
//			for(NameValue t : activity.getParameters()) {
//				if(parameterMap.containsKey(t.getKey())) {
//					TypedParameter p = parameterMap.get(t.getKey());
//					p.setValue(t.getValue());
//				}
//			}
//		}
//		activity.setContext(TypedParameter.asList(parameterMap));
//
//		/*
//		 * Step 2. Create progress object
//		 * ------------------------------
//		 * The progress object is used to track the progress of the command
//		 * execution. It is a lightweight object that can be easily monitored
//		 * by an external process such as a UI client.
//		 */
//
//		Progress progress = new Progress(activity.getName(), myCommand.getName(), myCommand.getDescription(), activity.getUri(), user.getUri());
//		dao.progress().add(progress);
//		activity.setProgress(progress.getUri());
//
//		/*
//		 * Step 3. Build actions and file dependency list
//		 * ----------------------------------------------
//		 */
//
//
//		/*
//		 * Initialize the vmResidentFiles.
//		 * The vmResidentFiles holds the list of local (vm resident) files that are produced or consumed
//		 * by the specified actions. 
//		 */
//		Map<String, FileMonitor> vmResidentFiles = new HashMap<String, FileMonitor>();
//		for(ActionSpecification w : profile.getActions()) {
//			List<FileSpecification> inputs = w.getInputFiles();
//			if(inputs != null) {
//				for(FileSpecification i : inputs) {
//					if(!vmResidentFiles.containsKey(i.getName())) {
//						vmResidentFiles.put(i.getName(), new FileMonitor(dao, i,user.getUri()));
//					}
//				}
//			}
//			List<FileSpecification> outputs = w.getOutputFiles();
//			if(outputs != null) {
//				for(FileSpecification i : outputs) {
//					if(!vmResidentFiles.containsKey(i.getName())) {
//						vmResidentFiles.put(i.getName(), new FileMonitor(dao, i,user.getUri()));
//					}
//				}
//			}
//		}
//
//		/*
//		 * Instantiate actions and build dependency inter-relationships.
//		 */
//		Map<String, Action> actionMap = new HashMap<String, Action>();
//		for(ActionSpecification actionSpec : profile.getActions()) {
//			//
//			//	FIXME: Need to test for duplicate action names
//			//
//			Action action = new Action(dao, actionSpec, activity, progress.getUri());
//			dao.action().add(action);
//			//activity.addAction(action); // not needed due to topological sort below
//			actionMap.put(action.getName(), action);
//		}
//
//
//		/*
//		 * The input files for the action execution that were specified as part of the activity invocation
//		 * are associated with the local files in the vmResidentFiles. File transfer actions are added to the
//		 * activity in order to create these local files on the VM. Similarly, File transfer actions are added
//		 * so that output files are transferred to nominated repository locations.
//		 * 
//		 */
//		String missingFiles = "";
//		for(ActionSpecification actionSpec : profile.getActions()) {
//			Action action = actionMap.get(actionSpec.getName());
//			List<FileRef> inputs = action.getActionTask().getInputFiles();
//
//			if(inputs != null && inputs.size() > 0) {
//				ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
//				String agentURI =  null;
//				for(TypedParameter t : credentialSource) {
//					if(t.getName().equals("agentBaseUrl")) {
//						agentURI = t.value();
//						break;
//					}
//				}
//				for(FileRef i : inputs) {
//					FileMonitor vmResidentFile = vmResidentFiles.get(i.getName());
//					if(commandLineSpecifiedInputFiles.containsKey(i.getName())) {
//						vmResidentFile.addConsumer(action, agentURI);
//						URI xferURI = vmResidentFile.producer(agentURI);
//						Action xfer = null;
//						if(xferURI == null) {
//							 FileRef src = commandLineSpecifiedInputFiles.get(i.getName());
//							 i.setLength(src.getLength());
//							 i.setModified(src.getModified());
//							 xfer = Action.newFileTransferTask(
//									i.getName(), //+"_"+action.getName(),
//									myCommand.getName()+"_"+profile.getId()+"_"+i.getName()+"_"+action.getName(),
//									user.getUri(),
//									src, 
//									i, 
//									progress.getUri(), 
//									activity.getUri(),
//									credentialSource
//							);	
//							actionMap.put(xfer.getName(), xfer);
//							dao.action().add(xfer);
//							//activity.addAction(xfer);
//							vmResidentFile.addProducer(xfer, agentURI);
//						} else {
//							xfer = dao.action().load(xferURI);
//							if(actionMap.containsKey(xfer.getName())) {
//								xfer = actionMap.get(xfer.getName());
//							} else {
//								log.severe("Expected actionMap to contain xfer task "+xfer.getName());
//							}
//						}
//						action.hasDependencyOn(xfer);
//						dao.action().update(xfer);
//						dao.action().update(action);
//
//					} else {
//						if(!i.isOptional()) {
//							log.severe("Profile defines file "+i.toString()+" that is not specified in the execution infiles "+commandLineSpecifiedInputFiles.keySet());
//							if(missingFiles.length() != 0)
//								missingFiles += ", ";
//							missingFiles += i.toString();
//						} else {
//							log.severe("Optional file "+i.toString()+" that is not specified in the execution infiles "+commandLineSpecifiedInputFiles.keySet());
//
//						}
//					}
//				}
//			}
//			List<FileRef> outputs = action.getActionTask().getOutputFiles();
//			if(outputs != null && outputs.size() > 0) {
//				ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
//				for(FileRef i : outputs) {
//					FileMonitor f = vmResidentFiles.get(i.getName());
//					try {
//						f.addProducer(action,"output");
//					} catch (IllegalArgumentException e) {
//						log.severe("Processing action "+action.getName()+":Output File "+i.getName()+" has multiple producers. Processing "+outputs);
//						rollbackActivityCreation(activity, progress, actionMap.values());
//						throw new UnprocessableEntityException("Processing action "+action.getName()+": File "+i.getName()+" has multiple producers");
//					}
//					if(commandLineSpecifiedOutputFiles.containsKey(i.getName())) {
//						Action xfer = Action.newFileTransferTask(
//								/* action.getName()+"_"+*/i.getName(),
//								myCommand.getName()+"_"+profile.getId()+"_"+action.getName()+"_"+i.getName(),
//								user.getUri(),
//								i, 
//								commandLineSpecifiedOutputFiles.get(i.getName()), 
//								progress.getUri(), 
//								activity.getUri(),
//								credentialSource
//						);
//						actionMap.put(xfer.getName(), xfer);
//						dao.action().add(xfer);
//						//activity.addAction(xfer);
//						f.addConsumer(xfer,"output");
//						xfer.hasDependencyOn(action);
//						dao.action().update(action);
//						dao.action().update(xfer);
//					}
//				}
//			}
//		}
//		if(missingFiles.length() > 0) {
//			rollbackActivityCreation(activity, progress, actionMap.values());
//			throw new UnprocessableEntityException("Missing input file specification: "+missingFiles);
//		}
//		/*
//		 * Step 4. Create dependency association for parameter values
//		 * ---------------------------------------------------------
//		 * 
//		 */
//		
//		/*
//		 *  Update the join action such that it references the other actions in the iteration set, and
//		 *  update the dependencies
//		 */
//		
//		for(Action a : actionMap.values()) {
//			if(a.getKind() == ActionType.join) {
//				updateJoinDependencies(a, actionMap);
//			}
//		}
//		
//		String unknownRefs="";
//		Pattern variableReference = Pattern.compile("\\$\\<([A-Za-z0-9_-]+)(\\.[A-Za-z0-9_][A-Za-z0-9_-]*)\\>");
//		for(Action action : actionMap.values()) {
//			if(action.getActionTask().getExecutionParameters() != null) {
//				log.warning("Dependency matching for "+action.getName());
//				for(TypedParameter p : action.getActionTask().getExecutionParameters()) {
//					log.info("...Dependency for "+p.getName()+" with value "+p.value()+" default "+p.defaultValue());
//					Matcher match = variableReference.matcher(p.value()+" "+p.defaultValue());
//					while(match.find()) {
//						String source = match.group(1);
//						log.info("......Match with "+source);
//						if(actionMap.containsKey(source) || !parameterMap.containsKey(source) ) {
//							Action src = actionMap.get(source);
//							if(src != null) {
//								log.info("......Found action "+src.getName());
//								action.hasDependencyOn(src);
//							} else {
//								if(unknownRefs.length() > 0) {
//									unknownRefs += ", ";
//								}
//								log.severe("Unresolved reference for action "+action.getName()+" parameter "+p.getName()
//										+" reference "+source+" in "+p.getValue()+" "+p.getDefaultValue());
//								unknownRefs += action.getName()+":"+p.getName()+":"+source;
//							}
//						}
//					}
//				}
//			}
//			if(unknownRefs.length() > 0) {
//				rollbackActivityCreation(activity, progress, actionMap.values());
//				throw new UnprocessableEntityException("Unknown references: "+unknownRefs);
//			}
//		}
//
//
//		/*
//		 *  Step 5. Persist actions
//		 * ------------------------
//		 * 
//		 */
//		for(Action action : actionMap.values()) {
//			action.getActionTaskImpl().setParentAction(action.getUri());
//			action.getActionTask().getDuration();
//			dao.action().update(action);
//		}
//
//		/*
//		 *  Step 6. Topological sort and persist of activity
//		 * -------------------------------------------------
//		 * 
//		 */
//
//		try {
//			ArrayList<Action> sorted = topologicalSort(actionMap.values());
//			ArrayList<URI> sortedURI = new ArrayList<URI>(sorted.size());
//			for(Action a : sorted) {
//				sortedURI.add(a.getUri());
//			}
//			activity.setActions(sortedURI);
//
//		} catch (UnprocessableEntityException e) {
//			rollbackActivityCreation(activity, progress, actionMap.values());
//			throw e;
//		}
//		activity.setFileTable(fileMapToTable(vmResidentFiles));
//		dao.activity().update(activity);
//
//		return activity;
//	}
//	
	
	
	private Activity realize(Activity activity) {
		/*
		 * Step 0. Validate arguments and access
		 */
		User user = dao.user().load(activity.getOwner());
		Account myAccount = dao.account().load(activity.getAccount(), user);
		Command myCommand = dao.command().load(activity.getCommand(), user);
		Cloud myCloud = dao.cloud().load(myAccount.getCloud());
		log.fine(String.format("Realize activity for command %s on cloud %s for user %s account %s",
				myCommand.getName(), myCloud.getName(), user.getName(), myAccount.getName()));

		CloudProfile profile = null;
		for(CloudProfile p : myCommand.getCloudProfiles()) {
			if(p.getId().equals(activity.getCloudProfileId())) {
				if(p.getCloud().equals(myAccount.getCloud())) {
					profile = p;
					break;
				} else {
					throw new IllegalArgumentException("CloudProfile not associated with Account");
				}
			}
		}
		if(profile == null)
			throw new IllegalArgumentException("Cloud profile Id "+activity.getCloudProfileId()+" not associated with command "+activity.getCommand());

		if(profile.getActions() == null || profile.getActions().size() == 0) {
			throw new IllegalArgumentException("No defined actions");
		}
		
		/*
		 * Check the specified files
		 */
		/*
		 * Build maps of the input and output files specified as part of the
		 * command execution invocation. These files represent the data sources to be used.
		 */
		Map<String, FileRef> commandLineSpecifiedInputFiles = new HashMap<String, FileRef>();
		Map<String, FileRef> commandLineSpecifiedOutputFiles = new HashMap<String, FileRef>();
		FileSpecification lastRepo = null;
		long totalInput = 0;
		try {
			if(activity.getInputs() != null) {
				for(FileSpecification f : activity.getInputs()) {
					lastRepo = f;
					FileRef fileRef = f.toFileRef(dao, user);
					getRepositoryResource().updateFileSpecificationInfo(f, user);
					fileRef.setLength(f.getSize());
					fileRef.setModified(f.getModified());
					totalInput += f.getSize();
					commandLineSpecifiedInputFiles.put(f.getName(), fileRef);
				}
			}

			if(activity.getOutputs() != null) {
				for(FileSpecification f : activity.getOutputs()) {
					lastRepo = f;
					FileRef fileRef = f.toFileRef(dao, user);
					commandLineSpecifiedOutputFiles.put(f.getName(), fileRef);
				}
			}
		} catch (NotFoundException nf) {
			throw new UnprocessableEntityException("Invalid repository or file specification: "+lastRepo);

		} catch (Exception e) {
			log.log(Level.WARNING, "Error processing activity "+activity.toString(), e);
			throw new UnprocessableEntityException("Invalid file specification: "+lastRepo+" cause:"+e.getMessage());
		}		
		
		dao.activity().add(activity);

		/*
		 * Step 1. Merge parameters
		 * ------------------------
		 */

		Map<String, TypedParameter> parameterMap = TypedParameter.asMap(myCommand.getExecutionParameters());

		if(activity.getParameters() != null) {
			for(NameValue t : activity.getParameters()) {
				if(parameterMap.containsKey(t.getKey())) {
					TypedParameter p = parameterMap.get(t.getKey());
					p.setValue(t.getValue());
				}
			}
		}
		activity.setContext(TypedParameter.asList(parameterMap));

		/*
		 * Step 2. Create progress object
		 * ------------------------------
		 * The progress object is used to track the progress of the command
		 * execution. It is a lightweight object that can be easily monitored
		 * by an external process such as a UI client.
		 */

		Progress progress = new Progress(activity.getName(), myCommand.getName(), myCommand.getDescription(), activity.getUri(), user.getUri());
		progress.setDuration(estimateExecutionTime(myCommand.getName(), totalInput));
		dao.progress().add(progress);
		activity.setProgress(progress.getUri());

		/*
		 * Step 3. Build actions and file dependency list
		 * ----------------------------------------------
		 */


		/*
		 * Initialize the vmResidentFiles.
		 * The vmResidentFiles holds the list of local (vm resident) files that are produced or consumed
		 * by the specified actions. 
		 */
		Map<String, FileMonitor> vmResidentFiles = new HashMap<String, FileMonitor>();
		for(ActionSpecification w : profile.getActions()) {
			List<FileSpecification> inputs = w.getInputFiles();
			if(inputs != null) {
				for(FileSpecification i : inputs) {
					log.info("Processing input Action fileSpec "+i+" "+vmResidentFiles);
					if(!vmResidentFiles.containsKey(i.getName())) {
						vmResidentFiles.put(i.getName(), new FileMonitor(dao, i,user));
					}
				}
			}
			List<FileSpecification> outputs = w.getOutputFiles();
			if(outputs != null) {
				for(FileSpecification i : outputs) {
		//			log.info("Processing output Action fileSpec "+i+" "+vmResidentFiles);
					if(!vmResidentFiles.containsKey(i.getName())) {
						vmResidentFiles.put(i.getName(), new FileMonitor(dao, i,user));
					}
				}
			}
		}

		/*
		 * Instantiate actions and build dependency inter-relationships.
		 */
		Map<String, Action> actionMap = new HashMap<String, Action>();
		Map<URI, Action> actionUriMap = new HashMap<URI, Action>();
		for(ActionSpecification actionSpec : profile.getActions()) {

			Action action = new Action(dao, actionSpec, activity, progress.getUri());
			if(actionMap.containsKey(action.getName())) {
				log.severe("Duplicate action named "+action.getName());
				rollbackActivityCreation(activity, progress, actionMap.values());
				throw new UnprocessableEntityException("Duplicate action named "+action.getName());
			}
			dao.action().add(action);
			//activity.addAction(action); // not needed due to topological sort below
			actionMap.put(action.getName(), action);
			actionUriMap.put(action.getUri(), action);
		}

		
		/* 
		 * Process actions tracking their producer/consumer association
		 * to the vmResident files. 
		 * 
		 */

		for(ActionSpecification actionSpec : profile.getActions()) {
			Action action = actionMap.get(actionSpec.getName());
			List<FileRef> inputs = action.getActionTask().getInputFiles();

			if(inputs != null && inputs.size() > 0) {
				ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
				String agentURI =  null;
				for(TypedParameter t : credentialSource) {
					if(t.getName().equals("agentBaseUrl")) {
						agentURI = t.value();
						break;
					}
				}
				for(FileRef i : inputs) {
					FileMonitor vmResidentFile = vmResidentFiles.get(i.getName());
					vmResidentFile.addConsumer(action, agentURI);
				}
			}
			List<FileRef> outputs = action.getActionTask().getOutputFiles();
			if(outputs != null && outputs.size() > 0) {
				ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
				String agentURI =  null;
				for(TypedParameter t : credentialSource) {
					if(t.getName().equals("agentBaseUrl")) {
						agentURI = t.value();
						break;
					}
				}
				for(FileRef i : outputs) {
					FileMonitor f = vmResidentFiles.get(i.getName());
					try {
						f.addProducer(action,agentURI);
					} catch (IllegalArgumentException e) {
						log.severe("Processing action "+action.getName()+":Output File "+i.getName()+" has multiple producers. Processing "+outputs);
						rollbackActivityCreation(activity, progress, actionMap.values());
						throw new UnprocessableEntityException("Processing action "+action.getName()+": File "+i.getName()+" has multiple producers");
					}
				}
			}
		}
		

		/* . 
		 * File transfer operations are added to copy externally specified files into the
		 * context of the actions that need to consume them. These file transfer actions are
		 * marked as producers for the particular vmResidentFile contexts
		 */
		
		for(FileRef src : commandLineSpecifiedInputFiles.values()) {
			FileMonitor vmResidentFile = vmResidentFiles.get(src.getName());
			Map<String, List<String>> consumers = vmResidentFile.getConsumers();
			if(consumers != null && !consumers.isEmpty()) {
				/*
				 * Create a file transfer task for each consumer context
				 */
				for(Map.Entry<String, List<String>> entry : consumers.entrySet()) {
					String context = entry.getKey();
					List<String> uris = entry.getValue();
					if(uris != null && uris.size() != 0) {
						URI consumerUri = URI.create(uris.get(0));
						Action consumer = actionUriMap.get(consumerUri);
						Action xfer = Action.newFileTransferTask(
								src.getName(),
								myCommand.getName()+"-"+src.getName(),
								user.getUri(),
								src, 
								new FileRef(vmResidentFile), 
								progress.getUri(), 
								activity.getUri(),
								createCredentialSource(consumer)
							);	
						dao.action().add(xfer);
						actionMap.put(xfer.getName(), xfer);
						actionUriMap.put(xfer.getUri(), xfer);

						try {
							vmResidentFile.addProducer(xfer, context);
						} catch (IllegalArgumentException e) {
							log.severe("Processing action "+consumer.getName()+":Output File "+vmResidentFile.getName()+" has multiple producers.");
							rollbackActivityCreation(activity, progress, actionMap.values());
							throw new UnprocessableEntityException("Processing action "+consumer.getName()+": File "+vmResidentFile.getName()+" has multiple producers");
						}
					}
				}
			}
		}
		
		/* 
		 * Similarly, File transfer actions are added so that output files are transferred to command line 
		 * nominated repository locations. These file transfer tasks are added as consumers on the vmresident files. 
		 */
		String nullFiles = "";
		for(FileRef dest : commandLineSpecifiedOutputFiles.values()) {
			FileMonitor vmResidentFile = vmResidentFiles.get(dest.getName());
			Map<String, String> producers = vmResidentFile==null?null:vmResidentFile.getProducer();
			if(producers != null && !producers.isEmpty()) {
				/*
				 * Create a file transfer task from the first producer context
				 */
				if(producers.size() > 1) {
					log.severe("File "+vmResidentFile.getName()+" has the following producer contexts: "+producers.keySet());
					Map<String, List<String>> consumers = vmResidentFile.getConsumers();
					log.severe("The file has the following consumer contexts: "+(consumers==null?null:consumers.keySet()));
				}
				for(Map.Entry<String, String> entry : producers.entrySet()) {
					String context = entry.getKey();

					URI producerUri = URI.create(entry.getValue());
					Action producer = actionUriMap.get(producerUri);
					Action xfer = Action.newFileTransferTask(
							dest.getName(),
							myCommand.getName()+"-"+dest.getName(),
							user.getUri(),
							new FileRef(vmResidentFile), 
							dest,
							progress.getUri(), 
							activity.getUri(),
							createCredentialSource(producer)
						);	
					dao.action().add(xfer);
					actionMap.put(xfer.getName(), xfer);
					actionUriMap.put(xfer.getUri(), xfer);
					vmResidentFile.addConsumer(xfer, context);
					break; // only do the first
				}
			} else {
				log.severe("Specified file "+vmResidentFile+" has no producers");
				if(nullFiles.length() != 0)
					nullFiles += ", ";
				nullFiles += vmResidentFile;
			}
		}
		if(nullFiles.length() > 0) {
			rollbackActivityCreation(activity, progress, actionMap.values());
			throw new UnprocessableEntityException("Missing producers for files: "+nullFiles);
		}
		
		/*
		 * TODO: The gap in the logic is for a file that is produced in one context that
		 * needs to be consumed in another. In this case we don't have an appropriate agent to agent
		 * file transfer mechanism. This is an analogy to a pipe.
		 */

		/*
		 * Update the actions dependencies based on the produced and consumed files.
		 * Identify any missing files in the process
		 */
		String missingFiles = "";
		for(FileMonitor vmResidentFile: vmResidentFiles.values()) {
			if(vmResidentFile.getProducer() != null && !vmResidentFile.getProducer().isEmpty()) {
				Map<String, String> producers = vmResidentFile.getProducer();
				Map<String, List<String>> consumers = vmResidentFile.getConsumers();
				if(consumers != null) {
					for(Map.Entry<String, String> tuple : producers.entrySet()) {
						String context = tuple.getKey();
						Action producer = actionUriMap.get(URI.create(tuple.getValue()));
						List<String> consumerUris = consumers.get(context);
					//	log.info("Context = "+context+" producer is "+producer+" consumers "+consumers+" seeking "+consumerUris+" actionUriMap"+actionUriMap);
	
						for(String uri : consumerUris) {
							Action consumer = actionUriMap.get(URI.create(uri));
							consumer.hasDependencyOn(producer);
						}
					}
				}
			} else {
				if(!vmResidentFile.isOptional()) {
					log.severe("Profile defines file "+vmResidentFile.toString()+" that is not specified in the execution infiles "+commandLineSpecifiedInputFiles.keySet());
					if(missingFiles.length() != 0)
						missingFiles += ", ";
					missingFiles += vmResidentFile.toString();
				} else {
					log.severe("Optional file "+vmResidentFile.toString()+" that is not specified in the execution infiles "+commandLineSpecifiedInputFiles.keySet());
				}
			}
		}
		if(missingFiles.length() > 0) {
			rollbackActivityCreation(activity, progress, actionMap.values());
			throw new UnprocessableEntityException("Missing input file specification: "+missingFiles);
		}
		

		/*
		 * Step 4. Create dependency association for parameter values
		 * ---------------------------------------------------------
		 * 
		 */
		
		/*
		 *  Update the join action such that it references the other actions in the iteration set, and
		 *  update the dependencies
		 */
		
		for(Action a : actionMap.values()) {
			if(a.getKind() == ActionType.join) {
				updateJoinDependencies(a, actionMap);
			}
		}
		
		String unknownRefs="";
		Pattern variableReference = Pattern.compile("\\$\\<([A-Za-z0-9_-]+)(\\.[A-Za-z0-9_][A-Za-z0-9_-]*)\\>");
		for(Action action : actionMap.values()) {
			if(action.getActionTask().getExecutionParameters() != null) {
				log.warning("Dependency matching for "+action.getName());
				for(TypedParameter p : action.getActionTask().getExecutionParameters()) {
					log.info("...Dependency for "+p.getName()+" with value "+p.value()+" default "+p.defaultValue());
					Matcher match = variableReference.matcher(p.value()+" "+p.defaultValue());
					while(match.find()) {
						String source = match.group(1);
						log.info("......Match with "+source);
						if(actionMap.containsKey(source) || !parameterMap.containsKey(source) ) {
							Action src = actionMap.get(source);
							if(src != null) {
								log.info("......Found action "+src.getName());
								action.hasDependencyOn(src);
							} else {
								if(unknownRefs.length() > 0) {
									unknownRefs += ", ";
								}
								log.severe("Unresolved reference for action "+action.getName()+" parameter "+p.getName()
										+" reference "+source+" in "+p.getValue()+" "+p.getDefaultValue());
								unknownRefs += action.getName()+":"+p.getName()+":"+source;
							}
						}
					}
				}
			}
			if(unknownRefs.length() > 0) {
				rollbackActivityCreation(activity, progress, actionMap.values());
				throw new UnprocessableEntityException("Unknown references: "+unknownRefs);
			}
		}


		/*
		 *  Step 5. Persist actions
		 * ------------------------
		 * 
		 */
		for(Action action : actionMap.values()) {
			action.getActionTaskImpl().setParentAction(action.getUri());
			//action.getActionTask().getDuration();
			dao.action().update(action);
		}

		/*
		 *  Step 6. Topological sort and persist of activity
		 * -------------------------------------------------
		 * 
		 */

		try {
			ArrayList<Action> sorted = topologicalSort(actionMap.values());
			ArrayList<URI> sortedURI = new ArrayList<URI>(sorted.size());
			for(Action a : sorted) {
				sortedURI.add(a.getUri());
			}
			activity.setActions(sortedURI);

		} catch (UnprocessableEntityException e) {
			rollbackActivityCreation(activity, progress, actionMap.values());
			throw e;
		}
		activity.setFileTable(fileMapToTable(vmResidentFiles));
		dao.activity().update(activity);
		return activity;
	}
	
	private RepositoryResource repositoryResource;
	private RepositoryResource getRepositoryResource() {
		if(repositoryResource == null)
			repositoryResource = new RepositoryResource(dao);
		return repositoryResource;
	}

	public Activity assimilatev0(Action iterator, URI activityURI, List<Action> spawn) throws NotFoundException,InterruptedException,UnprocessableEntityException   {
		Activity activity = null;
		List<Action> assimilateList = new ArrayList<Action> ();
		try {
			Activity candidate = dao.activity().load(activityURI);
			while(true) {
				try {
					activity = dao.activity().lock(candidate.getId());
					break;
				} catch (NotFoundException e) {
					throw e;
				} catch (ConcurrentModificationException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						break;
					}
				}
			}	
			if(activity == null) throw new InterruptedException("Awaiting lock on "+activityURI);
			/*
			 * Step 0. Get command context information
			 */
			User user = dao.user().load(activity.getOwner());
			Account myAccount = dao.account().load(activity.getAccount(), user);
			Command myCommand = dao.command().load(activity.getCommand(), user);
			Cloud myCloud = dao.cloud().load(myAccount.getCloud());
			log.fine(String.format("Assimilate new actions for activity of command %s on cloud %s for user %s account %s",
					myCommand.getName(), myCloud.getName(), user.getName(), myAccount.getName()));
	
			CloudProfile profile = null;
			for(CloudProfile p : myCommand.getCloudProfiles()) {
				if(p.getId().equals(activity.getCloudProfileId())) {
					if(p.getCloud().equals(myAccount.getCloud())) {
						profile = p;
						break;
					} else {
						throw new IllegalArgumentException("CloudProfile not associated with Account");
					}
				}
			}
			if(profile == null)
				throw new IllegalArgumentException("Cloud profile Id "+activity.getCloudProfileId()+" not associated with command "+activity.getCommand());
	
			if(profile.getActions() == null || profile.getActions().size() == 0) {
				throw new IllegalArgumentException("No defined actions");
			}
			
			/*
			 * Step 1. Get parameters
			 * ------------------------
			 */
	
			Map<String, TypedParameter> parameterMap = TypedParameter.asMap(activity.getContext());
	
			/*
			 * Step 2. Create progress object
			 * ------------------------------
			 * The progress object is used to track the progress of the command
			 * execution. It is a lightweight object that can be easily monitored
			 * by an external process such as a UI client.
			 */
			
			
			
			Progress progress = dao.progress().load(activity.getProgress());
			
			
			
			/*
			 * Step 3. Build actions and file dependency list
			 * ----------------------------------------------
			 */
	
			/*
			 * Build maps of the input and output files specified as part of the
			 * command execution invocation. These files represent the data sources to be used.
			 */
	
			Map<String, FileRef> inputMap = new HashMap<String, FileRef>();
			if(activity.getInputs() != null) {
				for(FileSpecification f : activity.getInputs()) {
					inputMap.put(f.getName(), f.toFileRef(dao, user));
				}
			}
			Map<String, FileRef> outputMap = new HashMap<String, FileRef>();
			if(activity.getOutputs() != null) {
				for(FileSpecification f : activity.getOutputs()) {
					outputMap.put(f.getName(), f.toFileRef(dao, user));
				}
			}
			/*
			 * Create the fileMap.
			 * The fileMap holds the list of local (vm resident) files that are produced or consumed
			 * by the command and their associations
			 */
			Map<String, FileMonitor> fileMap = fileTableToMap(activity.getFileTable());
	
			/*
			 * Instantiate actions and build dependency inter-relationships.
			 */
			Map<String, Action> actionMap = new HashMap<String, Action>();
			for(Action action : spawn) {
				dao.action().add(action);
				actionMap.put(action.getName(), action);
				assimilateList.add(action);
			}
			
			int k = 0;
			for(Action action : dao.action().loadAll(activity.getActions())) {
				actionMap.put(action.getName(), action);
				k++;
			}
			if(k != activity.getActions().size())
				log.severe("Loaded "+k+" actions. List is "+activity.getActions().size()+" long");
			
			actionMap.put(iterator.getName(), iterator); // replace map object with iterator copy passed in
			Set<Action> needsUpdate = new HashSet<Action>();
	
			String missingFiles = "";
			for(Action action : spawn) {
				List<FileRef> inputs = action.getActionTask().getInputFiles();
				if(inputs != null && inputs.size()>0) {
					ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
					String agentURI =  null;
					for(TypedParameter t : credentialSource) {
						if(t.getName().equals("agentBaseUrl")) {
							agentURI = t.value();
							break;
						}
					}
					for(FileRef i : inputs) {
						FileMonitor f = fileMap.get(i.getName());
						if(inputMap.containsKey(i.getName())) {
							f.addConsumer(action, agentURI);
							URI xferURI = f.producer(agentURI);
							Action xfer = null;
							if(xferURI == null) {
								 xfer = Action.newFileTransferTask(
										i.getName()+"_"+action.getName(),
										myCommand.getName()+"_"+profile.getId()+"_"+i.getName()+"_"+action.getName(),
										user.getUri(),
										inputMap.get(i.getName()), 
										i, 
										progress.getUri(), 
										activity.getUri(),
										credentialSource
								);	
								actionMap.put(xfer.getName(), xfer);
								dao.action().add(xfer);
								assimilateList.add(xfer);
								f.addProducer(xfer, agentURI);
							} else {
								xfer = dao.action().load(xferURI);
							}
							action.hasDependencyOn(xfer);
							needsUpdate.add(xfer);
	
						} else {
							if(!i.isOptional()) {
								log.severe("Profile defines file "+i.toString()+" that is not specified in the execution infiles "+inputMap.keySet());
								if(missingFiles.length() != 0)
									missingFiles += ", ";
								missingFiles += i.toString();
							} else {
								log.severe("Optional file "+i.toString()+" that is not specified in the execution infiles "+inputMap.keySet());

							}

						}
					}
				}
				List<FileRef> outputs = action.getActionTask().getOutputFiles();
				if(outputs != null && outputs.size()>0) {
					ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
					for(FileRef i : outputs) {
						FileMonitor f = fileMap.get(i.getName());
						f.addProducer(action,"output");
						if(outputMap.containsKey(i.getName())) {
							Action xfer = Action.newFileTransferTask(
									action.getName()+"_"+i.getName(),
									myCommand.getName()+"_"+profile.getId()+"_"+action.getName()+"_"+i.getName(),
									user.getUri(),
									i, 
									outputMap.get(i.getName()), 
									progress.getUri(), 
									activity.getUri(),
									credentialSource
							);
							actionMap.put(xfer.getName(), xfer);
							dao.action().add(xfer);
							assimilateList.add(xfer);
							f.addConsumer(xfer,"output");
							xfer.hasDependencyOn(action);
						}
					}
				}
			}
			if(missingFiles.length() > 0) {
				rollbackAssimilate(assimilateList);
				throw new UnprocessableEntityException("Missing input file specification: "+missingFiles);
			}
	
			
			/*
			 * Step 4. Create dependency association for parameter values
			 * ---------------------------------------------------------
			 * 
			 */
			
			/*
			 *  Update the join action such that it references the other actions in the iteration set, and
			 *  update the dependencies
			 */
			
			for(Action a : actionMap.values()) {
				if(a.getKind() == ActionType.join) {
					if(updateJoinDependencies(a, actionMap)) {
						needsUpdate.add(a);
					}
				}
			}	
			
			/*
			 *  Update other parameter related dependencies
			 */
			String unknownRefs="";
			Pattern variableReference = Pattern.compile("\\$\\<([A-Za-z0-9_-]+)(\\.[A-Za-z0-9_][A-Za-z0-9_-]*)\\>");
			for(Action action : spawn) {
				if(action.getActionTask().getExecutionParameters() != null) {
					for(TypedParameter p : action.getActionTask().getExecutionParameters()) {
						Matcher match = variableReference.matcher(p.value()+" "+p.defaultValue());
						while(match.find()) {
							String source = match.group(1);
							if(actionMap.containsKey(source) || !parameterMap.containsKey(source) ) {
								Action src = actionMap.get(source);
								if(src != null) {
									action.hasDependencyOn(src);
									needsUpdate.add(src);
									log.info("Action "+action.getName()+" depends on "+src.getName()+" for "+p.getName());
								} else {
									if(unknownRefs.length() > 0) {
										unknownRefs += ", ";
									}
									log.severe("Unresolved reference for action "+action.getName()+" parameter "+p.getName()
											+" reference "+source+" in "+p.getValue()+" "+p.getDefaultValue());
									unknownRefs += action.getName()+":"+p.getName()+":"+source;
								}
							}
						}
					}
				}
				if(unknownRefs.length() > 0) {
					rollbackAssimilate(assimilateList);
					throw new UnprocessableEntityException("Unknown references: "+unknownRefs);
				}
			}


			
			/*
			 *  Step 5. Persist actions
			 * ------------------------
			 * Updates come in 2 categories..
			 * (1) new items & the iterator that triggered the processing
			 * 		.. these can simply be persisted in a bulk operation
			 * (2) other blocked/running items
			 * 		.. these need to be locked and then updated to exclude race conditions
			 * 		.. with the refresh processing
			 * 
			 */
			dao.action().update(iterator);
			for(Action action : assimilateList) {
				action.getActionTaskImpl().setParentAction(action.getUri());
				action.getActionTask().getDuration();
			}
			log.info("needs update contains iterator "+needsUpdate.contains(iterator));
			log.info("iterator is dependency for "+iterator.getActionTaskImpl().getDependencyFor().size());

			dao.action().updateAll(assimilateList);
			dao.action().update(iterator);
			needsUpdate.remove(iterator);
			dao.action().updateDependencies(needsUpdate);
	
			/*
			 *  Step 6. Topological sort and persist of activity
			 * -------------------------------------------------
			 * 
			 */
	
			try {
				/*
				 * Topological sort is destructive, so we put a copy of the
				 * iterator object in the map for this phase
				 */
				Action iteratorCopy = dao.action().load(iterator.getUri());
				actionMap.put(iteratorCopy.getName(), iteratorCopy);
				ArrayList<Action> sorted = topologicalSort(actionMap.values());
				ArrayList<URI> sortedURI = new ArrayList<URI>(sorted.size());
				for(Action a : sorted) {
					sortedURI.add(a.getUri());
				}
				activity.setActions(sortedURI);
	
			} catch (UnprocessableEntityException e) {
				rollbackAssimilate(assimilateList);
				throw e;
			}
			activity.setFileTable(fileMapToTable(fileMap));
			dao.activity().update(activity);
			getActionResource().initiate(assimilateList);
			return activity;
		} finally {
			dao.activity().release(activity);
		}
	}
	
	public Activity assimilate(Action iterator, URI activityURI, List<Action> spawn) throws NotFoundException,InterruptedException,UnprocessableEntityException   {
		Activity activity = null;
		List<Action> assimilateList = new ArrayList<Action> ();
		try {
			Activity candidate = dao.activity().load(activityURI);
			while(true) {
				try {
					activity = dao.activity().lock(candidate.getId());
					break;
				} catch (NotFoundException e) {
					throw e;
				} catch (ConcurrentModificationException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						break;
					}
				}
			}	
			if(activity == null) throw new InterruptedException("Awaiting lock on "+activityURI);
			/*
			 * Step 0. Get command context information
			 */
			User user = dao.user().load(activity.getOwner());
			Account myAccount = dao.account().load(activity.getAccount(), user);
			Command myCommand = dao.command().load(activity.getCommand(), user);
			Cloud myCloud = dao.cloud().load(myAccount.getCloud());
			log.fine(String.format("Assimilate new actions for activity of command %s on cloud %s for user %s account %s",
					myCommand.getName(), myCloud.getName(), user.getName(), myAccount.getName()));
	
			CloudProfile profile = null;
			for(CloudProfile p : myCommand.getCloudProfiles()) {
				if(p.getId().equals(activity.getCloudProfileId())) {
					if(p.getCloud().equals(myAccount.getCloud())) {
						profile = p;
						break;
					} else {
						throw new IllegalArgumentException("CloudProfile not associated with Account");
					}
				}
			}
			if(profile == null)
				throw new IllegalArgumentException("Cloud profile Id "+activity.getCloudProfileId()+" not associated with command "+activity.getCommand());
	
			if(profile.getActions() == null || profile.getActions().size() == 0) {
				throw new IllegalArgumentException("No defined actions");
			}
			
			/*
			 * Step 1. Get parameters
			 * ------------------------
			 */
	
			Map<String, TypedParameter> parameterMap = TypedParameter.asMap(activity.getContext());
	
			/*
			 * Step 2. Create progress object
			 * ------------------------------
			 * The progress object is used to track the progress of the command
			 * execution. It is a lightweight object that can be easily monitored
			 * by an external process such as a UI client.
			 */
			
			
			
			Progress progress = dao.progress().load(activity.getProgress());
			
			
			
			/*
			 * Step 3. Build actions and file dependency list
			 * ----------------------------------------------
			 */
	
			/*
			 * Build maps of the input and output files specified as part of the
			 * command execution invocation. These files represent the data sources to be used.
			 */
	
			Map<String, FileRef> commandLineSpecifiedInputFiles = new HashMap<String, FileRef>();
			if(activity.getInputs() != null) {
				for(FileSpecification f : activity.getInputs()) {
					commandLineSpecifiedInputFiles.put(f.getName(), f.toFileRef(dao, user));
				}
			}
			Map<String, FileRef> commandLineSpecifiedOutputFiles = new HashMap<String, FileRef>();
			if(activity.getOutputs() != null) {
				for(FileSpecification f : activity.getOutputs()) {
					commandLineSpecifiedOutputFiles.put(f.getName(), f.toFileRef(dao, user));
				}
			}
			/*
			 * Initialize the vmResidentFiles.
			 * The vmResidentFiles holds the list of local (vm resident) files that are produced or consumed
			 * by the specified actions. 
			 */
			Map<String, FileMonitor> vmResidentFiles = fileTableToMap(activity.getFileTable());
			for(Action w : spawn) {
				ArrayList<FileRef> inputs = w.getActionTaskImpl().getInputFiles();
				if(inputs != null) {
					for(FileRef i : inputs) {
						if(!vmResidentFiles.containsKey(i.getName())) {
							log.info("Adding input file "+i.getName());
							vmResidentFiles.put(i.getName(), new FileMonitor(i));
						}
					}
				}
				ArrayList<FileRef> outputs = w.getActionTaskImpl().getOutputFiles();
				if(outputs != null) {
					for(FileRef i : outputs) {
						if(!vmResidentFiles.containsKey(i.getName())) {
							log.info("Adding output file "+i.getName());
							vmResidentFiles.put(i.getName(), new FileMonitor(i));
						}
					}
				}
			}
	
			/*
			 * Instantiate actions and build dependency inter-relationships.
			 */
			Map<String, Action> actionMap = new HashMap<String, Action>();
			Map<URI, Action> actionUriMap = new HashMap<URI, Action>();
			for(Action action : spawn) {
				dao.action().add(action);
				actionMap.put(action.getName(), action);
				actionUriMap.put(action.getUri(), action);
				assimilateList.add(action);
			}
			
			int k = 0;
			for(Action action : dao.action().loadAll(activity.getActions())) {
				actionMap.put(action.getName(), action);
				actionUriMap.put(action.getUri(), action);
				k++;
			}
			if(k != activity.getActions().size())
				log.severe("Loaded "+k+" actions. List is "+activity.getActions().size()+" long");
			
			actionMap.put(iterator.getName(), iterator); // replace map object with iterator copy passed in
			Set<Action> needsUpdate = new HashSet<Action>();
			
			
			/* 
			 * Process actions tracking their producer/consumer association
			 * to the vmResident files. 
			 * 
			 */

			for(Action action: spawn) {
				List<FileRef> inputs = action.getActionTask().getInputFiles();

				if(inputs != null && inputs.size() > 0) {
					ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
					String agentURI =  null;
					for(TypedParameter t : credentialSource) {
						if(t.getName().equals("agentBaseUrl")) {
							agentURI = t.value();
							break;
						}
					}
					for(FileRef i : inputs) {
						FileMonitor vmResidentFile = vmResidentFiles.get(i.getName());
						vmResidentFile.addConsumer(action, agentURI);
					}
				}
				List<FileRef> outputs = action.getActionTask().getOutputFiles();
				if(outputs != null && outputs.size() > 0) {
					ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
					String agentURI =  null;
					for(TypedParameter t : credentialSource) {
						if(t.getName().equals("agentBaseUrl")) {
							agentURI = t.value();
							break;
						}
					}
					for(FileRef i : outputs) {
						FileMonitor f = vmResidentFiles.get(i.getName());
						try {
							f.addProducer(action,agentURI);
						} catch (IllegalArgumentException e) {
							log.severe("Processing action "+action.getName()+":Output File "+i.getName()+" has multiple producers. Processing "+outputs);
							rollbackAssimilate(assimilateList);
							throw new UnprocessableEntityException("Processing action "+action.getName()+": File "+i.getName()+" has multiple producers");
						}
					}
				}
			}
			
			/* . 
			 * File transfer operations are added to copy externally specified files into the
			 * context of the actions that need to consume them. These file transfer actions are
			 * marked as producers for the particular vmResidentFile contexts
			 */
			
			for(FileRef src : commandLineSpecifiedInputFiles.values()) {
				FileMonitor vmResidentFile = vmResidentFiles.get(src.getName());
				Map<String, List<String>> consumers = vmResidentFile.getConsumers();
				if(consumers != null && !consumers.isEmpty()) {
					/*
					 * Create a file transfer task for each consumer context
					 */
					for(Map.Entry<String, List<String>> entry : consumers.entrySet()) {
						String context = entry.getKey();
						if(vmResidentFile.producer(context) == null) {
							List<String> uris = entry.getValue();
							URI consumerUri = URI.create(uris.get(0));
							Action consumer = actionUriMap.get(consumerUri);
							Action xfer = Action.newFileTransferTask(
									src.getName(),
									myCommand.getName()+"-"+src.getName(),
									user.getUri(),
									src, 
									new FileRef(vmResidentFile), 
									progress.getUri(), 
									activity.getUri(),
									createCredentialSource(consumer)
								);	
							actionMap.put(xfer.getName(), xfer);
							actionUriMap.put(xfer.getUri(), xfer);
							dao.action().add(xfer);
							needsUpdate.add(xfer);
							try {
								vmResidentFile.addProducer(xfer, context);
							} catch (IllegalArgumentException e) {
								log.severe("Processing action "+consumer.getName()+":Output File "+vmResidentFile.getName()+" has multiple producers.");
								rollbackAssimilate(assimilateList);
								throw new UnprocessableEntityException("Processing action "+consumer.getName()+": File "+vmResidentFile.getName()+" has multiple producers");
							}
						}
					}
				}
			}
			
			/*
			 * Update the actions dependencies based on the produced and consumed files.
			 * Identify any missing files in the process
			 */
			
			for(Action action: spawn) {
				List<FileRef> inputs = action.getActionTask().getInputFiles();
				String missingFiles = "";
				if(inputs != null && inputs.size() > 0) {
					ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
					String agentURI =  null;
					for(TypedParameter t : credentialSource) {
						if(t.getName().equals("agentBaseUrl")) {
							agentURI = t.value();
							break;
						}
					}
					for(FileRef i : inputs) {
						FileMonitor vmResidentFile = vmResidentFiles.get(i.getName());
						URI producerUri = vmResidentFile.producer(agentURI);
						Action producer = actionUriMap.get(producerUri);
						if(producer != null) {
							action.hasDependencyOn(producer);
							needsUpdate.add(producer);
						} else {
							if(!vmResidentFile.isOptional()) {
								log.severe("Profile defines file "+vmResidentFile.toString()+" that is not specified in the execution infiles "+commandLineSpecifiedInputFiles.keySet());
								if(missingFiles.length() != 0)
									missingFiles += ", ";
								missingFiles += vmResidentFile.toString();
							} else {
								log.severe("Optional file "+vmResidentFile.toString()+" that is not specified in the execution infiles "+commandLineSpecifiedInputFiles.keySet());
							}
						}
					}
				}
				if(missingFiles.length() > 0) {
					rollbackActivityCreation(activity, progress, actionMap.values());
					throw new UnprocessableEntityException("Missing input file specification: "+missingFiles);
				}

				List<FileRef> outputs = action.getActionTask().getOutputFiles();
				if(outputs != null && outputs.size() > 0) {
					ArrayList<TypedParameter> credentialSource =  createCredentialSource(action);
					String agentURI =  null;
					for(TypedParameter t : credentialSource) {
						if(t.getName().equals("agentBaseUrl")) {
							agentURI = t.value();
							break;
						}
					}
					for(FileRef i : outputs) {
						FileMonitor f = vmResidentFiles.get(i.getName());
						Map<String, List<String>> consumers = f.getConsumers();
						
						List<String> consumerUris = consumers.get(agentURI);
						if(consumerUris != null) {
							for(String uri : consumerUris) {
								Action consumer = actionUriMap.get(URI.create(uri));
								consumer.hasDependencyOn(action);
							}
						}
					}
				}
			}
		
			/*
			 * Step 4. Create dependency association for parameter values
			 * ---------------------------------------------------------
			 * 
			 */
			
			/*
			 *  Update the join action such that it references the other actions in the iteration set, and
			 *  update the dependencies
			 */
			
			for(Action a : actionMap.values()) {
				if(a.getKind() == ActionType.join) {
					if(updateJoinDependencies(a, actionMap)) {
						needsUpdate.add(a);
					}
				}
			}	
			
			/*
			 *  Update other parameter related dependencies
			 */
			String unknownRefs="";
			Pattern variableReference = Pattern.compile("\\$\\<([A-Za-z0-9_-]+)(\\.[A-Za-z0-9_][A-Za-z0-9_-]*)\\>");
			for(Action action : spawn) {
				if(action.getActionTask().getExecutionParameters() != null) {
					for(TypedParameter p : action.getActionTask().getExecutionParameters()) {
						Matcher match = variableReference.matcher(p.value()+" "+p.defaultValue());
						while(match.find()) {
							String source = match.group(1);
							if(actionMap.containsKey(source) || !parameterMap.containsKey(source) ) {
								Action src = actionMap.get(source);
								if(src != null) {
									action.hasDependencyOn(src);
									needsUpdate.add(src);
									log.info("Action "+action.getName()+" depends on "+src.getName()+" for "+p.getName());
								} else {
									if(unknownRefs.length() > 0) {
										unknownRefs += ", ";
									}
									log.severe("Unresolved reference for action "+action.getName()+" parameter "+p.getName()
											+" reference "+source+" in "+p.getValue()+" "+p.getDefaultValue());
									unknownRefs += action.getName()+":"+p.getName()+":"+source;
								}
							}
						}
					}
				}
				if(unknownRefs.length() > 0) {
					rollbackAssimilate(assimilateList);
					throw new UnprocessableEntityException("Unknown references: "+unknownRefs);
				}
			}


			
			/*
			 *  Step 5. Persist actions
			 * ------------------------
			 * Updates come in 2 categories..
			 * (1) new items & the iterator that triggered the processing
			 * 		.. these can simply be persisted in a bulk operation
			 * (2) other blocked/running items
			 * 		.. these need to be locked and then updated to exclude race conditions
			 * 		.. with the refresh processing
			 * 
			 */
			dao.action().update(iterator);
			for(Action action : assimilateList) {
				action.getActionTaskImpl().setParentAction(action.getUri());
				action.getActionTask().getDuration();
			}
			log.info("needs update contains iterator "+needsUpdate.contains(iterator));
			log.info("iterator is dependency for "+iterator.getActionTaskImpl().getDependencyFor().size());

			dao.action().updateAll(assimilateList);
			dao.action().update(iterator);
			needsUpdate.remove(iterator);
			dao.action().updateDependencies(needsUpdate);
	
			/*
			 *  Step 6. Topological sort and persist of activity
			 * -------------------------------------------------
			 * 
			 */
	
			try {
				/*
				 * Topological sort is destructive, so we put a copy of the
				 * iterator object in the map for this phase
				 */
				Action iteratorCopy = dao.action().load(iterator.getUri());
				actionMap.put(iteratorCopy.getName(), iteratorCopy);
				ArrayList<Action> sorted = topologicalSort(actionMap.values());
				ArrayList<URI> sortedURI = new ArrayList<URI>(sorted.size());
				for(Action a : sorted) {
					sortedURI.add(a.getUri());
				}
				activity.setActions(sortedURI);
	
			} catch (UnprocessableEntityException e) {
				rollbackAssimilate(assimilateList);
				throw e;
			}
			activity.setFileTable(fileMapToTable(vmResidentFiles));
			dao.activity().update(activity);
			getActionResource().initiate(assimilateList);
			return activity;
		} finally {
			dao.activity().release(activity);
		}
	}
	
	private void rollbackAssimilate(java.util.Collection<Action> fileXfer) {
		for(Action x : fileXfer) {
			dao.action().delete(x);
		}
	}

	private void rollbackActivityCreation(Activity activity, Progress progress, java.util.Collection<Action> actions) {
		if(actions != null) {
			for(Action x : actions) {
				dao.action().delete(x);
			}
		}
		dao.progress().delete(progress);
		dao.activity().delete(activity);
	}
	
	private boolean updateJoinDependencies(Action joinAction, Map<String, Action> actionMap) {
		Map<String, ArrayList<String>> args = new HashMap<String, ArrayList<String>>();
		boolean updated = false;
		for(TypedParameter p : joinAction.getActionTask().getExecutionParameters()) {
			if(p.getType() == ParameterType.List){
				args.put(p.getName(), new ArrayList<String>());
			}
		}
		for(Action a : actionMap.values()) {
			TypedParameter i;
			if(a.getName().startsWith(joinAction.getName()+"-")) { 
			   if ((i = a.getInputParameter("i")) == null) {
					if(a.getName().equals(joinAction.getName()+"-0")) {
						i = new TypedParameter("i", "instance number", ParameterType.Long, "0", "0");
						ArrayList<TypedParameter> params = a.getActionTaskImpl().getExecutionParameters();
						if(params == null) a.getActionTaskImpl().setExecutionParameters(params = new ArrayList<TypedParameter>());
						params.add(i);	
					} else {
						log.severe("Action "+a.getName()+" has no parameter i");
						continue;
					}
				}
				int ii = Integer.valueOf(i.getValue());
				for(String name : args.keySet()) {
					if(a.getOutputParameter(name) != null) {
						ArrayList<String> list = args.get(name);
						while(list.size() < ii+1) {
							list.add(null);
						}
						list.set(ii, "$<"+a.getName()+"."+name+">");
						joinAction.hasDependencyOn(a);
					}
				}
			}
		}
		for(String name : args.keySet()) {
			ArrayList<String> value = args.get(name);
			StringBuffer b = new StringBuffer();
			b.append("[");
			int i = 0;
			for(String s : value) {
				if(i++ != 0)
					b.append(",");
				if(s == null) {
					b.append("\"\"");
				} else {
					b.append(s);
				}
			}
			b.append("]");
			String old = joinAction.getInputParameter(name).value();
			String newValue = b.toString();
			if(!old.equals(newValue)) {
				joinAction.setInputParameter(name, newValue);
				updated = true;
			}
			
		}
		return updated;
	}

	/** (destructive) topological sort. Orders the list of actions based on the dependency chain.
	 * <p>
	 * WARNING: the ordering process destroys the action object "dependentOn" list.
	 * @param unsorted list of actions
	 * @return sorted list of action
	 * @throws UnprocessableEntityException
	 * @see http://en.wikipedia.org/wiki/Topological_sort for algorithm details.
	 */
	private ArrayList<Action> topologicalSort(java.util.Collection<Action> unsorted) throws UnprocessableEntityException {
		int edgeCount = 0;
		ArrayList<Action> noIncoming = new ArrayList<Action>();
		Map<URI,Action> graph = new HashMap<URI,Action>();
		for(Action action : unsorted) {
			ArrayList<URI> dependencies = action.getActionTaskImpl().getDependentOn();
			if(dependencies != null && dependencies.size() > 0) {
				edgeCount += dependencies.size();
				graph.put(action.getUri(), action);
			} else {
				noIncoming.add(action);
			}
		}
		if(edgeCount >= unsorted.size()) {
			log.warning(String.format("Action graph with %d edges and %d nodes",edgeCount, unsorted.size()));
			// throw new UnprocessableEntityException("Circular reference in action graph");
		}
		/*
		 * 	do topological sort. See http://en.wikipedia.org/wiki/Topological_sort
		 */
		ArrayList<Action> sorted = new ArrayList<Action>();
		while(!noIncoming.isEmpty()) {
			Action n = noIncoming.remove(0);
			sorted.add(n);
			ArrayList<URI> edges = n.getActionTaskImpl().getDependencyFor();
			if(edges != null) {
				for(URI e : edges) {
					Action m = graph.get(e);
					ArrayList<URI> dependencies = m.getActionTaskImpl().getDependentOn();
					dependencies.remove(n.getUri());
					m.getActionTaskImpl().setDependentOn(dependencies);	// necessary due to our internal object representation as string
					if(dependencies.size() == 0) {
						noIncoming.add(m);
						graph.remove(e);
					}
				}
			}
		}
		if(graph.size() != 0) {
			log.severe(String.format("Command definition ERROR -- unexpected circular reference. Graph has %d nodes remaining",graph.size()));
			for(Action a : graph.values()) {
				log.severe(String.format("Remaining action "+a.getName()+" "+a.getUri()+" with dependencies "+a.getActionTaskImpl().getDependentOn()));
			}
			throw new UnprocessableEntityException("Command definition ERROR -- unexpected circular reference in action ");
		}
		log.info("Topological sort:");
		for(Action a : sorted) {
			log.info(String.format("%s [dependencyFor=%s] ",
					a.getName(),a.getActionTaskImpl().getDependencyFor()));
		}

		return sorted;
	}

	private void updateHistory(String name, long actual, Activity activity) {
		long totalInput = 0;

		if(activity.getInputs() != null) {
			for(FileSpecification f : activity.getInputs()) {
				totalInput += f.getSize();
			}
		}
		new HistoryResource.HistoryManager().addSample(actual, name, makeKey(totalInput));
	}
	
	private long estimateExecutionTime(String name, long totalInput) {
		long estimate = dao.history().getNinetyPercentile(name, makeKey(totalInput));
		return estimate;
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

	
	private ArrayList<FileMonitor> fileMapToTable(Map<String, FileMonitor> map) {
		ArrayList<FileMonitor> result = new ArrayList<FileMonitor>(map.values().size());
		for(FileMonitor fm : map.values()) {
			if(fm.getProducer()!=null && !fm.getProducer().isEmpty()) {
				result.add(fm);
			} else {
				if(!fm.isOptional()) {
					log.severe("File "+fm.getName()+" has not been supplied");
				}
			}
		}
		return result;
	}
	
	
	
	private Map<String, FileMonitor> fileTableToMap(ArrayList<FileMonitor> fileTable) {
		Map<String, FileMonitor> result = new HashMap<String, FileMonitor>();
		if(fileTable != null) {
			for(FileMonitor f : fileTable) {
				result.put(f.getName(), f);
			}
		}
		return result;
	}

	
	private ArrayList<TypedParameter> createCredentialSource(Action action) {
		ArrayList<TypedParameter> result = new ArrayList<TypedParameter>();
		TypedParameter url = action.getInputParameter("agentBaseUrl");
		TypedParameter user = action.getInputParameter("agentUser");
		TypedParameter secret = action.getInputParameter("agentPassword");

		if(url != null && user != null && secret != null) {			 
			result.add(url);
			result.add(user);
			result.add(secret);
		} else {
			log.severe("Action "+action.getName()+" has no association to agent credentials for file transfer");
			throw new UnprocessableEntityException("Action "+action.getName()+" has no association to agent credentials for file transfer");
		}

		return result;
	}

	private void updateFileTable(Action action, Map<String, FileMonitor> files, Map<String, FileSpecification> outputs, Activity activity) {
		if(action != null) {
			List<FileRef> outputFiles = action.getActionTask().getOutputFiles();

			if(outputFiles != null && outputFiles.size() != 0) {
				for(FileRef outputFile : outputFiles) {
					FileMonitor fileTableNode = files.get(outputFile.getName());
					if(fileTableNode != null && fileTableNode.getKind().equals(outputFile.getKind())) {
						fileTableNode.setLength(outputFile.getLength());
						fileTableNode.setModified(outputFile.getModified());
					} else {
						FileSpecification commandLineOutputFile = outputs.get(outputFile.getName());
						if(commandLineOutputFile != null) {
							commandLineOutputFile.setSize(outputFile.getLength());
							commandLineOutputFile.setModified(outputFile.getModified());
							UriBuilder builder = UriBuilder.fromUri(outputFile.getSource());
							builder.path(outputFile.getRoot()).path(outputFile.getKey());		
							commandLineOutputFile.setCanonicalPath(builder.build().toString());

							ChangeManager.factory().addChange(commandLineOutputFile.getRepository());
						} else {
							if(outputFile.getKind()!=null && !outputFile.getKind().equals("File") && outputFile.getName().endsWith("/")) {
								FileSpecification zipFile = outputs.get(outputFile.getName().substring(0,outputFile.getName().length()-1));
								if(zipFile != null) {
									FileSpecification f = new FileSpecification(outputFile);
									f.setRepository(zipFile.getRepository());
									f.setSize(outputFile.getLength());
									f.setModified(outputFile.getModified());
									UriBuilder builder = UriBuilder.fromUri(outputFile.getSource());
									builder.path(outputFile.getRoot()).path(outputFile.getKey());		
									f.setCanonicalPath(builder.build().toString());
									log.info("Creating filespec "+f);
									activity.getOutputs().add(f);

									ChangeManager.factory().addChange(f.getRepository());
								}
							}
						}
					}
				}
			}
		}
	}
	
	private Map<String, FileSpecification> fileSpecificationListToMap(List<FileSpecification> list) {
		Map<String, FileSpecification> map = new HashMap<String, FileSpecification>();
		if(list != null) {
			for(FileSpecification f : list) {
				map.put(f.getName(), f);
			}
		}
		return map;
	}
	
	private Progress updateProgress(Activity activity) {
		log.info("Update progress");
		long maxRemaining = 0;
		long maxDuration = 0;
		Progress result = null;
		Map<URI, Action> map = new HashMap<URI,Action>();
		Map<URI, long[]> estimateUpdate = new HashMap<URI, long[]>();
		Map<String, FileMonitor> files = fileTableToMap(activity.getFileTable());
		Map<String, FileSpecification> outputs = fileSpecificationListToMap(activity.getOutputs());
		Set<Long> dispatch = new HashSet<Long>();
		try {
			for(URI u : activity.getActions()) {
				Action a = dao.action().load(u);
				map.put(u, a);
				ActionState state = a.getActionTask().getState();
				if(ActionState.INIT == state)
					return null;
				
				if((ActionState.COMPLETE == state) && !a.isFinalized()) {
					updateFileTable(a, files, outputs, activity);
				}
			}
			int[] stateCount = new int[ActionState.values().length];
			for(URI u : activity.getActions()) {
				Action a = map.get(u);
				if(!a.getActionTaskImpl().getDependentOn().isEmpty())
					break;
				long estimates[] = getEstimates(a, map, estimateUpdate, dispatch, files);
				log.warning("=>Action "+a.getName()+" state "+ActionState.values()[(int)estimates[2]]+" duration "+estimates[1]+" remaining "+estimates[0]);

				maxRemaining = Math.max(maxRemaining, estimates[0]);
				maxDuration = Math.max(maxDuration, estimates[1]);
				stateCount[(int) estimates[2]] += 1;
			}
			//int progress = 1000 - (int) ((1000 * maxRemaining)/maxDuration);
			int progress = 900;
			ActionState state = summarizeState(stateCount);
			log.warning("=>Summarized state "+state);
			if(state == ActionState.COMPLETE || state == ActionState.FAILED || state == ActionState.CANCELLED){
				if(state != activity.getState()) {
					result = dao.progress().updateEstimates(activity.getProgress(), progress, maxDuration, state, 
							Calendar.getInstance().getTime());
					if(state == ActionState.COMPLETE){
						updateHistory(result.getCommand(), maxDuration, activity);
					}
					return result;
				}
			} else {
				if(progress == 1000) {
					progress = 999;
				}
			}
			result = dao.progress().updateEstimates(activity.getProgress(), progress, maxDuration, state);
			if(!dispatch.isEmpty()) {
				log.info("Dispatching "+dispatch.size());
				for(Long id : dispatch) {
					getActionResource().dispatchAction(id);
				}
			}
		} catch (Exception e) {
			log.log(Level.INFO, "Progress update errors for activity "+activity.getUri().toString(),e);
		}
		return result;
	}

	private void updateFileInformation(Activity activity) {
		int size = 0;
		List<FileSpecification> inputs = activity.getInputs();
		List<FileSpecification> outputs = activity.getOutputs();
		if(inputs != null)
			size += inputs.size();
		if(outputs != null)
			size += outputs.size();
		ArrayList<String> origin = new ArrayList<String>(size);

		if(outputs != null) {
			for(FileSpecification f : outputs) {
				try {
					if(f.getCanonicalPath()!=null && f.getCanonicalPath().length()!=0) {
						origin.add(Origin.createKey(f.getCanonicalPath(), false));
					}
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error updating file info "+f, e);
				}
			}	
		}
		if(inputs != null) {
			for(FileSpecification f : inputs) {
				try {
					if(f.getCanonicalPath()!=null && f.getCanonicalPath().length()!=0) {
						origin.add(Origin.createKey(f.getCanonicalPath(), true));
					}
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error updating file info "+f, e);
				}
			}	
		}
		activity.setOrigin(origin);
	}	

	
	private long[] getEstimates(Action a, Map<URI, Action>map, Map<URI, long[]> estimateUpdate, Set<Long> dispatch, Map<String, FileMonitor>files) {
		if(estimateUpdate.containsKey(a.getUri())) return estimateUpdate.get(a.getUri());
		long duration = a.getActionTask().getDuration();
		long remaining = (1000-a.getActionTask().progress())*duration/1000;
		ActionState state = a.getActionTask().getState();
		if(state == ActionState.BLOCKED){
			List<URI> dependents = a.getActionTaskImpl().getDependentOn();
			boolean makePending=true;
			if(dependents != null) {
				for(URI u : dependents) {
					Action d = map.get(u);
					if(d.getActionTask().getState() != ActionState.COMPLETE){
						makePending=false;
						break;
					}
				}
			}
			if(makePending) {
				try {
					getActionResource().makePending(a, map, files);
					dispatch.add(a.getId());
				} catch (Exception e) {
					log.log(Level.INFO, "Error launching action "+a.getUri().toString(),e);
				}
			}
			state = a.getActionTask().getState();
		}
		log.warning("......Action "+a.getName()+" state "+state+" duration "+duration+" remaining "+remaining);
		long[] result = {remaining, duration, state.ordinal()};
		List<URI> dependency = a.getActionTaskImpl().getDependencyFor();
		if(dependency != null && dependency.size() > 0) {
			long maxRemaining = 0;
			long maxDuration = 0;
			int[] stateCount = new int[ActionState.values().length];
			for(URI u : dependency) {
				Action d = map.get(u);
				long estimates[] = getEstimates(d, map, estimateUpdate, dispatch, files);
				log.warning("==>Summary Action "+d.getName()+" state "+ActionState.values()[(int)estimates[2]]+" duration "+estimates[1]+" remaining "+estimates[0]);
				maxRemaining = Math.max(maxRemaining, estimates[0]);
				maxDuration = Math.max(maxDuration, estimates[1]);
				stateCount[(int) estimates[2]] += 1;
			}
			result[0] += maxRemaining;
			result[1] += maxDuration;
			log.warning("==>"+a.getName()+" Remaining "+ result[0]+" duration "+result[1]);
			if(state == ActionState.COMPLETE){
				log.warning("==>Summarized state"+summarizeState(stateCount));
				result[2] = summarizeState(stateCount).ordinal();
			}
		}
		

		
		estimateUpdate.put(a.getUri(), result);

		if(state == ActionState.COMPLETE && !a.isFinalized()) {
			getActionResource().finalizeAction(a.getId());
		}
		return result;
	}
	

	
	private static ActionState summarizeState(int[] stateCount) {
		ActionState result = ActionState.COMPLETE;
		if(stateCount[ActionState.FAILED.ordinal()] != 0) {
			result = ActionState.FAILED;
		} else if(stateCount[ActionState.CANCELLED.ordinal()] != 0) {
			result = ActionState.CANCELLED;
		} else if(stateCount[ActionState.RUNNING.ordinal()] != 0) {
			result = ActionState.RUNNING;
		} else if(stateCount[ActionState.PENDING.ordinal()] != 0) {
			result = ActionState.RUNNING;
		} else if(stateCount[ActionState.BLOCKED.ordinal()] != 0) {
			result = ActionState.BLOCKED;
		} 
		return result;
	}
	
	
	public static class ActivityManager extends CachingAbstractManager<Activity> {
		public ActivityManager() {
		}
		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(ActivityResource.class).build();
		}

		@Override
		protected GenericModelDao<Activity> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<Activity>(Activity.class, transactional);
		}
	
		private Activity lock(long id) throws ConcurrentModificationException, NotFoundException  {
			GenericModelDao<Activity> itemDaoTxn = this.itemDaoFactory(true);
			Activity item;
			for(int i=0; i < 150; i++) {
				try {
					Long now = Calendar.getInstance().getTimeInMillis();
					itemDaoTxn = this.itemDaoFactory(true);
					item = itemDaoTxn.get(id);
					Long oldLock = item.getLock();
					if(oldLock == null || oldLock == 0 || oldLock+30000 < now) {
						item.setLock(now);
						itemDaoTxn.put(item);
						itemDaoTxn.ofy().getTxn().commit();
						if(oldLock != null && oldLock != 0)
							log.warning("Breaking lock on Activity "+id);
						return item;
					} else {
						Thread.sleep(100);
						continue;
					}
				} catch (com.googlecode.objectify.NotFoundException e) {
					throw new NotFoundException();
				} catch (Exception e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
					continue;
				} finally {
					if(itemDaoTxn.ofy().getTxn().isActive())
						itemDaoTxn.ofy().getTxn().rollback();
				}
			}
			throw new ConcurrentModificationException();
		}
		
		private void release(Activity activity) {
			if(activity != null) {
				activity.setLock(null);
				update(activity);
			}
		}
		
		
		
		
		public Activity load(Long id, User requestor) throws NotFoundException { return super.get(id, requestor); }
		/**
		 * Locate a item from the persistent store based on the item name.
		 * @param name
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public Activity load(String name, User requestor) throws NotFoundException { return super.get(name, requestor); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public Activity load(URI uri, User requestor) throws NotFoundException { return super.get(uri, requestor); }
		
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public Activity load(URI uri) throws NotFoundException { return super.get(uri); }
		/**
		 * Return a list of all activities.
		 * @return list of activity
		 */
		public List<Activity> getAll() { return super.getAll(); }
		

		public Origin getCurrentReference(String canonicalName) {
			String key = Origin.createKey(canonicalName, false);
			log.info("Get reference for "+key);
			List<Activity> refs = this.itemDao().listByProperty("origin", key);
			if(refs != null) {
				if(refs.size() == 1) {
					return createOriginFrom(refs.get(0), 1, canonicalName);
				} else if(refs.size() > 1) {
					Date last = Calendar.getInstance().getTime();
					last.setTime(0);
					Activity lastActivity = null;
					for(Activity activity : refs) {
						FileSpecification f = findFileSpec(activity, canonicalName);
						if(f.getModified()!=null && f.getModified().after(last)) {
							last = f.getModified();
							lastActivity = activity;
						} else if(f.getModified()==null && lastActivity==null) {
							lastActivity = activity;
						}
					}
					if(lastActivity != null) {
						return createOriginFrom(lastActivity, refs.size(), canonicalName);
					}
				}
			}
			return new Origin();
		}
		
		private Origin createOriginFrom(Activity activity, int count, String canonicalName) {
			Origin result = new Origin();
			result.setTotal(count);
			result.setActivity(activity.getUri());
			FileSpecification fileSpec = findFileSpec(activity, canonicalName);
			result.setActivityModification(fileSpec.getModified());
			result.setActivityName(activity.getName());
			result.setActivitySize(fileSpec.getSize());
			
			return result;
		}
		
		private FileSpecification findFileSpec(Activity activity, String canonicalName) {
			List<FileSpecification> outputs = activity.getOutputs();
			if(outputs != null) {
				for(FileSpecification f : outputs) {
					if(f.getCanonicalPath()!=null && f.getCanonicalPath().equals(canonicalName)) {
						return f;
					}
				}
			}
			return null;
		}
	}
}


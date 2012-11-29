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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import n3phele.service.actions.tasks.ActionLog;
import n3phele.service.core.NotFoundException;
import n3phele.service.core.Resource;
import n3phele.service.expression.Evaluator;
import n3phele.service.model.Action;
import n3phele.service.model.ActionState;
import n3phele.service.model.Activity;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.FileMonitor;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.BaseEntity;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.User;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.taskqueue.TransientFailureException;
import com.google.apphosting.api.ApiProxy.CapabilityDisabledException;
import com.google.apphosting.api.DeadlineExceededException;

@Path("/action")
public class ActionResource {
	private static Logger log = Logger.getLogger(ActionResource.class.getName()); 
	private final Dao dao;
	public ActionResource() {
		this(new Dao());
	}
	public ActionResource(Dao dao) {
		this.dao = dao;
	}
	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public Collection<BaseEntity> list(
			@DefaultValue("false") @QueryParam("summary") Boolean summary)  {

		log.warning("getAction entered with summary "+summary);

		Collection<BaseEntity> result = dao.action().getCollection(UserResource.toUser(securityContext)).collection(summary);
		return result;
	}


	@GET
	// @Produces("application/vnd.com.n3phele.ActionEntry+json")
	@Produces("application/json")
	@RolesAllowed("authenticated")
	@Path("{id}") 
	public Action get( @PathParam ("id") Long id) throws NotFoundException {

		Action item = dao.action().load(id, UserResource.toUser(securityContext));
		return item;
	}
	/*
	 * This is an eventing endpoint that can be invoked by an http request with
	 * no authentication.
	 */
	@GET
	@Produces("text/plain")
	@Path("{id}/event") 
	public Response event( @PathParam ("id") Long id,
						 @QueryParam ("reference") String reference,
						 @QueryParam ("sequence") int sequence,
						 @QueryParam ("source") String source,
						 @QueryParam ("oldStatus") String oldStatus,
						 @QueryParam ("newStatus") String newStatus) {

		log.info(String.format("Event %s sequence %d received for Action from %s oldStatus %s new Status %s", reference, sequence, source, oldStatus, newStatus ));
		Action a = null;
		try {
			a = dao.action().get(id);
		} catch (NotFoundException e) {
			return Response.status(Status.GONE).build();
		}
		if(!a.isFinalized()) {
			try {
				dispatchAction(a.getId());	
			} catch (TaskAlreadyExistsException ignore) {
				// intentionally using the reference field to as not to flood queue with requests to the same action
			}
		}
		return Response.ok().build();
	}


	/*
	 * ------------------------------------------------------------------------------------------ *
	 *                      Google app engine entry points.
	 *  The following entry points are intended to by invoked by internal app engine processes.
	 *  Their invocation is protected by google app engine "admin" security settings.
	 * ------------------------------------------------------------------------------------------ *
	 */

	/** Dispatch processing of an action.
	 * This entry point will be protected by the restricted admin interface of google and is invoked by the
	 * Queue service in google app engine.
	 * @param id
	 * @return
	 */
	public NameValue do_dispatch(long id, boolean finalize) {
		log.info("===========> do_dispatch "+Long.toString(id)+" finalize "+finalize);
		NameValue result = new NameValue("dispatched", "false");

		Action action = null;
				
		try {
			action = dao.action().lock(id);
			if(action.isFinalized()) {
				if(finalize)
					log.info("Dispatch on finialized action "+action.getUri());
				else
					log.severe("Dispatch on finialized action "+action.getUri());
				return result;
			}
			switch(action.getActionTask().getState()) {
			case RUNNING:
			case PENDING:
				if(finalize)
					log.severe("Finalize called on active action");
				boolean change;
				result.setValue(Boolean.toString(change = action.getActionTask().call()));
				dao.action().update(action);
				ActionState newState = action.getActionTask().getState();
				if(change || newState == ActionState.COMPLETE || newState == ActionState.FAILED) {
					conditionallyDispatchParentActivity(action, null, newState == ActionState.FAILED);
				}
				break;
				
			case CANCELLED:
			case COMPLETE:
			case FAILED:
				if(finalize)
					action.setFinalized(true);
				break;
			case INIT:
			case BLOCKED:
				if(finalize)
					log.severe("Finalize called on dormant action");
			default:
				break;
			}
		} catch (DeadlineExceededException timeout) {
			log.info("=== DeadlineExceededException ==== "+Long.toString(id));
			throw timeout; 
		} catch (ConcurrentModificationException mod) {
			log.info("=== ConcurrentModificationException ==== "+Long.toString(id));
			// update already in progress
		} catch (TransientFailureException e) {
			log.log(Level.SEVERE, "processing "+action.getUri()+" "+action.getName(), e); 
		} catch (NotFoundException e) {
			log.log(Level.SEVERE, "processing "+action.getUri()+" "+action.getName(), e); 
		} catch (CapabilityDisabledException e) {
			log.log(Level.SEVERE, "processing "+action.getUri()+" "+action.getName(), e); 
		} catch (DatastoreTimeoutException e) {
			log.log(Level.SEVERE, "processing "+action.getUri()+" "+action.getName(), e); 
		} catch (Exception e) {
			log.log(Level.SEVERE, "processing "+action.getUri()+" "+action.getName(), e);
			try {
				if(action != null) {
//					action.getActionTask().setComplete(Calendar.getInstance().getTime());
//					action.getActionTask().setState(ActionState.FAILED);
//					conditionallyDispatchParentActivity(action, null, true);
				}
			} catch (Exception ignore) {
	
			} 
		} finally {
			dao.action().release(action);
			log.info("===========< do_dispatch "+Long.toString(id));
		}
		
		return result;
	}


	public void makePending(Action action, Map<URI, Action> actions, Map<String, FileMonitor> files) throws Exception {
		try {
			prepareActionForExecution(action, actions, files);
			dao.action().update(action);
		} catch (Exception e) {
			action.getActionTask().setState(ActionState.FAILED);
			action.getActionTask().setStart(Calendar.getInstance().getTime());
			dao.action().update(action);
			throw e;
		} 
		
	}

	private void prepareActionForExecution(Action action,Map<URI, Action> actions, Map<String, FileMonitor> files) {
		log.info("Prepare action "+action.getUri()+" "+action.getName()+" for execution");
		ArrayList<URI> dependency = action.getActionTaskImpl().getDependentOn();
		List<TypedParameter> params = action.getActionTask().getExecutionParameters();
		if(params != null && params.size()>0) {
			Map<String, Map<String, TypedParameter>> map = new HashMap<String, Map<String, TypedParameter>> ();
			Activity parent = dao.activity().load(action.getActionTask().getParent());
			Map<String, TypedParameter> contextParams = new HashMap<String, TypedParameter>();
			for(TypedParameter t : parent.getContext()) {
				contextParams.put(t.getName(), t);
			}
			map.put("$$", contextParams);
			if(dependency != null && dependency.size() > 0) {
				for(URI u : dependency) {
					Action a = actions.get(u);
					Map<String, TypedParameter> actionParams = new HashMap<String, TypedParameter>();
					map.put(a.getName(), actionParams);
					if(a.getActionTask().getExecutionParameters()!= null) {
						for(TypedParameter t : a.getActionTask().getExecutionParameters()) {
							actionParams.put(t.getName(), t);
						}
					}

					if(a.getActionTask().getOutputParameters()!= null) {
						for(TypedParameter t : a.getActionTask().getOutputParameters()) {
							actionParams.put(t.getName(), t);
						}
					}
				}
			}
			Evaluator.invoke(params, map, ActionLog.name(action.getActionTask())); 
		}
		/*
		 * Inject input file information
		 */
		List<FileRef> inputs = action.getActionTask().getInputFiles();
		if(inputs != null) {
			for(FileRef input : inputs) {
				FileMonitor node = files.get(input.getName());
				if(node != null) {
					if(node.getKind().equals(input.getKind())) {
						input.setLength(node.getLength());
						input.setModified(node.getModified());
					}
					log.info("Set file "+input.getName()+" size "+node.getLength());
				} else {
					log.severe("File "+input.getName()+" not in fileTable");
				}
			}
		}
		action.getActionTask().setStart(Calendar.getInstance().getTime());
		action.getActionTask().setState(ActionState.PENDING);
	}
	
	
	/** Cancel or free up resources from an action.
	 * This can be used to cancel or abort an existing task by the user or due to failure. It is also used at the
	 * final completion of an activity to free any resources held by the actions, including any running VMs.
	 * This entry point will be protected by the restricted admin interface of google. This interface is invoked by
	 * the Queue facility in google add engine.
	 * @param id
	 * @return
	 */

	public NameValue do_terminate(long id, boolean failure) {
		log.info("terminate "+id);
		NameValue result = new NameValue("terminate", "false");
		//boolean dispatchToSuccessors = false;

		Action action = null;
		try {
			action = dao.action().lock(id);
			switch(action.getActionTask().getState()) {
			case COMPLETE:
			case FAILED:
				if(failure) {
					action.getActionTask().stop();
				} else {
					action.getActionTask().cleanup();
				}
				// no break;
			case CANCELLED:
				action.setFinalized(true);
				break;
			case INIT:
			case BLOCKED:
				action.getActionTask().setStart(Calendar.getInstance().getTime());
				//NO break;
			case RUNNING:
			case PENDING:
				action.getActionTask().cancel();
				action.getActionTask().setState(ActionState.CANCELLED);
				action.getActionTask().setComplete(Calendar.getInstance().getTime());
				//dispatchToSuccessors = true;
				result.setValue(Boolean.toString(true));
				action.setFinalized(true);
				break;
			default:
				break;
			}

		} catch (ConcurrentModificationException e) {
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(Builder.withUrl(String.format("/admin/worker/action/%d/kill",id)).param("failure", Boolean.toString(failure)).retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)).countdownMillis(30000).method(Method.GET));
		} catch (Exception e) {
			log.log(Level.WARNING, String.format("processing %d %s %s", id, (action != null)?action.getUri():null, (action!=null)?action.getName():null), e);
			try {
				if(action != null) {
					action.getActionTask().setComplete(Calendar.getInstance().getTime());
					action.getActionTask().setState(ActionState.FAILED);
					action.setFinalized(true);
					//dispatchToSuccessors = true;
					action.getActionTask().cleanup();
				}
			} catch (Exception ignore) {

			}
		} finally {
			try {
				dao.action().release(action);
			} catch (Exception ignore) {
				log.log(Level.SEVERE, "terminate unlock exception", ignore);
			}
		}
		//if(dispatchToSuccessors)
		//	dispatchSuccessorTree(action, false);
		return result;
	}
	
	
	//FIXME  This is where we will clean up recalsitrant actions, versus embedded in refresh and dispatch processing

	/** Garbage collection actions that have completed.
	 * This entry point will be protected by the restricted admin interface of google. This interface is invoked by the
	 * cron facility in google app engine.
	 * @param id
	 * @return
	 */
	public NameValue do_garbageCollection() throws TransientFailureException {
		NameValue result = new NameValue("garbageCollected", "0");

		Collection<Action> collection = dao.action().getCollection();
		Long dispatched = 0L;
		if(collection.getTotal() > 0) {
			Calendar expire = Calendar.getInstance();
			expire.add(Calendar.MINUTE, -Integer.valueOf(Resource.get("actionGarbageCollectionDelayInMinutes", "5")));
			Date trash = expire.getTime();
			for(Action a : collection.getElements()) {
				switch(a.getActionTask().getState()) {
				case INIT:
					if(a.getActionTask() == null || a.getActionTask().getComplete() == null)
						break;
				case CANCELLED:
				case COMPLETE:
				case FAILED:
					if(trash.after(a.getActionTask().getComplete())) {
						dispatched++;
						log.fine("Garbage collection of "+a.getId()+" state:"+a.getActionTask().getState());
						try {
							dao.action().delete(a);
						} catch (Exception ignore) {

						}
					}

					break;
				case RUNNING:
				case PENDING:
				case BLOCKED:
				default:
					break;
				}
			}
		}
		log.info("Garbage collected "+dispatched);
		result.setValue(Long.toString(dispatched));
		return result;
	}
	
	/** Refresh the status of active actions.
	 * This entry point will be protected by the restricted admin interface of google. This interface is invoked by the
	 * cron facility in google app engine.
	 * @param id
	 * @return
	 */

	public NameValue do_refresh() throws TransientFailureException {
		NameValue result = new NameValue("refreshed", "0");
		List<Action> collection = dao.action().getNonfinalized();
		Set<String> queued = null;
		Long dispatched = 0L;
		int blocked = 0;
		if(collection != null && collection.size()>0) {
			Queue queue = QueueFactory.getDefaultQueue();
			for(Action a : collection) {
				ActionState state = a.getActionTask().getState();
				switch(state) {
				default:
				case INIT:
					continue;
				case FAILED:
				case CANCELLED:
				case COMPLETE:
					if(queued == null) {
						queued = new HashSet<String>();
					}
					dispatched += conditionallyDispatchParentActivity(a, queued, state != ActionState.COMPLETE);
					// dispatchSuccessorTree(a, false);  // FIXME.. only benefit of this is activity update. Otherwise the successors will be caught anyway.
													  // At this point we need to remove the processed successors from the candidate list so no
													  // double dispatch. 
					break;
				case RUNNING:
				case PENDING:
					dispatchAction(queue, a.getId(), false);
					dispatched++;
					break;
				case BLOCKED:
					blocked++;
					break;
				}
			}

		}
		log.info("Dispatched "+dispatched+" blocked "+blocked);
		result.setValue(Long.toString(dispatched));
		return result;
	}
	
	/*
	 * ------------------------------------------------------------------------------------------ *
	 *                      	== Private & Internal support functions ==
	 * ------------------------------------------------------------------------------------------ *
	 */

	
	private int conditionallyDispatchParentActivity(Action action, Set<String> queued, boolean failure) {
		int dispatched = 0;
		
		Activity parent = dao.activity().load(action.getActionTask().getParent());
		String identity = "action ";
		if(failure) {
			switch(action.getKind()) {
			case createVM:
				identity = "vm creation action ";
				break;
			case fileXfer:
				identity = "file transfer action ";
				break;
			case iterator:
				identity = "iterator action ";
				break;
			case join:
				identity = "join action ";
				break;
			case shellExecution:
				identity = "shell command execution action ";
				break;
			case log:
				identity = "log action ";
				break;
			default:
				identity = "action ";
			}
			ActionLog.withProgress(parent.getProgress()).withName("Activity").error("Terminated due to failure of "+identity+"\""+action.getName()+"\"");
		}
		String key= ("/admin/worker/activity/dispatch?id="+parent.getId().toString()+"&failure="+Boolean.toString(failure));
		if(queued!= null && queued.contains(key)) {
			if(failure)
				log.severe(action.getUri()+" queueing parent, but already queued "+key);
			else
				log.warning(action.getUri()+" queueing parent, but already queued "+key);
		} else {
			ActivityResource.dispatchActivity(parent.getId(), failure, failure?".Failure of "+identity+"\""+action.getName()+"\"":null);
			if(queued != null) queued.add(key);
			dispatched++;
			if(failure)
				log.severe(action.getUri()+" queuing parent with key "+key);
			else
				log.info(action.getUri()+" queuing parent with key "+key);
		}

		return dispatched;
	}
	
	public void finalizeAction(Long id) {
		dispatchAction(QueueFactory.getDefaultQueue(), id, true);
	}
	
	
	public void dispatchAction(Long id) {
		dispatchAction(QueueFactory.getDefaultQueue(), id, false);
	}
	public void dispatchAction(Queue queue, Long id, boolean finalize) {
		queue.add(Builder.withUrl("/admin/worker/action/dispatch").retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)).param("id", id.toString()).param("finalize", Boolean.toString(finalize)).method(Method.GET));
	}

	/** Cancel or cleanup resources from an action.
	 * This can be used to cancel or abort an existing task by the user or due to failure.
	 * It is also used at the
	 * final completion of an activity to free any resources held by the actions, including any running VMs. 
	 * This entry point is intended to be invoked by internal processing.
	 * @param u
	 * @param failure true if this is associated with a task failure, false a normal completion or operator cancellation
	 */
	public void terminate(URI u, boolean failure)  {
		try {
			Action a = dao.action().load(u);
			// if(a.isFinalized()) return;
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(Builder.withUrl(String.format("/admin/worker/action/%d/kill",a.getId())).param("failure", Boolean.toString(failure)).retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)).method(Method.GET));
		} catch (Exception ignore) {
			log.log(Level.SEVERE, "kill processing exception", ignore);
		}
	}
		
	
	/*
	 * Initiate execution of a list of actions
	 */
	
	public int initiate(List<?> actions) {
		Action action = null;
		int dispatched = 0;
//		Set<Long> dispatchList = new HashSet<Long>();
		List<Action> updateList = new ArrayList<Action>();
		Map<URI, Action> actionMap = new HashMap<URI, Action>();
		try {
			for(Object a : actions) {
				if(a instanceof URI) {
					action = dao.action().load((URI)a);
				} else if(a instanceof Action) {
					action = dao.action().get(((Action)a).getId());
				} else {
					throw new IllegalArgumentException("Cannot process "+a.getClass().getCanonicalName());
				}
				actionMap.put(action.getUri(), action);
				action.getActionTask().setState(ActionState.BLOCKED);
				updateList.add(action);
//				List<URI> dependency = action.getActionTaskImpl().getDependentOn();
//				if(dependency == null || dependency.size() == 0) {
//					prepareActionForExecution(action, actionMap);
//					dispatchList.add(action.getId());
//				}
				dao.action().itemDao().putAll(updateList);
			}
		} catch (Exception error) {
			
		} 
//		Queue queue = QueueFactory.getDefaultQueue();
//		for(Long id : dispatchList) {
//			queue.add(Builder.withUrl("/admin/worker/action/dispatch").retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)).param("id", id.toString()).method(Method.GET));
//			dispatched++;
//
//		}
		return dispatched;
	}
	
	public static class ActionManager extends CachingAbstractManager<Action> {
		public ActionManager() {
		}
		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(ActionResource.class).build();
		}

		@Override
		protected GenericModelDao<Action> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<Action>(Action.class, transactional);
		}

	
		private Action lock(long id) throws ConcurrentModificationException, NotFoundException  {
			Long now = Calendar.getInstance().getTimeInMillis();
			GenericModelDao<Action> itemDaoTxn = this.itemDaoFactory(true);
			Action item;
			try {
				item = itemDaoTxn.get(id);
				Long oldLock = item.getLock();
				if(oldLock == null || oldLock == 0 || oldLock+600000 < now) {
					item.setLock(now);
					itemDaoTxn.put(item);
					itemDaoTxn.ofy().getTxn().commit();
					if(oldLock != null && oldLock != 0) {
						Date date = new Date(oldLock);
						log.severe("Breaking lock on Action "+id+" value is "+oldLock+" representing "+date.toString());
					}
				} else {
					log.info("action "+id+" is already locked");
					throw new ConcurrentModificationException();
				}
			} catch (com.googlecode.objectify.NotFoundException e) {
				throw new NotFoundException();
			}catch (Exception e) {
				log.log(Level.WARNING, "Action lock gets exception ",e);
				throw new ConcurrentModificationException(e.getMessage());
			} finally {
				if(itemDaoTxn.ofy().getTxn().isActive()) {
					itemDaoTxn.ofy().getTxn().rollback();
				}
			}
			return item;
		}
		
		private void release(Action action) {
			if(action != null) {
				action.setLock(null);
				update(action);
			}
		}
		
	
		/**
		 * Locate a item from the persistent store based on the item id.
		 * @param id
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException if the object does not exist
		 */	
		public Action load(Long id, User requestor) throws NotFoundException { return super.get(id, requestor); }
		/**
		 * Locate a item from the persistent store based on the item name.
		 * @param name
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException if the object does not exist
		 */
		public Action load(String name, User requestor) throws NotFoundException { return super.get(name, requestor); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException if the object does not exist
		 */
		public Action load(URI uri, User requestor) throws NotFoundException { return super.get(uri, requestor); }
		
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @return the item
		 * @throws NotFoundException if the object does not exist
		 */
		public Action load(URI uri) throws NotFoundException { return super.get(uri); }
		
		
		/** Loads a set of Action URIs
		 * @param list of URIs to load
		 * @return returned iteratable collection of action objects
		 */
		public java.util.Collection<Action> loadAll(List<URI> list)  {
			Long[] keys = new Long[list.size()];
			int i=0;
			for(URI u : list) {
				String s = u.getPath();
				keys[i++] = Long.valueOf(s.substring(s.lastIndexOf("/")+1));
			}
			return this.itemDao().ofy().get(Action.class, keys).values();
		}
		
		
		public List<Action> getNonfinalized() { return super.itemDao().ofy().query(Action.class).filter("finalized", false).list(); }
	
	
		public void updateAll(List<Action> set) { super.itemDao().putAll(set); }
	
	
		public void updateDependencies(Set<Action> needsUpdate) {
			List<Action>removed = new ArrayList<Action>();
			int guard = 30;
			while(!needsUpdate.isEmpty() && guard-- > 0) {
				for(Action a : needsUpdate) {
					Action update = null;
					try {
						update = lock(a.getId());
						for(URI u : a.getActionTaskImpl().getDependencyFor()) {
							log.info("Action "+update.getName()+" dependencyFor "+this.load(u).getName());
							update.getActionTaskImpl().dependencyFor(u);
						}
						for(URI u : a.getActionTaskImpl().getDependentOn()) {
							log.info("Action "+update.getName()+" dependencyOn "+this.load(u).getName());
							update.getActionTaskImpl().hasDependencyOn(u);
						}
						log.info("Updated under lock "+a.getName());
						removed.add(a);
					} catch (NotFoundException e) {
						removed.add(a);
						add(a);
						log.severe("Action not found.. adding "+a.getName()+" at "+a.getUri());
					} catch (ConcurrentModificationException e) {
						log.severe("Waiting on lock for "+a.getName()+" at "+a.getUri());
						
					} finally {
						release(update);
					}
				}
				needsUpdate.removeAll(removed);
				if(!needsUpdate.isEmpty() && removed.size() <= 1)
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
				removed.clear();
			}
			if(!needsUpdate.isEmpty()) {
				log.severe("Update using locks failed... writing to store");
				updateAll(Arrays.asList(needsUpdate.toArray(new Action[needsUpdate.size()])));
			}
		}
	}
}

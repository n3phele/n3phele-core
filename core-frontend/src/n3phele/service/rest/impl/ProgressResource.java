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
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.googlecode.objectify.Query;

import n3phele.service.core.NotFoundException;
import n3phele.service.core.Resource;
import n3phele.service.core.UnprocessableEntityException;
import n3phele.service.model.ActionState;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.ChangeManager;
import n3phele.service.model.Narrative;
import n3phele.service.model.NarrativeLevel;
import n3phele.service.model.Progress;
import n3phele.service.model.ProgressCollection;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.User;

@Path("/progress")
public class ProgressResource {
	private static Logger log = Logger.getLogger(ProgressResource.class.getName()); 

	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	private final Dao dao;
	public ProgressResource() {
		this(new Dao());
	}

	public ProgressResource(Dao dao) {
		this.dao = dao;
	}
	

	@GET
	@RolesAllowed("authenticated")
	@Produces("application/json")

	public ProgressCollection list(
			@DefaultValue("false") @QueryParam("summary") Boolean summary,
			@DefaultValue("0") @QueryParam("start") int start,
			@DefaultValue("-1") @QueryParam("end") int end) throws NotFoundException {

		log.info("getProgress entered with summary "+summary+" from start="+start+" to end="+end);

//		Collection<Progress> result = dao.progress().getCollection(UserResource.toUser(securityContext));// .collection(summary);
//		if(summary) {
//			if(result.getElements() != null) {
//				for(int i=0; i < result.getElements().size(); i++) {
//					result.getElements().set(i, Progress.summary(result.getElements().get(i)));
//				}
//			}
//		}
//		
//		
//		return new ProgressCollection(result, start, end);
		
		
 // this approach while conceptually good surfaces a bug in objectify/gae
		int n = 0;
		if(start < 0)
			start = 0;
		if(end >= 0) {
			n = end - start + 1;
			if(n <= 0)
				n = 1;
		}
		Collection<Progress> result = dao.progress().getCollection(start, n, UserResource.toUser(securityContext));// .collection(summary);
		if(summary) {
			if(result.getElements() != null) {
				for(int i=0; i < result.getElements().size(); i++) {
					result.getElements().set(i, Progress.summary(result.getElements().get(i)));
				}
			}
		}
		
		return new ProgressCollection(result);
	}


	@GET
	@RolesAllowed("authenticated")
	@Produces("application/json")
	@Path("{id}") 
	public Progress get( @PathParam ("id") Long id,
			@DefaultValue("false") @QueryParam("summary") Boolean summary) throws NotFoundException {

		Progress item = dao.progress().load(id, UserResource.toUser(securityContext));
		if(summary) {
			item = Progress.summary(item);
		}
		return item;
	}

	@POST
	@RolesAllowed("authenticated")
	@Produces("text/plain")
	@Path("{id}")
	public Response add(@PathParam("id") String id,
			@QueryParam("id") String nid, @QueryParam("state") NarrativeLevel state,
			@QueryParam("message")String message) {
		Progress item = dao.progress().load(id, UserResource.toUser(securityContext));
		try {
			dao.progress().addNarrative(item.getUri(), nid, state, message);
		} catch (Exception e) {
			throw new UnprocessableEntityException(e.getMessage());
		}

		log.warning("Created "+nid);
		return Response.created(item.getUri()).build();

	}
	


	public static class ProgressManager extends CachingAbstractManager<Progress> {
		public ProgressManager() {
		}
		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(ProgressResource.class).build();
		}

		@Override
		protected GenericModelDao<Progress> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<Progress>(Progress.class, transactional);
		} 

		public Progress lock(long id) throws ConcurrentModificationException, NotFoundException  {
	
			GenericModelDao<Progress> itemDaoTxn = null;
			Progress item;
			for(int i=0; i < 250; i++) {
				try {
					Long now = Calendar.getInstance().getTimeInMillis();
					itemDaoTxn = this.itemDaoFactory(true);
					item = itemDaoTxn.get(id);
					Long oldLock = item.getLock();
					if(oldLock == null || oldLock == 0 || oldLock+2000 < now) {
						item.setLock(now);
						itemDaoTxn.put(item);
						itemDaoTxn.ofy().getTxn().commit();
						if(oldLock != null && oldLock != 0)
							log.warning("Breaking lock on Progress "+id);
						return item;
					} else {
						Thread.sleep(10);
						continue;
					}
				} catch (com.googlecode.objectify.NotFoundException e) {
					throw new NotFoundException();
				} catch (Exception e) {
					try {
						Thread.sleep(10);
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
		
		private void release(Progress progress) {
			if(progress != null) {
				progress.setLock(null);
				update(progress);
			}
		}
		
		
		
		
		public Progress load(Long id, User requestor) throws NotFoundException { return super.get(id, requestor); }
		/**
		 * Locate a item from the persistent store based on the item name.
		 * @param name
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public Progress load(String name, User requestor) throws NotFoundException { return super.get(name, requestor); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public Progress load(URI uri, User requestor) throws NotFoundException { return super.get(uri, requestor); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public Progress load(URI uri) throws NotFoundException { return super.get(uri); }
		/** Add a new item to the persistent data store. The item will be updated with a unique key, as well
		 * the item URI will be updated to include that defined unique team.
		 * @param item to be added
		 * @throws IllegalArgumentException for a null argument
		 */


		/**
		 * Collection of resources of a particular class in the persistent store. The will be extended
		 * in the future to return the collection of resources accessible to a particular user.
		 * @return the collection
		 */
		public Collection<Progress> getCollection(int start, int n, User owner) {
			return owner.isAdmin()? getCollection(start, n):getCollection(start, n, owner.getUri());
		}
		
		/**
		 * Collection of resources of a particular class in the persistent store. The will be extended
		 * in the future to return the collection of resources accessible to a particular user.
		 * @return the collection
		 */
		public Collection<Progress> getCollection(int start, int n) {
			log.info("admin query");
			Collection<Progress> result = null;
			try {
				List<Progress> children = itemDao.ofy().query(itemDao.clazz).order("-started").offset(start).limit(n).list();
				if(children != null) {
					log.info("children is "+children.size());
				} else {
					log.info("children is null");
				}
				result = new Collection<Progress>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), children);
			} catch (NotFoundException e) {
			}
			result.setTotal(itemDao.ofy().query(itemDao.clazz).count());
			log.info("admin query total (with sort) -is- "+result.getTotal());

			return result;
		}
		
		/**
		 * Collection of resources of a particular class in the persistent store. The will be extended
		 * in the future to return the collection of resources accessible to a particular user.
		 * @return the collection
		 */
		public Collection<Progress> getCollection(int start, int n, URI owner) {
			log.info("non-admin query");
			Collection<Progress> result = null;
			Query<Progress> query = itemDao.ofy().query(itemDao.clazz).filter("owner", owner.toString());
			try {
				// 3 fetch approaches.. the one in place appears to be fastest for now
				List<Progress> items = query.order("-started").offset(start).limit(n).list();
				// vs ....
				// 	QueryResultIterable<Key<Progress>> keylist = query.order("-started").offset(start).limit(n).fetchKeys();
				//	Map<Key<Progress>, Progress> map = itemDao.ofy().get(keylist);
				//  List<Progress> items = new ArrayList<Progress>(n);
				//  items.addAll(map.values());
				// vs ....
//				QueryResultIterable<Key<Progress>> keylist = query.order("-started").offset(start).limit(n).fetchKeys();	
//				List<Progress> items = new ArrayList<Progress>(n);
//				for(Key<Progress> key : keylist) {
//					items.add(itemDao.get(key));
//				}
				result = new Collection<Progress>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), items);
			} catch (NotFoundException e) {
			}
			result.setTotal(itemDao.ofy().query(itemDao.clazz).filter("owner", owner.toString()).count());
			return result;
		}
		
		/*
		 * ------------------------------------------------------------------------------------------ *
		 *                      	== Internal support functions ==
		 * ------------------------------------------------------------------------------------------ *
		 */

		public void addNarrative(URI uri, String id,
				NarrativeLevel state, String message) throws NotFoundException, ConcurrentModificationException  {
			Narrative entry = new Narrative();
			entry.setId(id);
			entry.setState(state);
			entry.setText(message);
			entry.setStamp(Calendar.getInstance().getTime());

			Progress progress = null;
			try {
				progress = load(uri);
				progress = lock(progress.getId());
				progress.getNarratives().add(entry);
			} finally {
				release(progress);
			}
		}

		public void updateStart(URI uri, Date started, ActionState state, int percentx10Complete) throws NotFoundException, ConcurrentModificationException {

			Progress progress = null;
			try {
				progress = load(uri);
				progress = lock(progress.getId());
				progress.setStarted(started);
				progress.setPercentx10Complete(percentx10Complete);
				progress.setStatus(state);
			} finally {
				release(progress);
			}
			ChangeManager.factory().addChange(myPath()); // updating start has changed the ordered list
		}

		public void updateComplete(URI uri, Date completed) throws NotFoundException, ConcurrentModificationException {

			Progress progress = null;
			try {
				progress = load(uri);
				progress = lock(progress.getId());
				progress.setCompleted(completed);
			} finally {
				release(progress);
			}
		}
		
		/**
		 * @param uri
		 * @param percentx10
		 * @param duration
		 * @param state 
		 * @param completion
		 * @throws ConcurrentModificationException 
		 * @throws NotFoundException 
		 */
		public Progress updateEstimates(URI uri, int percentx10, ActionState state, Date completion) throws NotFoundException, ConcurrentModificationException {

			Progress progress = null;
			try {
				progress = load(uri);
				progress = lock(progress.getId());
				progress.setPercentx10Complete(percentx10);
				progress.setStatus(state);
				progress.setCompleted(completion);
				return progress;
			} finally {
				release(progress);
			}
		}
		
		/**
		 * @param uri
		 * @param percentx10
		 * @param duration
		 * @param state 
		 * @throws ConcurrentModificationException 
		 * @throws NotFoundException 
		 */
		public Progress updateEstimates(URI uri, int percentx10, long duration, ActionState state) throws NotFoundException, ConcurrentModificationException {

			Progress progress = null;
			try {
				progress = load(uri);
				progress = lock(progress.getId());
				progress.setPercentx10Complete(percentx10);
				if(percentx10 == 1000 || progress.getDuration() == 0) {
					progress.setDuration(duration);
				} else {
					progress.setDuration((long)((0.1*duration)+(0.9*progress.getDuration())));
				}
				
				progress.setStatus(state);
				return progress;
			} finally {
				release(progress);
			}
		}
		
		/**
		 * @param uri
		 * @param percentx10
		 * @param duration
		 * @param state 
		 * @param completion
		 * @throws EntityNotFoundException
		 */
		public Progress updateEstimates(URI uri, int percentx10, long duration, ActionState state, Date completion) throws ConcurrentModificationException, NotFoundException {

			Progress progress = null;
			try {
				progress = load(uri);
				progress = lock(progress.getId());
				progress.setPercentx10Complete(percentx10);
				if(percentx10 == 1000 || progress.getDuration() == 0) {
					progress.setDuration(duration);
				} else {
					progress.setDuration((long)((0.1*duration)+(0.9*progress.getDuration())));
				}
				progress.setStatus(state);
				progress.setCompleted(completion);
				return progress;
			} finally {
				release(progress);
			}
		}

	}

}

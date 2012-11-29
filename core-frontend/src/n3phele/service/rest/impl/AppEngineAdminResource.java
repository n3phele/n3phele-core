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

import java.util.Calendar;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import n3phele.service.core.Resource;
import n3phele.service.model.core.NameValue;

@Path("worker")
public class AppEngineAdminResource {
	private static Logger log = Logger.getLogger(AppEngineAdminResource.class.getName()); 

	@Context HttpServletRequest request;
	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	
	private final Dao dao;
	public AppEngineAdminResource() {
		dao = new Dao();
	}
	
	/*
	 * Task Queue Entry Points.
	 * These entry point will be protected by the restricted admin interface of google and is invoked by the
	 * Queue service in google app engine.
	 */
	
	@GET
	@Path("test")
	@RolesAllowed("admin")
	public String test() {
		log.fine("Test entered"); 
		return Calendar.getInstance().getTime().toString();
	}
	
	/** Dispatch processing of an action.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("action/dispatch")
	@RolesAllowed("admin")
	public NameValue actionDdispatch(@QueryParam ("id") long id, 
									@DefaultValue("false") @QueryParam ("finalize") boolean finalize) {
		int retry = request.getIntHeader("X-AppEngine-TaskRetryCount");
		int retryLimit = Integer.valueOf(Resource.get("actionDispatchRetryLimit", "3"));
		log.fine("Retry count is "+retry);
		if(retry > retryLimit) {
			log.info("Abandoning work .. retry is "+retry);
			return new NameValue("Retry-abandoned", Integer.toString(retry));
		} else
			return new ActionResource(dao).do_dispatch(id, finalize);
	}
	
	/** Kill an existing action.
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("action/{id}/kill")
	@RolesAllowed("admin")
	public NameValue kill(@PathParam ("id") Long id,
						  @QueryParam ("failure") boolean failure) {
		int retry = request.getIntHeader("X-AppEngine-TaskRetryCount");
		int retryLimit = Integer.valueOf(Resource.get("actionKillRetryLimit", "1"));
		log.fine("Retry count is "+retry);
		if(retry > retryLimit) {
			log.info("Abandoning work .. retry is "+retry);
			return new NameValue("Retry-abandoned", Integer.toString(retry));
		} else
			return new ActionResource(dao).do_terminate(id, failure);
	}
	
	/** Delete a repo folder.
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("repo/{id}/deleteFolder")
	@RolesAllowed("admin")
	public NameValue deleteFolder(@PathParam ("id") Long id,
						  @QueryParam ("filename") String filename) {
		int retry = request.getIntHeader("X-AppEngine-TaskRetryCount");
		int retryLimit = Integer.valueOf(Resource.get("actionKillRetryLimit", "1"));
		log.fine("Retry count is "+retry);
		if(retry > retryLimit) {
			log.info("Abandoning work .. retry is "+retry);
			return new NameValue("Retry-abandoned", Integer.toString(retry));
		} else
			return new NameValue("Folder-delete", Boolean.toString(new RepositoryResource(dao).do_deleteFolder(id, filename)));
	}
	

	/** Dispatch processing of an existing Activity.
	 *
	 * @param id
	 * @return
	 */
	
	@GET
	@Path("activity/dispatch")
	@RolesAllowed("admin")
	public Response activityDispatch(@QueryParam ("id") long id,
			 					@QueryParam ("failure")boolean failure,
			 					@QueryParam ("reason") String reason) {
		int retry = request.getIntHeader("X-AppEngine-TaskRetryCount");
		int retryLimit = Integer.valueOf(Resource.get("activityDispatchRetryLimit", "0"));
		log.fine("Retry count is "+retry);
		if(retry > retryLimit) {
			log.info("Abandoning work .. retry is "+retry);
			return Response.ok().build();
		} else
			return new ActivityResource(dao).do_dispatch(id, failure, reason);
	}
	
	/*
	 * Cron Entry Points.
	 * These entry point will be protected by the restricted admin interface of google and is invoked by the
	 * CRON service in google app engine.
	 */

	
	/** Clean away actions that have completed.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("action/clean")
	@RolesAllowed("admin")
	public NameValue cleanSweep() {
		int retry = request.getIntHeader("X-AppEngine-TaskRetryCount");
		int retryLimit = Integer.valueOf(Resource.get("garbageCollectionRetryLimit", "0"));
		log.fine("Retry count is "+retry);
		if(retry > retryLimit) {
			log.info("Abandoning work .. retry is "+retry);
			return new NameValue("Retry-abandoned", Integer.toString(retry));
		} else
			return new ActionResource(dao).do_garbageCollection();
	}

	/** Refresh the status of active actions.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("action/refresh")
	@RolesAllowed("admin")
	public NameValue actionRefresh() {
		return new ActionResource(dao).do_refresh();
	}
	
	/** Refresh the status of active activities.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("activity/refresh")
	@RolesAllowed("admin")
	public NameValue activityRefresh() {
		return new ActivityResource(dao).do_refresh();
	}

}

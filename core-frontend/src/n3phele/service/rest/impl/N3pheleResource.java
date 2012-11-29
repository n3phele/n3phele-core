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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.Activity;
import n3phele.service.model.ChangeGroup;
import n3phele.service.model.ChangeManager;
import n3phele.service.model.CloudProfile;
import n3phele.service.model.Command;
import n3phele.service.model.FileSpecification;
import n3phele.service.model.History;
import n3phele.service.model.ObjectBag;
import n3phele.service.model.Progress;
import n3phele.service.model.Root;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.User;
import n3phele.service.model.repository.Repository;

@Path("/")
public class N3pheleResource {
	private static Logger log = Logger.getLogger(N3pheleResource.class.getName()); 


	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	
	private final Dao dao;
	public N3pheleResource() {
		dao = new Dao();
	}
	
	

	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	public Root list(
			@DefaultValue("true") @QueryParam("summary") Boolean summary,
			@DefaultValue("false") @QueryParam("changeOnly") Boolean changeOnly,
								   @QueryParam("since") Long since) {
		User user = UserResource.toUser(securityContext);
		log.warning("getN3phele entered with summary "+summary+" since "+since+" for user "+user.getName());
		Root root = new Root();
		if(!changeOnly) {
			root.setCloud(dao.cloud().getCollection(UserResource.toUser(securityContext)).collection(summary));
			root.setAccount(dao.account().getCollection(UserResource.toUser(securityContext)).collection(summary));
			root.setRepository(dao.repository().getCollection(UserResource.toUser(securityContext)).collection(summary));
			root.setCommand(dao.command().getCollection(UserResource.toUser(securityContext)).collection(summary));
			root.setActivity(dao.activity().getCollection(UserResource.toUser(securityContext)).collection(summary));
			root.setProgress(dao.progress().getCollection(UserResource.toUser(securityContext)).collection(summary));
		} 
		if(since == null || since == 0) { 
			since = ChangeManager.factory().initCache();
		}
		ChangeGroup changes = ChangeManager.factory().getChanges(since, user.getUri(), user.isAdmin());
		if(changes != null) {
			root.setStamp(changes.getStamp());
			root.setCacheAvailable(true);
			if(!summary) 
				root.setChangeGroup(changes);
			if(changes.getChange() != null) 
				root.setChangeCount(changes.getChange().size());
		} else {
			root.setStamp(ChangeManager.factory().initCache());
		}
		log.info("Change is "+root.getChangeGroup());
		return root;
	}
	
	@POST
	@Produces({"application/json",MediaType.APPLICATION_XML})
	@RolesAllowed("authenticated")
	public ObjectBag bulkFetch(
			@FormParam("progressSet") List<Long> progressIds)  {
		User user = UserResource.toUser(securityContext);
		ObjectBag result = new ObjectBag();
		if(progressIds != null) {
			for(Long i : progressIds) {
				try {
					Progress p = dao.progress().load(i, user);
					result.add(p);
				} catch (Exception e) {
					log.warning("Can't find Progress/"+i+" "+e.getMessage());
				}
			}
		}
		return result;
	}
	
//	@GET
//	@Produces({"application/json",MediaType.APPLICATION_XML})
//	@RolesAllowed("authenticated")
//	@Path("fixCloud") 
//
//	public List<Cloud> fixCloud(@QueryParam ("foo") String foo) {
//		List<Cloud> result = new ArrayList<Cloud>();
//		Collection<Cloud> clouds = dao.cloud().getCollection();
//		for(Cloud c : clouds.getElements()) {
//			URI update = fixURI(c.getUri());
//			URI owner = fixURI(c.getOwner());
//			if(c.getUri() != update || owner != c.getOwner()) {
//				c.setUri(update);
//				c.setOwner(owner);
//				dao.cloud().update(c);
//			}
//			result.add(c);
//		}
//		return result;
//	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixUser") 

	public List<User> fixUser(@QueryParam ("foo") String foo) {
		List<User> result = new ArrayList<User>();
		Collection<User> users = dao.user().getCollection();
		for(User c : users.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			if(c.getUri() != update || owner != c.getOwner()) {
				c.setUri(update);
				c.setOwner(owner);
				dao.user().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixAccount") 

	public List<Account> fixAccount(@QueryParam ("foo") String foo) {
		List<Account> result = new ArrayList<Account>();
		Collection<Account> accounts = dao.account().getCollection();
		for(Account c : accounts.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			URI cloud = fixURI(c.getCloud());
			if(c.getUri() != update || owner != c.getOwner() || cloud != c.getCloud()) {
				c.setUri(update);
				c.setOwner(owner);
				c.setCloud(cloud);
				dao.account().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixRepo") 

	public List<Repository> fixRepo(@QueryParam ("foo") String foo) {
		List<Repository> result = new ArrayList<Repository>();
		Collection<Repository> accounts = dao.repository().getCollection();
		for(Repository c : accounts.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			if(c.getUri() != update || owner != c.getOwner()) {
				c.setUri(update);
				c.setOwner(owner);
				dao.repository().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixProgress") 

	public List<Progress> fixProgress(@QueryParam ("foo") String foo) {
		List<Progress> result = new ArrayList<Progress>();
		Collection<Progress> progress = dao.progress().getCollection();
		for(Progress c : progress.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			URI activity = fixURI(c.getActivity());
			if(c.getUri() != update || owner != c.getOwner() || activity != c.getActivity()) {
				c.setUri(update);
				c.setOwner(owner);
				c.setActivity(activity);
				dao.progress().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixHistory") 

	public List<History> fixHistory(@QueryParam ("foo") String foo) {
		List<History> result = new ArrayList<History>();
		Collection<History> history = dao.history().getCollection();
		for(History c : history.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			if(c.getUri() != update || owner != c.getOwner()) {
				c.setUri(update);
				c.setOwner(owner);
				dao.history().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixCommand") 

	public List<Command> fixCommand(@QueryParam ("foo") String foo) {
		List<Command> result = new ArrayList<Command>();
		Collection<Command> commands = dao.command().getCollection();
		for(Command c : commands.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			boolean dirty = false;
			if(c.getCloudProfiles() != null) {
				for(CloudProfile p : c.getCloudProfiles()) {
					URI cloud = fixURI(p.getCloud());
		
					if(cloud != p.getCloud()) {
						p.setCloud(cloud);
						dirty = true;
					}
				}
			}
			if(c.getUri() != update || owner != c.getOwner() || dirty) {
				c.setUri(update);
				c.setOwner(owner);
				dao.command().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	@GET
	@Produces({"application/json"})
	@RolesAllowed("authenticated")
	@Path("fixActivity") 

	public List<Activity> fixActivity(@QueryParam ("foo") String foo) {
		List<Activity> result = new ArrayList<Activity>();
		Collection<Activity> activities = dao.activity().getCollection();
		for(Activity c : activities.getElements()) {
			URI update = fixURI(c.getUri());
			URI owner = fixURI(c.getOwner());
			URI account = fixURI(c.getAccount());
			URI command = fixURI(c.getCommand());
			URI progress = fixURI(c.getProgress());
			boolean dirtyActions = false;
			if(c.getActions() != null) {
				ArrayList<URI> actions = c.getActions();
				for(int i=0; i < actions.size(); i++) {
					URI p = actions.get(i);
					URI action = fixURI(p);
		
					if(action != p) {
						actions.set(i, action);
						dirtyActions = true;
					}
				}
				if(dirtyActions) {
					c.setActions(actions);
				}
			}
			//
			boolean dirtyInputs = false;
			if(c.getInputs() != null) {
				ArrayList<FileSpecification> inputs = c.getInputs();
				for(int i=0; i < inputs.size(); i++) {
					FileSpecification p = inputs.get(i);
					URI repo = fixURI(p.getRepository());
		
					if(repo != p.getRepository()) {
						p.setRepository(repo);
						dirtyInputs = true;
					}
				}
				if(dirtyInputs) {
					c.setInputs(inputs);
				}
			}
			//
			boolean dirtyOutputs = false;
			if(c.getInputs() != null) {
				ArrayList<FileSpecification> outputs = c.getInputs();
				for(int i=0; i < outputs.size(); i++) {
					FileSpecification p = outputs.get(i);
					URI repo = fixURI(p.getRepository());
		
					if(repo != p.getRepository()) {
						p.setRepository(repo);
						dirtyOutputs = true;
					}
				}
				if(dirtyOutputs) {
					c.setOutputs(outputs);
				}
			}
			
			if(c.getUri() != update || owner != c.getOwner() || account != c.getAccount() || command != c.getCommand() || progress != c.getProgress() 
					|| dirtyActions || dirtyInputs || dirtyOutputs) {
				c.setUri(update);
				c.setOwner(owner);
				c.setAccount(account);
				c.setCommand(command);
				c.setProgress(progress);
				dao.activity().update(c);
			}
			result.add(c);
		}
		return result;
	}
	
	private URI fixURI(URI old) {
		URI result = old;
		if(old != null) {
			String s = old.toString();
			if(!s.startsWith(Resource.get("baseURI", "https://n3phel.appspot.com/resources"))) {
				String updated = s.replaceFirst("https:.*/resources", Resource.get("baseURI", "https://n3phel.appspot.com/resources"));
				result = URI.create(updated);
			}
		}
		return result;
	}
	
}


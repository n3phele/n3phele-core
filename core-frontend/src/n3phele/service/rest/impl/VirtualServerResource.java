/**
 /**
 * @author Cristina Scheibler
 * @author Tiago Ortiz
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;

import com.google.appengine.api.datastore.EntityNotFoundException;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import n3phele.service.actions.tasks.ClientFactory;
import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.Cloud;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.VirtualServerCollection;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Entity;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.User;
import n3phele.service.model.core.VirtualServer;

@Path("/virtualServers")
public class VirtualServerResource {
	private static Logger log = Logger.getLogger(VirtualServerResource.class.getName());
	private static Client client=null;
	private static WebResource resource;
	
	@Context
	UriInfo uriInfo;
	@Context
	SecurityContext securityContext;
	
	private final Dao dao;

	public VirtualServerResource(Dao dao) {
		this.dao = dao;
		if (client==null)
			client = Client.create();
	}

	public VirtualServerResource() {
		this(new Dao());
		if (client==null)
			client = Client.create();
	}

	/*
	 * Create and add a new VirtualServer to the GAE Data Store
	 */

	@POST
	@RolesAllowed("authenticated")
	@Produces("text/plain")
	public Response add(@FormParam("id") String id, @FormParam("name") String name, @FormParam("description") String description, @FormParam("location") URI location,
			@FormParam("parametersList") String parametersList, @FormParam("notification") URI notification, @FormParam("instanceId") String instanceId,
			@FormParam("spotId") String spotId, @FormParam("owner") URI owner, @FormParam("created") String created, @FormParam("price") String price,
			@FormParam("activity") URI activity, @FormParam("account") URI account, @FormParam("clouduri") String clouduri) {

		log.warning("VirtualServer creation started");

		VirtualServer vs = null;
		Type collectionType = new TypeToken<ArrayList<NameValue>>() {
		}.getType();

		// Creating the parameters list from the json
		ArrayList<NameValue> parameters = new Gson().fromJson(parametersList, collectionType);

		Date dateCreated = new Date(created);
		// Creating a new VirtualServer
		vs = new VirtualServer(name, description, location, parameters, notification, instanceId, spotId, owner, dateCreated, price, activity, Long.valueOf(id), account, clouduri);
		if (vs==null)
			log.severe("Can't create Virtual server! ");
		// Ading to the GAE Data Store
		dao.virtualServer().add(vs);

		log.warning("Created " + vs);

		// Return a 201 code
		return Response.created(vs.getUri()).build();
	}

	/*
	 * Return a collection of VirtualServer objects
	 */
	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public VirtualServerCollection list(@DefaultValue("false") @QueryParam("summary") Boolean summary) {

		log.warning("User: " + UserResource.toUser(securityContext).getFirstName() + " " + UserResource.toUser(securityContext).getLastName());

		// Retrieve the collection of VirtualServer from GAE Data Store
		Collection<VirtualServer> result = dao.virtualServer().getCollection(UserResource.toUser(securityContext));

		log.warning("Collection size: " + result.getElements().size());

		if (summary) {
			if (result.getElements() != null) {
				for (int i = 0; i < result.getElements().size(); i++) {
					result.getElements().set(i, VirtualServer.summary(result.getElements().get(i)));
				}
			}
		}

		return new VirtualServerCollection(result, 0, -1);
	}

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	@Path("/account/{id}")
	public VirtualServerCollection listForAccount(@PathParam("id") Long id) {

		// Retrieve the collection of VirtualServer from GAE Data Store
		Collection<VirtualServer> result = dao.virtualServer().getCollection(UserResource.toUser(securityContext));
		List<VirtualServer> accountVS = new ArrayList<VirtualServer>();

		for (VirtualServer vs : result.getElements()) {
			String vsId = vs.getAccount().substring(vs.getAccount().lastIndexOf('/') + 1, vs.getAccount().length());
			if (vsId.equals(id.toString())) {
				accountVS.add(vs);
			}
		}

		// Return the collection
		Collection<VirtualServer> ret = new Collection<VirtualServer>();
		ret.setElements(accountVS);
		return new VirtualServerCollection(ret, 0, -1);
	}

	@GET
	// @RolesAllowed("authenticated")
	@Path("/updateStatus")
	public Response updateStatus() {

		log.warning("Entered n3phele updateStatus");
		
		Collection<VirtualServer> virtualServerCollection = dao.virtualServer().getCollection(); 
		
		Cloud cloud;
		URI factory = null;
		if (!virtualServerCollection.getElements().isEmpty()) {
			
			log.warning("Retrieved virtual server collection");
			
			for (VirtualServer vsDao : virtualServerCollection.getElements()) {
				// Get the cloud information
				if(vsDao!=null && vsDao.getCloudURI()!=null){

					URI uriCloud = URI.create(vsDao.getCloudURI());
					cloud = dao.cloud().get(uriCloud);
					// Connect with the factory
					if(factory == null || factory != cloud.getFactory()) {
						factory = cloud.getFactory();
						client.addFilter(new HTTPBasicAuthFilter(cloud.getFactoryCredential().decrypt().getAccount(), cloud.getFactoryCredential().decrypt().getSecret()));
						client.setConnectTimeout(20000);
						resource = client.resource(factory.toString());
						VirtualServer vs = null;
						try{
					// Get the virtual server of EC2
					vs = resource.path("/" + vsDao.getId()).type(MediaType.APPLICATION_JSON_TYPE).get(VirtualServer.class);
							log.warning("URI === "+resource.path("/" + vsDao.getId()).type(MediaType.APPLICATION_JSON_TYPE));
						} catch (Exception e){
							log.info("Not found in EC2" + e);
						}
					boolean exists = false;

					// compare, and refresh, the two virtual servers
					if (vs != null) {
						if (vsDao.getInstanceId().equals(vs.getInstanceId())) {
							log.warning("Instance " + vs.getInstanceId() + " is " + vs.getStatus() + " name " + vs.getName());
							exists = true;
							if (vs.getStatus().equalsIgnoreCase("terminated") || vs.isZombie()) {
								vsDao.setStatus("terminated");
								vsDao.setEndDate(vs.getEndDate());
								log.warning(vsDao.getInstanceId() + " set as terminated");
								break;
							} else {
								vsDao.setStatus("running");
								log.warning(vsDao.getInstanceId() + " set as running");
								break;
							}
						}
					}
					
					if (exists) {
						dao.virtualServer().update(vsDao);
					} 
					else {
						dao.virtualServer().delete(vsDao);
						log.warning(vsDao.getInstanceId() + " deleted");
					}
				}
			}
		}
		
		
		}
		return Response.ok().build();
	}

	/*
	 * Return a specific VirtualServer according to it's ID
	 */
	@GET
	@Produces("application/json")
	@Path("{id}")
	public VirtualServer getVS(@PathParam("id") Long id) throws URISyntaxException, EntityNotFoundException {

		log.warning("User: " + UserResource.toUser(securityContext).getFirstName() + " " + UserResource.toUser(securityContext).getLastName());

		// Retrieve the VirtualServer related to the specific ID
		VirtualServer vs = dao.virtualServer().load(id, UserResource.toUser(securityContext));
		vs.setAccessKey("");
		vs.setEncryptedKey("");

		return vs;
	}

	@GET
	@Produces("application/json")
	@Path("instanceId/{id}")
	public VirtualServer getVsByInstanceId(@PathParam("id") String instanceId) throws URISyntaxException, EntityNotFoundException {

		log.warning("Entered getVsByInstanceId with: " + instanceId);

		Objectify ofy = ObjectifyService.begin();
		VirtualServer vs = ofy.query(VirtualServer.class).filter("instanceId", instanceId).get();

		if (vs != null) {
			log.warning("VirtualServer: " + vs.getName() + " found");
		} else {
			log.warning("VirtualServer: " + instanceId + " not found");
		}

		return vs;
	}

	/*
	 * Delete a specific VirtualServer acording to it's ID
	 */
	@DELETE
	@Path("{id}")
	@Produces("plain/text")
	@RolesAllowed("authenticated")
	public Response deleteVS(@PathParam("id") Long id, @Context HttpHeaders httpHeaders) throws NotFoundException {

		boolean delete = false;
		if (httpHeaders.getRequestHeader("delete") != null) {
			delete = true;
		}

		log.info("Delete Virtual Server " + id);

		// Load the specific VirtualServer object
		final VirtualServer vs = dao.virtualServer().load(id, UserResource.toUser(securityContext));

		if (vs != null) {

			if (delete) {

				log.warning("Entered deleteVs on VirtualServerResource with instanceId: " + vs.getInstanceId());
				dao.virtualServer().delete(vs);
				return Response.noContent().build();

			} else {

				String account = httpHeaders.getRequestHeader("account").get(0);
				account = account.substring(account.lastIndexOf('/') + 1, account.length());

				// Load the account that owns this VirtualServer
				Account acc = dao.account().load(Long.valueOf(account), UserResource.toUser(securityContext));

				Cloud cloud = dao.cloud().load(acc.getCloud(), UserResource.toUser(securityContext));

				final String factoryURI = cloud.getFactory().toString() + "/" + id.toString();
				client.addFilter(new HTTPBasicAuthFilter(cloud.getFactoryCredential().decrypt().getAccount(), cloud.getFactoryCredential().decrypt().getSecret()));
				client.setReadTimeout(30000);
				client.setConnectTimeout(20000);

				ClientResponse response = client.resource(factoryURI).type(MediaType.APPLICATION_FORM_URLENCODED).delete(ClientResponse.class);

				log.info("Delete response status: " + response.getStatus());

				// Updating the status of the VirtualServer
				if (response.getStatus() == 204) {
					log.info("Marking VS as terminated: " + id);
					vs.setStatus("Terminated");
					vs.setIsAlive(false);
					vs.setEndDate(new Date());
					dao.virtualServer().update(vs);
				}

				// Send a 200 http code
				return Response.noContent().build();
			}

		} else {
			// Send a not modified http code
			return Response.notModified().build();
		}
	}

	static public class VirtualServerManager extends CachingAbstractManager<VirtualServer> {
		public VirtualServerManager() {
		}

		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(VirtualServerResource.class).build();
		}

		@Override
		protected GenericModelDao<VirtualServer> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<VirtualServer>(VirtualServer.class, transactional);
		}

		public VirtualServer load(Long id) throws NotFoundException {
			return super.get(id);
		}

		/**
		 * Locate a item from the persistent store based on the item name.
		 * 
		 * @param name
		 * @param requestor
		 *            requesting user
		 * @return the item
		 * @throws NotFoundException
		 *             is the object does not exist
		 */
		public VirtualServer load(String name) throws NotFoundException {
			return super.get(name);
		}

		/**
		 * Locate a item from the persistent store based on the item URI.
		 * 
		 * @param uri
		 * @param requestor
		 *            requesting user
		 * @return the item
		 * @throws NotFoundException
		 *             is the object does not exist
		 */
		public VirtualServer load(URI uri, User requestor) throws NotFoundException {
			return super.get(uri);
		}

		/**
		 * Locate a item from the persistent store based on the item URI.
		 * 
		 * @param id
		 * @return the item
		 * @throws NotFoundException
		 *             is the object does not exist
		 */
		public VirtualServer load(Long id, User requestor) throws NotFoundException {
			return super.get(id, requestor);
		}
	}
}
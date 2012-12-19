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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
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
import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.Cloud;
import n3phele.service.model.CloudCollection;
import n3phele.service.model.JsonWrapper;
import n3phele.service.model.Region;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.User;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.representation.Form;

@Path("/cloud")
public class CloudResource {
	private static Logger log = Logger.getLogger(CloudResource.class.getName());

	@Context
	UriInfo uriInfo;
	@Context
	SecurityContext securityContext;
	private final Dao dao;
	private JsonWrapper wrapper = null;

	public CloudResource() {
		dao = new Dao();
	}

	@GET
	@Path("zones")
	@Produces("text/plain")
	@RolesAllowed({ "signup", "administrator" })
	public Response getZones(@Context HttpHeaders httpHeaders) {

		String cloudType = httpHeaders.getRequestHeader("cloudType").get(0);

		if (cloudType.equals("HP Cloud")) {
			cloudType = "hp";
		} else if (cloudType.equals("Amazon EC2")) {
			cloudType = "ec2";
		}

		List<Cloud> clouds = dao.cloud().getAll();
		StringBuilder zones = new StringBuilder();
		for (Cloud c : clouds) {
			if (c.getName().toLowerCase().contains(cloudType)) {
				zones.append(c.getName() + ";");
			}
		}
		return Response.ok(zones.toString()).build();
	}

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public CloudCollection list(@DefaultValue("false") @QueryParam("summary") Boolean summary) {

		log.warning("getCloud entered with summary " + summary);

		Collection<Cloud> result = dao.cloud().getCollection(UserResource.toUser(securityContext));
		if (summary) {
			if (result.getElements() != null) {
				for (int i = 0; i < result.getElements().size(); i++) {
					result.getElements().set(i, Cloud.summary(result.getElements().get(i)));
				}
			}
		}
		return new CloudCollection(result, 0, -1);
	}

	@POST
	@Produces("text/plain")
	@RolesAllowed("authenticated")
	public Response create(@FormParam("name") String name, @FormParam("description") String description,
			@FormParam("location") URI location, @FormParam("factory") URI factory, @FormParam("factoryId") String factoryId,
			@FormParam("secret") String secret, @FormParam("isPublic") boolean isPublic,
			@FormParam("regionPrices") String regionPrices) {

		preparePrices();
		Region region = null;
		Region[] regions = wrapper.getConfig().getRegions();
		for (int i = 0; i < regions.length; i++) {
			if (regions[i].getRegion().equals(regionPrices)) {
				region = regions[i];
				break;
			}
		}

		Cloud result = new Cloud(name, description, location, factory, new Credential(factoryId, secret).encrypt(),
				UserResource.toUser(securityContext).getUri(), isPublic, region);
		fetchParameters(result);
		dao.cloud().add(result);

		log.warning("Created " + result);
		return Response.created(result.getUri()).build();
	}

	@GET
	@RolesAllowed("authenticated")
	// @Produces("application/vnd.com.n3phele.CloudEntry+json")
	@Produces("application/json")
	@Path("{id}")
	public Cloud get(@PathParam("id") Long id) throws NotFoundException {

		Cloud item = dao.cloud().load(id, UserResource.toUser(securityContext));
		return item;
	}

	@DELETE
	@RolesAllowed("authenticated")
	@Path("{id}")
	public void delete(@PathParam("id") Long id) throws NotFoundException {
		Cloud item = dao.cloud().load(id, UserResource.toUser(securityContext));
		dao.cloud().delete(item);
	}

	private void preparePrices() {
		try {
			URL url = new URL("http://aws.amazon.com/ec2/pricing/pricing-on-demand-instances.json");

			URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
			
			FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		    fetchOptions.doNotValidateCertificate();
		    fetchOptions.setDeadline(60D);
			
			HTTPRequest request = new HTTPRequest(url, HTTPMethod.GET, fetchOptions);
			HTTPResponse httpResponse = urlFetchService.fetch(request);
		    String response = "";
		    
		    if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_OK) {
		      response = new String(httpResponse.getContent());
		    } 
		    
			Gson gson = new Gson();
			wrapper = gson.fromJson(response, JsonWrapper.class);

		} catch (MalformedURLException e) {
			log.log(Level.SEVERE, "Prepare price exception ", e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Prepare price exception ", e);
		}
	}
	
	/*@GET
	@Produces("text/plain")
	@Path("teste")
	public String teste() {
		new ActionResource(new Dao()).do_dispatch(12, false);
		return "foeee";
	}*/
	

	/*
	 * --------------------------------------------------------------------------
	 * ---------------- * == Private & Internal support functions ==
	 * ------------
	 * --------------------------------------------------------------
	 * ---------------- *
	 */
	private void fetchParameters(Cloud cloud) {
		Client client = Client.create();
		Credential plain = Credential.unencrypted(cloud.getFactoryCredential());
		client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
		
		//Sometimes ec2Factory takes time to respond 
		client.setConnectTimeout(20000);
		
		TypedParameter inputParameters[] = client.resource(cloud.getFactory()).path("inputParameters")
				.get(TypedParameter[].class);
		TypedParameter outputParameters[] = client.resource(cloud.getFactory()).path("outputParameters")
				.get(TypedParameter[].class);
		ArrayList<TypedParameter> in = new ArrayList<TypedParameter>();
		in.addAll(Arrays.asList(inputParameters));
		ArrayList<TypedParameter> out = new ArrayList<TypedParameter>();
		out.addAll(Arrays.asList(outputParameters));
		cloud.setInputParameters(in);
		cloud.setOutputParameters(out);

	}

	public static String testAccount(Cloud cloud, User user, Account account, boolean fix) {
		String result;
		Client client = Client.create();
		Credential plain = Credential.unencrypted(cloud.getFactoryCredential());
		client.addFilter(new HTTPBasicAuthFilter(plain.getAccount(), plain.getSecret()));
		WebResource resource = client.resource(cloud.getFactory()).path("accountTest");
		Credential c = Credential.reencrypt(account.getCredential(), Credential.unencrypted(cloud.getFactoryCredential())
				.getSecret());

		Form form = new Form();
		form.add("fix", fix);
		form.add("id", c.getAccount());
		form.add("secret", c.getSecret());
		form.add("key", account.getName());
		form.add("location", cloud.getLocation().toString());
		form.add("email", user.getName());
		form.add("firstName", user.getFirstName());
		form.add("lastName", user.getLastName());
		form.add("securityGroup", "default");
		try {
			result = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, form);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Test Account exception ", e);
			result = e.getMessage();
		}
		return result;
	}

	public static class CloudManager extends CachingAbstractManager<Cloud> {
		public CloudManager() {
		}

		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(CloudResource.class)
					.build();
		}

		@Override
		protected GenericModelDao<Cloud> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<Cloud>(Cloud.class, transactional);
		}

		public Cloud load(Long id, User requestor) throws NotFoundException {
			return super.get(id, requestor);
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
		public Cloud load(String name, User requestor) throws NotFoundException {
			return super.get(name, requestor);
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
		public Cloud load(URI uri, User requestor) throws NotFoundException {
			return super.get(uri, requestor);
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
		public Cloud load(URI uri) throws NotFoundException {
			return super.get(uri);
		}

	}
}

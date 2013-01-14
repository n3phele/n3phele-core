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
import java.util.HashMap;
import java.util.Map;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.AccountCollection;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.Cloud;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.User;

@Path("/account")
public class AccountResource {
	private static Logger log = Logger.getLogger(AccountResource.class
			.getName());
	private final Dao dao;

	public AccountResource(Dao dao) {
		this.dao = dao;
	}

	public AccountResource() {
		this(new Dao());
	}

	@Context
	UriInfo uriInfo;
	@Context
	SecurityContext securityContext;

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public AccountCollection list(
			@DefaultValue("false") @QueryParam("summary") Boolean summary) {

		log.warning("list Accounts entered with summary " + summary);

		Collection<Account> result = getAccountList(
				UserResource.toUser(securityContext), summary);

		return new AccountCollection(result, 0, -1);
	}

	@POST
	@Produces("text/plain")
	@RolesAllowed("authenticated")
	public Response add(@FormParam("name") String name,
			@FormParam("description") String description,
			@FormParam("cloud") URI cloud,
			@FormParam("accountId") String accountId,
			@FormParam("secret") String secret) {

		Cloud myCloud = dao.cloud().load(cloud,
				UserResource.toUser(securityContext));
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("bad name");
		}
		Account account = new Account(name, description, cloud, new Credential(
				accountId, secret).encrypt(), UserResource.toUser(
				securityContext).getUri(), false);

		dao.account().add(account);
		String result = CloudResource.testAccount(myCloud,
				UserResource.toUser(securityContext), account, true);
		if (result == null || result.trim().length() == 0) {
			log.warning("Created " + account.getUri());
			return Response.created(account.getUri()).build();
		} else {
			log.warning("Created " + account.getUri() + " with warnings "
					+ result);
			return Response.ok(result, MediaType.TEXT_PLAIN_TYPE)
					.location(account.getUri()).build();
		}
	}

	@POST
	@Produces("application/json")
	@Path("{id}")
	@RolesAllowed("authenticated")
	public Account update(@PathParam("id") Long id,
			@FormParam("name") String name,
			@FormParam("description") String description,
			@FormParam("cloud") URI cloud,
			@FormParam("accountId") String accountId,
			@FormParam("secret") String secret) {

		Cloud myCloud = dao.cloud().load(cloud,
				UserResource.toUser(securityContext));
		Account item = dao.account().load(id,
				UserResource.toUser(securityContext));
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("bad name");
		}
		Credential credential = null;
		if (secret != null && secret.trim().length() != 0) {
			credential = new Credential(accountId, secret).encrypt();
		}

		item.setName(name);
		item.setDescription(description == null ? null : description.trim());
		item.setCloud(cloud);
		if (credential != null)
			item.setCredential(credential);
		dao.account().update(item);
		String result = CloudResource.testAccount(myCloud,
				UserResource.toUser(securityContext), item, true);

		log.warning("Updated "
				+ item.getUri()
				+ ((credential != null) ? " including credential " + result
						: ""));
		return item;
	}

	@GET
	@Produces("application/json")
	@Path("{id}")
	@RolesAllowed("authenticated")
	public Account get(@PathParam("id") Long id) throws NotFoundException {
		
		Account item = dao.account().load(id,
				UserResource.toUser(securityContext));
		return item;
	}

	@GET
	@Path("{id}/init")
	@Produces("text/plain")
	@RolesAllowed("authenticated")
	public Response init(@PathParam("id") Long id) throws NotFoundException {
		Account item = dao.account().load(id,
				UserResource.toUser(securityContext));
		Cloud myCloud = dao.cloud().load(item.getCloud(),
				UserResource.toUser(securityContext));
		String result = CloudResource.testAccount(myCloud,
				UserResource.toUser(securityContext), item, true);
		if (result == null || result.trim().length() == 0) {
			return Response.ok("ok", MediaType.TEXT_PLAIN_TYPE)
					.location(item.getUri()).build();
		} else {
			log.warning("Init " + item.getUri() + " with warnings " + result);
			return Response.ok(result, MediaType.TEXT_PLAIN_TYPE)
					.location(item.getUri()).build();
		}
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed("authenticated")
	public void delete(@PathParam("id") Long id) throws NotFoundException {
		Account item = dao.account().load(id,
				UserResource.toUser(securityContext));
		dao.account().delete(item);
	}

	public String createAccountForUser(User user, String accountId,
			String secret, String cloudZoneName) throws NotFoundException {

		Cloud cloud = dao.cloud().load(cloudZoneName, user);
		
		Credential credential = null;
		if (secret != null && secret.trim().length() != 0) {
			credential = new Credential(accountId, secret).encrypt();
		}
		
		String accountName = user.getFirstName() + user.getLastName() + "_" + cloud.getName(); 
		Account defaultAccount = new Account(accountName, cloud.getDescription(),
				cloud.getUri(), credential, user.getUri(), false);
		dao.account().add(defaultAccount);
		String result = CloudResource.testAccount(cloud, user, defaultAccount,
				true);
		return result;
	}

	public Collection<Account> getAccountList(User user, boolean summary) {

		log.warning("list Accounts entered with summary " + summary);

		Collection<Account> result = dao.account().getCollection(user);
		Map<URI, String> cloudMap = new HashMap<URI, String>();

		if (result.getElements() != null) {
			for (int i = 0; i < result.getElements().size(); i++) {
				Account account = result.getElements().get(i);
				URI cloud = account.getCloud();
				if (cloud != null) {
					String cloudName = cloudMap.get(cloud);
					if (cloudName == null) {
						try {
							Cloud c = dao.cloud().get(cloud);
							cloudName = c.getName();
							cloudMap.put(cloud, cloudName);
						} catch (NotFoundException nfe) {
							log.severe("Unknown cloud on account "
									+ account.getUri());
							cloudName = cloud.toString();
						}
					}
					account.setCloudName(cloudName);
				}
				if (summary)
					result.getElements().set(i, Account.summary(account));
			}
		}
		return result;
	}

	static public class AccountManager extends CachingAbstractManager<Account> {
		public AccountManager() {
		}

		@Override
		protected URI myPath() {
			return UriBuilder
					.fromUri(
							Resource.get("baseURI",
									"http://localhost:8888/resources"))
					.path(AccountResource.class).build();
		}

		@Override
		protected GenericModelDao<Account> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<Account>(Account.class, transactional);
		}

		public Account load(Long id, User requestor) throws NotFoundException {
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
		public Account load(String name, User requestor)
				throws NotFoundException {
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
		public Account load(URI uri, User requestor) throws NotFoundException {
			return super.get(uri, requestor);
		}

	}
}

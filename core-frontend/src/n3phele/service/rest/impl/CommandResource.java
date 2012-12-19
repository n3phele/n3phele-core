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
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.xml.bind.JAXBException;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Key;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;
import com.sun.jersey.core.util.Base64;

import n3phele.service.actions.tasks.ActionURI;
import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.ActionSpecification;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.ChangeManager;
import n3phele.service.model.CloudProfile;
import n3phele.service.model.Command;
import n3phele.service.model.AddCommandRequest;
import n3phele.service.model.CommandCollection;
import n3phele.service.model.CommandDefinition;
import n3phele.service.model.CommandDefinitions;
import n3phele.service.model.FileSpecification;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.User;

@Path("/command")
public class CommandResource {

	private static Logger log = Logger.getLogger(CommandResource.class.getName());

	@Context
	UriInfo uriInfo;
	@Context
	SecurityContext securityContext;
	private final Dao dao;

	public CommandResource() {
		dao = new Dao();
	}

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public CommandCollection list(@DefaultValue("false") @QueryParam("summary") Boolean summary,
			@DefaultValue("false") @QueryParam("preferred") boolean preferred,
			@DefaultValue("0") @QueryParam("start") int start, @DefaultValue("-1") @QueryParam("end") int end,
			@QueryParam("search") String search) {

		log.warning("list entered with summary " + summary + " start " + start + " end " + end + " preferred " + preferred);
		int n = 0;
		if (start < 0)
			start = 0;
		if (end >= 0) {
			n = end - start + 1;
			if (n <= 0)
				n = 1;
		}
		if (search != null) {
			search = search.trim().toLowerCase(Locale.ENGLISH);
			if (search.length() == 0)
				search = null;
		}
		Collection<Command> result = null;
		if (search != null || preferred == false) {
			result = dao.command().getCollection(UserResource.toUser(securityContext), preferred);

			List<Command> filtered;
			if (search != null) {
				filtered = new ArrayList<Command>(result.getElements().size());
				for (Command item : result.getElements()) {
					Boolean nameMatch = null;
					Boolean descriptionMatch = null;
					if ((nameMatch = item.getName().toLowerCase(Locale.ENGLISH).contains(search))
							|| (item.getDescription() != null && (descriptionMatch = item.getDescription()
									.toLowerCase(Locale.ENGLISH).contains(search)))) {
						log.info("Adding " + item.getName() + " under " + search + " nameMatch " + nameMatch
								+ " descriptionMatch " + descriptionMatch);
						filtered.add(item);
					}
				}
				result.setElements(filtered);
			}

			result = fixOwnerNameInCollection(result, summary);
			return new CommandCollection(result, start, end);
		} else {
			result = getPreferredCommandFromCache(UserResource.toUser(securityContext), start, n);
			CommandCollection reply = new CommandCollection(result);
			return reply;
		}

	}

	private Collection<Command> fixOwnerNameInCollection(Collection<Command> result, boolean summary) {
		if (result != null) {
			for (int i = 0; i < result.getElements().size(); i++) {
				Command item = result.getElements().get(i);
				if (summary) {
					Command summarized = Command.summary(item);
					try {
						summarized.setOwnerName(getOwnerName(summarized.getOwner()));

					} catch (NotFoundException exception) {
						summarized.setOwnerName(summarized.getOwner().toString());
					}
					result.getElements().set(i, summarized);
				} else {
					try {
						item.setOwnerName(getOwnerName(item.getOwner()));

					} catch (NotFoundException exception) {
						item.setOwnerName(item.getOwner().toString());
					}
				}
			}
		}
		return result;

	}

	private Map<URI, String> cache = null;

	private String getOwnerName(URI owner) throws NotFoundException {
		String result = null;
		if (cache == null)
			cache = new HashMap<URI, String>();
		if (cache.containsKey(owner))
			result = cache.get(owner);
		else {
			User user = dao.user().get(owner);
			result = user.toString();
			cache.put(owner, result);
		}
		return result;
	}

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	@Path("export")
	public CommandDefinitions export(@DefaultValue("false") @QueryParam("preferred") boolean preferred,
			@DefaultValue("0") @QueryParam("start") int start, @DefaultValue("-1") @QueryParam("end") int end,
			@DefaultValue("0") @QueryParam("clone") int clone, @QueryParam("search") String search) {

		log.warning("list entered with start " + start + " end " + end + " preferred " + preferred);
		List<CommandDefinition> result = new ArrayList<CommandDefinition>();
		Collection<Command> cmds = dao.command().getCollection(UserResource.toUser(securityContext), preferred);
		if (cmds.getElements() != null) {
			if (search != null) {
				search = search.trim().toLowerCase(Locale.ENGLISH);
				if (search.length() == 0)
					search = null;
			}
			if (search != null) {
				List<Command> filtered = new ArrayList<Command>(cmds.getElements().size());
				for (Command item : cmds.getElements()) {
					Boolean nameMatch = null;
					Boolean descriptionMatch = null;
					Boolean ownerMatch = null;
					Boolean versionMatch = null;
					if ((nameMatch = item.getName().toLowerCase(Locale.ENGLISH).contains(search))
							|| (item.getDescription() != null && (descriptionMatch = item.getDescription()
									.toLowerCase(Locale.ENGLISH).contains(search)))
							|| (item.getOwner() != null && (ownerMatch = item.getOwner().toString().toLowerCase(Locale.ENGLISH)
									.contains(search)))
							|| (item.getVersion() != null && (versionMatch = item.getVersion().toLowerCase(Locale.ENGLISH)
									.contains(search)))) {
						log.info("Adding " + item.getName() + " under " + search + " nameMatch " + nameMatch
								+ " descriptionMatch " + descriptionMatch + " ownerMatch " + ownerMatch + " versionMatch "
								+ versionMatch);
						filtered.add(item);
					}
				}
				cmds.setElements(filtered);
			}
			for (int i = 0; i < cmds.getElements().size(); i++) {
				Command item = cmds.getElements().get(i);
				if (clone == 1) {
					ArrayList<CloudProfile> profiles = item.getCloudProfiles();
					CloudProfile profile = new CloudProfile(profiles.get(0));
					profile.setId(profiles.size() + 1L);
					profile.setDescription("HP Cloud");
					profile.setCloud(URI.create("https://n3phele.appspot.com/resources/cloud/901002"));
					profile.setActions(new ArrayList<ActionSpecification>());
					for (ActionSpecification a : profiles.get(0).getActions()) {
						if (a.getAction().equals(URI.create("http://www.n3phele.com/createvm"))) {
							a = new ActionSpecification(a);
							for (TypedParameter x : a.getInputParameters()) {
								if (x.getName().equals("imageId")) {
									x.setValue("ami-000000e5");
								} else if (x.getName().equals("instanceType")) {
									x.setValue("standard.xsmall");
								}
								if (x.getName().equals("userData")) {
									x.setValue(new String(
											Base64.encode("#!/bin/bash\nchmod a+rwx /mnt\napt-get -y update; apt-get -y install openjdk-6-jre-headless\nset -x\nwget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ubuntu ~/agent /mnt/sandbox https://region-a.geo-1.objects.hpcloudsvc.com:443/v1.0/AUTH_dc700102-734c-4a97-afc8-50530e87a171/n3phele-agent/agent-with-swift.tgz' ubuntu\n")));
								}
							}
						}
						profile.getActions().add(a);
					}

					profiles.add(profile);
					item.setCloudProfiles(profiles);
				} else if (clone == 2) {
					ArrayList<CloudProfile> profiles = item.getCloudProfiles();
					CloudProfile profile = new CloudProfile(profiles.get(0));
					profile.setId(profiles.size() + 1L);
					profile.setDescription("HP Cloud");
					profile.setCloud(URI.create("https://n3phele.appspot.com/resources/cloud/901002"));
					profile.setActions(new ArrayList<ActionSpecification>());
					for (ActionSpecification a : profiles.get(0).getActions()) {
						if (a.getAction().equals(URI.create("http://www.n3phele.com/createvm"))) {
							a = new ActionSpecification(a);
							for (TypedParameter x : a.getInputParameters()) {
								if (x.getName().equals("imageId")) {
									x.setValue("ami-0000379f");
								} else if (x.getName().equals("instanceType")) {
									x.setValue("standard.xlarge");
								}
								if (x.getName().equals("userData")) {
									x.setValue(new String(
											Base64.encode("#!/bin/bash\nchmod a+rwx /mnt\nset -x\nwget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ubuntu ~/agent /mnt/sandbox https://region-a.geo-1.objects.hpcloudsvc.com:443/v1.0/AUTH_dc700102-734c-4a97-afc8-50530e87a171/n3phele-agent/agent-with-swift.tgz' ubuntu\n")));
								}
							}
						}
						profile.getActions().add(a);
					}

					profiles.add(profile);
					item.setCloudProfiles(profiles);
				}
				result.add(new CommandDefinition(item));
			}
		}
		return new CommandDefinitions(result);
	}

	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("import")
	@RolesAllowed("authenticated")
	public Response importer(@Context HttpServletRequest request) {

		try {
			ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iterator = upload.getItemIterator(request);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					log.warning("Got a form field: " + item.getFieldName());
				} else {
					log.warning("Got an uploaded file: " + item.getFieldName() + ", name = " + item.getName());
					// JAXBContext jc =
					// JAXBContext.newInstance(CommandDefinitions.class);
					// Unmarshaller u = jc.createUnmarshaller();
					// CommandDefinitions a = (CommandDefinitions)
					// u.unmarshal(stream);
					JSONJAXBContext jc = new JSONJAXBContext(CommandDefinitions.class);
					JSONUnmarshaller u = jc.createJSONUnmarshaller();

					CommandDefinitions a = u.unmarshalFromJSON(stream, CommandDefinitions.class);
					StringBuilder response = new StringBuilder("Processing " + a.getCommand().size() + " commands\n");

					URI requestor = UserResource.toUser(securityContext).getUri();

					for (CommandDefinition cd : a.getCommand()) {
						response.append(cd.getName());
						response.append(".....");
						Command existing = null;

						try {
							if (cd.getUri() != null && cd.getUri().toString().length() != 0) {
								try {
									existing = dao.command().get(cd.getUri());
								} catch (NotFoundException e1) {
									List<Command> others = null;
									try {
										others = dao.command().getList(cd.getName());
										for (Command c : others) {
											if (c.getVersion().equals(cd.getVersion()) || !requestor.equals(c.getOwner())) {
												existing = c;
											} else {
												if (c.isPreferred() && cd.isPreferred()) {
													c.setPreferred(false);
													dao.command().update(c);
												}
											}
										}
									} catch (Exception e) {
										// not found
									}
								}
							} else {
								List<Command> others = null;
								try {
									others = dao.command().getList(cd.getName());
									for (Command c : others) {
										if (c.getVersion().equals(cd.getVersion()) || !requestor.equals(c.getOwner())) {
											existing = c;
											break;
										}
									}
								} catch (Exception e) {
									// not found
								}

							}

						} catch (NotFoundException e) {

						}

						if (existing == null) {
							response.append(addCommand(cd));
						} else {
							// update or illegal operation
							boolean isOwner = requestor.equals(existing.getOwner());
							boolean mismatchedUri = (cd.getUri() != null && cd.getUri().toString().length() != 0)
									&& !cd.getUri().equals(existing.getUri());
							log.info("requestor is " + requestor + " owner is " + existing.getOwner() + " isOwner " + isOwner);
							log.info("URI in definition is " + cd.getUri() + " existing is " + existing.getUri()
									+ " mismatchedUri " + mismatchedUri);

							if (!isOwner || mismatchedUri) {
								response.append("ignored: already exists");
							} else {
								response.append(updateCommand(cd, existing));
								response.append("\nupdated uri " + existing.getUri());
							}
						}

						response.append("\n");
						// response.append("</li>");
					}
					// response.append("</ol>");
					return Response.ok(response.toString()).build();
				}
			}

		} catch (JAXBException e) {
			log.log(Level.WARNING, "JAXBException", e);

		} catch (FileUploadException e) {
			log.log(Level.WARNING, "FileUploadException", e);
		} catch (IOException e) {
			log.log(Level.WARNING, "IOException", e);
		}
		return Response.notModified().build();
	}

	@POST
	@Produces("text/plain")
	@RolesAllowed("authenticated")
	public Response add(AddCommandRequest request) {

		ArrayList<FileSpecification> inputs = new ArrayList<FileSpecification>();
		if (request.getInputFiles() != null)
			inputs.addAll(request.getInputFiles());

		ArrayList<FileSpecification> outputs = new ArrayList<FileSpecification>();
		if (request.getOutputFiles() != null)
			outputs.addAll(request.getOutputFiles());

		ArrayList<TypedParameter> executionParameters = new ArrayList<TypedParameter>();
		if (request.getExecutionParameters() != null)
			executionParameters.addAll(request.getExecutionParameters());
		ArrayList<TypedParameter> outputParameters = new ArrayList<TypedParameter>();
		if (request.getOutputParameters() != null)
			outputParameters.addAll(request.getOutputParameters());

		Command result = new Command(request.getName(), request.getDescription(),
				UserResource.toUser(securityContext).getUri(), request.isPublic(), request.isPreferred(), request.getVersion(),
				request.getApplication(), inputs, executionParameters, outputs, outputParameters, null);
		dao.command().add(result);
		log.warning("Created " + result);
		if (request.isPreferred()) {
			Collection<Command> list = dao.command().getCollection(UserResource.toUser(securityContext), true);
			if (list != null && list.getTotal() > 1) {
				for (Command x : list.getElements()) {
					if (!x.getId().equals(result.getId())) {
						if (x.getName().equals(result.getName())) {
							x.setPreferred(false);
							dao.command().update(x);
						}
					}
				}
			}
		}
		return Response.created(result.getUri()).build();
	}

	@POST
	@Produces("text/plain")
	@RolesAllowed("authenticated")
	@Path("{Id}/cloudProfiles")
	public Response addCloudProfile(@PathParam("Id") Long id, CloudProfile profile) throws Exception {

		User owner = dao.user().load(profile.getOwner(), UserResource.toUser(securityContext)); // validate
																								// owner
																								// exists
		log.info("adding profile for" + owner.getName());

		Command item = dao.command().load(id, UserResource.toUser(securityContext));
		item = addProfile(id, profile);
		log.warning("Added " + profile.getId());
		return Response.created(
				uriInfo.getAbsolutePathBuilder().uri(item.getUri()).path("cloudProfiles").path(Long.toString(profile.getId()))
						.build()).build();
	}

	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	@Path("{Id}/cloudProfiles/{Pid}")
	public CloudProfile getCloudProfile(@PathParam("Id") Long id, @PathParam("Pid") long pid) {
		Command command = dao.command().load(id, UserResource.toUser(securityContext));
		List<CloudProfile> profiles = command.getCloudProfiles();
		for (CloudProfile c : profiles) {
			if (c.getId() == pid) {
				return c;
			}
		}
		throw new NotFoundException();
	}

	@GET
	@RolesAllowed("authenticated")
	// @Produces("application/vnd.com.n3phele.CatalogEntry+json")
	@Produces("application/json")
	@Path("{Id}")
	public Command get(@PathParam("Id") Long id) throws NotFoundException {

		Command item = dao.command().load(id, UserResource.toUser(securityContext));
		User owner = dao.user().get(item.getOwner());
		item.setOwnerName(owner.toString());
		URI userUri = UserResource.toUser(securityContext).getUri();
		boolean isAdmin = UserResource.toUser(securityContext).isAdmin();
		Map<URI, List<Account>> accountMap = new HashMap<URI, List<Account>>();
		if (item.getCloudProfiles() != null) {
			Collection<Account> accounts = new AccountResource(dao).getAccountList(UserResource.toUser(securityContext), false);
			if (accounts != null && accounts.getElements() != null) {
				for (Account account : accounts.getElements()) {
					if (accountMap.containsKey(account.getCloud())) {
						List<Account> list = accountMap.get(account.getCloud());
						list.add(account);
					} else {
						List<Account> list = new ArrayList<Account>();
						list.add(account);
						accountMap.put(account.getCloud(), list);
					}
				}
			}
			List<CloudProfile> profiles = item.getCloudProfiles();
			ArrayList<CloudProfile> decoratedProfiles = new ArrayList<CloudProfile>();
			for (CloudProfile profile : profiles) {
				if (isAdmin || profile.isPublic() || profile.getOwner().equals(userUri)) {
					if (accountMap.containsKey(profile.getCloud())) {
						for (Account account : accountMap.get(profile.getCloud())) {
							CloudProfile decorated = new CloudProfile(profile);
							decorated.setAccountName(account.getName());
							decorated.setCloudName(account.getCloudName());
							decorated.setAccountUri(account.getUri());
							decoratedProfiles.add(decorated);
						}
					}
				}
			}
			item.setCloudProfiles(decoratedProfiles);
		}
		return item;
	}

	@DELETE
	@RolesAllowed("authenticated")
	@Path("{id}")
	public void delete(@PathParam("id") Long id) throws NotFoundException {
		Command item = dao.command().load(id, UserResource.toUser(securityContext));
		dao.command().delete(item);
	}

	/*
	 * --------------------------------------------------------------------------
	 * ---------------- * == Private & Internal support functions ==
	 * ------------
	 * --------------------------------------------------------------
	 * ---------------- *
	 */

	private final static String commandCacheKey = "n3phele-command-cache";

	private Collection<Command> getPreferredCommandFromCache(User user, int start, int n) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		CmdLet[] cachedCommands = (CmdLet[]) memcache.get(commandCacheKey);
		if (cachedCommands == null) {
			log.info("Command cache miss");
			cachedCommands = loadCommandCache();
		}

		List<Command> commands = new ArrayList<Command>();
		for (CmdLet cl : cachedCommands) {
			if (cl.isPublic || user.isAdmin() || user.getUri().toString().equals(cl.owner)) {
				Command c = new Command();
				c.setUri(URI.create(cl.URI));
				c.setName(cl.name);
				c.setDescription(cl.description);
				c.setOwnerName(cl.ownerName);
				c.setVersion(cl.version);
				c.setApplication(URI.create(cl.application));
				c.setOwner(URI.create(cl.owner));
				c.setPublic(cl.isPublic);
				c.setPreferred(true);
				commands.add(c);
			}
		}
		int end = start + n;
		if (end > commands.size())
			end = commands.size();
		Collection<Command> collection = dao.command().collectionFactory(commands.subList(start, end));
		collection.setTotal(commands.size());
		return collection;
	}

	private CmdLet[] loadCommandCache() {
		log.info("Init command cache");
		CmdLet[] cachedCommands = null;
		try {
			MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
			Collection<Command> preferredCommands = dao.command().getPreferredCollection();
			cachedCommands = new CmdLet[preferredCommands.getElements().size()];
			for (int i = 0; i < preferredCommands.getElements().size(); i++) {
				Command c = preferredCommands.getElements().get(i);
				CmdLet cl = new CmdLet();
				cl.URI = safeURIString(c.getUri());
				cl.name = c.getName();
				cl.description = c.getDescription();
				cl.ownerName = getOwnerName(c.getOwner());
				cl.version = c.getVersion();
				cl.application = safeURIString(c.getApplication());
				cl.owner = safeURIString(c.getOwner());
				cl.isPublic = c.isPublic();
				cachedCommands[i] = cl;
			}
			memcache.put(commandCacheKey, cachedCommands);
		} catch (Exception e) {
			log.info("Error loading memcache:");
		}
		return cachedCommands;
	}

	private String safeURIString(URI u) {
		if (u == null)
			return null;
		return u.toString();
	}

	private static class CmdLet implements Serializable {
		private static final long serialVersionUID = 1L;
		String URI;
		String name;
		String description;
		String ownerName;
		String version;
		String application;
		String owner;
		boolean isPublic;
	}

	private Command addProfile(long id, CloudProfile profile) throws Exception {
		GenericModelDao<Command> itemDaoTxn = dao.command().itemDaoFactory(true);
		try {
			Command command = itemDaoTxn.get(id);
			if (command.getCloudProfiles() == null)
				command.setCloudProfiles(new ArrayList<CloudProfile>());
			Long maxProfile = 0L;
			for (CloudProfile p : command.getCloudProfiles()) {
				if (p.getId() != null && p.getId() > maxProfile)
					maxProfile = p.getId();
			}
			profile.setId(maxProfile + 1);
			command.getCloudProfiles().add(profile);
			itemDaoTxn.put(command);
			itemDaoTxn.ofy().getTxn().commit();
			ChangeManager.factory().addChange(command);
			return command;
		} catch (Exception e) {
			try {
				itemDaoTxn.ofy().getTxn().rollback();
			} catch (Exception ignore) {

			}
			throw e;
		}
	}

	private String addCommand(CommandDefinition cd) {
		StringBuffer response = new StringBuffer();
		ArrayList<FileSpecification> inputs = new ArrayList<FileSpecification>();
		if (cd.getInputFiles() != null)
			inputs.addAll(cd.getInputFiles());

		ArrayList<FileSpecification> outputs = new ArrayList<FileSpecification>();
		if (cd.getOutputFiles() != null)
			outputs.addAll(cd.getOutputFiles());

		ArrayList<TypedParameter> executionParameters = new ArrayList<TypedParameter>();
		if (cd.getExecutionParameters() != null)
			executionParameters.addAll(cd.getExecutionParameters());
		ArrayList<TypedParameter> outputParameters = new ArrayList<TypedParameter>();
		if (cd.getOutputParameters() != null)
			outputParameters.addAll(cd.getOutputParameters());

		Command result = new Command(cd.getName(), cd.getDescription(), UserResource.toUser(securityContext).getUri(),
				cd.isPublic(), cd.isPreferred(), cd.getVersion(), cd.getIcon(), inputs, executionParameters, outputs,
				outputParameters, null);
		dao.command().add(result);
		log.warning("Created " + result);
		response.append("created " + result.getUri());
		for (CloudProfile profile : cd.getExecutionProfiles()) {
			profile.setOwner(UserResource.toUser(securityContext).toString());
			try {
				fixUserdata(profile);
				result = addProfile(result.getId(), profile);
			} catch (Exception e) {
				response.append(" ignored profile");
			}
			log.warning("Added " + profile.getId());
			response.append(" with profile " + profile.getId());
		}
		if (result.isPreferred()) {
			List<Command> list = dao.command().getList(result.getName());
			if (list != null && list.size() > 1) {
				for (Command x : list) {
					if (!x.getId().equals(result.getId())) {
						if (x.isPreferred()) {
							x.setPreferred(false);
							dao.command().update(x);
						}
					}
				}
			}
		}

		return response.toString();
	}

	private String updateCommand(CommandDefinition cd, Command existing) {
		StringBuffer result = new StringBuffer("updated");
		try {
			existing.setName(cd.getName());
			existing.setDescription(cd.getDescription());
			existing.setVersion(cd.getVersion());
			existing.setPublic(cd.isPublic());
			if (existing.isPreferred() != cd.isPreferred()) {
				if (cd.isPreferred()) {
					List<Command> list = dao.command().getList(existing.getName());
					if (list != null && list.size() > 1) {
						for (Command x : list) {
							if (!x.getId().equals(existing.getId())) {
								if (x.isPreferred()) {
									x.setPreferred(false);
									dao.command().update(x);
								}
							}
						}
					}
				}
			}
			existing.setPreferred(cd.isPreferred());
			existing.setApplication(cd.getIcon());
			ArrayList<FileSpecification> copy = new ArrayList<FileSpecification>();
			if (cd.getInputFiles() != null)
				copy.addAll(cd.getInputFiles());
			existing.setInputFiles(copy);
			copy = new ArrayList<FileSpecification>();
			if (cd.getOutputFiles() != null)
				copy.addAll(cd.getOutputFiles());
			existing.setOutputFiles(copy);
			ArrayList<TypedParameter> copy2 = new ArrayList<TypedParameter>();
			if (cd.getExecutionParameters() != null)
				copy2.addAll(cd.getExecutionParameters());
			existing.setExecutionParameters(copy2);
			copy2 = new ArrayList<TypedParameter>();
			if (cd.getOutputParameters() != null)
				copy2.addAll(cd.getOutputParameters());
			existing.setOutputParameters(copy2);
			if (existing.getCloudProfiles() != null)
				existing.getCloudProfiles().clear();
			dao.command().update(existing);
			ChangeManager.factory().addChange(dao.command().myPath());
			if (cd.getExecutionProfiles() != null) {
				for (CloudProfile profile : cd.getExecutionProfiles()) {
					profile.setOwner(UserResource.toUser(securityContext).toString());
					try {
						fixUserdata(profile);
						addProfile(existing.getId(), profile);
					} catch (Exception e) {
						result.append(" ignored profile");
					}
					log.warning("Added " + profile.getId());
					result.append(" with profile " + profile.getId());
				}
			}
		} catch (NotFoundException nf) {
			return "ignored";
		}

		return result.toString();
	}

	private void fixUserdata(CloudProfile cp) {
		if (cp != null) {
			if (cp.getActions() != null) {
				for (ActionSpecification action : cp.getActions()) {
					if (ActionURI.createvm.equals(action.getAction()) || ActionURI.createVM.equals(action.getAction())) {
						if (action.getInputParameters() != null) {
							for (TypedParameter p : action.getInputParameters()) {
								if ("userData".equals(p.getName())) {
									try {
										if (!isNullOrBlank(p.getValue())) {
											String encoded = new String(Base64.encode(p.getValue()));
											p.setValue(encoded);
										}
									} catch (Exception e) {

									}
									try {
										if (!isNullOrBlank(p.getDefaultValue())) {
											String encoded = new String(Base64.encode(p.getDefaultValue()));
											p.setDefaultValue(encoded);
										}
									} catch (Exception e) {

									}
								}
							}
						}
					}
				}
			}

		}

	}

	private boolean isNullOrBlank(String s) {
		return s == null || s.length() == 0;
	}

	public static class CommandManager extends CachingAbstractManager<Command> {
		public CommandManager() {
		}

		public Collection<Command> collectionFactory(List<Command> list) {
			return new Collection<Command>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), list);
		}

		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(CommandResource.class)
					.build();
		}

		@Override
		protected GenericModelDao<Command> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<Command>(Command.class, transactional);
		}

		public Command load(Long id, User requestor) throws NotFoundException {
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
		public Command load(String name, User requestor) throws NotFoundException {
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
		public Command load(URI uri, User requestor) throws NotFoundException {
			return super.get(uri, requestor);
		}

		/**
		 * Locate a item from the persistent store based on the item URI.
		 * 
		 * @param uri
		 * @return the item
		 * @throws NotFoundException
		 *             is the object does not exist
		 */
		public Command load(URI uri) throws NotFoundException {
			return super.get(uri);
		}

		/**
		 * Update existing Command
		 * 
		 * @param item
		 * @throws NotFoundException
		 */
		public void update(Command item) throws NotFoundException {
			MemcacheServiceFactory.getMemcacheService().delete(commandCacheKey);
			super.update(item);
			MemcacheServiceFactory.getMemcacheService().delete(commandCacheKey);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * n3phele.service.model.CachingAbstractManager#add(n3phele.service.
		 * model.core.Entity)
		 */
		public void add(Command item) throws IllegalArgumentException {
			MemcacheServiceFactory.getMemcacheService().delete(commandCacheKey);
			super.add(item);
			MemcacheServiceFactory.getMemcacheService().delete(commandCacheKey);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * n3phele.service.model.CachingAbstractManager#delete(n3phele.service
		 * .model.core.Entity)
		 */
		public void delete(Command item) {
			MemcacheServiceFactory.getMemcacheService().delete(commandCacheKey);
			super.add(item);
			MemcacheServiceFactory.getMemcacheService().delete(commandCacheKey);
		}

		/**
		 * Collection of all preferred resources of a particular class in the
		 * persistent store.
		 * 
		 * @return the collection
		 */
		public Collection<Command> getPreferredCollection() {
			Collection<Command> result = null;
			try {
				List<Command> children = itemDao.ofy().query(itemDao.clazz).filter("preferred", true).order("name").list();
				result = new Collection<Command>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), children);
			} catch (NotFoundException e) {
			}
			result.setTotal(result.getElements().size());
			return result;
		}

		/**
		 * Collection of all preferred resources of a particular class in the
		 * persistent store.
		 * 
		 * @return the collection
		 */
		public List<Command> getAllPreferredPublic() {
			List<Command> result = null;
			try {
				result = itemDao.ofy().query(itemDao.clazz).filter("preferred", true).filter("isPublic", true).order("name")
						.list();
			} catch (NotFoundException e) {
			}
			return result;
		}

		/**
		 * Subset of a collection of all preferred resources of a particular
		 * class in the persistent store.
		 * 
		 * @param start
		 *            offset for retrieval
		 * @param n
		 *            maximum number of elements to retrieve
		 * @return the collection
		 */
		public Collection<Command> getPreferredCollection(int start, int n) {
			Collection<Command> result = null;
			try {
				log.info("Admin fetch preferred collection start=" + start + " n = " + n);
				List<Key<Command>> keys = itemDao.ofy().query(itemDao.clazz).order("name").filter("preferred", true)
						.offset(start).limit(n).listKeys();
				Map<Key<Command>, Command> childrenMap = itemDao.ofy().get(keys);
				List<Command> children = new ArrayList<Command>(keys.size());
				for (Key<Command> k : keys) {
					children.add(childrenMap.get(k));
				}

				result = new Collection<Command>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), children);
			} catch (NotFoundException e) {
			}
			log.info("Admin fetch preferred collection size");
			result.setTotal(itemDao.ofy().query(itemDao.clazz).filter("preferred", true).count());
			log.info("Admin fetch preferred collection size=" + result.getTotal());
			return result;
		}

		/**
		 * Collection of preferred resources of a particular class accessible by
		 * an owner in the persistent store.
		 * 
		 * @param owner
		 * @return the collection
		 */
		public Collection<Command> getPreferredCollection(URI owner) {
			Collection<Command> result = null;
			try {
				List<Command> owned = itemDao.ofy().query(itemDao.clazz).filter("owner", owner.toString())
						.filter("preferred", true).list();
				List<Command> shared = itemDao.ofy().query(itemDao.clazz).filter("preferred", true).filter("isPublic", true)
						.list();
				List<Command> items = mergeResults(owned, shared, owner);
				result = new Collection<Command>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), items);
			} catch (NotFoundException e) {
			}
			result.setTotal(result.getElements().size());
			return result;
		}

		/**
		 * Subset of a collection of all preferred resources of accessible by an
		 * owner in the persistent store.
		 * 
		 * @param owner
		 * @param start
		 *            offset for retrieval
		 * @param n
		 *            maximum number of elements to retrieve
		 * @return the collection
		 */
		public Collection<Command> getPreferredCollection(URI owner, int start, int n) {
			Collection<Command> result = null;
			List<Command> list = null;
			int owned = 0;
			try {
				owned = itemDao.ofy().query(itemDao.clazz).filter("owner", owner.toString()).filter("preferred", true)
						.filter("isPublic", false).count();
				log.info("Owned is " + owned);
				if (owned > start) {
					list = itemDao.ofy().query(itemDao.clazz).order("name").filter("owner", owner.toString())
							.filter("preferred", true).filter("isPublic", false).offset(start).limit(n).list();
					log.info("Fetched owned " + (list != null ? list.size() : 0));
				}
				if (list == null || list.size() < n) {
					if (list != null) {
						start = 0;
						n = n - list.size();
					}
					log.info("Fetching shared start=" + start + " n=" + n);
					List<Command> shared = itemDao.ofy().query(itemDao.clazz).order("name").filter("preferred", true)
							.filter("isPublic", true).offset(start).limit(n).list();
					if (list == null) {
						list = shared;
					} else {
						list.addAll(shared);
					}
				}
				result = new Collection<Command>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), list);
			} catch (NotFoundException e) {
			}
			int publicCommands = itemDao.ofy().query(itemDao.clazz).filter("preferred", true).filter("isPublic", true).count();
			log.info("Size is owned " + owned + " with " + publicCommands + " public commands");
			result.setTotal(owned + publicCommands);
			return result;
		}

		/**
		 * Collection of resources of a particular class in the persistent
		 * store.
		 * 
		 * @return the collection
		 */
		public Collection<Command> getCollection(User owner, boolean preferred) {
			if (preferred) {
				return owner.isAdmin() ? getPreferredCollection() : getPreferredCollection(owner.getUri());
			} else {
				return owner.isAdmin() ? getCollection() : getCollection(owner.getUri());
			}
		}

		/**
		 * Collection of resources of a particular class in the persistent
		 * store.
		 * 
		 * @return the collection
		 */
		public Collection<Command> getPreferredCollection(User owner, int start, int n) {
			return owner.isAdmin() ? getPreferredCollection(start, n) : getPreferredCollection(owner.getUri(), start, n);
		}

	}
}

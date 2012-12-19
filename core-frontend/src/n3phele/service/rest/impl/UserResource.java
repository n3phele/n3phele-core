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
import java.security.Principal;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import n3phele.service.core.Resource;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.BaseEntity;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.User;

@Path("/user")
public class UserResource {
	final static Logger log = Logger.getLogger(UserResource.class.getName()); 

	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	private final Dao dao;
	public UserResource() {
		dao = new Dao();
	}
	@GET
	@RolesAllowed("authenticated")
	@Produces("application/json")
	public Collection<BaseEntity> list(
			@DefaultValue("false") @QueryParam("summary") Boolean summary) {

		log.warning("list Users entered with summary "+summary);

		Collection<User> result = dao.user().getCollection(UserResource.toUser(securityContext));
		return result.collection(summary);
	}
	/*
	 * This code is used to get the browser to remember the username/password
	 */
	@POST
	@Path("login")
	@Produces("text/plain")
	public Response login() {
		log.info("login.");
		return Response.created(dao.user().myPath()).build();
	}

	@POST
	@RolesAllowed({"signup","administrator"})
	@Produces("text/plain")
	public Response add(@FormParam("email") String email,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("secret") String secret,
			@FormParam("cloudId") String cloudId,
			@FormParam("cloudSecret") String cloudSecret,
			@FormParam("cloudZoneName") String cloudZoneName) {
		
		if(email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
			throw new IllegalArgumentException("bad email");
		}
		if(firstName == null || firstName.trim().length() == 0)
			throw new IllegalArgumentException("bad firstName");
		if(lastName == null || lastName.trim().length() == 0)
			throw new IllegalArgumentException("bad lastName");

		User user = new User(email, firstName, lastName, secret);
		try {
			User exists = dao.user().load(email, Root);
			throw new IllegalArgumentException("User "+exists.getName()+" already exists.");
		} catch (NotFoundException e) {
			
		}
		dao.user().add(user);
		boolean hasCloudInfo = cloudId != null && cloudId.trim().length() != 0;
		welcome(user, hasCloudInfo);
		if(hasCloudInfo) {
			new AccountResource(dao).createAccountForUser(user, cloudId, cloudSecret, cloudZoneName);
			new RepositoryResource(dao).createRepoForUser(user, cloudId, cloudSecret, cloudZoneName);
		}
		log.warning("Created "+user);
		return Response.created(user.getUri()).build();
	}

	private void welcome(User user, boolean ec2Info) {

			try {
				StringBuilder subject = new StringBuilder();
				StringBuilder body = new StringBuilder();
					subject.append("Welcome to cloud computing with n3phele.");
					body.append(user.getFirstName());
					body.append(",\n\nWelcome!\n\nYour account with username \"");
					body.append(user.getName());
					body.append("\" has been established, and is now ready for you to login.\n\n");
					if(ec2Info) {
						body.append("During the account signup process you supplied information about your Amazon EC2 account.");
						body.append("This information is being used to setup a default cloud account for you within the n3phele environment. ");
						body.append("As well, a default repository on Amazon S3 will be set up for you to use. You should recieve additional ");
						body.append("email about these shortly.\n\n");
					}
					body.append("n3phele\n---\nhttps://n3phele.appspot.com");
					

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
						"Email for activity " + user.getUri(), e);
			}
	}
	
	@POST
	@Path("reset")
	@RolesAllowed({"signup","administrator"})
	@Produces("text/plain")
	public Response forgot(@FormParam("email") String email,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName) {
		if(email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
			throw new IllegalArgumentException("bad email");
		}
		if(firstName == null || firstName.trim().length() == 0)
			throw new IllegalArgumentException("bad firstName");
		if(lastName == null || lastName.trim().length() == 0)
			throw new IllegalArgumentException("bad lastName");
		User exists = null;
		try {
			exists = dao.user().load(email, Root);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("User "+exists.getName()+" is unknown.");
		}
		String secret = UUID.randomUUID().toString();
		exists.setCredential(new Credential(email, secret).encrypt());
		dao.user().update(exists);
		sendPasswordChangedEmail(exists, secret);
		log.warning("Updated "+exists);
		return Response.created(exists.getUri()).build();
	}
	
	private void sendPasswordChangedEmail(User user, String secret) {

		try {
			StringBuilder subject = new StringBuilder();
			StringBuilder body = new StringBuilder();
				subject.append("n3phele password reset notification");
				body.append(user.getFirstName());
				body.append(",\n\nYour n3phele login password has been reset. \n\nYour account with username \"");
				body.append(user.getName());
				body.append("\" has the following new password assigned, and is now ready for you to login.\n\n");
				body.append("Your new temporary password is:\n\n");
				body.append(secret);
				body.append("\n\n\nn3phele\n---\nhttps://n3phele.appspot.com");
				

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
					"Email for activity " + user.getUri(), e);
		}
}


	@GET
	@RolesAllowed("authenticated")
	// @Produces("application/vnd.com.n3phele.User+json")
	@Produces("application/json")
	@Path("{id}") 
	public User get( @PathParam ("id") Long id) throws NotFoundException {

		User item = dao.user().load(id, UserResource.toUser(securityContext));
		return item;
	}
	
	@POST
	@RolesAllowed("authenticated")
	// @Produces("application/vnd.com.n3phele.User+json")
	@Produces("application/json")
	@Path("{id}") 
	public User update( @PathParam ("id") Long id,
						@FormParam("email") String email,
						@FormParam("firstName") String firstName,
						@FormParam("lastName") String lastName,
						@FormParam("secret") String secret) throws NotFoundException {
		Credential credential = null;
		User item = dao.user().load(id, UserResource.toUser(securityContext));
		if(email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
			if(!email.equals(item.getName()))
					throw new IllegalArgumentException("bad email");
		}
		if(firstName == null || firstName.trim().length() == 0)
			throw new IllegalArgumentException("bad firstName");
		if(lastName == null || lastName.trim().length() == 0)
			throw new IllegalArgumentException("bad lastName");
		if(secret != null && secret.trim().length() != 0) {
			credential = new Credential(email, secret).encrypt();
		}
			
		item.setName(email);
		item.setFirstName(firstName.trim());
		item.setLastName(lastName.trim());
		if(credential != null)
			item.setCredential(credential);
		dao.user().update(item);
		log.warning("Updated "+ item.getUri()+((credential != null)?" including credential.":""));
		return item;
	}
	
	@GET
	@RolesAllowed("authenticated")
	// @Produces("application/vnd.com.n3phele.User+json")
	@Produces("application/json")
	@Path("byName") 
	public User get( @QueryParam ("id") String id) throws NotFoundException {
		log.info("Username is "+id);
		User item = dao.user().load(id, UserResource.toUser(securityContext));
		return item;
	}

	@DELETE
	@RolesAllowed("authenticated")
	@Path("{id}")
	public void delete(@PathParam ("id") Long id) throws NotFoundException {
		User item = dao.user().load(id, UserResource.toUser(securityContext));
		dao.user().delete(item);
	}

	public static class UserManager extends CachingAbstractManager<User> {
		public UserManager() {	
		}
		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(UserResource.class).build();
		}

		@Override
		protected GenericModelDao<User> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<User>(User.class, transactional);
		}


		public User load(Long id, User requestor) throws NotFoundException { return super.get(id, requestor); }
		/**
		 * Locate a item from the persistent store based on the item name.
		 * @param name
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public User load(String name, User requestor) throws NotFoundException { return super.get(name, requestor); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public User load(URI uri, User requestor) throws NotFoundException { return super.get(uri, requestor); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @param requestor requesting user
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public User load(URI uri) throws NotFoundException { return super.get(uri); }

		/** Add a new item to the persistent data store. The item will be updated with a unique key, as well
		 * the item URI will be updated to include that defined unique team.
		 * @param item to be added
		 * @throws IllegalArgumentException for a null argument
		 */
		public void add(User item) throws IllegalArgumentException { 
			item.setOwner(Root.getUri());
			super.add(item); 
			item.setOwner(item.getUri());
			this.update(item);
		}
		
		/** Tests whether the user has administrator privilege
		 * @param user URI of the user to test
		 * @return true if user has admin privilege
		 */
		public boolean isAdmin(URI user) {
			if(user != null) {
				try {
					User u = load(user);
					return u.isAdmin();
				} catch (Exception e) {
					
				}
			}
			return false;
		}
	}

	public static final User Root;
	static {
		User root = null;
		UserManager initDao = new UserManager();
		try {
			root = initDao.get("root");
			if(!(root.getCredential().decrypt().getAccount().equals("root") ||
					root.isAdmin() ||
					!root.isPublic() ||
					!root.isValidated() ||
					root.getValidationDate()==null)) {
				log.log(Level.SEVERE, "Root object suspect.. recreating "+root.toString());
				throw new Exception();
			}
			log.log(Level.INFO, "Root exists.");
		} catch (Exception e) {
			if(root != null)
				initDao.delete(root);
			root = new User("root", "n3phele", "root-administrator", Resource.get("serviceSecret", "3"));
			root.setAdmin(true);
			root.setValidated(true);
			root.setOwner(URI.create("http://www.n3phele.com"));
			root.setValidationDate(Calendar.getInstance().getTime());  // birthday!
			initDao.addWithoutChangeTracking(root);
			root.setOwner(root.getUri());
			initDao.update(root);
			log.log(Level.SEVERE, "Root object created.");
		}
		Root = root;
	}
	public static User toUser(SecurityContext securityContext) {
		Principal principal = securityContext.getUserPrincipal();
		if(principal instanceof User) {
			return (User) principal;
		}
		throw new IllegalArgumentException();	
	}

}

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

package n3phele.backend;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.SecurityContext;

import com.googlecode.objectify.ObjectifyService;

import n3phele.service.model.core.GenericModelDao;
import n3phele.service.core.NotFoundException;
import n3phele.service.model.core.User;
import n3phele.service.model.repository.Repository;

public class Dao {
	private final static Logger log = Logger.getLogger(Dao.class.getName()); 
	private static UserManager userManager = null;
	private static RepositoryManager repoManager = null;
	static {
		ObjectifyService.register(Repository.class);
		ObjectifyService.register(User.class);
	}

	public static class UserManager extends GenericModelDao<User>{

		public UserManager() {
			super(User.class, false);
		}

		public User load(String username) {
			try {
				User item = this.getByProperty("name", username);
				if(item != null)
					return item;
				log.info("Cant find "+username);
			} catch (Exception e) {
				log.log(Level.INFO, "Cant find "+username, e);
			}
			throw new NotFoundException();
		}

		public User toUser(SecurityContext securityContext) {
			Principal principal = securityContext.getUserPrincipal();
			if(principal instanceof User) {
				return (User) principal;
			}
			throw new IllegalArgumentException();	
		}

	}
	
	public static class RepositoryManager extends GenericModelDao<Repository>{
		protected GenericModelDao<Repository> itemDao;

		public RepositoryManager() {
			super(Repository.class, false);
		}

		public Repository load(Long id, User user) {
			Repository item = get(id);
			if(user.isAdmin() || item.isPublic() || user.getUri().equals(item.getOwner()))
				return item;
			else
				throw new NotFoundException();
		}
		
		public Repository load(Long id) {
			Repository item = get(id);
			return item;
		}


	}

	public static RepositoryManager repository() {
		if(repoManager == null)
			repoManager = new RepositoryManager();
		return repoManager;
	}

	public static UserManager user() {
		if(userManager == null)
			userManager = new UserManager();
		return userManager;
	}
	
}

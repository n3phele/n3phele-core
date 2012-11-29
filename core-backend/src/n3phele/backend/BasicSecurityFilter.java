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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import n3phele.service.core.AuthenticationException;
import n3phele.service.core.ForbiddenException;
import n3phele.service.model.core.User;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class BasicSecurityFilter implements ContainerRequestFilter {
	private final static Logger log = Logger.getLogger(BasicSecurityFilter.class.getName()); 
    @Context UriInfo uriInfo;
    @Context SecurityContext securityContext;
    private static final String REALM = "N3phele user authentication";

    public ContainerRequest filter(ContainerRequest request) {
    	if(securityContext == null || securityContext.getUserPrincipal() == null) {
	        User user = authenticate(request);
	        request.setSecurityContext(new Authorizer(user));
    	} else {
    		log.info(String.format("Security context exists for %s", securityContext.getUserPrincipal().getName()));
    	}
        return request;
    }

    private User authenticate(ContainerRequest request) {
        // Extract authentication credentials
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
    	if(_isSecure())  {
    		log.warning("Secure. Path is "+request.getPath());
    	} 

        if (authentication == null) {
        	log.info("Authentication against path "+request.getPath());
        	if(request.getPath().equals("start") || request.getPath().equals("stop"))
        		return null;
            throw new MappableContainerException
            (new AuthenticationException("Authentication credentials are required", REALM));
        }
        if (!authentication.startsWith("Basic ")) {
            return null;
        }
        
        int end = authentication.indexOf(',');
        if(end > 0) {
        	log.severe("Authentication is "+request.getHeaderValue(ContainerRequest.AUTHORIZATION));
        	log.severe("end is "+end);
        	authentication = authentication.substring(0,end);
        	log.severe("trimmed authentication is "+authentication);
        }
        
        String uidpassword = Base64.base64Decode(authentication.substring("Basic ".length()));
        int idx = uidpassword.indexOf(':');
        if(idx < 0) {
        	throw new WebApplicationException(400); // "Invalid syntax for username and password"
        }
        
       
        String username = uidpassword.substring(0,idx).replace(".at-.", "@");
        String password = uidpassword.substring(idx+1);
        if ((username == null) || (password == null)) {
            throw new WebApplicationException(400);
            // "Missing username or password"
        }

        // Validate the extracted credentials
        User user=null;
        try {
        	user = Dao.user().load(username);
        } catch (Exception badUser) {
        }

        if(user == null || !user.getCredential().decrypt().getSecret().equals(password)) {
        	log.severe("Invalid username or password user='"+username+"' password='"+password+"'");
        	log.severe("Authorization header is '"+request.getHeaderValue(ContainerRequest.AUTHORIZATION)+"'");
            throw new MappableContainerException(new ForbiddenException("Invalid username or password"));
        }
        return user;
    }
    private boolean _isSecure() {
    	return "https".equals(uriInfo.getRequestUri().getScheme());
    }
    
    public class Authorizer implements SecurityContext {

        private User user;

        public Authorizer(final User user) {
            this.user = user;
        }

        public Principal getUserPrincipal() {
            return this.user;
        }

        public boolean isUserInRole(String role) {
        	log.info("Checking role "+role+" with user "+(user==null?"null":"defined"));
        	if(user == null && role.equals("admin")) return true;  // assumes google admin enforcement for cron and tasks
        	if(user == null) {
        		log.warning("Null user checking in role");
        		throw new MappableContainerException (new ForbiddenException("Authentication credentials are required"));
        	}
        	if("authenticated".equals(role)) return user != null;
        	if("administrator".equals(role)) return user.isAdmin();
        	log.severe("Checking for role "+role);
        	return false;
        }

        public boolean isSecure() {
            return _isSecure();
        }

        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }
    
}
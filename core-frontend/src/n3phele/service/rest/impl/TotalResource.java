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
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import n3phele.service.model.Action;

@Path("/sl")
public class TotalResource {
	private static Logger log = Logger.getLogger(TotalResource.class.getName()); 


	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	private final Dao dao;
	public TotalResource() {
		dao = new Dao();
	}
	/*
	 * Warmup entry point
	 */
	@GET
	@Produces("text/plain")
	@Path("total")
	public String total(@DefaultValue("false") @QueryParam("log")boolean logThem) {
		String result;
		List<Action> servers = dao.action().getNonfinalized();
		result=Long.toString(servers.size())+"\n";
		result += Calendar.getInstance().getTime().toString()+"\n";
		log.info(servers.size()+" non-finalized.");
		if(logThem) {
			for(Action a : servers) {
				log.info("Action "+a.getUri()+" name "+a.getName()+" state "+a.getActionTaskImpl().getState());
			}
		}
		return result;
	}
}
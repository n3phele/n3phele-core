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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import n3phele.service.core.Resource;
import n3phele.service.model.CachingAbstractManager;
import n3phele.service.model.History;
import n3phele.service.model.ServiceModelDao;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.BaseEntity;
import n3phele.service.model.core.GenericModelDao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.sun.jersey.api.NotFoundException;

@Path("/history")
public class HistoryResource {
	private static Logger log = Logger.getLogger(HistoryResource.class.getName()); 

	@Context UriInfo uriInfo;
	private final Dao dao;
	public HistoryResource() {
		dao = new Dao();
	}
	@GET
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public Collection<BaseEntity> list(
			@DefaultValue("false") @QueryParam("summary") Boolean summary) throws NotFoundException {

		log.warning("getHistory entered with summary "+summary);

		Collection<BaseEntity> result = dao.history().getCollection().collection(summary);
		return result;
	}

	@GET
	@Path("find/{name}")
	@Produces("application/json")
	@RolesAllowed("authenticated")
	public History find(@PathParam("name") String name,
			@QueryParam("arg") List<String> argument) throws NotFoundException {

		History result = dao.history().findByName(name,argument);
		return result;
	}


	@GET
	// @Produces("application/vnd.com.n3phele.HistoryEntry+json")
	@Produces("application/json")
	@RolesAllowed("authenticated")
	@Path("{id}") 
	public History get( @PathParam ("id") Long id) throws URISyntaxException, EntityNotFoundException {

		History item = dao.history().load(id);
		return item;
	}

	@POST
	@RolesAllowed("authenticated")
	@Produces("text/plain")
	public Response create(@FormParam("value") long value, 
			@FormParam("name") String name, 
			@FormParam("arguments")  List<String> arguments) {
		History result = dao.history().addSample(value, name, arguments.toArray(new String[arguments.size()]));

		dao.history().add(result);

		log.warning("Created "+result.getUri());
		return Response.created(result.getUri()).build();

	}

	@DELETE
	@RolesAllowed("administrator")
	@Path("{id}")
	public void delete(@PathParam ("id") Long id) throws NotFoundException {
		History item = dao.history().load(id);
		dao.history().delete(item);
	}



	public static class  HistoryManager extends CachingAbstractManager<History> {
		public HistoryManager() {
		}
		@Override
		protected URI myPath() {
			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8888/resources")).path(HistoryResource.class).build();
		}

		@Override
		protected GenericModelDao<History> itemDaoFactory(boolean transactional) {
			return new ServiceModelDao<History>(History.class, transactional);
		}

		/**
		 * Located a item from the persistent store based on the item id.
		 * @param id
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public History load(Long id) throws NotFoundException { return super.get(id); }
		/**
		 * Locate a item from the persistent store based on the item name.
		 * @param name
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public History load(String name) throws NotFoundException { return super.get(name); }
		/**
		 * Locate a item from the persistent store based on the item URI.
		 * @param uri
		 * @return the item
		 * @throws NotFoundException is the object does not exist
		 */
		public History load(URI uri) throws NotFoundException { return super.get(uri); }

		
		public History addSample(long value, String name, String ... args) {
			ArrayList<String> arguments = new ArrayList<String>(args.length);
			arguments.addAll(Arrays.asList(args));
			return addSample(value, name, arguments);
		}
	
		public History addSample(long value, String name, ArrayList<String> arguments) {
			History prior = null;
			List<History> histories = this.itemDao().listByProperty("name", name);
			if(histories != null && histories.size() != 0) {
				for(History h : histories) {
					if(subListEquals(h.getArguments(), arguments, arguments.size())) {
						prior = h;
						break;
					}
				}
			}
			if(prior == null) {
				prior = new History(name, arguments, null, UserResource.Root.getUri());
				add(prior);
			}
	
			ArrayList<Long> samples = prior.getMetric();
			if(samples == null || samples.size() == 0)
				prior.setMetric(samples = new ArrayList<Long>(Arrays.asList(0L,0L,0L,0L,0L,0L,0L,0L,0L,0L)));
			samples.remove(0);
			samples.add(value);
			update(prior);
			log.info("Added history "+prior.toString());
			return prior;
		}
	
	
		public History findByName(String name, String ... args) {
			return findByName(name, Arrays.asList(args));
		}
	
	
		public History findByName(String name, List<String> argument) {
			List<History> histories = this.itemDao().listByProperty("name", name);
			if(histories == null || histories.size() == 0)
				return null;
			if(histories.size() == 1)
				return histories.get(0);
	
			for(int i= argument.size(); i > 0; i--) {
				ArrayList<History> result = new ArrayList<History>();
				for(History h : histories) {
					if(subListEquals(h.getArguments(), argument, i)) {
						result.add(h);
					}
				}
				if(result.size() == 0)
					continue;
				if(result.size() == 1)
					return result.get(0);
				return result.get(new Random().nextInt(result.size()));
			}
			return null;
		}

		private boolean subListEquals(List<String> a, List<String>b, int subLength) {
			if(subLength > ((a==null)?0:a.size()))
				return false;
			if(subLength > ((b==null)?0:b.size()))
				return false;
	
			for(int i=0; i < subLength; i++) {
				if(!a.get(i).equals(b.get(i))) {
					return false;
				}
			}
			return true;
		}
	
		public long getNinetyPercentile(String name, String ... args) {
			return getNinetyPercentile(name, Arrays.asList(args));
		}
	
		/**
		 * @return the ninetyPercentile
		 */
		public long getNinetyPercentile(String name, List<String> argument) {
			History history = findByName(name, argument);
			log.info("found history "+history);
			if(history == null) {
				return 0;
			}
			List<Long> samples = history.getMetric();
			long ninetyPercentile = 0;
			if(ninetyPercentile == 0) {
				long top=0;
				long next=0;
				for(Long value : samples) {
					if(value > top) {
						next = top;
						top = value;
					} else if (value > next) {
						next = value;
					}
				}
				if(next == 0) next = top;
				ninetyPercentile = (next + top)/2;
			}
			return ninetyPercentile;
		}
	}

}

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
package n3phele.service.model.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;

public abstract class AbstractManager<Item extends Entity> {

	protected Logger log = Logger.getLogger(this.getClass().getName());
	protected GenericModelDao<Item> itemDao;

	public AbstractManager() {
		super();
		itemDao = itemDaoFactory(false);
	}
	
	/**
	 * Located a item from the persistent store based on the item id.
	 * @param id
	 * @return the item
	 * @throws NotFoundException is the object does not exist
	 */
	public Item get(Long id) throws NotFoundException {
		 try {
			Item item = itemDao.get(id);
			if(item != null)
				return item;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+myPath()+"/"+id.toString(),e);
		}
		throw new NotFoundException();
	 }
	
	/**
	 * Locate a item from the persistent store based on the item URI.
	 * @param uri
	 * @return the item
	 */
	public Item get0(URI uri) throws NotFoundException {
		try {
			Item item = itemDao.getByProperty("uri", uri.toString());
			if(item != null)
				return item;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+uri.toString(),e);
		}
		throw new NotFoundException();
	}
	
	/**
	 * Locate a item from the persistent store based on the item URI.
	 * @param uri
	 * @return the item
	 */
	public Item get(URI uri) throws NotFoundException {
		try {
			String s = uri.toString();
			if(!s.startsWith(myPath().toString())) {
				log.severe("Wrong key "+s+" passed to DAO "+myPath());
				throw new NotFoundException("Wrong key "+s+" passed to DAO "+myPath());
			}
			long id = Long.valueOf(s.substring(s.lastIndexOf("/")+1));
			Item item = itemDao.get(id);
			if(item != null)
				return item;
		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+uri.toString(),e);
		}
		throw new NotFoundException();
	}

	/**
	 * Locate a item from the persistent store based on the item name.
	 * @param name
	 * @return the item
	 */
	public Item get(String name) throws NotFoundException {
		try {
			Item item = itemDao.getByProperty("name", name);
			if(item != null)
				return item;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+myPath()+"with name="+name,e);
		}
		throw new NotFoundException();
	}
	
	/**
	 * Locate a list of item from the persistent store based on the item name.
	 * @param name
	 * @return the List of item
	 */
	public List<Item> getList(String name) throws NotFoundException {
		try {
			List<Item> item = itemDao.listByProperty("name", name);
			if(item != null)
				return item;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+myPath()+"with name="+name,e);
		}
		throw new NotFoundException();
	}
	
	/**
	 * Located a item from the persistent store based on the item id.
	 * @param id
	 * @param owner
	 * @return the item
	 * @throws NotFoundException is the object does not exist
	 */
	public Item get(Long id, URI owner) throws NotFoundException {

		Item item = get(id);
		if(item.isPublic() || owner.equals(item.getOwner()))
			return item;
		else
			throw new NotFoundException();

	 }
	
	/**
	 * Locate a item from the persistent store based on the item URI.
	 * @param uri
	 * @param owner
	 * @return the item
	 */
	private Item get(URI uri, URI owner) throws NotFoundException {
		try {
			Item item = get(uri);
			if(item != null && (item.isPublic() || owner.equals(item.getOwner())))
				return item;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+uri.toString(),e);
		}
		throw new NotFoundException();
	}

	/**
	 * Locate a item from the persistent store based on the item name.
	 * @param name
	 * @param owner
	 * @return the item
	 */
	private Item get(String name, URI owner) throws NotFoundException {
		try {
			Item item = get(name);
			if(item != null && (item.isPublic() || owner.equals(item.getOwner())))
				return item;
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception on fetch of "+myPath()+"with name="+name,e);
		}
		throw new NotFoundException();
	}
	
	/**
	 * Located a item from the persistent store based on the item id.
	 * @param id
	 * @param owner
	 * @return the item
	 * @throws NotFoundException is the object does not exist
	 */
	public Item get(Long id, User owner) throws NotFoundException {
		return owner.isAdmin()? get(id):get(id, owner.getUri());
	 }
	
	/**
	 * Locate a item from the persistent store based on the item URI.
	 * @param uri
	 * @param owner
	 * @return the item
	 */
	public Item get(URI uri, User owner) throws NotFoundException {
		return owner.isAdmin()? get(uri):get(uri, owner.getUri());
	}

	/**
	 * Locate a item from the persistent store based on the item name.
	 * @param name
	 * @param owner
	 * @return the item
	 */
	public Item get(String name, User owner) throws NotFoundException {
		return owner.isAdmin()? get(name):get(name, owner.getUri());
	}
	
	/** Add a new item to the persistent data store. The item will be updated with a unique key, as well
	 * the item URI will be updated to include that defined unique team.
	 * @param item to be added
	 * @throws IllegalArgumentException for a null argument
	 */
	public void add(Item item) throws IllegalArgumentException {
		if(item != null) {
			if(item.getOwner()==null)
				if(!item.isPublic())
					throw new IllegalArgumentException("attempt to persist a private object without owner");
				else
					log.severe("=========================>Object "+item.getName()+" has no owner");
			Key<Item>key = itemDao.put(item);
			item.setUri(URI.create(myPath()+"/"+key.getId()));
			itemDao.put(item);
			log.info("item "+item.getName()+" has id "+key.getId());
			// ChangeManager.factory().addChange(myPath());
		} else 
			throw new IllegalArgumentException("attempt to persist a null object");
	}
	
	/** Update a particular object in the persistent data store
	 * @param item the item to update
	 */
	public void update(Item item)throws NotFoundException {
		if(item != null) {
			Key<Item>key = itemDao.put(item);
			if(!URI.create(myPath()+"/"+key.getId()).equals(item.getUri())) {
				log.severe("Update on non-existent item "+item.toString());
				this.delete(item);
				// ChangeManager.factory().addChange(myPath());
				// ChangeManager.factory().addDelete(item);
				throw new NotFoundException("Cannot update <"+item.getName()+"> at "+item.getUri()+" non-existent item");
			}
			// ChangeManager.factory().addChange(item);	
		}
	}
	
	/**
	 * Delete item from the persistent store
	 * @param item to be deleted
	 */
	public void delete(Item item) {
		if(item != null) {
			log.info("Delete of item "+item.toString());
			itemDao.delete(item);
			// ChangeManager.factory().addChange(myPath());
			// ChangeManager.factory().addDelete(item);
		}
	}
	

	/**
	 * Collection of resources of a particular class in the persistent store. 
	 * @return the collection
	 */
	public Collection<Item> getCollection() {
		Collection<Item> result = null;
		try {
			List<Item> children = itemDao.ofy().query(itemDao.clazz).list();
			result = new Collection<Item>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), children);
		} catch (NotFoundException e) {
		}
		result.setTotal(result.getElements().size());
		return result;
	}
	
	/**
	 * Collection of resources of a particular class in the persistent store.  
	 * @return the collection
	 */
	public Collection<Item> getCollection(URI owner) {
		Collection<Item> result = null;
		try {
			List<Item> owned = itemDao.ofy().query(itemDao.clazz).filter("owner", owner.toString()).list();
			List<Item> shared = itemDao.ofy().query(itemDao.clazz).filter("isPublic", true).list();
			List<Item> items = mergeResults(owned, shared, owner);			
			result = new Collection<Item>(itemDao.clazz.getSimpleName(), URI.create(myPath().toString()), items);
		} catch (NotFoundException e) {
		}
		result.setTotal(result.getElements().size());
		return result;
	}
	
	protected List<Item> mergeResults(List<Item>owned, List<Item>shared, URI owner) {
		List<Item> items = null;
		if(owned != null && owned.size()>0 && shared != null && shared.size()>0) {
			items = new ArrayList<Item>(owned.size()+shared.size());
			if(owned.size() > shared.size()) {
				String ownerId = owner.toString();
				for(Item i : shared) {
					if(!ownerId.equals(i.owner))
						items.add(i);
				}
				items.addAll(owned);
				
			} else {
				for(Item i : owned) {
					if(!i.isPublic)
						items.add(i);
				}
				items.addAll(shared);
			}
		} else if(owned != null && owned.size() > 0) {
			items = owned;
		} else {
			items = shared;
		}
		return items;
		
	}
	
	/**
	 * Collection of resources of a particular class in the persistent store. The will be extended
	 * in the future to return the collection of resources accessible to a particular user.
	 * @return the collection
	 */
	public Collection<Item> getCollection(User owner) {
		return owner.isAdmin()? getCollection():getCollection(owner.getUri());
	}
	
	public List<Item> getAll() {
		List<Item> result = null;
		try {
			result = itemDao.ofy().query(itemDao.clazz).list();
		} catch (NotFoundException e) {
		}
		return result;

	}
	
	public GenericModelDao<Item> itemDao() {
		return this.itemDao;
	}
	/**
	 * 
	 * @return the base URI path for this class
	 */
	protected abstract URI myPath();

	/** Model Data access object for this class of object
	 * @param transactional true to create a transactional persistent model management, false for non-transactional 
	 * @return the modelDao
	 */
	protected abstract GenericModelDao<Item> itemDaoFactory(boolean transactional);
	

}
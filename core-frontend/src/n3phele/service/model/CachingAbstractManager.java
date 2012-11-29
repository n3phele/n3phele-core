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
package n3phele.service.model;

import n3phele.service.core.NotFoundException;
import n3phele.service.model.core.AbstractManager;
import n3phele.service.model.core.Entity;


public abstract class CachingAbstractManager<Item extends Entity> extends AbstractManager<Item> {
	/** Add a new item to the persistent data store. The item will be updated with a unique key, as well
	 * the item URI will be updated to include that defined unique team.
	 * @param item to be added
	 * @throws IllegalArgumentException for a null argument
	 */
	@Override
	public void add(Item item) throws IllegalArgumentException {
			super.add(item);
			if(item != null) ChangeManager.factory().addChange(myPath());
	}
	
	public void addWithoutChangeTracking(Item item) throws IllegalArgumentException {
		super.add(item);
}
	
	
	/** Update a particular object in the persistent data store
	 * @param item the item to update
	 */
	@Override
	public void update(Item item)throws NotFoundException {
		try {
			super.update(item);
		} catch (NotFoundException e) {
			ChangeManager.factory().addChange(myPath());
			throw e;
		}
		if(item != null) ChangeManager.factory().addChange(item);	
	}
	
	/**
	 * Delete item from the persistent store
	 * @param item to be deleted
	 */
	@Override
	public void delete(Item item) {
		if(item != null) {
			super.delete(item);
			ChangeManager.factory().addChange(myPath());
			ChangeManager.factory().addDelete(item);
		}
	}
}

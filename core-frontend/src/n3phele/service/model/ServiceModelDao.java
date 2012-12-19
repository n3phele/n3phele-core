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
/**
 *
 */

import com.googlecode.objectify.ObjectifyService;

import n3phele.service.actions.tasks.CreateVmTask;
import n3phele.service.actions.tasks.ExecuteCommandTask;
import n3phele.service.actions.tasks.FileTransferTask;
import n3phele.service.model.core.GenericModelDao;
import n3phele.service.model.core.User;
import n3phele.service.model.core.VirtualServer;
import n3phele.service.model.repository.Repository;

public class ServiceModelDao<T> extends GenericModelDao<T> {

	static {
		// Register all your entity classes here
		ObjectifyService.register(Command.class);
		ObjectifyService.register(Cloud.class);
		ObjectifyService.register(Repository.class);
		ObjectifyService.register(Activity.class);
		ObjectifyService.register(Action.class);
		ObjectifyService.register(CreateVmTask.class);
		ObjectifyService.register(ExecuteCommandTask.class);
		ObjectifyService.register(FileTransferTask.class);
		ObjectifyService.register(History.class);
		ObjectifyService.register(Account.class);
		ObjectifyService.register(Progress.class);
		ObjectifyService.register(FileSpecification.class);
		ObjectifyService.register(User.class);
		
		//ENTITIES RELATED TO THE CLOUD PRICES
		ObjectifyService.register(Price.class);
		ObjectifyService.register(InstancePrice.class);
		ObjectifyService.register(InstanceSize.class);
		ObjectifyService.register(InstanceType.class);
		ObjectifyService.register(Region.class);
		
		//ENTITIES RELATED TO THE VIRTUALMACHINES CONTROL INSIDE DE ACCOUNTS 
		ObjectifyService.register(VirtualServer.class);

	}
	public ServiceModelDao(Class<T> clazz, boolean transactional) {
		super(clazz, transactional);
	}

}

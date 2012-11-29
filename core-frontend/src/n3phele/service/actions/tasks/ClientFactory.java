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
package n3phele.service.actions.tasks;

import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.api.client.Client;

public class ClientFactory {
	private static ClientFactory instance = null;
	private List<Client> pool;
	
	protected ClientFactory() {} {
		pool = new ArrayList<Client>();
	}
	
	private Client borrow() {
		Client client = null;
		synchronized (pool) {
			if(!pool.isEmpty())
				client = pool.remove(0);
		}
		if(client == null) {
			client = Client.create();
		} else {
			client.removeAllFilters();
		}
		return client;
	}
	
	private void payback(Client client) {
		synchronized (pool) {
			pool.add(client);
		}
	}
	
	public static Client create() {
		if(instance == null) {
			instance = new ClientFactory();
		}
		return instance.borrow();
	}
	
	public static void give(Client client) {
		if(instance == null) {
			instance = new ClientFactory();
		}
		instance.payback(client);
	}

}

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

import n3phele.service.rest.impl.AccountResource.AccountManager;
import n3phele.service.rest.impl.ActionResource.ActionManager;
import n3phele.service.rest.impl.ActivityResource.ActivityManager;
import n3phele.service.rest.impl.CloudResource.CloudManager;
import n3phele.service.rest.impl.CommandResource.CommandManager;
import n3phele.service.rest.impl.HistoryResource.HistoryManager;
import n3phele.service.rest.impl.ProgressResource.ProgressManager;
import n3phele.service.rest.impl.RepositoryResource.RepositoryManager;
import n3phele.service.rest.impl.UserResource.UserManager;
import n3phele.service.rest.impl.VirtualServerResource.VirtualServerManager;

public class Dao {
	private AccountManager accountManager;
	private ActionManager actionManager;
	private ActivityManager activityManager;
	private CloudManager cloudManager;
	private CommandManager commandManager;
	private HistoryManager historyManager;
	private ProgressManager progressManager;
	private UserManager userManager;
	private RepositoryManager repositoryManager;
	private VirtualServerManager virtualServerManager;
	
	public Dao() {};
	
	public AccountManager account() {
		if(accountManager == null)
			accountManager = new AccountResource.AccountManager();
		return accountManager;
	}
	
	public ActionManager action() {
		if(actionManager == null)
			actionManager = new ActionResource.ActionManager();
		return actionManager;
	}

	public ActivityManager activity() {
		if(activityManager == null)
			activityManager = new ActivityResource.ActivityManager();
		return activityManager;
	}

	public CloudManager cloud() {
		if(cloudManager == null)
			cloudManager = new CloudResource.CloudManager();
		return cloudManager;
	}

	public CommandManager command() {
		if(commandManager == null)
			commandManager = new CommandResource.CommandManager();
		return commandManager;
	}

	public HistoryManager history() {
		if(historyManager == null)
			historyManager = new HistoryResource.HistoryManager();
		return historyManager;
	}

	public ProgressManager progress() {
		if(progressManager == null)
			progressManager = new ProgressResource.ProgressManager();
		return progressManager;
	}

	public RepositoryManager repository() {
		if(repositoryManager == null)
			repositoryManager = new RepositoryResource.RepositoryManager();
		return repositoryManager;
	}
	
	public UserManager user() {
		if(userManager == null)
			userManager = new UserResource.UserManager();
		return userManager;
	}
	
	public VirtualServerManager virtualServer(){
		if(virtualServerManager == null)
			virtualServerManager = new VirtualServerResource.VirtualServerManager();
		return virtualServerManager;
	}
}

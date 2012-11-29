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

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import n3phele.service.model.NarrativeLevel;
import n3phele.service.rest.impl.ProgressResource;
import n3phele.service.rest.impl.ProgressResource.ProgressManager;


public class ActionLog {
	private static Logger debug = Logger.getLogger(ActionLog.class.getName()); 
	private URI progressUri;
	private String id;
	
	private ActionLog() {};
	
	public ActionLog(URI progress, String id) {
		this.progressUri= progress;
		this.id = id;
	}
	
	public ActionLog(ActionLog log, String id) {
		this.progressUri = log.progressUri;
		this.id = id;
	}
	static final int MAXMESSAGE=500*1024;
	public void log(NarrativeLevel state, String message) {
		try {
			ProgressManager pm = new ProgressResource.ProgressManager();
			if(message == null || message.length() <= MAXMESSAGE) {
				pm.addNarrative(progressUri, id, state, message);
			} else {
				while(message.length() > MAXMESSAGE) {
					String chunk = message.substring(0, MAXMESSAGE);
					int index = chunk.lastIndexOf("\n");
					if(index < (90*MAXMESSAGE)/100)
						index = MAXMESSAGE;
					if(index != MAXMESSAGE)
						chunk = message.substring(0, index);
					if(message.length() > index+1)
						message = message.substring(index+1);
					else
						message = "";
					pm.addNarrative(progressUri, id, state, chunk);
				}
				if(message.length() > 0)
					pm.addNarrative(progressUri, id, state, message);
			}
			
		} catch (Exception e) {
			debug.log(Level.SEVERE, String.format("Narrative %s %s %s %s failed %s", progressUri, id, state, message, e.getMessage()),e);
		}
	}
	
	public void error(String message) {
		log(NarrativeLevel.error, message);
	}
	
	public void success(String message) {
		log(NarrativeLevel.success, message);
	}
	
	public void warning(String message) {
		log(NarrativeLevel.warning, message);
	}

	public void info(String message) {
		log(NarrativeLevel.info, message);
		
	}
	
	public static ActionLog name(ActionTask me) {
		return new ActionLog(me.getProgress(), me.getName());
	}
	
	public ActionLog withName(String name) {
		this.id = name;
		return this;
	}
	
	public static ActionLog withProgress(URI uri) {
		ActionLog x = new ActionLog();
		x.progressUri = uri;
		return x;
	}
}

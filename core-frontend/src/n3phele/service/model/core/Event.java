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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Event")
@XmlType(name="Event", propOrder={"source", "oldStatus", "newStatus"})
public class Event {
	public URI source;
	public String oldStatus;
	public String newStatus;
	
	public Event() {};
	
	public Event(URI source, String oldStatus, String newStatus) {
		this.source = source;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	
	}
	
	
	public static Event create(URI source, String oldStatus, String newStatus) {
		return new Event(source, oldStatus, newStatus);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Event [source=%s, oldStatus=%s, newStatus=%s]",
				source, oldStatus, newStatus);
	}
	
}

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

import java.text.DateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import com.google.appengine.api.datastore.Text;


@XmlRootElement(name="Narrative")
@XmlType(name = "Narrative", propOrder = { "text", "state", "id", "stamp"}) 

public class Narrative {
	protected String id;
	protected Date stamp;
	protected NarrativeLevel state = NarrativeLevel.undefined;
	protected Text text;
	private final static DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	
	public Narrative() {
		super();
	}
	
	public String toString() {
		return formatter.format(stamp)+" "+id+" "+state+" "+getText();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the stamp
	 */
	public Date getStamp() {
		return stamp;
	}

	/**
	 * @param stamp the stamp to set
	 */
	public void setStamp(Date stamp) {
		this.stamp = stamp;
	}

	/**
	 * @return the state
	 */
	public NarrativeLevel getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(NarrativeLevel state) {
		this.state = state;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return (text==null)?null:text.getValue();
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = (text==null)?null:new Text(text);
	}
	
}

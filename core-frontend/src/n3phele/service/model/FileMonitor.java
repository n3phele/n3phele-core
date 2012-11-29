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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.User;
import n3phele.service.rest.impl.Dao;

@XmlRootElement(name="FileMonitor")
@XmlType(name="FileMonitor", propOrder={})
@Unindexed
@Cached
public class FileMonitor extends FileRef {
	private final static Logger log = Logger.getLogger(FileMonitor.class.getName()); 
	@Serialized private Map<String, String> producer;
	@Serialized private Map<String, List<String>> consumers;
	
	public FileMonitor() {};
	public FileMonitor(FileRef i) {
		super(i);
	}

	public FileMonitor(Dao dao, FileSpecification i, User user) {
		super(i.toFileRef(dao, user));
	}
	public void addConsumer(Action action, String context) {
		if(consumers == null)
			consumers = new HashMap<String, List<String>>();
		if(consumers.get(context) == null) {
			consumers.put(context, new ArrayList<String>());
		}
		consumers.get(context).add(action.getUri().toString());
	}

	public void addProducer(Action action, String context) {
		if(this.producer == null)
			this.producer = new HashMap<String, String> ();
		if(this.producer.get(context) != null) {
			log.severe("Producer "+this.producer+" has been specified. Attempt to add "+action.getUri().toString());
			throw new IllegalArgumentException("Producer "+this.producer+" has been specified. Attempt to add "+action.getUri().toString());
		}
		this.producer.put(context, action.getUri().toString());
	}
	/**
	 * @return the producer
	 */
	@XmlTransient
	public Map<String, String> getProducer() {
		return producer;
	}
	/**
	 * @param producer the producer to set
	 */
	public void setProducer(Map<String, String> producer) {
		this.producer = producer;
	}
	/**
	 * @return the consumers
	 */
	@XmlTransient
	public Map<String, List<String>> getConsumers() {
		return consumers;
	}
	/**
	 * @param consumers the consumers to set
	 */
	public void setConsumers(Map<String, List<String>> consumers) {
		this.consumers = consumers;
	}

	public URI producer(String agentURI) {
		String result = null;
		if(this.producer != null) {
			result = this.producer.get(agentURI);
		}
		return result == null ? null : URI.create(result);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"FileMonitor [producer=%s, consumers=%s, toString()=%s]",
				this.producer, this.consumers, super.toString());
	}

}

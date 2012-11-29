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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.Entity;

@XmlRootElement
@XmlType(name="ObjectBag", propOrder={"stamp", "progress"})
@XmlSeeAlso(Entity.class)
public class ObjectBag {
	private long stamp;
	private Map<Long,Progress> progress;
	
	public ObjectBag() {}
	
	/**
	 * @return the stamp
	 */
	public long getStamp() {
		return stamp;
	}




	/**
	 * @param stamp the stamp to set
	 */
	public void setStamp(long stamp) {
		this.stamp = stamp;
	}




	/**
	 * @return the progress
	 */
	public Collection<Progress> getProgress() {
		return progress==null?null:progress.values();
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(Collection<Progress> progress) {
		if(progress == null || progress.size()==0)
			this.progress = null;
		else {
			this.progress = new HashMap<Long,Progress>();
		}
		for(Progress p : progress) {
			this.progress.put(p.getId(), p);
		}
	};
	
	public void add(Progress progress) {
		if(this.progress == null) this.progress = new HashMap<Long, Progress>();
		this.progress.put(progress.getId(), progress);
	}
	
	
	
	

}

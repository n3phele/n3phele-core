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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Entity;

@XmlRootElement(name="ProgressCollection")
@XmlType(name="Collection", propOrder={"total", "elements"})

public class ProgressCollection extends Entity  {
	private long total;
	private List<Progress> elements;
	/**
	 * 
	 */
	public ProgressCollection() {
		super();
	}
	
	public ProgressCollection(Collection<Progress> pc) {
		super(pc.getName(), pc.getUri(), pc.getMime(), pc.getOwner(), pc.isPublic());
		this.total =( pc.getTotal());
		this.elements = pc.getElements();
	}

	/**
	 * @param name
	 * @param uri
	 * @param elements
	 */
	public ProgressCollection(Collection<Progress> pc, int start, int end) {
		super(pc.getName(), pc.getUri(), pc.getMime(), pc.getOwner(), pc.isPublic());
		this.total =( pc.getTotal());
		this.elements = pc.getElements();
		Collections.sort(this.elements, new Comparator<Progress>() {
			public int compare(Progress o1, Progress o2) {
				Date d1 = o1.getStarted();
				Date d2 = o2.getStarted();
				if(d1 == null || d2 == null) {
					if(d1 == null && d2 == null)
						return 0;
					else if(d1==null)
						return -1;
					else
						return +1;
				} 
				return d2.compareTo(d1);
			}
		});
		if(end < 0 || end > this.elements.size()) end = this.elements.size();
		if(start != 0 || end != this.elements.size())
			this.elements = this.elements.subList(start, end);
	}

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * @return the elements
	 */
	public List<Progress> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<Progress> elements) {
		this.elements = elements;
	}


}

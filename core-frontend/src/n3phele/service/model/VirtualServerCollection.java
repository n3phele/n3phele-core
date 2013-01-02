/**
 * @author Cristina Scheibler
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
 *
 */
package n3phele.service.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Embedded;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.gwt.user.datepicker.client.CalendarUtil;

import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Entity;
import n3phele.service.model.core.VirtualServer;


@XmlRootElement(name="VSCollection")
@XmlType(name="Collection")
public class VirtualServerCollection extends Entity {
	
	private long total;
	@Embedded
	private List<VirtualServer> elements;

	public VirtualServerCollection(){
		super();
	}
	
	/**
	 * @param name
	 * @param uri
	 * @param elements
	 */
	public VirtualServerCollection(Collection<VirtualServer> vs, int start, int end) {
		super(vs.getName(), vs.getUri(), vs.getMime(), vs.getOwner(), vs.isPublic());
		this.total =( vs.getTotal());
		this.elements = vs.getElements();
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
	public List<VirtualServer> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<VirtualServer> elements) {
		this.elements = elements;
	}

}
	

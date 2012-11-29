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

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.Entity;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="History")
@XmlType(name="History", propOrder={"arguments", "metric"})
@Unindexed
@Cached
public class History extends Entity {
	@Id private Long id;
	private ArrayList<Long> metric;
	private ArrayList<String> arguments;

	
	public History() {}
	

	/** 
	 * @param name nominates the category of historical record. An actions on a particular
	 * type of resource is an example category
	 * @param arguments a list of request characteristics that have bearing on the action. For example input
	 * file size, or a selected processing option in match importance order. 
	 * @param metric list of historical metrics for this operation
	 */
	public History(String name, ArrayList<String> arguments, ArrayList<Long> metric, URI owner) {
		super(name, null, "application/vnd.com.n3phele.History+json", owner, true);
		this.id = null;
		this.arguments = arguments;
		this.metric = metric;
	}
	
	

	
	/**
	 * @return the id
	 */
	@XmlTransient
	public Long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * @return the metric
	 */
	public ArrayList<Long> getMetric() {
		return metric;
	}


	/**
	 * @param metric the metric to set
	 */
	public void setMetric(ArrayList<Long> metric) {
		this.metric = metric;
	}


	/**
	 * @return the arguments
	 */
	public ArrayList<String> getArguments() {
		return arguments;
	}


	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(ArrayList<String> arguments) {
		this.arguments = arguments;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 20;
		return String.format(
				"History [id=%s, metric=%s, arguments=%s]",
				id,
				metric != null ? metric.subList(0,
						Math.min(metric.size(), maxLen)) : null,
				arguments != null ? arguments.subList(0,
						Math.min(arguments.size(), maxLen)) : null);
	}
	

}
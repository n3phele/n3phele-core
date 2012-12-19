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
import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.Entity;
import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Account")
@XmlType(name="Account", propOrder={"description", "cloud", "cloudName"})
@Unindexed
@Cached
public class Account extends Entity {
	@Id private Long id;
	private Text description;
	private String cloud;
	private String cloudName;
	@Embedded private Credential credential;
	
	public Account() {}
	

	public Account(String name, String description,
		 URI cloud, Credential credential, URI owner, boolean isPublic) {
		super(name, null, "factory/vnd.com.n3phele.Account+json", owner, isPublic);
		this.id = null;
		setDescription(description);
		this.cloud = (cloud == null) ? null : cloud.toString();
		this.credential = credential;
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
	 * @return the description
	 */
	public String getDescription() {
		return (description==null)?null:description.getValue();
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = (description==null)?null:new Text(description);
	}


	/**
	 * @return the history
	 */
	public URI getCloud() {
		URI result = null;
		if(this.cloud != null)
			result = URI.create(this.cloud);
		return result;
	}


	/**
	 * @param history the history to set
	 */
	public void setCloud(URI cloud) {
		this.cloud = (cloud != null)? cloud.toString():null;
	}


	/**
	 * @return the credential
	 */

	@XmlTransient
	public Credential getCredential() {
		return this.credential;
	}

	

	/**
	 * @return the cloudName
	 */
	@Transient
	public String getCloudName() {
		return this.cloudName;
	}


	/**
	 * @param cloudName the cloudName to set
	 */
	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
	}


	/**
	 * @param credential the credential to set
	 */
	public void setCredential(Credential credential) {
		this.credential = credential;
	}


	public static Account summary(Account c) {
		if(c == null) return null;
		Account result = new Account(c.name, null, null, null, c.getOwner(), c.isPublic);
		result.uri = c.uri;
		return result;
	}
}

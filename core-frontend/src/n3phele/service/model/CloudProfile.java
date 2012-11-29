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

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Serialized;


@XmlRootElement(name="CloudProfile")
@XmlType(name="CloudProfile", propOrder={"id", "description", "cloud", "owner", "public", "actions", "accountName", "accountUri", "cloudName"})


public class CloudProfile  {
	private Long id;
	private Text description;
	private String cloud;
	private String owner;
	private boolean isPublic;
	@Serialized
	private ArrayList<ActionSpecification> actions;
	private String accountName;
	private String cloudName;
	private String accountUri;
	
	public CloudProfile() {}
	
	public CloudProfile(String description, URI cloud, ArrayList<ActionSpecification> actions, String owner, boolean isPublic) {
		setCloud(cloud);
		setDescription(description);
		this.actions = actions;
		setOwner(owner);
		this.isPublic = isPublic;
	}
	
	public CloudProfile(CloudProfile stereotype) {
		this.id = stereotype.id;
		this.description = stereotype.description;
		this.cloud = stereotype.cloud;
		this.owner = stereotype.owner;
		this.isPublic = stereotype.isPublic;
		this.actions = stereotype.actions;
	}

	
	public CloudProfile withAction(ActionSpecification task) {
		if(actions == null) {
			actions = new ArrayList<ActionSpecification>();
		}
		actions.add(task);
		return this;
	}
	
	public CloudProfile withCloud(URI uri) {
		setCloud(uri);
		return this;
	}
	
	/**
	 * @return the id
	 */
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
	 * @return the history
	 */
	public URI getCloud() {
		return (cloud==null)?null:URI.create(cloud);
	}


	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the isPublic
	 */
	public boolean isPublic() {
		return isPublic;
	}
	
	/**
	 * @return the isPublic
	 */
	public boolean getPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @param history the history to set
	 */
	public void setCloud(URI cloud) {
		this.cloud = (cloud==null)?null:cloud.toString();
	}

	/**
	 * @return the actions
	 */
	public ArrayList<ActionSpecification> getActions() {
		return actions;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(ArrayList<ActionSpecification> actions) {
		this.actions = actions;
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
	 * @return the accountName
	 */
	@Transient
	public String getAccountName() {
		return this.accountName;
	}

	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * @return the accountUri
	 */
	@Transient
	public URI getAccountUri() {
		return this.accountUri==null?null:URI.create(this.accountUri);
	}

	/**
	 * @param accountUri the accountUri to set
	 */
	public void setAccountUri(URI accountUri) {
		this.accountUri = accountUri==null?null:accountUri.toString();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 20;
		return String
				.format("CloudProfile [id=%s, description=%s, cloud=%s, owner=%s, isPublic=%s, actions=%s]",
						id,
						getDescription(),
						cloud,
						owner,
						isPublic,
						actions != null ? actions.subList(0,
								Math.min(actions.size(), maxLen)) : null);
	}

}

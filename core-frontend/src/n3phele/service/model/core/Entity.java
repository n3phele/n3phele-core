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

import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Entity")
@XmlType(name="Entity", propOrder={"name", "uri", "mime", "owner", "public", "lock"})
public class Entity {
	
	@Indexed protected String name;
	@Indexed protected String uri;
	@Unindexed protected String mime;
	@Indexed protected String owner;
	@Indexed protected boolean isPublic;
	@Unindexed protected Long lock;

	public Entity() {
		super();
		this.lock = null;
	}
	

	/**
	 * @param name
	 * @param uri
	 * @param mime
	 * @param owner
	 * @param isPublic
	 */
	public Entity(String name, URI uri, String mime, URI owner, boolean isPublic) {
		this();
		this.name = name;
		this.uri = (uri == null)? null : uri.toString();
		this.mime = mime;
		this.owner = (owner == null)?null:owner.toString();
		this.isPublic = isPublic;
	}

	/*
	 * Getters and Setters..
	 * ---------------------
	 */
	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the uri
	 */
	public URI getUri() {
		URI result = null;
		if(this.uri != null)
			result = URI.create(uri);
		return result;
	}


	/**
	 * @param uri the uri to set
	 */
	public void setUri(URI uri) {
		if(uri != null)
			this.uri = uri.toString();
		else
			uri = null;
	}


	/**
	 * @return the mime
	 */
	public String getMime() {
		return mime;
	}


	/**
	 * @param mime the mime to set
	 */
	public void setMime(String mime) {
		this.mime = mime;
	}
	
	/**
	 * @return the owner uri
	 */
	public URI getOwner() {
		return (this.owner==null)?null:URI.create(this.owner);
	}


	/**
	 * @param owner the owner uri to set
	 */
	public void setOwner(URI owner) {
		this.owner = (owner == null)?null: owner.toString();
	}

	/**
	 * @return the isPublic visibility
	 */
	public boolean isPublic() {
		return isPublic;
	}
	
	/**
	 * @return the isPublic visibility
	 */
	public boolean getPublic() {
		return isPublic;
	}

	/**
	 * @param visibility the isPublic visibility to set
	 */
	public void setPublic(boolean visibility) {
		this.isPublic = visibility;
	}


	/**
	 * @return the lock
	 */
	public Long getLock() {
		return lock;
	}


	/**
	 * @param lock the lock to set
	 */
	public void setLock(Long lock) {
		this.lock = lock;
	}
	
}

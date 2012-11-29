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

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.User;
import n3phele.service.model.repository.Repository;
import n3phele.service.rest.impl.Dao;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="FileSpecification")
@XmlType(name="FileSpecification", propOrder={"name", "description", "filename", "repository", "canonicalPath", "size", "modified", "optional"})
@Unindexed
@Cached
public class FileSpecification implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id private long id;
	private String name;
	private Text description;
	private String repository;
	private String filename; // relative to repo root
	private Text canonicalPath;
	private Long size;
	private Date modified;
	private boolean isOptional;
	
	FileSpecification() {}

	
	/**
	 * @param name
	 * @param description
	 * @param repository
	 * @param filename
	 * @param isOptional
	 */
	public FileSpecification(String name, String description, URI repository,
			String filename, boolean isOptional) {
		super();
		this.name = name;
		setDescription(description);
		this.repository = (repository==null)?null:repository.toString();
		this.filename = filename;
		this.isOptional = isOptional;
	}
	
	/**
	 * @param name
	 * @param description
	 * @param repository
	 * @param filename
	 */
	public FileSpecification(String name, String description, URI repository,
			String filename) {
		this(name, description, repository, filename, false);
	}
	
	public FileSpecification(FileRef src) {
		this(src.getName(), src.getDescription(), src.getSource(), src.getKey(), src.isOptional());
	}
	
	/**
	 * @param name
	 * @param description
	 * @param isOptional
	 */
	public FileSpecification(String name, String description, boolean isOptional) {
		this(name, description, null, null, isOptional);
	}
	
	/**
	 * @param name
	 * @param description
	 */
	public FileSpecification(String name, String description) {
		this(name, description, null, null, false);
	}

	public FileSpecification(FileSpecification f) {
		super();
		this.name = f.name;
		setDescription(f.getDescription());
		this.repository = f.repository;
		this.filename = f.filename;
		this.isOptional = f.isOptional;
	}


	public FileRef toFileRef(Dao dao, User user) {
    	FileRef result = new FileRef();
		result.setName(this.name);
		result.setDescription(getDescription());
		result.setKey(this.filename);
		result.setOptional(this.isOptional());
		if (this.repository == null) {
			result.setKind("File");
			result.setRoot(".");
		} else {
			Repository r = dao.repository().load(URI.create(this.repository), user);
			result.setKind(r.getKind());
			result.setRoot(r.getRoot());
			result.setRepoCredential(r.getCredential());
			result.setSource(r.getTarget());
		}
		return result;
	}
	
	/**
	 * @return the id
	 */
	@XmlTransient
	public long getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}


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
	 * @return the repository
	 */
	public URI getRepository() {
		return (repository==null)?null:URI.create(repository);
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(URI repository) {
		this.repository = (repository==null)?null:repository.toString();
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	

	/**
	 * @return the canonicalPath
	 */
	public String getCanonicalPath() {
		return this.canonicalPath==null?null:this.canonicalPath.getValue();
	}

	/**
	 * @param canonicalPath the canonicalPath to set
	 */
	public void setCanonicalPath(String canonicalPath) {
		this.canonicalPath = canonicalPath==null?null:new Text(canonicalPath);
	}

	/**
	 * @return the size
	 */
	public Long getSize() {
		return this.size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Long size) {
		this.size = size;
	}

	/**
	 * @return the modified
	 */
	public Date getModified() {
		return this.modified;
	}

	/**
	 * @param modified the modified to set
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}


	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("FileSpecification [id=%s, name=%s, description=%s, repository=%s, filename=%s, canonicalPath=%s, size=%s, modified=%s, isOptional=%s]",
						this.id, this.name, this.description, this.repository,
						this.filename, this.canonicalPath, this.size,
						this.modified, this.isOptional);
	}

	/**
	 * @return the isOptional
	 */
	public boolean isOptional() {
		return this.isOptional;
	}
	
	/**
	 * @return the isOptional
	 */
	public boolean getOptional() {
		return this.isOptional;
	}

	/**
	 * @param isOptional the isOptional to set
	 */
	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}



}

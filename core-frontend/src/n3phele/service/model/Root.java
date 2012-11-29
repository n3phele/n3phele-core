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


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.BaseEntity;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Entity;


@XmlRootElement
@XmlType(name="Root", propOrder={"stamp", "changeGroup", "changeCount", "cacheAvailable", "account", "activity", "cloud", "command", "fileNode", "progress", "repository", "study", "user"})
@XmlSeeAlso(Entity.class)
public class Root {
	private long stamp;
	private ChangeGroup changeGroup;
	private int changeCount=0;
	private boolean cacheAvailable = false;
	private Collection<BaseEntity> account;
	private Collection<BaseEntity> activity;
	private Collection<BaseEntity> cloud;
	private Collection<BaseEntity> command;
	private Collection<BaseEntity> fileNode;
	private Collection<BaseEntity> progress;
	private Collection<BaseEntity> repository;
	private Collection<BaseEntity> study;
	private Collection<BaseEntity> user;

	
	public Root() {}
	
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
	 * @return the changeGroup
	 */
	public ChangeGroup getChangeGroup() {
		return changeGroup;
	}


	/**
	 * @param changeGroup the changeGroup to set
	 */
	public void setChangeGroup(ChangeGroup changeGroup) {
		this.changeGroup = changeGroup;
	}




	/**
	 * @return the changeCount
	 */
	public int getChangeCount() {
		return changeCount;
	}

	/**
	 * @param changeCount the changeCount to set
	 */
	public void setChangeCount(int changeCount) {
		this.changeCount = changeCount;
	}

	/**
	 * @return the cacheAvailable
	 */
	public boolean isCacheAvailable() {
		return cacheAvailable;
	}

	/**
	 * @param cacheAvailable the cacheAvailable to set
	 */
	public void setCacheAvailable(boolean cacheAvailable) {
		this.cacheAvailable = cacheAvailable;
	}

	/**
	 * @return the account
	 */
	public Collection<BaseEntity> getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Collection<BaseEntity> account) {
		this.account = account;
	}

	/**
	 * @return the activity
	 */
	public Collection<BaseEntity> getActivity() {
		return activity;
	}

	/**
	 * @param activity the activity to set
	 */
	public void setActivity(Collection<BaseEntity> activity) {
		this.activity = activity;
	}

	/**
	 * @return the cloud
	 */
	public Collection<BaseEntity> getCloud() {
		return cloud;
	}

	/**
	 * @param cloud the cloud to set
	 */
	public void setCloud(Collection<BaseEntity> cloud) {
		this.cloud = cloud;
	}

	/**
	 * @return the command
	 */
	public Collection<BaseEntity> getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(Collection<BaseEntity> command) {
		this.command = command;
	}

	/**
	 * @return the progress
	 */
	public Collection<BaseEntity> getProgress() {
		return progress;
	}

	/**
	 * @return the fileNode
	 */
	public Collection<BaseEntity> getFileNode() {
		return this.fileNode;
	}

	/**
	 * @param fileNode the fileNode to set
	 */
	public void setFileNode(Collection<BaseEntity> fileNode) {
		this.fileNode = fileNode;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(Collection<BaseEntity> progress) {
		this.progress = progress;
	}

	/**
	 * @return the repository
	 */
	public Collection<BaseEntity> getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Collection<BaseEntity> repository) {
		this.repository = repository;
	}

	/**
	 * @return the study
	 */
	public Collection<BaseEntity> getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(Collection<BaseEntity> study) {
		this.study = study;
	}

	/**
	 * @return the user
	 */
	public Collection<BaseEntity> getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(Collection<BaseEntity> user) {
		this.user = user;
	}


	
	
}

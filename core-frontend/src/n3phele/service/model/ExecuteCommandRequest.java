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
import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.core.NameValue;

@XmlRootElement(name="ExecuteCommandRequest")
@XmlType(name="ExecuteCommandRequest", propOrder={"name", "notify", "user", "command", "profileId", "account", "parameters", "inputFiles", "outputFiles"})

public class ExecuteCommandRequest {
	private String name;
	private boolean notify;
	private String user;
	private URI command;
	private long profileId;
	private URI account;
	private NameValue[] parameters;
	private FileSpecification[] inputFiles;
	private FileSpecification[] outputFiles;
	
	public ExecuteCommandRequest() {}

	
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
	 * @return the notify
	 */
	public boolean isNotify() {
		return notify;
	}
	/**
	 * @return the notify
	 */
	public boolean getNotify() {
		return notify;
	}


	/**
	 * @param notify the notify to set
	 */
	public void setNotify(boolean notify) {
		this.notify = notify;
	}


	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}


	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}


	/**
	 * @return the command
	 */
	public URI getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(URI command) {
		this.command = command;
	}

	/**
	 * @return the profileId
	 */
	public long getProfileId() {
		return profileId;
	}

	/**
	 * @param profileId the profileId to set
	 */
	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	/**
	 * @return the account
	 */
	public URI getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(URI account) {
		this.account = account;
	}

	/**
	 * @return the parameters
	 */
	public NameValue[] getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(NameValue[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the inputFiles
	 */
	public FileSpecification[] getInputFiles() {
		return inputFiles;
	}

	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputFiles(FileSpecification[] inputFiles) {
		this.inputFiles = inputFiles;
	}

	/**
	 * @return the outputFiles
	 */
	public FileSpecification[] getOutputFiles() {
		return outputFiles;
	}

	/**
	 * @param outputFiles the outputFiles to set
	 */
	public void setOutputFiles(FileSpecification[] outputFiles) {
		this.outputFiles = outputFiles;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + Arrays.hashCode(inputFiles);
		result = prime * result + Arrays.hashCode(outputFiles);
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result + (int) (profileId ^ (profileId >>> 32));
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}





	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("ExecuteCommandRequest [name=%s, notify=%s, user=%s, command=%s, profileId=%s, account=%s, parameters=%s, inputFiles=%s, outputFiles=%s]",
						this.name, this.notify, this.user, this.command,
						this.profileId, this.account,
						Arrays.deepToString(this.parameters),
						Arrays.deepToString(this.inputFiles),
						Arrays.deepToString(this.outputFiles));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecuteCommandRequest other = (ExecuteCommandRequest) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (!Arrays.equals(inputFiles, other.inputFiles))
			return false;
		if (!Arrays.equals(outputFiles, other.outputFiles))
			return false;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		if (profileId != other.profileId)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}


}

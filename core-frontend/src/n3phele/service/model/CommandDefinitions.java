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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="CommandDefinitions")
@XmlType(name="CommandDefinitions", propOrder={"command"})

public class CommandDefinitions {
    private List<CommandDefinition> command;
    
    public CommandDefinitions() {}
    
    public CommandDefinitions(List<CommandDefinition> commands) {
    	this.command = commands;
    }

	/**
	 * @return the command
	 */
	public List<CommandDefinition> getCommand() {
		return this.command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(List<CommandDefinition> command) {
		this.command = command;
	}
    
    
}

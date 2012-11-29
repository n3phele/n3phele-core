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
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="ValidationResponse")
@XmlType(name="ValidationResponse", propOrder={"exists"})
public class ValidationResponse {
	private boolean exists;
	
	public ValidationResponse() {}
	
	public ValidationResponse(boolean exists) {
		this.exists = exists;
	}

	
	/**
	 * @return the exists
	 */
	public boolean getExists() {
		return this.exists;
	}

	/**
	 * @param exists the exists to set
	 */
	public void setExists(boolean exists) {
		this.exists = exists;
	}

}

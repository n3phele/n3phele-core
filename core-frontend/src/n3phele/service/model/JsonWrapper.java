/**
 *  @author Guilherme Birk
 *
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

public class JsonWrapper {
	
	private CloudRegions config;

	public JsonWrapper() {}
	
	public JsonWrapper(CloudRegions config) {
		this.config = config;
	}

	public CloudRegions getConfig() {
		return this.config;
	}

	public void setConfig(CloudRegions config) {
		this.config = config;
	}
}
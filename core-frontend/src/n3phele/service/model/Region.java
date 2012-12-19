/** 
 * @author Guilherme Birk
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

import java.util.ArrayList;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Region")
@XmlType(name="Region")
@Unindexed
@Cached
public class Region {

	@Id
	private Long id;
	private String region;
	@Embedded
	private ArrayList<InstanceType> instanceTypes;
	
	public Region() {}
	
	public Region(String region, ArrayList<InstanceType> instanceTypes) {
		this.region = region;
		this.instanceTypes = instanceTypes;
	}

	public String getRegion() {
		return this.region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public ArrayList<InstanceType> getInstanceTypes() {
		return this.instanceTypes;
	}

	public void setInstanceTypes(ArrayList<InstanceType> instanceTypes) {
		this.instanceTypes = instanceTypes;
	}
}

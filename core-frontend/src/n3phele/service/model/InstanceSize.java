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
 * 
 */

package n3phele.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="InstanceSize")
@XmlType(name="InstanceSize")
@Unindexed
@Cached
public class InstanceSize implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	private Long id;
	private String size;
	@Serialized
	private ArrayList<InstancePrice> valueColumns;
	
	public InstanceSize() {}
	
	public InstanceSize(String size, ArrayList<InstancePrice> valueColumns) {
		this.size = size;
		this.valueColumns = valueColumns;
	}
	
	public void setSize(String size) {
		this.size = size;
	}

	public void setValueColumns(ArrayList<InstancePrice> valueColumns) {
		this.valueColumns = valueColumns;
	}
	
	public String getSize() {
		return this.size;
	}
	
	public ArrayList<InstancePrice> getValueColumns() {
		return this.valueColumns;
	}
}

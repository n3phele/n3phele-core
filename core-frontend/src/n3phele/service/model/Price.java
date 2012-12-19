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

import java.io.Serializable;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Price")
@XmlType(name="Price")
@Unindexed
@Cached
public class Price implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	private Long id;
	private String USD;
	
	public Price() {}
	
	public Price(String USD) {
		this.USD = USD;
	}
	
	public String getUSD() {
		return this.USD;
	}
	
	public void setUSD(String USD) {
		this.USD = USD;
	}
}

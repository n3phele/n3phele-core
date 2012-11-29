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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="RepoSet")
@XmlType(name="RepoSet", propOrder={"nextMarker", "content"})
public class RepoSet  {
	private String marker;
	private ArrayList<String> content;
	
	public RepoSet() {};

	public ArrayList<String> getContent() {
		return this.content;
	}
	
	
	public void setContent(ArrayList<String> content) {
		this.content = content;
	}
	
	public String getNextMarker() {
		return this.marker;
	}

	public void setNextMarker(String marker) {
		this.marker = marker;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"RepoSet [marker=%s, content=%s]",
				this.marker,
				this.content != null ? this.content : null);
	}
	
}

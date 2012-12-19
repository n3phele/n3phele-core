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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.NotSaved;
import com.googlecode.objectify.annotation.Unindexed;

@Unindexed
public class GlobalSetting {
	@NotSaved private final static Objectify ofy = ObjectifyService.begin();
	@Id private Long id;
	@Indexed private String name; // these are indexed so they show in the GAE console datastore viewer create tab
	@Indexed private String value;
	
	public GlobalSetting() {}

	public GlobalSetting(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("GlobalSetting [name=%s, value=\"%s\"]", this.name,
				this.value);
	};
	
	public static Map<String, String> getAll() {
		Map<String, String> result = new HashMap<String,String>();

		List<GlobalSetting> all = ofy.query(GlobalSetting.class).list();
		for(GlobalSetting g : all) {
			result.put(g.getName(), g.getValue());
		}
		return result;
	}
	
	public static boolean init(String initialName, String initialValue) {
		int count = ofy.query(GlobalSetting.class).count();
		if(count == 0) {
			GlobalSetting i = new GlobalSetting(initialName, initialValue);
			ofy.put(i);
		}
		return count == 0;
	}
}

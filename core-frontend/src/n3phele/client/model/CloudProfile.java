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
package n3phele.client.model;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import n3phele.client.model.JsList;

public class CloudProfile extends JavaScriptObject{
	protected CloudProfile() {}
	/**
	 * @return the id
	 */

	public final Long getId() {
		String s = id();
		return s==null?null:Long.valueOf(s);
	}
	public native final String id() /*-{
		return this.id;
	}-*/;
	/**
	 * @return the description
	 */
	public native final String getDescription() /*-{
		return this.description;
	}-*/;
	/**
	 * @return the cloud
	 */
	public native final String getCloud() /*-{
		return this.cloud;
	}-*/;
	/**
	 * @return the owner
	 */
	public native final String getOwner() /*-{
		return this.owner;
	}-*/;
	
	/**
	 * @return the accountName
	 */
	public native final String getAccountName() /*-{
		return this.accountName;
	}-*/;
	
	/**
	 * @return the accountUri
	 */
	public native final String getAccountUri() /*-{
		return this.accountUri;
	}-*/;
	
	/**
	 * @return the cloudName
	 */
	public native final String getCloudName() /*-{
		return this.cloudName;
	}-*/;
	
	/**
	 * @return the isPublic
	 */
	public native final boolean isPublic() /*-{
		return this['public']==null?false:this['public']=="true";
	}-*/;
	/**
	 * @return the actions 
	 */
	public final List<ActionSpecification> getActions() {
		JavaScriptObject jsa = actions();
		return JsList.asList(jsa);
	}
	public native final JavaScriptObject actions() /*-{
		var array = [];
		if(this.actions != undefined && this.actions !=null) {
			if(this.actions.length==undefined) {
				array[0] = this.actions;
			} else {
				array = this.actions;
			}
		}
		return this.actions;
	}-*/;
	
	

}

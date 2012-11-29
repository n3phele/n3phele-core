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

import com.google.gwt.core.client.JavaScriptObject;

public class Change extends JavaScriptObject {
	protected Change() {};
	/**
	 * @return the stamp
	 */
	public final Long getStamp() {
		String s = stamp();
		return s==null?null:Long.valueOf(s);
	}
	public native final String stamp() /*-{
		return this.stamp;
	}-*/;
	/**
	 * @return the uri
	 */
	public native final String getUri() /*-{
		return this.uri;
	}-*/;
	/**
	 * @return the delete
	 */
	public native final boolean isDeleted() /*-{
		return this.deleted==null?false:this.deleted=="true";
	}-*/;
	/**
	 * @return the isPublic
	 */
	public native final boolean isPublic() /*-{
		return this.isPublic==null?false:this.isPublic=="true";
	}-*/;
	/**
	 * @return the owner
	 */
	public native final String getOwner() /*-{
		return this.owner;
	}-*/;
	
	

}

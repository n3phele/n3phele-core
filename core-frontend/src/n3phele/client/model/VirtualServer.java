/**
 * @author Gabriela Lavina
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
 **/
package n3phele.client.model;

import java.util.Date;

import n3phele.client.presenter.helpers.SafeDate;
import n3phele.client.model.Activity;

public class VirtualServer extends Entity{
	protected VirtualServer() {}
	
	public static final native String getDescription() /*-{
		return this.description;
	}-*/;
	
	public final Date getCreated() {
		return SafeDate.parse(created());
	}
	protected final native String created() /*-{
		return this.created;
	}-*/;
	
	public final Date getEndDate() {
		return SafeDate.parse(endDate());
	}
	protected final native String endDate() /*-{
		return this.endDate;
	}-*/;
	
	public final native String getActivity() /*-{
		return this.activity;
	}-*/;
	
	/*public final String getPrice(){
		return price();
	}*/
	
	public final native String getPrice() /*-{
		return this.price;
	}-*/;
	

	public static final native VSCollection<VirtualServer> asCollection(String assumedSafe) /*-{
		return eval("("+assumedSafe+")");
		// return JSON.parse(assumedSafe);
	}-*/;
	
	public static final native VirtualServer asVirtualServer(String assumedSafe) /*-{
		return eval("("+assumedSafe+")");
		// return JSON.parse(assumedSafe);
	}-*/;

}

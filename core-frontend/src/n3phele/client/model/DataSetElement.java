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

public class DataSetElement extends FileNode {
	
	protected DataSetElement() {}

	/**
	 * @return the dataSetName
	 */
	public native final String getDataSetName() /*-{
		return this.dataSetName;
	}-*/;

	/**
	 * @return the description
	 */
	public native final String getDescription() /*-{
		return this.description;
	}-*/;


}
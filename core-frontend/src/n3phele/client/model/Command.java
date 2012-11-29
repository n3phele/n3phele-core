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
import n3phele.client.model.JsList;

import com.google.gwt.core.client.JavaScriptObject;


public class Command extends Entity {
	protected Command() {}
	
	/**
	 * @return the description
	 */
	public native final String getDescription() /*-{
		return this.description;
	}-*/;
	/**
	 * @return the version
	 */
	public native final String getVersion() /*-{
		return this.version;
	}-*/;
	
	/**
	 * @return the ownerName
	 */
	public native final String getOwnerName() /*-{
		return this.ownerName;
	}-*/;
	
	/**
	 * @return the preferred
	 */
	public native final boolean isPreferred() /*-{
		return this['preferred']==null?false:this['preferred']=="true";
	}-*/;
	
	/**
	 * @return the application
	 */
	public native final String getApplication() /*-{
		return this.application;
	}-*/;
	/**
	 * @return the outputFiles
	 */
	public final List<FileSpecification> getOutputFiles() {
		JavaScriptObject jsa = outputFiles();
		return JsList.asList(jsa);
	}
	public native final JavaScriptObject outputFiles() /*-{
		var array = [];
		if(this.outputFiles != undefined && this.outputFiles !=null) {
			if(this.outputFiles.length==undefined) {
				array[0] = this.outputFiles;
			} else {
				array = this.outputFiles;
			}
		}
		return array;
	}-*/;

	/**
	 * @return the outputParameters
	 */
	public final List<TypedParameter> getOutputParameters() {
		JavaScriptObject jsa = outputParameters();
		return JsList.asList(jsa);
	}
	public native final JavaScriptObject outputParameters() /*-{
		var array = [];
		if(this.outputParameters != undefined && this.outputParameters !=null) {
			if(this.outputParameters.length==undefined) {
				array[0] = this.outputParameters;
			} else {
				array = this.outputParameters;
			}
		}
		return array;
	}-*/;
	/**
	 * @return the inputFiles
	 */
	public final List<FileSpecification> getInputFiles() {
		JavaScriptObject jsa = inputFiles();
		return JsList.asList(jsa);
	}
	public native final JavaScriptObject inputFiles() /*-{
		var array = [];
		if(this.inputFiles != undefined && this.inputFiles !=null) {
			if(this.inputFiles.length==undefined) {
				array[0] = this.inputFiles;
			} else {
				array = this.inputFiles;
			}
		}
		return array;
	}-*/;
	/**
	 * @return the executionParameters
	 */
	public final List<TypedParameter> getExecutionParameters() {
		JavaScriptObject jsa = executionParameters();
		return JsList.asList(jsa);
	}
	public native final JavaScriptObject executionParameters() /*-{
		var array = [];
		if(this.executionParameters != undefined && this.executionParameters !=null) {
			if(this.executionParameters.length==undefined) {
				array[0] = this.executionParameters;
			} else {
				array = this.executionParameters;
			}
		}
		return array;
	}-*/;
	/**
	 * @return the cloudProfiles
	 */

	public final List<CloudProfile> getCloudProfiles() {
		JavaScriptObject jsa = cloudProfiles();
		return JsList.asList(jsa);
	}
	public native final JavaScriptObject cloudProfiles() /*-{
		var array = [];
		if(this.cloudProfiles != undefined && this.cloudProfiles !=null) {
			if(this.cloudProfiles.length==undefined) {
				array[0] = this.cloudProfiles;
			} else {
				array = this.cloudProfiles;
			}
		}
		return array;
	}-*/;
	
	public static final native Collection<Command> asCollection(String assumedSafe) /*-{
	return eval("("+assumedSafe+")");
	// return JSON.parse(assumedSafe);
	}-*/;
	public static final native Command asCommand(String assumedSafe) /*-{
	return eval("("+assumedSafe+")");
	// return JSON.parse(assumedSafe);
	}-*/;

}

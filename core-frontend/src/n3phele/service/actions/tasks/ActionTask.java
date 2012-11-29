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
package n3phele.service.actions.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import n3phele.service.model.ActionState;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.TypedParameter;


public interface ActionTask {

	public boolean call() throws Exception;
	public void cleanup() throws Exception;
	public void cancel();
	public void stop() throws Exception;
	
	
	/**
	 * Queries the job progress expressed as a integer between
	 * 0 (no progress) to 1000 (COMPLETE). Progress is expected to occur
	 * approximately linearly - such that a progress of 500 should occur when
	 * the activity is half way COMPLETE.
	 * @return
	 * 		progress 0 to 1000
	 */
	public int progress();	
	public long getDuration() ;

	public void setStart (Date start);
	public Date getStart();
	public void setComplete (Date complete);
	public Date getComplete();
	public ActionState getState();
	public void setState(ActionState actionState);
	public void setParent(URI parent);
	public URI getParent();
	public String getName();


	
	/**
	 * @return the inputFiles
	 */
	public ArrayList<FileRef> getInputFiles() ;

	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputFiles(ArrayList<FileRef> inputFiles) ;

	/**
	 * @return the executionParameters
	 */
	public ArrayList<TypedParameter> getExecutionParameters() ;

	/**
	 * @param executionParameters the executionParameters to set
	 */
	public void setExecutionParameters(ArrayList<TypedParameter> executionParameters) ;

	/**
	 * @return the outputFiles
	 */
	public ArrayList<FileRef> getOutputFiles() ;

	/**
	 * @param outputFiles the outputFiles to set
	 */
	public void setOutputFiles(ArrayList<FileRef> outputFiles) ;

	/**
	 * @return the outputParameters
	 */
	public ArrayList<TypedParameter> getOutputParameters() ;

	/**
	 * @param outputParameters the outputParameters to set
	 */
	public void setOutputParameters(ArrayList<TypedParameter> outputParameters);
	
	public void hasDependencyOn(URI actionTaskUri);
	public void dependencyFor(URI actionTaskUri);
	public URI getProgress();
	
}

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

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.ActionState;
import n3phele.service.model.NarrativeLevel;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.TypedParameter;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="JoinTask")
@XmlType(name="JoinTask", propOrder={})
@Unindexed
@Cached
public class JoinTask extends ActionTaskImpl implements ActionTask {
	private final static Logger log = Logger.getLogger(JoinTask.class.getName()); 
	
	@Id private Long id;
	protected ActionState finalState;
	protected NarrativeLevel narrativeLevel;
	
	public JoinTask() {};
	
	public JoinTask(String name, String message, String workloadKey,
			ArrayList<FileRef> inputFiles, ArrayList<TypedParameter> executionParameters,
			ArrayList<FileRef> outputFiles, ArrayList<TypedParameter> outputParameters) {
		super(name, message, workloadKey, inputFiles, executionParameters, outputFiles, outputParameters);
		finalState = ActionState.COMPLETE;
		narrativeLevel = NarrativeLevel.info;
	}
	

	@Override
	public boolean call() throws Exception {
		if(this.state == ActionState.INIT)
			return false;
		if(isAwaitingDependencies())
			return false;
		
		switch(state) {
		default:
		case INIT:
		case CANCELLED:
		case BLOCKED:
		case COMPLETE:
		case FAILED:
			return false;

		case PENDING:
			state = finalState;
			String message = getExecutionParameterValue("message");
			if(message == null || message.length() == 0)
				message = this.description;
			log.fine(this.name+" "+message);
			ActionLog.name(this).log(narrativeLevel, message);
			break;

		case RUNNING:
			this.updateOutputParameter("complete", "true");
			this.updateOutputParameter("exitCode", this.getExecutionParameterValue("exitCode"));
			this.updateOutputParameter("stdout", this.getExecutionParameterValue("stdout"));
			this.updateOutputParameter("stderr", this.getExecutionParameterValue("stderr"));
			state = ActionState.COMPLETE;
			break;
		}
		return true;
	}
	

	/* (non-Javadoc)
	 * @see n3phele.service.actions.tasks.ActionTask#cleanup()
	 */
	@Override
	public void cleanup() throws Exception {

		cancel();
		
	}

	@Override
	public void stop() throws Exception {
		cancel();
	}

	@Override
	public void cancel() {
	}
	
	@Override
	public int progress() {
		if(this.state == ActionState.CANCELLED ||
		   this.state == ActionState.COMPLETE ||
		   this.state == ActionState.FAILED)
			return 1000;
		return 0;
	}
	
	/**
	 * @return the duration
	 */
	public long getDuration() {
		return 1;
	}

	
	/*
	 * Getters and Setters
	 * -------------------
	 */
	
	/**
	 * @return the id
	 */
	@XmlTransient
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the finalState
	 */
	@XmlTransient
	public ActionState getFinalState() {
		return finalState;
	}

	/**
	 * @param finalState the finalState to set
	 */
	public void setFinalState(ActionState finalState) {
		this.finalState = finalState;
	}

	/**
	 * @return the narrativeLevel
	 */
	@XmlTransient
	public NarrativeLevel getNarrativeLevel() {
		return narrativeLevel;
	}

	/**
	 * @param narrativeLevel the narrativeLevel to set
	 */
	public void setNarrativeLevel(NarrativeLevel narrativeState) {
		this.narrativeLevel = narrativeState;
	}



	public final static TypedParameter[] defaultInputParameter = {
		new TypedParameter("stdout", "task execution stdout", ParameterType.List, "", ""),
		new TypedParameter("stderr", "task execution stderr", ParameterType.List, "", ""),
		new TypedParameter("exitCode", "operating system process exitcode", ParameterType.List, "", ""),
		new TypedParameter("dependency", "other dependency", ParameterType.List, "", "[]"),
	};
	
	public final static TypedParameter[] defaultOutputParameter = {
		new TypedParameter("complete", "All action completed", ParameterType.Boolean, "", ""),
		new TypedParameter("stdout", "task execution stdout", ParameterType.List, "", ""),
		new TypedParameter("stderr", "task execution stderr", ParameterType.List, "", ""),
		new TypedParameter("exitCode", "operating system process exitcode", ParameterType.List, "", ""),
	};

}

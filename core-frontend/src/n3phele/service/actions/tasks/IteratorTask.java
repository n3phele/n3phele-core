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
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.model.Action;
import n3phele.service.model.ActionState;
import n3phele.service.model.ActionType;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.rest.impl.ActionResource;
import n3phele.service.rest.impl.ActionResource.ActionManager;
import n3phele.service.rest.impl.ActivityResource;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="InteratorTask")
@XmlType(name="IteratorTask", propOrder={})
@Unindexed
@Cached
public class IteratorTask extends ActionTaskImpl implements ActionTask {
	private final static Logger log = Logger.getLogger(IteratorTask.class.getName()); 
	
	@Id private Long id;
	
	public IteratorTask() {};
	
	public IteratorTask(String name, String message, String workloadKey,
			ArrayList<FileRef> inputFiles, ArrayList<TypedParameter> executionParameters,
			ArrayList<FileRef> outputFiles, ArrayList<TypedParameter> outputParameters) {
		super(name, message, workloadKey, inputFiles, executionParameters, outputFiles, outputParameters);
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

			long n = Long.valueOf(getExecutionParameterValue("n"));	
			String iteratorVariable = this.getName();
			log.info("iterating "+iteratorVariable+" "+n+" times");
			List<Action> spawn = new ArrayList<Action>((int) n);
			ArrayList<URI> dependants = getDependencyFor();
			ActionManager actionManager = new ActionResource.ActionManager();
			for(URI u : dependants) {
				Action a = actionManager.load(u);
				if(a.getKind() != ActionType.fileXfer && a.getName().endsWith("-0")) {
					TypedParameter index = a.getInputParameter("i");
					if(index == null) {
						index = new TypedParameter("i", "instance number", ParameterType.Long, "", "0");
						ArrayList<TypedParameter> params = a.getActionTaskImpl().getExecutionParameters();
						if(params == null) a.getActionTaskImpl().setExecutionParameters(params = new ArrayList<TypedParameter>());
						params.add(index);	
					}
					if(index.getValue()== null || !index.getValue().equals("0")) {
						index.setValue("0");
						actionManager.update(a);
					}
					Action clone; 
					for(int i=1; i < n; i++) {
						clone = a.createReplicant(i);
						if(clone != null) {
							spawn.add(clone);
						}
					}
				}
			}
			Action moi = actionManager.load(getParentAction());
			moi.setActionTask(this);
			new ActivityResource().assimilate(moi, getParent(), spawn);
			state = ActionState.RUNNING;
			// no break;

		case RUNNING:
			for(TypedParameter p  : getExecutionParameters()) {
				updateOutputParameter(p.getName(), p.value());
			}
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



	public final static TypedParameter[] defaultInputParameter = {
		new TypedParameter("n", "Number of iterations", ParameterType.Long, "", "1"),
		new TypedParameter("agentUser", "username for authenticated access to the vm agent", ParameterType.Secret, "", ""),
		new TypedParameter("agentPassword", "password for authenticated access to the vm agent", ParameterType.Secret, "", ""),
		new TypedParameter("agentUrls", "url for access to the vm agent", ParameterType.List, "", "[]"),
		new TypedParameter("vmURIs", "List of URIs for VM(s) created", ParameterType.List, "", "[]"),
		new TypedParameter("publicIpAddressList", "list of public IP addresses of the created VM(s)", ParameterType.List, "", "[]"),
		new TypedParameter("privateIpAddressList", "private IP addresses of the created VMs", ParameterType.List, "", "[]"),
		new TypedParameter("dependency", "other dependency", ParameterType.List, "", "[]"),

	};
	
	public final static TypedParameter[] defaultOutputParameter = {
		new TypedParameter("complete", "Action completed", ParameterType.Boolean, "", ""),
		new TypedParameter("agentUser", "username for authenticated access to the vm agent", ParameterType.Secret, "", ""),
		new TypedParameter("agentPassword", "password for authenticated access to the vm agent", ParameterType.Secret, "", ""),
		new TypedParameter("agentUrls", "url for access to the vm agent", ParameterType.List, "", "[]"),
		new TypedParameter("vmURIs", "List of URIs for VM(s) created", ParameterType.List, "", "[]"),
		new TypedParameter("publicIpAddressList", "list of public IP addresses of the created VM(s)", ParameterType.List, "", "[]"),
		new TypedParameter("privateIpAddressList", "private IP addresses of the created VMs", ParameterType.List, "", "[]"),
	};

	

}

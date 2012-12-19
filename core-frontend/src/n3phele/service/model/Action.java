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
package n3phele.service.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import n3phele.service.actions.tasks.ActionTask;
import n3phele.service.actions.tasks.ActionTaskImpl;
import n3phele.service.actions.tasks.ActionURI;
import n3phele.service.actions.tasks.CreateVmTask;
import n3phele.service.actions.tasks.ExecuteCommandTask;
import n3phele.service.actions.tasks.FileTransferTask;
import n3phele.service.actions.tasks.IteratorTask;
import n3phele.service.actions.tasks.JoinTask;
import n3phele.service.actions.tasks.LogTask;
import n3phele.service.model.core.Entity;
import n3phele.service.model.core.FileRef;
import n3phele.service.model.core.ParameterType;
import n3phele.service.model.core.TypedParameter;
import n3phele.service.model.core.User;
import n3phele.service.rest.impl.Dao;

import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;
import com.sun.jersey.api.NotFoundException;

@XmlRootElement(name="Action")
@XmlType(name="Action", propOrder={"id", "kind", "actionTaskImpl", "finalized"})
@Unindexed
//@Cached
public class Action extends Entity  {
	final private static Logger log = Logger.getLogger(Action.class.getName());
	@Id private Long id;

	private ActionType kind = null;
	@XmlTransient
	@Embedded 
	private CreateVmTask cva = null;
	@XmlTransient
	@Embedded 
	private FileTransferTask ffa = null;
	@XmlTransient
	@Embedded 
	private ExecuteCommandTask eca = null;
	@XmlTransient
	@Embedded 
	private LogTask la = null;
	@XmlTransient
	@Embedded 
	private IteratorTask it = null;
	@XmlTransient
	@Embedded 
	private JoinTask jt = null;
	@Indexed private boolean finalized = false;


	private void storeActionTask(ActionTask actionTask) {
		this.kind = kind(actionTask);
		switch(kind) {
			case createVM:
				cva = (CreateVmTask) actionTask;
				break;
			case fileXfer:
				ffa = (FileTransferTask) actionTask;
				break;
			case shellExecution:
				eca = (ExecuteCommandTask) actionTask;
				break;
			case log:
				la = (LogTask) actionTask;
				break;
			case iterator:
				it = (IteratorTask) actionTask;
				break;
			case join:
				jt = (JoinTask) actionTask;
				break;
				default:
					throw new IllegalArgumentException("invalid action task");
		}
	}
	
	private ActionTask findActionTask() {
		ActionTask actionTask = null;
		switch(kind) {
		case createVM:
			actionTask = cva;
			break;
		case fileXfer:
			actionTask = ffa;
			break;
		case shellExecution:
			actionTask = eca;
			break;
		case log:
			actionTask = la;
			break;
		case iterator:
			actionTask = it;
			break;
		case join:
			actionTask = jt;
			break;
		default:
			break;
		}

		return actionTask;
	}
	
	public static ActionType kind(Object o) {
		if(o == null) return null;
		if(o instanceof CreateVmTask) {
			return ActionType.createVM;
		} else if (o instanceof FileTransferTask) {
			return ActionType.fileXfer;
		} else if (o instanceof ExecuteCommandTask) {
			return ActionType.shellExecution;
		} else if (o instanceof IteratorTask) {
			return ActionType.iterator;
		} else if (o instanceof JoinTask) {
			return ActionType.join;
		} else if (LogTask.class.isAssignableFrom(o.getClass())) {
			return ActionType.log;
		}
		throw new NotFoundException();
	}
	
	public Action() {
		super(null, null,"application/vnd.com.n3phele.Action+json", null, false);
	}
	
	public Action(String name, URI owner, boolean isPublic) {
		super(name, null,"application/vnd.com.n3phele.Action+json", owner, isPublic);
	}


	
	/**
	 * @param from
	 * @param to
	 * @param progress
	 * @param parent
	 * @param executionParameters
	 * @return
	 */
	public static Action newFileTransferTask(String name, String workloadKey, URI owner,
			FileRef from, FileRef to, URI progress, URI parent, ArrayList<TypedParameter> executionParameters) {
		ArrayList<TypedParameter>inParams = new ArrayList<TypedParameter>();
		inParams.addAll(Arrays.asList(FileTransferTask.defaultInputParameter));
		mergeParameters(inParams, executionParameters);
		
		ArrayList<TypedParameter>outParams = new ArrayList<TypedParameter>();
		outParams.addAll(Arrays.asList(FileTransferTask.defaultOutputParameter));

		FileTransferTask task = new FileTransferTask(name, workloadKey, from, to, progress, parent, inParams, outParams);
		Action result = new Action(name, owner, false);
		result.setActionTask(task);
		log.fine(String.format("Created file transfer %s for %s", name, owner));
		return result;
	}
	
	public TypedParameter getInputParameter(String name) {
		return getActionTaskImpl().getExecutionParameter(name);
	}
	
	public boolean setInputParameter(String name, String string) {
		return getActionTaskImpl().setExecutionParameter(name, string);
	}
	
	public TypedParameter getOutputParameter(String name) {
		return getActionTaskImpl().getOutputParameter(name);
	}
	
	public Action(Dao dao, ActionSpecification spec, Activity activity, URI progress) {
		super(spec.getName(),null,"application/vnd.com.n3phele.Action+json", activity.getOwner(), false);
		URI actionURI = spec.getAction();
		ArrayList<FileRef> inputFiles = new ArrayList<FileRef>();
		User owner = dao.user().get(activity.getOwner());
		if(spec.getInputFiles() != null) {
			for(FileSpecification s : spec.getInputFiles()) {
				FileRef f = s.toFileRef(dao, owner);
				inputFiles.add(f);
			}
		}
		ArrayList<FileRef> outputFiles = new ArrayList<FileRef>();
		if(spec.getOutputFiles() != null) {
			for(FileSpecification s : spec.getOutputFiles()) {
				FileRef f = s.toFileRef(dao, owner);
				outputFiles.add(f);
			}
		}
		if(actionURI.equals(ActionURI.createVM) || actionURI.equals(ActionURI.createvm)) {
			Account account = dao.account().load(activity.getAccount(), dao.user().get(activity.getOwner()));
			ArrayList<TypedParameter>inParams = new ArrayList<TypedParameter>();
			inParams.addAll(TypedParameter.cloneOf(CreateVmTask.defaultInputParameter));
			mergeParameters(inParams, spec.getInputParameters());
			
			ArrayList<TypedParameter>outParams = new ArrayList<TypedParameter>();
			outParams.addAll(TypedParameter.cloneOf(CreateVmTask.defaultOutputParameter));
			mergeParameters(outParams, spec.getOutputParameters());
			
			CreateVmTask task = new CreateVmTask(spec.getName(), spec.getDescription(), spec.getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			this.setActionTask(task);
			task.setParent(activity.getUri());
			task.setCloud(account.getCloud());
			task.setCloudCredential(account.getCredential());
			task.setAccountName(account.getName());
			// Added to put this information inside the creating of VirtualServers
			task.setAccountURI(account.getUri());
			task.setProgress(progress);
		} else if(actionURI.equals(ActionURI.executeCommand) || actionURI.equals(ActionURI.executecommand)) {
			ArrayList<TypedParameter>inParams = new ArrayList<TypedParameter>();
			inParams.addAll(TypedParameter.cloneOf(ExecuteCommandTask.defaultInputParameter));
			mergeParameters(inParams, spec.getInputParameters());
			
			ArrayList<TypedParameter>outParams = new ArrayList<TypedParameter>();
			outParams.addAll(TypedParameter.cloneOf(ExecuteCommandTask.defaultOutputParameter));
			mergeParameters(outParams, spec.getOutputParameters());
			
			ExecuteCommandTask task = new ExecuteCommandTask(spec.getName(), spec.getDescription(), spec.getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			this.setActionTask(task);
			task.setParent(activity.getUri());
			task.setProgress(progress);
		} else if(actionURI.equals(ActionURI.log)) {
			ArrayList<TypedParameter>inParams = new ArrayList<TypedParameter>();
			inParams.addAll(TypedParameter.cloneOf(ExecuteCommandTask.defaultInputParameter));
			mergeParameters(inParams, spec.getInputParameters());
			
			ArrayList<TypedParameter>outParams = new ArrayList<TypedParameter>();
			outParams.addAll(TypedParameter.cloneOf(ExecuteCommandTask.defaultOutputParameter));
			mergeParameters(inParams, spec.getOutputParameters());
			
			LogTask task = new LogTask(spec.getName(), spec.getDescription(), spec.getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			this.setActionTask(task);
			task.setParent(activity.getUri());
			task.setProgress(progress);
		} else if(actionURI.equals(ActionURI.iterator)|| actionURI.equals(ActionURI.oldIterator)) {
			ArrayList<TypedParameter>inParams = new ArrayList<TypedParameter>();
			inParams.addAll(TypedParameter.cloneOf(IteratorTask.defaultInputParameter));
			mergeParameters(inParams, spec.getInputParameters());
			
			ArrayList<TypedParameter>outParams = new ArrayList<TypedParameter>();
			outParams.addAll(TypedParameter.cloneOf(IteratorTask.defaultOutputParameter));
			mergeParameters(inParams, spec.getOutputParameters());
			
			IteratorTask task = new IteratorTask(spec.getName(), spec.getDescription(), spec.getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			this.setActionTask(task);
			task.setParent(activity.getUri());
			task.setProgress(progress);
		} else if(actionURI.equals(ActionURI.join)||actionURI.equals(ActionURI.oldJoin) ) {
			ArrayList<TypedParameter>inParams = new ArrayList<TypedParameter>();
			inParams.addAll(TypedParameter.cloneOf(JoinTask.defaultInputParameter));
			mergeParameters(inParams, spec.getInputParameters());
			
			ArrayList<TypedParameter>outParams = new ArrayList<TypedParameter>();
			outParams.addAll(TypedParameter.cloneOf(JoinTask.defaultOutputParameter));
			mergeParameters(inParams, spec.getOutputParameters());
			
			JoinTask task = new JoinTask(spec.getName(), spec.getDescription(), spec.getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			this.setActionTask(task);
			task.setParent(activity.getUri());
			task.setProgress(progress);
		}
		log.fine(String.format("Created action %s for %s", spec.toString(), activity.getOwner()));
	}
	
	private static void mergeParameters(List<TypedParameter> dest, List<TypedParameter> params) {
		Map<String, TypedParameter> map = new HashMap<String, TypedParameter>();
		if(params != null) {
			for(TypedParameter p : dest) {
				map.put(p.getName(), p);
			}
			for(TypedParameter p : params) {
				if(map.containsKey(p.getName())) {
					TypedParameter t = map.get(p.getName());
					t.setValue(p.value());
					if(p.getDefaultValue()!= null && p.getDefaultValue().length() > 0) {
						t.setDefaultValue(p.defaultValue());
					}
				} else {
					dest.add(p);
				}
			}
		}
	}
	
	public void hasDependencyOn(Action action) {
		this.getActionTask().hasDependencyOn(action.getUri());
		action.getActionTask().dependencyFor(this.getUri());
	}

	public Action createReplicant(int i) {
		URI activity = this.getActionTaskImpl().getParent();
		URI progress = this.getActionTaskImpl().getProgress();
		ArrayList<FileRef> inputFiles = this.getActionTaskImpl().getInputFiles();
		ArrayList<FileRef> outputFiles = this.getActionTaskImpl().getOutputFiles();
		ArrayList<TypedParameter> inParams = new ArrayList<TypedParameter>(this.getActionTaskImpl().getExecutionParameters().size());
		inParams.addAll(this.getActionTaskImpl().getExecutionParameters());
		ArrayList<TypedParameter> outParams = this.getActionTaskImpl().getOutputParameters();
		boolean foundI = false;
		for(int j=0; j < inParams.size(); j++) {
			TypedParameter oldI = inParams.get(j);
			if(oldI.getName().equals("i")) {
				TypedParameter myI = new TypedParameter(oldI.getName(), oldI.getDescription(), ParameterType.Long, Integer.toString(i), "");
				inParams.set(j, myI);
				foundI = true;
				break;
			}
		}
		if(!foundI) {
			inParams.add(new TypedParameter("i", "Instance index", ParameterType.Long, Integer.toString(i), ""));
		}
		Action replicant = null;
		String baseName = this.getName();
		if(baseName.endsWith("-0")) {
			baseName = baseName.substring(0, baseName.length()-2);
		} else {
			log.warning("Replicant action name "+this.getName()+" does not end with -0 .. skipping");
			return null;
		}
		switch (this.getKind()) {
		case createVM:
			replicant = new Action(baseName+"-"+i,this.getOwner(), false);
			CreateVmTask task = new CreateVmTask(baseName+"-"+i, this.getActionTaskImpl().getDescription(), this.getActionTaskImpl().getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			CreateVmTask me = (CreateVmTask) this.getActionTask();
			replicant.setActionTask(task);
			task.setParent(activity);
			task.setCloud(me.getCloud());
			task.setCloudCredential(me.getCloudCredential());
			task.setAccountName(me.getAccountName());
			task.setProgress(progress);
			break;
		case shellExecution:
			replicant = new Action(baseName+"-"+i,this.getOwner(), false);
			ExecuteCommandTask ecTask = new ExecuteCommandTask(baseName+"-"+i, this.getActionTaskImpl().getDescription(), this.getActionTaskImpl().getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			replicant.setActionTask(ecTask);
			ecTask.setParent(activity);
			ecTask.setProgress(progress);
			break;
		case log:
			replicant = new Action(baseName+"-"+i,this.getOwner(), false);
			LogTask lTask = new LogTask(baseName+"-"+i, this.getActionTaskImpl().getDescription(), this.getActionTaskImpl().getWorkloadKey(), inputFiles, inParams, 
					outputFiles, outParams);
			replicant.setActionTask(lTask);
			lTask.setParent(activity);
			lTask.setProgress(progress);
		case fileXfer:
		case join:
		case iterator:
			log.severe("Cannot support iterator of task "+name+" of type "+this.getKind());
			throw new IllegalArgumentException("Cannot support iterator of task "+name+" of type "+this.getKind());
			default:
		}

		log.fine(String.format("Created replicant %s for %s", replicant.toString(), owner));
		
		
		return replicant;
	}

	
	/**
	 * @return the id
	 */
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
	 * @return the actionTask
	 */
	@XmlTransient
	public ActionTask getActionTask() {
		return findActionTask();
	}
	
	/**
	 * @param actionTask the ActionTask to set
	 */
	public void setActionTask(ActionTask actionTask) {
		storeActionTask(actionTask);
	}
	
	public ActionTaskImpl getActionTaskImpl() {
		return (ActionTaskImpl) findActionTask();
	}
	
	/**
	 * @param actionTask the ActionTask to set
	 */
	public void setActionTaskImpl(ActionTaskImpl actionTask) {
		storeActionTask((ActionTask)actionTask);
	}

	/**
	 * @return the kind
	 */
	public ActionType getKind() {
		return kind;
	}

	/**
	 * @param kind the kind to set
	 */
	public void setKind(ActionType kind) {
		this.kind = kind;
	}

	/**
	 * @return the finalized
	 */
	public boolean isFinalized() {
		return finalized;
	}

	/**
	 * @param finalized the finalized to set
	 */
	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}


}
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
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

import n3phele.service.model.core.Entity;
@XmlRootElement(name="Progress")
@XmlType(name="Progress", propOrder={"command", "description", "started", "completed", "lastUpdate", "duration", "percentx10Complete", "status", "activity", "narratives"})
@Unindexed
@Cached
public class Progress extends Entity {
	@Id private Long id;
	@Embedded private ArrayList<Narrative> narratives;
	@Indexed
	private Date started;
	private Date completed;
	private Date lastUpdate;
	private long duration;
	private int percentx10Complete;		// percentComplete*10
	private ActionState status;
	private String activity;
	private String command;
	private String description;
		
	public Progress () {};
	
	public Progress(String name, String command, String description, URI activity, URI owner) {
		super(name, null, "factory/vnd.com.n3phele.Progress+json", owner, false);
		this.id = null;
		setActivity(activity);
		this.command = command;
		this.description = description;
	}
	
	@Override
	public String getName() { return super.getName(); }
	
	@Override
	public void setName(String name) { super.setName(name); }
	
	
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
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the narratives
	 */
	public ArrayList<Narrative> getNarratives() {
		if(this.narratives == null)
			this.narratives = new ArrayList<Narrative>();
		return narratives;
	}


	/**
	 * @param narratives the narratives to set
	 */
	public void setNarratives(ArrayList<Narrative> narratives) {
		this.narratives = narratives;
	}


	/**
	 * @return the timestamp of the activity start
	 */
	public Date getStarted() {
		return started;
	}


	/**
	 * @param started the timestamp of the activity start to set
	 */
	public void setStarted(Date started) {
		this.started = started;
	}


	/**
	 * @return the timestamp of the activity completion
	 */
	public Date getCompleted() {
		return completed;
	}


	/**
	 * @param completed the timestamp of the activity completion to set
	 */
	public void setCompleted(Date completed) {
		this.completed = completed;
	}


	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}


	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
		this.lastUpdate = Calendar.getInstance().getTime();
	}


	/**
	 * @return the percentx10Complete
	 */
	public int getPercentx10Complete() {
		return percentx10Complete;
	}


	/**
	 * @param percentx10Complete the percentx10Complete to set
	 */
	public void setPercentx10Complete(int percentx10Complete) {
		this.percentx10Complete = percentx10Complete;
		this.lastUpdate = Calendar.getInstance().getTime();
	}


	/**
	 * @return the status
	 */
	public ActionState getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(ActionState status) {
		this.status = status;
	}


	/**
	 * @return the activity
	 */
	public URI getActivity() {
		return (activity==null)?null:URI.create(activity);
	}


	/**
	 * @param activity the activity to set
	 */
	public void setActivity(URI activity) {
		this.activity = (activity==null)?null:activity.toString();
	}

	/**
	 * @return the lastUpdate
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public static Progress summary(Progress progress) {
		if(progress == null) return null;
		int len;
		if(progress.narratives!=null && (len = progress.narratives.size()) != 0)
			progress.narratives = new ArrayList<Narrative> (progress.narratives.subList(len-1, len));
		return progress;
	}

	
}

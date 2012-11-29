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
package n3phele.client.view;

import java.util.ArrayList;
import java.util.List;

import n3phele.client.N3phele;
import n3phele.client.model.Narrative;
import n3phele.client.model.Progress;
import n3phele.client.presenter.AbstractActivityProgressActivity;
import n3phele.client.presenter.helpers.AuthenticatedRequestFactory;
import n3phele.client.widgets.ActionDialogBox;
import n3phele.client.widgets.CancelButtonCell;
import n3phele.client.widgets.MenuItem;
import n3phele.client.widgets.WorkspaceVerticalPanel;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
public class ActivityListView extends WorkspaceVerticalPanel implements ActivityView {
	private static final int PAGESIZE = 15;
	private ActivityStatusList cellTable;
	private List<Progress> data = null;
	private AbstractActivityProgressActivity presenter;
	private ActionDialogBox<Progress> dialog = null;
	public ActivityListView() {
		super(new MenuItem(N3phele.n3pheleResource.activityIcon(), "Activity History", null));
		
		HorizontalPanel heading = new HorizontalPanel();
		heading.setWidth("500px");
		heading.setStyleName(N3phele.n3pheleResource.css().sectionPanelHeader());
		add(heading);
		
		heading.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
	    SimplePager simplePager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		heading.add(simplePager);
		heading.setCellHorizontalAlignment(simplePager, HorizontalPanel.ALIGN_CENTER);

		this.cellTable = new ActivityStatusList();
		this.cellTable.setWidth("100%");
		TextColumn<Progress> narrative = new TextColumn<Progress>(){

			@Override
			public String getValue(Progress progress) {
				String result = "";
				List<Narrative> narrative = progress.getNarratives();
				if(narrative != null && narrative.size() > 0) {
					result = narrative.get(narrative.size()-1).getText();
				}

				return result;
			}};
			this.cellTable.addColumn(narrative);
			this.cellTable.setColumnWidth(narrative, "55%");
			Column<Progress, Progress> cancelColumn = new Column<Progress, Progress>(
					 new CancelButtonCell<Progress>(new Delegate<Progress>() {

						@Override
						public void execute(Progress value) {
							if(value != null) {
								cellTable.getSelectionModel().setSelected(value, false);
								getDialog(value).show();
							}
						}}, "cancel activity")) {
				@Override
				public Progress getValue(Progress object) {
					String status = object.getStatus();
					if(status == null || status.equalsIgnoreCase("COMPLETE") || status.equalsIgnoreCase("FAILED") ||
							status.equalsIgnoreCase("CANCELLED")) {
							return null;	
					}
					return object;
				}
			};
			cellTable.addColumn(cancelColumn);
			cellTable.setColumnWidth(cancelColumn, "26px");
		//cellTable.setSize("455px", "");
		this.add(cellTable);
		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		final SingleSelectionModel<Progress> selectionModel = new SingleSelectionModel<Progress>();
	    cellTable.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	      public void onSelectionChange(SelectionChangeEvent event) {
	        Progress selected = selectionModel.getSelectedObject();
	        if (selected != null) {
	          if(presenter != null) {
	        	 presenter.onSelect(selected);
	        	// selectionModel.setSelected(selected, false);
	          }
	         
	        }
	      }
	    });
	    
	    /*
	     * Add Table paging
	     */
	    simplePager.setDisplay(cellTable);
		simplePager.setPageSize(PAGESIZE);
		cellTable.addRangeChangeHandler(new RangeChangeEvent.Handler(){

			/* (non-Javadoc)
			 * @see com.google.gwt.view.client.RangeChangeEvent.Handler#onRangeChange(com.google.gwt.view.client.RangeChangeEvent)
			 */
			@Override
			public void onRangeChange(RangeChangeEvent event) {
				Range range = cellTable.getVisibleRange();
				int start = range.getStart();

//				if(data == null || (data.size() < start) ){
					GWT.log("Fetch "+start);
					presenter.refresh(start);
//				} else {
//					if(length+start > data.size())
//						length = data.size()-start;
//					GWT.log("data available start="+start);
//					grid.setRowData(start, chunk(data.subList(start, start+length)));
//				}
			}
	    	
	    });
		this.add(cellTable);	

	    
	    
	    
	}
	
	/* (non-Javadoc)
	 * @see n3phele.client.view.ActivityView#setDisplayList(java.util.List)
	 */
	@Override
	public void setDisplayList(List<Progress> progressList, int start, int max) {
		if(progressList == null)
			progressList = new ArrayList<Progress>();
		this.cellTable.setRowCount(max, true);
		this.cellTable.setRowData(start, data=progressList);
		//N3phele.checkSize();
	}

	/* (non-Javadoc)
	 * @see n3phele.client.view.ActivityView#setPresenter(com.google.gwt.activity.shared.Activity)
	 */
	@Override
	public void setPresenter(AbstractActivityProgressActivity presenter) {
		this.presenter = presenter;
		
	}

	/* (non-Javadoc)
	 * @see n3phele.client.view.ActivityView#refresh(int, n3phele.client.model.Progress)
	 */
	@Override
	public void refresh(int i, Progress update) {
		this.cellTable.setRowData(i, data.subList(i, i+1));
	}
	
	protected ActionDialogBox<Progress> getDialog(Progress item) {
		if(dialog == null) {
			dialog = new ActionDialogBox<Progress>("Activity Terminate Confirmation",
					"No", "Yes", new Delegate<Progress>(){

						@Override
						public void execute(Progress object) {
							kill(object.getActivity());
							
						}});
			 dialog.setGlassEnabled(true);
			 dialog.setAnimationEnabled(true);

		}
		dialog.setValue("Terminate processing of running activity \""+item.getName()+"\"?", item);
		dialog.center();
		return dialog;
	}
	
	private void kill(String uri) {
		 String url = uri;
		 // Send request to server and catch any errors.
		    RequestBuilder builder = AuthenticatedRequestFactory.newRequest(RequestBuilder.DELETE, url);

		    try {
		      @SuppressWarnings("unused")
			Request request = builder.sendRequest(null, new RequestCallback() {
		        public void onError(Request request, Throwable exception) {
		        	Window.alert("Couldn't delete "+exception.getMessage());
		        }

		        public void onResponseReceived(Request request, Response response) {
		          if (204 == response.getStatusCode()) {
		          } else {
		        	  Window.alert("Couldn't delete (" + response.getStatusText() + ")");
		          }
		        }
		      });
		    } catch (RequestException e) {
		    	Window.alert("Couldn't delete "+e.getMessage());
		    
		    }
	}

	@Override
	public int getPageSize() {
		return PAGESIZE;
	}
}

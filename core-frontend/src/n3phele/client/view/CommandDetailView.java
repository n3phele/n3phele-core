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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import n3phele.client.N3phele;
import n3phele.client.model.CloudProfile;
import n3phele.client.model.Command;
import n3phele.client.model.FileNode;
import n3phele.client.model.FileSpecification;
import n3phele.client.model.Repository;
import n3phele.client.model.TypedParameter;
import n3phele.client.presenter.CommandActivity;
import n3phele.client.presenter.helpers.StyledTextCellRenderer;
import n3phele.client.widgets.FileNodeBrowser;
import n3phele.client.widgets.MenuItem;
import n3phele.client.widgets.SectionPanel;
import n3phele.client.widgets.SensitiveCheckBoxCell;
import n3phele.client.widgets.StyledButtonCell;
import n3phele.client.widgets.ValidInputIndicatorCell;
import n3phele.client.widgets.WorkspaceVerticalPanel;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.TextInputCell.ViewData;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.user.client.ui.Label;

public class CommandDetailView extends WorkspaceVerticalPanel {
	private Command data = null;
	private String commandNameText = "commandNameText";
	private String commandDescriptionText = "command description";
	private String commandVersionText = "1.0";
	final private FlexTable title;
	final private HTML commandDescription;
	final private HTML commandVersion;
	final private HTML commandName;
	final private SectionPanel parameters;
	final private SectionPanel inputs;
	final private SectionPanel outputs;
	final private SectionPanel clouds;
	final private CellTable<TypedParameter> paramTable;
	final private CellTable<FileSpecification> inputTable;
	final private CellTable<FileSpecification> outputTable;
	final private List<CloudProfile> profiles = new ArrayList<CloudProfile>();
	final private CellTable<CloudProfile> accountTable;
	final private List<Repository> repos = new ArrayList<Repository>();
//	final private Map<String, Repository> nameToRepoMap = new HashMap<String, Repository>();
	final private Map<String,Repository> uriToRepoMap = new HashMap<String, Repository>();
//	final private List<String> repoNames = new ArrayList<String>();
	final private Column<FileSpecification, FileSpecification> nullRepoColumnIn;
	final private Column<FileSpecification,FileSpecification> nullRepoColumnOut;
	private Column<FileSpecification,FileSpecification> inputRepoColumn;
	private Column<FileSpecification,FileSpecification> outputRepoColumn;
	private Long selectedProfile = null;
	final private Button cancel;
	final private Button run;
	private CommandActivity presenter = null;
	private final CheckBox sendEmail;
	private HorizontalPanel buttons;
	protected TextBox jobName;
	private TextInputCell parameterTextInputCell;
	private CellWidget<String> gotExecutionSelection;
	private CellWidget<String> errorsOnPage;
	private String selectedAccountURI = null;
	private DisappearingCheckBoxCell parameterCheckboxCell;
	private String lastRepo = null;
	private String lastPath = "";

	public CommandDetailView() {
		this(N3phele.n3pheleResource.commandIcon(), "Command", "run", "cancel");
	}
	
	
	public CommandDetailView(ImageResource icon, String heading, String runButtonText, String cancelButtonText) {
		super(new MenuItem(icon, heading, null));
		title = new FlexTable();
		title.setCellSpacing(8);
		title.setWidth("100%");
		commandName = new InlineHTML(commandDescriptionText);
		title.setWidget(0, 0, commandName);
		commandName.addStyleName(N3phele.n3pheleResource.css().commandDetailHeader());
		commandDescription = new InlineHTML(commandDescriptionText);
		commandDescription.addStyleName(N3phele.n3pheleResource.css().commandDetailText());
		title.setWidget(0, 1, commandDescription);
		commandVersion = new InlineHTML(commandVersionText);
		title.setWidget(1, 0, commandVersion);
		title.getCellFormatter().setStylePrimaryName(1, 0, N3phele.n3pheleResource.css().commandDetailText());
		title.getFlexCellFormatter().setRowSpan(0, 1, 2);

		this.add(title);
		
		/*
		 * Setup parameters section of the view
		 */
		
		parameters = new SectionPanel("Parameters");
		this.paramTable = createParameterTable();
		parameters.add(paramTable);
		this.add(parameters);
		
		/*
		 * Setup input and output files sections of the view
		 */
		
		nullRepoColumnIn = new Column<FileSpecification,FileSpecification>(createRepoRefCompositeCell(true)) {
			@Override
			public FileSpecification getValue(FileSpecification fs) {
				return fs;
			}
		};

		nullRepoColumnOut = new Column<FileSpecification,FileSpecification>(createRepoRefCompositeCell(false)) {
			@Override
			public FileSpecification getValue(FileSpecification fs) {
				return fs;
			}
		};

		inputs = new SectionPanel("Input Files");
		inputTable = createFileTable(inputRepoColumn = nullRepoColumnIn);
		inputs.add(inputTable);
		this.add(inputs);
		outputs = new SectionPanel("Output Files");
		outputTable = createFileTable(outputRepoColumn = nullRepoColumnOut);
		outputs.add(outputTable);
		this.add(outputs);	
		
		
		clouds = new SectionPanel("Execute On");
		HorizontalPanel divider = new HorizontalPanel();
		divider.setWidth("100%");
		clouds.add(divider);
		gotExecutionSelection = new CellWidget<String>(new ValidInputIndicatorCell(N3phele.n3pheleResource.inputErrorIcon()),"+Execution target selection required");
		divider.add(gotExecutionSelection);
		divider.setCellVerticalAlignment(gotExecutionSelection, HorizontalPanel.ALIGN_MIDDLE);
		divider.setCellWidth(gotExecutionSelection, "20px");
		accountTable = createAccountTable();
		divider.add(accountTable);
		this.sendEmail = new CheckBox("Send you an email on completion");
		sendEmail.setWordWrap(true);
		divider.add(sendEmail);
		divider.setCellVerticalAlignment(sendEmail, HorizontalPanel.ALIGN_MIDDLE);
		divider.setCellHorizontalAlignment(sendEmail, HorizontalPanel.ALIGN_CENTER);
		divider.setCellWidth(sendEmail, "20%");
		this.add(clouds);	
		
		buttons = new HorizontalPanel();
		buttons.setWidth("90%");
		buttons.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		Label jobLabel = new Label("with job name ");
		buttons.add(jobLabel);
		jobName = new TextBox();
		buttons.add(jobName);
		Label fill = new Label("");
		buttons.add(fill);
		buttons.setCellWidth(fill, "10%");
		
		run = new Button(runButtonText);
		run.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doRun();
			}
		});
		run.setEnabled(false);
		buttons.add(run);
		errorsOnPage = new CellWidget<String>(new ValidInputIndicatorCell(N3phele.n3pheleResource.inputErrorIcon()),"+check for missing or invalid parameters marked with this icon");
		buttons.add(errorsOnPage);
		buttons.setCellWidth(errorsOnPage, "20px");
		
		cancel = new Button(cancelButtonText);
		cancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doCancel();
			}
		});
		buttons.add(cancel);
		
		this.add(buttons);
	
	}
	
	private void buttonVisibility(boolean on) {
		buttons.setVisible(on);
		clouds.setVisible(on);
	}
	
	public void updateRunButton(boolean allValid) {
		
		if(allValid && data != null) {
			allValid = checkParameterValues(data.getExecutionParameters());
		}
		if(allValid && data.getInputFiles() != null && data.getInputFiles().size() > 0) {
//			if(nameToRepoMap.isEmpty()) {
//				allValid = false;
//			} else {
				allValid = validateRepoRefs(data.getInputFiles(), true);
//			}
		}
		if(allValid && data.getOutputFiles() != null && data.getOutputFiles().size() > 0) {
			allValid = validateRepoRefs(data.getOutputFiles(), false);
		}
		if(allValid)
			allValid = getSelectedAccount() != null;
			
		this.run.setEnabled(allValid);
		this.errorsOnPage.setValue((allValid?"-":"+")+this.errorsOnPage.getValue().substring(1));
	}
	
	public boolean checkParameterValues(List<TypedParameter> paramList) {
		for(TypedParameter p : paramList) {
			if(!checkParameterValue(p)) return false;
		}
		return true;
	}
	
	
	public Map<String,String> getParameterValuesAndClear(List<TypedParameter> paramList) {
		Map<String, String> result = new HashMap<String, String>();
		if(this.parameterTextInputCell != null) {
			for(TypedParameter p : paramList) {
				String name = p.getName();
				if(p.getType().equalsIgnoreCase("boolean")) {
					Boolean vd = this.parameterCheckboxCell.getViewData(name);
					if(vd != null) {
						result.put(name, vd.toString());
						this.parameterCheckboxCell.clearViewData(name);
					} else {
						String value = p.getValue();
						if(value == null || value.length()==0) {
							value = p.getDefaultValue();
						}
						result.put(name, value);
					}
					
				} else {
					ViewData vd = this.parameterTextInputCell.getViewData(name);
					if(vd != null) {
						result.put(name, vd.getCurrentValue());
						this.parameterTextInputCell.clearViewData(name);
					} else {
						String value = p.getValue();
						if(value == null || value.length()==0) {
							value = p.getDefaultValue();
						}
						result.put(name, value);
					}
				}
			}
		}
		return result;
	}
	
	public boolean validateRepoRefs (List<FileSpecification> fileSpecs, boolean isInput) {
		for(FileSpecification p : fileSpecs) {
			String repoURI = p.getRepository();
			String repoFile = p.getFilename();
			boolean isOptional = p.isOptional();
			if(!validateRepo(repoURI, repoFile, isInput && !isOptional)) return false;
		}
		return true;
	}
	
	public List<FileSpecification> getRepoRefs(List<FileSpecification> fileSpecs) {
	List<FileSpecification> result = new ArrayList<FileSpecification>();


	for(FileSpecification p : fileSpecs) {
		String repoURI = p.getRepository();
		String repoFile = p.getFilename();
		if(repoURI != null) {
			GWT.log("File "+p.getName()+" maps to "+repoURI+"("+uriToRepoMap.get(repoURI)+")"+" path "+repoFile);
			if(repoFile!=null)
				repoFile = repoFile.trim();
			p.setFilename(repoFile);
			result.add(p);
		} else {
			GWT.log("File "+p.getName()+" not used");
		}
	}
	return result;
}
	
	private boolean getSendEmail() {
		return this.sendEmail.getValue();
	}
	
	private String getSelectedAccount() {
		return CommandDetailView.this.selectedAccountURI;
	}
	
	private long getSelectedProfileId() {
		return this.selectedProfile;
	}
	/**
	 * @param cloudProfileId
	 */
	public void setSelectedProfile(long cloudProfileId, String accountURI) {
		CommandDetailView.this.selectedProfile = cloudProfileId;
		CommandDetailView.this.selectedAccountURI = accountURI;
		if(this.profiles != null && this.profiles.size() > 0) {
			for(CloudProfile ep : this.profiles) {
				if(cloudProfileId == ep.getId() && ep.getAccountUri().equals(accountURI)) {
						this.accountTable.redraw();
						String visible = "-";

						CommandDetailView.this.gotExecutionSelection.setValue(visible+CommandDetailView.this.gotExecutionSelection.getValue().substring(1));
						updateRunButton(true);
						return;
				}
			}
		}
		CommandDetailView.this.gotExecutionSelection.setValue("+"+CommandDetailView.this.gotExecutionSelection.getValue().substring(1));
	}
	
	private void doCancel() {
		if(presenter != null) {
			if(data != null) getParameterValuesAndClear(data.getExecutionParameters());
			this.presenter.goToPrevious();
		}
	}
	
	private void doRun() {
		if(presenter != null) {
			this.run.setEnabled(false);
			this.presenter.run(data, jobName.getText(), getParameterValuesAndClear(data.getExecutionParameters()),
					getRepoRefs(data.getInputFiles()), getRepoRefs(data.getOutputFiles()), getSendEmail(), getSelectedAccount(), getSelectedProfileId());
		}
	}
	
	public void setCommandName(String name) {
		this.commandNameText = name;
		commandName.setHTML(commandNameText);
	}
	
	public void setCommandDescription(String description) {
		this.commandDescriptionText = description;
		commandDescription.setHTML(commandDescriptionText);
	}
	
	public void setCommandVersion(String version) {
		this.commandVersionText = version;
		commandVersion.setHTML("Version: "+commandVersionText);
	}

	public void setData(Command command) {
		this.data = command;
		if(command != null) {
			setCommandName(command.getName());
			setCommandDescription(command.getDescription());
			setCommandVersion(command.getVersion());
			setParameters(command.getExecutionParameters());
			setInputFiles(command.getInputFiles());
			setOutputFiles(command.getOutputFiles());
			setProfiles(command.getCloudProfiles());
			updateRunButton(true);
			buttonVisibility(true);
		} else {
			setCommandName("");
			setCommandDescription("");
			setCommandVersion("");
			setParameters(nullParams);
			setInputFiles(nullFiles);
			setOutputFiles(nullFiles);
			setProfiles(nullProfiles);
			setRepositories(nullRepos);
			jobName.setText("");
			sendEmail.setValue(false);
			updateRunButton(false);
			buttonVisibility(false);
		}
	}

	final private static List<TypedParameter> nullParams = new ArrayList<TypedParameter>(0);
	final private static List<FileSpecification> nullFiles = new ArrayList<FileSpecification>(0);
	final private static List<CloudProfile> nullProfiles = new ArrayList<CloudProfile>(0);
	final private static List<Repository> nullRepos = new ArrayList<Repository>(0);

	public void setParameters(List<TypedParameter> params) {
		paramTable.setRowCount(params.size());
		paramTable.setRowData(params);
		parameters.setVisible(params.size() > 0);
	}
	
	public void setInputFiles(List<FileSpecification> fileList) {
		inputTable.setRowCount(fileList.size());
		inputTable.setRowData(fileList);
		inputs.setVisible(fileList.size() > 0);
	}
	
	public void setOutputFiles(List<FileSpecification> fileList) {
		outputTable.setRowCount(fileList.size());
		outputTable.setRowData(fileList);
		outputs.setVisible(fileList.size() > 0);
	}
	
	public void setProfiles(List<CloudProfile> profiles) {
		this.profiles.clear();
		this.profiles.addAll(profiles);
		accountTable.setRowCount(this.profiles.size());
		accountTable.setRowData(this.profiles);
		accountTable.setVisible(this.profiles.size() > 0);
		
		if(CommandDetailView.this.selectedProfile!=null) {
			Long profileId = CommandDetailView.this.selectedProfile;
			String accountURI = CommandDetailView.this.selectedAccountURI;
			setSelectedProfile(profileId, accountURI);
		} else {
			String visible = "+";
			CommandDetailView.this.gotExecutionSelection.setValue(visible+CommandDetailView.this.gotExecutionSelection.getValue().substring(1));
		}
	}
	
	public void setRepositories(List<Repository>repos) {
		this.repos.clear();
		this.repos.addAll(repos);
//		this.repoNames.clear();
//		this.repoNames.add("none");
//		this.nameToRepoMap.clear();
		this.uriToRepoMap.clear();
		for(Repository r : repos) {
//			nameToRepoMap.put(r.getName(), r);
			uriToRepoMap.put(r.getUri(), r);
//			repoNames.add(r.getName());
		}
		if(repos.size() > 0) {
			inputRepoColumn = replaceSelectionRepoColumn(inputTable, inputRepoColumn, true);
			outputRepoColumn = replaceSelectionRepoColumn(outputTable, outputRepoColumn, false);
		} else {
			inputRepoColumn = replaceSelectionRepoColumn(inputTable, nullRepoColumnIn, inputRepoColumn);
			outputRepoColumn = replaceSelectionRepoColumn(outputTable, nullRepoColumnOut, outputRepoColumn);
		}
		updateRunButton(repos.size()>0);
	}

	public void setPresenter(CommandActivity presenter) {
		this.presenter = presenter;
		this.selectedProfile = null;
		if(presenter == null) {
			this.setData(null);
		}
	}

	public void refresh(Command command) {
		setData(command);
	}
	
	public void setJobName(String jobName) {
		this.jobName.setText(jobName);
	}
	
	/*
	 * -------------------------------------------------------------------------
	 * 								Panel constructors
	 * -------------------------------------------------------------------------
	 */
	
	
	
	
	/*
	 * =============== Parameter table =============== 
	 */
	
	

	public CellTable<TypedParameter> createParameterTable() {
		final ProvidesKey<TypedParameter> KEY_PROVIDER = new ProvidesKey<TypedParameter>() {
		    public Object getKey(TypedParameter item) {
		      return item.getName();
		    }
		  };
		final CellTable<TypedParameter> table;
		table = new CellTable<TypedParameter>(KEY_PROVIDER);
		table.setWidth("100%");
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		TextColumn<TypedParameter> descriptionColumn = new TextColumn<TypedParameter>() {
			@Override
			public String getValue(TypedParameter command) {
				String result = "";
				if(command != null)
					return command.getDescription();
				return result;
			}
		};
		table.addColumn(descriptionColumn);

		this.parameterTextInputCell = new DisappearingTextInputCell();
		this.parameterCheckboxCell = new DisappearingCheckBoxCell();
		final ValidInputIndicatorCell valid = new ValidInputIndicatorCell(N3phele.n3pheleResource.inputErrorIcon());
	
		HasCell<TypedParameter,?> valueHasCell = new HasCell<TypedParameter,String>() {

			@Override
			public Cell<String> getCell() {
				return CommandDetailView.this.parameterTextInputCell;
			}

			@Override
			public FieldUpdater<TypedParameter, String> getFieldUpdater() {
				return 
				new FieldUpdater<TypedParameter, String>() { // Needs to exist so that the cell level updater is invoked
					@Override
					public void update(int index, TypedParameter object, String value) {
					}
					
				};
			}

			@Override
			public String getValue(TypedParameter object) {
				if(object != null) {
					if(!object.getType().equalsIgnoreCase("boolean")) {
						String result = object.getValue();
						if(isNullOrBlank(result))
							result = object.getDefaultValue();
						if(result==null)
							result = "";
						return result;
					}
				}
				return null;
			}
			
		};
		HasCell<TypedParameter,?> validHasCell = new HasCell<TypedParameter,String>() {

			@Override
			public Cell<String> getCell() {
				return valid;
			}

			@Override
			public FieldUpdater<TypedParameter, String> getFieldUpdater() {
				return null;
			}

			@Override
			public String getValue(TypedParameter object) {
				String errTooltip;
				errTooltip = errorMessageMap().get(object.getType());
				if(checkParameterValue(object)) {
					return "-"+errTooltip;
				} else {
					return "+"+errTooltip;
				}
			}
			
		};
		
		HasCell<TypedParameter,?> booleanHasCell = new HasCell<TypedParameter,Boolean>() {

			@Override
			public Cell<Boolean> getCell() {
				return parameterCheckboxCell;
			}

			@Override
			public FieldUpdater<TypedParameter, Boolean> getFieldUpdater() {
				return 
				new FieldUpdater<TypedParameter, Boolean>() { // Needs to exist so that the cell level updater is invoked
					@Override
					public void update(int index, TypedParameter object, Boolean value) {
					}
					
				};
			}

			@Override
			public Boolean getValue(TypedParameter object) {
				if(object != null) {
					if(object.getType().equalsIgnoreCase("boolean")) {
						String result = object.getValue();
						if(isNullOrBlank(result))
							result = object.getDefaultValue();
						return Boolean.valueOf(result);
					}
				}
				return null;
			}

			
		};
		List<HasCell<TypedParameter,?>> arg = new ArrayList<HasCell<TypedParameter,?>>(2);
		arg.add(validHasCell);
		arg.add(valueHasCell);
		arg.add(booleanHasCell);


		Column<TypedParameter,  TypedParameter> valueColumn = new Column<TypedParameter,  TypedParameter>(new TypedPropertyComposite(arg)) {
			@Override
			public TypedParameter getValue(TypedParameter parameter) {
				return parameter;
			}
		};
		valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		table.addColumn(valueColumn);
		table.setWidth("100%", true);
		table.setColumnWidth(valueColumn, "210px");
	    valueColumn.setFieldUpdater(new FieldUpdater<TypedParameter, TypedParameter>() {
	      public void update(int index, final TypedParameter object, final TypedParameter value) {
	    	  GWT.runAsync(new RunAsyncCallback() {
					
					@Override
					public void onSuccess() {
						updateRunButton(true);
					}
					
					@Override
					public void onFailure(Throwable reason) {
						//
					}
				});
	    	  
	      }
	    });
		return table;
	}
	
	private Map<String,String> errMap = null;
	private Map<String, String> errorMessageMap() {
		if(errMap == null) {
			errMap = new HashMap<String,String>();
			errMap.put("String","Text string required");
			errMap.put("Long","Valid integer number required");
			errMap.put("Double","Valid floating point value required");
			errMap.put("Secret","Password text string required");
			errMap.put("List","List required");
			errMap.put("Boolean","true or false required");
		}
		return errMap;
	}
	
	private class TypedPropertyComposite extends CompositeCell<TypedParameter> {

		final private TextInputCell text;
		final private CheckboxCell checkbox;
		public TypedPropertyComposite(List<HasCell<TypedParameter, ?>> hasCells) {
			super(hasCells);
			assert(hasCells.size() == 3);
			assert(hasCells.get(1).getCell() instanceof DisappearingTextInputCell);
			assert(hasCells.get(0).getCell() instanceof ValidInputIndicatorCell);
			assert(hasCells.get(2).getCell() instanceof DisappearingCheckBoxCell);
			text = (TextInputCell) hasCells.get(1).getCell();
			checkbox = (CheckboxCell) hasCells.get(2).getCell();
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.cell.client.CompositeCell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, com.google.gwt.dom.client.Element, java.lang.Object, com.google.gwt.dom.client.NativeEvent, com.google.gwt.cell.client.ValueUpdater)
		 */
		@Override
		public void onBrowserEvent(Context context, Element parent,
				TypedParameter value, NativeEvent event,
				ValueUpdater<TypedParameter> valueUpdater) {
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element container = getContainerElement(parent);
				Element wrapper = container.getFirstChildElement();
				String prior = null;
				ViewData vd = text.getViewData(context.getKey());
				if(vd != null) {
					prior = vd.getCurrentValue();
				} else {
					Boolean cb = checkbox.getViewData(context.getKey());
					GWT.log("Checkbox is "+cb);
					if(cb != null)
						prior = cb.toString();
				}
				super.onBrowserEvent(context, parent, value, event, valueUpdater);
				String newValue = null;
				vd = text.getViewData(context.getKey());
				if(vd != null) {
					newValue = vd.getCurrentValue();
				} else {
					Boolean cb = checkbox.getViewData(context.getKey());
					GWT.log("Post event Checkbox is "+cb);
					if(cb != null)
						prior = cb.toString();
					
				}
				if(newValue != prior) {
					final boolean visible = !validateTypedParameter(value, newValue);
					UIObject.setVisible(wrapper.getFirstChildElement(), visible);
					GWT.runAsync(new RunAsyncCallback() {
						@Override
						public void onSuccess() {
							updateRunButton(!visible);
						}
						@Override
						public void onFailure(Throwable reason) {
							//
						}
					});
				}
			}
		}
	}
	
	
	private static boolean validateTypedParameter(TypedParameter object, String value) {
		boolean error = false;
		String type = object.getType();
  	  if(type.equals("Long")) {
  		  try {
  			  Long.valueOf(value.trim());
  		  } catch (Exception e) {
  			  error = true;
  		  }
  	  } else if(type.equals("Double")) {
  		  try {
  			  Double.valueOf(value);
  		  } catch (Exception e) {
  			  error = true;
  		  }
  		  
  	  } else if(type.equals("Boolean")) {
  		  try {
  			  Boolean.valueOf(value);
  		  } catch (Exception e) {
  			  error = true;
  		  }
  		  
  	  } else  {
  		  error = value == null || value.trim().length()==0;
  	  }
  	  return !error;
	}
	
	public boolean checkParameterValue(TypedParameter p) {
		if(this.parameterTextInputCell == null) return false;
		String name = p.getName();
		String value;
		if(p.getType().equalsIgnoreCase("boolean")) {
			Boolean vd = this.parameterCheckboxCell.getViewData(name);
			if(vd != null) {
				value = vd.toString();
			} else {
				value = p.getValue();
				if(value == null || value.length()==0) {
					value = p.getDefaultValue();
				}
			}
		} else {
			ViewData vd = this.parameterTextInputCell.getViewData(name);
			if(vd != null) {
				value = vd.getCurrentValue();
			} else {
				value = p.getValue();
				if(value == null || value.length()==0) {
					value = p.getDefaultValue();
				}
			}
		}
		
		return validateTypedParameter(p, value);

	}
	
	
	/*
	 * =============== File table =============== 
	 */
	
	public CellTable<FileSpecification> createFileTable(Column<FileSpecification, FileSpecification> repoColumn) {
		final ProvidesKey<FileSpecification> KEY_PROVIDER = new ProvidesKey<FileSpecification>() {

		    public Object getKey(FileSpecification item) {
		      return item.getName();
		    }
		  };
		final CellTable<FileSpecification> table;
		table = new CellTable<FileSpecification>(KEY_PROVIDER);
		table.setWidth("100%", true);
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		TextColumn<FileSpecification> descriptionColumn = new TextColumn<FileSpecification>() {
			@Override
			public String getValue(FileSpecification file) {
				String result = "";
				if(file != null)
					return file.getDescription();
				return result;
			}
		};
		table.addColumn(descriptionColumn);
	
		
		table.addColumn(repoColumn);
		repoColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

	
		return table;
	}

	private boolean validateRepo(String repoName, String pathValue,
			boolean isMandatory) {
		if(pathValue != null) { 
			pathValue = pathValue.trim();
			if(pathValue.length()==0) pathValue = null;
		}
		
		if(isMandatory) {
			return pathValue != null;
		} else {
			if(repoName == null || repoName.equalsIgnoreCase("none")) {
				
				return pathValue == null;
			} else {
				return pathValue != null;
			}
		}
	}
	
	private static class BrowseActionCell extends StyledButtonCell<FileSpecification> {

		public BrowseActionCell(Delegate<FileSpecification> delegate, String tip, String label) {
			super(delegate, N3phele.n3pheleResource.css().browseButton(), tip);
			this.setText(label);
		}
	}
	
	public static class FilenamePathRenderer extends StyledTextCellRenderer <String> {

		private static FilenamePathRenderer instance;

		public static FilenamePathRenderer getInstance() {
			if (instance == null) {
				instance = new FilenamePathRenderer(N3phele.n3pheleResource.css().filePathText());
			}
			return instance;
		}
		public FilenamePathRenderer(String style) {
			super(style);
		}

		public String getValue(String object) {
			return object;
		}
	}
	
	private CompositeCell<FileSpecification> createRepoRefCompositeCell(final boolean isInput) {
		final RepoIconTextCell repo = new RepoIconTextCell(N3phele.n3pheleResource.repositorySmallIcon(), new TextCell(RepoTextCellRenderer.getInstance()));
		final TextCell path = new TextCell(FilenamePathRenderer.getInstance());
		final ValidInputIndicatorCell valid = new ValidInputIndicatorCell(N3phele.n3pheleResource.inputErrorIcon());
		final BrowseActionCell browse = new BrowseActionCell(new Delegate<FileSpecification>(){

			@Override
			public void execute(final FileSpecification fileSpecification) {
				final PopupPanel popup = new PopupPanel(true);
				FileNodeBrowser.BrowserPresenter presenter;
				FileNodeBrowser view = new FileNodeBrowser(presenter = new FileNodeBrowser.BrowserPresenter() {
					FileNodeBrowser view;
					@Override
					public void setView(FileNodeBrowser view) {
						this.view = view;
					}
					/* (non-Javadoc)
					 * @see n3phele.client.widgets.FileNodeBrowser.BrowserPresenter#select(n3phele.client.model.FileNode)
					 */
					@Override
					public void selectFolder(FileNode value) {
						if(!value.getMime().equals("application/vnd.com.n3phele.Repository+json")) {
							String path = value.getPath()==null?value.getName()+"/":(value.getPath()+value.getName()+"/");
							if(path.startsWith("/"))
								GWT.log("FileNode "+value+" produces query with leading /");
							CommandDetailView.this.presenter.fetchFiles(view, 
								value.getRepository(), path);
						} else {
							CommandDetailView.this.presenter.fetchFiles(view, 
									value.getRepository(), null);
						}
						
					}
					
					/* (non-Javadoc)
					 * @see n3phele.client.widgets.FileNodeBrowser.BrowserPresenter#select(n3phele.client.model.FileNode)
					 */
					@Override
					public void selectFolder(Repository repo) {
						if(repo != null) {
							CommandDetailView.this.presenter.fetchFiles(view, 
									repo.getUri(), null);
						} else {
							view.show(new ArrayList<FileNode>(0), new ArrayList<FileNode>(0));
						}
						
					}

					/* (non-Javadoc)
					 * @see n3phele.client.widgets.FileNodeBrowser.BrowserPresenter#hide()
					 */
					@Override
					public void hide() {
						popup.hide();
					}

					/* (non-Javadoc)
					 * @see n3phele.client.widgets.FileNodeBrowser.BrowserPresenter#save(java.lang.String)
					 */
					@Override
					public void save(String repoURI, String filename) {
						fileSpecification.setFilename(filename);
						fileSpecification.setRepository(repoURI);
						if(isInput)
							inputTable.redraw();
						else
							outputTable.redraw();
						updateRunButton(true);
						CommandDetailView.this.lastRepo = repoURI;
						CommandDetailView.this.lastPath = getPath(filename);						
						popup.hide();
					}

					/* (non-Javadoc)
					 * @see n3phele.client.widgets.FileNodeBrowser.BrowserPresenter#validate(java.lang.String)
					 */
					@Override
					public boolean validate(String repoURI, String filename) {
						if(!isInput) {
							return repoURI==null || (repoURI != null && filename!=null && filename.trim().length()!=0);
						}
						
						CommandDetailView.this.presenter.checkExists(view, repoURI, filename);
						return false;
					}
					@Override
					public void addPlaceholder(FileNode folder) {
						if(CommandDetailView.this.presenter != null) {
							CommandDetailView.this.presenter.addPlaceholder(view, folder);
						}
					}
					
				}, isInput);
				presenter.setView(view);
				view.setRepos(CommandDetailView.this.repos, !isInput || fileSpecification.isOptional());
				if((CommandDetailView.this.repos != null && CommandDetailView.this.repos.size() > 0) && (isInput || !isNullOrBlank(fileSpecification.getRepository()))) {
					if(isNullOrBlank(fileSpecification.getRepository())) {
						if(CommandDetailView.this.lastRepo != null) {
							CommandDetailView.this.presenter.fetchFiles(view, 
									CommandDetailView.this.lastRepo,
									CommandDetailView.this.lastPath);
						} else {
							CommandDetailView.this.presenter.fetchFiles(view, 
									CommandDetailView.this.repos.get(0).getUri(),
										getPath(fileSpecification.getFilename()));
						}
					} else {
						CommandDetailView.this.presenter.fetchFiles(view, 
									fileSpecification.getRepository(),
									getPath(fileSpecification.getFilename()));
					}
				} else {
					if(CommandDetailView.this.lastRepo != null) {
						CommandDetailView.this.presenter.fetchFiles(view, 
								CommandDetailView.this.lastRepo,
								CommandDetailView.this.lastPath);
					} else {
						view.show(new ArrayList<FileNode>(0), new ArrayList<FileNode>(0));
					}
					
				}
				view.setFilename(isNullOrBlank(fileSpecification.getFilename())?getFilename(fileSpecification.getName()):getFilename(fileSpecification.getFilename()),
						!isInput && fileSpecification.getName().endsWith(".zip"), !isInput || fileSpecification.isOptional());
				popup.add(view);
				popup.center();

			}},isInput?"browse repository to select input file":"browse repository to select output file location and name",
					isInput?"select file":"save as..");

		HasCell<FileSpecification,String> repoHasCell = new HasCell<FileSpecification,String>() {

			@Override
			public Cell<String> getCell() {
				return repo;
			}

			@Override
			public FieldUpdater<FileSpecification, String> getFieldUpdater() {
				return new FieldUpdater<FileSpecification, String>() {

					@Override
					public void update(int index, FileSpecification object, String value) {	
					}
				};
			}

			@Override
			public String getValue(FileSpecification object) {
				Repository r = CommandDetailView.this.uriToRepoMap.get(object.getRepository());
				if(r != null && r.getName()!=null)
					return r.getName();
				else
					return "";
			}
		};
		

		
		HasCell<FileSpecification,String> pathHasCell = new HasCell<FileSpecification,String>() {

			@Override
			public Cell<String> getCell() {
				return path;
			}

			@Override
			public FieldUpdater<FileSpecification, String> getFieldUpdater() {
				return new FieldUpdater<FileSpecification, String>() {
					@Override
					public void update(int index, FileSpecification object, String value) {
					}
				};
			}

			@Override
			public String getValue(FileSpecification object) {
				return object.getFilename();
			}
			
		};
		HasCell<FileSpecification,String> validHasCell = new HasCell<FileSpecification,String>() {

			@Override
			public Cell<String> getCell() {
				return valid;
			}

			@Override
			public FieldUpdater<FileSpecification, String> getFieldUpdater() {
				return new FieldUpdater<FileSpecification, String>() {
					@Override
					public void update(int index, FileSpecification object, String value) {
					}
				};
			}

			@Override
			public String getValue(FileSpecification object) {
				String text;
				if(isInput && !object.isOptional()) {
					text = "Repository name and file required";
				} else {
					text = "Repository name and file required";
				}
				boolean gotRepo = false;
				boolean gotPath = false;
				if(object != null) {
					gotRepo = !isNullOrBlank(object.getRepository());
					gotPath = !isNullOrBlank(object.getFilename()); 
				}
				boolean visible;
				if(gotRepo || gotPath) {
					visible = !(gotRepo && gotPath);
				} else {
					visible = isInput && !object.isOptional();
				}
				return (visible?"+":"-")+text;
			}
			
		};
		
		HasCell<FileSpecification,FileSpecification> browseHasCell = new HasCell<FileSpecification,FileSpecification>() {

			@Override
			public Cell<FileSpecification> getCell() {
				return browse;
			}

			@Override
			public FieldUpdater<FileSpecification, FileSpecification> getFieldUpdater() {
				return new FieldUpdater<FileSpecification, FileSpecification>() {
					@Override
					public void update(int index, FileSpecification object, FileSpecification value) {
					}
				};
			}

			@Override
			public FileSpecification getValue(FileSpecification object) {
				return object;
			}
			
		};
		
		
		List<HasCell<FileSpecification,?>> arg = new ArrayList<HasCell<FileSpecification,?>>(4);
		arg.add(validHasCell);
		arg.add(repoHasCell);
		
		arg.add(pathHasCell);
		arg.add(browseHasCell);

		return new CompositeCell<FileSpecification>(arg /*, isInput, /*repoRoot, options*/);
	}
	
	private String getPath(String canonical) {
		String result = canonical;
		if(canonical!=null && canonical.length()!=0) {
			if(!canonical.endsWith("/")) {
				result = canonical.substring(0, canonical.lastIndexOf("/")+1);	
			}
		}
		if(isNullOrBlank(result))
			result = null;
		return result;
	}
	
	private String getFilename(String canonical) {
		String result = "";
		if(canonical!=null && canonical.length()!=0) {
			if(!canonical.endsWith("/")) {
				result = canonical.substring(canonical.lastIndexOf("/")+1);	
			}
		}
		if(isNullOrBlank(result))
			result = null;
		return result;
	}

	
	private boolean isNullOrBlank(String s) {
		return s==null || s.length()==0;
	}
	
	private static class RepoIconTextCell extends IconCellDecorator<String> {

		/**
		 * @param icon
		 * @param cell
		 */
		public RepoIconTextCell(ImageResource icon, Cell<String> cell) {
			super(icon, cell);
		}
		

		/* (non-Javadoc)
		 * @see com.google.gwt.cell.client.IconCellDecorator#isIconUsed(java.lang.Object)
		 */
		@Override
		protected boolean isIconUsed(String value) {
			return value!=null && value.trim().length()>0 ;
		}
		
		
	}
	
	private static class RepoTextCellRenderer implements SafeHtmlRenderer<String> {

		  private static RepoTextCellRenderer instance;

		  public static RepoTextCellRenderer getInstance() {
		    if (instance == null) {
		      instance = new RepoTextCellRenderer();
		    }
		    return instance;
		  }

		  private RepoTextCellRenderer() {
		  }

		  public SafeHtml render(String object) {
			  if(object == null || object.trim().length() == 0) {
				  return SafeHtmlUtils.fromSafeConstant("<i>you have not specified a file</i>");
			  } else {
				  return SafeHtmlUtils.fromTrustedString("<div class='"+N3phele.n3pheleResource.css().repoTextCell()+"'>"+SafeHtmlUtils.htmlEscape(object)+"</div>");
			  }
		  }

		  public void render(String object, SafeHtmlBuilder appendable) {
		    appendable.append(render(object));
		  }
	}
	
	/*
	 * =============== Account table =============== 
	 */
	
	
	public CellTable<CloudProfile> createAccountTable(/*final SingleSelectionModel<ExtendedCloudProfile> selectionModel*/) {
		final SensitiveCheckBoxCell checkbox = new SensitiveCheckBoxCell(true, true);
		final ProvidesKey<CloudProfile> KEY_PROVIDER = new ProvidesKey<CloudProfile>() {

		    public Object getKey(CloudProfile item) {
		      return item.id();
		    }
		  };
		final CellTable<CloudProfile> table = new CellTable<CloudProfile>(KEY_PROVIDER);
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		Column<CloudProfile, Boolean> checkColumn = new Column<CloudProfile, Boolean>(checkbox)
		{
			@Override
		      public Boolean getValue(CloudProfile profile) {
				return (profile.getId().equals(CommandDetailView.this.selectedProfile))
				&& (profile.getAccountUri().equals(CommandDetailView.this.selectedAccountURI));
		      }

		};
		checkColumn.setFieldUpdater(new FieldUpdater<CloudProfile, Boolean>() {
			
			@Override
			public void update(int index, CloudProfile profile,
					Boolean value) {
				if(profile != null) {
					if(value) {
						CommandDetailView.this.selectedProfile = profile.getId();
					    CommandDetailView.this.selectedAccountURI = profile.getAccountUri();
					} else {
						if(profile.getId().equals(CommandDetailView.this.selectedProfile)) {
							CommandDetailView.this.selectedProfile = null;
							CommandDetailView.this.selectedAccountURI = null;
						}
					}
					table.redraw();
					String visible = value?"-":"+";
					CommandDetailView.this.gotExecutionSelection.setValue(visible+CommandDetailView.this.gotExecutionSelection.getValue().substring(1));
					updateRunButton(true);
				} else {
					checkbox.clearViewData(KEY_PROVIDER.getKey(profile));
					table.redraw();
					updateRunButton(false);
					CommandDetailView.this.gotExecutionSelection.setValue("+"+CommandDetailView.this.gotExecutionSelection.getValue().substring(1));
				}
				
			}
			
		});
		table.addColumn(checkColumn);
		TextColumn<CloudProfile> accountColumn = new TextColumn<CloudProfile>() {
			@Override
			public String getValue(CloudProfile profile) {
				String result = "";
				if(profile != null)
					return profile.getAccountName();
				return result;
			}
		};
		table.addColumn(accountColumn);
		TextColumn<CloudProfile> nameColumn = new TextColumn<CloudProfile>() {
			@Override
			public String getValue(CloudProfile profile) {
				String result = "";
				if(profile != null) {
					return profile.getCloudName();
				}
				return result;
			}
		};
		table.addColumn(nameColumn);
		TextColumn<CloudProfile> descriptionColumn = new TextColumn<CloudProfile>() {
			@Override
			public String getValue(CloudProfile profile) {
				if(profile != null)
					return profile.getDescription();
				else
					return null;
			}
		};
		table.addColumn(descriptionColumn);
		return table;
	}

	protected Column<FileSpecification,FileSpecification> replaceSelectionRepoColumn(CellTable<FileSpecification> table, Column<FileSpecification,FileSpecification> repoColumn, boolean isInput) {
		Column<FileSpecification,FileSpecification> newColumn = new Column<FileSpecification,FileSpecification>(createRepoRefCompositeCell(isInput)) {
			@Override
			public FileSpecification getValue(FileSpecification fs) {
				return fs;
			}
		};
		return replaceSelectionRepoColumn(table, newColumn, repoColumn);
	}
	
	protected Column<FileSpecification,FileSpecification> replaceSelectionRepoColumn(CellTable<FileSpecification> table, Column<FileSpecification,FileSpecification> newColumn, Column<FileSpecification,FileSpecification> repoColumn) {
		int index = table.getColumnIndex(repoColumn);
		table.removeColumn(index);
		table.insertColumn(index, newColumn);
		newColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		return newColumn;
	}
	
	private static class DisappearingTextInputCell extends TextInputCell {
		  @Override
		  public void render(Context context, String value, SafeHtmlBuilder sb) {
		    if(value != null)
		    	super.render(context, value, sb);
		  }
	}
	
	private static class DisappearingCheckBoxCell extends CheckboxCell {
		  @Override
		  public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
		    if(value != null)
		    	super.render(context, value, sb);
		  }
	}
}

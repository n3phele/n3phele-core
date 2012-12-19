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
package n3phele.service.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import n3phele.service.model.Account;
import n3phele.service.model.Activity;
import n3phele.service.model.CloudProfile;
import n3phele.service.model.Command;
import n3phele.service.model.ExecuteCommandRequest;
import n3phele.service.model.FileSpecification;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.NotFoundException;
import n3phele.service.model.core.UnprocessableEntityException;
import n3phele.service.model.repository.Repository;

@Path("/cli")
public class CliHelperResource {
	private static Logger log = Logger.getLogger(CliHelperResource.class.getName()); 

	@Context UriInfo uriInfo;
	@Context SecurityContext securityContext;
	private final Dao dao;
	public CliHelperResource() {
		dao = new Dao();
	}

	@POST
	@Produces("text/plain")
	@RolesAllowed("authenticated")
	@Path("run")
	public Response run(@FormParam("command") String commandName,
			@FormParam("name") String jobName,
			@FormParam("notify") boolean notify,
			@FormParam("on") String accountName,
			@FormParam("arg") List<String> parameterList,
			@FormParam("in") List<String> inputFileList,
			@FormParam("out") List<String> outputFileList) throws Exception {
		
		String result = "Run "+commandName+" for "+UserResource.toUser(securityContext).getName()+"\n";
		log.info(result);
		Command command = null;
		try {
			command = dao.command().load(commandName, UserResource.toUser(securityContext));
		} catch (Exception e) {
			throw new IllegalArgumentException("Unknown command "+commandName, e);
		}
		
		
		List<NameValue> parameters = new ArrayList<NameValue>();
		if(parameterList != null) {
			for(String item : parameterList) {
				String[] argVal = item.split(":");
				parameters.add(new NameValue(argVal[0].trim(), argVal[1].trim()));
				result += argVal[0].trim()+"="+argVal[1].trim()+"\n";
			}
		}
		
		List<FileSpecification> inputs = new ArrayList<FileSpecification>();
		if(inputFileList != null) {
			for(String item : inputFileList) {
				String[] argVal = null;
				String[] argVal2 = null;
				try {
					argVal = item.split(">");
					if(argVal.length != 2) {
						throw new UnprocessableEntityException("Input file "+item+" must be of the form repo://repo/path>command_input_file. Suspect missing >");
					}
					argVal2 = argVal[0].split("://");
					if(argVal2.length != 2) {
						throw new UnprocessableEntityException("Input file "+item+" must be of the form repo://repo/path>command_input_file. repo://repo/path is ill-formed");
					}

					Repository r = dao.repository().load(argVal2[0].trim(), UserResource.toUser(securityContext));
			    	FileSpecification inputFile = new FileSpecification(argVal[1].trim(), null, r.getUri(), argVal2[1].trim(), false);
			    	result += "Input "+argVal[1].trim()+" is "+argVal2[1].trim()+" from repo "+argVal2[0].trim()+" ("+r.getUri()+")\n";
					inputs.add(inputFile);
				} catch (UnprocessableEntityException e) {
					throw e;
				}  catch (NotFoundException e) {
					throw new UnprocessableEntityException("Unprocessible input "+item+" Repository "+argVal2[0]+" not found.");
				}catch (Exception e) {
					throw new UnprocessableEntityException("Unprocessible input "+item+" Cause: "+e.getMessage());
				}
			}
		}
		
		List<FileSpecification> outputs = new ArrayList<FileSpecification>();
		if(outputFileList != null) {
			for(String item : outputFileList) {
				String[] argVal = null;
				String[] argVal2 = null;
				try {
					argVal = item.split(">");
					if(argVal.length != 2) {
						throw new UnprocessableEntityException("Output file "+item+" must be of the form command_output_file>repo://repo/path. Suspect missing >");
					}

					argVal2 = argVal[1].split("://");
					if(argVal2.length != 2) {
						throw new UnprocessableEntityException("Output file "+item+" must be of the form command_output_file>repo://repo/path. repo://repo/path is ill-formed");
					}

					Repository r = dao.repository().load(argVal2[0].trim(), UserResource.toUser(securityContext));
			    	FileSpecification outputFile = new FileSpecification(argVal[0].trim(), null, r.getUri(), argVal2[1].trim(), false);
			    	result += "Output "+argVal[0].trim()+" is "+argVal2[1].trim()+" on repo "+argVal2[0].trim()+" ("+r.getUri()+")\n";
					outputs.add(outputFile);
				} catch (UnprocessableEntityException e) {
					throw e;
				} catch (NotFoundException e) {
					throw new UnprocessableEntityException("Unprocessible output "+item+" Repository "+argVal2[0]+" not found.");
				} catch (Exception e) {
					throw new UnprocessableEntityException("Unprocessible output "+item+" "+e.getClass().getSimpleName()+((e.getMessage() != null)?" Cause: "+e.getMessage():""));
				}
			}
		}

    	Collection<Account> accounts = dao.account().getCollection(UserResource.toUser(securityContext));
    	
    	Account account = null;
    	if(accounts.getTotal() > 0) {
	    	for(Account c : accounts.getElements()) {
	    		if(c.getName().equals(accountName)) {
	    			account = c;
	    		}
	    	}
    	}
    	if(account == null) {
     		throw new UnprocessableEntityException("No such account "+accountName);
     	}
      	
     	CloudProfile myProfile = null;
     	if(command.getCloudProfiles() != null) {
     		for(CloudProfile p : command.getCloudProfiles()) {
     			if(p.getCloud().equals(account.getCloud())) {
     				myProfile = p;
     			}
     		}
     	}
     	
     	if(myProfile == null) {
     		throw new UnprocessableEntityException("No command profile for Account "+accountName);
     	}
     	
    	
    	ExecuteCommandRequest request = new ExecuteCommandRequest();
    	request.setName(jobName);
    	request.setNotify(notify);
    	request.setUser(UserResource.toUser(securityContext).getName());
    	request.setCommand(command.getUri());
    	request.setAccount(account.getUri());
    	request.setProfileId(myProfile.getId());
    	request.setParameters(parameters.toArray(new NameValue[parameters.size()]));
    	request.setInputFiles(inputs.toArray(new FileSpecification[inputs.size()]));
    	request.setOutputFiles(outputs.toArray(new FileSpecification[outputs.size()]));
    	
    	ActivityResource z = new ActivityResource();
    	z.securityContext = this.securityContext;
    	z.uriInfo = this.uriInfo;
    	Activity activity = z.executeForUser(request, UserResource.toUser(securityContext));
    	result += "Created activity "+activity.getUri()+"\n";
    	result += "Monitor progress on "+activity.getProgress()+"\n";
		return Response.ok(result,MediaType.TEXT_PLAIN_TYPE).location(activity.getUri()).build();
	}


}

package n3pheleTests;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.Cloud;
import n3phele.service.model.VirtualServerCollection;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.Entity;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.VirtualServer;
import n3phele.service.rest.impl.Dao;
import n3phele.service.rest.impl.N3pheleResource;
import n3phele.service.rest.impl.VirtualServerResource;
import n3phele.service.rest.impl.CloudResource.CloudManager;
import n3phele.service.rest.impl.VirtualServerResource.VirtualServerManager;

import com.google.gson.Gson;
import com.google.gwt.http.client.RequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

public class VirtualServerTest extends JerseyTest {

	private static Logger log = Logger.getLogger(VirtualServerTest.class.getName());
	
	protected ResourceBundle bundle;

	public VirtualServerTest() throws Exception {
		super(new WebAppDescriptor.Builder(N3pheleResource.class.getPackage().getName()).servletPath("/resources").build());
		
		fillParameters();
	}
	
	static String userName;
	static String userPwd;
	
	public void fillParameters() {
		userName = Resource.get("testRootUsername", "");
		userPwd = Resource.get("testRootPassword", "");
	}
	

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new SimpleTestContainerFactory(Resource.get("baseURI", "http://localhost:8888"));
	}

	/*
	 * Test the process of adding a new VirtualServer object to GAE Data Store through API calls.
	 */
	@Test
	public void testVirtualServerCreationListAndDeletion() throws RequestException {		
		
		//add credentials to request
		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));

		String id = "999999";
		
		//Get the list of initial Servers
		VirtualServerCollection list = listVirtualServers();
		int initialCount = (list.getElements() == null) ? 0 : list.getElements().size();
		
		//Create Virtual Server
		ClientResponse clientResponse = addVirtualServer(id);
		
		Assert.assertEquals(201, clientResponse.getStatus());		
		
		list = listVirtualServers();
		int count = (list.getElements() == null) ? 0 : list.getElements().size();
		
		//check if list has grow by one
		Assert.assertTrue( (initialCount + 1) == count );
		
		//Delete Virtual Server
		clientResponse = removeVirtualServer(id);
		Assert.assertTrue( (200 == clientResponse.getStatus() ) || (204 == clientResponse.getStatus() ) );		

		list = listVirtualServers();
		count = (list.getElements() == null) ? 0 : list.getElements().size();
		
		//check if total servers number is equal as in the beginning
		Assert.assertTrue( initialCount == count );
	}
	
	@Test
	public void testGetVirtualServer() throws RequestException {
		
		//add credentials to request
		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));
		
		String id = "999999";
		
		//Create Virtual Server
		ClientResponse clientResponse = addVirtualServer(id);		
		Assert.assertEquals(201, clientResponse.getStatus());
		
		//Get Virtual Server
		VirtualServer vs = getVirtualServer(id);
		Assert.assertNotNull(vs);
		//Assert.assertEquals( (long)vs.getId(), Long.parseLong(id));
		
		//Delete Virtual Server
		clientResponse = removeVirtualServer(id);
		Assert.assertTrue( (200 == clientResponse.getStatus() ) || (204 == clientResponse.getStatus() ) );		
	}
	
	@Test
	public void testGetVirtualServerByInstanceId() throws RequestException {		

		//add credentials to request
		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));
		
		String id = "999999";
		
		//Create Virtual Server
		ClientResponse clientResponse = addVirtualServer(id);
		
		Assert.assertEquals(201, clientResponse.getStatus());
		
		//Get Virtual Server By instanceId.
		//Use the same id as the virtual server for this, because the add method created both as the same
		VirtualServer vs = getVirtualServer(id);
		Assert.assertNotNull(vs);
		//Assert.assertEquals( (long)vs.getId(), Long.parseLong(id));
		
		//Delete Virtual Server
		clientResponse = removeVirtualServer(id);
		Assert.assertTrue( (200 == clientResponse.getStatus() ) || (204 == clientResponse.getStatus() ) );		
	}
	

	/**
	 * Create a Virtual Server through the REST API call using the value of vmId, isntanceId, and spotId as the id passed by parameter.
	 * 
	 * @param id
	 * @return
	 */
	public ClientResponse addVirtualServer(String id) {

		// VM parameters
		String vmName = "VM-001";
		String vmDescription = "Virtual Machine with Windows 7";
		URI vmLocation = URI.create("http://www.test.com");

		// Creating the parameters list and sending as a json string
		ArrayList<NameValue> nameValueList = new ArrayList<NameValue>();
		nameValueList.add(new NameValue("testKey", "testValue"));
		String vmParametersList = new Gson().toJson(nameValueList);

		URI vmNotification = URI.create("http://www.test.com");
		String vmInstanceId = id;
		String vmSpotId = id;		
		URI vmOwner = URI.create("http://n3phele-dev.appspot.com/resources/user/1");
		String vmPrice = "1.00";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		String vmCreated = sdf.format(new Date());
		String vmActivity = "http://activity.com";
		Random rdm = new Random();
		int vmId = Integer.valueOf(id);
		String vmAccount = "http://n3phele-dev.appspot.com/resources/account/30022";
		String clouduri = "http://cloud.com";

		// Creating the form to call the add method in the VirtualServerResource
		Form form = new Form();
		form.add("id", String.valueOf(vmId));
		form.add("name", vmName);
		form.add("description", vmDescription);
		form.add("location", vmLocation);
		form.add("parametersList", vmParametersList);
		form.add("notification", vmNotification);
		form.add("instanceId", vmInstanceId);
		form.add("spotId", vmSpotId);
		form.add("owner", vmOwner);
		form.add("created", vmCreated);
		form.add("price", vmPrice);
		form.add("activity", vmActivity);
		form.add("account", vmAccount);
		form.add("clouduri", clouduri);
		
		// Executing the request
		ClientResponse clientResponse = resource().path("/virtualServers").post(ClientResponse.class, form);
		return clientResponse;
	}


	public VirtualServerCollection listVirtualServers() {

		// Executing the request
		VirtualServerCollection list = resource().path("/virtualServers").get(VirtualServerCollection.class);

		return list;
	}
	
	public VirtualServer getVirtualServer(String id) {		
		VirtualServer vs = null;
		
		// Build the request URL
		StringBuilder sbPath = new StringBuilder();
		sbPath.append("/virtualServers/");
		sbPath.append(id);

		// Execute the request
		vs = resource().path(sbPath.toString()).get(VirtualServer.class);
				
		return vs;
	}
	
	public VirtualServer getVirtualServerByInstanceId(String id) {
		VirtualServer vs = null;
		
		StringBuilder sbPath = new StringBuilder();
		sbPath.append("/virtualServers/");
		sbPath.append("instanceId/");
		sbPath.append(id);

		// Execute the request
		vs = resource().path(sbPath.toString()).get(VirtualServer.class);
		
		return vs;
	}

		
	/**
	 * Removes a Virtual Server through the REST API call using the id passed by parameter.
	 * 
	 * @param id
	 * @return
	 */
	public ClientResponse removeVirtualServer(String id) {

		// Build the request URL
		StringBuilder sbPath = new StringBuilder();
		sbPath.append("/virtualServers/");
		sbPath.append(id);

		VirtualServer vs = resource().path(sbPath.toString()).get(VirtualServer.class);
		String accId = vs.getAccount().substring(vs.getAccount().lastIndexOf('/') + 1, vs.getAccount().length());

		// Execute the request
		ClientResponse clientResponse = resource().path(sbPath.toString()).header("account", accId).header("delete", "true").delete(ClientResponse.class);

		return clientResponse;
	}
	
	@Test
	public void testUpdateStatusTerminateMachine() {
		
		// *** Mock classes used by the VirtualServerResource into the updateStatus method ***
		//mock the local list of VMs
		Dao dao = mock(Dao.class);
		VirtualServerManager vsm = mock(VirtualServerManager.class);
		when(dao.virtualServer()).thenReturn(vsm);
		
		Collection<VirtualServer> vsCollection = new Collection<VirtualServer>();
		List<VirtualServer> vsList = new ArrayList<VirtualServer>();		
		
		VirtualServer vs = null;		
		//Add one fake virtual server to the list
		try {			
			vs = spy( new VirtualServer("name","description", new URI("http://location"), null, 
					new URI("http://notification"), "instanceId", "spotId", 
					new URI("http://owner"), new Date(), "price", new URI("http://activity"),
					1l, new URI("http://account"), "cloudURI", "http://entityURI" ) );
			vsList.add(vs);
		} catch (URISyntaxException e) {
			fail();
		}		
		vs.setId(1L);
		vsCollection.setElements(vsList);		
		//when the method ask for vms, give the list with the created vm
		when(vsm.getCollection()).thenReturn(vsCollection);
		
		//Mock the call to vms in the EC2 as empty
		final WebResource resource = mock(WebResource.class);
		when(resource.get(new GenericType<Collection<n3phele.service.model.core.Entity>>() {})).thenReturn(new Collection<n3phele.service.model.core.Entity>());
		
		//Mock the call to the cloud database search
		CloudManager cm = mock(CloudManager.class);
		Cloud cloud = mock(Cloud.class);
		//The dao returns the mocked cloudManager
		when(dao.cloud()).thenReturn(cm);
		when(cm.get(any(URI.class))).thenReturn(cloud);
		//The cloud returns a null factoryURI that is used for the vm list get. Since we mocked the call it can be any value
		when(cloud.getFactory()).thenReturn(null);
		
		//Create the virtual Server
		VirtualServerResource vsr = spy( new VirtualServerResource(dao) {
			//Override the prepare resource method that access the cloud
			@Override
			protected WebResource createFactoryWebResource(Cloud cloud, URI factory) {
				return resource;
			}
		} );
		
		//Execute tested method
		vsr.updateStatus();
		
		verify(vsm).getCollection();
		verify(vs, times(1)).setStatus("terminated");		
		
	}
	
	@Test
	public void testUpdateStatusKeepMachine() throws URISyntaxException {
		
		// *** Mock classes used by the VirtualServerResource into the updateStatus method ***
		//mock the local list of VMs
		Dao dao = mock(Dao.class);
		VirtualServerManager vsm = mock(VirtualServerManager.class);
		when(dao.virtualServer()).thenReturn(vsm);
		
		Collection<VirtualServer> vsCollection = new Collection<VirtualServer>();
		List<VirtualServer> vsList = new ArrayList<VirtualServer>();		
		
		//Add one fake virtual server to the list
		final VirtualServer	vs = spy( new VirtualServer("name","description", new URI("http://location"), null, 
					new URI("http://notification"), "instanceId", "spotId", 
					new URI("http://owner"), new Date(), "price", new URI("http://activity"),
					1l, new URI("http://account"), "cloudURI", "entityURI" ) );
			vsList.add(vs);
			
		vs.setId(1L);
		vsCollection.setElements(vsList);		
		//when the method ask for vms, give the list with the created vm
		when(vsm.getCollection()).thenReturn(vsCollection);
		
		//Mock the call to vms in the EC2. Return the same virtual server object from the local database. 
		final WebResource resource = mock(WebResource.class);
		Collection col = new Collection<n3phele.service.model.core.Entity>();
		List list = new ArrayList<Entity>();
		list.add(vs);
		col.setElements(list);
		when(resource.get(any(GenericType.class) ) ).thenReturn(col);
		
		//Mock the call to the cloud database search
		CloudManager cm = mock(CloudManager.class);
		Cloud cloud = mock(Cloud.class);
		//The dao returns the mocked cloudManager
		when(dao.cloud()).thenReturn(cm);
		when(cm.get(any(URI.class))).thenReturn(cloud);
		//The cloud returns a null factoryURI that is used for the vm list get. Since we mocked the call it can be any value
		when(cloud.getFactory()).thenReturn(null);
		
		//Create the virtual Server
		VirtualServerResource vsr = spy( new VirtualServerResource(dao) {
			//Override the prepare resource method that access the cloud
			@Override
			protected WebResource createFactoryWebResource(Cloud cloud, URI factory) {
				return resource;
			}
			
			//Override the method that populates an local Map to hold vm information from the cloud. 
			@Override
			protected Map<String,Entity> populateTable(Collection<Entity> col, WebResource resource){
				Map<String,Entity> vmTable = new HashMap<String,Entity>();
				
				vmTable.put(vs.getInstanceId(), vs);
				
				return vmTable;
			}
		} );
		
		//Execute tested method
		vsr.updateStatus();
		
		verify(vsm).getCollection();
		verify(vs, times(0)).setStatus("terminated");		
		Assert.assertEquals(vs.getStatus(), "running");
		
	}
}
package n3pheleTests;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import n3phele.service.core.Resource;
import n3phele.service.model.Account;
import n3phele.service.model.VirtualServerCollection;
import n3phele.service.model.core.Credential;
import n3phele.service.model.core.NameValue;
import n3phele.service.model.core.VirtualServer;
import n3phele.service.rest.impl.N3pheleResource;

import com.google.appengine.labs.repackaged.com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gwt.http.client.RequestException;
import com.sun.jersey.api.client.ClientResponse;
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
	 * Test the process of adding a new VirtualServer object to GAE Data Store.
	 */
	@Test
	public void testAddVS() throws RequestException {

		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));		

		ClientResponse c = resource().path("account").get(ClientResponse.class);
		

		// VM parameters
		String vmName = "VM-001";
		String vmDescription = "Virtual Machine with Windows 7";
		URI vmLocation = URI.create("http://www.test.com");

		// Creating the parameters list and sending as a json string
		ArrayList<NameValue> nameValueList = new ArrayList<NameValue>();
		nameValueList.add(new NameValue("testKey", "testValue"));
		String vmParametersList = new Gson().toJson(nameValueList);

		URI vmNotification = URI.create("http://www.test.com");
		String vmInstanceId = "12345";
		String vmSpotId = "54321";		
		URI vmOwner = URI.create("http://n3phele-dev.appspot.com/resources/user/1");
		String vmPrice = "1.00";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		String vmCreated = sdf.format(new Date());
		String vmActivity = "http://activity.com";
		Random rdm = new Random();
		int vmId = rdm.nextInt(9999);
		String vmAccount = "http://n3phele-dev.appspot.com/resources/account/30022";

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

		// Executing the request
		ClientResponse clientResponse = resource().path("/virtualServers").post(ClientResponse.class, form);

		Assert.assertEquals(201, clientResponse.getStatus());
		
		
	}

	/*
	 * Tests the process of retrieving a collection of VirtualServer
	 */
	@Test
	public void testListVS() {

		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));

		// Executing the request
		VirtualServerCollection list = resource().path("/virtualServers").get(VirtualServerCollection.class);

		// List is null ?
		Assert.assertNotNull(list.getElements());
	}

	//@Test
	public void testListVSForAccount() {

		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));

		// Executing the request
		VirtualServerCollection list = resource().path("/virtualServers/account/" + 5).get(VirtualServerCollection.class);

		// List is null ?
		Assert.assertNotNull(list.getElements());
	}

	/*
	 * Tests the process of retrieving a VirtualServer
	 */
	@Test
	public void testGetVS() {

		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));

		// Retrieves a list of VirtualServer objects
		VirtualServerCollection list = resource().path("/virtualServers").get(VirtualServerCollection.class);

		if (list.getElements().isEmpty()) {
			// The list of VirtualServer objects is empty
			log.info("testGetVS - The list of VirtualServer is empty");
			Assert.assertTrue(false);
		} else {

			// Get the VirtualServer ID
			int pos = 0;
			for (VirtualServer s : list.getElements()) {
				if (s.getIsAlive()) {
					break;
				}
				pos++;
			}
			String vsURI = list.getElements().get(pos).getUri().toString();
			String vsId = vsURI.substring(vsURI.toString().lastIndexOf('/') + 1, vsURI.length());

			// Build the request URL
			StringBuilder sbPath = new StringBuilder();
			sbPath.append("/virtualServers/");
			sbPath.append(vsId);

			// Execute the request
			VirtualServer vs = resource().path(sbPath.toString()).get(VirtualServer.class);

			Assert.assertNotNull(vs);
			
			URI path = resource().getURI();
			}
	}

	/*
	 * Test the process of retrieving a VirtualServer by the instanceId
	 */
	//@Test
	public void testGetVSByInstanceId() {

		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));

		Account acc = resource().path("/account/5").get(Account.class);
	
		Credential cred = acc.getCredential();
		
		this.client().removeAllFilters();
		this.client().addFilter(new HTTPBasicAuthFilter(cred.getAccount(), cred.getSecret()));
		
		// Retrieves a list of VirtualServer objects
		VirtualServerCollection list = resource().path("/virtualServers").get(VirtualServerCollection.class);
		String vsInstanceId = "";
		
		if (list.getElements().isEmpty()) {
			
			// The list of VirtualServer objects is empty
			log.info("testGetVS - The list of VirtualServer is empty");
			Assert.assertTrue(false);
			
		} else {

			// Get the VirtualServer ID
			int pos = 0;
			for (VirtualServer s : list.getElements()) {
				if (s.getIsAlive()) {
					vsInstanceId += s.getInstanceId();
					break;
				}
				pos++;
			}

			// Build the request URL
			StringBuilder sbPath = new StringBuilder();
			sbPath.append("/virtualServers/");
			sbPath.append("instanceId/");
			sbPath.append(vsInstanceId);

			// Execute the request
			VirtualServer vs = resource().path(sbPath.toString()).get(VirtualServer.class);
			Assert.assertNotNull(vs);
		}

	}

	/*
	 * Tests the process of removing a VirtualServer
	 */
	//@Test
	public void removeVS() {

		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));

		// Retrieves a list of VirtualServer objects
		VirtualServerCollection list = resource().path("/virtualServers").get(VirtualServerCollection.class);

		if (list.getElements().isEmpty()) {
			// The list of VirtualServer objects is empty
			log.info("testRemoveVS - The list of VirtualServer is empty");
			Assert.assertTrue(false);
		} else {

			// Get the VirtualServer ID
			int pos = 0;
			for (VirtualServer s : list.getElements()) {
				if (s.getIsAlive()) {
					break;
				}
				pos++;
			}
			String vsURI = list.getElements().get(pos).getUri().toString();
			String vsId = vsURI.substring(vsURI.toString().lastIndexOf('/') + 1, vsURI.length());

			// Build the request URL
			StringBuilder sbPath = new StringBuilder();
			sbPath.append("/virtualServers/");
			sbPath.append(vsId);

			VirtualServer vs = resource().path(sbPath.toString()).get(VirtualServer.class);
			String accId = vs.getAccount().substring(vs.getAccount().lastIndexOf('/') + 1, vs.getAccount().length());

			// Execute the request
			ClientResponse c = resource().path(sbPath.toString()).header("account", accId).delete(ClientResponse.class);

			Assert.assertEquals(200, c.getStatus());
		}
	}
	
	//@Test
	public void updateVS(){
		this.client().addFilter(new HTTPBasicAuthFilter(userName, userPwd));
		System.out.println(resource().getURI().toString());
		ClientResponse response = resource().path("virtualServers/updateStatus").get(
				ClientResponse.class);
		
		Assert.assertEquals(200, response.getStatus());
	}
}
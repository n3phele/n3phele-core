package n3pheleTests;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import n3phele.service.model.Cloud;
import n3phele.service.model.core.Collection;
import n3phele.service.model.core.VirtualServer;
import n3phele.service.rest.impl.Dao;
import n3phele.service.rest.impl.VirtualServerResource;
import n3phele.service.rest.impl.CloudResource.CloudManager;
import n3phele.service.rest.impl.VirtualServerResource.VirtualServerManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import static org.mockito.Mockito.*;

public class TestTemplate {
	
	private final LocalServiceTestHelper helper =   new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig()
				.setStoreDelayMs(5),
			new LocalTaskQueueTestConfig()
								.setDisableAutoTaskExecution(false)             
								.setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class)) ;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		helper.setUp();
	}
	
	@After     
	public void tearDown() {
		helper.tearDown();     
	}
	
	// run this test twice to prove we're not leaking any state across tests
    private void doTest() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
        ds.put(new Entity("yam"));
        ds.put(new Entity("yam"));
        assertEquals(2, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
    }

    @Test
    public void testInsert1() {
        doTest();
    }

    @Test
    public void testInsert2() {
        doTest();
    }

	
	@Test
	public void addTest() {		
		int c = add(2,2);
		assertEquals(4, c);		
	}
	
	public int add(int a, int b)
	{
		return a+b;
	}	
	
	
	/*
	 * 
	 * Mockito examples from : http://docs.mockito.googlecode.com/hg/latest/org/mockito/Mockito.html
	 * 
	 */	
	@Test
	public void testVerifyInteractions()	{
		
		List mockedList = mock(List.class);
		
		mockedList.add("one");
		mockedList.clear();
		
		verify(mockedList).add("one");
		verify(mockedList).clear();		
	}
	
	@Test
	public void testStubMethods1() {
		
		LinkedList mockedList = mock(LinkedList.class);
		when(mockedList.get(0)).thenReturn("return");
		
		System.out.println(mockedList.get(0));
		System.out.println(mockedList.get(999));		
		
	}
	
	@Test(expected=RuntimeException.class)
	public void testStubMethods2() {
		
		//You can mock concrete classes, not only interfaces
		LinkedList mockedList = mock(LinkedList.class);
		//stubbing
		when(mockedList.get(0)).thenReturn("first");
		when(mockedList.get(1)).thenThrow(new RuntimeException());
		
		//following prints "first"
		assertEquals("first", mockedList.get(0));
		//following throws runtime exception
		System.out.println(mockedList.get(1));
		//following prints "null" because get(999) was not stubbed
		assertEquals(null,(mockedList.get(999)) );
		
		//Although it is possible to verify a stubbed invocation, usually it's just redundant
		//If your code cares what get(0) returns then something else breaks (often before even verify() gets executed).
		//If your code doesn't care what get(0) returns then it should not be stubbed. Not convinced? See here.
		verify(mockedList).get(0);
		
	}
		
}

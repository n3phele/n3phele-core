package n3pheleTests;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

public class SimpleTestContainerFactory implements TestContainerFactory {
	private String defaultHost;
	public SimpleTestContainerFactory(String defaultHost) {
		this.defaultHost = defaultHost;
	}
	public Class<WebAppDescriptor> supports() {
		return WebAppDescriptor.class;
	}

	public TestContainer create(URI baseUri, AppDescriptor ad) {
		if (!(ad instanceof WebAppDescriptor))
			throw new IllegalArgumentException(
			"The application descriptor must be an instance of WebAppDescriptor");
//		ClientConfig cc = ((WebAppDescriptor) ad).getClientConfig();
//		 cc.getClasses().add(JacksonJsonProvider.class);

		if(defaultHost != null && defaultHost.endsWith(((WebAppDescriptor) ad).getServletPath())) {
			return new SimpleTestContainer(URI.create(defaultHost));
		}
		return new SimpleTestContainer(baseUri,
				(WebAppDescriptor) ad);
	}


	/**
	 * * Class which helps running tests on an external container. It assumes
	 * that * the container is started/stopped explicitly and also that the
	 * application is * pre-deployed.
	 */
	private static class SimpleTestContainer implements TestContainer {
		final URI baseUri;

		private SimpleTestContainer(URI baseUri, WebAppDescriptor ad) {
			this(UriBuilder.fromUri(baseUri)
			.path(ad.getContextPath()).path(ad.getServletPath())
			.build());
		}
		
		private SimpleTestContainer(URI baseUri) { this.baseUri = baseUri;}

		public Client getClient() {
			return null;
		}

		public URI getBaseUri() {
			return baseUri;
		}

		public void start() { // do nothing } public void stop() { // do nothing
			// } }}
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub

		}
	}
}
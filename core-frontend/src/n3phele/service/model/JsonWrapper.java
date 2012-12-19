package n3phele.service.model;

public class JsonWrapper {
	
	private CloudRegions config;

	public JsonWrapper() {}
	
	public JsonWrapper(CloudRegions config) {
		this.config = config;
	}

	public CloudRegions getConfig() {
		return this.config;
	}

	public void setConfig(CloudRegions config) {
		this.config = config;
	}
}

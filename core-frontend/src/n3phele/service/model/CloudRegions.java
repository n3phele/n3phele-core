package n3phele.service.model;

public class CloudRegions {
	
	private Region[] regions;

	public CloudRegions() {}
	
	public CloudRegions(Region[] regions) {
		this.regions = regions;
	}

	public Region[] getRegions() {
		return this.regions;
	}

	public void setRegions(Region[] regions) {
		this.regions = regions;
	}
}

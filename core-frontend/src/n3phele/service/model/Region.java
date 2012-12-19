package n3phele.service.model;

import java.util.ArrayList;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Region")
@XmlType(name="Region")
@Unindexed
@Cached
public class Region {

	@Id
	private Long id;
	private String region;
	@Embedded
	private ArrayList<InstanceType> instanceTypes;
	
	public Region() {}
	
	public Region(String region, ArrayList<InstanceType> instanceTypes) {
		this.region = region;
		this.instanceTypes = instanceTypes;
	}

	public String getRegion() {
		return this.region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public ArrayList<InstanceType> getInstanceTypes() {
		return this.instanceTypes;
	}

	public void setInstanceTypes(ArrayList<InstanceType> instanceTypes) {
		this.instanceTypes = instanceTypes;
	}
}

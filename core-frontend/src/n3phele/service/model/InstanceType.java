package n3phele.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="InstanceType")
@XmlType(name="InstanceType")
@Unindexed
@Cached
public class InstanceType implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	private Long id;
	private String type;
	@Serialized
	private ArrayList<InstanceSize> sizes;
	
	public InstanceType() {}
	
	public InstanceType(String type, ArrayList<InstanceSize> sizes) {
		this.type = type;
		this.sizes = sizes;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setSizes(ArrayList<InstanceSize> sizes) {
		this.sizes = sizes;
	}
	
	public String getType() {
		return this.type;
	}
	
	public ArrayList<InstanceSize> getSizes() {
		return this.sizes;
	}
}

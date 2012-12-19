package n3phele.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="InstanceSize")
@XmlType(name="InstanceSize")
@Unindexed
@Cached
public class InstanceSize implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	private Long id;
	private String size;
	@Serialized
	private ArrayList<InstancePrice> valueColumns;
	
	public InstanceSize() {}
	
	public InstanceSize(String size, ArrayList<InstancePrice> valueColumns) {
		this.size = size;
		this.valueColumns = valueColumns;
	}
	
	public void setSize(String size) {
		this.size = size;
	}

	public void setValueColumns(ArrayList<InstancePrice> valueColumns) {
		this.valueColumns = valueColumns;
	}
	
	public String getSize() {
		return this.size;
	}
	
	public ArrayList<InstancePrice> getValueColumns() {
		return this.valueColumns;
	}
}

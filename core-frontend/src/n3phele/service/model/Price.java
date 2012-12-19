package n3phele.service.model;

import java.io.Serializable;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@XmlRootElement(name="Price")
@XmlType(name="Price")
@Unindexed
@Cached
public class Price implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	private Long id;
	private String USD;
	
	public Price() {}
	
	public Price(String USD) {
		this.USD = USD;
	}
	
	public String getUSD() {
		return this.USD;
	}
	
	public void setUSD(String USD) {
		this.USD = USD;
	}
}

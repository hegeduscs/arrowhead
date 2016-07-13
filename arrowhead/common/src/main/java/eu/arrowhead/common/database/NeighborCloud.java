package eu.arrowhead.common.database;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import eu.arrowhead.common.model.ArrowheadCloud;

/**
 * @author umlaufz
 * 
 * Entity class for storing nearby Cloud informations in the database.
 * The "operator" and "cloud_name" columns must be unique together.
 */
@Entity
@Table(name="neighborhood", uniqueConstraints={@UniqueConstraint(columnNames = {"cloud_id"})})
public class NeighborCloud {
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
    private int id;
	
	@JoinColumn(name="cloud_id")
	@OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
	private ArrowheadCloud cloud;
	
	public NeighborCloud(){
	}
	
	public NeighborCloud(ArrowheadCloud cloud) {
		this.cloud = cloud;
	}
	
	public int getId() {
		return id;
	}
	
	public ArrowheadCloud getCloud() {
		return cloud;
	}

	public void setCloud(ArrowheadCloud cloud) {
		this.cloud = cloud;
	}

	public boolean isPayloadUsable(){
		if(cloud == null || !cloud.isValid())
			return false;
		return true;
	}

	
}

package eu.arrowhead.core.authorization.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author umlaufz
 * 
 * Entity class for storing Arrowhead Clouds in the database.
 * The "operator" and "cloud_name" columns must be unique together.
 */
@Entity
@Table(name="arrowhead_cloud", uniqueConstraints={@UniqueConstraint(columnNames = {"operator", "cloud_name"})})
@XmlRootElement
public class ArrowheadCloud {
   
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
    private int id;
	
    @Column(name="operator")
    private String operator;
    
    @Column(name="cloud_name")
    private String cloudName;
    
    @Column(name="authentication_info")
    private String authenticationInfo;
   
    public ArrowheadCloud(){
    }
    
    public ArrowheadCloud(String operator, String cloudName, String authenticationInfo) {
        this.operator = operator;
        this.cloudName = cloudName;
        this.authenticationInfo = authenticationInfo;
    }    
    
    public int getId() {
		return id;
	}

	public String getOperator() {
        return operator;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }
    public String getCloudName() {
        return cloudName;
    }
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }
    public String getAuthenticationInfo() {
        return authenticationInfo;
    }
    public void setAuthenticationInfo(String authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }
   
   
}
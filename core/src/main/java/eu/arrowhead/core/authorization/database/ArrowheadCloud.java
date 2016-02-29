package eu.arrowhead.core.authorization.database;
 
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

 
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"operator", "cloudName"})})
@XmlRootElement
public class ArrowheadCloud {
   
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
    private int id;
    private String operator;
    private String cloudName;
    private String authenticationInfo;
    @ManyToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinTable (name="Clouds_Services")
    private Collection<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
   
    public ArrowheadCloud(){
       
    }
    
    public ArrowheadCloud(String operator, String cloudName, String authenticationInfo) {
        super();
        this.operator = operator;
        this.cloudName = cloudName;
        this.authenticationInfo = authenticationInfo;
    }    
    public ArrowheadCloud(String operator, String cloudName, String authenticationInfo,
            Collection<ArrowheadService> serviceList) {
        super();
        this.operator = operator;
        this.cloudName = cloudName;
        this.authenticationInfo = authenticationInfo;
        this.serviceList = serviceList;
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
    public Collection<ArrowheadService> getServiceList() {
        return serviceList;
    }
    public void setServiceList(Collection<ArrowheadService> serviceList) {
        this.serviceList = serviceList;
    }
    
    public boolean isEqual(ArrowheadCloud arrowheadCloud){
    	boolean op = (this.operator.equals(arrowheadCloud.getOperator()));
    	boolean cn = (this.cloudName.equals(arrowheadCloud.getCloudName()));
    	boolean ai = (this.authenticationInfo.equals(arrowheadCloud.getAuthenticationInfo()));
    	boolean sl = (this.serviceList.size() == arrowheadCloud.getServiceList().size());
    	
    	return op && cn && ai && sl;
    }
   
   
}
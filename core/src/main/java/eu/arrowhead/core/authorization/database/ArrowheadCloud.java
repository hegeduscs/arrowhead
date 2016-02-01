package eu.arrowhead.core.authorization.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ArrowheadCloud {
	
	@Id @GeneratedValue
	private int id;
	private String operator;
	private String cloudName;
	private String authenticationInfo;
	
	public ArrowheadCloud(){
		
	}
	
	public ArrowheadCloud(String operator, String cloudName, String authenticationInfo) {
		this.operator = operator;
		this.cloudName = cloudName;
		this.authenticationInfo = authenticationInfo;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getName() {
		return cloudName;
	}
	public void setName(String cloudName) {
		this.cloudName = cloudName;
	}
	
	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}
	

}
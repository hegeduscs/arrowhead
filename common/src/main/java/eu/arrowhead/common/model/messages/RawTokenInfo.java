package eu.arrowhead.common.model.messages;

public class RawTokenInfo {

	private String d;
	private String s;
	private String c;
	private String i;
	
	public RawTokenInfo() {
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public String getI() {
		return i;
	}

	public void setI(String i) {
		this.i = i;
	}

	@Override
	public String toString() {
		return "ClassPojo [d = " + d + ", s = " + s + ", c = " + c + ", i = " + i + "]";
	}
}
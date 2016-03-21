package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QoSReservationResponse {
	boolean response;

	public QoSReservationResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	public QoSReservationResponse(boolean response) {
		this.response = response;
	}

	public boolean isResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}

}

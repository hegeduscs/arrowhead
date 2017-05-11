package eu.arrowhead.common.model.messages;

/**
 * Message Exchanged from QoSVerifierAlgorithm to QoSManager.
 *
 * @author Paulo
 *
 */
public class QoSVerifierResponse {

    private boolean response;

    public static enum RejectMotivationTypes {
        ALWAYS, TEMPORARY, COMBINATION;
    }

    private RejectMotivationTypes RejectMotivaiton;

    public QoSVerifierResponse() {

    }

    public QoSVerifierResponse(boolean response, RejectMotivationTypes rejectMotivaiton) {
        super();
        this.response = response;
        RejectMotivaiton = rejectMotivaiton;
    }

    public RejectMotivationTypes getRejectMotivation() {
        return RejectMotivaiton;
    }

    public void setRejectMotivaiton(RejectMotivationTypes rejectMotivaiton) {
        RejectMotivaiton = rejectMotivaiton;
    }

    public boolean getResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

}

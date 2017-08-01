package eu.arrowhead.common.model.messages;

public class ICNEnd {

  private OrchestrationResponse instructions;

  public ICNEnd() {
  }

  public ICNEnd(OrchestrationResponse instructions) {
    this.instructions = instructions;
  }

  public OrchestrationResponse getInstructions() {
    return instructions;
  }

  public void setInstructions(OrchestrationResponse instructions) {
    this.instructions = instructions;
  }

}

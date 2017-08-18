package eu.arrowhead.common.model.messages;

public class ICNResult {

  private OrchestrationResponse instructions;

  public ICNResult() {
  }

  public ICNResult(OrchestrationResponse instructions) {
    this.instructions = instructions;
  }

  public ICNResult(ICNEnd icnEnd) {
    this.instructions = icnEnd.getInstructions();
  }

  public OrchestrationResponse getInstructions() {
    return instructions;
  }

  public void setInstructions(OrchestrationResponse instructions) {
    this.instructions = instructions;
  }

  public boolean isValid() {
    return instructions != null && instructions.getResponse() != null && !instructions.getResponse().isEmpty();
  }

}

package edu.colorado.typestate.lstar.automatarepresentation;

public class Tuple {

	private String acceptedString;
	private Integer stateId;
	public String getAcceptedString() {
		return acceptedString;
	}
	public void setAcceptedString(String acceptedString) {
		this.acceptedString = acceptedString;
	}
	public Integer getStateId() {
		return stateId;
	}
	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
	@Override
	public String toString() {
		return "Tuple [acceptedString=" + acceptedString + ", stateId="
				+ stateId + "]";
	}
}

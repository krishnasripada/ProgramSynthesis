package edu.colorado.typestate.lstar.automatarepresentation;

public class Transition {

	private String prefix;
	private String input;
	private String output;
	private int currentState;
	private int nextState;
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getInput() {
		return input;
	}
	public String getOutput() {
		return output;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public int getCurrentState() {
		return currentState;
	}
	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	public int getNextState() {
		return nextState;
	}
	public void setNextState(int nextState) {
		this.nextState = nextState;
	}
	@Override
	public String toString() {
		return "Transition [prefix=" + prefix + ", input=" + input
				+ ", output=" + output + ", currentState=" + currentState
				+ ", nextState=" + nextState + "]";
	}
}

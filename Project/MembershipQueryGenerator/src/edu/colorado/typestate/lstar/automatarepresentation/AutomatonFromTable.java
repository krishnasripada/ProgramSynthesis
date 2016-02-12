package edu.colorado.typestate.lstar.automatarepresentation;

import java.util.ArrayList;
import java.util.List;

public class AutomatonFromTable {

	private List<String> inputs;
	protected List<Transition> transitions;
	protected final int inputSize;
	protected int numStates;
	protected int initial = -1;
	protected List<Integer> finalStates;
	
	public AutomatonFromTable(List<String> inputs) {
		this.inputs = inputs;
		this.inputSize = inputs.size();
		this.transitions = new ArrayList<Transition>();
		this.finalStates = new ArrayList<>();
	}

	protected static final int getId(Integer id) {
		return (id != null) ? id.intValue() : -1;
	}
	
	public Integer addInitialState() {
		int newState = numStates++;
		if(initial==-1){
			setInitialState(newState);
		}else{
			finalStates.add(newState);
		}
		return newState;
	}

	public Integer addState() {
		return addInitialState();
	}

	private void setInitialState(int newState) {
		initial = newState;	
	}
	
	public Integer getInputSize(){
		return inputSize;
	}
	
	public Integer getNumberStates(){
		return numStates;
	}
	
	public Integer getInputState(){
		return initial;
	}

	public List<String> getInputs() {
		return inputs;
	}

	public void addTransition(Transition transition){
		transitions.add(transition);
	}
	
	public List<Transition> getTransitions(){
		return transitions;
	}

	public List<Integer> getFinalStates() {
		return finalStates;
	}
}

package edu.colorado.typestate.lstar.oracles;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import edu.colorado.typestate.lstar.automatarepresentation.AutomatonFromTable;
import edu.colorado.typestate.lstar.automatarepresentation.Transition;

public class EquivalenceOracle {

	public String findCounterExample(AutomatonFromTable hypothesis, List<String> inputs, int bound, Map<Integer, String> pickReps, Map<String, String> membershipQueryResults) {
		
		for(int i=0;i<hypothesis.getNumberStates();i++){
	        if(hypothesis.getFinalStates().contains(i)!=askAndroid(pickReps.get(i), membershipQueryResults)){
	        	return pickReps.get(i);
	        }
	        for(String input: inputs){
	        	int newTransitionState = getNewTransitionState(i, input, hypothesis);
	        	String counterExample = check(pickReps.get(i)+"$"+input, newTransitionState, membershipQueryResults, bound, hypothesis);
	        	if(counterExample!=null){
	        		return pickReps.get(i)+"$"+input+"$"+counterExample;
	        	}
	        }
	    }
		return null;
	}

	private String check(String newString, int newTransitionState, Map<String, String> membershipQueryResults, 
			int bound, AutomatonFromTable hypothesis) {
		
		List<String> inputs = hypothesis.getInputs();
		for(String input: inputs){
			int boundChecker = input.split("\\$").length;
			if(boundChecker<=bound){
				boolean androidResult = askAndroid(newString+"$"+input, membershipQueryResults);
				//System.out.println("Android Result "+androidResult);
				boolean hypothesisResult = askHypothesis(input, newTransitionState, hypothesis);
				//System.out.println("Hypothesis Result "+hypothesisResult);
				if(androidResult!=hypothesisResult){
					return input;
				}
			}
		}
		return null;
	}

	private boolean askHypothesis(String checker, int newTransitionState, AutomatonFromTable hypothesis) {
		
		int newTransition = getNewTransitionState(newTransitionState, checker, hypothesis);
		if(hypothesis.getFinalStates().contains(newTransition)){
			return true;
		}
		return false;
	}

	private boolean askAndroid(String checker, Map<String, String> membershipQueryResults) {
		
		if(checker.split("\\$").length>2){
			checker = checker.substring(3);
			Iterator<Entry<String, String>> it = membershipQueryResults.entrySet().iterator();
			String matcher = "";
			while(it.hasNext()){
				Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
				String key = pair.getKey();
				String pref[] = key.split("\\$");
				matcher = "";
				for(String prefix: pref){
					if(prefix.contains("<") && prefix.contains(">") && prefix.length()>2){
						matcher+=prefix.split(",")[0].replace("<", "")+"$";
					}else{
						matcher+=prefix;
					}
				}
				
				if(checker.equals(matcher)){
					return true;
				}
			}
		}else if(membershipQueryResults.containsKey(checker)){
			return true;
		}
		return false;
	}

	private int getNewTransitionState(int currentState, String input, AutomatonFromTable hypothesis) {
		for(Transition transition: hypothesis.getTransitions()){
			if(transition.getCurrentState()==currentState && transition.getInput().equals(input)){
				return transition.getNextState();
			}
		}
		return -1;
	}

	public Map<Integer, String> pickRepresentativeStrings(AutomatonFromTable hypothesis, List<String> inputs) {
		Map<Integer, String> representativeStrings = new TreeMap<Integer, String>();
		Queue<Integer> states = new LinkedList<Integer>();
		Set<Integer> statesVisited = new HashSet<>();
		
		representativeStrings.put(0, "<>");
		states.add(0);
		while(!states.isEmpty()){
			int currentState = states.poll();
			if(statesVisited.contains(currentState)){
				continue;
			}
			statesVisited.add(currentState);
			for(String input: inputs){
				int newTransitionState = getNewTransitionState(currentState, input, hypothesis);
				if(!representativeStrings.containsKey(newTransitionState)){
					String newString = representativeStrings.get(currentState)+"$"+input;
					representativeStrings.put(newTransitionState, newString);
					states.add(newTransitionState);
				}
			}
		}
		return representativeStrings;
	}
}

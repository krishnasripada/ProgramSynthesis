package edu.colorado.typestate.lstar.algorithms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.colorado.typestate.lstar.automatarepresentation.AutomatonFromTable;
import edu.colorado.typestate.lstar.automatarepresentation.Transition;
import edu.colorado.typestate.lstar.oracles.EquivalenceOracle;
import edu.colorado.typestate.lstar.oracles.MembershipOracle;
import edu.colorado.typestate.lstar.util.LongestPrefix;

public class LStarAlgorithm {

    private List<String> initialPrefixes;
    private List<String> initialSuffixes;
    private MembershipOracle mqOracle;
    private Map<String, List<String>> observationTableUpper;
    private Map<String, List<String>> observationTableLower;
    private int initialRound;
    private List<String> whatFields = new ArrayList<>();
    private List<String> inputs;
    private List<Integer> errorStates = new ArrayList<>();
    private Map<Integer, List<Transition>> automatonTree;
    private Properties properties = null;
    private int bound = -1;
    private Map<String, String> membershipQueryResults = null;

    public LStarAlgorithm(MembershipOracle mqOracle, List<String> inputs, List<String> initialPrefixes, List<String> initialSuffixes){
        this.initialPrefixes = initialPrefixes;
        this.initialSuffixes = initialSuffixes;
        this.mqOracle = mqOracle;
        this.inputs = inputs;
        observationTableUpper = new TreeMap<String, List<String>>(new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                if (s1.length() < s2.length()) {
                    return -1;
                } else if (s1.length() > s2.length()) {
                    return 1;
                } else {
                    return s1.compareTo(s2);
                }
            }
        });
        observationTableLower = new TreeMap<String, List<String>>(new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                if (s1.length() < s2.length()) {
                    return -1;
                } else if (s1.length() > s2.length()) {
                    return 1;
                } else {
                    return s1.compareTo(s2);
                }
            }
        });
        
        membershipQueryResults = new TreeMap<String, String>(new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                if (s1.length() < s2.length()) {
                    return -1;
                } else if (s1.length() > s2.length()) {
                    return 1;
                } else {
                    return s1.compareTo(s2);
                }
            }
        });
        properties = getProperties();
        if(properties!=null){
        	bound = Integer.parseInt(properties.getProperty("bound"))-2;
        }
        automatonTree = new TreeMap<Integer, List<Transition>>();
        initialRound = 0;
        //Keep adding to this list as more applications are supported
        errorStates.add(100);
    }

    public void getObservationTable(Socket socket, BufferedReader incomingBuffer) {
        int count = 0;
        while(count<initialPrefixes.size()){
            List<String> result = new ArrayList<String>();
            for(int j=0;j<initialSuffixes.size();j++){
                result.add(initialSuffixes.get(j));
                String queryResult = mqOracle.answerQuery(initialPrefixes.get(count), initialSuffixes.get(j), socket);
                String incomingMsg = "";
                try{
                	if(incomingBuffer!=null){
                		while((incomingMsg = incomingBuffer.readLine())!=null){
        					if(incomingMsg.equals("END")){
        						break;
        					}else{
        						if(!whatFields.contains(incomingMsg)){
        							whatFields.add(incomingMsg);
        						}
        					}
        				}
                	}
                	if(queryResult.equals("-")){
                		result.add(queryResult);
                    	System.out.println("Query is!!!!!!!! "+initialPrefixes.get(count)+"$"+initialSuffixes.get(j)+" Result is!!!!! "+queryResult);
                    	membershipQueryResults.put(initialPrefixes.get(count)+"$"+initialSuffixes.get(j), queryResult);
                	}
                	else if(whatFields.size()>0){
                    	String traceRunnerResponse = whatFields.get(whatFields.size()-1);
                    	result.add(traceRunnerResponse);
                    	System.out.println("Query is!!!!!!!! "+initialPrefixes.get(count)+"$"+initialSuffixes.get(j)+" Result is!!!!! "+traceRunnerResponse);
                    	membershipQueryResults.put(initialPrefixes.get(count)+"$"+initialSuffixes.get(j), traceRunnerResponse);
                	}else{
                    	result.add(queryResult);
                    	System.out.println("Query is!!!!!!!! "+initialPrefixes.get(count)+"$"+initialSuffixes.get(j)+" Result is!!!!! "+queryResult);
                    	membershipQueryResults.put(initialPrefixes.get(count)+"$"+initialSuffixes.get(j), queryResult);
                	}
                    whatFields.clear();
                }catch(IOException ioe){
                	System.err.println("Problem getting data from TraceRunner");
                }
            }

            if(observationTableUpper.size()==initialRound){
                if(initialRound==0){
                    observationTableUpper.put(initialPrefixes.get(count), result);
                }
            }else{
                observationTableLower.put(initialPrefixes.get(count), result);
            }
            count+=1;
        }
		/*
		 * Check if the table is closed. If not, move rows and ask membership queries again
		 */
        if(!checkTableClosure()){
            //Move rows between upper and lower part of the table to make it closed
            Map<String, List<String>> keysToAdd = new HashMap<>();
            Set<String> keysUpper = observationTableUpper.keySet();
            Set<String> keysLower = observationTableLower.keySet();
            for(String keyLower: keysLower){
                boolean addOrNot = false;
                List<String> transLower = observationTableLower.get(keyLower);
                for(String keyUpper: keysUpper){
                    List<String> transUpper = observationTableUpper.get(keyUpper);
                    if(transUpper.equals(transLower)){
                        addOrNot = false;
                        break;
                    }else{
                        addOrNot = true;
                    }
                }
                if(addOrNot){
                    keysToAdd.put(keyLower, transLower);
                }
            }

            Map<String, List<String>> rowsMoved = new HashMap<>();
            Set<String> keysAdd = keysToAdd.keySet();
            for(String keys: keysAdd){
                if(!observationTableUpper.containsValue(keysToAdd.get(keys))){
                    observationTableUpper.put(keys, keysToAdd.get(keys));
                    observationTableLower.remove(keys);
                    rowsMoved.put(keys, keysToAdd.get(keys));
                }
            }
            for(String keys: keysAdd){
                observationTableUpper.put(keys, keysToAdd.get(keys));
                observationTableLower.remove(keys);
            }

            // Queries for the new rows moved to the upper part of the table
            List<String> prefixes = getQueryStrings(keysToAdd, socket, incomingBuffer);
            initialPrefixes.clear();
            initialPrefixes.addAll(prefixes);
            initialRound = -1;
            // Repeat the process again till the table is closed
            getObservationTable(socket, incomingBuffer);

        }
        
		/*
		 * Conjecture what is learned. If no is returned, then add the longest prefix to S and remaining suffixes to
		 * E and continue the whole process --> Equivalence
		 */
        
        Iterator<Entry<String, String>> it = membershipQueryResults.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, String> entry = (Map.Entry<String, String>)it.next();
			System.out.println("membershipQueryResults.put("+"\""+entry.getKey()+","+entry.getValue()+"\""+");");
		}
        
        AutomatonFromTable hypothesis = automatonFromTable();
        System.out.println("Bound is "+bound);
        EquivalenceOracle eqOracle = new EquivalenceOracle();
        Map<Integer, String> pickReps = eqOracle.pickRepresentativeStrings(hypothesis, inputs);
        
        boolean done = false;
		while(!done){
			String counterExample = eqOracle.findCounterExample(hypothesis, inputs, bound, pickReps, membershipQueryResults);
			if(counterExample==null){
				System.out.println("No counter examples are generated");
				break;
			}
			else{
				/* Refine the hypothesis by adding the counter example as follows
				 * 1. Longest Prefix to the list of states if not already present
				 * 2. Remaining Suffixes to the list of experiments if not already present
				 * and re-run the algorithm
				 */
				System.out.println("Counter Example is :"+counterExample);
				//The below list is needed for generating longest prefix and suffix
				List<String> prefixAndSuffixWithOutputs = getPrefixAndSuffixWithOutputs(counterExample, automatonTree);
				//Get the output of the counter example by asking Android
				String output = getOutputFromAndroid(counterExample, socket, incomingBuffer);
				String counterExampleWithOutputs = "";
				for(int i=0;i<prefixAndSuffixWithOutputs.size();i++){
					if(i==prefixAndSuffixWithOutputs.size()-1){
						counterExampleWithOutputs+="<"+prefixAndSuffixWithOutputs.get(i)+","+output+">";
					}else{
						counterExampleWithOutputs+=prefixAndSuffixWithOutputs.get(i);
					}
				}
				
				List<String> longestPrefixSuffix = getLongestPrefixSuffix(counterExampleWithOutputs, observationTableUpper, observationTableLower);
				for(int i=0;i<longestPrefixSuffix.size();i++){
					if(i==0){
						String newPrefix = longestPrefixSuffix.get(i);
						boolean result = checkIfNewPrefixAlreadyExists(newPrefix, observationTableLower, observationTableUpper);
						if(!result){
							// Add to new Prefix to the existing list
							initialPrefixes.clear();
							initialPrefixes.add(newPrefix);
						}
					}else{
						String remainingSuffix = longestPrefixSuffix.get(i);
						boolean result = checkIfNewSuffixAlreadyExists(remainingSuffix);
						if(!result){
							// Add to new Suffix to the existing list
							initialSuffixes.add(remainingSuffix);
						}
					}
				}
				getObservationTable(socket, incomingBuffer);
			}
		}
    }
   
	private boolean checkIfNewSuffixAlreadyExists(String remainingSuffix) {
		if(initialSuffixes.contains(remainingSuffix)){
			return true;
		}
		return false;
	}

	private boolean checkIfNewPrefixAlreadyExists(String newPrefix, Map<String, List<String>> observationTableLower,
			Map<String, List<String>> observationTableUpper) {
		
    	Set<String> keysUpper = observationTableUpper.keySet();
    	for(String upper: keysUpper){
    		if(upper.equals(newPrefix)){
    			return true;
    		}
    	}
    	
    	Set<String> keysLower = observationTableLower.keySet();
    	for(String lower: keysLower){
    		if(lower.equals(newPrefix)){
    			return true;
    		}
    	}
		return false;
	}

	public List<String> getLongestPrefixSuffix(String counterExampleWithOutputs, Map<String, List<String>> observationTableUpper, 
    		Map<String, List<String>> observationTableLower) {
    	
    	/*
    	 * Setup for finding the longest prefix and the remaining suffixes
    	 */
    	
    	LongestPrefix longestPrefix = new LongestPrefix();
    	Set<String> keysUpper = observationTableUpper.keySet();
    	for(String upper: keysUpper){
    		longestPrefix.insert(upper);
    	}
    	Set<String> keysLower = observationTableLower.keySet();
    	for(String lower: keysLower){
    		longestPrefix.insert(lower);
    	}
    	List<String> prefixAndSuffix = new ArrayList<>();
    	String longPrefix = longestPrefix.getMatchingPrefix(counterExampleWithOutputs);
    	prefixAndSuffix.add(longPrefix);
    	List<String> remainingSuffixes = getRemainingSuffixes(longPrefix, counterExampleWithOutputs);
		prefixAndSuffix.addAll(remainingSuffixes);
    	return prefixAndSuffix;
	}

	private List<String> getRemainingSuffixes(String longPrefix, String counterExampleWithOutputs) {
		int startIndex = longPrefix.length();
		List<String> remainingSuffixes = new ArrayList<>();
		String remaining = counterExampleWithOutputs.substring(startIndex+1);
		String allremainingSuffixes[] = remaining.split("\\$");
		int i = 0;
		while(i<allremainingSuffixes.length){
			String suffix = "";
			for(int j=i;j<allremainingSuffixes.length;j++){
				suffix+=allremainingSuffixes[j]+"$";
			}
			suffix = suffix.substring(0, suffix.length()-1); //Remove the last delimiter i.e.,$
			remainingSuffixes.add(suffix);
			i++;
		}
		Collections.reverse(remainingSuffixes); //Suffixes are ordered small to big in side. So reversed
		return remainingSuffixes;
	}

	private String getOutputFromAndroid(String counterExample, Socket socket, BufferedReader incomingBuffer) {
    	String prefixes[] = counterExample.split("\\$");
    	String prefix="";
    	String suffix = "";
    	for(int i=1;i<prefixes.length;i++){
    		if(i==prefixes.length-1){
    			suffix = prefixes[i];
    		}else{
    			prefix+=prefixes[i];
    		}
    	}
    	String outputs = mqOracle.answerQuery(prefix, suffix, socket);
        String incomingMsg = "";
        try{
        	if(incomingBuffer!=null){
        		while((incomingMsg = incomingBuffer.readLine())!=null){
					if(incomingMsg.equals("END")){
						break;
					}else{
						if(!whatFields.contains(incomingMsg)){
							whatFields.add(incomingMsg);
						}
					}
				}
        	}
        	if(outputs.equals("-")){
        		whatFields.clear();
        		return outputs;
        	}
        	else if(whatFields.size()>0){
            	String traceRunnerResponse = whatFields.get(whatFields.size()-1);
            	whatFields.clear();
            	return traceRunnerResponse;
        	}else{
        		whatFields.clear();
        		return outputs;
            }
        }catch(IOException ioe){
        	System.err.println("Problem getting data from TraceRunner");
        }
		return null;
	}

	public List<String> getPrefixAndSuffixWithOutputs(String counterExample, Map<Integer, List<Transition>> automatonTree) {
    	String newPrefix="";
    	String suffix="";
    	List<String> result = new ArrayList<>();
    	String prefixes[] = counterExample.split("\\$");
		int currentState = 0;
		for(int i=0;i<prefixes.length;i++){
			if(i==prefixes.length-1){
				suffix=prefixes[i];
			}else if(i!=0){
				List<Transition> initialTrans = automatonTree.get(currentState);
				for(Transition transition: initialTrans){
					if(transition.getInput().equals(prefixes[i])){
						currentState = transition.getNextState();
						newPrefix+="<"+prefixes[i]+","+transition.getOutput()+">$";
					}
				}
			}
		}
		result.add(newPrefix);
		result.add(suffix);
		return result;
	}

	// Reconstructs the automaton from the observation table for finding equivalence
    private AutomatonFromTable automatonFromTable() {
    	AutomatonFromTable hypothesis = new AutomatonFromTable(inputs);
		/*
		 * Number of new states in the hypothesis is the 
		 * number of distinct rows in the upper half of the observation table
		 */
		int noOfNewStates = -1;
		List<List<String>> distinctStates = new ArrayList<>();
		Iterator<Entry<String, List<String>>> it = observationTableUpper.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>)it.next();
			distinctStates.add(entry.getValue());
		}
		noOfNewStates = new HashSet<>(distinctStates).size();
		//Adding those many number of states
		for(int i=0;i<noOfNewStates;i++){
			if(i==0){
				hypothesis.addInitialState();
			}else{
				hypothesis.addState();
			}
		}
		
		System.out.println("No.of new states "+noOfNewStates);
		
		
		List<Transition> prefixStateMap = new ArrayList<>();
		
		//Adding the transitions for those states
		it = observationTableUpper.entrySet().iterator();
		System.out.println("Upper table size "+observationTableUpper);
		int currentMaxState = 0;
		while(it.hasNext()){
			Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>)it.next();
			String prefix = entry.getKey();
			List<String> results = entry.getValue();
			int currentState = getCurrentState(prefix, prefixStateMap);
			System.out.println("Current State "+currentState);
			int nextState;
			if(prefix.contains("<>")){
				for(int i=0;i<results.size();i=i+2){
					Transition transitionDetails = new Transition();
					transitionDetails.setPrefix(prefix);
					transitionDetails.setInput(results.get(i));
					transitionDetails.setOutput(results.get(i+1));
					transitionDetails.setCurrentState(currentState);
					nextState = ++currentMaxState;
					transitionDetails.setNextState(nextState);
					prefixStateMap.add(transitionDetails);
				}
			}else if((nextState = checkErrorState(prefix, prefixStateMap))!=-1){
				for(int i=0;i<results.size();i=i+2){
					Transition transitionDetails = new Transition();
					transitionDetails.setPrefix(prefix);
					transitionDetails.setInput(results.get(i));
					transitionDetails.setOutput(results.get(i+1));
					transitionDetails.setCurrentState(currentState);
					transitionDetails.setNextState(nextState);
					prefixStateMap.add(transitionDetails);
				}
			}else{
				for(int i=0;i<results.size();i=i+2){
					if(!results.get(i+1).equals("-")){
						/*
						 * Check for two start calls and make sure they end up on the same state.
						 * This is Media Player specific and might need to be changed 
						 * add for conditional checks for other examples. 
						 * For Android Media Player, calling start after the first call to start
						 * doesn't lead to a transition to a new state. So that needs to be specifically handled.
						 */
						boolean exceptionCase = false;
						if(prefix.contains("$")){
							int lastIndex = prefix.lastIndexOf("$");
							String lastCall = prefix.substring(lastIndex+1, prefix.length()-1);
							if(lastCall.contains("start()") && results.get(i).contains("start()")){
								Transition transitionDetails = new Transition();
								transitionDetails.setPrefix(prefix);
								transitionDetails.setInput(results.get(i));
								transitionDetails.setOutput(results.get(i+1));
								transitionDetails.setCurrentState(currentState);
								int result = checkErrorState("<"+results.get(i)+","+results.get(i+1)+">", prefixStateMap);
								if(result!=-1){
									System.out.println("Error state is: "+result);
									nextState = result;
								}else{
									nextState = currentState;
								}
								transitionDetails.setNextState(nextState);
								prefixStateMap.add(transitionDetails);
								exceptionCase = true;
							}
						}if(!exceptionCase){
							Transition transitionDetails = new Transition();
							transitionDetails.setPrefix(prefix);
							transitionDetails.setInput(results.get(i));
							transitionDetails.setOutput(results.get(i+1));
							transitionDetails.setCurrentState(currentState);
							int result = checkErrorState("<"+results.get(i)+","+results.get(i+1)+">", prefixStateMap);
							if(result!=-1){
								System.out.println("Error state is: "+result);
								nextState = result;
							}else{
								nextState = ++currentMaxState;
							}
							transitionDetails.setNextState(nextState);
							prefixStateMap.add(transitionDetails);
						}
					}else{
						nextState = currentState;
						Transition transitionDetails = new Transition();
						transitionDetails.setPrefix(prefix);
						transitionDetails.setInput(results.get(i));
						transitionDetails.setOutput(results.get(i+1));
						transitionDetails.setCurrentState(currentState);
						transitionDetails.setNextState(nextState);
						prefixStateMap.add(transitionDetails);
					}
				}
			}
		}
		
		/*
		 * Add all the final transitions to the automaton object
		 */
		for(Transition transition: prefixStateMap){
			hypothesis.addTransition(transition);
		}
		
		/*
		 * Populate the Tree representation of the Automaton by doing a breadth-first search
		 */
		int i = 0;
		while(i<hypothesis.getNumberStates()){
			List<Transition> successorDetails = new ArrayList<>();
			for(Transition transition: prefixStateMap){
				int startState = transition.getCurrentState();
				if(startState==i){
					successorDetails.add(transition);
					automatonTree.put(startState, successorDetails);
				}
			}
			i++;
		}
		
		System.out.println(automatonTree);
		return hypothesis;
	}
    
    //TODO: Remove. This is just for unit testing
    public AutomatonFromTable automatonFromTable(Map<String, List<String>> observationTableUpper) {
		AutomatonFromTable hypothesis = new AutomatonFromTable(inputs);
		/*
		 * Number of new states in the hypothesis is the 
		 * number of distinct rows in the upper half of the observation table
		 */
		int noOfNewStates = -1;
		List<List<String>> distinctStates = new ArrayList<>();
		Iterator<Entry<String, List<String>>> it = observationTableUpper.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>)it.next();
			distinctStates.add(entry.getValue());
		}
		noOfNewStates = new HashSet<>(distinctStates).size();
		//Adding those many number of states
		for(int i=0;i<noOfNewStates;i++){
			if(i==0){
				hypothesis.addInitialState();
			}else{
				hypothesis.addState();
			}
		}
		
		System.out.println("No.of new states "+noOfNewStates);
		
		List<Transition> prefixStateMap = new ArrayList<>();
		
		//Adding the transitions for those states
		it = observationTableUpper.entrySet().iterator();
		System.out.println("Upper table size "+observationTableUpper);
		int currentMaxState = 0;
		while(it.hasNext()){
			Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>)it.next();
			String prefix = entry.getKey();
			List<String> results = entry.getValue();
			int currentState = getCurrentState(prefix, prefixStateMap);
			System.out.println("Current State "+currentState);
			int nextState;
			if(prefix.contains("<>")){
				for(int i=0;i<results.size();i=i+2){
					Transition transitionDetails = new Transition();
					transitionDetails.setPrefix(prefix);
					transitionDetails.setInput(results.get(i));
					transitionDetails.setOutput(results.get(i+1));
					transitionDetails.setCurrentState(currentState);
					nextState = ++currentMaxState;
					transitionDetails.setNextState(nextState);
					prefixStateMap.add(transitionDetails);
				}
			}else if((nextState = checkErrorState(prefix, prefixStateMap))!=-1){
				for(int i=0;i<results.size();i=i+2){
					Transition transitionDetails = new Transition();
					transitionDetails.setPrefix(prefix);
					transitionDetails.setInput(results.get(i));
					transitionDetails.setOutput(results.get(i+1));
					transitionDetails.setCurrentState(currentState);
					transitionDetails.setNextState(nextState);
					prefixStateMap.add(transitionDetails);
				}
			}else{
				for(int i=0;i<results.size();i=i+2){
					if(!results.get(i+1).equals("-")){
						/*
						 * Check for two start calls and make sure they end up on the same state.
						 * This is Media Player specific and might need to be changed 
						 * add for conditional checks for other examples. 
						 * For Android Media Player, calling start after the first call to start
						 * doesn't lead to a transition to a new state. So that needs to be specifically handled.
						 */
						boolean exceptionCase = false;
						if(prefix.contains("$")){
							int lastIndex = prefix.lastIndexOf("$");
							String lastCall = prefix.substring(lastIndex+1, prefix.length()-1);
							if(lastCall.contains("start()") && results.get(i).contains("start()")){
								Transition transitionDetails = new Transition();
								transitionDetails.setPrefix(prefix);
								transitionDetails.setInput(results.get(i));
								transitionDetails.setOutput(results.get(i+1));
								transitionDetails.setCurrentState(currentState);
								int result = checkErrorState("<"+results.get(i)+","+results.get(i+1)+">", prefixStateMap);
								if(result!=-1){
									System.out.println("Error state is: "+result);
									nextState = result;
								}else{
									nextState = currentState;
								}
								transitionDetails.setNextState(nextState);
								prefixStateMap.add(transitionDetails);
								exceptionCase = true;
							}
						}if(!exceptionCase){
							Transition transitionDetails = new Transition();
							transitionDetails.setPrefix(prefix);
							transitionDetails.setInput(results.get(i));
							transitionDetails.setOutput(results.get(i+1));
							transitionDetails.setCurrentState(currentState);
							int result = checkErrorState("<"+results.get(i)+","+results.get(i+1)+">", prefixStateMap);
							if(result!=-1){
								System.out.println("Error state is: "+result);
								nextState = result;
							}else{
								nextState = ++currentMaxState;
							}
							transitionDetails.setNextState(nextState);
							prefixStateMap.add(transitionDetails);
						}
					}else{
						nextState = currentState;
						Transition transitionDetails = new Transition();
						transitionDetails.setPrefix(prefix);
						transitionDetails.setInput(results.get(i));
						transitionDetails.setOutput(results.get(i+1));
						transitionDetails.setCurrentState(currentState);
						transitionDetails.setNextState(nextState);
						prefixStateMap.add(transitionDetails);
					}
				}
			}
		}
		
		/*
		 * Add all the final transitions to the automaton object
		 */
		for(Transition transition: prefixStateMap){
			hypothesis.addTransition(transition);
		}
		
		/*
		 * Populate the Tree representation of the Automaton by doing a breadth-first search
		 */
		int i = 0;
		while(i<hypothesis.getNumberStates()){
			List<Transition> successorDetails = new ArrayList<>();
			for(Transition transition: prefixStateMap){
				int startState = transition.getCurrentState();
				if(startState==i){
					successorDetails.add(transition);
					automatonTree.put(startState, successorDetails);
				}
			}
			i++;
		}
		
		System.out.println(automatonTree);
		return hypothesis;
	}

    private int checkErrorState(String errorString, List<Transition> prefixStateMap) {
    	int result = -1;
		for(Transition transition: prefixStateMap){
			String checker = "<"+transition.getInput()+","+transition.getOutput()+">";
			if(checker.equals(errorString)){
				for(Integer error: errorStates){
					if(errorString.contains(String.valueOf(error))){
						return transition.getNextState();
					}
				}
			}
		}
    	return result;
	}

	private int getCurrentState(String prefix, List<Transition> prefixStateMap) {
    	int currentState = 0;
		for(Transition transition: prefixStateMap){
			String checker = "<"+transition.getInput()+","+transition.getOutput()+">";
			if(prefix.contains("$")){
	    		String check = transition.getPrefix()+"$"+checker;
	    		if(check.equals(prefix)){
	    			return transition.getNextState();
	    		}
	    	}else{
	    		if(checker.equals(prefix)){
	    			return transition.getNextState();
	    		}
	    	}
		}
		return currentState;
	}

	private boolean checkTableClosure() {
        Set<String> keysUpper = observationTableUpper.keySet();
        Set<String> keysLower = observationTableLower.keySet();
        boolean closed = false;
        for(String keyLower: keysLower){
            closed = false;
            List<String> transLower = observationTableLower.get(keyLower);
            for(String keyUpper: keysUpper){
                List<String> transUpper = observationTableUpper.get(keyUpper);
                if(transUpper.equals(transLower)){
                    closed = true;
                    break;
                }else{
                    closed = false;
                }
            }
            if(!closed){
                break;
            }
        }
        return closed;
    }
    private List<String> getQueryStrings(Map<String, List<String>> keysToAdd, Socket socket, BufferedReader incomingBuffer) {
        List<String> prefixes = new ArrayList<>();
        Set<String> keysAdd = keysToAdd.keySet();
        for(String keys: keysAdd){
            for(int j=0;j<initialSuffixes.size();j++){
                String suffix = initialSuffixes.get(j);
                String temp = keys.split(",")[0];
                temp = temp.replace("<","");
                String outputs = mqOracle.answerQuery(temp, suffix, socket);
                String incomingMsg = "";
                try{
                	if(incomingBuffer!=null){
                		while((incomingMsg = incomingBuffer.readLine())!=null){
        					if(incomingMsg.equals("END")){
        						break;
        					}else{
        						if(!whatFields.contains(incomingMsg)){
        							whatFields.add(incomingMsg);
        						}
        					}
        				}
                	}
                	if(outputs.equals("-")){
                		System.out.println("Query is!!!!!!!! "+temp +"$"+initialSuffixes.get(j)+" Result is!!!!! "+outputs);
                		prefixes.add(keys+"$<"+suffix+","+outputs+">");
                	}
                	else if(whatFields.size()>0){
                    	String traceRunnerResponse = whatFields.get(whatFields.size()-1);
                    	System.out.println("Query is!!!!!!!! "+temp +"$"+initialSuffixes.get(j)+" Result is!!!!! "+traceRunnerResponse);
                    	prefixes.add(keys+"$<"+suffix+","+traceRunnerResponse+">");
                	}else{
                		System.out.println("Query is!!!!!!!! "+temp +"$"+initialSuffixes.get(j)+" Result is!!!!! "+outputs);
                		prefixes.add(keys+"$<"+suffix+","+outputs+">");
                    }
                    whatFields.clear();
                }catch(IOException ioe){
                	System.err.println("Problem getting data from TraceRunner");
                }
            }
        }
        return prefixes;
    }

    public void printObservationTable() {
        Set<String> keysUpper = observationTableUpper.keySet();
        Set<String> keysLower = observationTableLower.keySet();
        for(String key: keysLower){
            List<String> rowHeadings = observationTableLower.get(key);
            for(int i=0;i<rowHeadings.size();i=i+2){
                System.out.print("\t|"+rowHeadings.get(i)+"|");
            }
            System.out.println("\n");
            System.out.println("========================================================================================================================");
            break;
        }
        for(String key: keysUpper){
            List<String> rows = observationTableUpper.get(key);
            for(int i=1;i<rows.size();i=i+2){
                if(i==1){
                    System.out.print(key+"\t"+"|"+rows.get(i)+"|");
                }else{
                    System.out.print("\t"+"|"+rows.get(i)+"|");
                }

            }
            System.out.println("\n");
        }
        System.out.println("==========================================================================================================================");
        for(String key: keysLower){
            List<String> rows = observationTableLower.get(key);
            for(int i=1;i<rows.size();i=i+2){
                if(i==1){
                    System.out.print(key+"\t"+"|"+rows.get(i)+"|");
                }else{
                    System.out.print("\t"+"|"+rows.get(i)+"|");
                }

            }
            System.out.println("\n");
        }
    }
    
    private static Properties getProperties() {
		Properties properties = new Properties();
		InputStream propertyFileStream = null;
		try {
			propertyFileStream = new FileInputStream("config.properties");
			properties.load(propertyFileStream);
		} catch (FileNotFoundException e) {
			System.err.println("Property File not found");
		} catch (IOException e) {
			System.err.println("Property File Exception");
		} finally{
			if(propertyFileStream!=null){
				try {
					propertyFileStream.close();
				} catch (IOException e) {
					System.err.println("Property File Closing Exception");
				}
			}
		}
		return properties;
	}

    //TODO: For testing purpose. Can remove it
	public Map<String, String> getMembershipQueryResults() {
		return membershipQueryResults;
	}

	public Map<Integer, List<Transition>> getAutomatonTree() {
		return automatonTree;
	}

	public int getBound() {
		return bound;
	}	
}
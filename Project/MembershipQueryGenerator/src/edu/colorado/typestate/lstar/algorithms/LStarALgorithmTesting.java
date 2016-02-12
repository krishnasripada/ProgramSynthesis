package edu.colorado.typestate.lstar.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.colorado.typestate.lstar.automatarepresentation.AutomatonFromTable;
import edu.colorado.typestate.lstar.oracles.EquivalenceOracle;
import edu.colorado.typestate.lstar.oracles.MembershipOracle;

public class LStarALgorithmTesting {

	public static void main(String[] args) {
		MembershipOracle mqOracle = new MembershipOracle();
		List<String> inputs = new ArrayList<>();
		List<String> initialPrefixes = new ArrayList<>();
		List<String> initialSuffixes = new ArrayList<>();
		
		inputs.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		inputs.add("obj.prepareAsync();");
		inputs.add("obj.start();");
		
		initialPrefixes.add("<>");
		initialPrefixes.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		initialPrefixes.add("obj.prepareAsync();");
		initialPrefixes.add("obj.start();");
		
		initialSuffixes.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		initialSuffixes.add("obj.prepareAsync();");
		initialSuffixes.add("obj.start();");
		
		LStarAlgorithm lstar = new LStarAlgorithm(mqOracle, inputs, initialPrefixes, initialSuffixes);
		Map<String, List<String>> observationTableUpper = new TreeMap<String, List<String>>(new Comparator<String>(){
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
		
		List<String> elements = new ArrayList<>();
		elements.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		elements.add("+");
		elements.add("obj.prepareAsync();");
		elements.add("-");
		elements.add("obj.start();");
		elements.add("100");
		observationTableUpper.put("<>",elements);
		elements = new ArrayList<>();
		elements.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		elements.add("100");
		elements.add("obj.prepareAsync();");
		elements.add("-");
		elements.add("obj.start();");
		elements.add("100");
		observationTableUpper.put("<obj.start();,100>",elements);
		elements = new ArrayList<>();
		elements.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		elements.add("-");
		elements.add("obj.prepareAsync();");
		elements.add("-");
		elements.add("obj.start();");
		elements.add("-");
		observationTableUpper.put("<obj.prepareAsync();,->",elements);
		elements = new ArrayList<>();
		elements.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		elements.add("-");
		elements.add("obj.prepareAsync();");
		elements.add("1");
		elements.add("obj.start();");
		elements.add("100");
		observationTableUpper.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>",elements);
		elements = new ArrayList<>();
		elements.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		elements.add("-");
		elements.add("obj.prepareAsync();");
		elements.add("-");
		elements.add("obj.start();");
		elements.add("1");
		observationTableUpper.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>",elements);
		elements = new ArrayList<>();
		elements.add("obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');");
		elements.add("100");
		elements.add("obj.prepareAsync();");
		elements.add("-");
		elements.add("obj.start();");
		elements.add("1");
		observationTableUpper.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,1>",elements);
		elements = new ArrayList<>();
		
		System.out.println(observationTableUpper.size());
		
		Map<String, String> membershipQueryResults = new TreeMap<String, String>(new Comparator<String>(){
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
		membershipQueryResults.put("<>$obj.start();","100");
		membershipQueryResults.put("<>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.start();,100>$obj.start();","100");
		membershipQueryResults.put("<obj.prepareAsync();,->$obj.start();","-");
		membershipQueryResults.put("<obj.start();,100>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.start();,100>$<obj.start();,100>$obj.start();","100");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.start();,->$obj.start();","-");
		membershipQueryResults.put("<obj.start();,100>$<obj.prepareAsync();,->$obj.start();","-");
		membershipQueryResults.put("<obj.start();,100>$<obj.start();,100>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.prepareAsync();,->$obj.start();","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.start();,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.start();,100>$<obj.prepareAsync();,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.prepareAsync();,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","+");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$obj.start();","100");
		membershipQueryResults.put("<obj.start();,100>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","100");
		membershipQueryResults.put("<obj.prepareAsync();,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$obj.prepareAsync();","1");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.start();,100>$obj.start();","100");
		membershipQueryResults.put("<obj.start();,100>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,100>$obj.start();","100");
		membershipQueryResults.put("<obj.start();,100>$<obj.start();,100>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","100");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.start();","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.start();,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$obj.start();","1");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.start();,100>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.start();,100>$<obj.prepareAsync();,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.start();,100>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,100>$obj.prepareAsync();","1");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.prepareAsync();,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$obj.start();","1");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.prepareAsync();,1>$obj.start();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.prepareAsync();,1>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.start();,100>$obj.start();","1");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.prepareAsync();,1>$obj.start();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.start();,100>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.prepareAsync();,1>$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.start();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.start();,100>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","100");
		membershipQueryResults.put("<obj.start();,100>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,100>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.prepareAsync();,->$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.start();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","100");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.prepareAsync();,1>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.prepareAsync();","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.start();","100");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.start();,100>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","100");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.prepareAsync();,1>$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.prepareAsync();","1");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");
		membershipQueryResults.put("<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,+>$<obj.prepareAsync();,1>$<obj.start();,100>$<obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');,->$obj.setDataSource('https://csel.cs.colorado.edu/~krsr8608/panjatheme.mp3');","-");

		AutomatonFromTable hypothesis = lstar.automatonFromTable(observationTableUpper);
		EquivalenceOracle eqOracle = new EquivalenceOracle();
		Map<Integer, String> pickReps = eqOracle.pickRepresentativeStrings(hypothesis, inputs);
		boolean done = false;
		while(!done){
			String counterExample = eqOracle.findCounterExample(hypothesis, inputs, 10, pickReps, membershipQueryResults);
			if(counterExample==null){
				System.out.println("No counter examples are generated");
				break;
			}else{
				System.out.println("Counter Example is :"+counterExample);
				done = true;
			}
		}
	}
}

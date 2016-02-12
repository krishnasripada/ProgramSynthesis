package edu.colorado.typestate.lstar.oracles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MembershipOracle {

    public MembershipOracle(){}
    
    public String answerQuery(String prefix, String suffix, Socket socket){
    	return getQueryResult(prefix + "$" + suffix, socket);
    }
    
    public String getQueryResult(final String membershipQuery, Socket socket){
    	String  returnMessage = null;
    	try{
    		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
    		System.out.println("Flushed input "+membershipQuery);
    		printWriter.println(membershipQuery);
    		
    		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    		while((returnMessage = in.readLine())==null){}
    		System.out.println("Output: "+returnMessage);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return returnMessage;
    }
}


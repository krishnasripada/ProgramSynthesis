package edu.colorado.cs.mqgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.colorado.typestate.lstar.algorithms.LStarAlgorithm;
import edu.colorado.typestate.lstar.oracles.MembershipOracle;

public class MembershipQueryGenerator {

	private static final int PORT = 38308;
	private static final int TRPORT = 38310;
	private static final String COMMAND[] = new String[]{"adb", "forward" ,"tcp:"+PORT ,"tcp:"+PORT};
	private static Socket socket = null;
	private static ServerSocket trSocket = null;
	
	private static void establishTCPPortForwarding(){
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(COMMAND);
			processBuilder.redirectErrorStream(true);
			processBuilder.directory(new File("."));
			processBuilder.start();			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	public static void main(String[] args) throws Exception {
		establishTCPPortForwarding();
		socket = new Socket("localhost", PORT);
		trSocket = new ServerSocket(TRPORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		RunLStar lstar = new RunLStar(socket);
		Scanner scanner = new Scanner(System.in);
		boolean once = true;
		Socket incoming = null;
		BufferedReader incomingBuffer = null;
		List<String> whatFields = new ArrayList<>();
		System.out.print("Enter the command: ");
		while(scanner.hasNext()){
			String query = scanner.nextLine();
			if(query.contains("END")){
				lstar.setIncomingBuffer(incomingBuffer);
				Thread lstarTh = new Thread(lstar);
				lstarTh.start();
			}else{
				System.out.println("Query is "+query);
				//Form a socket connection and send this query
				printWriter.println(query);
				//Form a Server socket and read the trace runner's response
				String buffer = "";
				String incomingMsg = "";
				if(once){
					incoming = trSocket.accept();
					incomingBuffer = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
					once = false;
				}
				if(incoming!=null && incomingBuffer!=null){
					while((incomingMsg = incomingBuffer.readLine())!=null){
						System.out.println("Response from TraceRunner is: "+incomingMsg);
						if(incomingMsg.equals("END")){
							break;
						}else{
							if(!whatFields.contains(incomingMsg)){
								whatFields.add(incomingMsg);
							}
						}
					}
				}
				while((buffer=in.readLine())!=null){
					System.out.println("Response from Phone is: "+buffer);
					if(buffer.length()>0){
						break;
					} 
				}
				
				//Populate data to run LStar Algorithm
				if(!query.contains("=")){
					if(whatFields.size()>0){
						String traceRunnerResponse = whatFields.get(whatFields.size()-1);
						lstar.populateInitialPrefixes(query, traceRunnerResponse);
					}else{
						lstar.populateInitialPrefixes(query, buffer);
					}
					whatFields.clear();
					lstar.populateInputs(query);
					lstar.populateInitialSuffixes(query);
					lstar.dataCollected();
				}
				
				System.out.print("Enter the command: ");
			}
			
		}
		try {
			if(socket!=null){
				socket.close();
			}if(trSocket!=null){
				trSocket.close();
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class RunLStar implements Runnable{

	private List<String> initialPrefixes = new ArrayList<>();
	private List<String> initialSuffixes = new ArrayList<>();
	private List<String> inputs = new ArrayList<>();
	private Socket socket = null;
	private BufferedReader incomingBuffer = null;
	
	public RunLStar(Socket socket){
		initialPrefixes.add("<>");
		this.socket = socket;
	}
	@Override
	public void run() {
		System.out.println("LStar called");
		MembershipOracle membershipOracle = new MembershipOracle();
		LStarAlgorithm lStarAlgorithm = new LStarAlgorithm(membershipOracle, inputs, initialPrefixes, initialSuffixes);
		lStarAlgorithm.getObservationTable(socket, getIncomingBuffer());
		lStarAlgorithm.printObservationTable();
	}
	
	
	public BufferedReader getIncomingBuffer() {
		return incomingBuffer;
	}
	
	public void setIncomingBuffer(BufferedReader incomingBuffer) {
		this.incomingBuffer = incomingBuffer;
	}
	
	public void populateInitialPrefixes(String query, String outputs){
		initialPrefixes.add("<"+query+","+outputs+">");
	}
	
	public void populateInputs(String query){
		inputs.add(query);
	}
	
	public void populateInitialSuffixes(String query){
		initialSuffixes.add(query);
	}
	
	//TODO: Remove Later
	public void dataCollected(){
		System.out.println(initialPrefixes);
		System.out.println(initialSuffixes);
	}
}

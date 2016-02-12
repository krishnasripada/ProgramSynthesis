package edu.colorado.typestate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import edu.colorado.tracerunner.TraceRunner;

public class TypestateWithTraceRunner {
	
	public static void main(String[] args) {
		try{
			Thread tracerRunner = new Thread(new RunTraceRunner(getProperties()));
			tracerRunner.start();
		}catch(Exception e){
			e.printStackTrace();
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
}

class RunTraceRunner implements Runnable{
	
	private static final String NSPORT = "19422";
	private static final String CMDPORT = "38310";
	private static final String HOSTNAME="localhost";
	private Properties properties = null;
	
	static{
		String params[] = new String[2];
		params[0] = NSPORT;
		PySystemState state = new PySystemState();
		state.argv.append (new PyString(params[0]));
		PythonInterpreter interp = new PythonInterpreter(null, state);
		String scriptname = "messageTrace.py";
		interp.execfile(scriptname);
		interp.close();
	}
	
	public RunTraceRunner(Properties properties) {
		this.properties = properties;
	}

	@Override
	public void run() {
		try {
			TraceRunner tr = new TraceRunner();
			List<String> filters = new ArrayList<>();
			int waitTimeForCallbackInSeconds = 15;
			if(properties!=null){
				String allfilters = properties.getProperty("filters");
				waitTimeForCallbackInSeconds = Integer.parseInt(properties.getProperty("waitTimeForCallbackInSeconds"));
				String data[] = allfilters.split(",");
				for(int i=0;i<data.length;i++){
					filters.add(data[i]);
				}
			}
			tr.executeTracerRunner(HOSTNAME, NSPORT, CMDPORT, filters, waitTimeForCallbackInSeconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

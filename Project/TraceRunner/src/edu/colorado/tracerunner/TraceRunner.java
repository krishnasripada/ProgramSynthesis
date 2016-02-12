package edu.colorado.tracerunner;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;


/**
 * Created by s on 8/11/15.
 * This is the record everything trace runner, it will probably be too slow and need refactoring.
 * TODO: In progress
 */
public class TraceRunner {
	public static List<String> getfields(){
        List<String> out = new ArrayList<>();
        out.add("what");
        out.add("Message.when");
        out.add("target");
        out.add("callback");
        return out;
    }
    public void executeTracerRunner(String hostname, String nsport, String cmdport, List<String> filters, int waitTimeForCallbackInSeconds) throws Exception
    {
    	List<String> mentered = new ArrayList<>();
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector socketConnector = null;
        List<AttachingConnector> attachingConnectors = vmMgr.attachingConnectors();
        for (AttachingConnector ac: attachingConnectors){
            if (ac.transport().name().equals("dt_socket")){
            	socketConnector = ac;
            	break;
            }
        }
        if (socketConnector != null){
            Map paramsMap = socketConnector.defaultArguments();
            Connector.IntegerArgument portArg = (Connector.IntegerArgument)paramsMap.get("port");
            portArg.setValue(Integer.parseInt(nsport));
            Connector.Argument hostName = (Connector.Argument)paramsMap.get("hostname");
            hostName.setValue(hostname);
            VirtualMachine vm = socketConnector.attach(paramsMap);
            System.out.println("Attached to process '" + vm.name() + "'");
            
            Method dispatchMessage = getDispatchMessage(vm);
            Location breakpointLocation = dispatchMessage.location();
            EventRequestManager evtReqMgr = vm.eventRequestManager();
            BreakpointRequest bReq = evtReqMgr.createBreakpointRequest(breakpointLocation);
            bReq.setSuspendPolicy(BreakpointRequest.SUSPEND_ALL);
            bReq.enable();
            
            for(String filter : filters) {
            	MethodEntryRequest methodEntryRequest = evtReqMgr.createMethodEntryRequest();
                methodEntryRequest.addClassFilter(filter);
                methodEntryRequest.enable();  
            }
            
            EventQueue evtQueue = vm.eventQueue();
            //Form a socket connection 
            Socket socket = new Socket("localhost", Integer.parseInt(cmdport));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            
            final List<Boolean> flags = new ArrayList<>();
            flags.add(false);
        	Timer timer = new Timer();
            
            try{
	            while(true) {
	            	if(!flags.get(0)){
	            		flags.set(0, true);
	            		timer.schedule(new TimerTask() {
	        				@Override
	        				public void run() {
	        					//Terminate Sending messages
	        					StringBuilder sb = new StringBuilder();
	        					sb.append("END");
	        					printWriter.println(sb.toString());
	                            flags.set(0, false);
	        				}
	        			}, waitTimeForCallbackInSeconds*1000);
	            	}
	            	
	            	EventSet evtSet = evtQueue.remove();
	                EventIterator evtIter = evtSet.eventIterator();
	                
	                while(evtIter.hasNext()) {
	                    try{
	                    	Event evt = evtIter.next();
	                    	Map<String, Value> eventDetails = processEvent(evt, mentered);
	                    	StringBuilder sb = new StringBuilder();
	                    	List<String> whatFieldValues = new ArrayList<>();
	                    	if(eventDetails.size()>0){
	                    		System.out.println(eventDetails);
	                    		Iterator<Map.Entry<String, Value>> it = eventDetails.entrySet().iterator();
	                            while(it.hasNext()){
	                                Map.Entry<String, Value> pair = it.next();
	                                if(pair.getKey().equals("what")){
	                                	whatFieldValues.add(pair.getValue().toString());
	                                	sb.append(pair.getValue());
	                                }else if(pair.getKey().equals("target")){
	                                	String instance = pair.getValue().toString();
	                                	for(String filter : filters) {
	                                		filter = filter.replace("*", "");
	                                		if(!instance.contains(filter)){
	                                			whatFieldValues.clear();
	                                		}else{
	                                			break;
	                                		}
	                                	}
	                                }
	                            }
	                            if(whatFieldValues.size()>0){
	                            	System.out.println("TraceRunner writes "+sb.toString());
	                                printWriter.println(sb.toString());
	                            }
                            }
                            mentered.clear();
	                    }
	
	                    catch (Exception exc){
	                        System.out.println(exc.getClass().getName() + ": " + exc.getMessage());
	                    }
	                    finally{
	                        evtSet.resume();
	                    }
	                }
	            }
            }catch (VMDisconnectedException e){
                System.err.println("Process exited");
            }
        }
    }
    
    private static Method getDispatchMessage(VirtualMachine vm) {
        List<ReferenceType> refTypes = vm.allClasses();
        ReferenceType handlerClass = null;
        for (ReferenceType refType: refTypes) {
            if (refType.name().equals("android.os.Handler")) {
                if (handlerClass != null) {
                    throw new IllegalStateException("more than one handler found");
                }else{
                    handlerClass = refType;
                }
            }
        }
        List<Method> methods = handlerClass.allMethods();
        Method dispatchMessage = null;
        for(Method method : methods){
            if(method.name().equals("dispatchMessage")){
                if(method.signature().equals("(Landroid/os/Message;)V")){
                    if(dispatchMessage != null){
                        throw new IllegalStateException("");
                    }
                    dispatchMessage = method;
                }

            }
        }
        return dispatchMessage;
    }
    
    private static Map<String, Value> processEvent(Event evt, List<String> mentered) throws IncompatibleThreadStateException, AbsentInformationException {
        Map<String, Value> retrievedValues = new HashMap<>();
        EventRequest evtReq = evt.request();
        if (evtReq instanceof BreakpointRequest) {
            BreakpointEvent brEvt = (BreakpointEvent) evt;
            ThreadReference threadRef = brEvt.thread();
            StackFrame stackFrame = threadRef.frame(0);
            List visVars = stackFrame.visibleVariables();
            LocalVariable msgvar = (LocalVariable) visVars.get(0); //Should only be one
            Value value = stackFrame.getValue(msgvar);
            List<Field> msgFields = null;
            if (value instanceof ObjectReference) {
                ObjectReference ovalue = (ObjectReference) value;
                msgFields = ovalue.referenceType().allFields();
                List<String> recordedFields = getfields();
                for (String sf : recordedFields) {
                    for (Field f : msgFields) {
                        if (f.name().equals(sf)) {
                            //Get value and add to return obj
                            Value v = ovalue.getValue(f);
                            retrievedValues.put(sf, v);

                        }
                    }
                }
            }
        }else{
            if(evt instanceof MethodEntryEvent) {
                MethodEntryEvent mer = (MethodEntryEvent) evt;
                mentered.add(mer.method().toString());
                //System.out.println("method entry: " + mer.method().toString());
            }
        }
        return retrievedValues;
    }
}

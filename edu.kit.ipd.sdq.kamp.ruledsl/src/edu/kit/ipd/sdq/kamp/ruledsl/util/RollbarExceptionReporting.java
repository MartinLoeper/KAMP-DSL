package edu.kit.ipd.sdq.kamp.ruledsl.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import com.rollbar.Rollbar;
import com.rollbar.payload.Payload;
import com.rollbar.payload.data.Data;
import com.rollbar.payload.data.Level;
import com.rollbar.payload.data.Person;
import com.rollbar.payload.data.Server;
import com.rollbar.payload.data.body.Body;
import com.rollbar.sender.ConnectionFailedException;
import com.rollbar.sender.RollbarResponse;

import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageFacade;
import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleLanguageUtil;

public enum RollbarExceptionReporting {
	INSTANCE;
	
	private static final String SERVER_POST_ACCESS_TOKEN = "1fcb2a1840dc4bf48e7233b219ce9d28";
	private static final String ENVIRONMENT = "research";
	private static final String PLATFORM = "KAMP";
	private final String VERSION;
	
	private final Rollbar rollbar = new Rollbar(SERVER_POST_ACCESS_TOKEN, ENVIRONMENT);
    private boolean init = false;
    
    private RollbarExceptionReporting() {
    	String version = "unknown";
		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
			while(resources.hasMoreElements()) {
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				String nameAttribute = manifest.getMainAttributes().getValue("Bundle-Name");
				if(nameAttribute != null && nameAttribute.equals("edu.kit.ipd.sdq.kamp.ruledsl")) {
					version = manifest.getMainAttributes().getValue("Bundle-Version");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			VERSION = version;
		}
    }
    
    /**
     * Sends the given throwable with additional information to the Rollbar server.
     * This method is non-blocking and returns immediately, after creating and starting the background thread.
     * 
     * @param t the Throwable which was thrown
     * @param errorContext the context in which the Throwable was thrown; this may be null if none of the ErrorContext constants is applicable
     * @param custom some custom key -> value string data which is appended to the log; null if nothing should be appended
     */
    public void log(final Throwable t, ErrorContext errorContext, Map<String, Object> custom) {    	
    	// do it in background
    	System.out.println("Starting background thread for error reporting...");
    	new Thread(() -> {
	    	// create the frames with a custom implementation in order to avoid errors
	    	Throwable safeThrowable = new Throwable(t.getMessage(), t.getCause());
	    	StackTraceElement[] stackTrace = new StackTraceElement[t.getStackTrace().length];
	    	for(int i = 0; i < t.getStackTrace().length; i++) {
	    		StackTraceElement cStackTElement = t.getStackTrace()[i];
	    		String filename = (cStackTElement.getFileName() == null) ? "<unknown>" : cStackTElement.getFileName();
	    		stackTrace[i] = new StackTraceElement(cStackTElement.getClassName(), cStackTElement.getMethodName(), filename, cStackTElement.getLineNumber());
	    	}
	    	
	    	safeThrowable.setStackTrace(stackTrace);

	    	String errorFileName = "<unknown>";
	    	if(t.getStackTrace().length > 0 && t.getStackTrace()[0].getFileName() != null) {
	    		errorFileName = t.getStackTrace()[0].getFileName();
	    	}
	    	
	    	String msg = "<no-msg>";
	    	if(t.getMessage() != null && t.getMessage().length() > 0) {
	    		msg = t.getMessage();
	    	}
	    	
	    	String title = t.getClass().getCanonicalName() + " [" + errorFileName + "]";
	    	if(title.length() >= 250) {
	    		title = getTruncated(title, 250);
	    	}
	    	
	    	String context = (errorContext != null) ? errorContext.toString() : null;
	    	
	    	Person person = null;
	    	if(System.getProperty("user.name") != null)
	    		person = new Person(System.getProperty("user.name"), System.getProperty("user.name"), null);
	    	
	    	Map<String, Object> allCustomData = new HashMap<>();
	    	if(custom != null)
	    		allCustomData.putAll(custom);
	    	if(System.getProperty("os.name") != null)
	    		allCustomData.put("os.name", System.getProperty("os.name"));
	    	
	    	Data data = new Data(ENVIRONMENT, Body.fromError(safeThrowable, msg), Level.ERROR, new Date(), VERSION, PLATFORM, "java", "KAMP", context, null, person, null, custom, null, title, null, null);
			Payload p = new Payload(SERVER_POST_ACCESS_TOKEN, data);
	       
			// Here you can filter or transform the payload as needed before sending it
	        RollbarResponse response = rollbar.getSender().send(p);
	        if(!response.isSuccessful()) {
	        	System.err.println("Rollbar Error Reporting failed: " + response.errorMessage());
	        } else {
	        	System.out.println("Rollbar Error Reporting successful");
	        }
    	}).start();
    }
    
    private static String getTruncated(String str, int maxSize){
        int limit = maxSize - 3;
        return (str.length() > maxSize) ? str.substring(0, limit) + "..." : str;
     }
	
	public void init() {
		if (init) {
			throw new IllegalStateException("Error, handler is already initialized.");
		}
		
		System.out.println("Register Rollbar Error Handler on Thread: " + Thread.currentThread().getName());
    	// registers a handler on the current thread
        rollbar.handleUncaughtErrors();
        init = true;
    }
	
	public boolean isInitialized() {
		return init;
	}
	
	private Rollbar getRollbar() {
		return rollbar;
	}
}

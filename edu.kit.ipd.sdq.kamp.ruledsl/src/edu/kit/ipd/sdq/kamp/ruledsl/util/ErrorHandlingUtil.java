package edu.kit.ipd.sdq.kamp.ruledsl.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import edu.kit.ipd.sdq.kamp.ruledsl.generator.KampRuleLanguageGenerator;

public final class ErrorHandlingUtil {
	private ErrorHandlingUtil() {}
	
	public static MultiStatus createMultiStatus(String pluginId, String msg, Throwable t) {
        List<Status> childStatuses = new ArrayList<>();
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTrace: stackTraces) {
            Status status = new Status(IStatus.ERROR, pluginId, stackTrace.toString());
            childStatuses.add(status);
        }

        MultiStatus ms = new MultiStatus(pluginId + ".xxxxxxxx",
                IStatus.ERROR, childStatuses.toArray(new Status[] {}), t.toString(), t);
        
        return ms;
    }
}

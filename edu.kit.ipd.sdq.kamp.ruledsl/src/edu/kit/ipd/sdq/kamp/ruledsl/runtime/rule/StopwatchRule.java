package edu.kit.ipd.sdq.kamp.ruledsl.runtime.rule;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;
import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;

/**
 * This standard (helper) rule is used to measure the time of a given rule.
 *
 * @author Martin Löper
 *
 */
public class StopwatchRule implements IRule {
	private final Stopwatch stopwatch;
	private final IRule rule;
	private final long iterations;
	
	/**
	 * Creates a Stopwatch (wrapper) rule for the given {@code rule}.
	 * @param rule the rule which will be observed
	 */
	public StopwatchRule(IRule rule) {
		this(rule, 1);
	}
	
	/**
	 * Creates a Stopwatch (wrapper) rule for the given {@code rule}.
	 * @param rule the rule which will be observed
	 * @param iterations the number of times the {@link IRule#apply(AbstractArchitectureVersion, ChangePropagationStepRegistry, AbstractChangePropagationAnalysis)} method of {@code rule} is called
	 */
	public StopwatchRule(IRule rule, long iterations) {
		this.stopwatch = Stopwatch.createUnstarted();
		this.rule = rule;
		this.iterations = iterations;
	}
	
	@Override
	public void apply(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry) {
		
		this.stopwatch.start();
		
		for(long i=0; i < this.iterations; i++) {
			this.rule.apply(version, registry);
		}
		
		this.stopwatch.stop();
	}
	
	/**
	 * Returns the elapsed time in the given time format.
	 * @see Stopwatch#elapsed(TimeUnit)
	 * @param timeUnit the time unit which is used to express the elapsed time
	 * @return the elapsed time in the given time unit
	 */
	public long getElapsedTime(TimeUnit timeUnit) {
		return this.stopwatch.elapsed(timeUnit);
	}
	
	/**
	 * Returns the elapsed time per iteration in the given time format.
	 * This essentially divides the total time by the number of iterations.
	 * @param timeUnit timeUnit the time unit which is used to express the elapsed time
	 * @return the elapsed time per iteration in the given time unit
	 */
	public long getElapsedTimePerIteration(TimeUnit timeUnit) {
		return this.stopwatch.elapsed(timeUnit) / this.iterations;
	}
	
	/**
	 * Returns the elapsed time in a human readable format.
	 * @see Stopwatch#toString()
	 * @return the elapsed time (human readable)
	 */
	public String getElapsedTimeAsString() {
		return this.stopwatch.toString();
	}
}

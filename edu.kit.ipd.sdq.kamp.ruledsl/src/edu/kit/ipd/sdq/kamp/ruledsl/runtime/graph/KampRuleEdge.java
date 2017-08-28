package edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph;

public class KampRuleEdge {
	private final KampRuleVertex origin;
	private final KampRuleVertex destination;
	private final Type type;
	
	public KampRuleEdge(KampRuleVertex origin, KampRuleVertex dest, Type type) {
		this.origin = origin;
		this.destination = dest;
		this.type = type;
	}

	public enum Type {
		CHILD, PARENT;
	}

	public KampRuleVertex getOrigin() {
		return origin;
	}

	public KampRuleVertex getDestination() {
		return destination;
	}

	public Type getType() {
		return type;
	}
}

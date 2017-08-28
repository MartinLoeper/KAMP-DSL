package edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;

public class KampRuleVertex {
	private final Class<? extends IRule> content;
	private final Collection<KampRuleVertex> children = new HashSet<>();
	private KampRuleVertex parent;
	private boolean active = true;
	private boolean disableAllParents = false;
	
	public KampRuleVertex(Class<? extends IRule> clazz) {
		this.content = clazz;
	}

	public KampRuleVertex getParent() {
		return this.parent;
	}
	
	public Collection<KampRuleVertex> getChildren() {
		return this.children;
	}
	
	public void setParent(KampRuleVertex vertex) {
		this.parent = vertex;
	}
	
	public void disableAllParents() {
		this.disableAllParents = true;
	}
	
	public boolean isDisableAllParents() {
		return this.disableAllParents;
	}
	
	public void addChild(KampRuleVertex child) {
		this.children.add(child);
	}

	public Class<? extends IRule> getContent() {
		return this.content;
	}
	
	public void setActive(boolean isActive) {
		this.active = isActive;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public Set<KampRuleEdge> getEdges() {
		Set<KampRuleEdge> edges = new HashSet<>();
		for(KampRuleVertex child : this.children) {
			edges.add(new KampRuleEdge(this, child, KampRuleEdge.Type.CHILD));
		}
		
		// add parent reference
		if(parent != null)
			edges.add(new KampRuleEdge(this, parent, KampRuleEdge.Type.PARENT));
		
		return edges;
	}
	
	@Override
	public String toString() {
		return "Class<" + this.content.getSimpleName() + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KampRuleVertex other = (KampRuleVertex) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

	public void removeAllChildren() {
		this.children.clear();
	}
}

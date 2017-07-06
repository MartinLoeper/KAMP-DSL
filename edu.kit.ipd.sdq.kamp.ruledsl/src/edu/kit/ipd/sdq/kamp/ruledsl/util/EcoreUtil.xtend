package edu.kit.ipd.sdq.kamp.ruledsl.util

import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import java.util.List

/**
 * <p>Yet another utility class.
 * <p>This is separated from {@link KampRuleLanguageEcoreUtil} which contains utility methods that are more
 * specific to the grammar of this language.
 * <p>Should at some point be merged with {@link edu.kit.ipd.sdq.commons.util.org.eclipse.emf.ecore.EObjectUtil}
 */
// TODO: merge with SDQ-Commons 
final class EcoreUtil {
	/** Utility classes should not have a public or default constructor. */
	private new() {
	}

	private def static EClassifier getCommonType(Iterable<? extends EClassifier> iterable, EClassifier currentType) {
		if (iterable == null || iterable.empty) {
			return currentType
		}

		if (currentType == null) {
			return getCommonType(iterable.tail, iterable.head)
		} else {
			return getCommonType(iterable.tail, EcoreUtil2.getCompatibleType(iterable.head, currentType))
		}
	}

	/**
	 * <p>Returns the common type of all {@link EClassifier EClassifiers} returned by the iterator
	 * that is obtained from the given Iterable.
	 * 
	 * @param iterable an Iterable that returns all classifiers to build a super type for.
	 *                 Should terminate at some point, since all types are collected during this operation.
	 */
	public def static getCommonType(Iterable<? extends EClassifier> iterable) {
		return getCommonType(iterable, null)
	}
	
	/**
	 * <p>Returns the first previous sibling of {@code element} that is assignable from one of the given types.
	 * <p>Siblings are all elements found via {@code element} {@link EObject#eContainer() .eContainer} {@link EObject#eContents() .eContents}.
	 * <p>The 'first previous' sibling is a element that:
	 * <ul>
	 *   <li>is assignable to one of the given types,</li>
	 *   <li>whose {@link List#indexOf(Object) position} in the siblings collection is <strong>smaller</strong> than that of {@code element}, and</li>
	 *   <li>whose position is nearest to that of {@code element}.</li>
	 * </ul> 
	 * <p>If no such sibling is found the method returns {@code null} 
	 */
	public static def <T extends EObject> T getPreviousSiblingOfType(EObject element, Class<? extends T>... types) {
		val container = element.eContainer
		val siblings = container.eContents
		for (var i = siblings.indexOf(element) - 1; i >= 0; i--) {
			val previous = siblings.get(i)
			if (types.exists[it.isAssignableFrom(previous.class)]) {
				// the type system calculates a suitable type for T, i.e.
				// a type that is assignable from all given types.
				// Therefore we can safely cast to T here.
				return (previous as T)
			}
		}
		return null
	}
	
	/**
	 * <p>Returns the first next sibling of {@code element} that is assignable from one of the given types.
	 * <p>Siblings are all elements found via {@code element} {@link EObject#eContainer() .eContainer} {@link EObject#eContents() .eContents}.
	 * <p>The 'first next' sibling is a element that:
	 * <ul>
	 *   <li>is assignable to one of the given types,</li>
	 *   <li>whose {@link List#indexOf(Object) position} in the siblings collection is <strong>greater</strong> than that of {@code element}, and</li>
	 *   <li>whose position is nearest to that of {@code element}.</li>
	 * </ul> 
	 * <p>If no such sibling is found the method returns {@code null} 
	 */
	public static def <T extends EObject> T getNextSiblingOfType(EObject element, Class<? extends T>... types) {
		val container = element.eContainer
		val siblings = container.eContents
		for (var i = siblings.indexOf(element) + 1; i < siblings.size; i++) {
			val next = siblings.get(i)
			if (types.exists[it.isAssignableFrom(next.class)]) {
				// the type system calculates a suitable type for T, i.e.
				// a type that is assignable from all given types.
				// Therefore we can safely cast to T here.
				return (next as T)
			}
		}
		return null
	}
}

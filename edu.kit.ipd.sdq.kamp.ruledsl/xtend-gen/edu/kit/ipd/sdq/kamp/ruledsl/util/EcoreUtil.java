package edu.kit.ipd.sdq.kamp.ruledsl.util;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

/**
 * <p>Yet another utility class.
 * <p>This is separated from {@link KampRuleLanguageEcoreUtil} which contains utility methods that are more
 * specific to the grammar of this language.
 * <p>Should at some point be merged with {@link edu.kit.ipd.sdq.commons.util.org.eclipse.emf.ecore.EObjectUtil}
 */
@SuppressWarnings("all")
public final class EcoreUtil {
  /**
   * Utility classes should not have a public or default constructor.
   */
  private EcoreUtil() {
  }
  
  private static EClassifier getCommonType(final Iterable<? extends EClassifier> iterable, final EClassifier currentType) {
    if ((Objects.equal(iterable, null) || IterableExtensions.isEmpty(iterable))) {
      return currentType;
    }
    boolean _equals = Objects.equal(currentType, null);
    if (_equals) {
      return EcoreUtil.getCommonType(IterableExtensions.tail(iterable), IterableExtensions.head(iterable));
    } else {
      return EcoreUtil.getCommonType(IterableExtensions.tail(iterable), EcoreUtil2.getCompatibleType(IterableExtensions.head(iterable), currentType));
    }
  }
  
  /**
   * <p>Returns the common type of all {@link EClassifier EClassifiers} returned by the iterator
   * that is obtained from the given Iterable.
   * 
   * @param iterable an Iterable that returns all classifiers to build a super type for.
   *                 Should terminate at some point, since all types are collected during this operation.
   */
  public static EClassifier getCommonType(final Iterable<? extends EClassifier> iterable) {
    return EcoreUtil.getCommonType(iterable, null);
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
  public static <T extends EObject> T getPreviousSiblingOfType(final EObject element, final Class<? extends T>... types) {
    final EObject container = element.eContainer();
    final EList<EObject> siblings = container.eContents();
    for (int i = (siblings.indexOf(element) - 1); (i >= 0); i--) {
      {
        final EObject previous = siblings.get(i);
        final Function1<Class<? extends T>, Boolean> _function = (Class<? extends T> it) -> {
          return Boolean.valueOf(it.isAssignableFrom(previous.getClass()));
        };
        boolean _exists = IterableExtensions.<Class<? extends T>>exists(((Iterable<Class<? extends T>>)Conversions.doWrapArray(types)), _function);
        if (_exists) {
          return ((T) previous);
        }
      }
    }
    return null;
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
  public static <T extends EObject> T getNextSiblingOfType(final EObject element, final Class<? extends T>... types) {
    final EObject container = element.eContainer();
    final EList<EObject> siblings = container.eContents();
    for (int i = (siblings.indexOf(element) + 1); (i < siblings.size()); i++) {
      {
        final EObject next = siblings.get(i);
        final Function1<Class<? extends T>, Boolean> _function = (Class<? extends T> it) -> {
          return Boolean.valueOf(it.isAssignableFrom(next.getClass()));
        };
        boolean _exists = IterableExtensions.<Class<? extends T>>exists(((Iterable<Class<? extends T>>)Conversions.doWrapArray(types)), _function);
        if (_exists) {
          return ((T) next);
        }
      }
    }
    return null;
  }
}

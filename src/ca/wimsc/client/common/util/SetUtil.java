/**
 * 
 */
package ca.wimsc.client.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils for working with sets
 */
public class SetUtil {

	public static <T> Set<T> toSet(T... theValues) {
		return toSet(null, theValues);
	}

	public static <T> ArrayList<T> toList(Set<T> theValues) {
		ArrayList<T> retVal = new ArrayList<T>();
		if (theValues != null) {
			for (T t : theValues) {
				retVal.add(t);
			}
		}
		return retVal;
	}
	
	public static <T> Set<T> toSet(IPredicate<T> thePredicate, T... theValues) {
		HashSet<T> retVal = new HashSet<T>();
		if (theValues != null) {
			for (T t : theValues) {
				if (thePredicate != null && !thePredicate.matches(t)) {
					continue;
				}
				retVal.add(t);
			}
		}
		return retVal;
	}

	public static <T> Set<T> toSet(IPredicate<T> thePredicate, Set<T> theValues) {
		HashSet<T> retVal = new HashSet<T>();
		if (theValues != null) {
			for (T t : theValues) {
				if (thePredicate != null && !thePredicate.matches(t)) {
					continue;
				}
				retVal.add(t);
			}
		}
		return retVal;
	}
	
	public interface IPredicate<T> {
		
		boolean matches(T theObject);
		
	}
	
}

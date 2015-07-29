/**
 * 
 */
package ca.wimsc.client.common.util;

/**
 * @author James
 *
 */
public class ObjectUtil {

	public static boolean equals(Object theO1, Object theO2) {
		if (theO1 == null && theO2 == null) {
			return true;
		}
		if (theO1 == null || theO2 == null) {
			return false;
		}
		return theO1.equals(theO2);
	}
	
}

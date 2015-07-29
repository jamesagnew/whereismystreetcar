/**
 * 
 */
package ca.wimsc.server.util;

/**
 * Constants / config
 */
public class ServerConstants {

	public static final boolean ENABLE_DIVERSION_SCAN = true;

	/**
	 * To be a diversion, the streetcar has to have been detected at least this far from the main route
	 */
	public static final long DIVERSION_MIN_DISTANCE_FROM_ROUTE = 200;

	public static final boolean ENABLE_NOT_MOVING_SCAN = false;

}

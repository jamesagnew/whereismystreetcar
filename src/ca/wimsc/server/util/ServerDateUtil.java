/**
 * 
 */
package ca.wimsc.server.util;

import java.text.ParseException;
import java.util.Date;

/**
 * @author James
 * 
 */
public class ServerDateUtil {
	private static java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");


	public static Date parseTwitterDate(String theDate) throws ParseException {
		if (theDate == null || theDate.length() == 0) {
			return null;
		}
		
		synchronized (format) {
			Date createdAt = format.parse(theDate);
			return createdAt;
		}
	}

}

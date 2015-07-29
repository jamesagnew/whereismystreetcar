/**
 * 
 */
package ca.wimsc.client.common.util;

/**
 * @author James
 * 
 */
public class ColourUtil {

	private static String[] COLOUR_CODES = { "FF0000", "008000", "0000FF", "808000", "800080", "008080", "800000", "00FF00", "000080", "FF3399", "CC66CC", "33CC99" , "666600"};

	
	public static String getColourNameWithoutHash(int theIndex) {
		theIndex = theIndex % COLOUR_CODES.length;
		return COLOUR_CODES[theIndex];
	}
}

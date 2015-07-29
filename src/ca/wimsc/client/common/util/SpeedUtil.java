/**
 * 
 */
package ca.wimsc.client.common.util;

/**
 * @author James
 * 
 */
public class SpeedUtil {

	/**
	 * @param theNextSpeed
	 * @return
	 */
	public static String toSpeedHexColour(int theNextSpeed) {
		int speed = Math.abs(theNextSpeed);
		speed = speed - 5;
		if (speed < 0) {
			speed = 0;
		}
		
		double v = speed / 15.0;
		double g;
		double r;

		if (v > 1.0) {
			g = 1.0;
			r = 0.0;
		} else if (v > 0.5) {
			g = v;
			r = 1.0 - v;
		} else {
			g = v;
			r = 1.0 - v;
		}

		String strokeColor = "#" + formatHex(r) + formatHex(g) + "00";
		return strokeColor;
	}


	/**
	 * @param theG
	 * @return
	 */
	private static String formatHex(double theG) {
		int value = (int) (theG * 256);
		String retVal = Integer.toHexString(value);

		int len = retVal.length();
		switch (len) {
		case 0:
			retVal = "00";
			break;
		case 1:
			retVal = "0" + retVal;
			break;
		case 2:
			break;
		default:
			retVal = "FF";
		}

		return retVal;
	}
}

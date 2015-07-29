package ca.wimsc.client.common.util;

import java.util.Date;

public class DateUtil {

    private static final int SECS_PER_MIN = 60;
    private static final int SECS_PER_HOUR = 60 * SECS_PER_MIN;
    private static final int SECS_PER_DAY = 24 * SECS_PER_HOUR;
	public static final long HALF_AN_HOUR_IN_MILLIS = 30 * 60 * 1000;

    public static String formatTimeElapsed(Date theDate) {
        int elapsedSeconds = getTimeElapsedInSeconds(theDate);
        
        StringBuilder retVal = new StringBuilder();
        
        int elapsed = 0;
        if (elapsedSeconds > (2 * SECS_PER_DAY)) {
            elapsed = elapsedSeconds / SECS_PER_DAY;
            retVal.append(elapsed);
            retVal.append(" day");
        } else if (elapsedSeconds > (2 * SECS_PER_HOUR)) {
            elapsed = elapsedSeconds / SECS_PER_HOUR;
            retVal.append(elapsed);
            retVal.append(" hour");
        } else {
            elapsed = elapsedSeconds / SECS_PER_MIN;
            retVal.append(elapsed);
            retVal.append(" min");
        }
        
        if (elapsed != 1) {
            retVal.append('s');
        }
        
        return retVal.toString();
    }

	public static int getTimeElapsedInSeconds(Date theDate) {
		int elapsedSeconds = (int) ((System.currentTimeMillis() - theDate.getTime()) / 1000);
		return elapsedSeconds;
	}

    
}

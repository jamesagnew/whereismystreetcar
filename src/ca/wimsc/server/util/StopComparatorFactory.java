package ca.wimsc.server.util;

import java.util.Comparator;

import ca.wimsc.client.common.model.Route.DirectionEnum;
import ca.wimsc.client.common.model.Stop;

/**
 * Provides Comparators for model stops which sort by direction from earliest to latest in the route (i.e. for a
 * westbound direction, sorts from easternmost to westernmost)
 */
public class StopComparatorFactory {

	private static final StopLatitudeComparator NB_COMPARATOR;
	private static final ReverseComparatorWrapper<Stop> SB_COMPARATOR;
	private static final StopLongitudeComparator EB_COMPARATOR;
	private static final ReverseComparatorWrapper<Stop> WB_COMPARATOR;

	static {
		NB_COMPARATOR = new StopLatitudeComparator();
		SB_COMPARATOR = new ReverseComparatorWrapper<Stop>(NB_COMPARATOR);
		EB_COMPARATOR = new StopLongitudeComparator();
		WB_COMPARATOR = new ReverseComparatorWrapper<Stop>(EB_COMPARATOR);
	}
	
	public static Comparator<Stop> getComparator(DirectionEnum theDirection) {
		assert theDirection != null;
		
		switch (theDirection) {
		case EASTBOUND:
			return EB_COMPARATOR;
		case WESTBOUND:
			return WB_COMPARATOR;
		case NORTHBOUND:
			return NB_COMPARATOR;
		case SOUTHBOUND:
			return SB_COMPARATOR;
		}
		
		throw new IllegalArgumentException("Unkonwn direction: " + theDirection);
		
	}
	
	
	private static class StopLatitudeComparator implements Comparator<Stop> {

		@Override
		public int compare(Stop theO1, Stop theO2) {
			double retVal = theO2.getLatitude() - theO1.getLatitude();
			if (retVal < 0.0) {
				return -1;
			}else if (retVal > 0.0) {
				return 1;
			} else {
				return 0;
			}
		}
		
	}

	private static class StopLongitudeComparator implements Comparator<Stop> {

		@Override
		public int compare(Stop theO1, Stop theO2) {
			double retVal = theO2.getLongitude() - theO1.getLongitude();
			if (retVal < 0.0) {
				return -1;
			}else if (retVal > 0.0) {
				return 1;
			} else {
				return 0;
			}
		}
		
	}

}

package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A list of numbers with a timestamp associated with each one, and a super compact serializable format suitable for
 * transmitting to clients.
 */
public class NumbersAndTimestamps implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;

	private String myInternalRepresentation;

	private transient List<NumberAndTimestamp> myValues;


	public NumbersAndTimestamps() {
		// nothing
	}


	public NumbersAndTimestamps(String theInternalRepresentation) {
		myInternalRepresentation = theInternalRepresentation;
	}


	public void addNumber(NumberAndTimestamp theNumberAndTimestamp) {
		addNumber(theNumberAndTimestamp, -1);
	}


	public void addNumber(NumberAndTimestamp theNext, int theMaxToKeep) {
		getValues().add(theNext);
		if (theMaxToKeep != -1 && myValues.size() > theMaxToKeep) {
			setValues(myValues.subList(myValues.size() - theMaxToKeep, myValues.size()));
		} else {
			updateInternalRepresentation();
		}
	}


	/**
	 * @return
	 */
	public int getHighest() {
		int max = 0;
		for (NumberAndTimestamp next : getValues()) {
			if (next.getNumber() > max) {
				max = next.getNumber();
			}
		}
		return max;
	}


	/**
	 * May return null!
	 */
	public NumberAndTimestamp getNewest() {
		if (myValues != null) {
			if (myValues.size() > 0) {
				return myValues.get(0);
			} else {
				return null;
			}
		} else {
			if (myInternalRepresentation == null || myInternalRepresentation.length() == 0) {
				return null;
			}

			int firstComma = -1;
			int secondComma = -1;
			int startIndex = myInternalRepresentation.indexOf(',') + 1;
			for (int charIndex = startIndex; charIndex < myInternalRepresentation.length(); charIndex++) {
				char nextChar = myInternalRepresentation.charAt(charIndex);
				if (nextChar == ',') {
					if (firstComma == -1) {
						firstComma = charIndex;
					} else if (secondComma == -1) {
						secondComma = charIndex;
						break;
					}
				}
			}

			if (firstComma == -1) {
				return null;
			}
			
			if (secondComma != -1) {
				return parse(myInternalRepresentation.substring(startIndex, firstComma), myInternalRepresentation.substring(firstComma + 1, secondComma), 0L);
			} else {
				return parse(myInternalRepresentation.substring(startIndex, firstComma), myInternalRepresentation.substring(firstComma + 1, myInternalRepresentation.length()), 0L);
			}
		}
	}


	/**
	 * @return Returns all timestamps associated wiht this collection
	 */
	public List<Date> getTimestamps() {
		ArrayList<Date> retVal = new ArrayList<Date>();
		for (NumberAndTimestamp next : getValues()) {
			retVal.add(next.getTimestamp());
		}
		return retVal;
	}


	public List<NumberAndTimestamp> getValues() {
		if (myValues == null) {

			// Parse in forward order to avoid expensive inserts at the start
			// Basically, avoiding needing a linked list to keep file size down
			// on the client
			ArrayList<NumberAndTimestamp> retValReversed = new ArrayList<NumberAndTimestamp>();
			if (myInternalRepresentation != null) {
				String[] values = myInternalRepresentation.split(",");

				int maxIndex = values.length - 1;
				Long previousTime = 0l;
				for (int i = 1; i < maxIndex; i = i + 2) {

					String value0 = values[i];
					String value1 = values[i + 1];

					NumberAndTimestamp nextBean = parse(value0, value1, previousTime);

					if (nextBean != null) {
						previousTime = nextBean.getTimestamp().getTime();
						retValReversed.add(nextBean);
					}
				}
			}

			// Now reverse the list
			myValues = new ArrayList<NumberAndTimestamp>(retValReversed.size());
			for (int i = retValReversed.size() - 1; i >= 0; i--) {
				NumberAndTimestamp next = retValReversed.get(i);
				myValues.add(next);
			}

		}
		return myValues;
	}


	public boolean isEmpty() {
		if (myInternalRepresentation != null) {
			return !myInternalRepresentation.contains(",");
		} else {
			return myValues == null || myValues.isEmpty();
		}
	}


	public String marshall() {
		if (myInternalRepresentation == null) {
			updateInternalRepresentation();
		}
		return myInternalRepresentation;
	}


	private NumberAndTimestamp parse(String theNumber, String theTimestamp, Long thePreviousTimestamp) {
		int number = 0;
		long date = 0;
		try {
			number = (int)Long.parseLong(theNumber, Character.MAX_RADIX);
			date = Long.parseLong(theTimestamp, Character.MAX_RADIX);
		} catch (NumberFormatException e) {
			GWT.log("Failed to parse " + theNumber + " or " + theTimestamp);
		}

		NumberAndTimestamp nextBean;
		if (date != 0) {
			nextBean = new NumberAndTimestamp(number, new Date(date + thePreviousTimestamp));
		} else {
			nextBean = null;
		}
		return nextBean;
	}


	public void setValues(List<NumberAndTimestamp> theValues) {
		myValues = theValues;
		updateInternalRepresentation();
	}


	public void sortByTimestamp() {
		Collections.sort(myValues);
	}


	public void unmarshall(String theInternalRepresenation) {
		myInternalRepresentation = theInternalRepresenation;
	}


	private void updateInternalRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append(serialVersionUID);

		if (myValues == null) {
			myValues = new ArrayList<NumberAndTimestamp>();
		}

		Long lastTimestamp = null;
		for (int i = myValues.size() - 1; i >= 0; i--) {
			NumberAndTimestamp next = myValues.get(i);

			builder.append(',');
			builder.append(Integer.toString(next.getNumber(), Character.MAX_RADIX));
			builder.append(',');
			if (lastTimestamp == null) {
				builder.append(Long.toString(next.getTimestamp().getTime(), Character.MAX_RADIX));
			} else {
				builder.append(Long.toString(next.getTimestamp().getTime() - lastTimestamp, Character.MAX_RADIX));
			}
			lastTimestamp = next.getTimestamp().getTime();
		}

		myInternalRepresentation = builder.toString();
	}
	
}

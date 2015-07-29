package ca.wimsc.server.util;

import java.util.Comparator;

public class ReverseComparatorWrapper<T> implements Comparator<T> {

	private Comparator<T> myComparatorToWrap;

	public ReverseComparatorWrapper(Comparator<T> theComparatorToWrap) {
		myComparatorToWrap = theComparatorToWrap;
	}
	
	@Override
	public int compare(T theArg0, T theArg1) {
		return -myComparatorToWrap.compare(theArg0, theArg1);
	}

}

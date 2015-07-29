package ca.wimsc.client.common.util;

/**
 * Bean to hold two objects of an arbitrary type
 */
public class Pair<T, V> {

	private T myObject1;
	private V myObject2;

	public Pair() {
	}

	public Pair(T theObject1, V theObject2) {
		super();
		myObject1 = theObject1;
		myObject2 = theObject2;
	}

	public T getObject1() {
		return myObject1;
	}

	public V getObject2() {
		return myObject2;
	}

	public void setObject1(T theObject1) {
		myObject1 = theObject1;
	}

	public void setObject2(V theObject2) {
		myObject2 = theObject2;
	}

}

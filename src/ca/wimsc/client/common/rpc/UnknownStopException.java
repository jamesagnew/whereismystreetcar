package ca.wimsc.client.common.rpc;

public class UnknownStopException extends Exception {

	private static final long serialVersionUID = 1L;
	private String myStopTag;


	public UnknownStopException() {
	}


	public UnknownStopException(String theStopTag) {
		myStopTag = theStopTag;
	}


	/**
	 * @return the stopTag
	 */
	public String getStopTag() {
		return myStopTag;
	}


	/**
	 * @param theStopTag
	 *            the stopTag to set
	 */
	public void setStopTag(String theStopTag) {
		myStopTag = theStopTag;
	}

}

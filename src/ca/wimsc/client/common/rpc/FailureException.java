package ca.wimsc.client.common.rpc;

public class FailureException extends Exception {

	private static final long serialVersionUID = 1L;

	public FailureException() {
		// no message
	}

	public FailureException(String string) {
		super(string);
	}

}

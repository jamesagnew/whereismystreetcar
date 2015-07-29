package ca.wimsc.client.common.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A single tweet
 */
public class Tweet implements Serializable, IsSerializable {

	private static DateTimeFormat ourClientSideDtFormat;

	private static final long serialVersionUID = 1L;

	private String created_at;
	private String from_user;
	private transient Date myCreatedAtDate;
	private String text;

	public Tweet() {
		super();
	}


	public final String getCreatedAt() {
		return created_at;
	}


	/**
	 * Note: Client mode calls only!
	 */
	public final Date getCreatedAtDate() {
		if (myCreatedAtDate == null) {
			final String str = getCreatedAt();
			try {

				if (GWT.isClient()) {
					if (ourClientSideDtFormat == null) {
						ourClientSideDtFormat = DateTimeFormat.getFormat("EEE, dd MMM yyyy HH:mm:ss Z");
					}
					myCreatedAtDate = ourClientSideDtFormat.parse(str);
				} else {
					throw new Exception("Method is for client side calls only");
				}

			} catch (Exception e) {
				GWT.log("Failed to parse date: " + str, e);
			}
		}
		return myCreatedAtDate;
	}


	public final String getFromUser() {
		return from_user;
	}


	public final String getText() {
		return text;
	}


	public void setCreatedAt(String theCreatedAt) {
		myCreatedAtDate = null;
		created_at = theCreatedAt;
	}


	public void setFromUser(String theFromUser) {
		from_user = theFromUser;
	}


	public void setText(String theText) {
		text = theText;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "@" + from_user + ": " + text;
	}
}

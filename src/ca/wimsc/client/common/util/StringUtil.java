package ca.wimsc.client.common.util;


public class StringUtil {

	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";

	/**
	 * <p>
	 * Adds HTML anchor tags around a block of text.
	 * </p>
	 * <p>
	 * E.g. <code>"Hello http://bit.ly/1111 aaaa"</code> becomes
	 * <code>"Hello &lt;a href='http://bit.ly/1111'>http://bit.ly/1111&lt;/a> aaaa"</code>
	 * </p>
	 */
	public static String addAnchorTagsAroundLinks(String theText) {
		if (theText == null) {
			theText = "";
		}

		int index = 0;
		StringBuilder builder = new StringBuilder();

		do {
			int httpIndex = theText.indexOf(HTTP_PREFIX, index);
			int httpsIndex = theText.indexOf(HTTPS_PREFIX, index);
			int startIndex = -1;
			if (httpIndex == -1 && httpsIndex != -1) {
				startIndex = httpsIndex;
			} else if (httpIndex != -1 && httpsIndex == -1) {
				startIndex = httpIndex;
			} else if (httpsIndex == -1 && httpIndex != -1) {
				startIndex = httpIndex;
			} else if (httpIndex == -1 && httpsIndex != -1) {
				startIndex = httpsIndex;
			} else if (httpIndex < httpsIndex) {
				startIndex = httpIndex;
			} else if (httpsIndex < httpIndex) {
				startIndex = httpsIndex;
			}

			if (startIndex == -1) {
				builder.append(theText.substring(index));
				break;
			}

			builder.append(theText, index, startIndex);

			int endIndex = theText.indexOf(' ', startIndex);
			if (endIndex == -1) {
				endIndex = theText.length();
			}

			builder.append("<a href=\"");
			builder.append(theText, startIndex, endIndex);
			builder.append("\" target=\"_blank\">");
			builder.append(theText, startIndex, endIndex);
			builder.append("</a>");

			index = endIndex;
		} while (index < theText.length());

		return builder.toString();
	}

	public static boolean isBlank(String theName) {
		return theName == null || theName.trim().length() == 0;
	}

	public static String stripSubstrings(String theInput, String... theSubstrings) {
		for (String string : theSubstrings) {
			int before = 0;
			int after = 0;
			do {
				before = theInput.length();
				theInput = theInput.replace(string, "");
				after = theInput.length();
			} while (before != after);
		}

		return theInput;
	}

	public static boolean isNotBlank(String theString) {
		return !isBlank(theString);
	}

	public static boolean equals(String theString1, String theString2) {
		if (theString1 == null && theString2 == null) {
			return true;
		}

		if (theString1 == null) {
			return false;
		}

		if (theString2 == null) {
			return false;
		}
		
		return theString1.equals(theString2);
	}

	/**
	 * Returns "" instead of null
	 */
	public static Object defaultString(String theString) {
		if (theString == null) {
			return "";
		}
		return theString;
	}
}

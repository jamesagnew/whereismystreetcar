package ca.wimsc.server.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import ca.wimsc.server.util.StackTraceDeobfuscator;
import ca.wimsc.server.util.StackTraceDeobfuscator.SymbolMap;

import com.google.gson.Gson;

/**
 * Receives notifications from clients that an uncaught exception was thrown
 */
public class ClientLoggingHandlerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ClientLoggingHandlerServlet.class.getName());
	private StackTraceDeobfuscator myDeobfuscator;

	public ClientLoggingHandlerServlet() {
		myDeobfuscator = new StackTraceDeobfuscator("symbols/whereismystreetcar/symbolMaps/");
	}

	@Override
	protected void doPost(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {

		Reader reader = new BufferedReader(new InputStreamReader(theReq.getInputStream()));
		String requestString = IOUtils.toString(reader);

		try {
			Gson gson = new Gson();
			MyReportedExceptionBean bean = gson.fromJson(requestString, MyReportedExceptionBean.class);
			bean.deobfuscate(myDeobfuscator);
			logger.log(Level.WARNING, bean.toString());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception during JSON parse: " + e.getMessage() + " - Request was: " + requestString);
		}

		theResp.setStatus(200);

	}

	static class MyReportedExceptionBean {

		private String strongName;
		private String message;

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("ClientException[");
			b.append("\n stongName=").append(getStrongName()).append(",\n");
			b.append(" Message=").append(getMessage()).append(",\n");
			b.append(" Stack Trace:\n");
			for (String next : getStackTrace()) {
				b.append(" * ").append(next).append("\n");
			}
			b.append("]");
			return b.toString();
		}

		public void deobfuscate(StackTraceDeobfuscator theDeobfuscator) {
			if (strongName == null) {
				return;
			}
			SymbolMap symbolMap = theDeobfuscator.loadSymbolMap(strongName);
			if (symbolMap == null) {
				return;
			}

			for (int i = 0; i < stackTrace.size(); i++) {
				String next = stackTrace.get(i);
				String value = symbolMap.get(next);
				if (value != null) {
					stackTrace.set(i, value);
				}
			}

		}

		public String getStrongName() {
			return strongName;
		}

		public void setStrongName(String theStrongName) {
			strongName = theStrongName;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String theMessage) {
			message = theMessage;
		}

		public List<String> getStackTrace() {
			return stackTrace;
		}

		public void setStackTrace(List<String> theStackTrace) {
			stackTrace = theStackTrace;
		}

		private List<String> stackTrace;

	}
}

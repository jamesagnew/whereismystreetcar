package ca.wimsc.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.svc.IActivityMonitorService;
import ca.wimsc.server.svc.ServiceFactory;

public class CronActivatorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private IActivityMonitorService myActivityMonitorService;

	public CronActivatorServlet() throws IOException {
		myActivityMonitorService = ServiceFactory.getInstance().getActivityMonitorService();
	}

	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {

		String message;
		try {
			if (theReq.getRequestURI().contains("updateVehicles")) {
				message = myActivityMonitorService.updateVehicles();
			} else if (theReq.getRequestURI().contains("purgeIncidents")) {
				message = myActivityMonitorService.purgeIncidents();
			} else if (theReq.getRequestURI().contains("forceUpdateRoutePaths")) {
				message = myActivityMonitorService.updateRoutePaths();
			} else {
				throw new ServletException("Invalid URI: " + theReq.getRequestURI());
			}
			theResp.setContentType("text/plain");
			theResp.getOutputStream().print(message);

		} catch (FailureException e) {
			throw new ServletException(e);
		}

	}

}

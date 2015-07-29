package ca.wimsc.server.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.client.common.util.StringUtil;
import ca.wimsc.server.svc.ServiceFactory;
import ca.wimsc.server.xml.route.Body;

public class HtmlChooseStopServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(HtmlChooseStopServlet.class.getName());


	@Override
	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		theReq.setAttribute("Start.Time", startTime);

		String requestUri = theReq.getRequestURI();
		int startIndex = requestUri.indexOf("/html/route/");
		if (startIndex == -1) {
			throw new ServletException("Error! Invalid route URI: " + requestUri);
		}
		startIndex += "/html/route/".length();

		int endIndex = requestUri.indexOf(".html", startIndex);
		if (endIndex == -1) {
			throw new ServletException("Error! Invalid route tail URI: " + requestUri);
		}

		String routeTag = requestUri.substring(startIndex, endIndex);
		theReq.setAttribute("Route.Tag", routeTag);

		Body.Route route;
		try {
			ServiceFactory serviceFactory = ServiceFactory.getInstance();
			if (serviceFactory.getNextbusFetcherService().loadRouteList().getRoute(routeTag) == null) {
				logger.warning("HTTP 404 - Request for unknown route: " + routeTag);
				theResp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown route: " + routeTag);
				return;
			}

			route = serviceFactory.getNextbusFetcherService().loadRouteConfig(routeTag).getRoute();
		} catch (FailureException e) {
			logger.log(Level.SEVERE, "HTTP 500 - Failure loading route", e);
			theResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong loading this route.. Please try again later!");
			return;
		}

		theReq.setAttribute("Body.Route", route);

		String direction = theReq.getParameter("direction");
		if (StringUtil.isBlank(direction)) {
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/html/html_choose_direction.jsp");
			dispatcher.include(theReq, theResp);
		} else {
			theReq.setAttribute("Direction.Tag", direction);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/html/html_choose_stop.jsp");
			dispatcher.include(theReq, theResp);
		}
	}

}

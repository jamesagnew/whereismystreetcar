package ca.wimsc.server.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.wimsc.client.common.model.PredictionsList;
import ca.wimsc.client.common.model.StopListForRoute;
import ca.wimsc.client.common.model.Tweet;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.svc.INextbusFetcherService;
import ca.wimsc.server.svc.ITwitterService;
import ca.wimsc.server.svc.ServiceFactory;
import ca.wimsc.server.xml.route.Body.Route;
import ca.wimsc.server.xml.route.Body.Route.Stop;

public class HtmlPredictionsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private INextbusFetcherService myNextbusFetcherService;
	private ITwitterService myTwitterService;

    public HtmlPredictionsServlet() throws IOException {
        myNextbusFetcherService = ServiceFactory.getInstance().getNextbusFetcherService();
        myTwitterService = ServiceFactory.getInstance().getTwitterService();
    }
    
    @Override
    protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        theReq.setAttribute("Start.Time", startTime);
        
        String requestUri = theReq.getRequestURI();
        int startIndex = requestUri.indexOf("/html/stop/");
        if (startIndex == -1) {
            throw new ServletException("Error! Invalid URI: " + requestUri);
        }
        startIndex += "/html/stop/".length();
        
        int endIndex = requestUri.indexOf("/", startIndex);
        if (endIndex == -1) {
            throw new ServletException("Error! Invalid route URI: " + requestUri);
        }

        String routeTag = requestUri.substring(startIndex, endIndex);
        theReq.setAttribute("Route.Tag", routeTag);

        startIndex = endIndex + 1;
        endIndex = requestUri.indexOf(".html", startIndex);
        if (endIndex == -1) {
            throw new ServletException("Error! Invalid stop URI: " + requestUri);
        }

        String stopTag = requestUri.substring(startIndex, endIndex);
        stopTag = StreetcarServiceImpl.replaceStopTagWithNewStopTag(stopTag);
        
        theReq.setAttribute("Stop.Tag", stopTag);

        try {
            PredictionsList predictions = myNextbusFetcherService.loadPredictions(routeTag, stopTag);
            theReq.setAttribute("Predictions.List", predictions);

            Route routeConfig = myNextbusFetcherService.loadRouteConfig(routeTag).getRoute();
            theReq.setAttribute("Route.Config", routeConfig);
            
            StopListForRoute stopList = myNextbusFetcherService.getStopListForRoute(routeTag);
            theReq.setAttribute("Stop.List.For.Route", stopList);
            
            Tweet tweet = myTwitterService.getMostRecentTweetForRoute(routeTag);
            if (tweet != null) {
                theReq.setAttribute("Most.Recent.Tweet", tweet);
            }
            
            theReq.setAttribute("Stop.Title", "");
            for (Stop nextStop : routeConfig.getStop()) {
            	if (nextStop.getTag().equals(stopTag)) {
                    theReq.setAttribute("Stop.Title", nextStop.getTitle());
                    break;
            	}
            }
            
        } catch (FailureException e) {
            throw new ServletException(e);
        }
        
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/html/html_predictions.jsp");
        dispatcher.include(theReq, theResp);        
    }

}

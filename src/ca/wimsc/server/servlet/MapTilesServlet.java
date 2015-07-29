package ca.wimsc.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class MapTilesServlet extends HttpServlet {
    private static final int SECS_PER_YEAR = 60 * 60 * 24 * 365;
    private static final int MILLIS_PER_YEAR = SECS_PER_YEAR * 1000;

    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    static {
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Logger logger = Logger.getLogger(MapTilesServlet.class.getName());

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
        String requestIfModifiedSince = theReq.getHeader("If-Modified-Since");
        if (requestIfModifiedSince != null) {
            theResp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            theResp.setHeader("Last-Modified", theReq.getHeader("If-Modified-Since"));
            return;
        }

        String x = theReq.getParameter("x");
        String y = theReq.getParameter("y");
        String z = theReq.getParameter("z");

        String resourceName = "zoom" + z + "/map-" + y + "-" + x + ".png";

        if (!validateNumeric(x) || !validateNumeric(y) || !validateNumeric(z)) {
            theResp.setStatus(404);
            theResp.setContentType("text/plain");
            theResp.getOutputStream().println("Unknown resource: " + resourceName);
            logger.warning("Invalid resource request: " + resourceName);
            return;
        }

        InputStream inputStream = MapTilesServlet.class.getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            theResp.setStatus(404);
            theResp.setContentType("text/plain");
            theResp.getOutputStream().println("Unknown resource: ");
            logger.warning("Unknown resource: " + resourceName);
            return;
        }

        // Try to encourage caching of the tiles
        theResp.setHeader("Last-Modified", "Sat, 6 May 1995 12:00:00 GMT");
        theResp.setHeader("Cache-Control", "max-age=" + SECS_PER_YEAR);
        String format = httpDateFormat.format(new Date(System.currentTimeMillis() + MILLIS_PER_YEAR));
        theResp.addHeader("Expires", format);

        theResp.setStatus(200);
        theResp.setContentType("image/png");

        ServletOutputStream outputStream = theResp.getOutputStream();
        IOUtils.copy(inputStream, outputStream);

    }

    private boolean validateNumeric(String theY) {
        for (int i = 0; i < theY.length(); i++) {
            if (!Character.isDigit(theY.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}

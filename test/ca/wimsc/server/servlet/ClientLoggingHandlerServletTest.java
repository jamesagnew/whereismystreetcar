package ca.wimsc.server.servlet;

import junit.framework.Assert;

import org.junit.Test;

import ca.wimsc.server.servlet.ClientLoggingHandlerServlet.MyReportedExceptionBean;

import com.google.gson.Gson;
import com.google.gwt.core.client.GWT;

public class ClientLoggingHandlerServletTest {

    @Test
    public void testDeserialize() {

        Throwable ex = null;
        try {
            throw new Exception("This is the message");
        } catch (Exception e) {
            ex = e;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\"strongName\" : ");
        sb.append("'" + (GWT.getPermutationStrongName()) + "'");
        sb.append(",\"message\" : ");
        sb.append("'" + (ex.getMessage()) + "'");

        sb.append(",\"stackTrace\" : [");
        boolean needsComma = false;
        for (StackTraceElement e : ex.getStackTrace()) {
          if (needsComma) {
            sb.append(",");
          } else {
            needsComma = true;
          }

          sb.append("'"+(e.getMethodName())+"'");
        }
        sb.append("]}");

        String gsonString = sb.toString();
        Gson gon = new Gson();
        MyReportedExceptionBean bean = gon.fromJson(gsonString, ClientLoggingHandlerServlet.MyReportedExceptionBean.class);
        
        Assert.assertEquals(bean.getMessage(), "This is the message");
        Assert.assertTrue(bean.getStackTrace().toString().contains("testDeserialize"));
        
    }
    
    
}

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.10.12 at 07:51:13 PM EDT 
//


package ca.wimsc.server.xml.stops;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ca.wimsc.server.xml.stops package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ca.wimsc.server.xml.stops
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Body.Predictions }
     * 
     */
    public Body.Predictions createBodyPredictions() {
        return new Body.Predictions();
    }

    /**
     * Create an instance of {@link Body }
     * 
     */
    public Body createBody() {
        return new Body();
    }

    /**
     * Create an instance of {@link Body.Predictions.Direction.Prediction }
     * 
     */
    public Body.Predictions.Direction.Prediction createBodyPredictionsDirectionPrediction() {
        return new Body.Predictions.Direction.Prediction();
    }

    /**
     * Create an instance of {@link Body.Predictions.Direction }
     * 
     */
    public Body.Predictions.Direction createBodyPredictionsDirection() {
        return new Body.Predictions.Direction();
    }

}

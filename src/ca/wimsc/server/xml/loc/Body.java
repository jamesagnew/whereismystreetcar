//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.10.16 at 09:51:27 AM EDT 
//


package ca.wimsc.server.xml.loc;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vehicle" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                 &lt;/sequence>
 *                 &lt;attribute name="dirTag" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="heading" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="lat" type="{http://www.w3.org/2001/XMLSchema}Double" />
 *                 &lt;attribute name="lon" type="{http://www.w3.org/2001/XMLSchema}Double" />
 *                 &lt;attribute name="predictable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="routeTag" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="secsSinceReport" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="speedKmHr" type="{http://www.w3.org/2001/XMLSchema}Double" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="lastTime">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                 &lt;/sequence>
 *                 &lt;attribute name="time" type="{http://www.w3.org/2001/XMLSchema}long" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="copyright" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vehicle",
    "lastTime"
})
@XmlRootElement(name = "body")
public class Body implements Serializable {

    @XmlElement(required = true)
    protected List<Body.Vehicle> vehicle;
    @XmlElement(required = true)
    protected Body.LastTime lastTime;
    @XmlAttribute
    protected String copyright;

    /**
     * Gets the value of the vehicle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vehicle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVehicle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Body.Vehicle }
     * 
     * 
     */
    public List<Body.Vehicle> getVehicle() {
        if (vehicle == null) {
            vehicle = new ArrayList<Body.Vehicle>();
        }
        return this.vehicle;
    }

    /**
     * Gets the value of the lastTime property.
     * 
     * @return
     *     possible object is
     *     {@link Body.LastTime }
     *     
     */
    public Body.LastTime getLastTime() {
        return lastTime;
    }

    /**
     * Sets the value of the lastTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Body.LastTime }
     *     
     */
    public void setLastTime(Body.LastTime value) {
        this.lastTime = value;
    }

    /**
     * Gets the value of the copyright property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCopyright(String value) {
        this.copyright = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *       &lt;/sequence>
     *       &lt;attribute name="time" type="{http://www.w3.org/2001/XMLSchema}long" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LastTime implements Serializable {

        @XmlAttribute
        protected Long time;

        /**
         * Gets the value of the time property.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getTime() {
            return time;
        }

        /**
         * Sets the value of the time property.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setTime(Long value) {
            this.time = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *       &lt;/sequence>
     *       &lt;attribute name="dirTag" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="heading" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="lat" type="{http://www.w3.org/2001/XMLSchema}Double" />
     *       &lt;attribute name="lon" type="{http://www.w3.org/2001/XMLSchema}Double" />
     *       &lt;attribute name="predictable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="routeTag" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="secsSinceReport" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="speedKmHr" type="{http://www.w3.org/2001/XMLSchema}Double" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Vehicle  implements Serializable {

        @XmlAttribute
        protected String dirTag;
        @XmlAttribute
        protected String heading;
        @XmlAttribute
        protected String id;
        @XmlAttribute
        protected Double lat;
        @XmlAttribute
        protected Double lon;
        @XmlAttribute
        protected Boolean predictable;
        @XmlAttribute
        protected String routeTag;
        @XmlAttribute
        protected BigInteger secsSinceReport;
        @XmlAttribute
        protected Double speedKmHr;

        /**
         * Gets the value of the dirTag property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDirTag() {
            return dirTag;
        }

        /**
         * Sets the value of the dirTag property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDirTag(String value) {
            this.dirTag = value;
        }

        /**
         * Gets the value of the heading property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public String getHeading() {
            return heading;
        }

        /**
         * Sets the value of the heading property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setHeading(String value) {
            this.heading = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setId(String value) {
            this.id = value;
        }

        /**
         * Gets the value of the lat property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getLat() {
            return lat;
        }

        /**
         * Sets the value of the lat property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setLat(Double value) {
            this.lat = value;
        }

        /**
         * Gets the value of the lon property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getLon() {
            return lon;
        }

        /**
         * Sets the value of the lon property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setLon(Double value) {
            this.lon = value;
        }

        /**
         * Gets the value of the predictable property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isPredictable() {
            return predictable;
        }

        /**
         * Sets the value of the predictable property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setPredictable(Boolean value) {
            this.predictable = value;
        }

        /**
         * Gets the value of the routeTag property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public String getRouteTag() {
            return routeTag;
        }

        /**
         * Sets the value of the routeTag property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setRouteTag(String value) {
            this.routeTag = value;
        }

        /**
         * Gets the value of the secsSinceReport property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getSecsSinceReport() {
            return secsSinceReport;
        }

        /**
         * Sets the value of the secsSinceReport property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setSecsSinceReport(BigInteger value) {
            this.secsSinceReport = value;
        }

        /**
         * Gets the value of the speedKmHr property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getSpeedKmHr() {
            return speedKmHr;
        }

        /**
         * Sets the value of the speedKmHr property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setSpeedKmHr(Double value) {
            this.speedKmHr = value;
        }

    }

}
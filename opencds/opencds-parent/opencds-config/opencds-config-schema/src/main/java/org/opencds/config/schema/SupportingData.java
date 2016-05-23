//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.05 at 09:07:42 PM EDT 
//


package org.opencds.config.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for SupportingData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupportingData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="link" type="{org.opencds.dss.config.v1_0}Link" minOccurs="0"/&gt;
 *         &lt;element name="identifier" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="kmId" type="{org.opencds.dss.config.v1_0}KMId"/&gt;
 *         &lt;element name="package" type="{org.opencds.dss.config.v1_0}Package"/&gt;
 *         &lt;element name="loadedBy" type="{org.opencds.dss.config.v1_0}PluginId"/&gt;
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="userId" type="{org.opencds.dss.config.v1_0}UserId"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupportingData", propOrder = {
    "link",
    "identifier",
    "kmId",
    "_package",
    "loadedBy",
    "timestamp",
    "userId"
})
public class SupportingData {

    protected Link link;
    @XmlElement(required = true)
    protected String identifier;
    @XmlElement(required = true)
    protected KMId kmId;
    @XmlElement(name = "package", required = true)
    protected Package _package;
    @XmlElement(required = true)
    protected PluginId loadedBy;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;
    @XmlElement(required = true)
    protected String userId;

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link Link }
     *     
     */
    public Link getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link }
     *     
     */
    public void setLink(Link value) {
        this.link = value;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the kmId property.
     * 
     * @return
     *     possible object is
     *     {@link KMId }
     *     
     */
    public KMId getKmId() {
        return kmId;
    }

    /**
     * Sets the value of the kmId property.
     * 
     * @param value
     *     allowed object is
     *     {@link KMId }
     *     
     */
    public void setKmId(KMId value) {
        this.kmId = value;
    }

    /**
     * Gets the value of the package property.
     * 
     * @return
     *     possible object is
     *     {@link Package }
     *     
     */
    public Package getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     * 
     * @param value
     *     allowed object is
     *     {@link Package }
     *     
     */
    public void setPackage(Package value) {
        this._package = value;
    }

    /**
     * Gets the value of the loadedBy property.
     * 
     * @return
     *     possible object is
     *     {@link PluginId }
     *     
     */
    public PluginId getLoadedBy() {
        return loadedBy;
    }

    /**
     * Sets the value of the loadedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link PluginId }
     *     
     */
    public void setLoadedBy(PluginId value) {
        this.loadedBy = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

}
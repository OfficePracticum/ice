//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.05 at 09:07:42 PM EDT 
//


package org.opencds.config.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Metadata about individual knowledge
 *                 modules (KMs).
 *             
 * 
 * <p>Java class for KnowledgeModule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KnowledgeModule"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="link" type="{org.opencds.dss.config.v1_0}Link" minOccurs="0"/&gt;
 *         &lt;element name="identifier" type="{org.opencds.dss.config.v1_0}KMId"/&gt;
 *         &lt;element name="status" type="{org.opencds.dss.config.v1_0}KMStatus"/&gt;
 *         &lt;element name="executionEngine" type="{org.opencds.dss.config.v1_0}ExecutionEngineIdentifier"/&gt;
 *         &lt;element name="semanticSignifierId" type="{org.opencds.dss.config.v1_0}SemanticSignifierId"/&gt;
 *         &lt;element name="conceptDeterminationMethods" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="primaryCDM" type="{org.opencds.dss.config.v1_0}ConceptDeterminationMethodBase"/&gt;
 *                   &lt;element name="secondaryCDM" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;extension base="{org.opencds.dss.config.v1_0}ConceptDeterminationMethodBase"&gt;
 *                           &lt;attribute name="method" use="required" type="{org.opencds.dss.config.v1_0}SupportMethod" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="package" type="{org.opencds.dss.config.v1_0}Package"/&gt;
 *         &lt;element name="primaryProcess" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="traitId" type="{org.opencds.dss.config.v1_0}EntityIdentifier" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="preProcessPlugins" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="preProcessPlugin" type="{org.opencds.dss.config.v1_0}PluginId" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="postProcessPlugins" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="postProcessPlugin" type="{org.opencds.dss.config.v1_0}PluginId" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
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
@XmlType(name = "KnowledgeModule", propOrder = {
    "link",
    "identifier",
    "status",
    "executionEngine",
    "semanticSignifierId",
    "conceptDeterminationMethods",
    "_package",
    "primaryProcess",
    "traitId",
    "preProcessPlugins",
    "postProcessPlugins",
    "timestamp",
    "userId"
})
public class KnowledgeModule {

    protected Link link;
    @XmlElement(required = true)
    protected KMId identifier;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected KMStatus status;
    @XmlElement(required = true)
    protected String executionEngine;
    @XmlElement(required = true)
    protected SemanticSignifierId semanticSignifierId;
    protected KnowledgeModule.ConceptDeterminationMethods conceptDeterminationMethods;
    @XmlElement(name = "package", required = true)
    protected Package _package;
    protected String primaryProcess;
    protected List<EntityIdentifier> traitId;
    protected KnowledgeModule.PreProcessPlugins preProcessPlugins;
    protected KnowledgeModule.PostProcessPlugins postProcessPlugins;
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
     *     {@link KMId }
     *     
     */
    public KMId getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link KMId }
     *     
     */
    public void setIdentifier(KMId value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link KMStatus }
     *     
     */
    public KMStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link KMStatus }
     *     
     */
    public void setStatus(KMStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the executionEngine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExecutionEngine() {
        return executionEngine;
    }

    /**
     * Sets the value of the executionEngine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExecutionEngine(String value) {
        this.executionEngine = value;
    }

    /**
     * Gets the value of the semanticSignifierId property.
     * 
     * @return
     *     possible object is
     *     {@link SemanticSignifierId }
     *     
     */
    public SemanticSignifierId getSemanticSignifierId() {
        return semanticSignifierId;
    }

    /**
     * Sets the value of the semanticSignifierId property.
     * 
     * @param value
     *     allowed object is
     *     {@link SemanticSignifierId }
     *     
     */
    public void setSemanticSignifierId(SemanticSignifierId value) {
        this.semanticSignifierId = value;
    }

    /**
     * Gets the value of the conceptDeterminationMethods property.
     * 
     * @return
     *     possible object is
     *     {@link KnowledgeModule.ConceptDeterminationMethods }
     *     
     */
    public KnowledgeModule.ConceptDeterminationMethods getConceptDeterminationMethods() {
        return conceptDeterminationMethods;
    }

    /**
     * Sets the value of the conceptDeterminationMethods property.
     * 
     * @param value
     *     allowed object is
     *     {@link KnowledgeModule.ConceptDeterminationMethods }
     *     
     */
    public void setConceptDeterminationMethods(KnowledgeModule.ConceptDeterminationMethods value) {
        this.conceptDeterminationMethods = value;
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
     * Gets the value of the primaryProcess property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimaryProcess() {
        return primaryProcess;
    }

    /**
     * Sets the value of the primaryProcess property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimaryProcess(String value) {
        this.primaryProcess = value;
    }

    /**
     * Gets the value of the traitId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traitId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraitId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EntityIdentifier }
     * 
     * 
     */
    public List<EntityIdentifier> getTraitId() {
        if (traitId == null) {
            traitId = new ArrayList<EntityIdentifier>();
        }
        return this.traitId;
    }

    /**
     * Gets the value of the preProcessPlugins property.
     * 
     * @return
     *     possible object is
     *     {@link KnowledgeModule.PreProcessPlugins }
     *     
     */
    public KnowledgeModule.PreProcessPlugins getPreProcessPlugins() {
        return preProcessPlugins;
    }

    /**
     * Sets the value of the preProcessPlugins property.
     * 
     * @param value
     *     allowed object is
     *     {@link KnowledgeModule.PreProcessPlugins }
     *     
     */
    public void setPreProcessPlugins(KnowledgeModule.PreProcessPlugins value) {
        this.preProcessPlugins = value;
    }

    /**
     * Gets the value of the postProcessPlugins property.
     * 
     * @return
     *     possible object is
     *     {@link KnowledgeModule.PostProcessPlugins }
     *     
     */
    public KnowledgeModule.PostProcessPlugins getPostProcessPlugins() {
        return postProcessPlugins;
    }

    /**
     * Sets the value of the postProcessPlugins property.
     * 
     * @param value
     *     allowed object is
     *     {@link KnowledgeModule.PostProcessPlugins }
     *     
     */
    public void setPostProcessPlugins(KnowledgeModule.PostProcessPlugins value) {
        this.postProcessPlugins = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="primaryCDM" type="{org.opencds.dss.config.v1_0}ConceptDeterminationMethodBase"/&gt;
     *         &lt;element name="secondaryCDM" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;extension base="{org.opencds.dss.config.v1_0}ConceptDeterminationMethodBase"&gt;
     *                 &lt;attribute name="method" use="required" type="{org.opencds.dss.config.v1_0}SupportMethod" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "primaryCDM",
        "secondaryCDM"
    })
    public static class ConceptDeterminationMethods {

        @XmlElement(required = true)
        protected ConceptDeterminationMethodBase primaryCDM;
        protected List<KnowledgeModule.ConceptDeterminationMethods.SecondaryCDM> secondaryCDM;

        /**
         * Gets the value of the primaryCDM property.
         * 
         * @return
         *     possible object is
         *     {@link ConceptDeterminationMethodBase }
         *     
         */
        public ConceptDeterminationMethodBase getPrimaryCDM() {
            return primaryCDM;
        }

        /**
         * Sets the value of the primaryCDM property.
         * 
         * @param value
         *     allowed object is
         *     {@link ConceptDeterminationMethodBase }
         *     
         */
        public void setPrimaryCDM(ConceptDeterminationMethodBase value) {
            this.primaryCDM = value;
        }

        /**
         * Gets the value of the secondaryCDM property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the secondaryCDM property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSecondaryCDM().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link KnowledgeModule.ConceptDeterminationMethods.SecondaryCDM }
         * 
         * 
         */
        public List<KnowledgeModule.ConceptDeterminationMethods.SecondaryCDM> getSecondaryCDM() {
            if (secondaryCDM == null) {
                secondaryCDM = new ArrayList<KnowledgeModule.ConceptDeterminationMethods.SecondaryCDM>();
            }
            return this.secondaryCDM;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;extension base="{org.opencds.dss.config.v1_0}ConceptDeterminationMethodBase"&gt;
         *       &lt;attribute name="method" use="required" type="{org.opencds.dss.config.v1_0}SupportMethod" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SecondaryCDM
            extends ConceptDeterminationMethodBase
        {

            @XmlAttribute(name = "method", required = true)
            protected SupportMethod method;

            /**
             * Gets the value of the method property.
             * 
             * @return
             *     possible object is
             *     {@link SupportMethod }
             *     
             */
            public SupportMethod getMethod() {
                return method;
            }

            /**
             * Sets the value of the method property.
             * 
             * @param value
             *     allowed object is
             *     {@link SupportMethod }
             *     
             */
            public void setMethod(SupportMethod value) {
                this.method = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="postProcessPlugin" type="{org.opencds.dss.config.v1_0}PluginId" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "postProcessPlugin"
    })
    public static class PostProcessPlugins {

        @XmlElement(required = true)
        protected List<PluginId> postProcessPlugin;

        /**
         * Gets the value of the postProcessPlugin property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the postProcessPlugin property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPostProcessPlugin().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PluginId }
         * 
         * 
         */
        public List<PluginId> getPostProcessPlugin() {
            if (postProcessPlugin == null) {
                postProcessPlugin = new ArrayList<PluginId>();
            }
            return this.postProcessPlugin;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="preProcessPlugin" type="{org.opencds.dss.config.v1_0}PluginId" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "preProcessPlugin"
    })
    public static class PreProcessPlugins {

        @XmlElement(required = true)
        protected List<PluginId> preProcessPlugin;

        /**
         * Gets the value of the preProcessPlugin property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the preProcessPlugin property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPreProcessPlugin().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PluginId }
         * 
         * 
         */
        public List<PluginId> getPreProcessPlugin() {
            if (preProcessPlugin == null) {
                preProcessPlugin = new ArrayList<PluginId>();
            }
            return this.preProcessPlugin;
        }

    }

}
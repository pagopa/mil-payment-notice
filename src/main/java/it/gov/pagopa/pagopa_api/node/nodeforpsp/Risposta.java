
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for risposta complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="risposta"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fault" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}faultBean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "risposta", propOrder = {
    "fault"
})
@XmlSeeAlso({
    NodoInviaFlussoRendicontazioneResponse.class,
    NodoChiediTemplateInformativaPSPResponse.class,
    NodoChiediInformativaPAResponse.class
})
public class Risposta {

    protected FaultBean fault;

    /**
     * Gets the value of the fault property.
     * 
     * @return
     *     possible object is
     *     {@link FaultBean }
     *     
     */
    public FaultBean getFault() {
        return fault;
    }

    /**
     * Sets the value of the fault property.
     * 
     * @param value
     *     allowed object is
     *     {@link FaultBean }
     *     
     */
    public void setFault(FaultBean value) {
        this.fault = value;
    }

}

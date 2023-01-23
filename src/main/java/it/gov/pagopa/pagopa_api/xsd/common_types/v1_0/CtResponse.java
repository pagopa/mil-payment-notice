
package it.gov.pagopa.pagopa_api.xsd.common_types.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeRes;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.DemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.NodoChiediCatalogoServiziV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeRes;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerificaBollettinoRes;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;


/**
 * <p>Java class for ctResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="outcome" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stOutcome"/&gt;
 *         &lt;element name="fault" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctFaultBean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctResponse", propOrder = {
    "outcome",
    "fault"
})
@XmlSeeAlso({
    VerificaBollettinoRes.class,
    VerifyPaymentNoticeRes.class,
    ActivatePaymentNoticeRes.class,
    SendPaymentOutcomeRes.class,
    DemandPaymentNoticeResponse.class,
    ActivatePaymentNoticeV2Response.class,
    SendPaymentOutcomeV2Response.class,
    NodoChiediCatalogoServiziV2Response.class
})
public class CtResponse {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StOutcome outcome;
    protected CtFaultBean fault;

    /**
     * Gets the value of the outcome property.
     * 
     * @return
     *     possible object is
     *     {@link StOutcome }
     *     
     */
    public StOutcome getOutcome() {
        return outcome;
    }

    /**
     * Sets the value of the outcome property.
     * 
     * @param value
     *     allowed object is
     *     {@link StOutcome }
     *     
     */
    public void setOutcome(StOutcome value) {
        this.outcome = value;
    }

    /**
     * Gets the value of the fault property.
     * 
     * @return
     *     possible object is
     *     {@link CtFaultBean }
     *     
     */
    public CtFaultBean getFault() {
        return fault;
    }

    /**
     * Sets the value of the fault property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtFaultBean }
     *     
     */
    public void setFault(CtFaultBean value) {
        this.fault = value;
    }

}

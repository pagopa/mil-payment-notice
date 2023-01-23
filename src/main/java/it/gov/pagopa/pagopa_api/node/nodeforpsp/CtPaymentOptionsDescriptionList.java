
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctPaymentOptionsDescriptionList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctPaymentOptionsDescriptionList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="paymentOptionDescription" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctPaymentOptionDescription" maxOccurs="5"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctPaymentOptionsDescriptionList", propOrder = {
    "paymentOptionDescription"
})
public class CtPaymentOptionsDescriptionList {

    @XmlElement(required = true)
    protected List<CtPaymentOptionDescription> paymentOptionDescription;

    /**
     * Gets the value of the paymentOptionDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paymentOptionDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPaymentOptionDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CtPaymentOptionDescription }
     * 
     * 
     */
    public List<CtPaymentOptionDescription> getPaymentOptionDescription() {
        if (paymentOptionDescription == null) {
            paymentOptionDescription = new ArrayList<CtPaymentOptionDescription>();
        }
        return this.paymentOptionDescription;
    }

}

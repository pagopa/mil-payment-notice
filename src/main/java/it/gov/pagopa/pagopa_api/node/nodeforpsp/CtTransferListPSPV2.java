
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctTransferListPSPV2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctTransferListPSPV2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="transfer" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctTransferPSPV2" maxOccurs="5"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctTransferListPSPV2", propOrder = {
    "transfer"
})
public class CtTransferListPSPV2 {

    @XmlElement(required = true)
    protected List<CtTransferPSPV2> transfer;

    /**
     * Gets the value of the transfer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transfer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransfer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CtTransferPSPV2 }
     * 
     * 
     */
    public List<CtTransferPSPV2> getTransfer() {
        if (transfer == null) {
            transfer = new ArrayList<CtTransferPSPV2>();
        }
        return this.transfer;
    }

}

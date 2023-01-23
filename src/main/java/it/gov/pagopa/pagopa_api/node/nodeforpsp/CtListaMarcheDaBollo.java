
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctListaMarcheDaBollo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctListaMarcheDaBollo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="marcaDaBollo" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctMarcaDaBollo" maxOccurs="25"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctListaMarcheDaBollo", propOrder = {
    "marcaDaBollo"
})
public class CtListaMarcheDaBollo {

    @XmlElement(required = true)
    protected List<CtMarcaDaBollo> marcaDaBollo;

    /**
     * Gets the value of the marcaDaBollo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the marcaDaBollo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMarcaDaBollo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CtMarcaDaBollo }
     * 
     * 
     */
    public List<CtMarcaDaBollo> getMarcaDaBollo() {
        if (marcaDaBollo == null) {
            marcaDaBollo = new ArrayList<CtMarcaDaBollo>();
        }
        return this.marcaDaBollo;
    }

}

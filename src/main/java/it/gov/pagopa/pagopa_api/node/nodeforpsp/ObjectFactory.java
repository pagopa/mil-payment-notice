
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.gov.pagopa.pagopa_api.node.nodeforpsp package. 
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

    private final static QName _VerificaBollettinoReq_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "verificaBollettinoReq");
    private final static QName _VerificaBollettinoRes_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "verificaBollettinoRes");
    private final static QName _VerifyPaymentNoticeReq_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "verifyPaymentNoticeReq");
    private final static QName _VerifyPaymentNoticeRes_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "verifyPaymentNoticeRes");
    private final static QName _ActivatePaymentNoticeReq_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "activatePaymentNoticeReq");
    private final static QName _ActivatePaymentNoticeRes_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "activatePaymentNoticeRes");
    private final static QName _SendPaymentOutcomeReq_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "sendPaymentOutcomeReq");
    private final static QName _SendPaymentOutcomeRes_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "sendPaymentOutcomeRes");
    private final static QName _NodoInviaFlussoRendicontazioneRequest_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoInviaFlussoRendicontazioneRequest");
    private final static QName _NodoInviaFlussoRendicontazioneResponse_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoInviaFlussoRendicontazioneResponse");
    private final static QName _NodoChiediTemplateInformativaPSPRequest_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoChiediTemplateInformativaPSPRequest");
    private final static QName _NodoChiediTemplateInformativaPSPResponse_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoChiediTemplateInformativaPSPResponse");
    private final static QName _NodoChiediInformativaPARequest_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoChiediInformativaPARequest");
    private final static QName _NodoChiediInformativaPAResponse_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoChiediInformativaPAResponse");
    private final static QName _DemandPaymentNoticeRequest_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "demandPaymentNoticeRequest");
    private final static QName _DemandPaymentNoticeResponse_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "demandPaymentNoticeResponse");
    private final static QName _ActivatePaymentNoticeV2Request_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "activatePaymentNoticeV2Request");
    private final static QName _ActivatePaymentNoticeV2Response_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "activatePaymentNoticeV2Response");
    private final static QName _SendPaymentOutcomeV2Request_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "sendPaymentOutcomeV2Request");
    private final static QName _SendPaymentOutcomeV2Response_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "sendPaymentOutcomeV2Response");
    private final static QName _NodoChiediCatalogoServiziV2Request_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoChiediCatalogoServiziV2Request");
    private final static QName _NodoChiediCatalogoServiziV2Response_QNAME = new QName("http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", "nodoChiediCatalogoServiziV2Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.gov.pagopa.pagopa_api.node.nodeforpsp
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link VerificaBollettinoReq }
     * 
     */
    public VerificaBollettinoReq createVerificaBollettinoReq() {
        return new VerificaBollettinoReq();
    }

    /**
     * Create an instance of {@link VerificaBollettinoRes }
     * 
     */
    public VerificaBollettinoRes createVerificaBollettinoRes() {
        return new VerificaBollettinoRes();
    }

    /**
     * Create an instance of {@link VerifyPaymentNoticeReq }
     * 
     */
    public VerifyPaymentNoticeReq createVerifyPaymentNoticeReq() {
        return new VerifyPaymentNoticeReq();
    }

    /**
     * Create an instance of {@link VerifyPaymentNoticeRes }
     * 
     */
    public VerifyPaymentNoticeRes createVerifyPaymentNoticeRes() {
        return new VerifyPaymentNoticeRes();
    }

    /**
     * Create an instance of {@link ActivatePaymentNoticeReq }
     * 
     */
    public ActivatePaymentNoticeReq createActivatePaymentNoticeReq() {
        return new ActivatePaymentNoticeReq();
    }

    /**
     * Create an instance of {@link ActivatePaymentNoticeRes }
     * 
     */
    public ActivatePaymentNoticeRes createActivatePaymentNoticeRes() {
        return new ActivatePaymentNoticeRes();
    }

    /**
     * Create an instance of {@link SendPaymentOutcomeReq }
     * 
     */
    public SendPaymentOutcomeReq createSendPaymentOutcomeReq() {
        return new SendPaymentOutcomeReq();
    }

    /**
     * Create an instance of {@link SendPaymentOutcomeRes }
     * 
     */
    public SendPaymentOutcomeRes createSendPaymentOutcomeRes() {
        return new SendPaymentOutcomeRes();
    }

    /**
     * Create an instance of {@link NodoInviaFlussoRendicontazioneRequest }
     * 
     */
    public NodoInviaFlussoRendicontazioneRequest createNodoInviaFlussoRendicontazioneRequest() {
        return new NodoInviaFlussoRendicontazioneRequest();
    }

    /**
     * Create an instance of {@link NodoInviaFlussoRendicontazioneResponse }
     * 
     */
    public NodoInviaFlussoRendicontazioneResponse createNodoInviaFlussoRendicontazioneResponse() {
        return new NodoInviaFlussoRendicontazioneResponse();
    }

    /**
     * Create an instance of {@link NodoChiediTemplateInformativaPSPRequest }
     * 
     */
    public NodoChiediTemplateInformativaPSPRequest createNodoChiediTemplateInformativaPSPRequest() {
        return new NodoChiediTemplateInformativaPSPRequest();
    }

    /**
     * Create an instance of {@link NodoChiediTemplateInformativaPSPResponse }
     * 
     */
    public NodoChiediTemplateInformativaPSPResponse createNodoChiediTemplateInformativaPSPResponse() {
        return new NodoChiediTemplateInformativaPSPResponse();
    }

    /**
     * Create an instance of {@link NodoChiediInformativaPARequest }
     * 
     */
    public NodoChiediInformativaPARequest createNodoChiediInformativaPARequest() {
        return new NodoChiediInformativaPARequest();
    }

    /**
     * Create an instance of {@link NodoChiediInformativaPAResponse }
     * 
     */
    public NodoChiediInformativaPAResponse createNodoChiediInformativaPAResponse() {
        return new NodoChiediInformativaPAResponse();
    }

    /**
     * Create an instance of {@link DemandPaymentNoticeRequest }
     * 
     */
    public DemandPaymentNoticeRequest createDemandPaymentNoticeRequest() {
        return new DemandPaymentNoticeRequest();
    }

    /**
     * Create an instance of {@link DemandPaymentNoticeResponse }
     * 
     */
    public DemandPaymentNoticeResponse createDemandPaymentNoticeResponse() {
        return new DemandPaymentNoticeResponse();
    }

    /**
     * Create an instance of {@link ActivatePaymentNoticeV2Request }
     * 
     */
    public ActivatePaymentNoticeV2Request createActivatePaymentNoticeV2Request() {
        return new ActivatePaymentNoticeV2Request();
    }

    /**
     * Create an instance of {@link ActivatePaymentNoticeV2Response }
     * 
     */
    public ActivatePaymentNoticeV2Response createActivatePaymentNoticeV2Response() {
        return new ActivatePaymentNoticeV2Response();
    }

    /**
     * Create an instance of {@link SendPaymentOutcomeV2Request }
     * 
     */
    public SendPaymentOutcomeV2Request createSendPaymentOutcomeV2Request() {
        return new SendPaymentOutcomeV2Request();
    }

    /**
     * Create an instance of {@link SendPaymentOutcomeV2Response }
     * 
     */
    public SendPaymentOutcomeV2Response createSendPaymentOutcomeV2Response() {
        return new SendPaymentOutcomeV2Response();
    }

    /**
     * Create an instance of {@link NodoChiediCatalogoServiziV2Request }
     * 
     */
    public NodoChiediCatalogoServiziV2Request createNodoChiediCatalogoServiziV2Request() {
        return new NodoChiediCatalogoServiziV2Request();
    }

    /**
     * Create an instance of {@link NodoChiediCatalogoServiziV2Response }
     * 
     */
    public NodoChiediCatalogoServiziV2Response createNodoChiediCatalogoServiziV2Response() {
        return new NodoChiediCatalogoServiziV2Response();
    }

    /**
     * Create an instance of {@link StPaymentTokens }
     * 
     */
    public StPaymentTokens createStPaymentTokens() {
        return new StPaymentTokens();
    }

    /**
     * Create an instance of {@link CtEntityUniqueIdentifier }
     * 
     */
    public CtEntityUniqueIdentifier createCtEntityUniqueIdentifier() {
        return new CtEntityUniqueIdentifier();
    }

    /**
     * Create an instance of {@link CtSubject }
     * 
     */
    public CtSubject createCtSubject() {
        return new CtSubject();
    }

    /**
     * Create an instance of {@link Risposta }
     * 
     */
    public Risposta createRisposta() {
        return new Risposta();
    }

    /**
     * Create an instance of {@link FaultBean }
     * 
     */
    public FaultBean createFaultBean() {
        return new FaultBean();
    }

    /**
     * Create an instance of {@link CtQrCode }
     * 
     */
    public CtQrCode createCtQrCode() {
        return new CtQrCode();
    }

    /**
     * Create an instance of {@link CtPaymentOptionDescription }
     * 
     */
    public CtPaymentOptionDescription createCtPaymentOptionDescription() {
        return new CtPaymentOptionDescription();
    }

    /**
     * Create an instance of {@link CtPaymentOptionBollettinoDescription }
     * 
     */
    public CtPaymentOptionBollettinoDescription createCtPaymentOptionBollettinoDescription() {
        return new CtPaymentOptionBollettinoDescription();
    }

    /**
     * Create an instance of {@link CtPaymentOptionsDescriptionList }
     * 
     */
    public CtPaymentOptionsDescriptionList createCtPaymentOptionsDescriptionList() {
        return new CtPaymentOptionsDescriptionList();
    }

    /**
     * Create an instance of {@link CtPaymentOptionsBollettinoDescriptionList }
     * 
     */
    public CtPaymentOptionsBollettinoDescriptionList createCtPaymentOptionsBollettinoDescriptionList() {
        return new CtPaymentOptionsBollettinoDescriptionList();
    }

    /**
     * Create an instance of {@link CtTransferPSP }
     * 
     */
    public CtTransferPSP createCtTransferPSP() {
        return new CtTransferPSP();
    }

    /**
     * Create an instance of {@link CtTransferListPSP }
     * 
     */
    public CtTransferListPSP createCtTransferListPSP() {
        return new CtTransferListPSP();
    }

    /**
     * Create an instance of {@link CtTransferPSPV2 }
     * 
     */
    public CtTransferPSPV2 createCtTransferPSPV2() {
        return new CtTransferPSPV2();
    }

    /**
     * Create an instance of {@link CtTransferListPSPV2 }
     * 
     */
    public CtTransferListPSPV2 createCtTransferListPSPV2() {
        return new CtTransferListPSPV2();
    }

    /**
     * Create an instance of {@link CtMarcaDaBollo }
     * 
     */
    public CtMarcaDaBollo createCtMarcaDaBollo() {
        return new CtMarcaDaBollo();
    }

    /**
     * Create an instance of {@link CtListaMarcheDaBollo }
     * 
     */
    public CtListaMarcheDaBollo createCtListaMarcheDaBollo() {
        return new CtListaMarcheDaBollo();
    }

    /**
     * Create an instance of {@link CtOutcomeDetails }
     * 
     */
    public CtOutcomeDetails createCtOutcomeDetails() {
        return new CtOutcomeDetails();
    }

    /**
     * Create an instance of {@link CtOutcomeDetailsV2 }
     * 
     */
    public CtOutcomeDetailsV2 createCtOutcomeDetailsV2() {
        return new CtOutcomeDetailsV2();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaBollettinoReq }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link VerificaBollettinoReq }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "verificaBollettinoReq")
    public JAXBElement<VerificaBollettinoReq> createVerificaBollettinoReq(VerificaBollettinoReq value) {
        return new JAXBElement<VerificaBollettinoReq>(_VerificaBollettinoReq_QNAME, VerificaBollettinoReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaBollettinoRes }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link VerificaBollettinoRes }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "verificaBollettinoRes")
    public JAXBElement<VerificaBollettinoRes> createVerificaBollettinoRes(VerificaBollettinoRes value) {
        return new JAXBElement<VerificaBollettinoRes>(_VerificaBollettinoRes_QNAME, VerificaBollettinoRes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerifyPaymentNoticeReq }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link VerifyPaymentNoticeReq }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "verifyPaymentNoticeReq")
    public JAXBElement<VerifyPaymentNoticeReq> createVerifyPaymentNoticeReq(VerifyPaymentNoticeReq value) {
        return new JAXBElement<VerifyPaymentNoticeReq>(_VerifyPaymentNoticeReq_QNAME, VerifyPaymentNoticeReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerifyPaymentNoticeRes }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link VerifyPaymentNoticeRes }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "verifyPaymentNoticeRes")
    public JAXBElement<VerifyPaymentNoticeRes> createVerifyPaymentNoticeRes(VerifyPaymentNoticeRes value) {
        return new JAXBElement<VerifyPaymentNoticeRes>(_VerifyPaymentNoticeRes_QNAME, VerifyPaymentNoticeRes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeReq }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeReq }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "activatePaymentNoticeReq")
    public JAXBElement<ActivatePaymentNoticeReq> createActivatePaymentNoticeReq(ActivatePaymentNoticeReq value) {
        return new JAXBElement<ActivatePaymentNoticeReq>(_ActivatePaymentNoticeReq_QNAME, ActivatePaymentNoticeReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeRes }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeRes }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "activatePaymentNoticeRes")
    public JAXBElement<ActivatePaymentNoticeRes> createActivatePaymentNoticeRes(ActivatePaymentNoticeRes value) {
        return new JAXBElement<ActivatePaymentNoticeRes>(_ActivatePaymentNoticeRes_QNAME, ActivatePaymentNoticeRes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeReq }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeReq }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "sendPaymentOutcomeReq")
    public JAXBElement<SendPaymentOutcomeReq> createSendPaymentOutcomeReq(SendPaymentOutcomeReq value) {
        return new JAXBElement<SendPaymentOutcomeReq>(_SendPaymentOutcomeReq_QNAME, SendPaymentOutcomeReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeRes }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeRes }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "sendPaymentOutcomeRes")
    public JAXBElement<SendPaymentOutcomeRes> createSendPaymentOutcomeRes(SendPaymentOutcomeRes value) {
        return new JAXBElement<SendPaymentOutcomeRes>(_SendPaymentOutcomeRes_QNAME, SendPaymentOutcomeRes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoInviaFlussoRendicontazioneRequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoInviaFlussoRendicontazioneRequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoInviaFlussoRendicontazioneRequest")
    public JAXBElement<NodoInviaFlussoRendicontazioneRequest> createNodoInviaFlussoRendicontazioneRequest(NodoInviaFlussoRendicontazioneRequest value) {
        return new JAXBElement<NodoInviaFlussoRendicontazioneRequest>(_NodoInviaFlussoRendicontazioneRequest_QNAME, NodoInviaFlussoRendicontazioneRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoInviaFlussoRendicontazioneResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoInviaFlussoRendicontazioneResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoInviaFlussoRendicontazioneResponse")
    public JAXBElement<NodoInviaFlussoRendicontazioneResponse> createNodoInviaFlussoRendicontazioneResponse(NodoInviaFlussoRendicontazioneResponse value) {
        return new JAXBElement<NodoInviaFlussoRendicontazioneResponse>(_NodoInviaFlussoRendicontazioneResponse_QNAME, NodoInviaFlussoRendicontazioneResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoChiediTemplateInformativaPSPRequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoChiediTemplateInformativaPSPRequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoChiediTemplateInformativaPSPRequest")
    public JAXBElement<NodoChiediTemplateInformativaPSPRequest> createNodoChiediTemplateInformativaPSPRequest(NodoChiediTemplateInformativaPSPRequest value) {
        return new JAXBElement<NodoChiediTemplateInformativaPSPRequest>(_NodoChiediTemplateInformativaPSPRequest_QNAME, NodoChiediTemplateInformativaPSPRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoChiediTemplateInformativaPSPResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoChiediTemplateInformativaPSPResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoChiediTemplateInformativaPSPResponse")
    public JAXBElement<NodoChiediTemplateInformativaPSPResponse> createNodoChiediTemplateInformativaPSPResponse(NodoChiediTemplateInformativaPSPResponse value) {
        return new JAXBElement<NodoChiediTemplateInformativaPSPResponse>(_NodoChiediTemplateInformativaPSPResponse_QNAME, NodoChiediTemplateInformativaPSPResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoChiediInformativaPARequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoChiediInformativaPARequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoChiediInformativaPARequest")
    public JAXBElement<NodoChiediInformativaPARequest> createNodoChiediInformativaPARequest(NodoChiediInformativaPARequest value) {
        return new JAXBElement<NodoChiediInformativaPARequest>(_NodoChiediInformativaPARequest_QNAME, NodoChiediInformativaPARequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoChiediInformativaPAResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoChiediInformativaPAResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoChiediInformativaPAResponse")
    public JAXBElement<NodoChiediInformativaPAResponse> createNodoChiediInformativaPAResponse(NodoChiediInformativaPAResponse value) {
        return new JAXBElement<NodoChiediInformativaPAResponse>(_NodoChiediInformativaPAResponse_QNAME, NodoChiediInformativaPAResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeRequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeRequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "demandPaymentNoticeRequest")
    public JAXBElement<DemandPaymentNoticeRequest> createDemandPaymentNoticeRequest(DemandPaymentNoticeRequest value) {
        return new JAXBElement<DemandPaymentNoticeRequest>(_DemandPaymentNoticeRequest_QNAME, DemandPaymentNoticeRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DemandPaymentNoticeResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "demandPaymentNoticeResponse")
    public JAXBElement<DemandPaymentNoticeResponse> createDemandPaymentNoticeResponse(DemandPaymentNoticeResponse value) {
        return new JAXBElement<DemandPaymentNoticeResponse>(_DemandPaymentNoticeResponse_QNAME, DemandPaymentNoticeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeV2Request }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeV2Request }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "activatePaymentNoticeV2Request")
    public JAXBElement<ActivatePaymentNoticeV2Request> createActivatePaymentNoticeV2Request(ActivatePaymentNoticeV2Request value) {
        return new JAXBElement<ActivatePaymentNoticeV2Request>(_ActivatePaymentNoticeV2Request_QNAME, ActivatePaymentNoticeV2Request.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeV2Response }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ActivatePaymentNoticeV2Response }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "activatePaymentNoticeV2Response")
    public JAXBElement<ActivatePaymentNoticeV2Response> createActivatePaymentNoticeV2Response(ActivatePaymentNoticeV2Response value) {
        return new JAXBElement<ActivatePaymentNoticeV2Response>(_ActivatePaymentNoticeV2Response_QNAME, ActivatePaymentNoticeV2Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeV2Request }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeV2Request }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "sendPaymentOutcomeV2Request")
    public JAXBElement<SendPaymentOutcomeV2Request> createSendPaymentOutcomeV2Request(SendPaymentOutcomeV2Request value) {
        return new JAXBElement<SendPaymentOutcomeV2Request>(_SendPaymentOutcomeV2Request_QNAME, SendPaymentOutcomeV2Request.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeV2Response }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SendPaymentOutcomeV2Response }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "sendPaymentOutcomeV2Response")
    public JAXBElement<SendPaymentOutcomeV2Response> createSendPaymentOutcomeV2Response(SendPaymentOutcomeV2Response value) {
        return new JAXBElement<SendPaymentOutcomeV2Response>(_SendPaymentOutcomeV2Response_QNAME, SendPaymentOutcomeV2Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoChiediCatalogoServiziV2Request }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoChiediCatalogoServiziV2Request }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoChiediCatalogoServiziV2Request")
    public JAXBElement<NodoChiediCatalogoServiziV2Request> createNodoChiediCatalogoServiziV2Request(NodoChiediCatalogoServiziV2Request value) {
        return new JAXBElement<NodoChiediCatalogoServiziV2Request>(_NodoChiediCatalogoServiziV2Request_QNAME, NodoChiediCatalogoServiziV2Request.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodoChiediCatalogoServiziV2Response }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link NodoChiediCatalogoServiziV2Response }{@code >}
     */
    @XmlElementDecl(namespace = "http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd", name = "nodoChiediCatalogoServiziV2Response")
    public JAXBElement<NodoChiediCatalogoServiziV2Response> createNodoChiediCatalogoServiziV2Response(NodoChiediCatalogoServiziV2Response value) {
        return new JAXBElement<NodoChiediCatalogoServiziV2Response>(_NodoChiediCatalogoServiziV2Response_QNAME, NodoChiediCatalogoServiziV2Response.class, null, value);
    }

}

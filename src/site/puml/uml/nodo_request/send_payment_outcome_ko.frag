__header__
SOAPAction: "sendPaymentOutcomeV2"
Ocp-Apim-Subscription-Key: //<nodo api-key>

__body__
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
	<soap:Body>
		<ns3:sendPaymentOutcomeV2Request
			xmlns:ns2="http://ws.pagamenti.telematici.gov/"
			xmlns:ns3="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
			<idPSP>//<psp id>//</idPSP>
			<idBrokerPSP>//<psp broker id>//</idBrokerPSP>
			<idChannel>//<channel id>//</idChannel>
			<password>PLACEHOLDER</password>
			<paymentTokens>
				<paymentToken>//<payment token #1>//</paymentToken>
				.
				.
				.
			</paymentTokens>
			<outcome>KO</outcome>
		</ns3:sendPaymentOutcomeV2Request>
	</soap:Body>
</soap:Envelope>
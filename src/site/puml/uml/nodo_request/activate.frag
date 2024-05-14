__header__
SOAPAction: "activatePaymentNoticeV2"
Ocp-Apim-Subscription-Key: //<nodo api-key>//

__body__
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
	<soap:Body>
		<ns3:activatePaymentNoticeV2Request
			xmlns:ns2="http://ws.pagamenti.telematici.gov/"
			xmlns:ns3="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
			<idPSP>//<psp id>//</idPSP>
			<idBrokerPSP>//<psp broker id>//</idBrokerPSP>
			<idChannel>//<channel id>//</idChannel>
			<password>PLACEHOLDER</password>
			<qrCode>
				<fiscalCode>//<pa tax code>//</fiscalCode>
				<noticeNumber>//<notice number>//</noticeNumber>
			</qrCode>
			<amount>//<amount>//</amount>
			<expirationTime>//<expiration time>//</expirationTime>
		</ns3:activatePaymentNoticeV2Request>
	</soap:Body>
</soap:Envelope>
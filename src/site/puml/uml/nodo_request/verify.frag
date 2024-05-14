__header__
SOAPAction: "verifyPaymentNotice"
Ocp-Apim-Subscription-Key: //<nodo api-key>//

__body__
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
	<soap:Body>
		<ns3:verifyPaymentNoticeReq
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
		</ns3:verifyPaymentNoticeReq>
	</soap:Body>
</soap:Envelope>
__body__
<soapenv:Envelope
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:common="http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/"
	xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
	<soapenv:Body>
		<nfp:activatePaymentNoticeV2Response>
			<outcome>OK</outcome>
			<totalAmount>//<amount>//</totalAmount>
			<paymentDescription>//<payment description>//</paymentDescription>
			<fiscalCodePA>//<pa tax code>//</fiscalCodePA>
			<companyName>//<institution name>//</companyName>
			<officeName>//<office name>//</officeName>
			<paymentToken>//<payment token>//</paymentToken>
			<transferList>
				<transfer>
					<idTransfer>1</idTransfer>
					<transferAmount>//<amount #1>//</transferAmount>
					<fiscalCodePA>//<pa tax code #1>//</fiscalCodePA>
					<IBAN>//<iban #1>//</IBAN>
					<remittanceInformation>//<remittance info #1>//</remittanceInformation>
					<transferCategory>//<category code #1>//</transferCategory>
					<metadata>
						<mapEntry>
							<key>//<key #1.1>//</key>
							<value>//<value #1.1>//</value>
						</mapEntry>
						.
						.
						.
					</metadata>
				</transfer>
				.
				.
				.
			</transferList>
			<metadata>
				<mapEntry>
					<key>//<key #1>//</key>
					<value>//<value #1>//</value>
				</mapEntry>
				.
				.
				.
			</metadata>
			<creditorReferenceId>//<iuv>//</creditorReferenceId>
		</nfp:activatePaymentNoticeV2Response>
	</soapenv:Body>
</soapenv:Envelope>
__body__
<soapenv:Envelope
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:common="http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/"
	xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
	<soapenv:Body>
		<nfp:verifyPaymentNoticeRes>
			<outcome>OK</outcome>
			<paymentList>
				<paymentOptionDescription>
					<amount>//<amount>//</amount>
					<options>EQ</options>
					<dueDate>//<due date>//</dueDate>
					<paymentNote>//<payment note>//</paymentNote>
				</paymentOptionDescription>
			</paymentList>
			<paymentDescription>//<payment description>//</paymentDescription>
			<fiscalCodePA>//<pa tax code>//</fiscalCodePA>
			<companyName>//<institution name>//</companyName>
			<officeName>//<office name>//</officeName>
		</nfp:verifyPaymentNoticeRes>
	</soapenv:Body>
</soapenv:Envelope>
__body__
<soapenv:Envelope
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:common="http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/"
	xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
	<soapenv:Body>
		<nfp:verifyPaymentNoticeRes>
			<outcome>KO</outcome>
			<fault>
				<faultCode>//<fault code>//</faultCode>
				<faultString>//<fault string>//</faultString>
				<id>//<fault id>//</id>
				<description>//<fault description>//</description>
			</fault>
		</nfp:verifyPaymentNoticeRes>
	</soapenv:Body>
</soapenv:Envelope>
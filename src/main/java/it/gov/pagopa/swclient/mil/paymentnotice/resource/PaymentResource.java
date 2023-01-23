package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNRepository;

@Path("/")
public class PaymentResource {

	@Inject
	protected NodeForPspWrapper wrapper;
	
    @Inject
    private PNRepository pnRepository;

	
	protected Uni<PspInfo> manageFindPspInfoByAcquirerId(String acquirerId) {
		Log.debugf("manageFindPspInfoByAcquirerId - find pspInfo by manageFindPspInfoByAcquirerId: %s ", acquirerId);
		
		
		 return pnRepository.findByIdOptional(acquirerId)
				 .onItem().transform(o -> o.orElseThrow(() -> 
				 			new NotFoundException(Response
								.status(Status.NOT_FOUND)
								.build())
						 )).map(t -> t.pspInfo);  

	}
	protected Tuple2<String,String> parseQrCode(String qrCode) {
		//PAGOPA|002|000000000000000000|00000000000|9999
		//the notice number is the third token
		//the paTaxCode is the forth token
		
		String[] tokens 	= qrCode.split("\\|");
		String noticenumber = tokens[2];
		String paTaxCode	= tokens[3];
		
		return Tuple2.of(paTaxCode, noticenumber);
	}
}

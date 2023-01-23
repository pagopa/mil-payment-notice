package it.gov.pagopa.swclient.mil.paymentnotice.exception;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import io.quarkus.logging.Log;

/**
 * Class used to intercept the error codes returned by the Rest call to the Node
 * called in the NodoService Class by the annotation @RegisterProvider(NodeResponseExceptionMapper.class) 
 * @author fabrizio.guerrini
 *
 */
public class NodeResponseExceptionMapper implements ResponseExceptionMapper<RuntimeException> {
	@Override
    public RuntimeException toThrowable(Response response) {
		Log.debugf("NodeResponseExceptionMapper arrived responseCode %s", response.getStatus());
		switch (response.getStatus()) {
			case 400,404,422: 
				Log.debugf("With this responseCode the returned response will be KO");
				return  new NodeExceptionManageKo();
			case 408:
				Log.debugf("With this responseCode the returned response will be OK");
				return  new NodeExceptionManageOk();
		
			default:
				Log.debugf("No responseCode to manage, throws the original exception");
				return new InternalServerErrorException();
			}
        
    }
}

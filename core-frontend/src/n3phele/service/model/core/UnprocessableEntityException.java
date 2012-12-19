/**
 * @author Nigel Cook
 *
 * (C) Copyright 2010-2011. All rights reserved.
 */
package n3phele.service.model.core;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UnprocessableEntityException extends WebApplicationException {
	private static final long serialVersionUID = 1L;

	/**
     * Create a HTTP 422 Unprocessable Entity exception.
     */
    public UnprocessableEntityException() {
        super(Response.status(422).build());
    }

    /**
     * Create a HTTP 422 (Unprocessable Entity) exception.
     * @param message the description of the semantic error associated with the 422 response.
     */
    public UnprocessableEntityException(String message) {
        super(Response.status(422).
                entity(message).type("text/plain").build());
    }
}
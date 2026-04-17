package org.tuvarna.resource.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof ConstraintViolationException cve) {

            Map<String, String> errors = cve.getConstraintViolations()
                    .stream()
                    .collect(Collectors.toMap(
                            this::extractFieldName,
                            ConstraintViolation::getMessage,
                            (msg1, msg2) -> msg1 + ", " + msg2
                    ));

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Validation failed",
                            "details", errors
                    ))
                    .build();
        }

        if (exception instanceof WebApplicationException wae) {

            int status = wae.getResponse().getStatus();

            return Response.status(status)
                    .entity(Map.of(
                            "error", wae.getMessage()
                    ))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                        "error", "Unexpected server error"
                ))
                .build();
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
}
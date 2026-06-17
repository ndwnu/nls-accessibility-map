package nu.ndw.nls.accessibilitymap.backend.accessibility.controller;

import static org.springframework.http.ResponseEntity.status;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.service.exception.AccessibilityLocationNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.APIErrorJson;
import nu.ndw.nls.springboot.api.error.dto.ErrorResponse;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.tracing.TracingService;
import nu.ndw.nls.springboot.web.error.RestControllerExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorHandlerController extends RestControllerExceptionHandler {

    public ErrorHandlerController(TracingService tracingService, ClockService clockService) {
        super(tracingService, clockService);
    }

    @ExceptionHandler(AccessibilityLocationNotFoundException.class)
    public ResponseEntity<APIErrorJson> handleAccessibilityLocationNotFoundException(AccessibilityLocationNotFoundException exception) {
        APIErrorJson restError = new APIErrorJson().message(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restError);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(ErrorResponse errorResponse) {

        return status(errorResponse.status()).body(new APIErrorJson()
                .message(errorResponse.errors().stream()
                        .map(error -> error.title() + ": " + error.description())
                        .collect(Collectors.joining("; "))));
    }
}

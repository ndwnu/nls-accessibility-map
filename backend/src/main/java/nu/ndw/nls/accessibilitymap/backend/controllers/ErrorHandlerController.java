package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.exceptions.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.exceptions.ResourceNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleTypeNotSupportedException;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleWeightRequiredException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.APIErrorJson;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorHandlerController extends ResponseEntityExceptionHandler {

    /**
     * Default handler, catches all exceptions that have no specific handlers. Does not expose any details about the
     * cause of the problem to avoid leaking security sensitive information.
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<APIErrorJson> handleInternalServerErrorException(Throwable exception) {
        log.error("Internal server occurred", exception);
        APIErrorJson restError = new APIErrorJson()
                .message("An internal server error occurred while processing this request");
        return ResponseEntity.internalServerError()
                .body(restError);
    }

    /**
     * Bad request handler for domain exceptions
     */
    @ExceptionHandler({VehicleTypeNotSupportedException.class, VehicleWeightRequiredException.class,
            IncompleteArgumentsException.class})
    public ResponseEntity<APIErrorJson> handleBadRequestException(RuntimeException exception) {
        APIErrorJson restError = new APIErrorJson()
                .message(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(restError);
    }

    /**
     * Bad request handler for MethodArgumentTypeMismatchExceptions thrown by spring framework
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<APIErrorJson> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        APIErrorJson restError = new APIErrorJson()
                .message("'" + exception.getPropertyName() + "' " + exception.getMessage());
        return ResponseEntity.badRequest()
                .body(restError);
    }

    /**
     * Make sure http 403 - forbidden is thrown
     */
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Void> handleAccessDenied(AccessDeniedException ignoredE) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Municipality not found handler
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<APIErrorJson> handleNotFoundException(ResourceNotFoundException exception) {
        APIErrorJson restError = new APIErrorJson()
                .message(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(restError);
    }

    /**
     * ConstraintViolationException from javax Validation, returns the exception message in the response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIErrorJson> handleConstraintViolationException(
            ConstraintViolationException exception) {
        Stream<String> messageStream = exception.getConstraintViolations()
                .stream()
                .map(c -> "'" + parsePropertyPath(c.getPropertyPath()) + "' " + c.getMessage());
        APIErrorJson validationError = getAPIErrorJson(messageStream);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(validationError);
    }

    private APIErrorJson getAPIErrorJson(Stream<String> messageStream) {
        return new APIErrorJson()
                .message(messageStream
                        // Sort messages to prevent random order breaking integration tests
                        .sorted()
                        .collect(Collectors.joining(", ")));
    }

    private static String parsePropertyPath(Path propertyPath) {
        if (propertyPath == null) {
            return "";
        } else if (propertyPath instanceof PathImpl pathImpl) {
            return pathImpl.getLeafNode().asString();
        } else {
            return propertyPath.toString();
        }
    }
}

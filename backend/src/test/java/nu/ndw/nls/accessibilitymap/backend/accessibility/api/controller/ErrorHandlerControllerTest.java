package nu.ndw.nls.accessibilitymap.backend.accessibility.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.backend.exception.IncompleteArgumentsException;
import nu.ndw.nls.accessibilitymap.backend.exception.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.generated.model.v1.APIErrorJson;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerControllerTest {

    private static final String MESSAGE = "test";

    private static final String PATH = "path";

    @InjectMocks
    private ErrorHandlerController errorHandlerController;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private ConstraintViolation<?> constraintViolation;

    @Mock
    private PathImpl pathImpl;

    @Mock
    private NodeImpl nodeImpl;

    @Mock
    private Path path;

    @Mock
    private MethodArgumentTypeMismatchException methodArgumentTypeMismatchException;

    @Test
    void handleInternalServerErrorException() {
        ResponseEntity<APIErrorJson> response = errorHandlerController.handleInternalServerErrorException(new Exception(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An internal server error occurred while processing this request");
    }

    @Test
    void handleBadRequestException() {
        IncompleteArgumentsException exception = new IncompleteArgumentsException("IncompleteArgumentsException");
        ResponseEntity<APIErrorJson> response = errorHandlerController.handleBadRequestException(
                new IncompleteArgumentsException("IncompleteArgumentsException"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    void handleApiException() {
        ApiException exception = new ApiException(UUID.randomUUID(), HttpStatus.BAD_REQUEST, "Title", "Message");
        ResponseEntity<APIErrorJson> response = errorHandlerController.handleApiException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    void handleAccessDenied() {
        ResponseEntity<Void> response = errorHandlerController.handleAccessDenied(new AccessDeniedException(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleMethodArgumentTypeMismatchException() {
        when(methodArgumentTypeMismatchException.getName()).thenReturn("name");
        when(methodArgumentTypeMismatchException.getValue()).thenReturn("value");
        ResponseEntity<APIErrorJson> response = errorHandlerController.handleMethodArgumentTypeMismatchException(
                methodArgumentTypeMismatchException);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Argument 'name' with value 'value' is not valid");
    }

    @Test
    void handleNotFoundException() {
        ResponseEntity<APIErrorJson> response = errorHandlerController.handleNotFoundException(new MunicipalityNotFoundException(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    void handleConstraintViolationException_pathNull() {
        when(constraintViolation.getPropertyPath()).thenReturn(null);

        handleConstraintViolationException("");
    }

    @Test
    void handleConstraintViolationException_pathImplLeafNodeAsString() {
        when(constraintViolation.getPropertyPath()).thenReturn(pathImpl);
        when(pathImpl.getLeafNode()).thenReturn(nodeImpl);
        when(nodeImpl.asString()).thenReturn(PATH);

        handleConstraintViolationException(PATH);
    }

    @Test
    void handleConstraintViolationException_pathToString() {
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(PATH);

        handleConstraintViolationException(PATH);
    }

    private void handleConstraintViolationException(String path) {
        when(constraintViolationException.getConstraintViolations()).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getMessage()).thenReturn(MESSAGE);

        ResponseEntity<APIErrorJson> response = errorHandlerController.handleConstraintViolationException(constraintViolationException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("'" + path + "' " + MESSAGE);
    }
}

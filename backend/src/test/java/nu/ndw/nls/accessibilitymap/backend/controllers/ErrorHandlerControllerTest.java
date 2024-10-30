package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.APIErrorJson;
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
    private static final String PROPERTY_NAME = "propertyName";

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
    void handleInternalServerErrorException_ok() {
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleInternalServerErrorException(new Exception(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("An internal server error occurred while processing this request");
    }

    @Test
    void handleBadRequestException_ok() {
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleBadRequestException(new RuntimeException(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    void handleAccessDenied_ok() {
        ResponseEntity<Void> response = errorHandlerController.handleAccessDenied(new AccessDeniedException(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleMethodArgumentTypeMismatchException_ok() {
        when(methodArgumentTypeMismatchException.getMessage()).thenReturn(MESSAGE);
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleMethodArgumentTypeMismatchException(methodArgumentTypeMismatchException);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    void handleNotFoundException_ok() {
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleNotFoundException(new MunicipalityNotFoundException(MESSAGE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    void handleConstraintViolationException_ok_pathNull() {
        when(constraintViolation.getPropertyPath()).thenReturn(null);

        handleConstraintViolationException_ok("");
    }

    @Test
    void handleConstraintViolationException_ok_pathImplLeafNodeAsString() {
        when(constraintViolation.getPropertyPath()).thenReturn(pathImpl);
        when(pathImpl.getLeafNode()).thenReturn(nodeImpl);
        when(nodeImpl.asString()).thenReturn(PATH);

        handleConstraintViolationException_ok(PATH);
    }

    @Test
    void handleConstraintViolationException_ok_pathToString() {
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(PATH);

        handleConstraintViolationException_ok(PATH);
    }

    private void handleConstraintViolationException_ok(String path) {
        when(constraintViolationException.getConstraintViolations()).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getMessage()).thenReturn(MESSAGE);

        ResponseEntity<APIErrorJson> response = errorHandlerController.handleConstraintViolationException(
                constraintViolationException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("'" + path + "' " + MESSAGE);
    }
}

package nu.ndw.nls.accessibilitymap.backend.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.APIErrorJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ErrorHandlerController errorHandlerController;


    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private ConstraintViolation constraintViolation;

    @Mock
    private MethodArgumentTypeMismatchException methodArgumentTypeMismatchException;

    @Mock
    private Path path;

    @BeforeEach
    void setup() {
        errorHandlerController = new ErrorHandlerController();
    }

    @Test
    void handleInternalServerErrorException_ok() {
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleInternalServerErrorException(new Exception(MESSAGE));

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("An internal server error occurred while processing this request");
    }

    @Test
    void handleBadRequestException_ok() {
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleBadRequestException(new RuntimeException(MESSAGE));
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo(MESSAGE);
    }

    @Test
    void handleAccessDenied_ok() {
        ResponseEntity<Void> response = errorHandlerController
                .handleAccessDenied(new AccessDeniedException(MESSAGE));
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    void handleMethodArgumentTypeMismatchException_ok() {
        when(methodArgumentTypeMismatchException.getPropertyName()).thenReturn(PROPERTY_NAME);
        when(methodArgumentTypeMismatchException.getMessage()).thenReturn(MESSAGE);
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleMethodArgumentTypeMismatchException(methodArgumentTypeMismatchException);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo(PROPERTY_NAME + " " + MESSAGE);
    }

    @Test
    void handleNotFoundException_ok() {
        ResponseEntity<APIErrorJson> response = errorHandlerController
                .handleNotFoundException(new MunicipalityNotFoundException(MESSAGE));
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo(MESSAGE);
    }

    @Test
    void handleConstraintViolationException_ok() {
        //noinspection unchecked
        when(constraintViolationException.getConstraintViolations())
                .thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(PATH);
        when(constraintViolation.getMessage()).thenReturn(MESSAGE);
        ResponseEntity<APIErrorJson> response = errorHandlerController.handleConstraintViolationException(
                constraintViolationException);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).isEqualTo(PATH + " " + MESSAGE);
    }
}

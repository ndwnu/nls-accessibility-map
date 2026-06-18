package nu.ndw.nls.accessibilitymap.backend.accessibility.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.service.exception.AccessibilityLocationNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.APIErrorJson;
import nu.ndw.nls.springboot.api.error.dto.ErrorResponse;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerControllerTest {

    private ErrorHandlerController errorHandlerController;

    @BeforeEach
    void setUp() {
        errorHandlerController = new ErrorHandlerController(null, null);
    }

    @Test
    void createResponseEntity() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .requestId("1")
                .timestamp(OffsetDateTime.now())
                .method("GET")
                .status(400)
                .path("/")
                .errors(List.of(
                        nu.ndw.nls.springboot.api.error.dto.Error.builder()
                                .id(UUID.randomUUID())
                                .title("Argument 'version' with value 'null' is not valid")
                                .description("must not be null")
                                .build(),
                        nu.ndw.nls.springboot.api.error.dto.Error.builder()
                                .id(UUID.randomUUID())
                                .title("Argument 'otherField' with value '' is not valid")
                                .description("must not be blank")
                                .build()))
                .build();

        ResponseEntity<Object> responseEntity = errorHandlerController.createResponseEntity(errorResponse);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.type(APIErrorJson.class))
                .extracting(APIErrorJson::getMessage)
                .isEqualTo("Argument 'version' with value 'null' is not valid: must not be null; "
                           + "Argument 'otherField' with value '' is not valid: must not be blank");
    }

    @Test
    void handleAccessibilityLocationNotFoundException() {
        ResponseEntity<APIErrorJson> responseEntity = errorHandlerController.handleAccessibilityLocationNotFoundException(
                new AccessibilityLocationNotFoundException(Location.builder()
                        .latitude(1.1)
                        .longitude(2.2)
                        .build()));

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.type(APIErrorJson.class))
                .extracting(APIErrorJson::getMessage)
                .isEqualTo("Location could not be resolved at 1.1, 2.2. Please try a different location that"
                           + " is closer to actual road sections in the network.");
    }

    @Test
    void handleAccessibilityLocationNotFoundException_exceptionHandlerAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                errorHandlerController.getClass(),
                ExceptionHandler.class,
                "handleAccessibilityLocationNotFoundException",
                annotation -> assertThat(annotation.value()).containsExactly(AccessibilityLocationNotFoundException.class));
    }
}

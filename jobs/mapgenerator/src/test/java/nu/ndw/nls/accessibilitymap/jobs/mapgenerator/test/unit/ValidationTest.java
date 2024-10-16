package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

public abstract class ValidationTest {

	@BeforeEach
	void setUpLocale() {

		Locale.setDefault(Locale.US);
	}

	protected void validate(Object objectToValidate, List<String> propertyErrors, List<String> errorMessages) {

		this.validate(objectToValidate, propertyErrors, errorMessages, List.of());
	}

	protected void validate(Object objectToValidate, List<String> propertyErrors, List<String> errorMessages, List<String> ignoreProperties) {

		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();

			Set<ConstraintViolation<Object>> errors = validator.validate(objectToValidate);

			List<String> propertyErrorsFound = errors.stream()
					.map(error -> error.getPropertyPath().toString())
					.filter(entry -> ignoreProperties.stream().noneMatch(entry::startsWith))
					.toList();

			assertThat(propertyErrorsFound).containsExactlyInAnyOrderElementsOf(propertyErrors);

			List<String> errorMessagesFound = errors.stream()
					.filter(entry -> ignoreProperties.stream()
							.noneMatch(ignore -> entry.getPropertyPath().toString().startsWith(ignore)))
					.map(ConstraintViolation::getMessage)
					.toList();

			assertThat(errorMessagesFound.stream().sorted().toList()).isEqualTo(errorMessages.stream().sorted().toList());
		}
	}

	@Test
	void annotation_validated() {

		assertThat(getClassToTest().getAnnotation(Validated.class)).isNotNull();
	}

	protected abstract Class<?> getClassToTest();

	protected void containsValidAnnotationForFields(Class<?> classToCheck, String... fieldsToCheck) {

		AnnotationUtil.fieldsContainsAnnotation(classToCheck, Valid.class, fieldsToCheck);
	}
}

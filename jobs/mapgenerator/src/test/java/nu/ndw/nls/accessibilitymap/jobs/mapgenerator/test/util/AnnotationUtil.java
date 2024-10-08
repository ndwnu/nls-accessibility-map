package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Consumer;

public class AnnotationUtil {

	public static <ANNOTATION extends Annotation> void methodsContainsAnnotation(final Class<?> classToCheck, Class<? extends ANNOTATION> annotationClass, final String methodName, Consumer<ANNOTATION> annotationTestCallback) {

		Arrays.stream(classToCheck.getDeclaredMethods())
			.filter(declaredMethod -> declaredMethod.getName().equals(methodName))
			.map(method -> method.getDeclaredAnnotation(annotationClass))
			.map(annotation -> annotationClass.cast(annotation))
			.findFirst()
			.ifPresentOrElse(
				aroundAnnotation -> {
					annotationTestCallback.accept(aroundAnnotation);
				},
				() -> fail(String.format("Missing annotation '%s' on method '%s'", annotationClass, methodName))
			);
	}

	public static <ANNOTATION extends Annotation> void classContainsAnnotation(final Class<?> classToCheck, Class<? extends ANNOTATION> annotationClass, Consumer<ANNOTATION> annotationTestCallback) {

		final var annotation = classToCheck.getAnnotation(annotationClass);
		assertThat(annotation).isNotNull();
		annotationTestCallback.accept(annotation);
	}
}

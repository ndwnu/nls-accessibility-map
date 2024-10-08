package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class AnnotationUtil {

    public static void fieldsContainsAnnotation(
			Class<?> classToCheck,
            Class<? extends Annotation> annotationClass,
			String... fieldsToCheck) {

		ArrayList<String> missedFields = new ArrayList<>();
		ArrayList<String> fieldsToCheckMutableList = new ArrayList<>(Arrays.asList(fieldsToCheck));

        Arrays.stream(classToCheck.getDeclaredFields()).forEach(field -> {
            if (fieldsToCheckMutableList.contains(field.getName())) {
                assertThat(field.getAnnotation(annotationClass)).isNotNull();
                fieldsToCheckMutableList.remove(field.getName());
            } else if (field.getAnnotation(annotationClass) != null) {
                missedFields.add(field.getName());
            }
        });

        //Validate that all field in the list have a Valid annotation
        assertThat(missedFields).isEmpty();
        assertThat(fieldsToCheckMutableList).isEmpty();
    }

    public static <ANNOTATION extends Annotation> void methodParameterContainsAnnotation(
			Class<?> classToCheck,
            Class<? extends ANNOTATION> annotationClass,
			String methodToCheck,
            String parameterToCheck, Consumer<ANNOTATION> annotationTestCallback) {

        Arrays.stream(classToCheck.getDeclaredMethods())
                .filter(declaredMethod -> declaredMethod.getName().equals(methodToCheck))
                .map(method -> Arrays.stream(method.getParameters())
                        .filter(parameter -> parameter.getName().equals(parameterToCheck))
                        .map(parameter -> parameter.getDeclaredAnnotation(annotationClass))
                        .filter(Objects::nonNull)
                        .map(annotation -> annotationClass.cast(annotation))
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresentOrElse(aroundAnnotation -> {
                            annotationTestCallback.accept(aroundAnnotation);
                        },
                        () -> fail(
                                String.format("Missing annotation '%s' on method '%s' parameter '%s'", annotationClass,
                                        methodToCheck, parameterToCheck)));
    }

    public static <ANNOTATION extends Annotation> void methodsContainsAnnotation(
			Class<?> classToCheck,
            Class<? extends ANNOTATION> annotationClass,
			String methodName,
            Consumer<ANNOTATION> annotationTestCallback) {

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

    public static <ANNOTATION extends Annotation> void classContainsAnnotation(
			Class<?> classToCheck,
            Class<? extends ANNOTATION> annotationClass,
			Consumer<ANNOTATION> annotationTestCallback) {

		ANNOTATION annotation = classToCheck.getAnnotation(annotationClass);
        assertThat(annotation).isNotNull();
        annotationTestCallback.accept(annotation);
    }
}

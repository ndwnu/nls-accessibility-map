package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

class BaseLoaderCommandTest {

    @Test
    void annotation_class_component() {

        AnnotationUtil.classContainsAnnotation(
                BaseLoaderCommand.class,
                Component.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void annotation_class_Command() {

        AnnotationUtil.classContainsAnnotation(
                BaseLoaderCommand.class,
                Command.class,
                annotation -> {
                    assertThat(annotation.name()).isEqualTo("jobs");
                    assertThat(annotation.subcommands()).containsExactlyInAnyOrder(GenerateGeoJsonCommand.class);
                }
        );
    }
}
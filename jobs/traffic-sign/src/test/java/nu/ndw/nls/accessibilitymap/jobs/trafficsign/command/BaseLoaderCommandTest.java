package nu.ndw.nls.accessibilitymap.jobs.trafficsign.command;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.springboot.messaging.commands.ConfigureRabbitMQCommand;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
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
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                BaseLoaderCommand.class,
                Command.class,
                annotation -> {
                    assertThat(annotation.name()).isEqualTo("jobs");
                    assertThat(annotation.subcommands()).containsExactlyInAnyOrder(
                            AnalyseCommand.class,
                            UpdateCacheCommand.class,
                            ConfigureRabbitMQCommand.class);
                }
        );
    }
}

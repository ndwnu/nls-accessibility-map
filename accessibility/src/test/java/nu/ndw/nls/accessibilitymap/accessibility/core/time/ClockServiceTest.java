package nu.ndw.nls.accessibilitymap.accessibility.core.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

class ClockServiceTest {

    private ClockService clockService;

    private Clock clock;

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(Instant.parse("2022-02-03T07:49:11.522059Z"), ZoneId.of("UTC"));
        clockService = new ClockService(clock);
    }

    @Test
    void now_ok() {

        assertThat(clockService.now()).isAtSameInstantAs(OffsetDateTime.now(clock));
    }

    @Test
    void class_serviceAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                clockService.getClass(),
                Service.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }
}

package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

@ExtendWith(MockitoExtension.class)
class TrafficSignDataServiceTest {

    private TrafficSignDataService trafficSignDataService;

    @Mock
    private TrafficSign trafficSign1;

    @Mock
    private TrafficSign trafficSign2;

    @Mock
    private TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    @BeforeEach
    void setUp() {

        trafficSignDataService = new TrafficSignDataService(trafficSignCacheReadWriter);
    }

    @Test
    void init() {

        when(trafficSignCacheReadWriter.read()).thenThrow(new RuntimeException("test exception"));
        assertThat(catchThrowable(() -> trafficSignDataService.init()))
                .hasMessage("test exception")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findAll() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));

        assertThat(trafficSignDataService.findAll()).isEmpty();

        trafficSignDataService.init();
        assertThat(trafficSignDataService.findAll()).containsExactlyInAnyOrder(trafficSign1, trafficSign2);
    }

    @Test
    void getTrafficSigns() {
        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));

        assertThat(trafficSignDataService.findAll()).isEmpty();

        trafficSignDataService.init();

        Set<TrafficSign> trafficSigns = trafficSignDataService.getTrafficSigns();
        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2);

        //should be cached
        Set<TrafficSign> cachedTrafficSigns = trafficSignDataService.getTrafficSigns();
        assertThat(cachedTrafficSigns).isEqualTo(trafficSigns);

        verify(trafficSignCacheReadWriter).read();
    }

    @Test
    @SuppressWarnings("java:S2925")
    void getTrafficSigns_threadSafe() throws InterruptedException {

        when(trafficSignCacheReadWriter.read()).thenAnswer(invocationOnMock -> {
            Thread.sleep(100);
            return Optional.of(new TrafficSigns(trafficSign1, trafficSign2));
        });

        trafficSignDataService.init();

        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            executorService.execute(() -> trafficSignDataService.getTrafficSigns());
            executorService.execute(() -> trafficSignDataService.getTrafficSigns());

            executorService.shutdown();
            assertThat(executorService.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        }

        verify(trafficSignCacheReadWriter).read();
    }

    @Test
    void class_configurationAnnotation() {

        AnnotationUtil.classContainsAnnotation(
                trafficSignDataService.getClass(),
                Service.class,
                annotation -> assertThat(annotation).isNotNull()
        );
    }

    @Test
    void init_annotation() {

        AnnotationUtil.methodContainsAnnotation(
                trafficSignDataService.getClass(),
                PostConstruct.class,
                "init",
                annotation -> assertThat(annotation).isNotNull()
        );
    }
}

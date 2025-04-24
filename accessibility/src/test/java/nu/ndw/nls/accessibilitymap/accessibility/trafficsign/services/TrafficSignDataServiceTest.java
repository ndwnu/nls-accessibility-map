package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance.TrafficSignRelevancy;
import nu.ndw.nls.accessibilitymap.accessibility.services.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
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
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    @Mock
    private TrafficSignRelevancy trafficSignRelevancy1;

    @Mock
    private TrafficSignRelevancy trafficSignRelevancy2;

    @Mock
    private NetworkCacheDataService networkCacheDataService;

    @BeforeEach
    void setUp() {

        trafficSignDataService = new TrafficSignDataService(
                trafficSignCacheReadWriter,
                List.of(trafficSignRelevancy1, trafficSignRelevancy2), networkCacheDataService);
    }

    @Test
    void init() {

        when(trafficSignCacheReadWriter.read()).thenThrow(new RuntimeException("test exception"));
        assertThat(catchThrowable(() -> trafficSignDataService.init()))
                .withFailMessage("test exception")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findAllBy() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));
        when(trafficSignRelevancy1.test(trafficSign1, accessibilityRequest)).thenReturn(true);
        when(trafficSignRelevancy2.test(trafficSign1, accessibilityRequest)).thenReturn(true);

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).containsExactly(trafficSign1);
    }

    @Test
    void findAllBy_notAllRelevantCriteriaSatisfied() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));
        when(trafficSignRelevancy1.test(trafficSign1, accessibilityRequest)).thenReturn(true);
        when(trafficSignRelevancy2.test(trafficSign1, accessibilityRequest)).thenReturn(false);

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();
    }

    @Test
    void getTrafficSigns() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        List<TrafficSign> trafficSigns = trafficSignDataService.getTrafficSigns();

        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2);

        //should be cached
        List<TrafficSign> cachedTrafficSigns = trafficSignDataService.getTrafficSigns();

        verify(trafficSignCacheReadWriter).read();
        verify(networkCacheDataService).create(argThat(t -> t.equals(trafficSigns)));
        assertThat(cachedTrafficSigns).isEqualTo(trafficSigns);
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

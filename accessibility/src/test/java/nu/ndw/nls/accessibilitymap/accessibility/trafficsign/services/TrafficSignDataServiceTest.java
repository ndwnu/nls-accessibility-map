package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
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

    @BeforeEach
    void setUp() throws IOException {

        trafficSignDataService = new TrafficSignDataService(trafficSignCacheReadWriter);
    }

    @Test
    void findAllBy() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));
        when(trafficSign1.isRelevant(accessibilityRequest)).thenReturn(true);
        when(trafficSign2.isRelevant(accessibilityRequest)).thenReturn(false);

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        assertThat(trafficSigns).containsExactly(trafficSign1);
    }

    @Test
    void getTrafficSigns() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));

        List<TrafficSign> trafficSigns = trafficSignDataService.getTrafficSigns();

        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2);

        //should be cached
        List<TrafficSign> cachedTrafficSigns = trafficSignDataService.getTrafficSigns();

        verify(trafficSignCacheReadWriter).read();
        assertThat(cachedTrafficSigns).isEqualTo(trafficSigns);
    }

    @Test
    @SuppressWarnings("java:S2925")
    void getTrafficSigns_threadSafe() throws InterruptedException {

        when(trafficSignCacheReadWriter.read()).thenAnswer(invocationOnMock -> {
            Thread.sleep(100);
            return Optional.of(new TrafficSigns(trafficSign1, trafficSign2));
        });

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

}

package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude.TrafficSignExclusion;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive.TrafficSignRestriction;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
    private TrafficSignRestriction trafficSignRestriction1;

    @Mock
    private TrafficSignRestriction trafficSignRestriction2;

    @Mock
    private TrafficSignExclusion trafficSignExclusion1;

    @Mock
    private TrafficSignExclusion trafficSignExclusion2;

    @Mock
    private NetworkCacheDataService networkCacheDataService;

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @BeforeEach
    void setUp() {

        trafficSignDataService = new TrafficSignDataService(
                trafficSignCacheReadWriter,
                List.of(trafficSignRestriction1, trafficSignRestriction2),
                List.of(trafficSignExclusion1, trafficSignExclusion2),
                networkCacheDataService,
                graphHopperService);
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
        when(trafficSignRestriction1.test(trafficSign1, accessibilityRequest)).thenReturn(true);
        when(trafficSignRestriction1.test(trafficSign2, accessibilityRequest)).thenReturn(true);

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).containsExactly(trafficSign1, trafficSign2);
    }

    @Test
    void findAllBy_notAllRestrictionsAreRestrictive() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));
        when(trafficSignRestriction1.test(trafficSign1, accessibilityRequest)).thenReturn(true);
        when(trafficSignRestriction1.test(trafficSign2, accessibilityRequest)).thenReturn(false);

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).containsExactly(trafficSign1);
    }

    @Test
    void findAllBy_excludedTrafficSign() {

        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));
        when(trafficSignExclusion1.test(trafficSign1, accessibilityRequest)).thenReturn(false);
        when(trafficSignExclusion2.test(trafficSign1, accessibilityRequest)).thenReturn(false);
        when(trafficSignExclusion1.test(trafficSign1, accessibilityRequest)).thenReturn(false);
        when(trafficSignExclusion2.test(trafficSign2, accessibilityRequest)).thenReturn(true);
        when(trafficSignRestriction1.test(trafficSign1, accessibilityRequest)).thenReturn(true);

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).containsExactly(trafficSign1);
    }

    @Test
    void getTrafficSigns() {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(trafficSignCacheReadWriter.read()).thenReturn(Optional.of(new TrafficSigns(trafficSign1, trafficSign2)));

        assertThat(trafficSignDataService.findAllBy(accessibilityRequest)).isEmpty();

        trafficSignDataService.init();
        List<TrafficSign> trafficSigns = trafficSignDataService.getTrafficSigns();

        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2);

        //should be cached
        List<TrafficSign> cachedTrafficSigns = trafficSignDataService.getTrafficSigns();

        verify(trafficSignCacheReadWriter).read();
        verify(networkCacheDataService).create(argThat(t -> t.equals(trafficSigns)), eq(networkGraphHopper));
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

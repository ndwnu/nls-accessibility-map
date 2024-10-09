package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AdditionalSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util.AnnotationUtil;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util.LoggerExtension;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityServiceTest {

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private AccessibilityService accessibilityService;

    @Mock
    private IsochroneServiceFactory isochroneServiceFactory;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private VehicleRestrictionsModelFactory vehicleRestrictionsModelFactory;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    private GeometryFactoryWgs84 geometryFactoryWgs84;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private QueryGraphConfigurer queryGraphConfigurer;

    @Mock
    private RoadSectionCombinator roadSectionCombinator;

    @Mock
    private ClockService clockService;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private Snap trafficSignSnap;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private QueryGraph queryGraph;

    @BeforeEach
    void setUp() {

        geometryFactoryWgs84 = new GeometryFactoryWgs84();

        accessibilityService = new AccessibilityService(
                isochroneServiceFactory,
                networkGraphHopper,
                vehicleRestrictionsModelFactory,
                trafficSignDataService,
                geometryFactoryWgs84,
                roadSectionMapper,
                queryGraphConfigurer,
                roadSectionCombinator,
                clockService
        );
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void calculateAccessibility(TrafficSignType trafficSignType) {

        when(clockService.now())
                .thenReturn(OffsetDateTime.MIN)
                .thenReturn(OffsetDateTime.MIN.plusMinutes(1).plusNanos(1000));

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(1d)
                .startLocationLongitude(2d)
                .trafficSignType(trafficSignType)
                .includeOnlyTimeWindowedSigns(true)
                .build();

        when(isochroneServiceFactory.createService(networkGraphHopper)).thenReturn(isochroneService);
        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        mockTrafficSignData(trafficSignType);

        ArgumentCaptor<List<Snap>> snapsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<AdditionalSnap>> additionalSnapsArgumentCaptor = ArgumentCaptor.forClass(List.class);

        try (MockedStatic<QueryGraph> mockStatic = Mockito.mockStatic(QueryGraph.class)) {
            mockStatic.when(() -> QueryGraph.create(eq(baseGraph), snapsArgumentCaptor.capture()))
                    .thenReturn(queryGraph);

            Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

            verify(queryGraphConfigurer).configure(eq(queryGraph), additionalSnapsArgumentCaptor.capture());

            loggerExtension.containsLog(
                    Level.DEBUG,
                    "Accessibility generation done. It took: 1001 ms"
            );
        }
    }

    private void mockTrafficSignData(TrafficSignType trafficSignType) {
        when(trafficSignDataService.findAllByType(trafficSignType)).thenReturn(List.of(trafficSign));
        when(trafficSign.hasTimeWindowedSign()).thenReturn(true);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(trafficSign.latitude(), trafficSign.longitude(),
                EdgeFilter.ALL_EDGES)).thenReturn(trafficSignSnap);
    }

    @Test
    void annotation_calculateAccessibility() {

        AnnotationUtil.methodsContainsAnnotation(
                accessibilityService.getClass(),
                Timed.class,
                "calculateAccessibility",
                annotation -> assertThat(annotation.description()).isEqualTo("Time spent calculating accessibility")
        );
    }
}
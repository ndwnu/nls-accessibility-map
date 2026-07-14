package nu.ndw.nls.accessibilitymap.job.trafficsign.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.TrafficSignBuilder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@ExtendWith(MockitoExtension.class)
class RebuildTrafficSignCacheCommandTest {

    private RebuildTrafficSignCacheCommand rebuildTrafficSignCacheCommand;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @Mock
    private TrafficSignService trafficSignService;

    @Mock
    private TrafficSignBuilder trafficSignBuilder;

    @Mock
    private TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json1;

    @Mock
    private TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json2;

    @Mock
    private TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json3;

    @Mock
    private TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json4;

    @Mock
    private TrafficSignPropertiesDtoV5Json trafficSignPropertiesDtoV5Json1;

    @Mock
    private TrafficSign trafficSign1;

    @Mock
    private TrafficSign trafficSign2;

    @Mock
    private TrafficSign trafficSign3;

    @Mock
    private NetworkDataService networkDataService;

    @Mock
    private NetworkData networkData;

    @Mock
    private LineString lineString;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        rebuildTrafficSignCacheCommand = new RebuildTrafficSignCacheCommand(
                trafficSignDataService,
                trafficSignService,
                trafficSignBuilder,
                networkDataService);
    }

    @Test
    void call_networkData_null() {
        int roadSectionId = 123;
        when(networkDataService.get())
                .thenReturn(null)
                .thenReturn(networkData);
        when(trafficSignService.getTrafficSigns(
                Arrays.stream(TrafficSignType.values())
                        .map(TrafficSignType::getRvvCode)
                        .collect(Collectors.toSet())))
                .thenReturn(List.of(trafficSignGeoJsonDtoV5Json1));
        when(trafficSignPropertiesDtoV5Json1.getRoadSectionId()).thenReturn(roadSectionId);
        when(trafficSignGeoJsonDtoV5Json1.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(networkData.findGeometryInNetwork(roadSectionId)).thenReturn(Optional.of(lineString));
        mockMapperCalls(trafficSignGeoJsonDtoV5Json1, lineString, trafficSign1);

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand).execute()).isZero();

        verify(trafficSignDataService).write(assertArg(trafficSigns -> assertThat(trafficSigns.get()).containsExactly(trafficSign1)));
        verify(networkDataService).read();
    }

    @Test
    void call_grapHopperNetwork_null() {
        int roadSectionId = 123;
        when(networkDataService.get()).thenReturn(networkData);
        when(trafficSignService.getTrafficSigns(
                Arrays.stream(TrafficSignType.values())
                        .map(TrafficSignType::getRvvCode)
                        .collect(Collectors.toSet())))
                .thenReturn(List.of(trafficSignGeoJsonDtoV5Json1));
        when(trafficSignPropertiesDtoV5Json1.getRoadSectionId()).thenReturn(roadSectionId);
        when(trafficSignGeoJsonDtoV5Json1.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(networkData.findGeometryInNetwork(roadSectionId)).thenReturn(Optional.of(lineString));
        mockMapperCalls(trafficSignGeoJsonDtoV5Json1, lineString, trafficSign1);

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand).execute()).isZero();

        verify(trafficSignDataService).write(assertArg(trafficSigns -> assertThat(trafficSigns.get()).containsExactly(trafficSign1)));
        verify(networkDataService).read();
    }

    @Test
    void call() {

        when(trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                .map(TrafficSignType::getRvvCode)
                .collect(Collectors.toSet()))).thenReturn(List.of(trafficSignGeoJsonDtoV5Json1, trafficSignGeoJsonDtoV5Json2,
                trafficSignGeoJsonDtoV5Json3, trafficSignGeoJsonDtoV5Json4));
        when(trafficSignPropertiesDtoV5Json1.getRoadSectionId()).thenReturn(123);
        when(trafficSignGeoJsonDtoV5Json1.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(trafficSignGeoJsonDtoV5Json2.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(trafficSignGeoJsonDtoV5Json3.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(trafficSignGeoJsonDtoV5Json4.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        NetworkGraphHopper networkGraphHopper = Mockito.mock(NetworkGraphHopper.class);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkData.findGeometryInNetwork(123L)).thenReturn(Optional.of(lineString));

        mockMapperCalls(trafficSignGeoJsonDtoV5Json1, lineString, trafficSign1);
        mockMapperCalls(trafficSignGeoJsonDtoV5Json2, lineString, trafficSign2);
        mockMapperCalls(trafficSignGeoJsonDtoV5Json3, lineString, trafficSign3);
        mockMapperCalls(trafficSignGeoJsonDtoV5Json4, lineString, null);

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand).execute()).isZero();

        verify(trafficSignDataService).write(assertArg(trafficSigns ->
                assertThat(trafficSigns.get()).containsExactlyInAnyOrder(trafficSign1, trafficSign2, trafficSign3)));

        verify(networkDataService, never()).read();
        loggerExtension.containsLog(Level.INFO, "Updating traffic signs");
    }

    @Test
    void call_roadSectionIsNull() {

        when(trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                .map(TrafficSignType::getRvvCode)
                .collect(Collectors.toSet()))).thenReturn(List.of(trafficSignGeoJsonDtoV5Json1, trafficSignGeoJsonDtoV5Json2,
                trafficSignGeoJsonDtoV5Json3, trafficSignGeoJsonDtoV5Json4));
        when(trafficSignPropertiesDtoV5Json1.getRoadSectionId()).thenReturn(null);
        when(trafficSignGeoJsonDtoV5Json1.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(trafficSignGeoJsonDtoV5Json2.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(trafficSignGeoJsonDtoV5Json3.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        when(trafficSignGeoJsonDtoV5Json4.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json1);
        NetworkGraphHopper networkGraphHopper = Mockito.mock(NetworkGraphHopper.class);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);


        mockMapperCalls(trafficSignGeoJsonDtoV5Json1, null, trafficSign1);
        mockMapperCalls(trafficSignGeoJsonDtoV5Json2, null, trafficSign2);
        mockMapperCalls(trafficSignGeoJsonDtoV5Json3, null, trafficSign3);
        mockMapperCalls(trafficSignGeoJsonDtoV5Json4, null, null);

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand).execute()).isZero();

        verify(trafficSignDataService).write(assertArg(trafficSigns ->
                assertThat(trafficSigns.get()).containsExactlyInAnyOrder(trafficSign1, trafficSign2, trafficSign3)));

        verify(networkData, never()).findGeometryInNetwork(anyLong());
        verify(networkDataService, never()).read();
        loggerExtension.containsLog(Level.INFO, "Updating traffic signs");
    }

    @Test
    void call_error() {

        when(trafficSignService.getTrafficSigns(anySet())).thenThrow(new RuntimeException("test exception"));

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand).execute()).isOne();

        loggerExtension.containsLog(Level.ERROR, "Failed updating traffic signs", "test exception");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                rebuildTrafficSignCacheCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name()).isEqualTo("rebuildTrafficSignCache")
        );
    }

    private void mockMapperCalls(TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json, LineString lineString, TrafficSign trafficSign) {

        when(trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                eq(lineString),
                eq(trafficSignGeoJsonDtoV5Json),
                any(AtomicInteger.class))
        ).thenReturn(Optional.ofNullable(trafficSign));
    }
}

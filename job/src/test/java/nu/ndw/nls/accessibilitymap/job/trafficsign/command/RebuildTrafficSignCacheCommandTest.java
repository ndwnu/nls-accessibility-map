package nu.ndw.nls.accessibilitymap.job.trafficsign.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.TrafficSignBuilder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
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
    private TrafficSignData trafficSignData;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto1;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto2;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto3;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto4;

    @Mock
    private TrafficSignPropertiesDto trafficSignPropertiesDto1;

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
    private NwbData nwbData;

    @Mock
    private LineString lineString;

    @Mock
    private NwbRoadSectionDto roadSection;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

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
    void call() {

        when(trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                .map(TrafficSignType::getRvvCode)
                .collect(Collectors.toSet()))).thenReturn(trafficSignData);
        when(trafficSignPropertiesDto1.getRoadSectionId()).thenReturn(123L);
        when(trafficSignGeoJsonDto1.getProperties()).thenReturn(trafficSignPropertiesDto1);
        when(trafficSignGeoJsonDto2.getProperties()).thenReturn(trafficSignPropertiesDto1);
        when(trafficSignGeoJsonDto3.getProperties()).thenReturn(trafficSignPropertiesDto1);
        when(trafficSignGeoJsonDto4.getProperties()).thenReturn(trafficSignPropertiesDto1);
        when(trafficSignData.trafficSignsByRoadSectionId()).thenReturn(Map.of(
                1L, List.of(trafficSignGeoJsonDto1, trafficSignGeoJsonDto2),
                2L, List.of(trafficSignGeoJsonDto3),
                3L, List.of(trafficSignGeoJsonDto4)
        ));
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.findAccessibilityNwbRoadSectionById(123L)).thenReturn(Optional.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSection.geometry()).thenReturn(lineString);

        mockMapperCalls(trafficSignGeoJsonDto1, trafficSign1);
        mockMapperCalls(trafficSignGeoJsonDto2, trafficSign2);
        mockMapperCalls(trafficSignGeoJsonDto3, trafficSign3);
        mockMapperCalls(trafficSignGeoJsonDto4, null);

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand).execute()).isZero();

        verify(trafficSignDataService).write(argThat(trafficSigns ->
                trafficSigns.size() == 3
                && trafficSigns.containsAll(List.of(trafficSign1, trafficSign2, trafficSign3))));

        loggerExtension.containsLog(Level.INFO, "Updating traffic signs");
    }

    @Test
    void call_error() {

        when(trafficSignService.getTrafficSigns(anySet())).thenThrow(new RuntimeException("test exception"));

        assertThat(new CommandLine(rebuildTrafficSignCacheCommand)
                .execute()
        ).isOne();

        loggerExtension.containsLog(Level.ERROR, "Failed updating traffic signs", "test exception");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                rebuildTrafficSignCacheCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name()).isEqualTo("update-cache")
        );
    }

    private void mockMapperCalls(TrafficSignGeoJsonDto trafficSignGeoJsonDto, TrafficSign trafficSign) {

        when(trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                eq(lineString),
                eq(trafficSignGeoJsonDto),
                any(AtomicInteger.class))
        ).thenReturn(Optional.ofNullable(trafficSign));
    }
}

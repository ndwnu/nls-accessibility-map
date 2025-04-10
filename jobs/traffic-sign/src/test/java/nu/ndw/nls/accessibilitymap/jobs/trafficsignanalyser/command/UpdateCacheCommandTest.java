//package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anySet;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import ch.qos.logback.classic.Level;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
//import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
//import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheReadWriter;
//import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
//import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.cache.TrafficSignMapper;
//import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
//import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
//import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
//import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
//import nu.ndw.nls.springboot.test.logging.LoggerExtension;
//import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.extension.RegisterExtension;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import picocli.CommandLine;
//import picocli.CommandLine.Command;
//
//@ExtendWith(MockitoExtension.class)
//class UpdateCacheCommandTest {
//
//    private UpdateCacheCommand updateCacheCommand;
//
//    @Mock
//    private TrafficSignCacheReadWriter trafficSignCacheReadWriter;
//
//    @Mock
//    private TrafficSignService trafficSignService;
//
//    @Mock
//    private TrafficSignMapper trafficSignMapper;
//
//    @Mock
//    private TrafficSignData trafficSignData;
//
//    @Mock
//    private TrafficSignGeoJsonDto trafficSignGeoJsonDto1;
//
//    @Mock
//    private TrafficSignGeoJsonDto trafficSignGeoJsonDto2;
//
//    @Mock
//    private TrafficSignGeoJsonDto trafficSignGeoJsonDto3;
//
//    @Mock
//    private TrafficSignGeoJsonDto trafficSignGeoJsonDto4;
//
//    @Mock
//    private TrafficSign trafficSign1;
//
//    @Mock
//    private TrafficSign trafficSign2;
//
//    @Mock
//    private TrafficSign trafficSign3;
//
//    @Mock
//    private GraphhopperMetaData graphhopperMetaData;
//
//    @Mock
//    private NwbRoadSectionCrudService roadSectionService;
//
//    @RegisterExtension
//    LoggerExtension loggerExtension = new LoggerExtension();
//
//    @BeforeEach
//    void setUp() {
//
//        updateCacheCommand = new UpdateCacheCommand(
//                trafficSignCacheReadWriter, trafficSignService, trafficSignMapper,
//                roadSectionService, graphhopperMetaData);
//    }
//
//    @Test
//    void call() {
//
//        when(trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
//                .map(TrafficSignType::getRvvCode)
//                .collect(Collectors.toSet()))).thenReturn(trafficSignData);
//
//        when(trafficSignData.trafficSignsByRoadSectionId()).thenReturn(Map.of(
//                1L, List.of(trafficSignGeoJsonDto1, trafficSignGeoJsonDto2),
//                2L, List.of(trafficSignGeoJsonDto3),
//                3L, List.of(trafficSignGeoJsonDto4)
//        ));
//
//        mockMapperCalls(trafficSignGeoJsonDto1, trafficSign1);
//        mockMapperCalls(trafficSignGeoJsonDto2, trafficSign2);
//        mockMapperCalls(trafficSignGeoJsonDto3, trafficSign3);
//        mockMapperCalls(trafficSignGeoJsonDto4, null);
//
//        assertThat(new CommandLine(updateCacheCommand).execute()).isZero();
//
//        verify(trafficSignCacheReadWriter).write(argThat(trafficSigns ->
//                trafficSigns.size() == 3
//                        && trafficSigns.containsAll(List.of(trafficSign1, trafficSign2, trafficSign3))));
//
//        loggerExtension.containsLog(Level.INFO, "Updating traffic signs");
//    }
//
//    @Test
//    void call_error() {
//
//        when(trafficSignService.getTrafficSigns(anySet())).thenThrow(new RuntimeException("test exception"));
//
//        assertThat(new CommandLine(updateCacheCommand)
//                .execute()
//        ).isOne();
//
//        loggerExtension.containsLog(Level.ERROR, "Failed updating traffic signs", "test exception");
//    }
//
//    @Test
//    void annotation_class_command() {
//
//        AnnotationUtil.classContainsAnnotation(
//                updateCacheCommand.getClass(),
//                Command.class,
//                annotation -> assertThat(annotation.name()).isEqualTo("update-cache")
//        );
//    }
//
//    private void mockMapperCalls(TrafficSignGeoJsonDto trafficSignGeoJsonDto, TrafficSign trafficSign) {
//
//        when(trafficSignMapper.mapFromTrafficSignGeoJsonDto(
//                eq(trafficSignGeoJsonDto),
//                any(IntegerSequenceSupplier.class))
//        ).thenReturn(Optional.ofNullable(trafficSign));
//    }
//}
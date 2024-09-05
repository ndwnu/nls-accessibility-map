package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService.ResultType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EffectivelyAccessibleGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.FullRoadSectionAccessibilityGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.TrafficSignSplitRoadSectionAccessibilityGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.VehicleTypeVehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GeoJsonOutputFormat;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateGeoJsonServiceTest {

    private static final int GENERATION_VERSION_INT = 20240102;
    private static final int NWB_VERSION_INT = 20240101;
    private static final double SEARCH_DISTANCE = Double.MAX_VALUE;
    private static final CmdGenerateGeoJsonType GENERATE_GEO_JSON_TYPE = CmdGenerateGeoJsonType.C6;
    private static final boolean PRODUCE_MESSAGE = true;

    @Mock
    private GenerateProperties generateProperties;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @Mock
    private AccessibilityMapService accessibilityMapService;

    @Mock
    private FileService fileService;

    @Mock
    private AccessibilityConfiguration accessibilityConfiguration;

    @Mock
    private MessageService messageService;

    @Mock
    private AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    @Mock
    private LocalDateVersionMapper localDateVersionMapper;

    @Mock
    private VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper;

    @Mock
    private EnrichTrafficSignService enrichTrafficSignService;

    @InjectMocks
    private GenerateGeoJsonService generateGeoJsonService;

    @Mock
    private AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    @Mock
    private Point startLocation;

    @Mock
    private SortedMap<Integer, RoadSection> idToRoadSectionSortedMap;

    @Mock
    private List<RoadSection> roadSections;

    @Mock
    private List<DirectionalRoadSectionAndTrafficSignGroupedById> directionalRoadSectionAndTrafficSignGroupedById;

    @Mock
    private DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedByIdA;

    @Mock
    private DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedByIdB;

    @Mock
    private List<DirectionalRoadSectionAndTrafficSign> directionalRoadSectionAndTrafficSignsA;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignA;

    @Mock
    private List<DirectionalRoadSectionAndTrafficSign> directionalRoadSectionAndTrafficSignsB;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignB;

    @Mock
    private AccessibilityGeoJsonFeatureCollection geoJson;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NlsEvent nlsEvent;

    @Mock
    private NlsEventSubject nlsEventSubject;

    @Mock
    private Path tmpFilePath;

    @Mock
    private File tmpFile;

    @Mock
    private VehicleProperties vehicleProperties;

    @Mock
    private FullRoadSectionAccessibilityGeoJsonMapper fullRoadSectionAccessibilityGeoJsonMapper;

    @Mock
    private TrafficSignSplitRoadSectionAccessibilityGeoJsonMapper trafficSignSplitRoadSectionAccessibilityGeoJsonMapper;

    @Mock
    private EffectivelyAccessibleGeoJsonMapper effectivelyAccessibleGeoJsonMapper;

    @Mock
    private EffectiveAccessibilityService effectiveAccessibilityService;


    @Test
    @SneakyThrows
    void generate_ok_fullRoadSections() {
        testByGeoJsonOutput(GeoJsonOutputFormat.FULL_ROAD_SECTION);
    }

    @Test
    @SneakyThrows
    void generate_ok_trafficSignSplit() {
        testByGeoJsonOutput(GeoJsonOutputFormat.TRAFFIC_SIGN_SPLIT);
    }

    @Test
    void generate_ok_effectivelyAccessibleSplit() {
        testByGeoJsonOutput(GeoJsonOutputFormat.EFFECTIVELY_ACCESSIBLE_SPLIT);
    }

    @SneakyThrows
    void testByGeoJsonOutput(GeoJsonOutputFormat geoJsonOutputFormat) {

        LocalDate versionDate = LocalDate.now();
        when(localDateVersionMapper.map(versionDate)).thenReturn(GENERATION_VERSION_INT);
        when(accessibilityConfiguration.accessibilityGraphhopperMetaData())
                .thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion()).thenReturn(NWB_VERSION_INT);

        when(generateConfiguration.getStartLocation()).thenReturn(startLocation);
        when(generateProperties.getSearchDistanceInMeters()).thenReturn(SEARCH_DISTANCE);

        when(vehicleTypeVehiclePropertiesMapper.map(GENERATE_GEO_JSON_TYPE)).thenReturn(vehicleProperties);

        when(accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties,
                startLocation, SEARCH_DISTANCE, ResultType.DIFFERENCE_OF_ADDED_RESTRICTIONS))
                .thenReturn(idToRoadSectionSortedMap);

        when(idToRoadSectionSortedMap.values()).thenReturn(roadSections);

        when(enrichTrafficSignService.addTrafficSigns(GENERATE_GEO_JSON_TYPE, roadSections))
                .thenReturn(directionalRoadSectionAndTrafficSignGroupedById);

        if (geoJsonOutputFormat == GeoJsonOutputFormat.FULL_ROAD_SECTION) {
            when(fullRoadSectionAccessibilityGeoJsonMapper.map(any(GeoJsonIdSequenceSupplier.class),
                    eq(directionalRoadSectionAndTrafficSignGroupedById), eq(NWB_VERSION_INT))).thenReturn(geoJson);
        } else if (geoJsonOutputFormat == GeoJsonOutputFormat.TRAFFIC_SIGN_SPLIT) {
            when(trafficSignSplitRoadSectionAccessibilityGeoJsonMapper.map(any(GeoJsonIdSequenceSupplier.class),
                    eq(directionalRoadSectionAndTrafficSignGroupedById), eq(NWB_VERSION_INT))).thenReturn(geoJson);
        } else {
            when(directionalRoadSectionAndTrafficSignGroupedById.stream()).thenReturn(
                    Stream.of(directionalRoadSectionAndTrafficSignGroupedByIdA,
                            directionalRoadSectionAndTrafficSignGroupedByIdB));

            when(effectiveAccessibilityService.determineEffectiveAccessibility(
                    directionalRoadSectionAndTrafficSignGroupedByIdA))
                    .thenReturn(directionalRoadSectionAndTrafficSignsA);
            when(directionalRoadSectionAndTrafficSignsA.stream())
                    .thenReturn(Stream.of(directionalRoadSectionAndTrafficSignA));

            when(effectiveAccessibilityService.determineEffectiveAccessibility(
                    directionalRoadSectionAndTrafficSignGroupedByIdB))
                    .thenReturn(directionalRoadSectionAndTrafficSignsB);
            when(directionalRoadSectionAndTrafficSignsB.stream())
                    .thenReturn(Stream.of(directionalRoadSectionAndTrafficSignB));

            when(effectivelyAccessibleGeoJsonMapper.map(any(GeoJsonIdSequenceSupplier.class),
                    eq(List.of(directionalRoadSectionAndTrafficSignA, directionalRoadSectionAndTrafficSignB)),
                    eq(NWB_VERSION_INT))).thenReturn(geoJson);
        }

        when(fileService.createTmpGeoJsonFile(GENERATE_GEO_JSON_TYPE)).thenReturn(tmpFilePath);
        when(tmpFilePath.toFile()).thenReturn(tmpFile);

        when(generateConfiguration.getObjectMapper()).thenReturn(objectMapper);

        when(accessibilityGeoJsonGeneratedEventMapper.map(eq(GENERATE_GEO_JSON_TYPE), eq(GENERATION_VERSION_INT),
                eq(NWB_VERSION_INT), any(Instant.class))).thenReturn(nlsEvent);

        when(nlsEvent.getType()).thenReturn(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT);
        when(nlsEvent.getSubject()).thenReturn(nlsEventSubject);
        when(nlsEventSubject.getType()).thenReturn(NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C6);

        generateGeoJsonService.generate(GENERATE_GEO_JSON_TYPE, geoJsonOutputFormat,
                PRODUCE_MESSAGE);

        verify(objectMapper).writeValue(tmpFile, geoJson);

        verify(messageService).publish(nlsEvent);
    }
}
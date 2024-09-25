package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.accessibility.services.NetworkService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.DirectionalRoadSectionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EnrichedRoadSectionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers.RvvCodeWindowTimeEncodedValueMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.DirectionalTrafficSignMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiRvvCodeMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrichTrafficSignServiceTest {

    private static final CmdGenerateGeoJsonType CMD_GENERATE_GEO_JSON_TYPE_C6 = CmdGenerateGeoJsonType.C6;
    private static final WindowTimeEncodedValue WINDOW_TIME_ENCODED_VALUE_C6 = WindowTimeEncodedValue.C6;
    private static final int ROAD_SECTION_A_ID = 123;

    @Mock
    private TrafficSignService trafficSignService;

    @Mock
    private TrafficSignApiRvvCodeMapper trafficSignApiRvvCodeMapper;

    @Mock
    private DirectionalRoadSectionMapper directionalRoadSectionMapper;

    @Mock
    private RvvCodeWindowTimeEncodedValueMapper rvvCodeWindowTimeEncodedValueMapper;

    @Mock
    private NetworkService networkService;

    @Mock
    private DirectionalTrafficSignMapper directionalTrafficSignMapper;

    @Mock
    private TrafficSignFilterService trafficSignFilterService;

    @Mock
    private EnrichedRoadSectionMapper enrichedRoadSectionMapper;

    @InjectMocks
    private EnrichTrafficSignService enrichTrafficSignService;

    @Mock
    private Set<String> rvvCodesC6;

    @Mock
    private RoadSection roadSectionA;
    @Mock
    private RoadSection roadSectionB;
    @Mock
    private TrafficSignData trafficSignDataA;
    @Mock
    private List<TrafficSignGeoJsonDto> roadSectionATrafficSignsFromApi;

    @Mock
    private TrafficSignGeoJsonDto roadSectionAFirstWindowTimeTrafficSignForward;
    @Mock
    private TrafficSignGeoJsonDto roadSectionASecondWindowTimeTrafficSignForward;
    @Mock
    private DirectionalTrafficSign firstDirectionalTrafficSignBackwards;

    @Mock
    private DirectionalRoadSection directionalRoadSectionAForward;
    @Mock
    private DirectionalRoadSection directionalRoadSectionABackwards;

    @Mock
    private DirectionalRoadSection directionalRoadSectionBForward;
    @Mock
    private DirectionalRoadSection directionalRoadSectionBBackwards;
    @Mock
    private DirectionalTrafficSign directionalTrafficSignAForward;
    @Mock
    private List<DirectionalRoadSectionAndTrafficSignGroupedById> directionalRoadSectionAndTrafficSignGroupedByIds;

    @Test
    void addTrafficSigns_ok() {
        when(rvvCodeWindowTimeEncodedValueMapper.map(CMD_GENERATE_GEO_JSON_TYPE_C6))
                .thenReturn(WINDOW_TIME_ENCODED_VALUE_C6);
        when(trafficSignApiRvvCodeMapper.mapRvvCode(CMD_GENERATE_GEO_JSON_TYPE_C6))
                .thenReturn(rvvCodesC6);

        // Create an example that has a traffic sign
        when(roadSectionA.getForwardAccessible()).thenReturn(Boolean.TRUE);
        when(roadSectionA.getBackwardAccessible()).thenReturn(Boolean.FALSE);
        when(roadSectionA.getRoadSectionId()).thenReturn(ROAD_SECTION_A_ID);

        // Entirely accessible, so no need to get any traffic signs
        when(roadSectionB.getForwardAccessible()).thenReturn(Boolean.TRUE);
        when(roadSectionB.getBackwardAccessible()).thenReturn(Boolean.TRUE);

        // Only A has backward inaccessible traffic sign
        when(networkService.hasWindowTimeByRoadSectionId( ROAD_SECTION_A_ID, WINDOW_TIME_ENCODED_VALUE_C6))
                .thenReturn(true);

        // Only a has a traffic sign and requires a lookup
        when(trafficSignService.getTrafficSigns(rvvCodesC6, Set.of((long) ROAD_SECTION_A_ID))).thenReturn(trafficSignDataA);

        when(trafficSignDataA.getTrafficSignsByRoadSectionId((long) ROAD_SECTION_A_ID))
                .thenReturn(roadSectionATrafficSignsFromApi);

        // Two signs in forward direction
        when(trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(roadSectionATrafficSignsFromApi,
                Direction.BACKWARD)).thenReturn(List.of( roadSectionAFirstWindowTimeTrafficSignForward,
                                                         roadSectionASecondWindowTimeTrafficSignForward));

        // No signs in reverse direction
        when(trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(roadSectionATrafficSignsFromApi,
                Direction.FORWARD)).thenReturn(List.of());

        // Only the first one should be used and mapped
        when(directionalTrafficSignMapper.map(roadSectionAFirstWindowTimeTrafficSignForward, Direction.BACKWARD))
                .thenReturn(firstDirectionalTrafficSignBackwards);

        when(directionalRoadSectionMapper.map(roadSectionA)).thenReturn(List.of(directionalRoadSectionAForward,
                directionalRoadSectionABackwards));
        when(directionalRoadSectionMapper.map(roadSectionB)).thenReturn(List.of(directionalRoadSectionBForward,
                directionalRoadSectionBBackwards));

        when(directionalRoadSectionAForward.getDirection()).thenReturn(Direction.FORWARD);
        when(directionalRoadSectionABackwards.getDirection()).thenReturn(Direction.BACKWARD);

        List<DirectionalRoadSectionAndTrafficSign> enrichedResultList =
                new ArrayList<>();

        enrichedResultList.add(DirectionalRoadSectionAndTrafficSign.builder()
                .roadSection(directionalRoadSectionAForward)
                .trafficSign(null)
                .build());

        enrichedResultList.add(DirectionalRoadSectionAndTrafficSign.builder()
                .roadSection(directionalRoadSectionABackwards)
                .trafficSign(firstDirectionalTrafficSignBackwards)
                .build());

        enrichedResultList.add(DirectionalRoadSectionAndTrafficSign.builder()
                .roadSection(directionalRoadSectionBForward)
                .trafficSign(null)
                .build());

        enrichedResultList.add(DirectionalRoadSectionAndTrafficSign.builder()
                .roadSection(directionalRoadSectionBBackwards)
                .trafficSign(null)
                .build());

        when(enrichedRoadSectionMapper.map(enrichedResultList)).thenReturn(
                directionalRoadSectionAndTrafficSignGroupedByIds);

        List<DirectionalRoadSectionAndTrafficSignGroupedById> result = enrichTrafficSignService.addTrafficSigns(
                CMD_GENERATE_GEO_JSON_TYPE_C6,
                List.of(roadSectionA, roadSectionB));

        assertThat(result).isEqualTo(directionalRoadSectionAndTrafficSignGroupedByIds);

    }
}

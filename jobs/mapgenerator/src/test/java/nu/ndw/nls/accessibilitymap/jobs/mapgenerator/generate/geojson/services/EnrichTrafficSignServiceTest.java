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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.RoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers.RvvCodeWindowTimeEncodedValueMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiRvvCodeMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignMapper;
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
    private TrafficSignMapper trafficSignMapper;

    @Mock
    private TrafficSignFilterService trafficSignFilterService;

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
    private TrafficSign firstTrafficSignForward;

    @Mock
    private DirectionalRoadSection directionalRoadSectionAForward;
    @Mock
    private DirectionalRoadSection directionalRoadSectionABackwards;

    @Mock
    private DirectionalRoadSection directionalRoadSectionBForward;
    @Mock
    private DirectionalRoadSection directionalRoadSectionBBackwards;
    @Mock
    private TrafficSign trafficSignAForward;

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

        when(networkService.hasWindowTimeByRoadSectionId((long) ROAD_SECTION_A_ID, WINDOW_TIME_ENCODED_VALUE_C6))
                .thenReturn(true);

        // Only a has a traffic sign and requires a lookup
        when(trafficSignService.getTrafficSigns(rvvCodesC6, Set.of(ROAD_SECTION_A_ID)))
                .thenReturn(trafficSignDataA);

        when(trafficSignDataA.getTrafficSignsByRoadSectionId((long) ROAD_SECTION_A_ID))
                .thenReturn(roadSectionATrafficSignsFromApi);

        // Two signs in forward direction
        when(trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(roadSectionATrafficSignsFromApi, true))
                .thenReturn(List.of(roadSectionAFirstWindowTimeTrafficSignForward,
                                    roadSectionASecondWindowTimeTrafficSignForward));

        // No signs in reverse direction
        when(trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(roadSectionATrafficSignsFromApi, false))
                .thenReturn(List.of());

        // Only the first one should be used and mapped
        when(trafficSignMapper.map(roadSectionAFirstWindowTimeTrafficSignForward)).thenReturn(firstTrafficSignForward);

        when(directionalRoadSectionMapper.map(roadSectionA)).thenReturn(List.of(directionalRoadSectionAForward,
                directionalRoadSectionABackwards));
        when(directionalRoadSectionMapper.map(roadSectionB)).thenReturn(List.of(directionalRoadSectionBForward,
                directionalRoadSectionBBackwards));

        when(directionalRoadSectionAForward.isForwards()).thenReturn(true);
        when(directionalRoadSectionABackwards.isForwards()).thenReturn(false);

        List<RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign>> result =
                enrichTrafficSignService.addTrafficSigns(CMD_GENERATE_GEO_JSON_TYPE_C6,
                        List.of(roadSectionA, roadSectionB));

        List<RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign>> expected = new ArrayList<>();

        expected.add(RoadSectionAndTrafficSign.<DirectionalRoadSection, TrafficSign>builder()
                .roadSection(directionalRoadSectionAForward)
                .trafficSign(firstTrafficSignForward)
                .build());

        expected.add(RoadSectionAndTrafficSign.<DirectionalRoadSection,
                        TrafficSign>builder()
                .roadSection(directionalRoadSectionABackwards)
                .trafficSign(null)
                .build());

        expected.add(RoadSectionAndTrafficSign.<DirectionalRoadSection, TrafficSign>builder()
                .roadSection(directionalRoadSectionBForward)
                .trafficSign(null)
                .build());

        expected.add(RoadSectionAndTrafficSign.<DirectionalRoadSection,
                        TrafficSign>builder()
                .roadSection(directionalRoadSectionBBackwards)
                .trafficSign(null)
                .build());

        assertThat(result).isEqualTo(expected);
    }
}
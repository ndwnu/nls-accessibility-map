package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalRoadSectionMapperTest {

    private static final int ROAD_SECTION_ID = 20;

    @InjectMocks
    private DirectionalRoadSectionMapper directionalRoadSectionMapper;

    @Mock
    private RoadSection roadSection;

    @Mock
    private LineString geometryForward;


    @Test
    void map_ok_bothAccessibleNullValuesNotAccessiblePriorRestrictions() {
        when(roadSection.getForwardAccessible()).thenReturn(null);
        when(roadSection.getBackwardAccessible()).thenReturn(null);
        assertEquals(Collections.emptyList(), directionalRoadSectionMapper.map(roadSection));
    }

    @Test
    void map_ok_forwardAccessibleOnly() {
        when(roadSection.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.getGeometry()).thenReturn(geometryForward);

        when(roadSection.getForwardAccessible()).thenReturn(true);
        when(roadSection.getBackwardAccessible()).thenReturn(null);
        assertEquals(List.of(
                DirectionalRoadSection.builder()
                        .nwbRoadSectionId(ROAD_SECTION_ID)
                        .nwbGeometry(geometryForward)
                        .accessible(true)
                        .direction(Direction.FORWARD)
                        .build()), directionalRoadSectionMapper.map(roadSection));
    }

    @Test
    void map_ok_backwardAccessibleOnly() {
        when(roadSection.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.getGeometry()).thenReturn(geometryForward);

        when(roadSection.getForwardAccessible()).thenReturn(null);
        when(roadSection.getBackwardAccessible()).thenReturn(true);
        assertEquals(List.of(
                DirectionalRoadSection.builder()
                        .nwbRoadSectionId(ROAD_SECTION_ID)
                        .nwbGeometry(geometryForward)
                        .direction(Direction.BACKWARD)
                        .accessible(true)
                        .build()), directionalRoadSectionMapper.map(roadSection));
    }

    @Test
    void map_ok_forwardAccessibleBackwardsInaccessible() {
        when(roadSection.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.getGeometry()).thenReturn(geometryForward);

        when(roadSection.getForwardAccessible()).thenReturn(true);
        when(roadSection.getBackwardAccessible()).thenReturn(false);
        assertEquals(List.of(
                DirectionalRoadSection.builder()
                        .nwbRoadSectionId(ROAD_SECTION_ID)
                        .nwbGeometry(geometryForward)
                        .accessible(false)
                        .direction(Direction.BACKWARD)
                        .build(),
                DirectionalRoadSection.builder()
                        .nwbRoadSectionId(ROAD_SECTION_ID)
                        .nwbGeometry(geometryForward)
                        .accessible(true)
                        .direction(Direction.FORWARD)
                        .build()), directionalRoadSectionMapper.map(roadSection));

    }

    @Test
    void map_ok_forwardInaccessibleBackwardsAccessible() {
        when(roadSection.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(roadSection.getGeometry()).thenReturn(geometryForward);

        when(roadSection.getForwardAccessible()).thenReturn(false);
        when(roadSection.getBackwardAccessible()).thenReturn(true);
        assertEquals(List.of(
                DirectionalRoadSection.builder()
                        .nwbRoadSectionId(ROAD_SECTION_ID)
                        .nwbGeometry(geometryForward)
                        .direction(Direction.BACKWARD)
                        .accessible(true)
                        .build(),
                DirectionalRoadSection.builder()
                        .nwbRoadSectionId(ROAD_SECTION_ID)
                        .nwbGeometry(geometryForward)
                        .accessible(false)
                        .direction(Direction.FORWARD)
                        .build()), directionalRoadSectionMapper.map(roadSection));

    }


}

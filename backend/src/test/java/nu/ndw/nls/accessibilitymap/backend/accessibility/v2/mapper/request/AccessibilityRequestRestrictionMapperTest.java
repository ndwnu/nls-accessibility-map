package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestRoadSectionRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestRestrictionMapperTest {

    private AccessibilityRequestRestrictionMapper accessibilityRequestRestrictionMapper;

    @Mock
    private AccessibilityRequestDirectionMapper accessibilityRequestDirectionMapper;

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private NetworkData networkData;

    @Mock
    private NwbData nwbData;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    @Mock
    private LineString roadSectionLineString;

    @Mock
    private CoordinateAndBearing coordinateAndBearing;

    @Mock
    private Coordinate coordinate;

    @BeforeEach
    void setUp() {

        accessibilityRequestRestrictionMapper = new AccessibilityRequestRestrictionMapper(
                accessibilityRequestDirectionMapper,
                fractionAndDistanceCalculator);
    }

    @Test
    void map() {
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.findAccessibilityNwbRoadSectionById(1)).thenReturn(Optional.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSection.geometry()).thenReturn(roadSectionLineString);
        when(fractionAndDistanceCalculator.getCoordinateAndBearing(roadSectionLineString, 2.0f)).thenReturn(coordinateAndBearing);

        when(coordinateAndBearing.coordinate()).thenReturn(coordinate);
        when(coordinate.getX()).thenReturn(3.0);
        when(coordinate.getY()).thenReturn(4.0);

        when(accessibilityRequestDirectionMapper.map(DirectionJson.BACKWARD)).thenReturn(Direction.BACKWARD);

        var accessibilityRequestRoadSectionRestrictionJson = AccessibilityRequestRoadSectionRestrictionJson.builder()
                .id(1)
                .direction(DirectionJson.BACKWARD)
                .fraction(2.0f)
                .build();

        Restriction restriction = accessibilityRequestRestrictionMapper.map(
                networkData,
                accessibilityRequestRoadSectionRestrictionJson);

        assertThat(restriction)
                .isNotNull()
                .isInstanceOf(RoadSectionRestriction.class);

        RoadSectionRestriction roadSectionRestriction = (RoadSectionRestriction) restriction;

        assertThat(roadSectionRestriction.id()).isEqualTo(1);
        assertThat(roadSectionRestriction.direction()).isEqualTo(Direction.BACKWARD);
        assertThat(roadSectionRestriction.fraction()).isEqualTo(accessibilityRequestRoadSectionRestrictionJson.getFraction().doubleValue());
        assertThat(roadSectionRestriction.networkSnappedLatitude()).isEqualTo(4.0);
        assertThat(roadSectionRestriction.networkSnappedLongitude()).isEqualTo(3.0);
    }

    @Test
    void map_unknownRestrictionType() {
        var accessibilityRequestRestrictionJson = Mockito.mock(AccessibilityRequestRestrictionJson.class);

        try {
            accessibilityRequestRestrictionMapper.map(
                    networkData,
                    accessibilityRequestRestrictionJson);
        } catch (ApiException exception) {

            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("c1f70586-ced2-43b8-b0fc-038dccad31ee"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid restriction type");
            assertThat(exception.getDescription())
                    .isEqualTo("Restriction type 'AccessibilityRequestRestrictionJson' is not a valid restriction type."
                               + " Please check the api specification for valid options.");
        }
    }

    @Test
    void map_coordinatesCouldNotBeSnappedToLine() {
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.findAccessibilityNwbRoadSectionById(1)).thenReturn(Optional.empty());

        var accessibilityRequestRoadSectionRestrictionJson = AccessibilityRequestRoadSectionRestrictionJson.builder()
                .id(1)
                .build();

        try {
            accessibilityRequestRestrictionMapper.map(networkData, accessibilityRequestRoadSectionRestrictionJson);
        } catch (ApiException exception) {
            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("355aba7d-4106-4aec-b0fc-94620647b37d"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid road section restriction");
            assertThat(exception.getDescription())
                    .isEqualTo("Road section with id '1' available in NWB version '0'. Please try a different road section.");
        }
    }
}

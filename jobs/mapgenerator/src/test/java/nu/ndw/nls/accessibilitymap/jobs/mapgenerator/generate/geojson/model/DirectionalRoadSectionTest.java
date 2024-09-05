package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalRoadSectionTest {

    @Mock
    private LineString lineString;

    @Mock
    private LineString reverseLineString;

    @Test
    void getGeometry_ok_isForwardOriginal() {
        assertThat(DirectionalRoadSection.builder()
                .direction(Direction.FORWARD)
                .nwbGeometry(lineString)
                .build().getGeometry()).isEqualTo(lineString);
    }

    @Test
    void getGeometry_ok_isBackwardsReversed() {

        when(lineString.reverse()).thenReturn(reverseLineString);

        assertThat(DirectionalRoadSection.builder()
                .direction(Direction.BACKWARD)
                .nwbGeometry(lineString)
                .build().getGeometry()).isEqualTo(reverseLineString);
    }
}
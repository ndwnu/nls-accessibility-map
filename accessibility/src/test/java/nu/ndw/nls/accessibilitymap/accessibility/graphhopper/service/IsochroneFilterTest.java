package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink.MUNICIPALITY_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneFilterTest {

    @Mock
    private IsoLabel isoLabel;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private IsochroneArguments isochroneArguments;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private PointList points;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue municipalityEncodeValueId;

    @ParameterizedTest
    @CsvSource(value = {
            "true, false",
            "false, true",
    })
    void isNotRoot(boolean isRoot, boolean expectedResult) {

        when(isoLabel.getEdge()).thenReturn(isRoot ? -1 : 1);

        assertThat(IsochroneFilter.isNotRoot(isoLabel)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true, true",
            "false, false",
            "null, false"
    }, nullValues = "null")
    void isWithinBoundingBox(Boolean boundingBoxIntersects, boolean expectedResult) {

        BBox boundingBox = mock(BBox.class);
        if (Objects.nonNull(boundingBoxIntersects)) {
            when(isochroneArguments.boundingBox()).thenReturn(boundingBox);

            when(isoLabel.getEdge()).thenReturn(1);
            when(isoLabel.getNode()).thenReturn(2);
            when(queryGraph.getEdgeIteratorState(1, 2)).thenReturn(edgeIteratorState);
            when(edgeIteratorState.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(points);
            when(boundingBox.intersects(points)).thenReturn(boundingBoxIntersects);

            assertThat(IsochroneFilter.isWithinBoundingBox(queryGraph, isoLabel, isochroneArguments)).isEqualTo(expectedResult);
        } else {
            assertThat(IsochroneFilter.isWithinBoundingBox(queryGraph, isoLabel, isochroneArguments)).isTrue();
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1, 1, true",
            "2, 1, false",
            "null, 1, true"
    }, nullValues = "null")
    void isWithinMunicipality(Integer isochroneArgumentMunicipalityId, Integer encodedMunicipalityId, boolean expectedResult) {

        when(isochroneArguments.municipalityId()).thenReturn(isochroneArgumentMunicipalityId);
        if (Objects.nonNull(isochroneArgumentMunicipalityId)) {

            when(isoLabel.getEdge()).thenReturn(1);
            when(isoLabel.getNode()).thenReturn(2);
            when(encodingManager.getIntEncodedValue(MUNICIPALITY_CODE)).thenReturn(municipalityEncodeValueId);
            when(queryGraph.getEdgeIteratorState(1, 2)).thenReturn(edgeIteratorState);
            when(edgeIteratorState.get(municipalityEncodeValueId)).thenReturn(encodedMunicipalityId);

            assertThat(IsochroneFilter.isWithinMunicipality(encodingManager, queryGraph, isoLabel, isochroneArguments)).isEqualTo(
                    expectedResult);
        } else {
            assertThat(IsochroneFilter.isWithinMunicipality(encodingManager, queryGraph, isoLabel, isochroneArguments)).isTrue();
        }
    }
}

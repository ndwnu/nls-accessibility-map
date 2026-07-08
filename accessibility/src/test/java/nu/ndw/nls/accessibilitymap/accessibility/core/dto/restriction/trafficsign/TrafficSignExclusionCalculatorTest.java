package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.util.shapes.BBox;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignExclusionCalculatorTest {

    private AccessibilityRequest accessibilityRequest;

    private TrafficSign trafficSign;

    @Mock
    private TransportRestrictions transportRestrictions;

    @Mock
    private BBox searchArea;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .latitude(2D)
                .longitude(3D)
                .textSigns(List.of())
                .transportRestrictions(transportRestrictions)
                .build();

        accessibilityRequest = AccessibilityRequest.builder()
                .searchArea(searchArea)
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, false",
            "true, false, true",
            "false, true, true",
            "false, false, true"
    })
    void isNotExcluded_hasExclusionsForZoneCodeTypes(boolean hasZoneType, boolean isExcluded, boolean expectedResult) {

        if(expectedResult) {
            when(searchArea.contains(trafficSign.latitude(), trafficSign.longitude())).thenReturn(true);
        }

        if (hasZoneType) {
            ZoneCodeType zoneCodeType = mock(ZoneCodeType.class);
            trafficSign = trafficSign.withZoneCodeType(zoneCodeType);
            if (isExcluded) {
                accessibilityRequest = accessibilityRequest.withExcludeTrafficSignZoneCodeTypes(Set.of(zoneCodeType));
            } else {
                accessibilityRequest = accessibilityRequest.withExcludeTrafficSignZoneCodeTypes(Set.of(mock(ZoneCodeType.class)));
            }
        } else {
            trafficSign = trafficSign.withZoneCodeType(null);
        }

        assertThat(TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, false",
            "true, false, true",
            "false, true, true",
            "false, false, true"
    })
    void isNotExcluded_hasExclusionsForTextSignTypes(
            boolean hasTrafficSignTextType,
            boolean matchesRequestTextSignType,
            boolean expectedResult) {

        if(expectedResult) {
            when(searchArea.contains(trafficSign.latitude(), trafficSign.longitude())).thenReturn(true);
        }

        TextSign textSign = mock(TextSign.class);
        trafficSign = trafficSign.withTextSigns(List.of(textSign));

        if (hasTrafficSignTextType) {
            TextSignType textSignType = mock(TextSignType.class);
            when(textSign.type()).thenReturn(textSignType);
            if (matchesRequestTextSignType) {
                accessibilityRequest = accessibilityRequest.withExcludeTrafficSignTextSignTypes(Set.of(textSignType));
            } else {
                accessibilityRequest = accessibilityRequest.withExcludeTrafficSignTextSignTypes(Set.of(mock(TextSignType.class)));
            }
        } else {
            when(textSign.type()).thenReturn(null);
        }

        assertThat(TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true, false",
            "true, false, true, true",
            "true, true, false, true",
            "true, false, false, true",
            "false, true, true, true",
            "false, false, true, true",
            "false, true, false, true",
            "false, false, false, true",
    })
    void isNotExcluded_hasExclusionsForEmissionZoneId(
            boolean requestHasExclusion,
            boolean trafficSignHasEmissionZone,
            boolean isExcluded,
            boolean expectedResult) {

        if(expectedResult) {
            when(searchArea.contains(trafficSign.latitude(), trafficSign.longitude())).thenReturn(true);
        }

        if (requestHasExclusion) {
            if (trafficSignHasEmissionZone) {
                EmissionZone emissionZone = mock(EmissionZone.class);
                when(transportRestrictions.emissionZone()).thenReturn(emissionZone);
                when(emissionZone.id()).thenReturn("s123");
                if (isExcluded) {
                    accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneIds(Set.of("s123"));
                } else {
                    accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneIds(Set.of("not_matching"));
                }
            } else {
                accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneIds(Set.of("s123"));
            }
        } else {
            accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneIds(null);
        }

        assertThat(TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true, false",
            "true, false, true, true",
            "true, true, false, true",
            "true, false, false, true",
            "false, true, true, true",
            "false, false, true, true",
            "false, true, false, true",
            "false, false, false, true",
    })
    void isNotExcluded_hasExclusionsForEmissionZoneType(
            boolean requestHasExclusion,
            boolean trafficSignHasEmissionZone,
            boolean isExcluded,
            boolean expectedResult) {

        if(expectedResult) {
            when(searchArea.contains(trafficSign.latitude(), trafficSign.longitude())).thenReturn(true);
        }

        if (requestHasExclusion) {
            if (trafficSignHasEmissionZone) {
                EmissionZone emissionZone = mock(EmissionZone.class);
                when(transportRestrictions.emissionZone()).thenReturn(emissionZone);
                EmissionZoneType emissionZoneType = mock(EmissionZoneType.class);
                when(emissionZone.type()).thenReturn(emissionZoneType);
                if (isExcluded) {
                    accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneTypes(Set.of(emissionZoneType));
                } else {
                    accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneTypes(Set.of(mock(EmissionZoneType.class)));
                }
            } else {
                accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneTypes(Set.of(mock(EmissionZoneType.class)));
            }
        } else {
            accessibilityRequest = accessibilityRequest.withExcludeRestrictionsWithEmissionZoneTypes(null);
        }

        assertThat(TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "true, false",
            "false, true",
    })
    void isNotExcluded_isOutsideOfSearchBoundingBox(boolean isExcluded, boolean expectedResult) {

        if (isExcluded) {
            when(searchArea.contains(trafficSign.latitude(), trafficSign.longitude())).thenReturn(false);
        } else {
            when(searchArea.contains(trafficSign.latitude(), trafficSign.longitude())).thenReturn(true);
        }

        assertThat(TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }
}

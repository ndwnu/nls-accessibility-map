package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionIsAbsoluteFilterPredicateTest {

    private static final TextSignType IGNORED_TOKEN_CONTAINS_UIT = TextSignType.EXCLUDING;
    private static final TextSignType IGNORED_TOKEN_CONTAINS_VOOR = TextSignType.PRE_ANNOUNCEMENT;
    private static final TextSignType IGNORED_TOKEN_CONTAINS_TIJD = TextSignType.TIME_PERIOD;
    private static final TextSignType IGNORED_TOKEN_CONTAINS_VRIJ = TextSignType.FREE_TEXT;

    @InjectMocks
    private RestrictionIsAbsoluteFilterPredicate restrictionIsAbsoluteFilterPredicate;

    @Test
    void test_ok_includedHasNoNullTextSigns() {
        TrafficSignPropertiesDto propertiesDto = Mockito.mock(TrafficSignPropertiesDto.class);
        TrafficSignGeoJsonDto trafficSignGeoJsonDto = Mockito.mock(TrafficSignGeoJsonDto.class);

        when(propertiesDto.getTextSigns()).thenReturn(null);
        when(trafficSignGeoJsonDto.getProperties()).thenReturn(propertiesDto);
        assertTrue(restrictionIsAbsoluteFilterPredicate.test(trafficSignGeoJsonDto));
    }

    @Test
    void test_ok_includedHasNoEmptyTextSignsList() {
        TrafficSignPropertiesDto propertiesDto = Mockito.mock(TrafficSignPropertiesDto.class);
        TrafficSignGeoJsonDto trafficSignGeoJsonDto = Mockito.mock(TrafficSignGeoJsonDto.class);

        when(propertiesDto.getTextSigns()).thenReturn(Collections.emptyList());
        when(trafficSignGeoJsonDto.getProperties()).thenReturn(propertiesDto);
        assertTrue(restrictionIsAbsoluteFilterPredicate.test(trafficSignGeoJsonDto));
    }

    @Test
    void test_ok_excludedBecauseItContainsUit() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_UIT));
    }

    @Test
    void test_ok_excludedBecauseItContainsVoor() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_VOOR));
    }

    @Test
    void test_ok_excludedBecauseItContainsTijd() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_TIJD));
    }

    @Test
    void test_ok_excludedBecauseItContainsVrij() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_VRIJ));
    }

    private TrafficSignGeoJsonDto mockSign(TextSignType textSignType) {
        TextSignDto mockedTextSign = Mockito.mock(TextSignDto.class);
        TrafficSignPropertiesDto propertiesDto = Mockito.mock(TrafficSignPropertiesDto.class);
        TrafficSignGeoJsonDto trafficSignGeoJsonDto = Mockito.mock(TrafficSignGeoJsonDto.class);

        when(mockedTextSign.getType()).thenReturn(textSignType);
        when(propertiesDto.getTextSigns()).thenReturn(List.of(mockedTextSign));
        when(trafficSignGeoJsonDto.getProperties()).thenReturn(propertiesDto);
        return trafficSignGeoJsonDto;
    }
}

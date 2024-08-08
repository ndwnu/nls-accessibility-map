package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionIsAbsoluteFilterPredicateTest {

    private static final String IGNORED_TOKEN_CONTAINS_UIT = "UIT";
    private static final String IGNORED_TOKEN_CONTAINS_VOOR = "VOOR";
    private static final String IGNORED_TOKEN_CONTAINS_TIJD = "TIJD";
    private static final String IGNORED_TOKEN_CONTAINS_VRIJ = "VRIJ";
    private static final String FORMAT_SOME_TEXT_TO_S_TEST_CONTAINS = "some text to %s test contains";

    @InjectMocks
    private RestrictionIsAbsoluteFilterPredicate restrictionIsAbsoluteFilterPredicate;

    @Test
    void test_ok_includedBecauseItUsesNoBlacklistedTokens() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign("als pasen en pinksteren op één dag vallen"));
    }

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
        restrictionIsAbsoluteFilterPredicate.test(
                mockSign(FORMAT_SOME_TEXT_TO_S_TEST_CONTAINS.formatted(IGNORED_TOKEN_CONTAINS_UIT)));
    }

    @Test
    void test_ok_excludedBecauseItContainsVoor() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_VOOR));
        restrictionIsAbsoluteFilterPredicate.test(
                mockSign(FORMAT_SOME_TEXT_TO_S_TEST_CONTAINS.formatted(IGNORED_TOKEN_CONTAINS_VOOR)));
    }

    @Test
    void test_ok_excludedBecauseItContainsTijd() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_TIJD));
        restrictionIsAbsoluteFilterPredicate.test(
                mockSign(FORMAT_SOME_TEXT_TO_S_TEST_CONTAINS.formatted(IGNORED_TOKEN_CONTAINS_TIJD)));
    }

    @Test
    void test_ok_excludedBecauseItContainsVrij() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign(IGNORED_TOKEN_CONTAINS_VRIJ));
        restrictionIsAbsoluteFilterPredicate.test(
                mockSign(FORMAT_SOME_TEXT_TO_S_TEST_CONTAINS.formatted(IGNORED_TOKEN_CONTAINS_VRIJ)));
    }

    private TrafficSignGeoJsonDto mockSign(String textSignType) {
        TextSignDto mockedTextSign = Mockito.mock(TextSignDto.class);
        TrafficSignPropertiesDto propertiesDto = Mockito.mock(TrafficSignPropertiesDto.class);
        TrafficSignGeoJsonDto trafficSignGeoJsonDto = Mockito.mock(TrafficSignGeoJsonDto.class);

        when(mockedTextSign.getType()).thenReturn(textSignType);
        when(propertiesDto.getTextSigns()).thenReturn(List.of(mockedTextSign));
        when(trafficSignGeoJsonDto.getProperties()).thenReturn(propertiesDto);
        return trafficSignGeoJsonDto;
    }
}

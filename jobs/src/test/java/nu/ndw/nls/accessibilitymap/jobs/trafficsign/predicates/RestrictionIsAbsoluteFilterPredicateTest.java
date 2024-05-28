package nu.ndw.nls.accessibilitymap.jobs.trafficsign.predicates;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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


    @Mock
    private TrafficSignJsonDtoV3 trafficSignJsonDtoV3;

    @Test
    void test_ok_includedBecauseItUsesNoBlacklistedTokens() {
        restrictionIsAbsoluteFilterPredicate.test(mockSign("als pasen en pinksteren op één dag vallen"));
    }

    @Test
    void test_ok_includedHasNoNullTextSigns() {
        when(trafficSignJsonDtoV3.getTextSigns()).thenReturn(null);
        assertTrue(restrictionIsAbsoluteFilterPredicate.test(trafficSignJsonDtoV3));
    }
    @Test
    void test_ok_includedHasNoEmptyTextSignsList() {
        when(trafficSignJsonDtoV3.getTextSigns()).thenReturn(Collections.emptyList());
        assertTrue(restrictionIsAbsoluteFilterPredicate.test(trafficSignJsonDtoV3));
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

    private TrafficSignJsonDtoV3 mockSign(String textSignType) {
        TrafficSignJsonDtoV3 mockSign = Mockito.mock(TrafficSignJsonDtoV3.class);
        TextSignJsonDtoV3 mockTextSign = Mockito.mock(TextSignJsonDtoV3.class);
        when(mockSign.getTextSigns()).thenReturn(List.of(mockTextSign));
        when(mockTextSign.getType()).thenReturn(textSignType);
        return mockSign;
    }
}

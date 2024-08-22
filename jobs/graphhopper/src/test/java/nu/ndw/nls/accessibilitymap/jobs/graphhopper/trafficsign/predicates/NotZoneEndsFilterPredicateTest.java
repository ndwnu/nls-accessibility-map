package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCodeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotZoneEndsFilterPredicateTest {

    @Mock
    private TrafficSignPropertiesDto trafficSignPropertiesDto;
    @Mock
    private TrafficSignGeoJsonDto trafficSignJsonDto;
    @InjectMocks
    NotZoneEndsFilterPredicate notZoneEndsFilterPredicate;

    @Test
    void test_ok_includingZoneBegins() {
        when(trafficSignPropertiesDto.getZoneCode()).thenReturn(ZoneCodeType.BEGIN.toString());
        when(trafficSignJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        assertTrue(notZoneEndsFilterPredicate.test(trafficSignJsonDto));
    }

    @Test
    void test_ok_includingNull() {
        when(trafficSignPropertiesDto.getZoneCode()).thenReturn(null);
        when(trafficSignJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        assertTrue(notZoneEndsFilterPredicate.test(trafficSignJsonDto));
    }

    @Test
    void test_ok_excludingZoneEnds() {
        when(trafficSignPropertiesDto.getZoneCode()).thenReturn(ZoneCodeType.END.toString());
        when(trafficSignJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        assertFalse(notZoneEndsFilterPredicate.test(trafficSignJsonDto));
    }
}
package nu.ndw.nls.accessibilitymap.accessibility.restriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionServiceTest {

    private RestrictionService restrictionService;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private TrafficSign trafficSignNotRestrictive;

    @Mock
    private TrafficSign trafficSignRestrictive;

    @BeforeEach
    void setUp() {

        restrictionService = new RestrictionService(trafficSignDataService);
    }

    @Test
    void findAllBy() {

        when(trafficSignDataService.findAll()).thenReturn(Set.of(trafficSignRestrictive, trafficSignNotRestrictive));
        when(trafficSignRestrictive.isRestrictive(accessibilityRequest)).thenReturn(true);
        when(trafficSignNotRestrictive.isRestrictive(accessibilityRequest)).thenReturn(false);

        Restrictions restrictions = restrictionService.findAllBy(accessibilityRequest);

        assertThat(restrictions).containsExactly(trafficSignRestrictive);
    }
}

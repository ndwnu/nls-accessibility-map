package nu.ndw.nls.accessibilitymap.backend.accessibility.services;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.backend.accessibility.service.PointMatchService;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointLocation;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.singlepoint.SinglePointMapMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointMatchServiceTest {

    private static final int CUTOFF_DISTANCE = 150;

    @Mock
    private MapMatcherFactory<SinglePointMapMatcher> singlePointMapMatcherMapMatcherFactory;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private SinglePointMapMatcher singlePointMapMatcher;

    @Mock
    private Point point;

    @Mock
    private SinglePointMatch singlePointMatch;

    @Mock
    private CandidateMatch candidateMatch;

    private PointMatchService pointMatchService;

    @BeforeEach
    void setUp() {
        when(singlePointMapMatcherMapMatcherFactory.createMapMatcher(networkGraphHopper, PROFILE.getName()))
                .thenReturn(singlePointMapMatcher);
        pointMatchService = new PointMatchService(singlePointMapMatcherMapMatcherFactory, networkGraphHopper);
    }

    @Test
    void match_success() {
        when(this.singlePointMapMatcher.match(SinglePointLocation.builder()
                .point(point)
                .cutoffDistance(CUTOFF_DISTANCE)
                .build())).thenReturn(singlePointMatch);
        when(singlePointMatch.getCandidateMatches()).thenReturn(List.of(candidateMatch));
        assertEquals(Optional.of(candidateMatch), pointMatchService.match(point));
    }

    @Test
    void match_noResult() {
        when(this.singlePointMapMatcher.match(SinglePointLocation.builder()
                .point(point)
                .cutoffDistance(CUTOFF_DISTANCE)
                .build())).thenReturn(singlePointMatch);
        when(singlePointMatch.getCandidateMatches()).thenReturn(Collections.emptyList());
        assertEquals(Optional.empty(), pointMatchService.match(point));
    }
}

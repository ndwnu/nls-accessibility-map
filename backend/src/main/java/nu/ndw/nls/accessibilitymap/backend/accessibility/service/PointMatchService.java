package nu.ndw.nls.accessibilitymap.backend.accessibility.service;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.PROFILE;

import java.util.Optional;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointLocation;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.singlepoint.SinglePointMapMatcher;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
public class PointMatchService {

    private static final int CUTOFF_DISTANCE = 150;

    private final SinglePointMapMatcher singlePointMapMatcher;

    public PointMatchService(
            MapMatcherFactory<SinglePointMapMatcher> singlePointMapMatcherMapMatcherFactory,
            NetworkGraphHopper networkGraphHopper
    ) {
        this.singlePointMapMatcher = singlePointMapMatcherMapMatcherFactory.createMapMatcher(networkGraphHopper, PROFILE.getName());
    }

    public Optional<CandidateMatch> match(Point point) {
        SinglePointMatch singlePointMatch = this.singlePointMapMatcher.match(SinglePointLocation.builder()
                .point(point)
                .cutoffDistance(CUTOFF_DISTANCE)
                .build());

        return singlePointMatch.getCandidateMatches().stream().findFirst();
    }
}

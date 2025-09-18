package nu.ndw.nls.accessibilitymap.accessibility.service;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointLocation;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.singlepoint.SinglePointMapMatcher;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointMatchService {

    private static final int CUTOFF_DISTANCE = 150;

    private final MapMatcherFactory<SinglePointMapMatcher> singlePointMapMatcherMapMatcherFactory;

    public Optional<CandidateMatch> match(NetworkGraphHopper networkGraphHopper, Point point) {

        SinglePointMapMatcher singlePointMapMatcher = singlePointMapMatcherMapMatcherFactory.createMapMatcher(
                networkGraphHopper,
                CAR_PROFILE.getName());

        SinglePointMatch singlePointMatch = singlePointMapMatcher.match(SinglePointLocation.builder()
                .point(point)
                .cutoffDistance(CUTOFF_DISTANCE)
                .build());

        return singlePointMatch.getCandidateMatches().stream().findFirst();
    }
}

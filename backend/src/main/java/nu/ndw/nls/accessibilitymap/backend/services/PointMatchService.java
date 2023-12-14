package nu.ndw.nls.accessibilitymap.backend.services;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.domain.SinglePointMapMatcher;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointLocation;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PointMatchService {
    private final SinglePointMapMatcher singlePointMapMatcher;

    public PointMatchService(MapMatcherFactory<SinglePointMapMatcher> singlePointMapMatcherMapMatcherFactory,
            NetworkGraphHopper networkGraphHopper) {
        this.singlePointMapMatcher = singlePointMapMatcherMapMatcherFactory.createMapMatcher(networkGraphHopper);
    }

    public Optional<CandidateMatch> match(Point point) {

        SinglePointMatch singlePointMatch = this.singlePointMapMatcher.match(SinglePointLocation.builder()
                        .point(point)
                        .build());

        List<CandidateMatch> candidateMatches = singlePointMatch.getCandidateMatches();
        if (CollectionUtils.isEmpty(candidateMatches)) {
            return Optional.empty();
        }

        return singlePointMatch.getCandidateMatches().stream().findFirst();
    }



}

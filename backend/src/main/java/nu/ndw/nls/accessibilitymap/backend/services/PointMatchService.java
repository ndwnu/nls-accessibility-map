package nu.ndw.nls.accessibilitymap.backend.services;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.routingmapmatcher.domain.SinglePointMapMatcher;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointLocation;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopperFactory;
import nu.ndw.nls.routingmapmatcher.graphhopper.singlepoint.GraphHopperSinglePointMapMatcherFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PointMatchService {
    private final SinglePointMapMatcher singlePointMapMatcher;

    public PointMatchService(NetworkGraphHopper networkGraphHopper) {

        GraphHopperSinglePointMapMatcherFactory factory = new GraphHopperSinglePointMapMatcherFactory(
                new NetworkGraphHopperFactory());

        this.singlePointMapMatcher = factory.createMapMatcher(networkGraphHopper);
    }

    public Optional<CandidateMatch> match(Point point) {

        SinglePointMatch matcher = this.singlePointMapMatcher.match(SinglePointLocation.builder()
                        .point(point)
                        .build());

        List<CandidateMatch> candidateMatches = matcher.getCandidateMatches();
        if (CollectionUtils.isEmpty(candidateMatches)) {
            return Optional.empty();
        }

        return matcher.getCandidateMatches().stream().findFirst();
    }



}

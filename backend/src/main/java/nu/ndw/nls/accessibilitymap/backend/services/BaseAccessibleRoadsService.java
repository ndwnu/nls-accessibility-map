package nu.ndw.nls.accessibilitymap.backend.services;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.routingmapmatcher.domain.AccessibilityMap;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.AccessibilityRequest;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BaseAccessibleRoadsService {

    private final MapMatcherFactory<AccessibilityMap> accessibilityMapFactory;
    private final NetworkGraphHopper networkGraphHopper;

    @Cacheable(key = "#municipality.municipalityId", cacheNames = "baseAccessibleRoadsByMunicipality", sync = true)
    public Set<IsochroneMatch> getBaseAccessibleRoadsByMunicipality(Municipality municipality) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory
                .createMapMatcher(networkGraphHopper);
        AccessibilityRequest accessibilityRequest = AccessibilityRequest
                .builder()
                .startPoint(municipality.getStartPoint())
                .municipalityId(municipality.municipalityIdAsInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();
        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }
}

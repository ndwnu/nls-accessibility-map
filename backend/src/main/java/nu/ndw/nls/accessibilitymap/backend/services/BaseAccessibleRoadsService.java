package nu.ndw.nls.accessibilitymap.backend.services;

import com.google.common.base.Stopwatch;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseAccessibleRoadsService {

    private final AccessibilityMapFactory accessibilityMapFactory;
    private final NetworkGraphHopper networkGraphHopper;

    @Cacheable(key = "#municipality.municipalityId", cacheNames = "baseAccessibleRoadsByMunicipality", sync = true)
    public Set<IsochroneMatch> getBaseAccessibleRoadsByMunicipality(Municipality municipality) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);

        AccessibilityRequest accessibilityRequest = AccessibilityRequest
                .builder()
                .startPoint(municipality.getStartPoint())
                .municipalityId(municipality.getMunicipalityIdAsInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();
        Stopwatch timerAll = Stopwatch.createStarted();
        Set<IsochroneMatch> isochroneMatches = accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
        log.trace("Calculating accessible network took {} ", timerAll.stop());
        return isochroneMatches;

    }
}

package nu.ndw.nls.accessibilitymap.accessibility.services;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetworkCacheDataService {

    private final QueryGraphFactory queryGraphFactory;
    private final TrafficSignSnapMapper trafficSignSnapMapper;
    private final IsochroneServiceFactory isochroneServiceFactory;
    private final RoadSectionMapper roadSectionMapper;
    private final NetworkGraphHopper networkGraphHopper;
    private final ReentrantLock dataLock = new ReentrantLock();

    private QueryGraph queryGraph;
    private Map<String, TrafficSignSnap> trafficSignSnaps;
    private Map<Integer, Collection<RoadSection>> baseAccessibilityByMunicipalityId = new HashMap<>();

    public void create(TrafficSigns trafficSigns) {
        List<TrafficSignSnap> trafficSignSnapList = trafficSignSnapMapper.map(trafficSigns.stream()
                .toList());
        Map<String, TrafficSignSnap> newTrafficSignSnaps = trafficSignSnapList.stream()
                .collect(Collectors.toMap(t -> t.getTrafficSign().externalId(),
                        Function.identity()));
        dataLock.lock();
        try {
            baseAccessibilityByMunicipalityId = new HashMap<>();
            trafficSignSnaps = newTrafficSignSnaps;
            queryGraph = queryGraphFactory.createQueryGraph(trafficSignSnapList);
        } finally {
            dataLock.unlock();
        }
    }

    public List<TrafficSignSnap> getTrafficSignSnaps(List<String> trafficSignsIds) {
        if (trafficSignSnaps == null) {
            throw new IllegalStateException("No trafficSignSnaps available");
        }
        dataLock.lock();
        try {
            return trafficSignsIds.stream()
                    .filter(trafficSignSnaps::containsKey)
                    .map(trafficSignSnaps::get)
                    .toList();
        } finally {
            dataLock.unlock();
        }
    }

    public QueryGraph getQueryGraph() {
        if (queryGraph == null) {
            throw new IllegalStateException("No queryGraph available");
        }
        dataLock.lock();
        try {
            return queryGraph;
        } finally {
            dataLock.unlock();
        }
    }

    public Collection<RoadSection> getBaseAccessibility(Integer municipalityId, Snap snap, double searchRadiusInMeters) {
        if (municipalityId == null) {
            return calculateBaseAccessibility(null, snap, searchRadiusInMeters);
        } else {
            dataLock.lock();
            try {
                return baseAccessibilityByMunicipalityId.computeIfAbsent(municipalityId,
                                id -> calculateBaseAccessibility(municipalityId, snap, searchRadiusInMeters)).stream()
                        .map(RoadSection::clone)
                        // assign traffic signs here in separate class (geen mapper)
                        .toList();
            } finally {
                dataLock.unlock();
            }
        }

    }

    private Collection<RoadSection> calculateBaseAccessibility(Integer municipalityId, Snap snappedPoint, double searchRadiusInMeters) {
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);
        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(new RestrictionWeightingAdapter(
                                        networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()),
                                        Set.of()))
                                .municipalityId(municipalityId)
                                .searchDistanceInMetres(searchRadiusInMeters)
                                .build(),
                        getQueryGraph(),
                        snappedPoint), Collections.emptyMap());
    }


}

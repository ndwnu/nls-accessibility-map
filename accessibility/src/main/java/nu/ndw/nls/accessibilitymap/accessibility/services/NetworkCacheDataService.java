package nu.ndw.nls.accessibilitymap.accessibility.services;

import static java.util.stream.Collectors.toCollection;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.ArrayList;
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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
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
    private final RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner;
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

    /**
     * Retrieves a collection of road sections representing the base accessibility for a given municipality or area based on specified
     * parameters, such as the municipality ID, snap point, search radius, and associated traffic signs. If a municipality ID is provided,
     * it computes and caches the result; otherwise, it calculates the accessibility directly without caching.
     *
     * @param municipalityId        the ID of the municipality for which to retrieve the base accessibility. If null, accessibility is
     *                              calculated without caching.
     * @param snap                  the snap point representing the geographic location from which to begin the calculation.
     * @param searchRadiusInMeters  the search radius (in meters) used to determine the area of interest.
     * @param trafficSignsByEdgeKey a map of traffic signs grouped by edge key that will be used to assign traffic signs to the road
     *                              sections.
     * @return a collection of road sections representing the base accessibility, including assigned traffic signs, where applicable.
     */
    public Collection<RoadSection> getBaseAccessibility(Integer municipalityId, Snap snap, double searchRadiusInMeters,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {
        if (municipalityId == null) {
            return calculateBaseAccessibility(null, snap, searchRadiusInMeters);
        } else {
            dataLock.lock();
            try {
                return baseAccessibilityByMunicipalityId.computeIfAbsent(municipalityId,
                                id -> calculateBaseAccessibility(municipalityId, snap, searchRadiusInMeters)).stream()
                        .map(RoadSection::cloneRoadSection)
                        .map(r -> roadSectionTrafficSignAssigner.assignTrafficSigns(r, trafficSignsByEdgeKey))
                        .collect(toCollection(ArrayList::new));
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

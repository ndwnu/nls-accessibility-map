package nu.ndw.nls.accessibilitymap.accessibility.services;

import static java.util.stream.Collectors.toCollection;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.EdgeRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

/**
 * The NetworkCacheDataService class provides functionality to manage cached traffic sign data, road section accessibility, and query graph
 * generation within a network. It ensures thread-safe access and modification of the cached data through the use of a lock mechanism.
 * <p>
 * This service is responsible for: - Creating and maintaining a query graph based on traffic sign data. - Retrieving snapped traffic sign
 * data for specified traffic sign IDs. - Calculating and managing base accessibility of road sections by municipality, including optional
 * caching.
 * <p>
 * The class ensures data consistency and protection against concurrent access through the use of a ReentrantLock.
 */
@Component
@RequiredArgsConstructor
public class NetworkCacheDataService {

    private final QueryGraphFactory queryGraphFactory;
    private final TrafficSignSnapMapper trafficSignSnapMapper;
    private final IsochroneServiceFactory isochroneServiceFactory;
    private final RoadSectionMapper roadSectionMapper;
    private final NetworkGraphHopper networkGraphHopper;
    private final RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner;
    private final QueryGraphConfigurer queryGraphConfigurer;
    private final ReentrantLock dataLock = new ReentrantLock();

    private QueryGraph queryGraph;
    private Map<String, TrafficSignSnap> trafficSignSnaps;
    private Map<Integer, Collection<RoadSection>> baseAccessibilityByMunicipalityId = new HashMap<>();
    private Collection<RoadSection> allBaseAccessibility;

    public void create(TrafficSigns trafficSigns) {
        List<TrafficSignSnap> trafficSignSnapList = trafficSignSnapMapper.map(trafficSigns);
        Map<String, TrafficSignSnap> newTrafficSignSnaps = trafficSignSnapList.stream()
                .collect(Collectors.toMap(t -> t.getTrafficSign().externalId(),
                        Function.identity()));
        QueryGraph newQueryGraph = queryGraphFactory.createQueryGraph(trafficSignSnapList);
        dataLock.lock();
        try {
            allBaseAccessibility = null;
            baseAccessibilityByMunicipalityId = new HashMap<>();
            trafficSignSnaps = newTrafficSignSnaps;
            queryGraph = newQueryGraph;
        } finally {
            dataLock.unlock();
        }
    }

    public NetworkData getNetworkData(Integer municipalityId, Snap snap, double searchRadiusInMeters,
            List<TrafficSign> trafficSigns) {
        if (queryGraph == null || trafficSignSnaps == null) {
            throw new IllegalStateException("NetworkData is not initialised. Call create() before calling getNetworkData().");
        }
        dataLock.lock();
        try {
            List<TrafficSignSnap> snappedTrafficSigns = getTrafficSignSnaps(trafficSigns.stream().map(TrafficSign::externalId).toList());
            EdgeRestrictions edgeRestrictions = queryGraphConfigurer.createEdgeRestrictions(getQueryGraph(), snappedTrafficSigns);
            return NetworkData.builder()
                    .edgeRestrictions(edgeRestrictions)
                    .queryGraph(getQueryGraph())
                    .baseAccessibleRoads(
                            getBaseAccessibility(municipalityId, snap, searchRadiusInMeters, edgeRestrictions.getTrafficSignsByEdgeKey()))
                    .build();
        } finally {
            dataLock.unlock();
        }
    }

    private List<TrafficSignSnap> getTrafficSignSnaps(List<String> trafficSignsIds) {
        return trafficSignsIds.stream()
                .filter(trafficSignSnaps::containsKey)
                .map(trafficSignSnaps::get)
                .toList();

    }

    private QueryGraph getQueryGraph() {

        return queryGraph;
    }

    /**
     * Retrieves base accessibility road sections, optionally restricted to a specific municipality. Adds traffic sign information to the
     * road sections based on the provided mappings.
     *
     * @param municipalityId        the ID of the municipality for which to calculate accessibility; if null, calculates for all
     *                              municipalities
     * @param snap                  the snapping point used for geographical calculations
     * @param searchRadiusInMeters  the search radius, in meters, used for accessibility calculations
     * @param trafficSignsByEdgeKey a map linking edge keys to their associated traffic signs
     * @return a collection of road sections representing the base accessibility, with traffic sign information included
     */
    private Collection<RoadSection> getBaseAccessibility(Integer municipalityId, Snap snap, double searchRadiusInMeters,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {
        dataLock.lock();
        try {
            if (municipalityId == null) {
                if (allBaseAccessibility == null) {
                    allBaseAccessibility = calculateBaseAccessibility(null, snap, searchRadiusInMeters);
                }
                return allBaseAccessibility.stream()
                        .map(RoadSection::copy)
                        .map(r -> roadSectionTrafficSignAssigner.assignTrafficSigns(r, trafficSignsByEdgeKey))
                        .collect(toCollection(ArrayList::new));
            } else {

                return baseAccessibilityByMunicipalityId.computeIfAbsent(municipalityId,
                                id -> calculateBaseAccessibility(municipalityId, snap, searchRadiusInMeters)).stream()
                        .map(RoadSection::copy)
                        .map(r -> roadSectionTrafficSignAssigner.assignTrafficSigns(r, trafficSignsByEdgeKey))
                        .collect(toCollection(ArrayList::new));

            }
        } finally {
            dataLock.unlock();
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
                        snappedPoint));
    }


}

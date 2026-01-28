package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static java.util.stream.Collectors.toCollection;

import com.graphhopper.util.PMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.RoadSectionTrafficSignAssigner;
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
@Slf4j
public class NetworkCacheDataService {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final RoadSectionMapper roadSectionMapper;

    private final RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner;

    public NetworkCacheDataService(
            IsochroneServiceFactory isochroneServiceFactory,
            RoadSectionMapper roadSectionMapper,
            RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner) {

        this.isochroneServiceFactory = isochroneServiceFactory;
        this.roadSectionMapper = roadSectionMapper;
        this.roadSectionTrafficSignAssigner = roadSectionTrafficSignAssigner;
    }

    public Collection<RoadSection> getBaseAccessibility(
            GraphHopperNetwork graphHopperNetwork,
            Integer municipalityId,
            double searchRadiusInMeters) {

        return calculateBaseAccessibility(graphHopperNetwork, searchRadiusInMeters, municipalityId).stream()
                .map(RoadSection::copy)
                .map(roadSection -> roadSectionTrafficSignAssigner.assignRestriction(
                        roadSection,
                        graphHopperNetwork.getRestrictionsByEdgeKey()))
                .collect(toCollection(ArrayList::new));
    }

    private Collection<RoadSection> calculateBaseAccessibility(
            GraphHopperNetwork graphHopperNetwork,
            double searchRadiusInMeters,
            Integer municipalityId) {

        log.debug("Calculating base accessibility for municipality id: '{}'", municipalityId);
        IsochroneService isochroneService = isochroneServiceFactory.createService(graphHopperNetwork);
        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(new RestrictionWeightingAdapter(
                                        graphHopperNetwork.getNetwork().createWeighting(NetworkConstants.CAR_PROFILE, new PMap()),
                                        Set.of()))
                                .municipalityId(municipalityId)
                                .searchDistanceInMetres(searchRadiusInMeters)
                                .build(),
                        graphHopperNetwork.getQueryGraph(),
                        graphHopperNetwork.getFrom()));
    }
}

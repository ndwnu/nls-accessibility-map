package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccessibilityCalculator {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final RoadSectionMapper roadSectionMapper;

    public AccessibilityCalculator(
            IsochroneServiceFactory isochroneServiceFactory,
            RoadSectionMapper roadSectionMapper) {

        this.isochroneServiceFactory = isochroneServiceFactory;
        this.roadSectionMapper = roadSectionMapper;
    }

    @Timed(value = "accessibilitymap.accessibility.calculateWithRestrictions")
    public Collection<RoadSection> calculateWithRestrictions(
            AccessibilityRequest accessibilityRequest,
            AccessibilityNetwork accessibilityNetwork) {

        return calculateAccessibility(accessibilityNetwork, accessibilityRequest, true);
    }

    @Timed(value = "accessibilitymap.accessibility.calculateWithoutRestrictions")
    public Collection<RoadSection> calculateWithoutRestrictions(
            AccessibilityRequest accessibilityRequest,
            AccessibilityNetwork accessibilityNetwork) {

        return calculateAccessibility(accessibilityNetwork, accessibilityRequest, false);
    }

    private Collection<RoadSection> calculateAccessibility(
            AccessibilityNetwork accessibilityNetwork,
            AccessibilityRequest accessibilityRequest,
            boolean applyRestrictions) {

        RestrictionWeightingAdapter weighting = createWeighting(
                accessibilityNetwork,
                applyRestrictions ? accessibilityNetwork.getBlockedEdges() : Set.of());
        IsochroneService isochroneService = isochroneServiceFactory.createService(accessibilityNetwork);

        List<IsochroneMatch> isochroneMatches = isochroneService.getIsochroneMatchesByMunicipalityId(
                IsochroneArguments.builder()
                        .weighting(weighting)
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(accessibilityRequest.maxSearchDistanceInMeters())
                        .build(),
                accessibilityNetwork.getQueryGraph(),
                accessibilityNetwork.getFrom());

        return roadSectionMapper.mapToRoadSections(
                isochroneMatches,
                accessibilityNetwork.getRestrictionsByEdgeKey()
        );
    }

    private static @NonNull RestrictionWeightingAdapter createWeighting(
            AccessibilityNetwork accessibilityNetwork,
            Set<Integer> blockedEdges) {

        NetworkGraphHopper networkGraphHopper = accessibilityNetwork.getNetworkData().getGraphHopperNetwork().network();

        return new RestrictionWeightingAdapter(
                networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()),
                blockedEdges);
    }
}

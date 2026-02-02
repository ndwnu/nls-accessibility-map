package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static java.util.stream.Collectors.toCollection;

import com.graphhopper.util.PMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.RoadSectionTrafficSignAssigner;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BaseAccessibilityCalculator {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final RoadSectionMapper roadSectionMapper;

    private final RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner;

    public BaseAccessibilityCalculator(
            IsochroneServiceFactory isochroneServiceFactory,
            RoadSectionMapper roadSectionMapper,
            RoadSectionTrafficSignAssigner roadSectionTrafficSignAssigner) {

        this.isochroneServiceFactory = isochroneServiceFactory;
        this.roadSectionMapper = roadSectionMapper;
        this.roadSectionTrafficSignAssigner = roadSectionTrafficSignAssigner;
    }

    public Collection<RoadSection> calculate(
            AccessibilityNetwork accessibilityNetwork,
            Integer municipalityId,
            double searchRadiusInMeters) {

        return calculateBaseAccessibility(accessibilityNetwork, searchRadiusInMeters, municipalityId).stream()
                .map(RoadSection::copy)
                .map(roadSection -> roadSectionTrafficSignAssigner.assignRestriction(
                        roadSection,
                        accessibilityNetwork.getRestrictionsByEdgeKey()))
                .collect(toCollection(ArrayList::new));
    }

    private Collection<RoadSection> calculateBaseAccessibility(
            AccessibilityNetwork accessibilityNetwork,
            double searchRadiusInMeters,
            Integer municipalityId) {

        log.debug("Calculating base accessibility for municipality id: '{}'", municipalityId);
        IsochroneService isochroneService = isochroneServiceFactory.createService(accessibilityNetwork);
        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(new RestrictionWeightingAdapter(
                                        accessibilityNetwork.getAccessibilityContext().graphHopperNetwork().network()
                                                .createWeighting(NetworkConstants.CAR_PROFILE, new PMap()), Set.of()))
                                .municipalityId(municipalityId)
                                .searchDistanceInMetres(searchRadiusInMeters)
                                .build(),
                        accessibilityNetwork.getQueryGraph(),
                        accessibilityNetwork.getFrom()));
    }
}

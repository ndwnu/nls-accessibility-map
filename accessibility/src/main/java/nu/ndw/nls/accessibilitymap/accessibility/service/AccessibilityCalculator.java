package nu.ndw.nls.accessibilitymap.accessibility.service;

import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccessibilityCalculator {

    private final IsochroneService isochroneService;

    private final RoadSectionMapper roadSectionMapper;

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

        log.debug("Calculating accessibility {} restrictions for {}", applyRestrictions ? "with" : "without", accessibilityRequest);

        RestrictionWeightingAdapter weighting = createWeighting(
                accessibilityNetwork,
                applyRestrictions ? accessibilityNetwork.getBlockedEdges() : Set.of());

        List<IsoLabel> isoLabels = isochroneService.search(
                accessibilityNetwork,
                IsochroneArguments.builder()
                        .weighting(weighting)
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(accessibilityRequest.maxSearchDistanceInMeters())
                        .build());
        log.debug("Found {} isochrone labels", isoLabels.size());

        Collection<RoadSection> roadSections = roadSectionMapper.map(
                accessibilityNetwork,
                isoLabels,
                accessibilityNetwork.getRestrictionsByEdgeKey()
        );

        log.debug("Calculated accessibility {} restrictions, found {} road sections for {}",
                applyRestrictions ? "with" : "without",
                roadSections.size(),
                accessibilityRequest);

        return roadSections;
    }

    private static @NonNull RestrictionWeightingAdapter createWeighting(
            AccessibilityNetwork accessibilityNetwork,
            Set<Integer> blockedEdges) {

        return new RestrictionWeightingAdapter(
                accessibilityNetwork.getWeighting(),
                blockedEdges);
    }
}

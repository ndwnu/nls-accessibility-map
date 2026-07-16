package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionsIsochroneLabel;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.limit.ExploreLimitCarAccessible;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.limit.ExploreLimitRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.dto.IsochroneLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimitComposite;
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
            AccessibilityNetwork accessibilityNetwork
    ) {

        return calculateAccessibility(accessibilityNetwork, accessibilityRequest, true);
    }

    @Timed(value = "accessibilitymap.accessibility.calculateWithoutRestrictions")
    public Collection<RoadSection> calculateWithoutRestrictions(
            AccessibilityRequest accessibilityRequest,
            AccessibilityNetwork accessibilityNetwork
    ) {

        return calculateAccessibility(accessibilityNetwork, accessibilityRequest, false);
    }

    private Collection<RoadSection> calculateAccessibility(
            AccessibilityNetwork accessibilityNetwork,
            AccessibilityRequest accessibilityRequest,
            boolean applyRestrictions
    ) {

        log.debug("Calculating accessibility {} restrictions for {}", applyRestrictions ? "with" : "without", accessibilityRequest);

        List<IsochroneLabel> isochroneLabels = isochroneService.search(
                accessibilityNetwork,
                IsochroneArguments.builder()
                        .exploreLimit(getExploreLimits(accessibilityNetwork, applyRestrictions))
                        .weighting(accessibilityNetwork.getWeighting())
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(accessibilityRequest.maxSearchDistanceInMeters())
                        .reverseFlow(false)
                        .build());
        log.debug("Found {} isochrone labels", isochroneLabels.size());

        Collection<RoadSection> roadSections = roadSectionMapper.map(
                accessibilityNetwork,
                isochroneLabels,
                accessibilityNetwork.getRestrictionsByEdgeKey()
        );

        log.debug(
                "Calculated accessibility {} restrictions, found {} road sections for {}",
                applyRestrictions ? "with" : "without",
                roadSections.size(),
                accessibilityRequest);

        return roadSections;
    }

    private static ExploreLimit<RestrictionsIsochroneLabel> getExploreLimits(
            AccessibilityNetwork accessibilityNetwork,
            boolean applyRestrictions) {

        if (applyRestrictions) {
            return new ExploreLimitComposite<>(
                    new ExploreLimitCarAccessible(
                            accessibilityNetwork.getQueryGraph(),
                            accessibilityNetwork.getNetworkData().getNwbNetworkData(),
                            new EdgeIteratorStateReverseExtractor()),
                    new ExploreLimitRestriction());
        } else {
            return new ExploreLimitCarAccessible(
                    accessibilityNetwork.getQueryGraph(),
                    accessibilityNetwork.getNetworkData().getNwbNetworkData(),
                    new EdgeIteratorStateReverseExtractor());
        }
    }
}

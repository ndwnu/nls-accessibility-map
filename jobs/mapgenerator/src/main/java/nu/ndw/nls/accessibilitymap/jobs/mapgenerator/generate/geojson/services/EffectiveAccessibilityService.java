package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EffectiveAccessibleDirectionalRoadSectionFragmentMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EffectiveAccessibleDirectionalRoadSectionsMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSections;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.services.RoadSectionFragmentService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EffectiveAccessibilityService {

    private final EffectiveAccessibleDirectionalRoadSectionFragmentMapper
            effectiveAccessibleDirectionalRoadSectionFragmentMapper;

    private final RoadSectionFragmentService roadSectionFragmentService;

    private final EffectiveAccessibleDirectionalRoadSectionsMapper effectiveAccessibleDirectionalRoadSectionsMapper;

    private final RoadSectionFragmentSplitService roadSectionFragmentSplitService;

    public List<DirectionalRoadSectionAndTrafficSign> determineEffectiveAccessibility(
            DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedById) {

        var backwardRoadSectionFragments = effectiveAccessibleDirectionalRoadSectionFragmentMapper.map(
                directionalRoadSectionAndTrafficSignGroupedById.getBackward());
        var forwardRoadSectionFragments = effectiveAccessibleDirectionalRoadSectionFragmentMapper.map(
                directionalRoadSectionAndTrafficSignGroupedById.getForward());

        // From the accessible describing fragments of forward and backward direction, split overlapping areas into
        // smaller fragments to cover each unique area and determine accessibility by whether either direction is
        // accessible
        List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections>> roadSectionFragments =
                roadSectionFragmentService.splitAndCombineData(backwardRoadSectionFragments,
                        forwardRoadSectionFragments, effectiveAccessibleDirectionalRoadSectionsMapper);

        // Merge sequential areas that have the same effective accessible state to reduce unnecessary fragments.
        List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections>> roadSectionFragmentsAccessibility =
                roadSectionFragmentService.combineSequentials(roadSectionFragments, Objects::equals,
                        (previousAccessibleSameAsNext, nextAccessibleSameAsPrevious) -> previousAccessibleSameAsNext);

        // Actually splitting the road sections into parts and returning the DirectionalRoadSectionAndTrafficSign parts
        return roadSectionFragmentsAccessibility.stream()
                .map(roadSectionFragmentSplitService::split)
                .flatMap(Collection::stream).toList();
    }



}

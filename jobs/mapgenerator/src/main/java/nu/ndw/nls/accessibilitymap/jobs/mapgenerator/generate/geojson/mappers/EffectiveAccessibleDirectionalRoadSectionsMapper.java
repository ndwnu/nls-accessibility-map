package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.Objects;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSections;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EffectiveAccessibleDirectionalRoadSectionsMapper implements
        BiFunction<EffectiveAccessibleDirectionalRoadSection, EffectiveAccessibleDirectionalRoadSection,
                EffectiveAccessibleDirectionalRoadSections> {

    private final AccessibilityMapper accessibilityMapper;

    @Override
    public EffectiveAccessibleDirectionalRoadSections apply(
            EffectiveAccessibleDirectionalRoadSection backward,
            EffectiveAccessibleDirectionalRoadSection forward) {
        return EffectiveAccessibleDirectionalRoadSections.builder()
                .accessibility(this.determineEffectiveAccessibility(backward, forward))
                .backward(getRoadSectionOrNullIfDirectionDoesNotExist(backward))
                .forward(getRoadSectionOrNullIfDirectionDoesNotExist(forward))
                .build();
    }

    private DirectionalRoadSectionAndTrafficSign getRoadSectionOrNullIfDirectionDoesNotExist(
            EffectiveAccessibleDirectionalRoadSection directionalRoadSection) {
        if (directionalRoadSection == null) {
            return null;
        }

        return directionalRoadSection.getRoadSection();
    }

    private boolean determineEffectiveAccessibility(EffectiveAccessibleDirectionalRoadSection backward,
            EffectiveAccessibleDirectionalRoadSection forward) {
        return isAccessible(backward) || isAccessible(forward);
    }

    private boolean isAccessible(EffectiveAccessibleDirectionalRoadSection direction) {
        return hasDirection(direction) && hasAccessibility(direction.getAccessibility());
    }

    private boolean hasAccessibility(Boolean accessibility) {
        return accessibilityMapper.map(accessibility);
    }

    private boolean hasDirection(EffectiveAccessibleDirectionalRoadSection direction) {
        return Objects.nonNull(direction);
    }
}

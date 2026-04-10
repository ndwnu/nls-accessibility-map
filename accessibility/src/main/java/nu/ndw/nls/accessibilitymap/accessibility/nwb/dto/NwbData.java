package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Validated
@ToString(of = {"nwbVersionId"})
public final class NwbData {

    @NotNull
    @Getter
    private final Integer nwbVersionId;

    @NotNull
    @JsonInclude(Include.ALWAYS)
    @Getter
    private final List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections;

    @NotNull
    @JsonIgnore
    private volatile Map<Long, AccessibilityNwbRoadSection> accessibilityNwbRoadSectionsById;

    @JsonCreator
    public NwbData(
            @JsonProperty("nwbVersionId") Integer nwbVersionId,
            @NonNull @JsonProperty("accessibilityNwbRoadSections") List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections
    ) {
        this.nwbVersionId = nwbVersionId;
        this.accessibilityNwbRoadSections = accessibilityNwbRoadSections;
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSections() {
        return accessibilityNwbRoadSections;
    }

    public Optional<AccessibilityNwbRoadSection> findAccessibilityNwbRoadSectionById(long roadSectionId) {

        return Optional.ofNullable(getMap().get(roadSectionId));
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSectionByMunicipalityId(int municipalityId) {
        return accessibilityNwbRoadSections.stream()
                .filter(accessibilityNwbRoadSection -> accessibilityNwbRoadSection.municipalityId().equals(municipalityId))
                .toList();
    }

    private Map<Long, AccessibilityNwbRoadSection> getMap() {
        Map<Long, AccessibilityNwbRoadSection> local = accessibilityNwbRoadSectionsById;

        if (local == null) {
            synchronized (this) {
                local = accessibilityNwbRoadSectionsById;
                if (local == null) {
                    List<AccessibilityNwbRoadSection> list = accessibilityNwbRoadSections;
                    Map<Long, AccessibilityNwbRoadSection> map =
                            new HashMap<>((int) (list.size() / 0.75f) + 1);
                    for (AccessibilityNwbRoadSection section : list) {
                        long key = section.roadSectionId();

                        // same merge logic as (a, b) -> a
                        map.putIfAbsent(key, section);
                    }
                    accessibilityNwbRoadSectionsById = map;
                    local = map;
                }
            }
        }
        return local;
    }
}

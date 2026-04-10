package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final SortedMap<Long, AccessibilityNwbRoadSection> accessibilityNwbRoadSectionsById;

    @JsonCreator
    public NwbData(
            @JsonProperty("nwbVersionId") Integer nwbVersionId,
            @NonNull @JsonProperty("accessibilityNwbRoadSections")  List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections) {
        this.nwbVersionId = nwbVersionId;
        this.accessibilityNwbRoadSections = accessibilityNwbRoadSections;

        accessibilityNwbRoadSectionsById = accessibilityNwbRoadSections.stream()
                .collect(Collectors.toMap(
                        AccessibilityNwbRoadSection::roadSectionId,               // key mapper (id)
                        Function.identity(),           // value mapper (the object)
                        (a, b) -> a,                   // merge function if duplicate ids occur (pick first; adjust if needed)
                        TreeMap::new
                ));
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSections() {
        return accessibilityNwbRoadSections;
    }

    public Optional<AccessibilityNwbRoadSection> findAccessibilityNwbRoadSectionById(long roadSectionId) {

        return Optional.ofNullable(accessibilityNwbRoadSectionsById.get(roadSectionId));
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSectionByMunicipalityId(int municipalityId) {
        return accessibilityNwbRoadSections.stream()
                .filter(accessibilityNwbRoadSection -> accessibilityNwbRoadSection.municipalityId().equals(municipalityId))
                .toList();
    }
}

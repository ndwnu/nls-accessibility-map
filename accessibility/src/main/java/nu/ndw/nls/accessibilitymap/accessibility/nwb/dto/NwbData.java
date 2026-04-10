package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
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
    private Integer nwbVersionId;

    @NotNull
    @JsonInclude(Include.ALWAYS)
    @Getter
    private List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections;

    @JsonIgnore
    private transient SortedMap<Long, AccessibilityNwbRoadSection> accessibilityNwbRoadSectionsById;

    /**
     * Kryo-compatible no-arg constructor
     */
    protected NwbData() {
        // for Kryo
    }

    @JsonCreator
    public NwbData(
            @JsonProperty("nwbVersionId") Integer nwbVersionId,
            @NonNull @JsonProperty("accessibilityNwbRoadSections")
            List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections
    ) {

        this.nwbVersionId = nwbVersionId;
        this.accessibilityNwbRoadSections = new ArrayList<>(accessibilityNwbRoadSections);
        rebuildIndex();
    }

    private void rebuildIndex() {
        this.accessibilityNwbRoadSectionsById =
                accessibilityNwbRoadSections.stream()
                        .collect(Collectors.toMap(
                                AccessibilityNwbRoadSection::roadSectionId,
                                Function.identity(),
                                (a, b) -> a,
                                TreeMap::new
                        ));
    }

    /**
     * Lazy init to support Kryo deserialization
     */
    private SortedMap<Long, AccessibilityNwbRoadSection> getIndex() {
        if (accessibilityNwbRoadSectionsById == null) {
            rebuildIndex();
        }
        return accessibilityNwbRoadSectionsById;
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSections() {
        return accessibilityNwbRoadSections;
    }

    public Optional<AccessibilityNwbRoadSection> findAccessibilityNwbRoadSectionById(long roadSectionId) {
        return Optional.ofNullable(getIndex().get(roadSectionId));
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSectionByMunicipalityId(int municipalityId) {
        return accessibilityNwbRoadSections.stream()
                .filter(s -> s.municipalityId().equals(municipalityId))
                .toList();
    }
}

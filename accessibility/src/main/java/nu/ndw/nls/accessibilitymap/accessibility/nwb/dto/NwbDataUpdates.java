package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public final class NwbDataUpdates {

    @NotNull
    @Getter
    private final Integer nwbVersionId;

    @NotNull
    @JsonInclude(Include.ALWAYS)
    @Getter
    private final List<AccessibilityNwbRoadSectionUpdate> accessibilityNwbRoadSectionUpdates;

    @NotNull
    @JsonIgnore
    private final SortedMap<Long, AccessibilityNwbRoadSectionUpdate> changedNwbRoadSectionsById;

    @JsonCreator
    public NwbDataUpdates(@NonNull @JsonProperty("nwbVersionId") Integer nwbVersionId,
            @NonNull @JsonProperty("changedNwbRoadSections") List<AccessibilityNwbRoadSectionUpdate> accessibilityNwbRoadSectionUpdates
    ) {
        this.nwbVersionId = nwbVersionId;

        this.changedNwbRoadSectionsById = accessibilityNwbRoadSectionUpdates.stream()
                .collect(Collectors.toMap(
                        AccessibilityNwbRoadSectionUpdate::roadSectionId,               // key mapper (id)
                        Function.identity(),           // value mapper (the object)
                        AccessibilityNwbRoadSectionUpdate::update,//merge function if duplicate ids occur update previous with new values
                        TreeMap::new
                ));

        this.accessibilityNwbRoadSectionUpdates = changedNwbRoadSectionsById.values().stream().toList();
    }

    public Optional<AccessibilityNwbRoadSectionUpdate> findChangedNwbRoadSectionById(long roadSectionId) {
        return Optional.ofNullable(changedNwbRoadSectionsById.get(roadSectionId));
    }

    public NwbDataUpdates merge(NwbDataUpdates other) {
        if (!isSameNwbVersion(other)) {
            throw new IllegalArgumentException("Cannot merge updates from different NWB versions");
        }

        // update existing roadSections with new changes
        List<AccessibilityNwbRoadSectionUpdate> updatedRoadSections = new ArrayList<>(this.accessibilityNwbRoadSectionUpdates.stream()
                .map(changedNwbRoadSection -> other.findChangedNwbRoadSectionById(changedNwbRoadSection.roadSectionId())
                        .map(changedNwbRoadSection::update)
                        .orElse(changedNwbRoadSection))
                .toList());
        // add only new changed roadSections
        List<AccessibilityNwbRoadSectionUpdate> newEntries = other.accessibilityNwbRoadSectionUpdates.stream()
                .filter(r -> this.findChangedNwbRoadSectionById(r.roadSectionId()).isEmpty())
                .toList();

        updatedRoadSections.addAll(newEntries);
        return new NwbDataUpdates(this.nwbVersionId, new ArrayList<>(updatedRoadSections));
    }

    private boolean isSameNwbVersion(NwbDataUpdates other) {
        return Objects.equals(other.nwbVersionId, this.nwbVersionId);
    }
}

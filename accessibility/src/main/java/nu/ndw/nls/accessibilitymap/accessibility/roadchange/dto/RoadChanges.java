package nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto;

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
public final class RoadChanges {

    @NotNull
    @Getter
    private final Integer nwbVersionId;

    @NotNull
    @JsonInclude(Include.ALWAYS)
    @Getter
    private final List<ChangedNwbRoadSection> changedNwbRoadSections;

    @NotNull
    @JsonIgnore
    private final SortedMap<Long, ChangedNwbRoadSection> changedNwbRoadSectionsById;

    @JsonCreator
    public RoadChanges(@NonNull @JsonProperty("nwbVersionId") Integer nwbVersionId,
            @NonNull @JsonProperty("changedNwbRoadSections") List<ChangedNwbRoadSection> changedNwbRoadSections
    ) {
        this.nwbVersionId = nwbVersionId;

        this.changedNwbRoadSectionsById = changedNwbRoadSections.stream()
                .collect(Collectors.toMap(
                        ChangedNwbRoadSection::roadSectionId,               // key mapper (id)
                        Function.identity(),           // value mapper (the object)
                        ChangedNwbRoadSection::update,//merge function if duplicate ids occur update previous with new values
                        TreeMap::new
                ));

        this.changedNwbRoadSections = changedNwbRoadSectionsById.values().stream().toList();
    }

    public Optional<ChangedNwbRoadSection> findChangedNwbRoadSectionById(long roadSectionId) {
        return Optional.ofNullable(changedNwbRoadSectionsById.get(roadSectionId));
    }

    public RoadChanges merge(RoadChanges other) {
        if (!isSameVersion(other)) {
            throw new IllegalArgumentException("Cannot merge road changes with different nwbVersionId (%s vs %s)".formatted(this.nwbVersionId,
                    other.nwbVersionId));
        }

        // update existing roadSections with new changes
        List<ChangedNwbRoadSection> mergedChangedNwbRoadSections = new ArrayList<>(this.changedNwbRoadSections.stream()
                .map(changedNwbRoadSection -> other.findChangedNwbRoadSectionById(changedNwbRoadSection.roadSectionId())
                        .map(changedNwbRoadSection::update)
                        .orElse(changedNwbRoadSection))
                .toList());
        // add only new changed roadSections
        List<ChangedNwbRoadSection> newEntries = other.changedNwbRoadSections.stream()
                .filter(r -> this.findChangedNwbRoadSectionById(r.roadSectionId()).isEmpty())
                .toList();

        mergedChangedNwbRoadSections.addAll(newEntries);
        return new RoadChanges(this.nwbVersionId, new ArrayList<>(mergedChangedNwbRoadSections));
    }

    public boolean isSameVersion(RoadChanges other) {
        return Objects.equals(other.nwbVersionId, this.nwbVersionId);
    }
}

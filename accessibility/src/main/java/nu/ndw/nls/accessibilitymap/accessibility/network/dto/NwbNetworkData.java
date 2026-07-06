package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;

@Getter
public class NwbNetworkData {

    @NotNull
    @Getter
    private final Integer nwbVersionId;

    @NotNull
    @JsonInclude(Include.ALWAYS)
    @Getter
    private final List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections;

    @NotNull
    @JsonIgnore
    private final Map<Long, AccessibilityNwbRoadSection> accessibilityNwbRoadSectionsById;

    @NotNull
    @Valid
    private final NwbData nwbData;

    @NotNull
    @Valid
    private final NwbDataUpdates nwbDataUpdates;

    public NwbNetworkData(NwbData nwbData, NwbDataUpdates nwbDataUpdates) {
        this.nwbData = nwbData;
        this.nwbDataUpdates = nwbDataUpdates;

        nwbVersionId = nwbData.getNwbVersionId();

        accessibilityNwbRoadSections = nwbData.getAccessibilityNwbRoadSections().stream()
                .map(AccessibilityNwbRoadSection::new)
                .map(accessibilityNwbRoadSection ->
                        nwbDataUpdates.findChangedNwbRoadSectionById(accessibilityNwbRoadSection.roadSectionId())
                                .map(update -> accessibilityNwbRoadSection
                                        .withBackwardAccessible(update.backwardAccessible())
                                        .withForwardAccessible(update.forwardAccessible())
                                        .withCarriagewayTypeCode(update.carriagewayTypeCode()))
                                .orElse(accessibilityNwbRoadSection))
                .toList();

        accessibilityNwbRoadSectionsById = accessibilityNwbRoadSections.stream()
                .collect(Collectors.toMap(AccessibilityNwbRoadSection::roadSectionId, Function.identity()));
    }

    public Optional<AccessibilityNwbRoadSection> findAccessibilityNwbRoadSectionById(long roadSectionId) {
        return Optional.ofNullable(accessibilityNwbRoadSectionsById.get(roadSectionId));
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSections() {
        return accessibilityNwbRoadSections;
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSectionByMunicipalityId(int municipalityId) {
        return accessibilityNwbRoadSections.stream()
                .filter(accessibilityNwbRoadSection -> accessibilityNwbRoadSection.municipalityId().equals(municipalityId))
                .toList();
    }
}

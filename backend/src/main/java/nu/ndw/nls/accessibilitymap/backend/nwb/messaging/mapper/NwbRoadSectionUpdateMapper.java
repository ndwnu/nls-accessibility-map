package nu.ndw.nls.accessibilitymap.backend.nwb.messaging.mapper;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto.NwbRoadSectionUpdate;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NwbRoadSectionUpdateMapper {

    private final NetworkDataService networkDataService;

    public AccessibilityNwbRoadSectionUpdate map(NwbRoadSectionUpdate nwbRoadSectionUpdate) {
        NwbData nwbData = networkDataService.get().getNwbData();
        return nwbData
                .findAccessibilityNwbRoadSectionById(nwbRoadSectionUpdate.roadSectionId())
                .map(
                        accessibilityNwbRoadSection -> mergeWithExistingRoadSection(nwbRoadSectionUpdate, accessibilityNwbRoadSection)

                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Road update road-section: %d is not present is active version".formatted(nwbRoadSectionUpdate.roadSectionId())));
    }

    private static AccessibilityNwbRoadSectionUpdate mergeWithExistingRoadSection(NwbRoadSectionUpdate nwbRoadSectionUpdate,
            AccessibilityNwbRoadSection accessibilityNwbRoadSection
    ) {
        CarriagewayTypeCode carriagewayTypeCode =
                Objects.nonNull(nwbRoadSectionUpdate.carriagewayTypeCode()) ? nwbRoadSectionUpdate.carriagewayTypeCode()
                        : accessibilityNwbRoadSection.carriagewayTypeCode();
        boolean forwardAccessible =
                Objects.nonNull(nwbRoadSectionUpdate.drivingDirection()) ? nwbRoadSectionUpdate.drivingDirection().isForwardAccessible()
                        : accessibilityNwbRoadSection.forwardAccessible();
        boolean backwardAccessible =
                Objects.nonNull(nwbRoadSectionUpdate.drivingDirection()) ? nwbRoadSectionUpdate.drivingDirection().isBackwardAccessible()
                        : accessibilityNwbRoadSection.backwardAccessible();
        return new AccessibilityNwbRoadSectionUpdate(
                nwbRoadSectionUpdate.roadSectionId(),
                forwardAccessible,
                backwardAccessible,
                carriagewayTypeCode);
    }
}

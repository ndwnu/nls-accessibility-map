package nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.springframework.validation.annotation.Validated;

@Jacksonized
@Builder
@Validated
public record NwbRoadSectionUpdate(@NotNull Integer roadSectionId, @NotNull LocalDate nwbVersion,
                                   @Nullable DrivingDirection drivingDirection,
                                   @Nullable CarriagewayTypeCode carriagewayTypeCode) {

}

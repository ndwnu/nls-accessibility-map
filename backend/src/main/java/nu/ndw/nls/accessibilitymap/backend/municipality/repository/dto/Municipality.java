package nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record Municipality(
        @NotNull @JsonProperty("start-coordinate-latitude") Double startCoordinateLatitude,
        @NotNull @JsonProperty("start-coordinate-longitude") Double startCoordinateLongitude,
        @NotNull @JsonProperty("search-distance-in-metres") Integer searchDistanceInMetres,
        @NotNull @JsonProperty("municipality-id") String municipalityId,
        @NotNull String name,
        @NotNull @Valid MunicipalityBoundingBox bounds,
        @JsonProperty("date-last-check") LocalDate dateLastCheck) {

    @SuppressWarnings("java:S5852")
    private static final Pattern PATTERN = Pattern.compile(".{2}0*(\\d+)$");

    public int municipalityIdAsInteger() {

        Matcher m = PATTERN.matcher(municipalityId);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalStateException("Incorrect municipalityId " + municipalityId);
        }
    }
}

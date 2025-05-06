package nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record MunicipalityBoundingBox(
        @NotNull @JsonProperty("longitude-from") Double longitudeFrom,
        @NotNull @JsonProperty("latitude-from") Double latitudeFrom,
        @NotNull @JsonProperty("longitude-to") Double longitudeTo,
        @NotNull @JsonProperty("latitude-to") Double latitudeTo) {

}

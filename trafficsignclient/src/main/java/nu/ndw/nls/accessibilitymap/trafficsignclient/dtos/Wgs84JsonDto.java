package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"latitude", "longitude"})
public final class Wgs84JsonDto {

    private final String latitude;
    private final String longitude;
}

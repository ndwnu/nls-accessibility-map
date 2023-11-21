package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"type", "text"})
public final class TextSignJsonDtoV3 {

    private final String type;
    private final String text;
}

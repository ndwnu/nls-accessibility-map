package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityResponseGeoJsonJson.TypeEnum;
import org.springframework.validation.annotation.Validated;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Validated
public class AccessibilityGeoJsonResponse  {

    private TypeEnum type;

    @Valid
    private List<Object> features = new ArrayList<>();
}

package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Validated
@ToString(of = {"nwbVersionId"})
public final class NwbData {

    @NotNull
    @Getter
    private final Integer nwbVersionId;

    @NotNull
    @JsonInclude(Include.ALWAYS)
    @Getter
    @Valid
    private final List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections;

    @JsonCreator
    public NwbData(
            @JsonProperty("nwbVersionId") Integer nwbVersionId,
            @NonNull @JsonProperty("accessibilityNwbRoadSections") List<AccessibilityNwbRoadSection> accessibilityNwbRoadSections
    ) {
        this.nwbVersionId = nwbVersionId;
        this.accessibilityNwbRoadSections = accessibilityNwbRoadSections;
    }

    public List<AccessibilityNwbRoadSection> findAllAccessibilityNwbRoadSections() {
        return accessibilityNwbRoadSections;
    }
}

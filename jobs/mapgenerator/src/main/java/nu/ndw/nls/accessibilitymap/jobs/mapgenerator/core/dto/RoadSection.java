package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Builder
@With
@Validated
public final class RoadSection {

    @NotNull
    private final Long id;

    private RoadSectionMetaData metaData;

    @Default
    @NotEmpty
    @Valid
    private List<RoadSectionFragment> roadSectionFragments = new ArrayList<>();

}

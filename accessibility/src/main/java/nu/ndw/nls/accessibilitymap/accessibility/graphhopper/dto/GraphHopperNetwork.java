package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
@With
public record GraphHopperNetwork(
        @NotNull NetworkGraphHopper network,
        @NotNull Integer nwbVersion
) {

    @Override
    public @NonNull String toString() {
        return "GraphHopperNetwork(" +
               "nwbVersion=" + nwbVersion +
               ')';
    }
}

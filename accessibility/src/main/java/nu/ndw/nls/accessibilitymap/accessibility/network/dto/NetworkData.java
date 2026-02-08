package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
public final class NetworkData {

    @NotNull
    private final Integer nwbVersion;

    @NotNull
    @Valid
    private final GraphHopperNetwork graphHopperNetwork;

    @NotNull
    @Valid
    private final NwbData nwbData;

    public NetworkData(
            GraphHopperNetwork graphHopperNetwork,
            NwbData nwbData) {
        this.graphHopperNetwork = graphHopperNetwork;

        if (!graphHopperNetwork.nwbVersion().equals(nwbData.getNwbVersionId())) {
            throw new IllegalArgumentException("Graph Hopper network and road sections do not match NWB versions.");
        }

        this.nwbVersion = graphHopperNetwork.nwbVersion();
        this.nwbData = nwbData;
    }

    @Override
    public String toString() {
        return "NetworkData[" +
               "graphHopperNetwork=" + graphHopperNetwork +
               ']';
    }
}

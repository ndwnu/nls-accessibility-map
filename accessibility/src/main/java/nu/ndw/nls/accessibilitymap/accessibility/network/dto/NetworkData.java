package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ToString(of = {"nwbVersion", "networkGraphHopper"})
public final class NetworkData {

    @NotNull
    private final Integer nwbVersion;

    @NotNull
    private final NetworkGraphHopper networkGraphHopper;

    @NotNull
    @Valid
    private final NwbData nwbData;

    @NotNull
    @Valid
    private final NwbDataUpdates nwbDataUpdates;

    public NetworkData(
            @NonNull GraphHopperNetwork graphHopperNetwork,
            @NonNull NwbData nwbData,
            @NonNull NwbDataUpdates nwbDataUpdates
    ) {

        this.networkGraphHopper = graphHopperNetwork.network();
        this.nwbData = nwbData;
        this.nwbDataUpdates = nwbDataUpdates;

        if (!graphHopperNetwork.nwbVersion().equals(nwbData.getNwbVersionId())) {
            throw new IllegalArgumentException("Graph Hopper network and road sections do not match NWB versions.");
        }
        this.nwbVersion = graphHopperNetwork.nwbVersion();
    }

    public NetworkData(
            NetworkGraphHopper networkGraphHopper,
            NwbData nwbData,
            NwbDataUpdates nwbDataUpdates
    ) {
        this(GraphHopperNetwork.builder().network(networkGraphHopper)
                        .nwbVersion(nwbData.getNwbVersionId()).build(),
                nwbData,
                nwbDataUpdates);
    }
}

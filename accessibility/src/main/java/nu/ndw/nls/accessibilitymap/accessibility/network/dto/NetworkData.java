package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import com.graphhopper.routing.util.EncodingManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetworkWithVersion;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
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

    public EncodingManager getEncodingManager() {
        return networkGraphHopper.getEncodingManager();
    }

    public NetworkData(
            @NonNull GraphHopperNetworkWithVersion graphHopperNetworkWithVersion,
            @NonNull NwbData nwbData
    ) {

        this.networkGraphHopper = graphHopperNetworkWithVersion.network();
        this.nwbData = nwbData;

        if (!graphHopperNetworkWithVersion.nwbVersion().equals(nwbData.getNwbVersionId())) {
            throw new IllegalArgumentException("Graph Hopper network and road sections do not match NWB versions.");
        }
        this.nwbVersion = graphHopperNetworkWithVersion.nwbVersion();
    }
}

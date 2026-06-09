package nu.ndw.nls.accessibilitymap.accessibility.network.dto;

import com.graphhopper.util.FetchMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.LineString;
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

    /**
     * Retrieves the geometric representation of a road section from the network using its unique identifier.
     *
     * @param roadSectionId the unique identifier of the road section in the network
     * @return a LineString object representing the geometry of the specified road section
     */
    public Optional<LineString> findGeometryInNetwork(long roadSectionId) {
        if (!networkGraphHopper.getWayIdToEdgeKey().containsKey(roadSectionId)) {
            return Optional.empty();
        }
        int edgeKey = networkGraphHopper.getWayIdToEdgeKey()
                .get(roadSectionId);
        return Optional.of(networkGraphHopper
                .getBaseGraph()
                .getEdgeIteratorStateForKey(edgeKey)
                .fetchWayGeometry(FetchMode.ALL)
                .toLineString(false));
    }

    public Optional<CarriagewayTypeCode> findCarriageWayTypeCodeByRoadSectionId(long roadSectionId) {
        return nwbDataUpdates.findChangedNwbRoadSectionById(roadSectionId)
                .map(AccessibilityNwbRoadSectionUpdate::carriagewayTypeCode)
                .or(() -> nwbData.findAccessibilityNwbRoadSectionById(roadSectionId)
                        .map(AccessibilityNwbRoadSection::carriagewayTypeCode));
    }
}

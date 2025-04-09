package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network;

import java.io.Serial;
import java.io.Serializable;

public record GraphhopperMetaData(int nwbVersion) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}

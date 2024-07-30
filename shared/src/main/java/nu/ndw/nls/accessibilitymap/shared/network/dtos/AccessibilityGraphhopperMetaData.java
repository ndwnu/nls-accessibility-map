package nu.ndw.nls.accessibilitymap.shared.network.dtos;

import java.io.Serial;
import java.io.Serializable;

public record AccessibilityGraphhopperMetaData(int nwbVersion) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}

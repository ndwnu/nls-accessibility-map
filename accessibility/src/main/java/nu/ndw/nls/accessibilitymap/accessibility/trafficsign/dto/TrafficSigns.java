package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;

@NoArgsConstructor
public class TrafficSigns extends LinkedHashSet<TrafficSign> {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrafficSigns(TrafficSign... trafficSigns) {
        this(Arrays.asList(trafficSigns));
    }

    public TrafficSigns(Collection<TrafficSign> trafficSigns) {
        super(trafficSigns);
    }
}

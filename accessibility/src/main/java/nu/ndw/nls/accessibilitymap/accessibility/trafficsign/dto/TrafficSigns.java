package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class TrafficSigns extends ArrayList<TrafficSign> {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrafficSigns(TrafficSign... trafficSigns) {
        this(Arrays.asList(trafficSigns));
    }

    public TrafficSigns(@NotNull Collection<TrafficSign> trafficSigns) {
        super(trafficSigns);
    }
}

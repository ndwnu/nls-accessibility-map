package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.LinkTag;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficSignToLinkTagMapper {

    private static final Map<String, LinkTag<Boolean>> NO_ENTRY_SIGNS = Map.of(
            "C6", LinkTag.C6_CAR_ACCESS_FORBIDDEN,
            "C7", LinkTag.C7_HGV_ACCESS_FORBIDDEN,
            "C7a", LinkTag.C7A_AUTO_BUS_ACCESS_FORBIDDEN,
            "C7b", LinkTag.C7B_HGV_AND_AUTO_BUS_ACCESS_FORBIDDEN,
            "C10", LinkTag.C10_TRAILER_ACCESS_FORBIDDEN,
            "C11", LinkTag.C11_MOTOR_BIKE_ACCESS_FORBIDDEN,
            "C12", LinkTag.C12_MOTOR_VEHICLE_ACCESS_FORBIDDEN
    );

    private static final Map<String, LinkTag<Double>> MAXIMUM_SIGNS = Map.of(
            "C17", LinkTag.C17_MAX_LENGTH,
            "C18", LinkTag.C18_MAX_WIDTH,
            "C19", LinkTag.C19_MAX_HEIGHT,
            "C20", LinkTag.C20_MAX_AXLE_LOAD,
            "C21", LinkTag.C21_MAX_WEIGHT
    );

    public void setLinkTags(Link link, List<TrafficSignJsonDtoV3> trafficSigns) {
        for (TrafficSignJsonDtoV3 trafficSign : trafficSigns) {
            String rvvCode = trafficSign.getRvvCode();
            if (NO_ENTRY_SIGNS.containsKey(rvvCode)) {
                setNoEntryTagValue(link, NO_ENTRY_SIGNS.get(rvvCode));
            } else if (MAXIMUM_SIGNS.containsKey(rvvCode)) {
                setMaximumTagValue(link, MAXIMUM_SIGNS.get(rvvCode), trafficSign);
            }
        }
    }

    private void setNoEntryTagValue(Link link, LinkTag<Boolean> linkTag) {
        link.setTag(linkTag, true, false);
        link.setTag(linkTag, true, true);
    }

    private void setMaximumTagValue(Link link, LinkTag<Double> linkTag, TrafficSignJsonDtoV3 trafficSign) {
        try {
            if (trafficSign.getBlackCode() != null) {
                double value = Double.parseDouble(trafficSign.getBlackCode().replace(",", "."));
                link.setTag(linkTag, value, false);
                link.setTag(linkTag, value, true);
            }
        } catch (NumberFormatException ignored) {
            log.debug("Unprocessable value {} for traffic sign with RVV code {} on road section {}",
                    trafficSign.getBlackCode(), trafficSign.getRvvCode(),
                    trafficSign.getLocation().getRoad().getRoadSectionId());
        }
    }
}

package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TextSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.LinkTag;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficSignToLinkTagMapper {

    private static final Map<String, BiConsumer<Link, TrafficSignJsonDtoV3>> SIGN_MAPPINGS = new HashMap<>();

    static {
        // No entry signs
        SIGN_MAPPINGS.put("C6", (l, t) -> setNoEntryTagValue(l, LinkTag.C6_CAR_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C7", (l, t) -> setNoEntryTagValue(l, LinkTag.C7_HGV_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C7a", (l, t) -> setNoEntryTagValue(l, LinkTag.C7A_BUS_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C7b", (l, t) -> setNoEntryTagValue(l, LinkTag.C7B_HGV_AND_BUS_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C8", (l, t) -> setNoEntryTagValue(l, LinkTag.C8_TRACTOR_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C9", (l, t) -> setNoEntryTagValue(l, LinkTag.C9_SLOW_VEHICLE_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C10", (l, t) -> setNoEntryTagValue(l, LinkTag.C10_TRAILER_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C11", (l, t) -> setNoEntryTagValue(l, LinkTag.C11_MOTORCYCLE_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C12", (l, t) -> setNoEntryTagValue(l, LinkTag.C12_MOTOR_VEHICLE_ACCESS_FORBIDDEN, t));
        SIGN_MAPPINGS.put("C22c", (l, t) -> setNoEntryTagValue(l, LinkTag.C22C_LCV_AND_HGV_ACCESS_FORBIDDEN, t));

        // Maximum signs
        SIGN_MAPPINGS.put("C17", (l, t) -> setMaximumTagValue(l, LinkTag.C17_MAX_LENGTH, t));
        SIGN_MAPPINGS.put("C18", (l, t) -> setMaximumTagValue(l, LinkTag.C18_MAX_WIDTH, t));
        SIGN_MAPPINGS.put("C19", (l, t) -> setMaximumTagValue(l, LinkTag.C19_MAX_HEIGHT, t));
        SIGN_MAPPINGS.put("C20", (l, t) -> setMaximumTagValue(l, LinkTag.C20_MAX_AXLE_LOAD, t));
        SIGN_MAPPINGS.put("C21", (l, t) -> setMaximumTagValue(l, LinkTag.C21_MAX_WEIGHT, t));
    }

    private static final List<String> IGNORED_TEXT_SIGN_TYPES = List.of(
            // Sign has exceptions, e.g. for local traffic.
            "UIT",
            // Sign is a pre-announcement, e.g. restriction starts in 800 metres.
            "VOOR",
            // Sign only applies between certain times.
            "TIJD");

    private static final String DRIVING_DIRECTION_BACKWARD = "T";
    private static final String DRIVING_DIRECTION_FORWARD = "H";

    public void setLinkTags(Link link, List<TrafficSignJsonDtoV3> trafficSigns) {
        for (TrafficSignJsonDtoV3 trafficSign : trafficSigns) {
            String rvvCode = trafficSign.getRvvCode();
            if (SIGN_MAPPINGS.containsKey(rvvCode) && restrictionIsAbsolute(trafficSign)) {
                SIGN_MAPPINGS.get(rvvCode).accept(link, trafficSign);
            }
        }
    }

    private boolean restrictionIsAbsolute(TrafficSignJsonDtoV3 trafficSign) {
        if (trafficSign.getTextSigns() == null) {
            return true;
        }
        return trafficSign.getTextSigns().stream()
                .map(TextSignJsonDtoV3::getType)
                .filter(Objects::nonNull)
                .noneMatch(IGNORED_TEXT_SIGN_TYPES::contains);
    }

    private static void setNoEntryTagValue(Link link, LinkTag<Boolean> linkTag, TrafficSignJsonDtoV3 trafficSign) {
        if (isForward(trafficSign)) {
            link.setTag(linkTag, true, false);
        }
        if (isBackward(trafficSign)) {
            link.setTag(linkTag, true, true);
        }
    }

    private static void setMaximumTagValue(Link link, LinkTag<Double> linkTag, TrafficSignJsonDtoV3 trafficSign) {
        try {
            if (trafficSign.getBlackCode() != null) {
                double value = Double.parseDouble(trafficSign.getBlackCode().replace(",", "."));
                if (isForward(trafficSign)) {
                    setIfSmaller(link, linkTag, value, false);
                }
                if (isBackward(trafficSign)) {
                    setIfSmaller(link, linkTag, value, true);
                }
            }
        } catch (NumberFormatException ignored) {
            log.debug("Unprocessable value {} for traffic sign with RVV code {} on road section {}",
                    trafficSign.getBlackCode(), trafficSign.getRvvCode(),
                    trafficSign.getLocation().getRoad().getRoadSectionId());
        }
    }

    private static boolean isBackward(TrafficSignJsonDtoV3 trafficSign) {
        // Driving direction null (unknown) is mapped to both directions.
        return !DRIVING_DIRECTION_FORWARD.equals(trafficSign.getLocation().getDrivingDirection());
    }

    private static boolean isForward(TrafficSignJsonDtoV3 trafficSign) {
        // Driving direction null (unknown) is mapped to both directions.
        return !DRIVING_DIRECTION_BACKWARD.equals(trafficSign.getLocation().getDrivingDirection());
    }

    private static void setIfSmaller(Link link, LinkTag<Double> linkTag, double value, boolean reverse) {
        double currentValue = link.getTag(linkTag, Double.POSITIVE_INFINITY, reverse);
        if (value < currentValue) {
            link.setTag(linkTag, value, reverse);
        }
    }
}

package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import com.graphhopper.routing.util.EncodingManager;
import lombok.ToString;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;

@ToString(callSuper = true)
public class ExploreLimitRestriction extends ExploreLimit<RestrictionsIsochroneLabel> {

    public static final int LIMIT = 1;

    public static final int IN_ACCESSIBLE = 2;

    public static final int ACCESSIBLE = 0;

    public ExploreLimitRestriction() {
        super(LIMIT, false);
    }

    @Override
    protected double getValueForLabel(RestrictionsIsochroneLabel isochroneLabel, EncodingManager encodingManager) {

        return isochroneLabel.getRestrictions().isEmpty()
                ? ACCESSIBLE
                : IN_ACCESSIBLE;
    }

    @Override
    public String debug(RestrictionsIsochroneLabel isochroneLabel, EncodingManager encodingManager) {
        return "ExploreLimitRestriction{limit=%s, restrictions=%s, reached=%s}".formatted(
                getLimit(),
                isochroneLabel.getRestrictions(),
                !isInLimit(isochroneLabel, encodingManager)
        );
    }
}

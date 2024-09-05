package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(builderClassName = "Builder")
public class RoadSectionFragment<T> {

    double fromFraction;
    double toFraction;
    T data;

    public static class Builder<T> {

        public RoadSectionFragment<T> build() {
            if (fromFraction < 0 || fromFraction > 1) {
                throw new IllegalArgumentException("From fraction should be between 0 and 1, but was: " + fromFraction);
            } else if (toFraction < 0 || toFraction > 1) {
                throw new IllegalArgumentException("To fraction should be between 0 and 1, but was: " + toFraction);
            } else if (fromFraction > toFraction) {
                throw new IllegalArgumentException(
                        "From fraction should be less than to fraction, but was from: " + fromFraction + " to: "
                                + toFraction);
            }

            return new RoadSectionFragment<>(fromFraction, toFraction, data);

        }
    }
}

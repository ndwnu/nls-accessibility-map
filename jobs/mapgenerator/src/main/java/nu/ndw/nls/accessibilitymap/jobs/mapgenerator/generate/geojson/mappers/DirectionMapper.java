package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import org.springframework.stereotype.Component;

@Component
public class DirectionMapper {

    public Direction map(boolean forward) {
        if (forward) {
            return Direction.FORWARD;
        } else {
            return Direction.BACKWARD;
        }
    }

}

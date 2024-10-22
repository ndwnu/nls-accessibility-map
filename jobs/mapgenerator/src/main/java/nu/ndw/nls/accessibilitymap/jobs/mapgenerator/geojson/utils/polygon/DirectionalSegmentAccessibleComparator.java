package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon;

import java.io.Serializable;
import java.util.Comparator;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import org.springframework.stereotype.Component;

@Component
public class DirectionalSegmentAccessibleComparator implements Comparator<DirectionalSegment>, Serializable {

    @Override
    public int compare(DirectionalSegment directionalSegment1, DirectionalSegment directionalSegment2) {

        if (directionalSegment1.isAccessible() == directionalSegment2.isAccessible()) {
            return 0;
        }

        if (directionalSegment1.isAccessible()) {
            return -1;
        } else {
            return 1;
        }
    }
}
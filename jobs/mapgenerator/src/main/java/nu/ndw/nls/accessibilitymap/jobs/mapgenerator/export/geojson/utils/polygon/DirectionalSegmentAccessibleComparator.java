package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.utils.polygon;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import org.springframework.stereotype.Component;

@Component
public class DirectionalSegmentAccessibleComparator implements Comparator<DirectionalSegment>, Serializable {

    @Serial
    private static final long serialVersionUID = 1;

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

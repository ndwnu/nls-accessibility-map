package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.routingmapmatcher.util.PointListUtil;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IsoLabelToGeometryMapper {

    private final PointListUtil pointListUtil;

    public LineString map(EdgeIteratorState edge) {

        return pointListUtil.toLineString(edge.fetchWayGeometry(FetchMode.ALL));
    }
}

package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier;

import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.data.api.nwb.dtos.RoadOperatorType;
import nu.ndw.nls.springboot.test.graph.dto.Edge;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.supplier.NwbRoadSectionDtoSupplier;

public class AccessibilityNwbRoadSectionDtoSupplier extends NwbRoadSectionDtoSupplier {

    @Override
    public NwbRoadSectionDto create(Edge edge, NwbVersionDto nwbVersionDto) {

        var nwbRoadSectionDto = super.create(edge, nwbVersionDto);

        return nwbRoadSectionDto
                .withMunicipalityId(1)
                .withRoadOperatorType(RoadOperatorType.MUNICIPALITY);
    }
}

package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.data.api.nwb.dtos.RoadOperatorType;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.graph.dto.Edge;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.supplier.NwbRoadSectionDtoSupplier;

@RequiredArgsConstructor
public class AccessibilityNwbRoadSectionDtoSupplier extends NwbRoadSectionDtoSupplier {

    private final Map<Long, CarriagewayTypeCode> carriagewayTypeCodeMap;

    @Override
    public NwbRoadSectionDto create(Edge edge, NwbVersionDto nwbVersionDto) {

        NwbRoadSectionDto nwbRoadSectionDto = super.create(edge, nwbVersionDto);
        String drivingDirection = edge.isForward() && edge.isBackward() ? "B" : edge.isForward() ? "H" : "T";
        return nwbRoadSectionDto
                .withFunctionalRoadClass("1")
                .withMunicipalityId(1)
                .withDrivingDirection(drivingDirection)
                .withCarriagewayTypeCode(
                        carriagewayTypeCodeMap.containsKey(edge.getId()) ? carriagewayTypeCodeMap.get(edge.getId()).getCode() : "RB")
                .withRoadOperatorType(RoadOperatorType.MUNICIPALITY);
    }
}

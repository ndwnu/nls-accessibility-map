package nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityNwbRoadSectionMapper {

    private final DrivingDirectionMapper drivingDirectionMapper;

    public AccessibilityNwbRoadSection map(NwbRoadSectionDto nwbRoadSectionDto) {
        DirectionalDto<Boolean> drivingDirection = drivingDirectionMapper.map(nwbRoadSectionDto.getDrivingDirection());
        boolean forwardAccessible = drivingDirection.forward();
        boolean reverseAccessible = drivingDirection.reverse();

        return new AccessibilityNwbRoadSection(
                nwbRoadSectionDto.getRoadSectionId(),
                nwbRoadSectionDto.getJunctionIdFrom(),
                nwbRoadSectionDto.getJunctionIdTo(),
                nwbRoadSectionDto.getMunicipalityId(),
                nwbRoadSectionDto.getGeometry(),
                forwardAccessible,
                reverseAccessible,
                CarriagewayTypeCode.valueOf(nwbRoadSectionDto.getCarriagewayTypeCode()),
                nwbRoadSectionDto.getFunctionalRoadClass());
    }
}

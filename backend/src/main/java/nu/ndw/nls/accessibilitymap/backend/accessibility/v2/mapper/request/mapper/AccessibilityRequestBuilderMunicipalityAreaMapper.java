package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import com.graphhopper.util.shapes.BBox;
import java.util.Objects;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.configuration.BoundingBoxAreaConfiguration;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityAreaRequestJson;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityRequestBuilderMunicipalityAreaMapper extends
        AccessibilityRequestBuilderAreaMapper {

    private final MunicipalityService municipalityService;

    protected AccessibilityRequestBuilderMunicipalityAreaMapper(
            MunicipalityService municipalityService,
            BoundingBoxAreaConfiguration boundingBoxAreaConfiguration) {
        super(boundingBoxAreaConfiguration);
        this.municipalityService = municipalityService;
    }

    public void build(AccessibilityRequestBuilder accessibilityRequestBuilder, AreaRequestJson areaRequestJson) {
        if (areaRequestJson instanceof MunicipalityAreaRequestJson municipalityAreaRequestJson) {
            Municipality municipality = municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId());
            if (Objects.isNull(municipality)) {
                throw new ApiException(
                        UUID.fromString("06d84c7c-7be2-4f79-a8fd-00264f06267d"),
                        HttpStatus.BAD_REQUEST,
                        "Invalid municipalityId",
                        "Municipality with id '%s' not found."
                                .formatted(municipalityAreaRequestJson.getId()));
            }
            BBox requestArea = BBox.fromPoints(
                    municipality.bounds().latitudeFrom(),
                    municipality.bounds().longitudeFrom(),
                    municipality.bounds().latitudeTo(),
                    municipality.bounds().longitudeTo()
            );
            BBox searchArea = applySearchDistance(
                    requestArea,
                    getBoundingBoxAreaConfiguration().getSearchDistanceGapFromRequestedSearchAreaInMeters());

            accessibilityRequestBuilder
                    .municipalityId(municipality.idAsInteger())
                    .requestArea(requestArea)
                    .searchArea(searchArea)
                    .startLocationLatitude(municipality.startCoordinateLatitude())
                    .startLocationLongitude(municipality.startCoordinateLongitude());
        } else {
            throw new IllegalArgumentException("AreaRequestJson must be of type MunicipalityAreaRequestJson");
        }
    }

    @Override
    public boolean canProcessAreaRequest(AreaRequestJson areaRequestJson) {
        return areaRequestJson instanceof MunicipalityAreaRequestJson;
    }
}

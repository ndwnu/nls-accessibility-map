package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.mappers;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficSignMapper {

    private final NwbRoadSectionCrudService roadSectionService;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;
    private final NetworkMetaDataService networkMetaDataService;
    private final CrsTransformer crsTransformer;


    public Optional<TrafficSign> mapFromTrafficSignGeoJsonDto(
            TrafficSignGeoJsonDto trafficSignGeoJsonDto,
            IntegerSequenceSupplier integerSequenceSupplier) {

        try {
            int nwbVersion = networkMetaDataService.loadMetaData().nwbVersion();

            NwbRoadSectionDto roadSectionDto = roadSectionService.findById(
                            new Id(nwbVersion, trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue()))
                    .orElseThrow();
            LineString lineStringWgs84 = (LineString) crsTransformer.transformFromRdNewToWgs84(
                    roadSectionDto.getGeometry());
            lineStringWgs84.setSRID(4326);
            CoordinateAndBearing coordinateAndBearing = fractionAndDistanceCalculator.getCoordinateAndBearing(
                    lineStringWgs84, trafficSignGeoJsonDto.getProperties().getFraction());
            //Latitude is the Y axis, longitude is the X axis.
            return Optional.of(TrafficSign.builder()
                    .id(integerSequenceSupplier.next())
                    .roadSectionId(trafficSignGeoJsonDto.getProperties().getRoadSectionId().intValue())
                    .trafficSignType(TrafficSignType.valueOf(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                    .direction(createDirection(trafficSignGeoJsonDto.getProperties().getDrivingDirection()))
                    .fraction(trafficSignGeoJsonDto.getProperties().getFraction())
                    .latitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLatitude())
                    .longitude(trafficSignGeoJsonDto.getGeometry().getCoordinates().getLongitude())
                    .latitudeOnNwb(coordinateAndBearing.coordinate().getY())
                    .longitudeOnNwb(coordinateAndBearing.coordinate().getX())
                    .iconUri(createIconUri(trafficSignGeoJsonDto.getProperties()))
                    .textSigns(trafficSignGeoJsonDto.getProperties().getTextSigns())
                    .build());
        } catch (Exception exception) {
            log.warn("Traffic sign with id '{}' is incomplete and will be skipped. Traffic sign: {}",
                    trafficSignGeoJsonDto.getId(), trafficSignGeoJsonDto, exception);
            return Optional.empty();
        }
    }

    private Direction createDirection(DirectionType drivingDirection) {

        switch (drivingDirection) {
            case FORTH -> {
                return Direction.FORWARD;
            }
            case BACK -> {
                return Direction.BACKWARD;
            }
            default -> throw new IllegalArgumentException(
                    "Driving direction '%s' could not be mapped.".formatted(drivingDirection));
        }
    }

    private URI createIconUri(TrafficSignPropertiesDto trafficSignPropertiesDto) {
        if (Objects.isNull(trafficSignPropertiesDto.getImageUrl())) {
            return null;
        }

        return URI.create(trafficSignPropertiesDto.getImageUrl());
    }
}

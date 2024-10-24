package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.services;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TrafficSignDataService {

    private final TrafficSignMapper trafficSignMapper;

    private final TrafficSignService trafficSignService;

    public List<TrafficSign> findAllByType(TrafficSignType trafficSignType) {

        IntegerSequenceSupplier idSupplier = new IntegerSequenceSupplier();

        return trafficSignService.getTrafficSigns(Set.of(mapTrafficSignTypeToRvvCode(trafficSignType)))
                .trafficSignsByRoadSectionId().values().stream()
                .flatMap(Collection::stream)
                .map(trafficSignGeoJsonDto -> trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                        trafficSignGeoJsonDto,
                        idSupplier))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private String mapTrafficSignTypeToRvvCode(TrafficSignType trafficSignType) {
        return trafficSignType.name().charAt(0)
                + trafficSignType.name().substring(1).toLowerCase(Locale.US);
    }
}

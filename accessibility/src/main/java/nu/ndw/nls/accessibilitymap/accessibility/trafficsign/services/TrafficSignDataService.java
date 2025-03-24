package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@ConditionalOnBean(TrafficSignService.class)
public class TrafficSignDataService {

    private final TrafficSignMapper trafficSignMapper;

    private final TrafficSignService trafficSignService;

    public List<TrafficSign> findAllBy(AccessibilityRequest accessibilityRequest) {

        IntegerSequenceSupplier idSupplier = new IntegerSequenceSupplier();

        return trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                        .map(TrafficSignType::getRvvCode)
                        .collect(Collectors.toSet()))
                .trafficSignsByRoadSectionId().values().stream()
                .flatMap(Collection::stream)
                .map(trafficSignGeoJsonDto -> trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                        trafficSignGeoJsonDto,
                        idSupplier))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(trafficSign -> trafficSign.isRelevant(accessibilityRequest))
                .toList();
    }

    public List<TrafficSign> findAllByTypes(List<TrafficSignType> trafficSignTypes) {

        IntegerSequenceSupplier idSupplier = new IntegerSequenceSupplier();

        return trafficSignService.getTrafficSigns(mapTrafficSignTypesToRvvCode(trafficSignTypes))
                .trafficSignsByRoadSectionId().values().stream()
                .flatMap(Collection::stream)
                .map(trafficSignGeoJsonDto -> trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                        trafficSignGeoJsonDto,
                        idSupplier))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Set<String> mapTrafficSignTypesToRvvCode(List<TrafficSignType> trafficSignTypes) {
        return trafficSignTypes.stream()
                .map(TrafficSignType::getRvvCode)
                .collect(Collectors.toSet());
    }
}

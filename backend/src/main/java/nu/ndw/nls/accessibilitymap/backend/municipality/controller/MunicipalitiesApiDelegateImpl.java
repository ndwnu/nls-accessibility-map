package nu.ndw.nls.accessibilitymap.backend.municipality.controller;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.MunicipalitiesApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper.MunicipalityFeatureMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalitiesApiDelegateImpl implements MunicipalitiesApiDelegate {

    private final MunicipalityFeatureMapper municipalityFeatureMapper;

    private final MunicipalityService municipalityService;

    @Override
    public ResponseEntity<MunicipalityFeatureCollectionJson> getMunicipalities() {

        return ResponseEntity.ok(municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                municipalityService.findAll().stream()
                        .sorted(Comparator.comparing(Municipality::municipalityId))
                        .toList())
        );
    }
}

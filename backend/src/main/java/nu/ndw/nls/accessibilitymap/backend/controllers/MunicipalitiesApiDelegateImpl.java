package nu.ndw.nls.accessibilitymap.backend.controllers;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.MunicipalitiesApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.MunicipalityFeatureMapper;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalitiesApiDelegateImpl implements MunicipalitiesApiDelegate {

    private final MunicipalityFeatureMapper municipalityFeatureMapper;
    private final MunicipalityConfiguration municipalityConfiguration;

    @Override
    public ResponseEntity<MunicipalityFeatureCollectionJson> getMunicipalities() {
        return ResponseEntity.ok(municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                municipalityConfiguration.getMunicipalities().values().stream()
                        .sorted(Comparator.comparing(Municipality::getMunicipalityId))
                        .toList())
        );
    }
}

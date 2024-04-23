package nu.ndw.nls.accessibilitymap.backend.controllers;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityConfiguration;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.MunicipalitiesApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.MunicipalityFeatureMapper;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalitiesApiDelegateImpl implements MunicipalitiesApiDelegate {

    private final MunicipalityFeatureMapper municipalityFeatureMapper;
    private final MunicipalityConfiguration municipalityConfiguration;

    @Override
    public ResponseEntity<FeatureCollectionJson> getMunicipalities() {
        return ResponseEntity.ok(municipalityFeatureMapper.mapToMunicipalitiesToGeoJSON(
                municipalityConfiguration.getMunicipalities().values().stream()
                        .sorted(Comparator.comparing(Municipality::getMunicipalityId))
                        .toList())
        );
    }

}

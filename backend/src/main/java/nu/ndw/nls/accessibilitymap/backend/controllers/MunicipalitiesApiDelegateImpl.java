package nu.ndw.nls.accessibilitymap.backend.controllers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.config.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.MunicipalitiesApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.MunicipalityMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalitiesApiDelegateImpl implements MunicipalitiesApiDelegate {

    private final MunicipalityMapper municipalityMapper;
    private final MunicipalityProperties municipalityProperties;

    @Override
    public ResponseEntity<FeatureCollectionJson> getMunicipalities() {
        return ResponseEntity.ok(municipalityMapper.mapToMunicipalitiesToGeoJSON(
                municipalityProperties.getMunicipalities().values()));
    }

}

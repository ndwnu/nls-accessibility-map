package nu.ndw.nls.accessibilitymap.backend.municipality.controller;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper.MunicipalityFeatureMapperV2;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.openapi.api.v2.MunicipalitiesV2ApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityFeatureCollectionJson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalitiesV2ApiDelegateImpl implements MunicipalitiesV2ApiDelegate {

    private final MunicipalityFeatureMapperV2 municipalityFeatureMapper;

    private final MunicipalityService municipalityService;

    @Override
    public ResponseEntity<MunicipalityFeatureCollectionJson> getMunicipalities() {

        return ResponseEntity.ok(municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                municipalityService.findAll().stream()
                        .sorted(Comparator.comparing(Municipality::id))
                        .toList())
        );
    }
}

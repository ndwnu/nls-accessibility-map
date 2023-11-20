package nu.ndw.nls.accessibilitymap.backend.services;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.config.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private final MunicipalityProperties municipalityProperties;

    public Municipality getMunicipalityById(String municipalityId) {
        return municipalityProperties.getMunicipalities().get(municipalityId);
    }
}

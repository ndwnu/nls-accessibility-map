package nu.ndw.nls.accessibilitymap.backend.municipality.services;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityConfiguration;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private static final String MESSAGE_TEMPLATE = "The municipality with id: %s cannot be found";
    private final MunicipalityConfiguration municipalityConfiguration;


    public Municipality getMunicipalityById(String municipalityId) {
        if (!municipalityConfiguration.getMunicipalities().containsKey(municipalityId)) {
            throw new MunicipalityNotFoundException(MESSAGE_TEMPLATE.formatted(municipalityId));
        }
        return municipalityConfiguration.getMunicipalities().get(municipalityId);
    }
}

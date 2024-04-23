package nu.ndw.nls.accessibilitymap.backend.services;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityConfiguration;
import nu.ndw.nls.accessibilitymap.backend.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private static final String MESSAGE_TEMPLATE = "The municipality with id: %s cannot be found";
    private final MunicipalityConfiguration municipalityConfiguration;


    public Municipality getMunicipalityById(String municipalityId) {
        if (!municipalityConfiguration.getMunicipalities().containsKey(municipalityId)) {
            throw new MunicipalityNotFoundException(String.format(MESSAGE_TEMPLATE, municipalityId));
        }
        return municipalityConfiguration.getMunicipalities().get(municipalityId);
    }
}

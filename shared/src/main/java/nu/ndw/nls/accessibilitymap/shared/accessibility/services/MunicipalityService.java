package nu.ndw.nls.accessibilitymap.shared.accessibility.services;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.accessibility.municipality.MunicipalityConfiguration;
import nu.ndw.nls.accessibilitymap.shared.accessibility.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.Municipality;
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

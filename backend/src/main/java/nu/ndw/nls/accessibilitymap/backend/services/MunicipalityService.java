package nu.ndw.nls.accessibilitymap.backend.services;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.config.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.exceptions.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private static final String MESSAGE_TEMPLATE = "The municipality with id: %s cannot be found";
    private final MunicipalityProperties municipalityProperties;


    public Municipality getMunicipalityById(String municipalityId) {
        if (!municipalityProperties.getMunicipalities().containsKey(municipalityId)) {
            throw new MunicipalityNotFoundException(String.format(MESSAGE_TEMPLATE, municipalityId));
        }
        return municipalityProperties.getMunicipalities().get(municipalityId);
    }
}

package nu.ndw.nls.accessibilitymap.backend.municipality.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.exception.MunicipalityNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.MunicipalityRepository;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private static final String MESSAGE_TEMPLATE = "The municipality with id: %s cannot be found";

    private final MunicipalityRepository municipalityRepository;


    public List<Municipality> findAll() {

        return municipalityRepository.findAll();
    }

    public Municipality getMunicipalityById(String municipalityId) {

        return municipalityRepository.findFirstById(municipalityId)
                .orElseThrow(() -> new MunicipalityNotFoundException(MESSAGE_TEMPLATE.formatted(municipalityId)));
    }
}

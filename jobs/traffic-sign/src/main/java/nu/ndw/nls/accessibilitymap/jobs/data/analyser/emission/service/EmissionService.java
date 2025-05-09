package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.service;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.client.EmissionZoneClient;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZone;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmissionService {

    private final EmissionZoneClient emissionZoneClient;

    private List<EmissionZone> cachedEmissionZones;

    public List<EmissionZone> findAll() {

        try {
            if(Objects.isNull(cachedEmissionZones)) {
                cachedEmissionZones = emissionZoneClient.findAll();
            }

            return cachedEmissionZones;
        } catch (final FeignException.FeignServerException | ConstraintViolationException exception) {
            log.error("Something went wrong with getting emission information from the Road Features Area API.", exception);
            return List.of();
        } catch (final FeignException.FeignClientException exception) {
            log.error("No emission information available.", exception);
            return List.of();
        } catch (final RuntimeException exception) {
            log.warn("Error while retrieving emission. Retrying at a later moment.", exception);
            throw exception;
        }
    }

    public Optional<EmissionZone> findById(String id) {

        return findAll().stream()
                .filter(emissionZone -> emissionZone.id().equals(id))
                .findFirst();
    }
}

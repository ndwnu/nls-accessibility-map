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
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.FuelType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmissionService {

    private final EmissionZoneClient emissionZoneClient;

    private List<EmissionZone> cachedEmissionZones;

    public List<EmissionZone> findAll() {

        try {
            if (Objects.isNull(cachedEmissionZones)) {
                cachedEmissionZones = emissionZoneClient.findAll().stream()
                        .filter(this::isValid)
                        .toList();
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

    public Optional<EmissionZone> findByTrafficRegulationOrderId(String trafficRegulationOrderId) {

        return findAll().stream()
                .filter(emissionZone -> emissionZone.trafficRegulationOrderId().equals(trafficRegulationOrderId))
                .findFirst();
    }

    private boolean isValid(EmissionZone emissionZone) {
        if (emissionZone.restriction().fuelType() == FuelType.BATTERY
            || emissionZone.restriction().fuelType() == FuelType.ALL) {
            log.error("Fuel type is BATTERY and ALL are not a supported type. This is technically supported in the api specifications from"
                      + " the Emission zone api but it can never be used because there is no exemption available for vehicle types with a"
                      + " zero emission classification in the emission zone api in field"
                      + " `euVehicleCategoryAndEmissionClassificationRestrictionExemptions`. If this does occur than we should contact"
                      + " W&R and Edwin van Wilgenburg about why this is now suddenly possible in the data. We where guaranteed that this"
                      + " combination would never be used as a solution that the exemptions could never support a zero emission"
                      + " classification. For this reason this emission zone will be considered invalid and can not be used.");
            return false;
        }

        return true;
    }
}

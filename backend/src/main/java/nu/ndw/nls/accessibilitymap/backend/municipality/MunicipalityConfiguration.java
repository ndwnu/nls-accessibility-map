package nu.ndw.nls.accessibilitymap.backend.municipality;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.backend.municipality.mappers.MunicipalityMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MunicipalityConfiguration {

    private final MunicipalityMapper municipalityMapper;

    private final Map<String, Municipality> municipalities;

    public MunicipalityConfiguration(MunicipalityMapper municipalityMapper, MunicipalityProperties municipalityProperties) {

        this.municipalityMapper = municipalityMapper;

        this.municipalities = new HashMap<>();
        municipalityProperties.getMunicipalities()
                .forEach((name, municipalityProperty) -> this.municipalities.put(name,
                        municipalityMapper.map(municipalityProperty)));
    }

}

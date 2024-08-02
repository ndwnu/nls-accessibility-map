package nu.ndw.nls.accessibilitymap.shared.accessibility.municipality;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.shared.accessibility.municipality.mappers.MunicipalityMapper;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MunicipalityConfiguration {

    private final MunicipalityMapper municipalityMapper;

    private final Map<String, Municipality> municipalities;

    public MunicipalityConfiguration(MunicipalityMapper municipalityMapper,
            MunicipalityProperties municipalityProperties) {
        this.municipalityMapper = municipalityMapper;

        this.municipalities = new HashMap<>();

        municipalityProperties.getMunicipalities()
                .forEach((name, municipalityProperty) -> this.municipalities.put(name,
                        municipalityMapper.map(municipalityProperty)));
    }

}

package nu.ndw.nls.accessibilitymap.shared.accessibility.municipality.mappers;

import nu.ndw.nls.accessibilitymap.shared.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.shared.accessibility.municipality.MunicipalityProperty;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses =
        {MunicipalityCoordinateMapper.class, MunicipalityIdMapper.class})
public interface MunicipalityMapper {

    @Mapping(source=".", target = "startPoint")
    @Mapping(source="municipalityId", target = "municipalityIdInteger")
    Municipality map(MunicipalityProperty municipalityProperty);

}

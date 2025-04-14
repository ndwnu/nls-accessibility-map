package nu.ndw.nls.accessibilitymap.backend.municipality.mappers;

import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityProperty;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses =
        {MunicipalityCoordinateMapper.class, MunicipalityIdMapper.class})
public interface MunicipalityMapper {

    @Mapping(source = ".", target = "startPoint")
    @Mapping(source = "municipalityId", target = "municipalityIdInteger")
    Municipality map(MunicipalityProperty municipalityProperty);

}

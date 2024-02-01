package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;


@Getter
@Setter
@RequiredArgsConstructor
public class TrafficSignAccessibilityDto {

    private DirectionalDto<Boolean> carAccessForbidden;

    private DirectionalDto<Boolean> hgvAccessForbidden;

    private DirectionalDto<Boolean> busAccessForbidden;

    private DirectionalDto<Boolean> hgvAndBusAccessForbidden;

    private DirectionalDto<Boolean> tractorAccessForbidden;

    private DirectionalDto<Boolean> slowVehicleAccessForbidden;

    private DirectionalDto<Boolean> trailerAccessForbidden;

    private DirectionalDto<Boolean> motorcycleAccessForbidden;

    private DirectionalDto<Boolean> motorVehicleAccessForbidden;

    private DirectionalDto<Double> maxLength;

    private DirectionalDto<Double> maxWidth;

    private DirectionalDto<Double> maxHeight;

    private DirectionalDto<Double> maxAxleLoad;

    private DirectionalDto<Double> maxWeight;

    private DirectionalDto<Boolean> lcvAndHgvAccessForbidden;

}

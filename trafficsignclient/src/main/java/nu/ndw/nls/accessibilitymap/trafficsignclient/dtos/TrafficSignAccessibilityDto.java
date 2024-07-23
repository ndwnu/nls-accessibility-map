package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;


@Getter
@Setter
@RequiredArgsConstructor
public class TrafficSignAccessibilityDto {

    private DirectionalDto<Boolean> carAccessForbidden;
    private DirectionalDto<Boolean> carAccessForbiddenWindowed;

    private DirectionalDto<Boolean> hgvAccessForbidden;
    private DirectionalDto<Boolean> hgvAccessForbiddenWindowed;

    private DirectionalDto<Boolean> busAccessForbidden;

    private DirectionalDto<Boolean> hgvAndBusAccessForbidden;
    private DirectionalDto<Boolean> hgvAndBusAccessForbiddenWindowed;

    private DirectionalDto<Boolean> tractorAccessForbidden;

    private DirectionalDto<Boolean> slowVehicleAccessForbidden;

    private DirectionalDto<Boolean> trailerAccessForbidden;

    private DirectionalDto<Boolean> motorcycleAccessForbidden;

    private DirectionalDto<Boolean> motorVehicleAccessForbidden;
    private DirectionalDto<Boolean> motorVehicleAccessForbiddenWindowed;

    private DirectionalDto<Boolean> lcvAndHgvAccessForbidden;
    private DirectionalDto<Boolean> lcvAndHgvAccessForbiddenWindowed;

    private DirectionalDto<Double> maxLength;

    private DirectionalDto<Double> maxWidth;

    private DirectionalDto<Double> maxHeight;

    private DirectionalDto<Double> maxAxleLoad;

    private DirectionalDto<Double> maxWeight;


}

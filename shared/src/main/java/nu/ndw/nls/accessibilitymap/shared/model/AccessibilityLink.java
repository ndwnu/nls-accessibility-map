package nu.ndw.nls.accessibilitymap.shared.model;

import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.routingmapmatcher.network.annotations.EncodedValue;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.Link;
import org.locationtech.jts.geom.LineString;

@Getter
public class AccessibilityLink extends Link {

    public static final String MUNICIPALITY_CODE = "municipality_code";
    public static final String CAR_ACCESS_FORBIDDEN = "car_access_forbidden";
    public static final String CAR_ACCESS_FORBIDDEN_WINDOWED = "car_access_forbidden_windowed";
    public static final String HGV_ACCESS_FORBIDDEN = "hgv_access_forbidden";
    public static final String HGV_ACCESS_FORBIDDEN_WINDOWED = "hgv_access_forbidden_windowed";
    public static final String BUS_ACCESS_FORBIDDEN = "bus_access_forbidden";
    public static final String HGV_AND_BUS_ACCESS_FORBIDDEN = "hgv_and_bus_access_forbidden";
    public static final String HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED = "hgv_and_bus_access_forbidden_windowed";
    public static final String TRACTOR_ACCESS_FORBIDDEN = "tractor_access_forbidden";
    public static final String SLOW_VEHICLE_ACCESS_FORBIDDEN = "slow_vehicle_access_forbidden";
    public static final String TRAILER_ACCESS_FORBIDDEN = "trailer_access_forbidden";
    public static final String MOTORCYCLE_ACCESS_FORBIDDEN = "motorcycle_access_forbidden";
    public static final String MOTOR_VEHICLE_ACCESS_FORBIDDEN = "motor_vehicle_access_forbidden";
    public static final String MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED = "motor_vehicle_access_forbidden_windowed";
    public static final String LCV_AND_HGV_ACCESS_FORBIDDEN = "lcv_and_hgv_access_forbidden";
    public static final String LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED = "lcv_and_hgv_access_forbidden_windowed";
    public static final String MAX_LENGTH = "max_length";
    public static final String MAX_WIDTH = "max_width";
    public static final String MAX_HEIGHT = "max_height";
    public static final String MAX_AXLE_LOAD = "max_axle_load";
    public static final String MAX_WEIGHT = "max_weight";

    private final DirectionalDto<Boolean> accessibility;

    @EncodedValue(key = CAR_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> carAccessForbidden;

    @EncodedValue(key = CAR_ACCESS_FORBIDDEN_WINDOWED)
    private final DirectionalDto<Boolean> carAccessForbiddenWindowed;

    @EncodedValue(key = HGV_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> hgvAccessForbidden;

    @EncodedValue(key = HGV_ACCESS_FORBIDDEN_WINDOWED)
    private final DirectionalDto<Boolean> hgvAccessForbiddenWindowed;

    @EncodedValue(key = BUS_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> busAccessForbidden;

    @EncodedValue(key = HGV_AND_BUS_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> hgvAndBusAccessForbidden;

    @EncodedValue(key = HGV_AND_BUS_ACCESS_FORBIDDEN_WINDOWED)
    private final DirectionalDto<Boolean> hgvAndBusAccessForbiddenWindowed;

    @EncodedValue(key = TRACTOR_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> tractorAccessForbidden;

    @EncodedValue(key = SLOW_VEHICLE_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> slowVehicleAccessForbidden;

    @EncodedValue(key = TRAILER_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> trailerAccessForbidden;

    @EncodedValue(key = MOTORCYCLE_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> motorcycleAccessForbidden;

    @EncodedValue(key = MOTOR_VEHICLE_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> motorVehicleAccessForbidden;

    @EncodedValue(key = MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED)
    private final DirectionalDto<Boolean> motorVehicleAccessForbiddenWindowed;

    @EncodedValue(key = LCV_AND_HGV_ACCESS_FORBIDDEN)
    private final DirectionalDto<Boolean> lcvAndHgvAccessForbidden;

    @EncodedValue(key = LCV_AND_HGV_ACCESS_FORBIDDEN_WINDOWED)
    private final DirectionalDto<Boolean> lcvAndHgvAccessForbiddenWindowed;

    @EncodedValue(key = MAX_LENGTH, bits = 7)
    private final DirectionalDto<Double> maxLength;

    @EncodedValue(key = MAX_WIDTH, bits = 7)
    private final DirectionalDto<Double> maxWidth;

    @EncodedValue(key = MAX_HEIGHT, bits = 7)
    private final DirectionalDto<Double> maxHeight;

    @EncodedValue(key = MAX_AXLE_LOAD, bits = 7)
    private final DirectionalDto<Double> maxAxleLoad;

    @EncodedValue(key = MAX_WEIGHT, bits = 7)
    private final DirectionalDto<Double> maxWeight;

    @EncodedValue(key = MUNICIPALITY_CODE, bits = 17)
    private final Integer municipalityCode;

    @Builder
    protected AccessibilityLink(long id, long fromNodeId, long toNodeId,
            DirectionalDto<Boolean> accessibility, double distanceInMeters,
            LineString geometry, DirectionalDto<Boolean> carAccessForbidden,
            DirectionalDto<Boolean> carAccessForbiddenWindowed,
            DirectionalDto<Boolean> hgvAccessForbidden,
            DirectionalDto<Boolean> hgvAccessForbiddenWindowed,
            DirectionalDto<Boolean> busAccessForbidden,
            DirectionalDto<Boolean> hgvAndBusAccessForbidden,
            DirectionalDto<Boolean> hgvAndBusAccessForbiddenWindowed,
            DirectionalDto<Boolean> tractorAccessForbidden,
            DirectionalDto<Boolean> slowVehicleAccessForbidden,
            DirectionalDto<Boolean> trailerAccessForbidden,
            DirectionalDto<Boolean> motorcycleAccessForbidden,
            DirectionalDto<Boolean> motorVehicleAccessForbidden,
            DirectionalDto<Boolean> motorVehicleAccessForbiddenWindowed,
            DirectionalDto<Boolean> lcvAndHgvAccessForbidden,
            DirectionalDto<Boolean> lcvAndHgvAccessForbiddenWindowed,
            DirectionalDto<Double> maxLength, DirectionalDto<Double> maxWidth,
            DirectionalDto<Double> maxHeight, DirectionalDto<Double> maxAxleLoad,
            DirectionalDto<Double> maxWeight, Integer municipalityCode) {
        super(id, fromNodeId, toNodeId, distanceInMeters, geometry);
        this.accessibility = accessibility;
        this.carAccessForbidden = carAccessForbidden;
        this.carAccessForbiddenWindowed = carAccessForbiddenWindowed;
        this.hgvAccessForbidden = hgvAccessForbidden;
        this.hgvAccessForbiddenWindowed = hgvAccessForbiddenWindowed;
        this.busAccessForbidden = busAccessForbidden;
        this.hgvAndBusAccessForbidden = hgvAndBusAccessForbidden;
        this.hgvAndBusAccessForbiddenWindowed = hgvAndBusAccessForbiddenWindowed;
        this.tractorAccessForbidden = tractorAccessForbidden;
        this.slowVehicleAccessForbidden = slowVehicleAccessForbidden;
        this.trailerAccessForbidden = trailerAccessForbidden;
        this.motorcycleAccessForbidden = motorcycleAccessForbidden;
        this.motorVehicleAccessForbidden = motorVehicleAccessForbidden;
        this.motorVehicleAccessForbiddenWindowed = motorVehicleAccessForbiddenWindowed;
        this.lcvAndHgvAccessForbidden = lcvAndHgvAccessForbidden;
        this.lcvAndHgvAccessForbiddenWindowed = lcvAndHgvAccessForbiddenWindowed;
        this.maxLength = maxLength;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxAxleLoad = maxAxleLoad;
        this.maxWeight = maxWeight;
        this.municipalityCode = municipalityCode;
    }
}
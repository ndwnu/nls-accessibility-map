package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TrafficSignPropertiesDto {

    private String externalId;
    private String validated;
    private LocalDate validatedOn;
    private String rvvCode;
    private String blackCode;
    private String zoneCode;
    private TrafficSignStatusType status;
    private List<TextSign> textSigns;

    // Location
    private Geometry geometry;
    private PlacementType placement;
    private LocationSideType side;
    private Integer bearing;
    private Integer nenTurningDirection;
    private Double fraction;
    private DirectionType drivingDirection;
    private String roadName;
    private RoadType roadType;
    private Integer roadNumber;
    // The API offers Integers, but we use long, so convert it to Long here to avoid a lot of conversions in our own
    // code. Road section id's can be null, for example when the signs are attached to buildings
    private Long roadSectionId;
    private LocalDate nwbVersion;
    private String countyName;
    private String countyCode;
    private String townName;
    private String bgtCode;

    // TrafficSignDetails
    private String imageUrl;
    private LocalDate firstSeenOn;
    // The first date the sign has been seen onn
    private LocalDate lastSeenOn;
    private LocalDate removedOn;
    private LocalDate placedOn;
    private LocalDate expectedPlacedOn;
    private LocalDate expectedRemovedOn;
    private String trafficOrderUrl;

}

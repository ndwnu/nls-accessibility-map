package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrafficSignPropertiesDto {

    private String externalId;
    private String validated;
    private LocalDate validatedOn;
    private String rvvCode;
    private String blackCode;
    private String zoneCode;
    private TrafficSignStatus status;
    private List<TextSignDto> textSigns;

    // Location
    private String placement;
    private String side;
    private Integer bearing;
    private Integer nenTurningDirection;
    private Double fraction;
    private String drivingDirection;
    private String roadName;
    private Integer roadType;
    private Integer roadNumber;
    private Integer roadSectionId;
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

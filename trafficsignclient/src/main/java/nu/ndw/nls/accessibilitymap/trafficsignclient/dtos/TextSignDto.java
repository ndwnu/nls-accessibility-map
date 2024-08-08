package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TextSignDto {

    private String type;
    private String text;
    private String openingHours;

}

package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import lombok.Builder;

//https://docs.ndw.nu/en/handleidingen/George/onderborden/
//OB254	Periode	ma t/m vr 06-10h
//OB256	Einde periode	ma t/m vr 06-10h
//OB259	Uitgezonderd periode	ma t/m vr 06-10h

// TextSignDtoV5 in API
@Builder
public record SupplementarySign(SupplementarySignType type, String text) {

    public boolean hasWindowTime() {
        return type() == SupplementarySignType.OB254 || type() == SupplementarySignType.OB256 || type() == SupplementarySignType.OB259;
    }
}

package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TextSignFilterServiceTest {

    @InjectMocks
    private TextSignFilterService textSignFilterService;

    @Mock
    private TextSign textSignDtoA;
    @Mock
    private TextSign textSignDtoB;
    @Mock
    private TextSign textSignDtoC;
    @Mock
    private TextSign textSignDtoD;
    @Mock
    private TextSign textSignDtoE;
    @Mock
    private TextSign textSignDtoF;
    @Mock
    private TextSign textSignDtoG;
    @Mock
    private TextSign textSignDtoH;

    @Test
    void findFirstWindowTimeTextSign_ok_findsFirst() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.DIRECTION_ARROWS);
        when(textSignDtoB.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoC.getType()).thenReturn(TextSignType.EXCLUDING);
        when(textSignDtoD.getType()).thenReturn(TextSignType.FREE_TEXT);
        when(textSignDtoE.getType()).thenReturn(TextSignType.LICENSE_PLATE);
        when(textSignDtoF.getType()).thenReturn(TextSignType.PRE_ANNOUNCEMENT);
        when(textSignDtoG.getType()).thenReturn(TextSignType.TIME_PERIOD);

        assertThat(textSignFilterService.findFirstWindowTimeTextSign(List.of(textSignDtoA, textSignDtoB, textSignDtoC
            ,textSignDtoD, textSignDtoE, textSignDtoF, textSignDtoG, textSignDtoH)))
                .isEqualTo(Optional.of(textSignDtoG));

        // non-parallel stream, should find textSignDtoG first and then return and never check more types
        verify(textSignDtoH, never()).getType();
    }

    @Test
    void findFirstWindowTimeTextSign_ok_findsNoneReturnsOptionalEmpty() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.DIRECTION_ARROWS);
        when(textSignDtoB.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoC.getType()).thenReturn(TextSignType.EXCLUDING);

        assertThat(textSignFilterService.findFirstWindowTimeTextSign(List.of(textSignDtoA, textSignDtoB, textSignDtoC)))
                .isEqualTo(Optional.empty());
    }

    @Test
    void hasWindowTime_ok_hasWindowTimeReturnsTrue() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.DIRECTION_ARROWS);
        when(textSignDtoB.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoC.getType()).thenReturn(TextSignType.EXCLUDING);
        when(textSignDtoD.getType()).thenReturn(TextSignType.FREE_TEXT);
        when(textSignDtoE.getType()).thenReturn(TextSignType.LICENSE_PLATE);
        when(textSignDtoF.getType()).thenReturn(TextSignType.PRE_ANNOUNCEMENT);
        when(textSignDtoG.getType()).thenReturn(TextSignType.TIME_PERIOD);

        assertThat(textSignFilterService.hasWindowTime(List.of(textSignDtoA, textSignDtoB, textSignDtoC
                ,textSignDtoD, textSignDtoE, textSignDtoF, textSignDtoG)))
                .isTrue();
    }

    @Test
    void hasWindowTime_ok_hasWindowTimeReturnsFalse() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.DIRECTION_ARROWS);
        when(textSignDtoB.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoC.getType()).thenReturn(TextSignType.EXCLUDING);

        assertThat(textSignFilterService.hasWindowTime(List.of(textSignDtoA, textSignDtoB, textSignDtoC)))
                .isFalse();
    }

    @Test
    void hasNoExcludingOrPreAnnouncement_ok_hasExcludingOrPreAnnouncementReturnsTrue() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.DIRECTION_ARROWS);
        when(textSignDtoB.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoC.getType()).thenReturn(TextSignType.FREE_TEXT);
        when(textSignDtoD.getType()).thenReturn(TextSignType.LICENSE_PLATE);
        when(textSignDtoE.getType()).thenReturn(TextSignType.TIME_PERIOD);

        assertThat(textSignFilterService.hasNoExcludingOrPreAnnouncement(List.of(textSignDtoA, textSignDtoB, textSignDtoC
                ,textSignDtoD, textSignDtoE)))
                .isTrue();
    }

    @Test
    void hasNoExcludingOrPreAnnouncement_ok_hasExcludingReturnsFalse() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoB.getType()).thenReturn(TextSignType.EXCLUDING);

        assertThat(textSignFilterService.hasNoExcludingOrPreAnnouncement(List.of(textSignDtoA, textSignDtoB,
                textSignDtoC)))
                .isFalse();

        // non-parallel stream, should find textSignDtoB first and then return and never check more types
        verify(textSignDtoC, never()).getType();
    }

    @Test
    void hasNoExcludingOrPreAnnouncement_ok_hasPreAnnouncementReturnsFalse() {
        when(textSignDtoA.getType()).thenReturn(TextSignType.EMISSION_ZONE);
        when(textSignDtoB.getType()).thenReturn(TextSignType.PRE_ANNOUNCEMENT);

        assertThat(textSignFilterService.hasNoExcludingOrPreAnnouncement(List.of(textSignDtoA, textSignDtoB,
                textSignDtoC)))
                .isFalse();

        // non-parallel stream, should find textSignDtoB first and then return and never check more types
        verify(textSignDtoC, never()).getType();
    }
}
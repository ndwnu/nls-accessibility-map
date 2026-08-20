package nu.ndw.nls.accessibilitymap.accessibility.osm.openinghours;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.osm.OpeningHoursVisitor;
import nu.ndw.nls.accessibilitymap.accessibility.osm.dto.MonthDayRange;
import nu.ndw.nls.accessibilitymap.accessibility.osm.dto.Window;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class OpeningHoursVisitorTest {

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {
        OpeningHoursVisitor.clearCache();
    }

    @ParameterizedTest
    @MethodSource("listOfUsedOpeningHoursExpressionsInTrafficSignApi")
    void parse_realLifeUseCasesTrafficSignApi(String openingHoursExpression, List<Window> expectedWindows) {
        assertThat(OpeningHoursVisitor.parse(openingHoursExpression)).contains(expectedWindows);
    }

    @ParameterizedTest
    @MethodSource("listOfUnsupportedOpeningHoursExpressionsInTrafficSignApi")
    void parse_emptyForUnsupportedExpressions(String openingHoursExpression) {
        assertThat(OpeningHoursVisitor.parse(openingHoursExpression)).isEmpty();

        assertParseFailureIsLogged(openingHoursExpression);
    }

    @ParameterizedTest
    @MethodSource("theoreticalOpeningHoursExpressions")
    void parse_theoreticalGrammarCases(String openingHoursExpression, List<Window> expectedWindows) {
        assertThat(OpeningHoursVisitor.parse(openingHoursExpression)).contains(expectedWindows);
    }

    @ParameterizedTest
    @MethodSource("theoreticalUnsupportedOpeningHoursExpressions")
    void parse_emptyForTheoreticalUnsupportedExpressions(String openingHoursExpression) {
        assertThat(OpeningHoursVisitor.parse(openingHoursExpression)).isEmpty();

        assertParseFailureIsLogged(openingHoursExpression);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not an opening hours expression", "Mo-Fr 09:00-", "99:99-10:00"})
    void parse_emptyForSyntacticallyInvalidExpressions(String openingHoursExpression) {
        assertThat(OpeningHoursVisitor.parse(openingHoursExpression)).isEmpty();

        assertParseFailureIsLogged(openingHoursExpression);
    }

    private static Stream<Arguments> theoreticalOpeningHoursExpressions() {
        return Stream.of(
                Arguments.of(
                        "24/7",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Mo",
                        List.of(window(EnumSet.of(DayOfWeek.MONDAY), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Mo-We",
                        List.of(window(
                                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "09:00-17:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Jan-Mar",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.MARCH)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Jan-Mar Mo-Fr 09:00-17:00",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.MARCH)),
                                weekdays(), LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Mo,We,Fr 09:00-10:00",
                        List.of(window(
                                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                                LocalTime.of(9, 0), LocalTime.of(10, 0)))),
                Arguments.of(
                        "Mo-Fr 08:00-09:00,14:00-15:00",
                        List.of(
                                window(weekdays(), LocalTime.of(8, 0), LocalTime.of(9, 0)),
                                window(weekdays(), LocalTime.of(14, 0), LocalTime.of(15, 0)))),
                Arguments.of(
                        "Mo 09:00-12:00; Tu 13:00-15:00",
                        List.of(
                                window(EnumSet.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                window(EnumSet.of(DayOfWeek.TUESDAY), LocalTime.of(13, 0), LocalTime.of(15, 0)))),
                Arguments.of(
                        "23:00-24:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(23, 0), LocalTime.MIDNIGHT))),
                Arguments.of(
                        "20:00-26:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(20, 0), LocalTime.of(2, 0)))),
                Arguments.of(
                        "Jan Mo 09:00-17:00",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.JANUARY)), EnumSet.of(DayOfWeek.MONDAY),
                                LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Jan 09:00-17:00",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.JANUARY)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Jan-Mar Mo-Fr",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.MARCH)), weekdays(),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Jan,Mar",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.JANUARY), monthRange(Month.MARCH, Month.MARCH)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Nov-Feb",
                        List.of(dates(
                                List.of(monthRange(Month.NOVEMBER, Month.FEBRUARY)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Jan-Dec",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.DECEMBER)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Feb-Apr,Jun-Aug,Sep,Oct,Dec",
                        List.of(dates(
                                List.of(
                                        monthRange(Month.FEBRUARY, Month.APRIL),
                                        monthRange(Month.JUNE, Month.AUGUST),
                                        monthRange(Month.SEPTEMBER, Month.SEPTEMBER),
                                        monthRange(Month.OCTOBER, Month.OCTOBER),
                                        monthRange(Month.DECEMBER, Month.DECEMBER)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Sa-Mo 09:00-17:00",
                        List.of(window(
                                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                                LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Mo-Su 09:00-17:00",
                        List.of(window(EnumSet.allOf(DayOfWeek.class), LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Tu-We,Th,Fr-Sa 09:00-17:00",
                        List.of(window(
                                EnumSet.of(
                                        DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                                        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Mo 09:00-12:00 || Tu 13:00-15:00",
                        List.of(
                                window(EnumSet.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(12, 0)),
                                window(EnumSet.of(DayOfWeek.TUESDAY), LocalTime.of(13, 0), LocalTime.of(15, 0)))),
                Arguments.of(
                        "Jan-Mar Mo-Fr 08:00-09:00,17:00-18:00; Jul Sa-Su 10:00-16:00",
                        List.of(
                                dates(
                                        List.of(monthRange(Month.JANUARY, Month.MARCH)), weekdays(),
                                        LocalTime.of(8, 0), LocalTime.of(9, 0)),
                                dates(
                                        List.of(monthRange(Month.JANUARY, Month.MARCH)), weekdays(),
                                        LocalTime.of(17, 0), LocalTime.of(18, 0)),
                                dates(
                                        List.of(monthRange(Month.JULY, Month.JULY)),
                                        EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                                        LocalTime.of(10, 0), LocalTime.of(16, 0)))),
                Arguments.of(
                        "Apr 01-Oct 01",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.OCTOBER, 1)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 01-Oct 01 Mo-Fr 09:00-17:00",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.OCTOBER, 1)), weekdays(),
                                LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Apr 01-Oct 01 09:00-17:00",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.OCTOBER, 1)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.of(9, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Apr 01",
                        List.of(dates(
                                List.of(singleDate(Month.APRIL, 1)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 1",
                        List.of(dates(
                                List.of(singleDate(Month.APRIL, 1)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 01-15",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.APRIL, 15)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 01-Apr 15",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.APRIL, 15)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 01+",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.DECEMBER, 31)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Dec 25+",
                        List.of(dates(
                                List.of(dateRange(Month.DECEMBER, 25, Month.DECEMBER, 31)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Dec 25-Jan 05",
                        List.of(dates(
                                List.of(dateRange(Month.DECEMBER, 25, Month.JANUARY, 5)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Feb",
                        List.of(dates(
                                List.of(dateRange(Month.FEBRUARY, 1, Month.FEBRUARY, 29)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Feb 29",
                        List.of(dates(
                                List.of(singleDate(Month.FEBRUARY, 29)), EnumSet.noneOf(DayOfWeek.class),
                                LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Jan-Feb,Jun-Jul",
                        List.of(dates(
                                List.of(monthRange(Month.JANUARY, Month.FEBRUARY), monthRange(Month.JUNE, Month.JULY)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Jan-Feb, Jun-Jul",
                        List.of(
                                dates(
                                        List.of(monthRange(Month.JANUARY, Month.FEBRUARY)), EnumSet.noneOf(DayOfWeek.class),
                                        LocalTime.MIDNIGHT, LocalTime.MIDNIGHT),
                                dates(
                                        List.of(monthRange(Month.JUNE, Month.JULY)), EnumSet.noneOf(DayOfWeek.class),
                                        LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 01-Jun 30,Sep 01-Oct 01",
                        List.of(dates(
                                List.of(
                                        dateRange(Month.APRIL, 1, Month.JUNE, 30),
                                        dateRange(Month.SEPTEMBER, 1, Month.OCTOBER, 1)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Jan,Apr 01-15",
                        List.of(dates(
                                List.of(
                                        monthRange(Month.JANUARY, Month.JANUARY),
                                        dateRange(Month.APRIL, 1, Month.APRIL, 15)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Apr 01-Jun 30 Mo-Fr 08:00-09:00,17:00-18:00; Dec 25-Jan 05 10:00-16:00",
                        List.of(
                                dates(
                                        List.of(dateRange(Month.APRIL, 1, Month.JUNE, 30)), weekdays(),
                                        LocalTime.of(8, 0), LocalTime.of(9, 0)),
                                dates(
                                        List.of(dateRange(Month.APRIL, 1, Month.JUNE, 30)), weekdays(),
                                        LocalTime.of(17, 0), LocalTime.of(18, 0)),
                                dates(
                                        List.of(dateRange(Month.DECEMBER, 25, Month.JANUARY, 5)),
                                        EnumSet.noneOf(DayOfWeek.class), LocalTime.of(10, 0), LocalTime.of(16, 0))))
        );
    }

    private static Stream<String> theoreticalUnsupportedOpeningHoursExpressions() {
        return Stream.of(
                "off",
                "Mo off",
                "\"comment\"",
                "2020 Mo-Fr 09:00-17:00",
                "week 01-10 Mo-Fr 09:00-17:00",
                "PH 09:00-17:00",
                "PH,Mo 09:00-17:00",
                "Mo[1] 09:00-17:00",
                "Mo[-1] 09:00-17:00",
                "Mo[1,2] 09:00-17:00",
                "Mo[1] +1 day 09:00-17:00",
                "09:00+",
                "09:00",
                "09:00-17:00+",
                "09:00-17:00/30",
                "09:00-17:00/01:30",
                "sunrise-17:00",
                "07:00-sunset",
                "(sunrise+01:00)-17:00",
                "07:00-(sunset-01:00)",
                "easter",
                "Apr 01-easter",
                "Apr 01-2020 Oct 01",
                "Apr-Dec,2020 Feb",
                "Apr 01 +1 day",
                "Apr 01 +Mo",
                "Feb 30",
                "\"note\": Mo 09:00-17:00",
                "off \"reason\"",
                "Mo 09:00-17:00 closed"
        );
    }

    private static Stream<Arguments> listOfUsedOpeningHoursExpressionsInTrafficSignApi() {
        // This is a list of all window times available in the traffic sign api to have a representable test.
        return Stream.of(
                Arguments.of(
                        "07:00-19:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(7, 0), LocalTime.of(19, 0)))),
                Arguments.of(
                        "11:00-23:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(11, 0), LocalTime.of(23, 0)))),
                Arguments.of(
                        "17:00-09:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(17, 0), LocalTime.of(9, 0)))),
                Arguments.of(
                        "18:00-02:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(18, 0), LocalTime.of(2, 0)))),
                Arguments.of(
                        "18:00-10:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(18, 0), LocalTime.of(10, 0)))),
                Arguments.of(
                        "18:00-11:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(18, 0), LocalTime.of(11, 0)))),
                Arguments.of(
                        "22:00-06:00",
                        List.of(window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(22, 0), LocalTime.of(6, 0)))),
                Arguments.of(
                        "Apr 01-Oct 01",
                        List.of(dates(
                                List.of(dateRange(Month.APRIL, 1, Month.OCTOBER, 1)),
                                EnumSet.noneOf(DayOfWeek.class), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Fr 06:00-21:00",
                        List.of(window(EnumSet.of(DayOfWeek.FRIDAY), LocalTime.of(6, 0), LocalTime.of(21, 0)))),
                Arguments.of(
                        "Fr,Sa 23:00-05:00",
                        List.of(window(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), LocalTime.of(23, 0), LocalTime.of(5, 0)))),
                Arguments.of(
                        "Mo,Tu,Th 08:00-16:00; We,Fr 08:00-13:00",
                        List.of(
                                window(
                                        EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                                        LocalTime.of(8, 0), LocalTime.of(16, 0)),
                                window(
                                        EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                                        LocalTime.of(8, 0), LocalTime.of(13, 0)))),
                Arguments.of(
                        "Mo-Fr 06:00-09:00, 15:00-19:00",
                        List.of(
                                window(weekdays(), LocalTime.of(6, 0), LocalTime.of(9, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(15, 0), LocalTime.of(19, 0)))),
                Arguments.of(
                        "Mo-Fr 06:00-09:00, 16:00-19:00",
                        List.of(
                                window(weekdays(), LocalTime.of(6, 0), LocalTime.of(9, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(16, 0), LocalTime.of(19, 0)))),
                Arguments.of(
                        "Mo-Fr 06:00-10:00, 15:00-19:00",
                        List.of(
                                window(weekdays(), LocalTime.of(6, 0), LocalTime.of(10, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(15, 0), LocalTime.of(19, 0)))),
                Arguments.of(
                        "Mo-Fr 07:00-09:00",
                        List.of(window(weekdays(), LocalTime.of(7, 0), LocalTime.of(9, 0)))),
                Arguments.of(
                        "Mo-Fr 07:00-09:00, 16:00-18:00",
                        List.of(
                                window(weekdays(), LocalTime.of(7, 0), LocalTime.of(9, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(16, 0), LocalTime.of(18, 0)))),
                Arguments.of(
                        "Mo-Fr 07:00-09:00, 16:00-19:00",
                        List.of(
                                window(weekdays(), LocalTime.of(7, 0), LocalTime.of(9, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(16, 0), LocalTime.of(19, 0)))),
                Arguments.of(
                        "Mo-Fr 07:30-09:00, 15:00-18:00",
                        List.of(
                                window(weekdays(), LocalTime.of(7, 30), LocalTime.of(9, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(15, 0), LocalTime.of(18, 0)))),
                Arguments.of(
                        "Mo-Fr 08:00-08:45; Mo-Fr 13:45-14:30",
                        List.of(
                                window(weekdays(), LocalTime.of(8, 0), LocalTime.of(8, 45)),
                                window(weekdays(), LocalTime.of(13, 45), LocalTime.of(14, 30)))),
                Arguments.of(
                        "Mo-Fr 08:00-09:00, 13:30-14:30",
                        List.of(
                                window(weekdays(), LocalTime.of(8, 0), LocalTime.of(9, 0)),
                                window(EnumSet.noneOf(DayOfWeek.class), LocalTime.of(13, 30), LocalTime.of(14, 30)))),
                Arguments.of(
                        "Mo-Sa 08:00-18:00, Fr 18:00-21:00",
                        List.of(
                                window(
                                        EnumSet.of(
                                                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                                                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), LocalTime.of(8, 0), LocalTime.of(18, 0)),
                                window(EnumSet.of(DayOfWeek.FRIDAY), LocalTime.of(18, 0), LocalTime.of(21, 0)))),
                Arguments.of(
                        "Mo-Sa 08:00-18:00; Fr 18:00-21:00",
                        List.of(
                                window(
                                        EnumSet.of(
                                                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                                                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), LocalTime.of(8, 0), LocalTime.of(18, 0)),
                                window(EnumSet.of(DayOfWeek.FRIDAY), LocalTime.of(18, 0), LocalTime.of(21, 0)))),
                Arguments.of(
                        "Mo-We,Fr-Su 07:00-20:00; Th 07:00-22:00",
                        List.of(
                                window(
                                        EnumSet.of(
                                                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY,
                                                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), LocalTime.of(7, 0), LocalTime.of(20, 0)),
                                window(EnumSet.of(DayOfWeek.THURSDAY), LocalTime.of(7, 0), LocalTime.of(22, 0)))),
                Arguments.of(
                        "Sa 02:00-19:30",
                        List.of(window(EnumSet.of(DayOfWeek.SATURDAY), LocalTime.of(2, 0), LocalTime.of(19, 30)))),
                Arguments.of(
                        "Sa, Su",
                        List.of(
                                window(EnumSet.of(DayOfWeek.SATURDAY), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT),
                                window(EnumSet.of(DayOfWeek.SUNDAY), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Su 00:00-24:00",
                        List.of(window(EnumSet.of(DayOfWeek.SUNDAY), LocalTime.MIDNIGHT, LocalTime.MIDNIGHT))),
                Arguments.of(
                        "Th 08:00-14:00; Sa 10:00-17:00",
                        List.of(
                                window(EnumSet.of(DayOfWeek.THURSDAY), LocalTime.of(8, 0), LocalTime.of(14, 0)),
                                window(EnumSet.of(DayOfWeek.SATURDAY), LocalTime.of(10, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "Th 08:00-17:00; Sa 10:00-17:00",
                        List.of(
                                window(EnumSet.of(DayOfWeek.THURSDAY), LocalTime.of(8, 0), LocalTime.of(17, 0)),
                                window(EnumSet.of(DayOfWeek.SATURDAY), LocalTime.of(10, 0), LocalTime.of(17, 0)))),
                Arguments.of(
                        "We,Sa 00:00-19:00",
                        List.of(window(
                                EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                                LocalTime.MIDNIGHT, LocalTime.of(19, 0)))),
                Arguments.of(
                        "We,Su 00:00-19:00",
                        List.of(window(
                                EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY),
                                LocalTime.MIDNIGHT, LocalTime.of(19, 0))))
        );
    }

    private static Stream<String> listOfUnsupportedOpeningHoursExpressionsInTrafficSignApi() {
        return Stream.of(
                "",
                "sunset-sunrise",
                "06:00-11:30\nMo-We,Fr-Su 11:30-12:00; Mo-We,Fr-Su 19:00-21:00\nTh 21:00-23:00",
                "11:30-12:00\nMo-We,Fr-Su 19:00-21:00\nTh 21:00-23:00",
                "Mo,Tu,Th,Fr 08:15-08:45, 14:15-14:45\nWe 08:15-08:45, 12:15-12:45");
    }

    private static EnumSet<DayOfWeek> weekdays() {
        return EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    }

    private static Window window(EnumSet<DayOfWeek> days, LocalTime start, LocalTime end) {
        return new Window(List.of(), days, start, end);
    }

    private static Window dates(List<MonthDayRange> dateRanges, EnumSet<DayOfWeek> days, LocalTime start, LocalTime end) {
        return new Window(dateRanges, days, start, end);
    }

    private static MonthDayRange monthRange(Month from, Month to) {
        return new MonthDayRange(MonthDay.of(from, 1), MonthDay.of(to, to.maxLength()));
    }

    private static MonthDayRange dateRange(Month fromMonth, int fromDayOfMonth, Month toMonth, int toDayOfMonth) {
        return new MonthDayRange(MonthDay.of(fromMonth, fromDayOfMonth), MonthDay.of(toMonth, toDayOfMonth));
    }

    private static MonthDayRange singleDate(Month month, int dayOfMonth) {
        return new MonthDayRange(MonthDay.of(month, dayOfMonth), MonthDay.of(month, dayOfMonth));
    }

    private void assertParseFailureIsLogged(String openingHoursExpression) {
        loggerExtension.containsLog(
                Level.WARN,
                "Could not parse opening hours expression '%s'".formatted(openingHoursExpression));
    }
}

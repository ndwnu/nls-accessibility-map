// See specs here: https://wiki.openstreetmap.org/wiki/Key:opening_hours/specification
grammar OpeningHours;

openingHours: rule_sequence ( any_rule_separator rule_sequence )* EOF ;

rule_sequence: (selector_sequence (SPACE rule_modifier)?)
             | rule_modifier
             ;

any_rule_separator: ';' SPACE
                  | ',' SPACE
                  | SPACE '||' SPACE
                  ;

rule_modifier: ('open'|'closed'|'off'|'unknown') ( SPACE COMMENT)?
             | COMMENT
             ;

selector_sequence: '24/7'
                 | wide_range_selector (SPACE small_range_selector)?
                 | small_range_selector
                 ;

wide_range_selector: (year_selector (SPACE monthday_selector)? (SPACE week_selector)? (':')?)
                   | (monthday_selector (SPACE week_selector)? (':')?)
                   | (week_selector (':')?)
                   | COMMENT ':'
                   ;

small_range_selector: weekday_selector ( SPACE time_selector)?
                    | time_selector
                    ;

year_selector: year_range (',' year_range)* ;
year_range: year
          | year  '-' year ('/' number )?
          | year '+'
          ;
year: DIGIT DIGIT DIGIT DIGIT ;

monthday_selector: monthday_range (',' monthday_range)* ;
monthday_range: (year SPACE)? month ( '-' month )?
              | date_from date_offset '+'?
              | date_from date_offset ( '-' date_to (SPACE date_offset)?)?
              ;

date_to: date_from
       | month_day
       ;

date_from: (year SPACE)? month SPACE month_day
         | variable_date
         ;

month_day: DIGIT | DIGIT DIGIT ;

date_offset: (SPACE plus_minus wday)? (day_offset)? ;

variable_date: 'easter' ;

month: 'Jan' | 'Feb' | 'Mar' | 'Apr' | 'May' | 'Jun' | 'Jul' | 'Aug' | 'Sep' | 'Oct' | 'Nov' | 'Dec' ;

week_selector : 'week' SPACE week (',' week)* ;
week: weeknum (( '-' weeknum ) ('/' number )?)? ;
weeknum: DIGIT DIGIT;

weekday_selector: weekday_sequence
                | holiday_sequence
                | holiday_sequence (','|SPACE) weekday_sequence
                | weekday_sequence ',' holiday_sequence
                ;
weekday_sequence: weekday_range (',' weekday_range)*;

weekday_range: wday ( '-' wday )?
             | wday '[' nth_entry (',' nth_entry)? ']' (day_offset)?
             ;

wday: 'Mo' | 'Tu' | 'We' | 'Th' | 'Fr' | 'Sa' | 'Su' ;
holiday_sequence: holiday (',' holiday)*;
holiday: ('PH' day_offset?)
         | 'SH'
         ;
day_offset: SPACE ('+'|'-') number SPACE 'days'
          | SPACE ('+'|'-') number SPACE 'day'
          ;

nth_entry: DIGIT ('-' DIGIT)? | '-' DIGIT ;

time_selector: timespan (',' timespan)* ;
timespan: time
        | (time '+')
        | (time '-' extended_time '+')
        | (time '-' extended_time ('/' (number | number ':' minute))?)
        ;
time: hour_minutes
    | variable_time
    ;
variable_time: event
             | '(' event plus_minus hour_minutes ')'
             ;
event: 'sunrise' | 'sunset' | 'dawn' | 'dusk' ;

plus_minus: '+' | '-' ;

hour_minutes: hour ':' minute ;

extended_time: (extended_hour ':' minute)
             | variable_time
             ;

hour: DIGIT DIGIT ;
minute: DIGIT DIGIT ;
extended_hour: DIGIT DIGIT ;
number: DIGIT+ ;

SPACE: ' ' ;
DIGIT : [0-9] ;
COMMENT:  '"' ~('"')+ '"' ;

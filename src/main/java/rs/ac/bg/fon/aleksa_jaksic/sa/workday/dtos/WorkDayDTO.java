package rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos;


import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkDayDTO(
    Long id,

    LocalTime openTime,

    LocalTime closeTime,

    DayOfWeek day
){}

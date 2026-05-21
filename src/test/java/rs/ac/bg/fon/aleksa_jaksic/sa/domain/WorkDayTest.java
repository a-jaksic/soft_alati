package rs.ac.bg.fon.aleksa_jaksic.sa.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain.WorkDay;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkDayTest {

    private Validator validator;
    private Restaurant mockRestaurant;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        mockRestaurant = Mockito.mock(Restaurant.class);
    }

    @Test
    @DisplayName("Should pass validation with perfectly valid schedule parameters")
    void validate_ValidWorkDay_NoViolations() {
        WorkDay workDay = WorkDay.builder()
                .id(1L)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(22, 0))
                .day(DayOfWeek.MONDAY)
                .restaurant(mockRestaurant)
                .build();

        Set<ConstraintViolation<WorkDay>> violations = validator.validate(workDay);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek.class)
    @DisplayName("Should pass validation across all seven explicit calendar days")
    void validate_AllDaysOfWeek_NoViolations(DayOfWeek validDay) {
        WorkDay workDay = WorkDay.builder()
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(17, 0))
                .day(validDay)
                .restaurant(mockRestaurant)
                .build();

        Set<ConstraintViolation<WorkDay>> violations = validator.validate(workDay);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "true,  08:00, 23:00",
            "true,  12:00, 16:00",
            "false, 17:00, 02:00",
            "false, 10:00, 10:00",
            "false, 21:00, 09:00",
    })
    @DisplayName("Should verify operational business rules catch faulty or non-chronological scheduling spans")
    void isValidTimeWindow_TimeMatrices_EvaluatesExpectedBoundaries(
            boolean expectedValidity, String openStr, String closeStr) {

        WorkDay workDay = WorkDay.builder()
                .openTime(LocalTime.parse(openStr))
                .closeTime(LocalTime.parse(closeStr))
                .build();

        assertEquals(expectedValidity, workDay.isValidTimeWindow());
    }

    @Test
    @DisplayName("Should fail validation when data elements are missing")
    void validate_NullProperties_HasViolations() {
        WorkDay workDay = WorkDay.builder()
                .openTime(null)
                .closeTime(null)
                .day(null)
                .restaurant(null)
                .build();

        Set<ConstraintViolation<WorkDay>> violations = validator.validate(workDay);
        assertFalse(violations.isEmpty());

        assertEquals(4, violations.size());
    }

    @Test
    @DisplayName("Should return false for time window validation when time parameters are null")
    void isValidTimeWindow_NullTimes_ReturnsFalse() {
        WorkDay workDayNullOpen = WorkDay.builder().openTime(null).closeTime(LocalTime.NOON).build();
        WorkDay workDayNullClose = WorkDay.builder().openTime(LocalTime.NOON).closeTime(null).build();

        assertFalse(workDayNullOpen.isValidTimeWindow());
        assertFalse(workDayNullClose.isValidTimeWindow());
    }

    @Test
    @DisplayName("Should confirm Lombok setters, getters, and constructors work correctly")
    void testLombokAndAssociations() {
        LocalTime open = LocalTime.of(7, 30);
        LocalTime close = LocalTime.of(15, 45);

        WorkDay workDay = new WorkDay();
        workDay.setId(14L);
        workDay.setOpenTime(open);
        workDay.setCloseTime(close);
        workDay.setDay(DayOfWeek.FRIDAY);
        workDay.setRestaurant(mockRestaurant);

        assertEquals(14L, workDay.getId());
        assertEquals(open, workDay.getOpenTime());
        assertEquals(close, workDay.getCloseTime());
        assertEquals(DayOfWeek.FRIDAY, workDay.getDay());
        assertEquals(mockRestaurant, workDay.getRestaurant());
    }
}
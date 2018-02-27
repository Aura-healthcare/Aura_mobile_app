package com.wearablesensor.aura.real_time_data_processor.analyser;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;


/**
 * Created by octo_tbr on 23/02/18.
 */
public class TimeSerieAnalyserTest {

    private TimeSerieAnalyser<Double> analyser;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static final int observationWindow = 3;

    @Before
    public void setUp(){
        analyser = TimeSerieAnalyser.<Double>builder()
        .maxValue(10d)
        .minValue(-10d)
        .observationWindow(observationWindow)
        .build();
    }

    @Test
    public void append_shouldAddObservation() throws Exception {
        // Given
        int initialCount = analyser.observationCount();

        // When
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 1d);
        int newCount = analyser.observationCount();

        // Then
        assertThat(newCount, is(initialCount+1));
    }

    @Test
    public void append_shouldPopOldestObservationWhenTimeWindowIsFull() throws Exception {
        // Given
        int initialCount = analyser.observationCount();

        // When
        analyser.append(LocalDate.parse("2018-02-02T01:58:40.013", formatter), 2d);
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 1d);
        analyser.append(LocalDate.parse("2018-02-02T02:00:00.013", formatter), 5d);
        analyser.append(LocalDate.parse("2018-02-02T02:00:10.013", formatter), 3d);
        int newCount = analyser.observationCount();

        // Then
        assertThat(newCount, is(observationWindow));
        assertThat(analyser.observations(), not(contains(2d)));
    }

    @Test
    public void isValid_shouldBeTrueIfTimeWindowIsNotCompltete() throws Exception {
        // Given
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 1d);

        // When
        boolean isValid = analyser.isValid();

        // Then
        assertThat("Should be valid", isValid);
    }

    @Test
    public void isValid_shouldBeTrueIfAllValuesAreInDefinedRange() throws Exception {
        // Given
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 1d);
        analyser.append(LocalDate.parse("2018-02-02T02:00:00.013", formatter), 5d);
        analyser.append(LocalDate.parse("2018-02-02T02:00:10.013", formatter), 3d);

        // When
        boolean isValid = analyser.isValid();

        // Then
        assertThat("Should be valid", isValid);
    }

    @Test
    public void isValid_shouldBeFalseIfValuesAreOutOfDefinedRange() throws Exception {
        // Given
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 1d);
        analyser.append(LocalDate.parse("2018-02-02T02:00:00.013", formatter), 42d);
        analyser.append(LocalDate.parse("2018-02-02T02:00:10.013", formatter), 3d);

        // When
        boolean isValid = analyser.isValid();

        // Then
        assertFalse("Series with out of range observation should not be valid", isValid);
    }

    @Test
    public void isValid_shouldBeTrueIfIrregularityPopsOutOfTimeWindow() throws Exception {
        // Given
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 1d);
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 42d);
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 3d);
        boolean initialValidity = analyser.isValid();


        // When
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), 7d);
        analyser.append(LocalDate.parse("2018-02-02T01:58:50.013", formatter), -2d);
        boolean newValidity = analyser.isValid();

        // Then
        assertFalse(initialValidity);
        assertThat("Analyser should be valid if out of range observations are out of time window", newValidity);
    }

}
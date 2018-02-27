package com.wearablesensor.aura.real_time_data_processor.analyser;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by octo_tbr on 23/02/18.
 */
public class RrIntervalAnalyserTest {

    @Test
    public void isValid() throws Exception {
        // Given
        TimeSerieAnalyser<Integer> analyser = TimeSerieAnalyser.<Integer>builder()
                .build();
        RRIntervalModel sig1 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:59.013", 0);

        // When
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        analyser.append(LocalDate.parse(sig1.getTimestamp(), formatter), sig1.getRrInterval());

        // Then
        analyser.isValid();
        assertThat("Signal is valid", analyser.isValid());
    }

}
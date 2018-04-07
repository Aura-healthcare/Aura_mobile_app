package com.wearablesensor.aura.real_time_data_processor.analyser;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.real_time_data_processor.MetricType;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
/**
 * Created by octo_tbr on 23/02/18.
 */
public class RrIntervalAnalyserTest {

    @Test
    public void isValidIfObservationWindowNotFull() throws Exception {
        // Given
        TimeSerieAnalyserObserver observer = mock(TimeSerieAnalyserObserver.class);
        TimeSerieAnalyser<Integer> analyser = TimeSerieAnalyser.<Integer>builder()
                .observationWindow(2)
                .metricType(MetricType.HEART_BEAT)
                .build();
        analyser.addObserver(observer);
        RRIntervalModel sig1 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:59.013", 0);

        // When
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        analyser.append(sig1.getRrInterval());

        // Then
        assertThat("Signal is valid", analyser.isValid());
    }

    @Test
    public void isValidNotify() throws Exception {
        // Given
        TimeSerieAnalyserObserver observer = mock(TimeSerieAnalyserObserver.class);
        TimeSerieAnalyser<Integer> analyser = TimeSerieAnalyser.<Integer>builder()
                .observationWindow(3)
                .metricType(MetricType.HEART_BEAT)
                .build();
        analyser.addObserver(observer);
        RRIntervalModel sig1 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:00.013", 700);
        RRIntervalModel sig2 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:01.013", 1000);
        analyser.append(sig1.getRrInterval());
        analyser.append(sig2.getRrInterval());
        verify(observer, never()).onNewState(Matchers.<MetricType>any(), Matchers.<TimeSerieState>any()); // no notification if window not full

        // When - observation window is full and valid
        RRIntervalModel sig3 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:02.013", 900);
        analyser.append(sig3.getRrInterval());


        // Then
        assertThat("Signal is valid", analyser.isValid());
        verify(observer).onNewState(Mockito.eq(MetricType.HEART_BEAT), Mockito.eq(TimeSerieState.NORMAL));
    }

    @Test
    public void notifyIfInvalidValues() throws Exception {
        // Given
        TimeSerieAnalyserObserver observer = mock(TimeSerieAnalyserObserver.class);
        TimeSerieAnalyser<Integer> analyser = TimeSerieAnalyser.<Integer>builder()
                .observationWindow(3)
                .metricType(MetricType.HEART_BEAT)
                .maxValue(1500) // 40 bpm
                .minValue(300) // 200 bpm
                .build();
        analyser.addObserver(observer);
        RRIntervalModel sig1 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:59.013", 700);
        RRIntervalModel sig2 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:59.013", 0);
        RRIntervalModel sig3 = new RRIntervalModel("01:02:03:04:05:06", "2018-02-02T01:59:59.013", 800);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        analyser.append(sig1.getRrInterval());
        analyser.append(sig2.getRrInterval());
        verify(observer, never()).onNewState(Matchers.<MetricType>any(), Matchers.<TimeSerieState>any()); // no notification if window not full

        // When
        analyser.append(sig3.getRrInterval());

        // Then
        assertFalse("Signal is not valid", analyser.isValid());
        verify(observer).onNewState(Mockito.eq(MetricType.HEART_BEAT), Mockito.eq(TimeSerieState.ANOMALY));
    }

}
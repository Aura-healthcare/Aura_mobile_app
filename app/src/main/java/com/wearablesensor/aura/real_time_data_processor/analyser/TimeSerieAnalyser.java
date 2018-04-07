/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.real_time_data_processor.analyser;

import com.wearablesensor.aura.real_time_data_processor.MetricType;
import com.wearablesensor.aura.utils.TimeSerie;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.Builder;

public class TimeSerieAnalyser<T extends Comparable<T>> implements SimpleTimeSerieAnalyser<T>, TimeSerie {

    private T maxValue = null;
    private T minValue = null;

    private LinkedList<T> serie = new LinkedList<>();

    private int observationWindow = 50;

    private boolean isValid = true;

    private MetricType metricType;
    private TimeSerieState previousState;
    private List<TimeSerieAnalyserObserver> observers = new ArrayList<>();

    @Builder
    public TimeSerieAnalyser(T maxValue, T minValue, int observationWindow, MetricType metricType){
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.observationWindow = observationWindow;
        this.metricType = metricType;
    }

    @Override
    public void append(T observation) {
        serie.addLast(observation);
        while(serie.size()>observationWindow){
            serie.removeFirst();
        }
        isValid = checkValidity();
        if(serie.size()<observationWindow){
            return;
        }
        if(isValid){
            checkState(TimeSerieState.NORMAL);
        } else {
            checkState(TimeSerieState.ANOMALY);
        }
    }

    private void checkState(TimeSerieState state){
        if(state == previousState){
            return;
        }
        previousState = state;
        notifyObservers(state);
    }

    private void notifyObservers(TimeSerieState state) {
        if(serie.size()<observationWindow)
            return;
        for(TimeSerieAnalyserObserver obs : observers){
            obs.onNewState(metricType, state);
        }
    }

    private boolean checkValidity() {
        if(serie.size()<this.observationWindow){
            return true;
        }
        for(T item : serie){
            if(isOutOfBounds(item)){
                return false;
            }
        }
        return true;
    }

    private boolean isOutOfBounds(T item){
        if(minValue != null && item.compareTo(minValue) < 0){
            return true;
        }
        if(maxValue != null && item.compareTo(maxValue) > 0){
            return true;
        }
        return false;
    }

    @Override
    public Iterable<T> observations() {
        return serie;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void addObserver(TimeSerieAnalyserObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(TimeSerieAnalyserObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void clearObservers() {
        observers.clear();
    }

    @Override
    public int observationCount() {
        return serie.size();
    }

    @Override
    public double mean() {
        return 0;
    }

    @Override
    public double median() {
        return 0;
    }

    @Override
    public double variance() {
        return 0;
    }

    @Override
    public double standardDeviation() {
        return 0;
    }


}

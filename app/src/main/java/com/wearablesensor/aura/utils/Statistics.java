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

package com.wearablesensor.aura.utils;

import java.util.Arrays;

public final class Statistics {

    private Statistics() {}

    public static double sumOf(final double[] data) {
        double sum = 0.0;
        for (double element : data) {
            sum += element;
        }
        return sum;
    }

    public static double meanOf(final double[] data) {
        final double sum = sumOf(data);
        return sum / data.length;
    }

    public static double varianceOf(final double[] data) {
        final int n = data.length;
        return sumOfSquaredDifferences(data, meanOf(data)) / (n - 1);
    }

    public static double stdDeviationOf(final double[] data) {
        return Math.sqrt(varianceOf(data));
    }

    public static double sumOfSquared(final double[] data) {
        return sumOf(squared(data));
    }

    static double sumOfSquaredDifferences(final double[] data, final double point) {
        return sumOf(squared(differences(data, point)));
    }

    static double[] squared(final double[] data) {
        final double[] squared = new double[data.length];
        for (int i = 0; i < squared.length; i++) {
            squared[i] = data[i] * data[i];
        }
        return squared;
    }

    static double[] differences(final double[] data, final double point) {
        final double[] differenced = new double[data.length];
        for (int i = 0; i < differenced.length; i++) {
            differenced[i] = data[i] - point;
        }
        return differenced;
    }

    // Arrays.sort uses quicksort algorithm as of Java 8.
    public static double medianOf(final double[] data) {
        double[] sorted = data.clone();
        Arrays.sort(sorted);
        if (sorted.length % 2 == 0) {
            return (sorted[(sorted.length / 2) - 1] + sorted[(sorted.length / 2)]) / 2.0;
        } else {
            return sorted[(sorted.length - 1) / 2];
        }
    }
}

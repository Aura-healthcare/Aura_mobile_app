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


/**
 * A collection of numerical observations.
 *
 * @author Jacob Rachiele
 *         Mar. 29, 2017
 */
public interface TimeSerie {

    /**
     * The count of the observations.
     *
     * @return the count of the observations.
     */
    int observationCount();

    /**
     * The mean of the observations.
     *
     * @return the mean of the observations.
     */
    double mean();

    /**
     * The median value of the observations.
     *
     * @return the median value of the observations.
     */
    double median();

    /**
     * The unbiased sample variance of the observations.
     *
     * @return the unbiased sample variance of the observations.
     */
    double variance();

    /**
     * The unbiased sample standard deviation of the observations.
     *
     * @return the unbiased sample standard deviation of the observations.
     */
    double standardDeviation();


}

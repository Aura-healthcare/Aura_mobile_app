/**
 * @file
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 *
 * DateIso8601Mapper,java is an helper class that allow to convert a String under iso 8601 format
 * to a Date java object and vice-versa
 */

package com.wearablesensor.aura.data_repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateIso8601Mapper {
    private static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"; /** iso 8601 format */

    /**
     * @brief convert a date string under iso8601 format to java Date object
     *
     * @param iDateString date string under iso8601 format
     * @return if succeed to parse iDateString returns corresponding java Date object, otherwise null
     */

    public static Date getDate(String iDateString){
        SimpleDateFormat lDatePattern = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT);
        try {
            return lDatePattern.parse(iDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @brief convert a java Date object to String under iso8601 format
     *
     * @param iDate date to be converted
     * @return if succeed returns corresponding java string under 8601 format, otherwise empty string
     */
    public static String getString(Date iDate){
        SimpleDateFormat lDatePattern = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT);
        if(iDate != null) {
            return lDatePattern.format(iDate);
        }
        else{
            return new String();
        }
    }
}

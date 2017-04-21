package com.wearablesensor.aura.data_repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lecoucl on 08/01/17.
 */

public class DateIso8601Mapper {
    private static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static Date getDate(String iDateString){
        SimpleDateFormat lDatePattern = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT);
        try {
            return lDatePattern.parse(iDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

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

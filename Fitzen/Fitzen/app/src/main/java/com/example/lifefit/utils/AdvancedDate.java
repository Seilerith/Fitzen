package com.example.lifefit.utils;

import androidx.annotation.NonNull;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class AdvancedDate {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static String serialize(Date date) {
        return sdf.format(date);
    }

    public static Date parse(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(date);
        } catch (ParseException e) {
//            e.printStackTrace();
            return null;
        }
    }
}

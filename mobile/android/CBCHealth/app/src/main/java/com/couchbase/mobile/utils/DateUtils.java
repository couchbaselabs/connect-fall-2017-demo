package com.couchbase.mobile.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    private static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String toJson(Date date) {
        return sdf.format(date);
    }

    public static Date fromJson(String json) {
        if (json == null) return null;

        try {
            return sdf.parse(json);
        } catch (ParseException ex) {
            return null;
        }
    }
}

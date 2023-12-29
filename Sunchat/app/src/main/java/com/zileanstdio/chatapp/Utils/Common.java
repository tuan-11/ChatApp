package com.zileanstdio.chatapp.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.zileanstdio.chatapp.R;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Common {

    public static String getReadableTime(Date date) {
        long timeDiff = Math.abs(date.getTime() - new Date().getTime());
        long hoursDiff = TimeUnit.HOURS.convert(timeDiff, TimeUnit.MILLISECONDS);
        if(hoursDiff < 24) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        }
        else {
            return new SimpleDateFormat("d MMM", Locale.getDefault()).format(date);
        }
    }

    public static String computeCallTime(Date start, Date end, Context context) {

        String time = "undefined";
        if(start == null || end == null) {
            time = "0 " + context.getString(R.string.time_unit_second);
        } else {
            long timeDiff = Math.abs(start.getTime() - end.getTime());
            long secondsDiff = TimeUnit.SECONDS.convert(timeDiff, TimeUnit.MILLISECONDS);
            if(secondsDiff >= 3600) {
                time = String.format("%s %s", Math.round((float)secondsDiff/3600), context.getString(R.string.time_unit_hour));
            } else if(secondsDiff > 60) {
                time = String.format("%s %s", Math.round((float) secondsDiff / 60), context.getString(R.string.time_unit_minute));
            } else if(secondsDiff <= 60) {
                time = secondsDiff != 60 ? secondsDiff + " " + context.getString(R.string.time_unit_second) : "1 " + context.getString(R.string.time_unit_minute);
            } else {

            }
        }


        return time;
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toUpperCase(Locale.ROOT);
    }

    public static void postDelay(Runnable runnable, long delayMillis) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, delayMillis);
    }
}
package com.example.blogapp;

import android.util.Log;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {
    private long currentTime = new Date().getTime();

    public String getTime(long time){

        long days = TimeUnit.MILLISECONDS.toDays(currentTime - time);
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime - time);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - time);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - time);

        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String simpleDate = dateFormat.format(date);

        String hhmmss = String.format("%02d:%02d:%02d",hours,minutes,seconds);
        Log.i("timeHHMMSS",hhmmss);

        if (seconds < 60){
            return "just now";
        }

        if (minutes == 1){
            return "1 minute ago";
        }

        if (minutes < 60){
            return minutes + " minute ago";
        }

        if (hours == 1){
            return "1 hour ago";
        }

        if (hours < 24){
            return hours + " hours ago";
        }

        if (days == 1){
            return "1 day ago";
        }

        if (days <= 15){
            return days + " days ago";
        }


        return simpleDate;
    }
}

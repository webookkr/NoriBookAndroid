package com.bookapp.noribook;

import android.app.Application;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("yyyy/MM/dd", calendar).toString();

        return date;
    }

}

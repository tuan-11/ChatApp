package com.zileanstdio.chatapp.Utils;

import android.util.Log;

public class Debug {
    public static void log(String msg) {
        Log.d("DEBUG", msg);
    }
    public static void log(String tag, String msg) {
        Log.d("DEBUG:" + tag, msg);
    }

}

package com.zileanstdio.chatapp.Utils;

import android.annotation.SuppressLint;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("StaticFieldLeak")
public class Stringee {

    public static String token;
    public static boolean isInCall = false;
    public static StringeeClient client;
    public static Map<String, StringeeCall> callsMap = new HashMap<>();
}
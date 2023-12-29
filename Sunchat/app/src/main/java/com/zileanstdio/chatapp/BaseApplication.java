package com.zileanstdio.chatapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


import com.zileanstdio.chatapp.DI.DaggerAppComponent;
import com.zileanstdio.chatapp.Ui.main.MainActivity;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

public class BaseApplication extends DaggerApplication {
    public String getTag() {
        return this.getClass().getSimpleName();
    }

    @SuppressLint("StaticFieldLeak")
    private static BaseApplication instance = null;

    private static synchronized void setInstance(BaseApplication instance) {
        BaseApplication.instance = instance;
    }

    public static BaseApplication getInstance() { return instance; }

    Context activityContext;

    private static SharedPreferences sharedPreferences = null;

    private static synchronized void setSharedPreferences(SharedPreferences sharedPreferences) {
        BaseApplication.sharedPreferences = sharedPreferences;
    }

    public Context getBaseApplicationContext() {
        return instance;
    }

    public void setActivityContext(Context context) {
        if(this.activityContext != context) {
            this.activityContext = context;
        }
    }

    public Context getActivityContext() {
        return activityContext;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();

    }
    
}

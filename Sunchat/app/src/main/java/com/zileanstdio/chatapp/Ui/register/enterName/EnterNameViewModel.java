package com.zileanstdio.chatapp.Ui.register.enterName;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

public class EnterNameViewModel extends ViewModel {
    private static final String TAG = "EnterNameViewModel";

    @Inject
    public EnterNameViewModel() {
        Log.d(TAG, "EnterNameViewModel:onConstructor");
    }
}

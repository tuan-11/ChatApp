package com.zileanstdio.chatapp.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StateResource<T> {
    @NonNull
    public final RegisterStatus status;

    @Nullable
    public final String message;

    @Nullable
    public final Object data;


    public StateResource(@NonNull RegisterStatus status, @Nullable String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public StateResource(@NonNull RegisterStatus status, @Nullable Object data) {
        this.status = status;
        this.message = null;
        this.data = data;
    }

    public static <T> StateResource<T> success () {
        return new StateResource<>(RegisterStatus.SUCCESS, null);
    }

    public static <T> StateResource<T> success (Object data) {
        return new StateResource<>(RegisterStatus.SUCCESS, data);
    }

    public static <T> StateResource<T> error(@NonNull String msg) {

        return new StateResource<>(RegisterStatus.ERROR, msg);
    }

    public static <T> StateResource<T> loading() {
        return new StateResource<>(RegisterStatus.LOADING, null);
    }

    public enum RegisterStatus {
        SUCCESS,
        ERROR,
        LOADING
    }
}

package com.zileanstdio.chatapp.Ui.login;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.zileanstdio.chatapp.BaseApplication;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.AuthRepository;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.StateResource;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends ViewModel {

    public static final String TAG = "LoginViewModel";
    private final AuthRepository authRepository;
    private final MediatorLiveData<StateResource> onLogin = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    public void login(String phoneNumber, String password) {
        authRepository.login(phoneNumber, password).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onLogin.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onLogin.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("LoginViewModel:login:onError", e.getMessage());
                        String message = "";
                        if (e.getMessage() != null) {
                            message = e.getMessage();
                        }
                        onLogin.setValue(StateResource.error(message));
                    }
                });
    }

    public LiveData<StateResource> observeLogin() {
        return onLogin;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
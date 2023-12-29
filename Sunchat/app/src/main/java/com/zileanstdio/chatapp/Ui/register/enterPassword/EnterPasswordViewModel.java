package com.zileanstdio.chatapp.Ui.register.enterPassword;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.zileanstdio.chatapp.BaseApplication;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.AuthRepository;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.StateResource;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EnterPasswordViewModel extends ViewModel {
    private static final String TAG = "EnterPasswordViewModel";
    private final AuthRepository authRepository;
    private final MediatorLiveData<StateResource> onCreateWithEmailPasswordAuthCredential = new MediatorLiveData<>();
    private final MediatorLiveData<StateResource> onLinkWithPhoneAuthProvider = new MediatorLiveData<>();
    private final MediatorLiveData<StateResource> onUpdateRegisterInfo = new MediatorLiveData<>();
    private final MediatorLiveData<StateResource> onLoginEvent = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public EnterPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void createWithEmailPasswordAuthCredential(String email, String password) {
        String emailFormat = String.format("%s@gmail.com", email);
        authRepository.createWithEmailPasswordAuthCredential(emailFormat, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FirebaseUser>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onCreateWithEmailPasswordAuthCredential.setValue(StateResource.loading());
                    }

                    @Override
                    public void onNext(@NonNull FirebaseUser firebaseUser) {
                        onCreateWithEmailPasswordAuthCredential.setValue(StateResource.success(firebaseUser));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if(e instanceof FirebaseAuthWeakPasswordException) {
                            String weakPasswordExceptionMessage = BaseApplication.getInstance().getString(R.string.weak_password_exception_message);
                            Debug.log(TAG + ":signInWithEmailPasswordAuthCredential", weakPasswordExceptionMessage);
                            onCreateWithEmailPasswordAuthCredential.setValue(StateResource.error(weakPasswordExceptionMessage));
                        } else {
                            String exceptionMessage = BaseApplication.getInstance().getString(R.string.has_error);
                            Debug.log(TAG + ":signInWithEmailPasswordAuthCredential", exceptionMessage);
                            onCreateWithEmailPasswordAuthCredential.setValue(StateResource.error(exceptionMessage));
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void updateRegisterInfo(User user) {
        authRepository.updateRegisterInfo(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onUpdateRegisterInfo.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onUpdateRegisterInfo.setValue(StateResource.success());

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        String exceptionMessage = BaseApplication.getInstance().getString(R.string.has_error);
                        Debug.log(TAG + ":updateRegisterInfo", exceptionMessage);
                        onUpdateRegisterInfo.setValue(StateResource.error(exceptionMessage));
                    }
                });
    }

    public void login(String phoneNumber, String password) {
        authRepository.login(phoneNumber, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onLoginEvent.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onLoginEvent.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log(TAG + ":login", e.getMessage());
                        if(e instanceof FirebaseAuthInvalidUserException | e instanceof FirebaseAuthInvalidCredentialsException) {
                            onLoginEvent.setValue(StateResource.error("Thông tin đăng nhập không khả dụng"));
                        } else {
                            String exceptionMessage = BaseApplication.getInstance().getString(R.string.has_error);
                            onLoginEvent.setValue(StateResource.error(exceptionMessage));
                        }
                    }
                });
    }


    public LiveData<StateResource> observeCreateWithEmailPassword() {
        return onCreateWithEmailPasswordAuthCredential;
    }

    public LiveData<StateResource> observeLogin() { return observeLogin(); }

    public LiveData<StateResource> observeUpdateRegisterInfo() {
        return onUpdateRegisterInfo;
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}

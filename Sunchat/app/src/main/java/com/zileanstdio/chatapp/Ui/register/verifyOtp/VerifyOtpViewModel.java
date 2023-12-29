package com.zileanstdio.chatapp.Ui.register.verifyOtp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.PhoneAuthCredential;
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

public class VerifyOtpViewModel extends ViewModel {
    public static final String TAG = "VerifyOtpViewModel";
    private final AuthRepository authRepository;
    private final MediatorLiveData<StateResource> onAuthResult = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public VerifyOtpViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        authRepository.signInWithPhoneAuthCredential(phoneAuthCredential)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onAuthResult.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        Debug.log("VerifyOtpViewModel:signInWithPhoneAuthCredential", phoneAuthCredential.getSmsCode());
                        onAuthResult.setValue(StateResource.success(phoneAuthCredential));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        onAuthResult.setValue(StateResource.error(e.getMessage()));
                    }
                });
    }


    public LiveData<StateResource> observeAuthResult() {
        return onAuthResult;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

}

package com.zileanstdio.chatapp.Ui.register.enterPhoneNumber;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.Data.repository.AuthRepository;
import com.zileanstdio.chatapp.Utils.StateResource;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EnterPhoneNumberViewModel extends ViewModel {
    public static final String TAG = "EnterPhoneNumberViewModel";
    private final AuthRepository authRepository;
    private final MediatorLiveData<StateResource> onCheckExistedPhoneNumber = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();


    @Inject
    public EnterPhoneNumberViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void checkExistedPhoneNumber(String phoneNumber) {
        authRepository.checkExistedPhoneNumber(phoneNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onCheckExistedPhoneNumber.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onCheckExistedPhoneNumber.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        onCheckExistedPhoneNumber.setValue(StateResource.error(e.getMessage()));
                    }
                });
    }

    public LiveData<StateResource> observeCheckExistedPhoneNumber() {
        return onCheckExistedPhoneNumber;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}

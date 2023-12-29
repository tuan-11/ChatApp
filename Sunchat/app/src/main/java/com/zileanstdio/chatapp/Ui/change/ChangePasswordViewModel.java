package com.zileanstdio.chatapp.Ui.change;

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

public class ChangePasswordViewModel extends ViewModel {

    public static final String TAG = "ChangePasswordViewModel";
    private final AuthRepository authRepository;
    private final MediatorLiveData<StateResource> onChangePassword = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public ChangePasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void changePassword(String email, String passwordOld, String passwordNew) {
        authRepository.changePassword(email, passwordOld, passwordNew).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onChangePassword.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onChangePassword.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        String message = "";
                        if (e.getMessage() != null) {
                            message = e.getMessage();
                        }
                        onChangePassword.setValue(StateResource.error(message));
                    }
                });
    }

    public LiveData<StateResource> observeChangePassword() {
        return onChangePassword;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
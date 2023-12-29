package com.zileanstdio.chatapp.Ui.main.connections.profile;

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

public class ProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MediatorLiveData<StateResource> onUpdate = new MediatorLiveData<>();
    private final MediatorLiveData<StateResource> onLogout = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public ProfileViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void updateUserName(String userName, String numberPhone) {
        authRepository.updateUserName(userName, numberPhone).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onUpdate.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onUpdate.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        onUpdate.setValue(StateResource.error(e.toString()));
                    }
                });
    }

    public LiveData<StateResource> observeUpdateUserName() {
        return onUpdate;
    }

    public void logout() {
        authRepository.logout().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                        onLogout.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onLogout.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        String message = "";
                        if (e.getMessage() != null) {
                            message = e.getMessage();
                        }
                        onLogout.setValue(StateResource.error(message));
                    }
                });
    }

    public LiveData<StateResource> observeLogout() {
        return onLogout;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }
}
package com.zileanstdio.chatapp.Ui.call.incoming;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.Utils.CipherUtils;

import javax.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class IncomingCallViewModel extends ViewModel {

    public static final String TAG = "OutgoingCallViewModel";
    private final DatabaseRepository databaseRepository;
    private final MediatorLiveData<User> userFrom = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Inject
    public IncomingCallViewModel(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    public void setUserFrom(String phoneNumber) {
        databaseRepository.searchUserFromPhoneNumber(CipherUtils.Hash.sha256(phoneNumber))
                .subscribeOn(Schedulers.io())
                .toObservable()
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        userFrom.setValue(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("AAA", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public LiveData<User> getUser() {
        return userFrom;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
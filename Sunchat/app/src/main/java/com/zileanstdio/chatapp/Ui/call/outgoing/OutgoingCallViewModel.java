package com.zileanstdio.chatapp.Ui.call.outgoing;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.Message;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.regex.Pattern;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class OutgoingCallViewModel extends ViewModel {

    public static final String TAG = "OutgoingCallViewModel";
    private final DatabaseRepository databaseRepository;
    private final MediatorLiveData<User> userTo = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();
    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Inject
    public OutgoingCallViewModel(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    public void setUserTo(String phoneNumber) {
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
                        userTo.setValue(user);
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

    public void sendCallMessage(ConversationWrapper conversationWrapper, Message message) {
        databaseRepository.sendMessage(conversationWrapper, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ConversationWrapper>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull ConversationWrapper conversationWrapper) {
                        navigator.notifySendCallMessageSuccess(conversationWrapper);
                        Debug.log(TAG + ":sendCallMessage:onSuccess", "Send call message successfully");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log(TAG + ":sendCallMessage:onError", e.getMessage());
                    }
                });
    }

//    public void sendCallMessage(ConversationWrapper conversationWrapper, Message message) {
//        databaseRepository.sendCallMessage(conversationWrapper, message).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new CompletableObserver() {
//
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                        disposable.add(d);
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Debug.log(TAG + ":sendMessage:onComplete", "Send message successfully");
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                        Debug.log(TAG + ":sendMessage:onError", e.getMessage());
//                    }
//                });
//    }

    public LiveData<User> getUser() {
        return userTo;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    public interface Navigator {
        public void notifySendCallMessageSuccess(ConversationWrapper conversationWrapper);
    }
}
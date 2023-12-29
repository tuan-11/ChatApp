package com.zileanstdio.chatapp.Ui.search;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class SearchViewModel extends ViewModel {

    public static final String TAG = "SearchViewModel";
    private final DatabaseRepository databaseRepository;
    private final MediatorLiveData<User> listUser = new MediatorLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final MutableLiveData<List<Contact>> contactList = new MutableLiveData<>();
    private final HashMap<String, Contact> contactHashMap = new HashMap<>();
    private final MutableLiveData<String> currentUid = new MutableLiveData<>();
    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }


    @Inject
    public SearchViewModel(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public MutableLiveData<String> getCurrentUid() {
        return currentUid;
    }

    public HashMap<String, Contact> getContactHashMap() {
        return contactHashMap;
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    public void search(String keyword) {
        if (Pattern.matches("^[0-9]{9}[0-9]+$", keyword)) {
            databaseRepository.searchUserFromPhoneNumber(CipherUtils.Hash.sha256(keyword))
                    .subscribeOn(Schedulers.io())
                    .toObservable()
                    .subscribe(new Observer<User>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onNext(@NonNull User user) {
                            listUser.setValue(user);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.d("AAA", e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            databaseRepository.searchUserFromUserName(keyword)
                    .subscribeOn(Schedulers.io())
                    .toObservable()
                    .subscribe(new Observer<User>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            disposable.add(d);
                        }

                        @Override
                        public void onNext(@NonNull User user) {
                            listUser.setValue(user);
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
    }

    public LiveData<User> getListUser() {
        return listUser;
    }

    public void loadContacts(String uid) {
        databaseRepository.getSingleContactList(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<HashMap<String, Contact>>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    disposable.add(d);
                }

                @Override
                public void onSuccess(@NonNull HashMap<String, Contact> stringContactHashMap) {
                    contactHashMap.putAll(stringContactHashMap);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Debug.log(TAG + ":loadContacts:onError", e.getMessage());
                    contactHashMap.putAll(new HashMap<>());
                }
            });
    }

    public LiveData<Boolean> sendFriendRequest(ContactWrapInfo contactWrapInfo, String sender) {
        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>();
        databaseRepository.sendFriendRequest(contactWrapInfo, sender)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        mutableLiveData.setValue(true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("sendFriendRequest", e.getMessage());
                        mutableLiveData.setValue(false);
                    }
                });
        return mutableLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    public interface Navigator {
        void sendFriendRequest(int position, ContactWrapInfo contactWrapInfo);
    }
}
package com.zileanstdio.chatapp.Ui.sync;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SyncContactViewModel extends ViewModel {
    private static final String TAG = "SyncContactViewModel";
    private final DatabaseRepository databaseRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    final MutableLiveData<User> currentUser = new MutableLiveData<>();
    final MutableLiveData<HashMap<String, String>> localContact = new MutableLiveData<>();
    final MutableLiveData<HashMap<String, ContactWrapInfo>> userSyncFromContactLiveData = new MutableLiveData<>();
    public HashMap<String, ContactWrapInfo> userSyncFromContact = new HashMap<>();
    public HashMap<String, ContactWrapInfo> cacheUserSyncFromContact = new HashMap<>();
    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Inject
    public SyncContactViewModel(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public MutableLiveData<HashMap<String, String>> getLocalContact() {
        return localContact;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(disposable != null) {
            disposable.dispose();
        }
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void syncLocalContact(HashMap<String, String> localContact, String uid) {
        databaseRepository.syncLocalContact(localContact, uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
            .subscribe(new Observer<ContactWrapInfo>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    disposable.add(d);
                }

                @Override
                public void onNext(@NonNull ContactWrapInfo user) {
                    Debug.log("syncLocalContact:ViewModel", "onNext: " + user.toString());
                    userSyncFromContact.put(user.getContact().getNumberPhone(), user);
                    userSyncFromContactLiveData.setValue(userSyncFromContact);

                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Debug.log("syncLocalContact:ViewModel", "onError: " + e.getMessage());
                    userSyncFromContactLiveData.setValue(userSyncFromContact);
                }

                @Override
                public void onComplete() {

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

    public interface Navigator {
        public void sendFriendRequest(int position, ContactWrapInfo contactWrapInfo, String sender);
    }
}

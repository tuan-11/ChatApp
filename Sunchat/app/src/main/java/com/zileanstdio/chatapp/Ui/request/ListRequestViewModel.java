package com.zileanstdio.chatapp.Ui.request;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
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

public class ListRequestViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private final DatabaseRepository databaseRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final HashMap<String, ContactWrapInfo> requestHashMap = new HashMap<>();
    final List<ContactWrapInfo> requestList = new ArrayList<>();
    final MutableLiveData<List<ContactWrapInfo>> requestLiveData = new MutableLiveData<>();
    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Inject
    public ListRequestViewModel(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public LiveData<Boolean> denyRequest(String uid, String contactId) {
        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>();
        databaseRepository.denyRequest(uid, contactId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    disposable.add(d);
                }

                @Override
                public void onComplete() {
                    mutableLiveData.postValue(true);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Debug.log("denyRequest", e.getMessage());
                    mutableLiveData.postValue(false);
                }
            });
        return mutableLiveData;
    }

    public LiveData<Boolean> acceptRequest(String uid, String contactId) {
        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>();
        databaseRepository.acceptRequest(uid, contactId)
            .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        mutableLiveData.postValue(true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("acceptRequest", e.getMessage());
                        mutableLiveData.postValue(false);
                    }
                });
        return mutableLiveData;
    }

    public void listenRequest(String uid) {
        databaseRepository.listenRequest(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
            .subscribe(new Observer<ContactWrapInfo>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    disposable.add(d);
                }

                @Override
                public void onNext(@NonNull ContactWrapInfo contactWrapInfo) {
                    Debug.log(TAG + ":listenRequest:onNext", contactWrapInfo.toString());
                    if(requestHashMap.containsKey(contactWrapInfo.getContact().getNumberPhone())) {
                        ContactWrapInfo prevData = requestHashMap.get(contactWrapInfo.getContact().getNumberPhone());
                        if (prevData != null && prevData.getContact().getRelationship() != contactWrapInfo.getContact().getRelationship()) {
                            requestHashMap.remove(contactWrapInfo.getContact().getNumberPhone());
                        }
                    } else if(contactWrapInfo.getContact().getRelationship() == 0) {
                        requestHashMap.put(contactWrapInfo.getContact().getNumberPhone(), contactWrapInfo);
                    }
                    requestList.clear();
                    requestList.addAll(0, new ArrayList<>(requestHashMap.values()));
                    requestList.sort((obj1, obj2) -> obj2.getContact().getModifiedAt()
                            .compareTo(obj1.getContact().getModifiedAt()));
                    requestLiveData.postValue(requestList);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    if(requestList.size() == 0) {
                        requestLiveData.postValue(new ArrayList<>());
                    }
                }

                @Override
                public void onComplete() {

                }
            });
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public MutableLiveData<List<ContactWrapInfo>> getRequestLiveData() {
        return requestLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    public interface Navigator {
        public void acceptCallback(int position);
        public void denyCallback(int position);
    }
}

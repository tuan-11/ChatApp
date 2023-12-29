package com.zileanstdio.chatapp.Ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.Conversation;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.AuthRepository;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.StateResource;
import com.zileanstdio.chatapp.Utils.Stringee;

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

public class MainViewModel extends ViewModel {

    static final String TAG = "MainViewModel";
    final DatabaseRepository databaseRepository;
    final AuthRepository authRepository;
    final CompositeDisposable disposable = new CompositeDisposable();
    final MediatorLiveData<User> currentUserInfo = new MediatorLiveData<>();

    final HashMap<String, ContactWrapInfo> requestHashMap = new HashMap<>();
    final List<ContactWrapInfo> requestList = new ArrayList<>();
    private final MediatorLiveData<List<ConversationWrapper>> conversationsLiveData = new MediatorLiveData<>();
    private final HashMap<String, ConversationWrapper> conversationList = new HashMap<>();
    private final List<ConversationWrapper> conversationWrapperList = new ArrayList<>();
    final MutableLiveData<List<ContactWrapInfo>> requestLiveData = new MutableLiveData<>();
    final MutableLiveData<List<Contact>> listMutableLiveData = new MutableLiveData<>();

    @Inject
    public MainViewModel(DatabaseRepository databaseRepository, AuthRepository authRepository) {
        this.databaseRepository = databaseRepository;
        this.authRepository = authRepository;

        FirebaseUser user = authRepository.getCurrentFirebaseUser();
        if(user != null) {
            String uid = user.getEmail();
            if(uid != null) {
                uid = uid.substring(0, uid.indexOf('@'));
                loadUserInfo(CipherUtils.Hash.sha256(uid));
                listenRequest(CipherUtils.Hash.sha256(uid));
                createStringeeToken(uid);
            }

        }
    }

    public MutableLiveData<List<Contact>> getListMutableLiveData() {
        return listMutableLiveData;
    }

    public void listenRecentConversation(List<String> conversationsId) {
        databaseRepository.getRecentConversations(conversationsId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<QuerySnapshot>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        Debug.log("loadRecentConversations:onNext");
                        if(queryDocumentSnapshots != null) {
                            for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if(documentChange.getType() == DocumentChange.Type.ADDED || documentChange.getType() == DocumentChange.Type.MODIFIED) {
                                    Conversation conversation = documentChange.getDocument().toObject(Conversation.class);
                                    String conversationId = documentChange.getDocument().getId();
                                    Debug.log("loadRecentConversation:onNext", conversation.toString());
                                    conversationList.put(conversationId, new ConversationWrapper(conversationId, conversation));
                                }
                            }
                            conversationWrapperList.clear();
                            conversationWrapperList.addAll(0, new ArrayList<>(conversationList.values()));
                            conversationWrapperList.sort((obj1, obj2) -> obj2.getConversation().getLastUpdated()
                                    .compareTo(obj1.getConversation().getLastUpdated()));
                            conversationsLiveData.postValue(conversationWrapperList);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("listenRecentConversations:onError", e.getMessage());
                        if(conversationWrapperList.size() == 0) {
                            conversationsLiveData.postValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onComplete() {
                        Debug.log("listenRecentConversations", "onComplete");
                    }
                });
    }

    public void loadUserInfo(String id) {
        databaseRepository.getUserInfo(id)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        currentUserInfo.setValue(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("loadUserInfo:onError", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
                        Debug.log("listenRequest:onError", e.getMessage());
                        if(requestList.size() == 0) {
                            requestLiveData.postValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public LiveData<List<ContactWrapInfo>> getRequestList() {
        return requestLiveData;
    }

    public LiveData<User> getUserInfo() {
        return currentUserInfo;
    }


    public void createStringeeToken(String phoneNumber) {
        authRepository.createAccessToken(phoneNumber).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Stringee.client.connect(Stringee.token);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    public MediatorLiveData<List<ConversationWrapper>> getConversationsLiveData() {
        return conversationsLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
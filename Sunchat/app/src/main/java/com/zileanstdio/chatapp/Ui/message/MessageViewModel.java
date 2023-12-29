package com.zileanstdio.chatapp.Ui.message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.Conversation;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.Message;
import com.zileanstdio.chatapp.Data.model.MessageWrapper;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private final DatabaseRepository databaseRepository;
    private final CompositeDisposable disposable;
    private final MutableLiveData<ConversationWrapper> conversationLiveData;
    private final MutableLiveData<Contact> contactLiveData;
    private final MutableLiveData<User> contactProfileLiveData;
    private final MutableLiveData<User> currentUser;
    private final List<MessageWrapper> messageWrapperList;
    private final HashMap<String, MessageWrapper> messageWrapperHashMap = new HashMap<>();
    private MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, String>> listImageUrlContact;
    private final MediatorLiveData<List<MessageWrapper>> messagesLiveData;
    private final MutableLiveData<String> uidLiveData;

    @Inject
    public MessageViewModel(DatabaseRepository repository) {
        Debug.log("constructor:MessageViewModel", "working");
        this.databaseRepository = repository;
        this.disposable = new CompositeDisposable();
        this.messagesLiveData = new MediatorLiveData<>();
        this.conversationLiveData = new MutableLiveData<>();
        this.messageWrapperList = new ArrayList<>();
        this.listImageUrlContact = new MutableLiveData<>();
        this.contactLiveData = new MutableLiveData<>();
        this.contactProfileLiveData = new MutableLiveData<>();
        this.currentUser = new MutableLiveData<>();
        this.uidLiveData = new MutableLiveData<>();
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }

    public MutableLiveData<HashMap<String, String>> getListImageUrlContact() {
        return listImageUrlContact;
    }

    public MutableLiveData<ConversationWrapper> getConversationLiveData() {
        return conversationLiveData;
    }

    public MutableLiveData<String> getUidLiveData() {
        return uidLiveData;
    }

    public MediatorLiveData<List<MessageWrapper>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public LiveData<User> getUserFromUid(String uid) {
        if(contactProfileLiveData.getValue() != null) {
            return contactProfileLiveData;
        }
        databaseRepository.getInfoFromUid(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull User user) {
                        Debug.log("getUserFromUid:onSuccess", user.toString());
                        contactProfileLiveData.setValue(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("getUserFromUid:onError", e.getMessage());
                        contactProfileLiveData.setValue(new User());
                    }
                });
        return contactProfileLiveData;
    }

    public MutableLiveData<Contact> getContactLiveData() {
        return contactLiveData;
    }

    public MutableLiveData<User> getContactProfileLiveData() {
        return contactProfileLiveData;
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Contact> getContact(String uid, String documentId) {
        if(contactLiveData.getValue() != null) {
            return contactLiveData;
        }
        databaseRepository.getContact(uid, documentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Contact>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Contact contact) {
                        Debug.log("getContact:onSuccess", contact.toString());
                        contactLiveData.setValue(contact);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("getContact:onError", e.getMessage());
                        contactLiveData.setValue(new Contact());
                    }
                });
        return contactLiveData;
    }

    public LiveData<User> getLatestInfoContact(String uid) {
        MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();
        databaseRepository.getUserInfo(uid)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        Debug.log("getLatestInfoContact:onNext", user.toString());
                        userMutableLiveData.setValue(user);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log("getLatestInfoContact:onError", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return userMutableLiveData;
    }

    public void sendMessage(ConversationWrapper conversationWrapper,Message message) {
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
                        conversationLiveData.postValue(conversationWrapper);
                        Debug.log(TAG + ":sendMessage:onSuccess", "Send message successfully");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log(TAG + ":sendMessage:onError", e.getMessage());
                    }
                });
    }

    public void getMessageList(String documentId) {
        databaseRepository.getMessageList(documentId)
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
                        if(queryDocumentSnapshots != null) {
                            for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                                    Message message = documentChange.getDocument().toObject(Message.class);
                                    String messageId = documentChange.getDocument().getId();
                                    messageWrapperHashMap.put(messageId, new MessageWrapper(messageId, message));
//                                    messageWrapperList.add(new MessageWrapper(messageId, message));
                                }
                            }
                            messageWrapperList.clear();
                            messageWrapperList.addAll(0, new ArrayList<>(messageWrapperHashMap.values()));
                            messageWrapperList.sort(Comparator.comparing(o -> o.getMessage().getSendAt()));
                            messagesLiveData.postValue(messageWrapperList);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Debug.log(TAG + ":getMessageList:onError", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void setCurrentUser(User user) {
        currentUserLiveData.setValue(user);
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}

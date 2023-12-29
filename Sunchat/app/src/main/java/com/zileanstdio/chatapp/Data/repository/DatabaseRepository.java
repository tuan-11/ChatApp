package com.zileanstdio.chatapp.Data.repository;

import com.google.firebase.firestore.QuerySnapshot;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.Message;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.DataSource.remote.FirestoreDBSource;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import kotlinx.coroutines.flow.Flow;

public class DatabaseRepository {

    FirestoreDBSource firestoreDBSource;

    @Inject
    public DatabaseRepository(FirestoreDBSource firestoreDBSource) {
        this.firestoreDBSource = firestoreDBSource;
    }

    public Flowable<QuerySnapshot> getRecentConversations(List<String> conversationsId) {
        return firestoreDBSource.getRecentConversations(conversationsId);
    }

    public Flowable<User> getUserInfo(String uid) {
        return firestoreDBSource.getUserInfo(uid);
    }

    public Single<User> getInfoFromUid(String uid) {
        return firestoreDBSource.getInfoFromUid(uid);
    }

    public Single<Contact> getContact(String uid, String documentId) {
        return firestoreDBSource.getContact(uid, documentId);
    }

    public Flowable<QuerySnapshot> getMessageList(final String uid) {
        return firestoreDBSource.getMessageList(uid);
    }

    public Flowable<User> searchUserFromUserName(String keyword) {
        return firestoreDBSource.searchUserFromUserName(keyword);
    }

    public Flowable<User> searchUserFromPhoneNumber(String phoneNumberHashed) {
        return firestoreDBSource.searchUserFromPhoneNumber(phoneNumberHashed);
    }

    public Single<ConversationWrapper> sendMessage(ConversationWrapper conversationWrapper, Message message) {
        return firestoreDBSource.sendMessage(conversationWrapper, message);
    }

    public Flowable<Contact> getContacts(String uid) {
        return firestoreDBSource.getContacts(uid);
    }

    public Single<HashMap<String, Contact>> getSingleContactList(String uid) {
        return firestoreDBSource.getSingleContactList(uid);
    }

    public Flowable<ContactWrapInfo> syncLocalContact(HashMap<String, String> localContact, String uid) {
        return firestoreDBSource.syncLocalContact(localContact, uid);
    }

    public Completable sendFriendRequest(ContactWrapInfo contactWrapInfo, String sender) {
        return firestoreDBSource.sendFriendRequest(contactWrapInfo, sender);
    }

    public Flowable<ContactWrapInfo> getContactWrapInfo(String uid) {
        return firestoreDBSource.getContactWrapInfo(uid);
    }

    public Completable sendCallMessage(ConversationWrapper conversationWrapper, Message message) {
        return firestoreDBSource.sendCallMessage(conversationWrapper, message);
    }

    public Flowable<ContactWrapInfo> listenRequest(String uid) {
        return firestoreDBSource.listenRequest(uid);
    }

    public Completable acceptRequest(final String uid, final String contactId) {
        return firestoreDBSource.acceptRequest(uid, contactId);
    }

    public Completable denyRequest(final String uid, final String contactId) {
        return firestoreDBSource.denyRequest(uid, contactId);
    }

}

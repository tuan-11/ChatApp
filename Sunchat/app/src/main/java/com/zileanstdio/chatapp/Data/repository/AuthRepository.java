package com.zileanstdio.chatapp.Data.repository;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.AuthCredential;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.DataSource.remote.FirebaseAuthSource;


import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class AuthRepository {
    FirebaseAuthSource firebaseAuthSource;

    public AuthRepository(FirebaseAuthSource firebaseAuthSource) {
        this.firebaseAuthSource = firebaseAuthSource;
    }

    public Completable checkExistedPhoneNumber(String phoneNumber) {
        return this.firebaseAuthSource.checkExistedPhoneNumber(phoneNumber);
    }

    public void phoneNumberVerification(String phoneNumber, FragmentActivity fragmentActivity, PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        this.firebaseAuthSource.phoneNumberVerification(phoneNumber, fragmentActivity, callbacks);
    }

    public Completable signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        return this.firebaseAuthSource.signInWithPhoneAuthCredential(phoneAuthCredential);
    }


    public Completable login(String phoneNumber, String password) {
        return this.firebaseAuthSource.login(phoneNumber, password);
    }

    public Observable<FirebaseUser> createWithEmailPasswordAuthCredential(String email, String password) {
        return this.firebaseAuthSource.createWithEmailPasswordAuthCredential(email, password);
    }
    public Completable updateRegisterInfo(User user) {
        return this.firebaseAuthSource.updateRegisterInfo(user);
    }

    public Completable checkLoginUser() {
        return this.firebaseAuthSource.checkLoginUser();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return this.firebaseAuthSource.getCurrentFirebaseUser();
    }

    public Completable logout() {
        return this.firebaseAuthSource.logout();
    }

    public Completable updateUserName(String userName, String phoneNumber) {
        return this.firebaseAuthSource.updateUserName(userName, phoneNumber);
    }

    public Completable changePassword(String email, String passwordOld, String passwordNew) {
        return this.firebaseAuthSource.changePassword(email, passwordOld, passwordNew);
    }

    public Completable createAccessToken(String phoneNumber) {
        return this.firebaseAuthSource.createAccessToken(phoneNumber);
    }
}
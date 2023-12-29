package com.zileanstdio.chatapp.DI;

import androidx.lifecycle.SavedStateHandle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.zileanstdio.chatapp.Data.repository.AuthRepository;
import com.zileanstdio.chatapp.Data.repository.DatabaseRepository;
import com.zileanstdio.chatapp.DataSource.remote.FirebaseAuthSource;
import com.zileanstdio.chatapp.DataSource.remote.FirestoreDBSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Singleton
    @Provides
    static FirebaseAuth getAuthInstance() {
        return FirebaseAuth.getInstance();
    }

    @Singleton
    @Provides
    static FirebaseFirestore getFireStoreInstance() {
        return FirebaseFirestore.getInstance();
    }

    @Singleton
    @Provides
    static FirebaseAuthSource getAuthSource(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        return new FirebaseAuthSource(firebaseAuth, firebaseFirestore);
    }

    @Singleton
    @Provides
    static AuthRepository provideAuthRepository(FirebaseAuthSource firebaseAuthSource) {
        return new AuthRepository(firebaseAuthSource);
    }

    @Singleton
    @Provides
    static FirebaseStorage getStorageInstance() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    public SavedStateHandle savedStateHandle() {
        return new SavedStateHandle();
    }

    @Provides
    static FirestoreDBSource getFirestoreDBSource(FirebaseFirestore firebaseFirestore) {
        return new FirestoreDBSource(firebaseFirestore);
    }

    @Singleton
    @Provides
    static DatabaseRepository databaseRepository(FirestoreDBSource firestoreDBSource) {
        return new DatabaseRepository(firestoreDBSource);
    }
}

package com.zileanstdio.chatapp.Ui.register;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.Data.repository.AuthRepository;

import javax.inject.Inject;


public class RegisterViewModel extends ViewModel {
    private static final String TAG = "RegisterViewModel";
    private static final String INPUT_SAVED_INSTANCE = "register_info";
    private static final String INPUT_VERIFICATION_ID = "verification_id";
    private final MutableLiveData<String> verificationId;
    private final MutableLiveData<String> phoneNumberWithCountryCode;
    private static final String PHONE_NUMBER_WITH_COUNTRY_CODE = "PHONE_NUMBER_WITH_COUNTRY_CODE";
    private final AuthRepository authRepository;

    @Inject
    SavedStateHandle savedStateHandle;

    @Inject
    public RegisterViewModel(SavedStateHandle savedStateHandle, AuthRepository authRepository) {
        this.savedStateHandle = savedStateHandle;
        this.authRepository = authRepository;
        verificationId = savedStateHandle.getLiveData(INPUT_VERIFICATION_ID);
        phoneNumberWithCountryCode = savedStateHandle.getLiveData(PHONE_NUMBER_WITH_COUNTRY_CODE);
    }

    public void phoneNumberVerification(String phoneNumber, FragmentActivity fragmentActivity, PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        authRepository.phoneNumberVerification(phoneNumber, fragmentActivity, callbacks);
    }

    public LiveData<User> getRegisterInfo() {
        return savedStateHandle.getLiveData(INPUT_SAVED_INSTANCE);
    }

    public LiveData<String> getVerificationId() {
        return verificationId;
//        return savedStateHandle.getLiveData(INPUT_VERIFICATION_ID);
    }
    
    public LiveData<String> getPhoneNumberWithCountryCode() {
        return phoneNumberWithCountryCode;
    }


    public void saveNameInput(String name) {
        User user = new User();
        if(savedStateHandle.contains(INPUT_SAVED_INSTANCE)) {
            user = savedStateHandle.get(INPUT_SAVED_INSTANCE);
        }
        if (user != null) {
            user.setUserName(name);
        }
        savedStateHandle.set(INPUT_SAVED_INSTANCE, user);
    }

    public void saveGender(String gender) {
        User user = new User();
        if(savedStateHandle.contains(INPUT_SAVED_INSTANCE)) {
            user = savedStateHandle.get(INPUT_SAVED_INSTANCE);
        }
        if (user != null) {
            user.setGender(gender);
        }
        savedStateHandle.set(INPUT_SAVED_INSTANCE, user);
    }

    public void saveBirthDate(String date) {
        User user = new User();
        if(savedStateHandle.contains(INPUT_SAVED_INSTANCE)) {
            user = savedStateHandle.get(INPUT_SAVED_INSTANCE);
        }
        if (user != null) {
            user.setBirthDate(date);
        }
        savedStateHandle.set(INPUT_SAVED_INSTANCE, user);
    }
    
    public void savePhoneNumberWithCountryCode(String phoneNumber) {
//        savedStateHandle.set(PHONE_NUMBER_WITH_COUNTRY_CODE, phoneNumber);
        phoneNumberWithCountryCode.setValue(phoneNumber);
    }

    public void savePhoneNumberInput(String phoneNumber) {
        User user = new User();
        if(savedStateHandle.contains(INPUT_SAVED_INSTANCE)) {
            user = savedStateHandle.get(INPUT_SAVED_INSTANCE);
        }
        if(user != null) {
            user.setPhoneNumber(phoneNumber);
        }
        savedStateHandle.set(INPUT_SAVED_INSTANCE, user);
    }

    public void saveVerificationId(String verificationId) {
        this.verificationId.setValue(verificationId);
    }
}

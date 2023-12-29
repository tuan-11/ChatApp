package com.zileanstdio.chatapp.Ui.register.enterPhoneNumber;

import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.register.RegisterActivity;
import com.zileanstdio.chatapp.Ui.register.RegisterViewModel;
import com.zileanstdio.chatapp.Ui.register.verifyOtp.VerifyOtpView;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class EnterPhoneNumberView extends BaseFragment {
    private static final String[] items = {"VN"};
    private String mVerificationId = null;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private TextInputLayout phoneNumberInputLayout;
    private EditText phoneNumberInputEditText;
    private final CompositeDisposable disposable = new CompositeDisposable();


    @Override
    public ViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        Log.d("ViewModelProviderFactory", "providerFactory: " + providerFactory);
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(EnterPhoneNumberViewModel.class);
        return viewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscribeObservers();
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                baseActivity.closeLoadingDialog();

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                baseActivity.closeLoadingDialog();
                phoneNumberInputEditText.setEnabled(true);
                ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(true);
                Debug.log("DEBUG_APP", e.getMessage());
                Snackbar.make(viewRoot, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                baseActivity.closeLoadingDialog();
                phoneNumberInputEditText.setEnabled(true);
                ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(true);
                mVerificationId = verificationId;
                ((RegisterViewModel)baseActivity.getViewModel()).saveVerificationId(verificationId);
                baseActivity.replaceFragment(new VerifyOtpView());

            }
        };
    }

    @Override
    protected void initAppBar() {

    }


    private void initView(View view) {
        phoneNumberInputLayout = view.findViewById(R.id.text_input_number_phone);
        phoneNumberInputEditText = phoneNumberInputLayout.getEditText();
        MaterialAutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.auto_complete_region_code);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_dropdown_list, items);
        autoCompleteTextView.setAdapter(arrayAdapter);
        autoCompleteTextView.setText(items[0]);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        if(phoneNumberInputEditText != null) {
            ((RegisterViewModel)baseActivity.getViewModel()).getRegisterInfo()
                    .observe(getViewLifecycleOwner(), user -> phoneNumberInputEditText.setText(user.getPhoneNumber()));

            Observable<Boolean> phoneNumberInputObservable = RxTextView.textChanges(phoneNumberInputEditText)
                    .map(inputText -> validatePhoneNumber(inputText.toString()))
                    .distinctUntilChanged();
            disposable.add(phoneNumberInputObservable.subscribe(isValid ->
                    ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(isValid)
            ));
        }

        ((RegisterActivity) baseActivity).getNextActionBtn().setOnClickListener(v1 -> openConfirmDialog(phoneNumberInputEditText.getText().toString().trim()));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_enter_number_phone_view, container, false);
        return super.onCreateView(inflater, view, savedInstanceState);
    }

    private boolean validatePhoneNumber(String phoneNumberInput) {
        if(phoneNumberInput.trim().length() == 0) {
            phoneNumberInputEditText.getText().clear();
            phoneNumberInputEditText.setSelection(0);
            return false;
        } else if(!Pattern.matches("^[0-9]{0,2}[\\s-]?[- ]?([0-9]{0,4})[- ]?([0-9]{0,4})[- ]?([0-9]{0,})$", phoneNumberInput)) {
            phoneNumberInputLayout.setError("Số điện thoại không khả dụng. (e.x:1-888-364-3577).");
            phoneNumberInputLayout.setErrorIconDrawable(null);
            return false;
        } else if(!Pattern.matches("^(?=(?:\\D*\\d){10,}\\D*$)[0-9]{1,2}[\\s-]?[- ]?([0-9]{3,4})[- ]?([0-9]{3,4})[- ]?([0-9]{3,10})$", phoneNumberInput)) {
            phoneNumberInputLayout.setError("Số điện thoại phải từ 10 đến 20 chữ số.");
            phoneNumberInputLayout.setErrorIconDrawable(null);
            return false;
        } else {
            phoneNumberInputLayout.setError(null);
            return true;
        }
    }

    private void openConfirmDialog(String phoneNumberInput) {
        String temp = phoneNumberInput;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber swissNumberProto = null;
        try {
            swissNumberProto = phoneNumberUtil.parse(temp, "VN");
        } catch (NumberParseException e) {
            Debug.log(TAG, "NumberParseException was thrown: " + e);

        }
        temp = phoneNumberUtil.format(swissNumberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        ((RegisterViewModel)baseActivity.getViewModel()).savePhoneNumberWithCountryCode(temp);

        String message = String.format("%s %s ?", getResources().getString(R.string.confirm_otp_auth_text), temp);
        new MaterialAlertDialogBuilder(baseActivity)
                .setTitle("Xác nhận số điện thoại")
                .setMessage(message)
                .setNegativeButton(getResources().getString(R.string.cancel_text), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getResources().getString(R.string.confirm_text), (dialog, which) -> {
                    dialog.dismiss();
                    ((EnterPhoneNumberViewModel) viewModel).checkExistedPhoneNumber(phoneNumberInput);
                }).show();
    }

    private void subscribeObservers(){
        ((EnterPhoneNumberViewModel) viewModel).observeCheckExistedPhoneNumber().observe(this, stateResource -> {
            if(stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        baseActivity.showLoadingDialog();
                        break;
                    case SUCCESS:
                        String phoneNumber = String.format("%s%s", "+84", phoneNumberInputEditText.getText().toString().trim());
                        ((RegisterViewModel)baseActivity.getViewModel()).phoneNumberVerification(phoneNumber, getActivity(), mCallBacks);
                        break;
                    case ERROR:
                        baseActivity.closeLoadingDialog();
                        showSnackBar(stateResource.message, Snackbar.LENGTH_LONG);
                        break;
                }
            }
        });
    }



    @Override
    public void onStop() {
        super.onStop();
        String phoneNumberInput = phoneNumberInputEditText.getText().toString().trim();
        Debug.log("DEBUG_APP", "onStop:phoneNumberInput - " + phoneNumberInput);
        Debug.log("DEBUG_APP", "onStop:mVerificationId - " + mVerificationId);

        ((RegisterViewModel)baseActivity.getViewModel()).savePhoneNumberInput(phoneNumberInput);
        disposable.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
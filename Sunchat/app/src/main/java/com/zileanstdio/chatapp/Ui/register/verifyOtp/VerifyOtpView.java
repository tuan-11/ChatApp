package com.zileanstdio.chatapp.Ui.register.verifyOtp;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.register.RegisterActivity;
import com.zileanstdio.chatapp.Ui.register.RegisterViewModel;
import com.zileanstdio.chatapp.Ui.register.selectGenderAndBirthDate.SelectGenderAndBirthDateView;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class VerifyOtpView extends BaseFragment<VerifyOtpViewModel> {
    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String verificationId;
    private String phoneNumber;
    private TextView resendOtpText;
    private final CompositeDisposable disposable = new CompositeDisposable();


    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public VerifyOtpViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(VerifyOtpViewModel.class);
        return viewModel;
    }

    @Override
    protected void initAppBar() {
        baseActivity.setTitleToolbar(getResources().getString(R.string.input_code_verify_text));
    }

    private void initView(View v) {

        resendOtpText = v.findViewById(R.id.resend_otp_text_view);
        inputCode1 = v.findViewById(R.id.input_code_1);
        inputCode1.requestFocus();
        inputCode2 = v.findViewById(R.id.input_code_2);
        inputCode3 = v.findViewById(R.id.input_code_3);
        inputCode4 = v.findViewById(R.id.input_code_4);
        inputCode5 = v.findViewById(R.id.input_code_5);
        inputCode6 = v.findViewById(R.id.input_code_6);
        setupOTPInputs();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
                Debug.log("DEBUG_APP", e.getMessage());
                Snackbar.make(viewRoot, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                baseActivity.closeLoadingDialog();
                ((RegisterViewModel)baseActivity.getViewModel()).saveVerificationId(verificationId);
                Snackbar.make(viewRoot, "OTP Resent", Snackbar.LENGTH_LONG).show();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_verify_otp_view, container, false);
        return super.onCreateView(inflater, view, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        ((RegisterViewModel)baseActivity.getViewModel()).getRegisterInfo()
                .observe(getViewLifecycleOwner(), user -> phoneNumber = user.getPhoneNumber());
        TextView notificationText = view.findViewById(R.id.notification_text_view);
        ((RegisterViewModel)baseActivity.getViewModel()).getVerificationId()
                .observe(getViewLifecycleOwner(), this::setVerificationId);
        ((RegisterViewModel)baseActivity.getViewModel()).getRegisterInfo()
                .observe(getViewLifecycleOwner(), user -> setPhoneNumber(user.getPhoneNumber()));
        ((RegisterViewModel)baseActivity.getViewModel()).getPhoneNumberWithCountryCode()
                .observe(getViewLifecycleOwner(), s -> notificationText.setText(String.format("%s%s",notificationText.getText().toString(), s)));

        setListeners();
    }

    private void setListeners() {
        ((RegisterActivity) baseActivity).getNextActionBtn().setOnClickListener(v1 -> {
            String code =
                    inputCode1.getText().toString() +
                            inputCode2.getText().toString() +
                            inputCode3.getText().toString() +
                            inputCode4.getText().toString() +
                            inputCode5.getText().toString() +
                            inputCode6.getText().toString();
            if(verificationId != null) {
                baseActivity.showLoadingDialog();
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, code);
                viewModel.signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        });
        disposable.add(RxView.clicks(resendOtpText)
                    .throttleFirst(60, TimeUnit.SECONDS)
                    .subscribe(unit -> {
                        baseActivity.showLoadingDialog();
                        String phoneNumberFormat = String.format("%s%s", "+84", phoneNumber);
                        Debug.log(TAG, "DEBUG_APP:phoneNumberFormat - " + phoneNumberFormat);
                        ((RegisterViewModel)baseActivity.getViewModel()).phoneNumberVerification(phoneNumberFormat, getActivity(), mCallBacks);

                    })
        );


    }

    private void subscribeObservers(){
        viewModel.observeAuthResult().observe(this, stateResource -> {
            if(stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        baseActivity.showLoadingDialog();
                        break;
                    case SUCCESS:
                        baseActivity.closeLoadingDialog();
                        baseActivity.getSupportFragmentManager().popBackStack();
                        baseActivity.replaceFragment(new SelectGenderAndBirthDateView());
                        break;
                    case ERROR:
                        baseActivity.closeLoadingDialog();
                        showSnackBar(stateResource.message, Snackbar.LENGTH_LONG);
                        break;
                }
            }
        });
    }

    private void setupOTPInputs() {
        Observable<Boolean> inputCode1Observable = RxTextView.textChanges(inputCode1)
                .skipInitialValue()
                .map(inputText -> validateInputCode(inputText.toString()))
                .distinctUntilChanged();
        disposable.add(inputCode1Observable.subscribe(isValid -> {
            if(isValid) inputCode2.requestFocus();
        }));

        Observable<Boolean> inputCode2Observable = RxTextView.textChanges(inputCode2)
                .skipInitialValue()
                .map(inputText -> validateInputCode(inputText.toString()))
                .distinctUntilChanged();
        disposable.add(inputCode2Observable.subscribe(isValid -> {
            if(isValid) {
                inputCode3.requestFocus();
            } else {
                inputCode1.requestFocus();
            }
        }));

        inputCode2.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP) {
                inputCode1.requestFocus();
                return true;
            }
            return false;
        });

        Observable<Boolean> inputCode3Observable = RxTextView.textChanges(inputCode3)
                .skipInitialValue()
                .map(inputText -> validateInputCode(inputText.toString()))
                .distinctUntilChanged();

        disposable.add(inputCode3Observable.subscribe(isValid -> {
            if(isValid) {
                inputCode4.requestFocus();
            } else {
                inputCode2.requestFocus();
            }
        }));

        inputCode3.setOnKeyListener((view, keyCode, keyEvent) -> {
            if(keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                inputCode2.requestFocus();
                return true;
            }
            return false;
        });

        Observable<Boolean> inputCode4Observable = RxTextView.textChanges(inputCode4)
                .skipInitialValue()
                .map(inputText -> validateInputCode(inputText.toString()))
                .distinctUntilChanged();
        disposable.add(inputCode4Observable.subscribe(isValid -> {
            if(isValid) {
                inputCode5.requestFocus();
            } else {
                inputCode3.requestFocus();
            }
        }));

        inputCode4.setOnKeyListener((view, keyCode, keyEvent) -> {
            if(keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                inputCode3.requestFocus();
                return true;
            }
            return false;
        });

        Observable<Boolean> inputCode5Observable = RxTextView.textChanges(inputCode5)
                .skipInitialValue()
                .map(inputText -> validateInputCode(inputText.toString()))
                .distinctUntilChanged();
        disposable.add(inputCode5Observable.subscribe(isValid -> {
            if(isValid) {
                inputCode6.requestFocus();
            } else {
                inputCode4.requestFocus();
            }
        }));

        inputCode5.setOnKeyListener((view, keyCode, keyEvent) -> {
            if(keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                inputCode4.requestFocus();
                return true;
            }
            return false;
        });

        Observable<Boolean> inputCode6Observable = RxTextView.textChanges(inputCode6)
                .skipInitialValue()
                .map(inputText -> validateInputCode(inputText.toString()))
                .distinctUntilChanged();
        disposable.add(inputCode6Observable.subscribe(isValid -> {
            if(isValid) {
                View view = baseActivity.getCurrentFocus();
                if(view != null) {
                    baseActivity.hideKeyboard();
                }
                ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(true);
            } else {
                inputCode5.requestFocus();
            }
        }));

        inputCode6.setOnKeyListener((view, keyCode, keyEvent) -> {
            if(keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                inputCode5.requestFocus();
                return true;
            }
            return false;
        });
        disposable.add(Observable
                .combineLatest(inputCode1Observable,
                                inputCode2Observable,
                                inputCode3Observable,
                                inputCode4Observable,
                                inputCode5Observable,
                                inputCode6Observable,
                                (i1, i2, i3, i4, i5, i6) -> i1 && i2 && i3 && i4 && i5 && i6)
                .subscribe(isValid -> ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(isValid))
        );


    }

    private boolean validateInputCode(String input) {
        return !input.trim().isEmpty();
    }

    @Override
    public void onStop() {
        super.onStop();
        disposable.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}

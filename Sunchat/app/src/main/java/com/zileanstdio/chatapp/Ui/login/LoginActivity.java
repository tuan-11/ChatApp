package com.zileanstdio.chatapp.Ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.main.MainActivity;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class LoginActivity extends BaseActivity {

    private boolean isValidatePhoneNumber, isValidatePassword;
    private FloatingActionButton loginButton;
    private TextInputLayout phoneNumberInputLayout, passwordInputLayout;
    private EditText phoneNumberEditText, passwordEditText;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppBar();

        subscribeObservers();

        loginButton = findViewById(R.id.activity_login_next_btn);
        phoneNumberInputLayout = findViewById(R.id.activity_login_text_input_number_phone);
        phoneNumberEditText = phoneNumberInputLayout.getEditText();
        passwordInputLayout = findViewById(R.id.activity_login_text_input_password);
        passwordEditText = passwordInputLayout.getEditText();

        if (phoneNumberEditText != null) {
            Observable<Boolean> phoneNumberInputObservable = RxTextView.textChanges(phoneNumberEditText)
                    .map(inputText -> validatePhoneNumber(inputText.toString()))
                    .distinctUntilChanged();
            ((LoginViewModel) viewModel).getDisposable().add(phoneNumberInputObservable.subscribe(isValid -> loginButton.setEnabled(isValid)));
        }

        if (passwordEditText != null) {
            Observable<Boolean> passwordInputObservable = RxTextView.textChanges(passwordEditText)
                    .map(inputText -> validatePassword(inputText.toString()))
                    .distinctUntilChanged();
            ((LoginViewModel) viewModel).getDisposable().add(passwordInputObservable.subscribe(isValid -> loginButton.setEnabled(isValid)));
        }

        loginButton.setOnClickListener(v -> {
            loginButton.setEnabled(false);
            phoneNumberEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            ((LoginViewModel) viewModel).login(phoneNumberEditText.getText().toString(), passwordEditText.getText().toString());
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    @Override
    public void initAppBar() {
        super.initAppBar();
        setTitleToolbar("Đăng nhập");
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public ViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(LoginViewModel.class);
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clLoginActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @Override
    public void onClick(View v) {

    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.trim().isEmpty()) {
            phoneNumberEditText.getText().clear();
            phoneNumberEditText.setSelection(0);
            isValidatePhoneNumber = false;
        } else if (!Pattern.matches("^[0-9]{0,2}[\\s-]?[- ]?([0-9]{0,4})[- ]?([0-9]{0,4})[- ]?([0-9]{0,})$", phoneNumber)) {
            phoneNumberInputLayout.setError("Số điện thoại không khả dụng. (e.x:1-888-364-3577).");
            phoneNumberInputLayout.setErrorIconDrawable(null);
            isValidatePhoneNumber = false;
        } else if  (!Pattern.matches("^(?=(?:\\D*\\d){10,}\\D*$)[0-9]{1,2}[\\s-]?[- ]?([0-9]{3,4})[- ]?([0-9]{3,4})[- ]?([0-9]{3,10})$", phoneNumber)) {
            phoneNumberInputLayout.setError("Số điện thoại phải từ 10 đến 20 chữ số");
            phoneNumberInputLayout.setErrorIconDrawable(null);
            isValidatePhoneNumber = false;
        } else {
            phoneNumberInputLayout.setError(null);
            isValidatePhoneNumber = true;
        }
        return (isValidatePhoneNumber && isValidatePassword);
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            passwordEditText.getText().clear();
            passwordEditText.setSelection(0);
            isValidatePassword = false;
        } else {
            passwordInputLayout.setError(null);
            isValidatePassword = true;
        }
        return (isValidatePassword && isValidatePhoneNumber);
    }

    public void subscribeObservers() {
        ((LoginViewModel) viewModel).observeLogin().observe(this, stateResource -> {
            if (stateResource != null) {
                Debug.log(stateResource.toString());
                switch (stateResource.status) {
                    case LOADING:
                        Debug.log("loginObserver", "LOADING");
                        showLoadingDialog();
                        break;
                    case SUCCESS:
                        Debug.log("loginObserver", "SUCCESS");
                        Toast.makeText(this, "Chào mừng đến với 'Sunchat'", Toast.LENGTH_SHORT).show();
                        Intent startMainActivity = new Intent(this, MainActivity.class);
                        startMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(startMainActivity);
                        break;
                    case ERROR:
                        Debug.log("loginObserver", "ERROR");
                        closeLoadingDialog();
                        phoneNumberEditText.setEnabled(true);
                        passwordEditText.setEnabled(true);
                        loginButton.setEnabled(true);
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        Debug.log(stateResource.message);
                        break;
                }
            }
        });
    }
}
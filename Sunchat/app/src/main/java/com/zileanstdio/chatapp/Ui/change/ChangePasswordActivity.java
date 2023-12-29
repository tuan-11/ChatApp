package com.zileanstdio.chatapp.Ui.change;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.main.MainActivity;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ChangePasswordActivity extends BaseActivity {

    private boolean checkOld, checkNew, checkConfirm;
    private int swapOld = 1, swapNew = 1, swapConfirm = 1;
    private String email;

    private FloatingActionButton changeButton;
    private TextInputLayout passwordOldTextInput, passwordNewTextInput, passwordConfirmTextInput;
    private EditText passwordOldEditText, passwordNewEditText, passwordConfirmEditText;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppBar();
        initView();
        subscribeObservers();

        if (passwordOldEditText != null && passwordNewEditText != null && passwordConfirmEditText != null) {
            Observable<Boolean> passwordOldInputObservable = RxTextView.textChanges(passwordOldEditText)
                    .map(password -> checkOldPassword(password.toString()))
                    .distinctUntilChanged();
            Observable<Boolean> passwordNewInputObservable = RxTextView.textChanges(passwordNewEditText)
                    .map(password -> checkNewPassword(password.toString()))
                    .distinctUntilChanged();
            Observable<Boolean> passwordConfirmInputObservable = RxTextView.textChanges(passwordConfirmEditText)
                    .map(password -> checkConfirmPassword(password.toString()))
                    .distinctUntilChanged();
            disposable.add(passwordOldInputObservable.subscribe(isValid -> {
                changeButton.setEnabled(isValid);
                Log.d("AAA", "New: " + isValid);
            }));
            disposable.add(passwordNewInputObservable.subscribe(isValid -> {
                changeButton.setEnabled(isValid);
                Log.d("AAA", "New: " + isValid);
            }));
            disposable.add(passwordConfirmInputObservable.subscribe(isValid -> {
                changeButton.setEnabled(isValid);
                Log.d("AAA", "Confirm: " + isValid);
            }));
            /*disposable.add(Observable.combineLatest(
                            passwordOldInputObservable, passwordNewInputObservable, passwordConfirmInputObservable,
                            (resultOld, resultNew, resultConfirm) -> {
                                if (swapOld == 0) {
                                    resultOld = !resultOld;
                                    swapOld = 1;
                                } else {
                                    swapOld = 0;
                                }
                                if (swapNew == 0) {
                                    resultNew = !resultNew;
                                    swapNew = 1;
                                } else {
                                    swapNew = 0;
                                }
                                if (swapConfirm == 0) {
                                    resultConfirm = !resultConfirm;
                                    swapConfirm = 1;
                                } else {
                                    swapConfirm = 0;
                                }
                                return resultOld && resultNew && resultConfirm;
                            }
                    ).subscribe(result -> changeButton.setEnabled(result))
            );*/
        }

        changeButton.setOnClickListener(v -> {
            changeButton.setEnabled(false);
            passwordOldEditText.setEnabled(false);
            passwordNewEditText.setEnabled(false);
            passwordConfirmEditText.setEnabled(false);
            ((ChangePasswordViewModel) viewModel).changePassword(email,
                    passwordOldEditText.getText().toString(),
                    passwordNewEditText.getText().toString());
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
        setTitleToolbar("Đổi mật khẩu");
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public ViewModel getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(ChangePasswordViewModel.class);
        }
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_change_password;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clChangePasswordActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @Override
    public void onClick(View view) {

    }

    public void initView() {
        passwordOldTextInput = findViewById(R.id.text_input_password_old);
        passwordOldEditText = passwordOldTextInput.getEditText();
        passwordNewTextInput = findViewById(R.id.text_input_password_new);
        passwordNewEditText = passwordNewTextInput.getEditText();
        passwordConfirmTextInput = findViewById(R.id.text_input_password_confirm);
        passwordConfirmEditText = passwordConfirmTextInput.getEditText();
        changeButton = findViewById(R.id.btn_change);
        email = getIntent().getStringExtra("email");
    }

    public boolean checkOldPassword(String password) {
        if (password.isEmpty()) {
            passwordNewEditText.getText().clear();
            passwordNewEditText.setSelection(0);
            checkOld = false;
        } else {
            checkOld = true;
        }
        return (checkOld && checkNew && checkConfirm);
    }

    public boolean checkNewPassword(String password) {
        if (password.isEmpty()) {
            passwordNewEditText.getText().clear();
            passwordNewEditText.setSelection(0);
            checkNew = false;
        } else if (!passwordConfirmEditText.getText().toString().equals(password)) {
            passwordConfirmTextInput.setError("Mật khẩu xác nhận không khớp");
            passwordConfirmTextInput.setErrorIconDrawable(null);
            checkNew = true;
            checkConfirm = false;
        } else {
            passwordConfirmTextInput.setError(null);
            checkNew = true;
        }
        return (checkOld && checkNew && checkConfirm);
    }

    public boolean checkConfirmPassword(String password) {
        if (password.isEmpty()) {
            passwordConfirmEditText.getText().clear();
            passwordConfirmEditText.setSelection(0);
            checkConfirm = false;
        } else if (!passwordNewEditText.getText().toString().equals(password)) {
            passwordConfirmTextInput.setError("Mật khẩu xác nhận không khớp");
            passwordConfirmTextInput.setErrorIconDrawable(null);
            checkConfirm = false;
        } else {
            passwordConfirmTextInput.setError(null);
            checkConfirm = true;
        }
        return (checkOld && checkNew && checkConfirm);
    }

    public void subscribeObservers() {
        ((ChangePasswordViewModel) viewModel).observeChangePassword().observe(this, stateResource -> {
            if (stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        showLoadingDialog();
                        break;
                    case SUCCESS:
                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case ERROR:
                        closeLoadingDialog();
                        passwordOldEditText.setEnabled(true);
                        passwordNewEditText.setEnabled(true);
                        passwordConfirmEditText.setEnabled(true);
                        passwordOldTextInput.setError(stateResource.message);
                        break;
                }
            }
        });
    }
}
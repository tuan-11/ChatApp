package com.zileanstdio.chatapp.Ui.register.enterName;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import androidx.lifecycle.ViewModel;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.register.RegisterActivity;
import com.zileanstdio.chatapp.Ui.register.RegisterViewModel;
import com.zileanstdio.chatapp.Ui.register.enterPhoneNumber.EnterPhoneNumberView;

import io.reactivex.rxjava3.core.Observable;

public class EnterNameView extends BaseFragment {

    private TextInputLayout nameInputLayout;
    private EditText nameInputEditText;

    @Override
    public ViewModel getViewModel() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initAppBar() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_enter_name_view, container, false);
        View v = super.onCreateView(inflater, view, savedInstanceState);
        nameInputLayout = v.findViewById(R.id.text_input_name);
        nameInputEditText = nameInputLayout.getEditText();

        if(nameInputEditText != null) {
            ((RegisterViewModel)baseActivity.getViewModel()).getRegisterInfo()
                    .observe(getViewLifecycleOwner(), user -> nameInputEditText.setText(user.getUserName()));

            Observable<Boolean> nameInputObservable = RxTextView.textChanges(nameInputEditText)
                    .map(inputText -> validateName(inputText.toString().trim()))
                    .distinctUntilChanged();

            nameInputObservable.subscribe(isValid -> {
                ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(isValid);
            });
        }
        ((RegisterActivity) baseActivity).getNextActionBtn().setOnClickListener(v1 -> {
            ((RegisterActivity) baseActivity).replaceFragment(new EnterPhoneNumberView());
        });
        return v;
    }

    private boolean validateName(String nameInput) {
        if(nameInput.matches(".*[0-9].*")) {
            nameInputLayout.setError("Tên không được chứa chữ số");
            nameInputLayout.setErrorIconDrawable(null);
            return false;
        }
        else if(nameInput.trim().length() == 0) {
            nameInputEditText.getText().clear();
            nameInputEditText.setSelection(0);
            return false;
        }
        else if(nameInput.length() > 40 || nameInput.length() < 2) {
            nameInputLayout.setError("Tên quá dài. Tên hợp lệ phải gồm 2-40 ký tự.");
            nameInputLayout.setErrorIconDrawable(null);
            return false;
        }
        else {
            nameInputLayout.setError(null);
            return true;
        }
    }

    @Override
    public void onPause() {
        ((RegisterViewModel)baseActivity.getViewModel()).saveNameInput(nameInputEditText.getText().toString());
        super.onPause();
    }
}
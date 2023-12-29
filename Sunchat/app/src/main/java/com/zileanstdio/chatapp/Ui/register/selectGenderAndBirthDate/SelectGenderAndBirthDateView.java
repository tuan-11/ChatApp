package com.zileanstdio.chatapp.Ui.register.selectGenderAndBirthDate;

import android.annotation.SuppressLint;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;

import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxCompoundButton;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.register.RegisterActivity;
import com.zileanstdio.chatapp.Ui.register.RegisterViewModel;
import com.zileanstdio.chatapp.Ui.register.enterPassword.EnterPasswordView;
import com.zileanstdio.chatapp.Utils.Debug;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class SelectGenderAndBirthDateView extends BaseFragment {
    private CheckBox maleCheckBox;
    private CheckBox femaleCheckBox;
    private DatePicker birthDatePicker;
    private final CompositeDisposable disposable = new CompositeDisposable();


    public SelectGenderAndBirthDateView() {
        // Required empty public constructor
    }

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
        baseActivity.setTitleToolbar(getString(R.string.genderAndBirthDate));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_select_gender_and_birth_date_view, container, false);
        return super.onCreateView(inflater, view, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListeners();
    }

    private void initView(View v) {
        maleCheckBox = v.findViewById(R.id.male_check_box);
        maleCheckBox.setChecked(false);
        femaleCheckBox = v.findViewById(R.id.female_check_box);
        femaleCheckBox.setChecked(false);
        birthDatePicker = v.findViewById(R.id.birthdate_picker);

    }

    private void setListeners() {
        disposable.add(RxCompoundButton.checkedChanges(maleCheckBox).startWithItem(true)
                        .distinctUntilChanged()
                        .subscribe(isChecked -> {
                            femaleCheckBox.setChecked(!isChecked);
                            ((RegisterActivity) baseActivity).getNextActionBtn().setEnabled(true);
                        }
        ));
        disposable.add(RxCompoundButton.checkedChanges(femaleCheckBox).startWithItem(false)
                        .distinctUntilChanged()
                        .subscribe(isChecked -> maleCheckBox.setChecked(!isChecked)
        ));

        disposable.add(RxView.clicks(((RegisterActivity) baseActivity).getNextActionBtn())
                .subscribe(unit -> {
                    String date = validateDatePicker();
                    if(date != null) {
                        ((RegisterViewModel) baseActivity.getViewModel()).saveBirthDate(date);
                        String gender = maleCheckBox.isChecked() ? "Male" : "Female";
                        ((RegisterViewModel) baseActivity.getViewModel()).saveGender(gender);
                        baseActivity.replaceFragment(new EnterPasswordView());

                    } else {
                        showSnackBar("Độ tuổi người dùng không phù hợp.", Snackbar.LENGTH_LONG);
                    }
        }));

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

    private String validateDatePicker() {
        String date = String.format("%s/%s/%s", birthDatePicker.getDayOfMonth(), birthDatePicker.getMonth() + 1, birthDatePicker.getYear());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date dateSelected = dateFormat.parse(date);
            if (dateSelected != null) {
                if(getDiffYears(dateSelected, new Date()) >= 16) {
                    return date;
                }

            }
            return null;
        } catch (ParseException e) {
            Debug.log(TAG, e.getMessage());
            return null;
        }
    }

    private int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }


    private Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
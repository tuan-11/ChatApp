package com.zileanstdio.chatapp.Ui.register;

import androidx.fragment.app.FragmentTransaction;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.register.enterName.EnterNameView;



public class RegisterActivity extends BaseActivity {
    private FloatingActionButton nextActionBtn;
    private Bundle bundle;
    private FragmentTransaction fragmentTransaction;

    public FloatingActionButton getNextActionBtn() {
        return nextActionBtn;
    }

    public FragmentTransaction getFragmentTransaction() {
        return fragmentTransaction;
    }

    @Override
    public ViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory.create(this, bundle)).get(RegisterViewModel.class);
        return viewModel;
    }


    @Override
    public Integer getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clRegisterActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_register_content_frame, fragment);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bundle = new Bundle();
        super.onCreate(savedInstanceState);
        nextActionBtn = findViewById(R.id.activity_register_next_btn);
        initAppBar();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_register_content_frame, new EnterNameView());
        fragmentTransaction.commit();
    }

    @Override
    public void initAppBar() {
        super.initAppBar();
        setTitleToolbar("Tạo tài khoản");
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {

    }

}
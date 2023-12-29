package com.zileanstdio.chatapp.Base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.ViewModel.ViewModelProviderFactory;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public abstract class BaseFragment<V extends ViewModel> extends DaggerFragment implements View.OnClickListener {
    protected final String TAG = this.getTag();

    protected V viewModel;

    public abstract V getViewModel();

    protected BaseActivity baseActivity;

    protected FrameLayout viewRoot;

    @Inject
    protected ViewModelProviderFactory providerFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getViewModel();
    }

    protected abstract void initAppBar();

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_layout, null, false);
        viewRoot = view.findViewById(R.id.baseFragmentLayout);
        viewRoot.addView(container);
        if(container != null) {
            container.setOnTouchListener((v, event) -> {
                baseActivity.hideKeyboard();
                return false;
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(baseActivity.toolbar != null) {
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewRoot.getLayoutParams();
//            params.setMargins(0, baseActivity.getSupportActionBar().getHeight(),0, 0);
//            viewRoot.setLayoutParams(params);
        }
        initAppBar();
    }

    @Override
    public void onAttach(Context context) {
        this.baseActivity = (BaseActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {

    }

    protected void showSnackBar(String msg, int duration) {
        Snackbar.make(viewRoot, msg, duration).show();
    }
}

package com.zileanstdio.chatapp.Base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.appbar.MaterialToolbar;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.ViewModel.ViewModelProviderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.rxjava3.core.Observable;

public abstract class BaseActivity<V extends ViewModel> extends DaggerAppCompatActivity implements View.OnClickListener {

    protected final String TAG = this.getClass().getSimpleName();

    protected HashMap<Integer, String> PERMISSIONS_NOT_GRANTED;

    protected final MutableLiveData<Boolean> observablePermissionsData = new MutableLiveData<>();
    private Dialog loadingDialog;

    protected V viewModel;

    public abstract V getViewModel();

    public abstract @LayoutRes Integer getLayoutId();

    public abstract @IdRes Integer getViewRootId();

    public abstract void replaceFragment(BaseFragment fragment);

    public MaterialToolbar toolbar = null;

    @Inject
    protected ViewModelProviderFactory providerFactory;

    public ViewModelProviderFactory getProviderFactory() {
        return providerFactory;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        if(getViewRootId() != null) {
            ViewGroup viewGroup = findViewById(getViewRootId());
            viewGroup.setOnTouchListener((v, event) -> {
                hideKeyboard();
                return false;
            });
        }
//        BaseApplication.getInstance().setActivityContext(this);
        createLoadingDialog();
        getViewModel();
    }

    public void initAppBar() {
        if(getViewRootId() != null) {
            ViewGroup viewGroup = findViewById(getViewRootId());
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View appBar = inflater.inflate(R.layout.layout_appbar, null);
            appBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            toolbar = appBar.findViewById(R.id.toolbar);
            setSupportActionBar(this.toolbar);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            viewGroup.addView(appBar, 0);
        }
    }

    public void setTitleToolbar(String title) {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void setNavigationIcon(@DrawableRes int resId) {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(resId);
        }
    }

    public void setDisplayShowHomeEnabled(boolean showHomeEnabled) {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(showHomeEnabled);
        }
    }
    public void setDisplayHomeAsUpEnabled(boolean homeAsUpEnabled) {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUpEnabled);
        }
    }

    private void createLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.layout_dialog_loading);
        loadingDialog.setCanceledOnTouchOutside(false);
        Window window = loadingDialog.getWindow();
        if(window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
    }

    public void showLoadingDialog(String content) {
        showLoadingDialog(content, true);
    }

    public void showLoadingDialog() {
        showLoadingDialog(null, true);
    }

    public void showLoadingDialog(boolean cancelable) {
        showLoadingDialog(null, false);
    }

    public void showLoadingDialog(String content, boolean cancelable) {
        try {
            hideKeyboard();
            if(loadingDialog != null && loadingDialog.isShowing()) {
                closeLoadingDialog();
            }
            createLoadingDialog();
            TextView loadingContent = loadingDialog.findViewById(R.id.txvContent);
            loadingContent.setText(getResources().getString(R.string.loading_text));
            if(content != null) {
                loadingContent.setText(content);
            }
            loadingDialog.setCancelable(cancelable);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }



    public void closeLoadingDialog() {
        if(loadingDialog != null) {
            try {
                loadingDialog.dismiss();
                loadingDialog = null;
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    public void showKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, 0);
            }
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public boolean handlePermissionsInitial(HashMap<Integer, String> PERMISSIONS_NOT_GRANTED) {
        List<String> permissionRequesting = new ArrayList<>();
        int REQUEST_MULTIPLE_PERMISSION_CODE = 99;
        int singleKey = REQUEST_MULTIPLE_PERMISSION_CODE;

        Iterator<Map.Entry<Integer, String>> it= PERMISSIONS_NOT_GRANTED.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Integer, String> permission = it.next();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    Environment.isExternalStorageManager() &&
                    permission.getValue().equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
            {
                Log.d(TAG,String.format("%s permission granted before", permission.getValue()));
                it.remove();

            }
            else if(checkPermission(permission.getValue())) {
                Log.d(TAG, String.format("%s permission granted before", permission.getValue()));
                it.remove();

            }
            else {
                Log.d(TAG, String.format("%s permission requesting", permission.getValue()));
                permissionRequesting.add(permission.getValue());
                singleKey = permission.getKey();

            }
        }
        String[] permissions = new String[(int)permissionRequesting.size()];
        if(permissionRequesting.size() > 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionRequesting.toArray(permissions), REQUEST_MULTIPLE_PERMISSION_CODE);

        } else if(permissionRequesting.size() > 0) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permissionRequesting.get(0).equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
            {
                handleManageStorageAndroidLargerThan10(singleKey);
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissionRequesting.toArray(permissions), singleKey);
                }
            }
        }
        return PERMISSIONS_NOT_GRANTED.size() <= 0;
    }

    public boolean checkPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void handleIfUserClickNoAskAgain(int requestCode) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Change Permissions in Settings");
        alertDialogBuilder
                .setMessage(R.string.ask_permission)
                .setCancelable(true)
                .setPositiveButton("SETTINGS", (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    Bundle bundle = new Bundle();
                    bundle.putInt("requestCode", requestCode);
                    intent.putExtras(bundle);
//                    startActivityForResult(intent, requestCode);
                    launcher.launch(intent);
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    observablePermissionsData.setValue(true);
                }
            }
    );

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        int index = 0;
        boolean ok = true;
        for(int result: grantResults) {
            if(result == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, String.format("%s PERMISSION GRANTED", permissions[index]));
            } else if(grantResults[0] == PackageManager.PERMISSION_DENIED && !shouldShowRequestPermissionRationale(permissions[0])) {
                handleIfUserClickNoAskAgain(requestCode);
                ok = false;
                return;
            }
            else {
                Log.d(TAG, String.format("%s PERMISSION DENIED", permissions[index]));
                ok = false;
            }
            index++;
        }
        if(ok) {
            observablePermissionsData.setValue(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handlePermissionResult(requestCode, permissions, grantResults);

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void handleManageStorageAndroidLargerThan10(int requestCode) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Change Permission to access to all files");
        alertDialogBuilder
                .setMessage("" + "\nWe need permission for this action, please enable to access to all files")
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    Bundle bundle = new Bundle();
                    bundle.putInt("requestCode", requestCode);
                    intent.putExtras(bundle);
//                    startActivityForResult(intent, requestCode);
                    launcher.launch(intent);
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

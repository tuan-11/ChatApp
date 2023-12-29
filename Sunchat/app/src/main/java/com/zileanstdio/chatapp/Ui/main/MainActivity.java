package com.zileanstdio.chatapp.Ui.main;


import static com.zileanstdio.chatapp.Ui.message.MessageActivity.ARG_CURRENT_USER;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.zileanstdio.chatapp.Adapter.MainViewPagerAdapter;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.call.incoming.IncomingCallActivity;
import com.zileanstdio.chatapp.Ui.register.RegisterViewModel;
import com.zileanstdio.chatapp.Ui.register.enterPassword.EnterPasswordViewModel;
import com.zileanstdio.chatapp.Ui.search.SearchActivity;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.Stringee;

import org.json.JSONObject;

public class MainActivity extends BaseActivity<MainViewModel> {
    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;
    private User currentUser;


    @Override
    public MainViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(MainViewModel.class);
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clMainActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @Override
    public void initAppBar() {
        super.initAppBar();
        setTitleToolbar("Tìm kiếm");
        setNavigationIcon(R.drawable.ic_magnifying_glass_light);
        setDisplayHomeAsUpEnabled(true);
        setDisplayShowHomeEnabled(false);
        toolbar.setNavigationOnClickListener(v -> {
            if (currentUser != null) {
                Intent startSearchActivity = new Intent(this, SearchActivity.class);
                startSearchActivity.putExtra("userName", currentUser.getUserName());
                startSearchActivity.putExtra("phoneNumber", currentUser.getPhoneNumber());
                startActivity(startSearchActivity);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppBar();
        Stringee.client = new StringeeClient(this);

        this.bottomNavigationView = findViewById(R.id.activity_main_bottom_navigation);
        this.viewPager2 = findViewById(R.id.activity_main_view_pager);
        MainViewPagerAdapter viewPagerAdapter = new MainViewPagerAdapter(this);
        this.viewPager2.setAdapter(viewPagerAdapter);

        this.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.navigiation_messages).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.navigiation_contacts).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.navigiation_profile).setChecked(true);
                        break;
                }
            }
        });
        this.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.navigiation_messages) {
                viewPager2.setCurrentItem(0);
            } else if(id == R.id.navigiation_contacts) {
                viewPager2.setCurrentItem(1);
            } else if(id == R.id.navigiation_profile) {
                viewPager2.setCurrentItem(2);
            }
            return true;
        });
        viewModel.getUserInfo().observe(this, user -> {
            currentUser = user;
        });
        viewModel.getListMutableLiveData().observe(this, contacts -> {});

        Stringee.client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {

            }

            @Override
            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {

            }

            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (Stringee.isInCall) {
                        stringeeCall.reject(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                    } else {
                        Stringee.callsMap.put(stringeeCall.getCallId(), stringeeCall);
                        Intent incomingCallIntent = new Intent(MainActivity.this, IncomingCallActivity.class);
                        incomingCallIntent.putExtra("callID", stringeeCall.getCallId());
                        incomingCallIntent.putExtra("isVideoCall", stringeeCall.isVideoCall());
                        startActivity(incomingCallIntent);
                    }
                });
            }

            @Override
            public void onIncomingCall2(StringeeCall2 stringeeCall2) {

            }

            @Override
            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                Toast.makeText(MainActivity.this, "Không thể kết nối đến máy chủ cuộc gọi", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestNewToken(StringeeClient stringeeClient) {
                viewModel.createStringeeToken(currentUser.getPhoneNumber());
            }

            @Override
            public void onCustomMessage(String s, JSONObject jsonObject) {

            }

            @Override
            public void onTopicMessage(String s, JSONObject jsonObject) {

            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
package com.zileanstdio.chatapp.Ui.request;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zileanstdio.chatapp.Adapter.RequestAdapter;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.List;

public class ListRequestActivity extends BaseActivity<ListRequestViewModel> implements ListRequestViewModel.Navigator {
    public static final String ARG_CURRENT_UID = "current_uid";
    private String currentUid;
    private RecyclerView rcvFriendRequest;
    private RequestAdapter requestAdapter;
    private List<ContactWrapInfo> infoList;

    @Override
    public ListRequestViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(ListRequestViewModel.class);
        return viewModel;
    }

    private void initView() {
        rcvFriendRequest = findViewById(R.id.rcv_friend_request);
        infoList = new ArrayList<>();
        requestAdapter = new RequestAdapter(infoList, this, viewModel);
        rcvFriendRequest.setAdapter(requestAdapter);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_list_request;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.root_view_list_request_activity;
    }

    @Override
    public void initAppBar() {
        super.initAppBar();
        setTitleToolbar("Danh sách lời mời");
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
        initAppBar();
        initView();

        viewModel.getRequestLiveData().observe(this, contactWrapInfoList -> {
            if(contactWrapInfoList != null) {
                Debug.log(TAG +":getRequestLiveData", contactWrapInfoList.toString());
                infoList.clear();
                infoList.addAll(0, contactWrapInfoList);
                requestAdapter.notifyDataSetChanged();
            }
        });

        if(getIntent() != null && getIntent().hasExtra(ARG_CURRENT_UID)) {
            currentUid = getIntent().getStringExtra(ARG_CURRENT_UID);
            if(currentUid != null) {
                Debug.log(TAG, "currentUid: " + currentUid);
                viewModel.listenRequest(currentUid);
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void acceptCallback(int position) {
        if(infoList.size() > position) {
            ContactWrapInfo contactWrapInfo = infoList.get(position);
            if(contactWrapInfo.getContact() != null) {
                viewModel.acceptRequest(currentUid, CipherUtils.Hash.sha256(contactWrapInfo.getContact().getNumberPhone()))
                    .observe(this, aBoolean -> {
                        if(aBoolean) {
                            Toast.makeText(this, "Accept thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }

    }

    @Override
    public void denyCallback(int position) {
        if(infoList.size() > position) {
            ContactWrapInfo contactWrapInfo = infoList.get(position);
            if(contactWrapInfo.getContact() != null) {
                viewModel.denyRequest(currentUid, CipherUtils.Hash.sha256(contactWrapInfo.getContact().getNumberPhone()))
                    .observe(this, aBoolean -> {
                        if(aBoolean) {
                            infoList.remove(position);
                            requestAdapter.notifyItemRemoved(position);
                            requestAdapter.notifyItemRangeChanged(position, infoList.size());
                        } else {
                            Toast.makeText(this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }
    }
}
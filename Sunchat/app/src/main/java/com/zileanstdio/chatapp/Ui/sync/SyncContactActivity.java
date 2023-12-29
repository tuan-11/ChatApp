package com.zileanstdio.chatapp.Ui.sync;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Constants;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class SyncContactActivity extends BaseActivity<SyncContactViewModel> implements SyncContactViewModel.Navigator {
    public static final String ARG_CURRENT_USER = "current_user";
    //private FragmentTransaction fragmentTransaction;

    private RecyclerView rcvSyncContact;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmer;

    private SyncContactAdapter contactAdapter;
    private List<Contact> contacts;
    private SearchView svContact;
    private MaterialTextView txvNoResult;
    private final BehaviorSubject<Boolean> shimmerListener = BehaviorSubject.createDefault(true);


    @Override
    public SyncContactViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SyncContactViewModel.class);
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_sync_contact;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clSyncContactActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        shimmer = findViewById(R.id.shimmer_view_sync_contact);
        svContact = findViewById(R.id.sv_contact);
        txvNoResult = findViewById(R.id.tv_no_result_contact);
        rcvSyncContact = findViewById(R.id.rcv_add_contact);
        rcvSyncContact.setLayoutManager(new LinearLayoutManager(this));
        rcvSyncContact.setItemAnimator(new DefaultItemAnimator());
        contactAdapter = new SyncContactAdapter(this, viewModel);
        rcvSyncContact.setAdapter(contactAdapter);
        initAppBar();

        if(getIntent() != null && getIntent().hasExtra(ARG_CURRENT_USER)) {
            User user = (User) getIntent().getSerializableExtra(ARG_CURRENT_USER);
            if(user != null) {
                viewModel.getCurrentUser().setValue(user);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Error")
                        .setMessage("Không tìm thấy người dùng hiện tại")
                        .setPositiveButton("Thoát", (dialog, which) -> {
                            finish();
                            dialog.dismiss();
                        }).show();
            }
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage("Không tìm thấy người dùng hiện tại")
                    .setPositiveButton("Thoát", (dialog, which) -> {
                        finish();
                        dialog.dismiss();
                    }).show();
        }

        shimmerListener.subscribe(new io.reactivex.rxjava3.core.Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                viewModel.getDisposable().add(d);
            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                if(aBoolean != null && aBoolean) {
                    showShimmer();
                } else {
                    hideShimmer();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        viewModel.userSyncFromContactLiveData.observe(this, stringContactWrapInfoHashMap -> {
            Debug.log("userSyncFromContactLiveData", String.valueOf(stringContactWrapInfoHashMap.size()));
            viewModel.cacheUserSyncFromContact.putAll(stringContactWrapInfoHashMap);
            shimmerListener.onNext(false);
            if(stringContactWrapInfoHashMap.size() == 0) {
                findViewById(R.id.tv_no_result_contact).setVisibility(View.VISIBLE);
                shimmerListener.onNext(false);

            } else {
                findViewById(R.id.tv_no_result_contact).setVisibility(View.GONE);
                contactAdapter.stringHashMap.clear();
                contactAdapter.submitList(new ArrayList<>(stringContactWrapInfoHashMap.values()));
                shimmerListener.onNext(false);
            }

        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if(viewModel.getLocalContact().getValue() != null) {
                shimmerListener.onNext(true);
                sync(viewModel.getLocalContact().getValue());
            }
        });
        viewModel.getLocalContact().observe(this, this::sync);

        PERMISSIONS_NOT_GRANTED = new HashMap<Integer, String>() {{
            put(Constants.READ_CONTACTS_CODE, Manifest.permission.READ_CONTACTS);
            put(Constants.WRITE_CONTACTS_CODE, Manifest.permission.WRITE_CONTACTS);
        }};
        observablePermissionsData.setValue(false);

        final Observer<Boolean> observerPermissionsNotGranted = isValid -> {
            Debug.log(TAG, "SyncContactActivity:observerPermissionNotGranted: " + isValid);
            if(isValid) {
                run();
            }
        };

        observablePermissionsData.observe(this, observerPermissionsNotGranted);

        run();
        svContact.clearFocus();
    }

    private void sync(HashMap<String, String> localContact) {
        if(viewModel.getCurrentUser().getValue() != null) {
            User currentUser = viewModel.getCurrentUser().getValue();
            String uid = CipherUtils.Hash.sha256(currentUser.getPhoneNumber());
            List<HashMap<String, String>> hashMapList = new ArrayList<>();
            int maxSize = 1;
            HashMap<String, String> temp = new HashMap<>();
            for(String key : localContact.keySet()) {
                temp.put(key, localContact.get(key));
                if(maxSize % 10 == 0) {
                    hashMapList.add(new HashMap<String, String>(){{putAll(temp);}});
                    temp.clear();
                } else {
                    if(maxSize == localContact.size()) {
                        hashMapList.add(new HashMap<String, String>(){{putAll(temp);}});
                        temp.clear();
                    }
                }
                maxSize++;
            }
            temp.clear();
            for(HashMap<String, String> hashMap : hashMapList) {
                viewModel.syncLocalContact(hashMap, uid);
            }
        } else {
            notFoundCurrentUser();
        }
    }

    private void notFoundCurrentUser() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Không tìm thấy người dùng hiện tại")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    finish();
                    dialog.dismiss();
                }).show();
    }


    public void showShimmer() {
        rcvSyncContact.setVisibility(View.GONE);
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }
    public void hideShimmer() {
        rcvSyncContact.setVisibility(View.VISIBLE);
        shimmer.setVisibility(View.GONE);
        shimmer.stopShimmer();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {

    }


    void run() {
        if(handlePermissionsInitial(PERMISSIONS_NOT_GRANTED)) {
            getContactList();
        }
    }

    @Override
    public void initAppBar() {
        super.initAppBar();
        setTitleToolbar("Danh bạ máy");
        setDisplayHomeAsUpEnabled(true);
    }

    private void getContactList() {
        Cursor cursor = null;
        HashMap<String, String> hashMapContact = new HashMap<>();
        List<Contact> localContact = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        try {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception e) {
            Log.d(TAG + ":getContactList", e.getMessage());
        }
        if(cursor != null) {

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Contact contact = new Contact();

                    @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    @SuppressLint("Range") String contactDisplayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    @SuppressLint("Range") int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    contact.setContactName(contactDisplayName);
                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                , null
                                , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                                , new String[]{contactId}
                                , null);

                        while (phoneCursor.moveToNext()) {
                            @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setNumberPhone(phoneNumber);
                        }
                        phoneCursor.close();
                    }
                    Debug.log("Local contact", contact.toString());
                    hashMapContact.put(contact.getNumberPhone(), contact.getContactName());
                }
                cursor.close();
            }
        }
        if(viewModel.getCurrentUser().getValue() != null) {
            try {
                hashMapContact.remove(viewModel.getCurrentUser().getValue().getPhoneNumber());
            } catch (Exception e) {
                Debug.log(e.getMessage() != null ? e.getMessage() : "Unknown error");
            }

        }
        viewModel.getLocalContact().setValue(hashMapContact);
    }

    @Override
    public void sendFriendRequest(int position, ContactWrapInfo contactWrapInfo, String sender) {
        contactWrapInfo.getContact().setRelationship(-1);
        viewModel.cacheUserSyncFromContact.put(contactWrapInfo.getContact().getNumberPhone(), contactWrapInfo);
        contactAdapter.submitList(new ArrayList<>(viewModel.cacheUserSyncFromContact.values()));
        contactAdapter.notifyItemChanged(position);
        viewModel.sendFriendRequest(contactWrapInfo, sender).observe(this, aBoolean -> {
            if(aBoolean) {
                Toast.makeText(this, "Gửi kết bạn thành công", Toast.LENGTH_SHORT).show();
            } else {
                contactWrapInfo.getContact().setRelationship(-2);
                viewModel.cacheUserSyncFromContact.put(contactWrapInfo.getContact().getNumberPhone(), contactWrapInfo);
                contactAdapter.submitList(new ArrayList<>(viewModel.cacheUserSyncFromContact.values()));
                contactAdapter.notifyItemChanged(position);
                Toast.makeText(this, "Đã xảy ra lỗi, thử lại sau", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
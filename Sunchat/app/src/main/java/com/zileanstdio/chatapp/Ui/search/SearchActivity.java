package com.zileanstdio.chatapp.Ui.search;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;
import com.jakewharton.rxbinding4.widget.RxSearchView;
import com.zileanstdio.chatapp.Adapter.SearchAdapter;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Utils.CipherUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility", "SetTextI18n"})
public class SearchActivity extends BaseActivity<SearchViewModel> implements SearchViewModel.Navigator {

    private Integer status = 0;
    private String userName;
    private String phoneNumber;

    private RelativeLayout layoutResult;
    private MaterialTextView txvStatus;
    private ProgressBar progressBar;
    private SearchView searchView;
    private RecyclerView rcvSearchResult;

    private SearchAdapter searchAdapter;
    private final List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        viewModel.setNavigator(this);
        initAppBar();
        setView();
        clearSearch();
        subscribeObserver();

        if (searchView != null) {
            Observable<Integer> searchInputObservable = RxSearchView.queryTextChanges(searchView)
                    .map(inputText -> specifyNumberPhone(String.valueOf(inputText)))
                    .distinctUntilChanged();

            viewModel.getDisposable().add(searchInputObservable.filter(item -> item != 0)
                    .debounce(800, TimeUnit.MILLISECONDS)
                    .subscribe(result -> {
                        users.clear();
                        new Handler(Looper.getMainLooper()).post(() -> searchAdapter.setUsers(users));
                        viewModel.search(String.valueOf(searchView.getQuery()));
                    }));

            searchView.setOnClickListener(v -> searchView.onActionViewExpanded());
        }

        layoutResult.setOnClickListener(v -> hideKeyboard());
        rcvSearchResult.setOnTouchListener((view, motionEvent) -> {
            hideKeyboard();
            return false;
        });
    }

    @Override
    public void initAppBar() {
        super.initAppBar();
        setTitleToolbar("Tìm kiếm");
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public SearchViewModel getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SearchViewModel.class);
        }
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clSearchActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @Override
    public void onClick(View view) {

    }

    public void setView() {
        layoutResult = findViewById(R.id.layout_result);
        txvStatus = findViewById(R.id.txv_status);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.search_view);
        rcvSearchResult = findViewById(R.id.rcv_search_result);

        searchAdapter = new SearchAdapter(this, viewModel);
        rcvSearchResult.setHasFixedSize(true);
        rcvSearchResult.setLayoutManager(new LinearLayoutManager(this));
        rcvSearchResult.setItemAnimator(new DefaultItemAnimator());
        rcvSearchResult.setAdapter(searchAdapter);

        userName = getIntent().getStringExtra("userName");
        viewModel.getCurrentUid().observe(this, s -> {
            if(s != null) {
                viewModel.loadContacts(s);
            }
        });
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if(phoneNumber != null) {
            viewModel.getCurrentUid().postValue(CipherUtils.Hash.sha256(phoneNumber));
        }
    }

    public void clearSearch() {
        rcvSearchResult.setVisibility(View.INVISIBLE);
        txvStatus.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        txvStatus.setText("Nhập từ khóa để tìm kiếm");
        status = 0;
    }

    public void startSearch() {
        rcvSearchResult.setVisibility(View.INVISIBLE);
        txvStatus.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        txvStatus.setText("Đang tìm kiếm...");
        status = (status == 1 ? 2 : 1);
    }

    public void searchWithData() {
        txvStatus.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        rcvSearchResult.setVisibility(View.VISIBLE);
    }

    public void searchWithoutData() {
        rcvSearchResult.setVisibility(View.INVISIBLE);
        txvStatus.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        txvStatus.setText("Không có kết quả trùng khớp");
    }

    public Integer specifyNumberPhone(String input) {
        if (!input.isEmpty()) {
            startSearch();
        } else {
            clearSearch();
        }
        return status;
    }

    public void subscribeObserver() {
        viewModel.getListUser().observe(this, user -> {
            if ((user.getUserName() == null) || (user.getPhoneNumber() == null) ) {
                searchWithoutData();
            } else {
                if (!user.getUserName().equals(userName) && !user.getPhoneNumber().equals(phoneNumber)) {

                    users.add(user);
                    searchAdapter.setUsers(users);
                    searchWithData();
                } else {
                    searchWithoutData();
                }
            }
        });
    }

    @Override
    public void sendFriendRequest(int position, ContactWrapInfo contactWrapInfo) {
        if(contactWrapInfo.getContact() == null) {
            Contact contact = new Contact(contactWrapInfo.getUser().getPhoneNumber(), null, -1, new Date());
            contactWrapInfo.setContact(contact);
        } else {
            contactWrapInfo.getContact().setRelationship(-1);
        }
        viewModel.sendFriendRequest(contactWrapInfo, phoneNumber).observe(this, aBoolean -> {
            if(aBoolean) {
                viewModel.getContactHashMap().put(contactWrapInfo.getUser().getPhoneNumber(), contactWrapInfo.getContact());
                searchAdapter.notifyItemChanged(position);
                Toast.makeText(this, "Gửi kết bạn thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã xảy ra lỗi, thử lại sau", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
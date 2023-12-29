package com.zileanstdio.chatapp.Ui.main.connections.chat;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.zileanstdio.chatapp.Adapter.ConversationAdapter;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.Conversation;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.main.MainActivity;
import com.zileanstdio.chatapp.Ui.message.MessageActivity;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.List;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;


public class ChatView extends BaseFragment<ChatViewModel> implements ChatViewModel.Navigator {
    private ShimmerFrameLayout shimmer;
    private RecyclerView rcvConversation;
    private LinearLayout viewEmpty;
    private ConversationAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final BehaviorSubject<Boolean> shimmerListener = BehaviorSubject.createDefault(true);

    public ChatView() {
        // Required empty public constructor
    }


    @Override
    public ChatViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(ChatViewModel.class);
        viewModel.setNavigator(this);
        return viewModel;
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
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.layout_chat_view, container, false);
        return super.onCreateView(inflater, viewGroup, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        shimmer = view.findViewById(R.id.shimmer_view_recent_conversation);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        rcvConversation = view.findViewById(R.id.rcv_conversation);
        viewEmpty = view.findViewById(R.id.view_empty);
        adapter = new ConversationAdapter(baseActivity, viewModel);
        rcvConversation.setLayoutManager(new LinearLayoutManager(baseActivity, LinearLayoutManager.VERTICAL, false));
        rcvConversation.setItemAnimator(new DefaultItemAnimator());
        rcvConversation.setAdapter(adapter);

        shimmerListener.subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                viewModel.getDisposable().add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                if(aBoolean != null && aBoolean) {
                    showShimmer();
                } else {
                    hideShimmer();
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Debug.log("shimmerListener:onError", e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });

        ((MainActivity) baseActivity).getViewModel().getConversationsLiveData().observe(getViewLifecycleOwner(), conversationWrappers -> {
            if(conversationWrappers.size() == 0) {
                shimmerListener.onNext(false);
            } else {
                Debug.log("getRecentConversation:onChanged", conversationWrappers.toString());
                adapter.setConversationWrapperList(conversationWrappers);
                shimmerListener.onNext(false);
            }
        });

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if ((user.getConversationList() != null) && (user.getConversationList().size() > 0)) {
                Debug.log("getCurrentUserObserver:", "conversationList.size() > 0");
                shimmerListener.onNext(true);
                viewEmpty.setVisibility(View.GONE);
                ((MainActivity) baseActivity).getViewModel().listenRecentConversation(user.getConversationList());
            } else {
                Debug.log("getCurrentUserObserver:", "shimmer listener need to be false");
                shimmerListener.onNext(false);
                viewEmpty.setVisibility(View.VISIBLE);
                ((MainActivity) baseActivity).getViewModel().listenRecentConversation(user.getConversationList());

            }
        });

        ((MainActivity) baseActivity).getViewModel().getUserInfo().observe(baseActivity, user -> {
            Debug.log("getUserInfo", user.toString());
            viewModel.getCurrentUser().setValue(user);

        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if(viewModel.getCurrentUser().getValue() != null) {
                shimmerListener.onNext(true);
                ((MainActivity) baseActivity).getViewModel().listenRecentConversation(viewModel.getCurrentUser().getValue().getConversationList());
            }
        });


    }

    public void showShimmer() {
        viewEmpty.setVisibility(View.GONE);
        rcvConversation.setVisibility(View.GONE);
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }
    public void hideShimmer() {
        viewEmpty.setVisibility(View.GONE);
        rcvConversation.setVisibility(View.VISIBLE);
        shimmer.setVisibility(View.GONE);
        shimmer.stopShimmer();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void navigateToMessage(ConversationWrapper conversationWrapper, Contact contact, User contactProfile) {
        Intent intent = new Intent(baseActivity, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MessageActivity.ARG_CONVERSATION_WRAPPER, conversationWrapper);
        if(viewModel.getCurrentUser().getValue() != null) {
            String uid = CipherUtils.Hash.sha256(viewModel.getCurrentUser().getValue().getPhoneNumber());
            bundle.putSerializable(MessageActivity.ARG_CURRENT_UID, uid);
            bundle.putSerializable(MessageActivity.ARG_CURRENT_USER, viewModel.getCurrentUser().getValue());
        }
        bundle.putSerializable(MessageActivity.ARG_CONTACT, contact);
        bundle.putSerializable(MessageActivity.ARG_CONTACT_PROFILE, contactProfile);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
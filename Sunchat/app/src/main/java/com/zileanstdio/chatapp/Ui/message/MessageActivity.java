package com.zileanstdio.chatapp.Ui.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.common.eventbus.EventBus;
import com.jakewharton.rxbinding4.view.RxView;
import com.zileanstdio.chatapp.Adapter.MessageAdapter;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.Conversation;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.Message;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.call.outgoing.OutgoingCallActivity;
import com.zileanstdio.chatapp.Ui.main.MainActivity;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.Stringee;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MessageActivity extends BaseActivity<MessageViewModel> {
    public static final String ARG_CURRENT_UID = "current_uid";
    public static final String ARG_CONVERSATION_WRAPPER = "conversation_wrapper";
    public static final String ARG_CONTACT = "contact";
    public static final String ARG_CURRENT_USER = "current_user";
    public static final String ARG_CONTACT_PROFILE = "contact_profile";
    private RecyclerView rcvMessage;
    private MessageAdapter messageAdapter;
    private String currentUid;
    private ConversationWrapper currentConversationWrapper = null;
    private Contact contact = null;
    private User contactProfile = null;
    private TextInputEditText edtMessage;
    private MaterialButton btnSendMessage;
    private ShapeableImageView imvAvatarContact;
    private ShapeableImageView viewStatus;
    private MaterialTextView txvContactName;
    private MaterialTextView txvOnlineStatus;

    private MaterialButton btnCallVoice, btnCallVideo;
    private BroadcastReceiver broadcastReceiver;

    @Override
    public MessageViewModel getViewModel() {
        if(viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(MessageViewModel.class);
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_message;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clMessageActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @Override
    public void initAppBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppBar();
        rcvMessage = findViewById(R.id.rcv_message);
        edtMessage = findViewById(R.id.edt_message);
        btnSendMessage = findViewById(R.id.btn_send_message);
        imvAvatarContact = findViewById(R.id.imv_avatar);
        viewStatus = findViewById(R.id.view_status);
        txvContactName = findViewById(R.id.txv_contact_name);
        txvOnlineStatus = findViewById(R.id.txv_online_status);
        btnCallVoice = findViewById(R.id.btn_call_voice);
        btnCallVideo = findViewById(R.id.btn_call_video);
        messageAdapter = new MessageAdapter(this, viewModel);
        rcvMessage.setAdapter(messageAdapter);

        observeMessages();
        if(getIntent() != null && getIntent().hasExtra(ARG_CONVERSATION_WRAPPER) && getIntent().hasExtra(ARG_CURRENT_UID)) {
            currentUid = getIntent().getStringExtra(ARG_CURRENT_UID);
            currentConversationWrapper = (ConversationWrapper) getIntent().getSerializableExtra(ARG_CONVERSATION_WRAPPER);
            contact = (Contact) getIntent().getSerializableExtra(ARG_CONTACT);
            contactProfile = (User) getIntent().getSerializableExtra(ARG_CONTACT_PROFILE);
            viewModel.getContactProfileLiveData().observe(this, user -> {
                initial(user);
                viewModel.setCurrentUser(user);
            });
            if(contact != null && contact.getContactName() != null) {
                Debug.log("contact", contact.toString());
                viewModel.getContactLiveData().setValue(contact);
            }
            if(currentUid != null) {
                viewModel.getUidLiveData().setValue(currentUid);
            }
            if(contactProfile != null && contactProfile.getUserName() != null) {
                Debug.log("contactProfile", contactProfile.toString());
                // Use for testing
                viewModel.getLatestInfoContact(CipherUtils.Hash.sha256(contactProfile.getPhoneNumber()))
                    .observe(this, user -> {
                        viewModel.getContactProfileLiveData().setValue(contactProfile);
                    });

            }
            if(currentConversationWrapper != null && currentConversationWrapper.getDocumentId() != null) {
                Debug.log("currentConversationWrapper", currentConversationWrapper.toString());
                viewModel.getMessageList(currentConversationWrapper.getDocumentId());
            }
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case OutgoingCallActivity.SEND_CALL_MESSAGE_SUCCESS:
                        if(intent.hasExtra("conversation") && intent.getSerializableExtra("conversation") != null) {
                            viewModel.getConversationLiveData().postValue((ConversationWrapper) intent.getSerializableExtra("conversation"));
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        viewModel.getConversationLiveData().observe(this, conversationWrapper -> {
            if(conversationWrapper != null) {
                if(currentConversationWrapper == null || !Objects.equals(conversationWrapper.getDocumentId(), currentConversationWrapper.getDocumentId())) {
                    Debug.log("getConversationLiveData", "true condition");
                    currentConversationWrapper = conversationWrapper;
                    viewModel.getMessageList(currentConversationWrapper.getDocumentId());
                } else {
                    Debug.log("getConversationLiveData", "else condition");
                    currentConversationWrapper = conversationWrapper;
                }
            }

        });

        btnSendMessage.setOnClickListener(v -> {
            if(edtMessage.getText() != null && edtMessage.getText().toString().trim().length() > 0) {
                Message message = new Message(currentUid,
                        Conversation.Type.TEXT.label,
                        edtMessage.getText().toString().trim(),
                        new Date());
                if(currentConversationWrapper != null) {
                    Debug.log("listenSendEvent", "currentConversationWrapper != null");
                    currentConversationWrapper.getConversation().setTypeMessage(Conversation.Type.TEXT.label);
                    currentConversationWrapper.getConversation().setLastSender(currentUid);
                    currentConversationWrapper.getConversation().setLastMessage(edtMessage.getText().toString().trim());
                    edtMessage.getText().clear();
                    currentConversationWrapper.getConversation().setLastUpdated(new Date());
                    viewModel.sendMessage(currentConversationWrapper, message);
                } else {
                    Debug.log("listenSendEvent", "currentConversationWrapper = null");
                    Conversation conversation = Conversation.TEXT;
                    conversation.setLastMessage(edtMessage.getText().toString().trim());
                    edtMessage.getText().clear();
                    conversation.setLastSender(currentUid);
                    conversation.setLastUpdated(new Date());
                    conversation.setCreatedAt(new Date());
                    conversation.setUserJoined(new ArrayList<String>(){{
                        add(currentUid);
                        add(CipherUtils.Hash.sha256(contactProfile.getPhoneNumber()));
                    }});

                    final ConversationWrapper conversationWrapper = new ConversationWrapper(null, conversation);
                    viewModel.sendMessage(conversationWrapper, message);
                }

            }
        });

        viewModel.getDisposable().add(RxView.clicks(btnCallVoice).subscribe(unit -> {
            if(Stringee.client.isConnected()) {
                Intent intent = new Intent(this, OutgoingCallActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtra("from", Stringee.client.getUserId());
                intent.putExtra("to", "." + contactProfile.getPhoneNumber());
                intent.putExtra("video", false);
                intent.putExtra("isVideoCall", false);
                bundle.putSerializable("contact", new ContactWrapInfo(contact, contactProfile));
                bundle.putSerializable("conversation", currentConversationWrapper);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }));

        viewModel.getDisposable().add(RxView.clicks(btnCallVideo).subscribe(unit -> {

            if(Stringee.client.isConnected()) {
                Intent intent = new Intent(this, OutgoingCallActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtra("from", Stringee.client.getUserId());
                intent.putExtra("to", "." + contactProfile.getPhoneNumber());
                intent.putExtra("video", true);
                intent.putExtra("isVideoCall", true);
                bundle.putSerializable("contact", new ContactWrapInfo(contact, contactProfile));
                bundle.putSerializable("conversation", currentConversationWrapper);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }));
    }

    private void observeMessages() {
        viewModel.getMessagesLiveData().observe(this, messageWrappers -> {
            Debug.log("observeMessages:onChanged", messageWrappers.toString());
            Debug.log("observeMessages:onChanged", String.valueOf(messageWrappers.size()));
            messageAdapter.submitList(messageWrappers);
            rcvMessage.smoothScrollToPosition(messageWrappers.size() - 1);
        });
    }

    private void initial(User user) {
        MessageActivity.this.runOnUiThread(() -> {
            if(user != null) {
                txvContactName.setText(user.getUserName());
                if(user.getAvatarImageUrl() != null && !user.getAvatarImageUrl().isEmpty()) {
                    Log.d("UploadImage", "Image URL updated successfully" + user.getAvatarImageUrl());
                    Glide.with(this)
                            .load(user.getAvatarImageUrl())
                            .error(R.drawable.ic_default_user)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imvAvatarContact);
                } else {
                    imvAvatarContact.setImageResource(R.drawable.ic_default_user);
                }
                toolbar.findViewById(R.id.view_status).setVisibility(user.isOnlineStatus() ? View.VISIBLE : View.GONE);
                toolbar.findViewById(R.id.view_status).requestLayout();

            }
            if(viewModel.getContactLiveData().getValue() != null && viewModel.getContactLiveData().getValue().getContactName() != null) {
                txvContactName.setText(viewModel.getContactLiveData().getValue().getContactName());
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(OutgoingCallActivity.SEND_CALL_MESSAGE_SUCCESS));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
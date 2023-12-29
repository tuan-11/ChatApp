package com.zileanstdio.chatapp.Ui.call.outgoing;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;
import com.stringee.call.StringeeCall;
import com.stringee.common.StringeeAudioManager;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.zileanstdio.chatapp.Base.BaseActivity;
import com.zileanstdio.chatapp.Base.BaseFragment;
import com.zileanstdio.chatapp.BaseApplication;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.Conversation;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.Message;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Common;
import com.zileanstdio.chatapp.Utils.Debug;
import com.zileanstdio.chatapp.Utils.SensorUtils;
import com.zileanstdio.chatapp.Utils.Stringee;

import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OutgoingCallActivity extends BaseActivity<OutgoingCallViewModel> implements OutgoingCallViewModel.Navigator {
    public static final String SEND_CALL_MESSAGE_SUCCESS = "send_call_message_success";
    private final int REQUEST_PERMISSION = 1;
    private final String START = "Kết nối cuộc gọi";
    private final String STARTED = "Cuộc gọi đi";
    private final String RING = "Đang đổ chuông...";
    private final String RECONNECT = "Đang kết nối lại...";
    private final String BUSY = "Máy bận";
    private final String END = "Cuộc gọi đã kết thúc";

    private String userTo;
    private String id;
    private ConversationWrapper conversationWrapper = null;
    private ContactWrapInfo contactWrapInfo = null;
    private LocalDateTime startTime = null;
    private boolean isVideoCall = false;

    private boolean isGranted = true;
    private boolean isNeverAskAgain = false;
    private boolean isSpeaker = false;
    private boolean isMute = false;
    private boolean isVideo = false;

    private FrameLayout layoutLocal, layoutRemote;
    private LinearLayout layoutControl;
    private ShapeableImageView imvAvatar;
    private MaterialTextView txvUser, txvStatus;
    private ImageButton imgBtnSwitch;
    private ImageButton imgBtnSpeaker, imgBtnMute, imgBtnVideo, imgBtnEnd;

    private StringeeCall stringeeCall;
    private StringeeAudioManager stringeeAudioManager;
    private StringeeCall.SignalingState signalingState;
    private StringeeCall.MediaState mediaState;
    private SensorUtils sensorUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        setContentView(R.layout.activity_outgoing_call);
        viewModel.setNavigator(this);

        sensorUtils = SensorUtils.getInstance(this);
        sensorUtils.acquireProximitySensor(getLocalClassName());

        Stringee.isInCall = true;

        subscribeObserver();
        initView();
        initAction();
        viewModel.setUserTo(userTo.substring(1));

        if (!checkPermission()) {
            return;
        }
        calling();
    }

    @Override
    public OutgoingCallViewModel getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(OutgoingCallViewModel.class);
        }
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_outgoing_call;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clOutgoingCallActivity;
    }

    @Override
    public void replaceFragment(BaseFragment fragment) {

    }

    @Override
    public void onClick(View view) {

    }

    public void initView() {
        layoutLocal = findViewById(R.id.layout_local);
        layoutRemote = findViewById(R.id.layout_remote);
        imgBtnSwitch = findViewById(R.id.img_btn_switch);

        imvAvatar = findViewById(R.id.imv_avatar);
        txvUser = findViewById(R.id.txv_user);
        txvStatus = findViewById(R.id.txv_status);

        layoutControl = findViewById(R.id.layout_control);
        imgBtnSpeaker = findViewById(R.id.img_btn_speaker);
        imgBtnMute = findViewById(R.id.img_btn_mute);
        imgBtnVideo = findViewById(R.id.img_btn_video);
        imgBtnEnd = findViewById(R.id.img_btn_end);
    }

    public void initAction() {

        if(getIntent().hasExtra("to")) {
            userTo = getIntent().getStringExtra("to");
        }
        if(getIntent().hasExtra("from")) {
            id = getIntent().getStringExtra("from");
        }
        if(getIntent().hasExtra("conversation")) {
            conversationWrapper = (ConversationWrapper) getIntent().getSerializableExtra("conversation");
        }
        if(getIntent().hasExtra("contact")) {
            contactWrapInfo = (ContactWrapInfo) getIntent().getSerializableExtra("contact");
        }
//        conversationWrapper = (ConversationWrapper) getIntent().getSerializableExtra("conversation");
        isVideoCall = getIntent().getBooleanExtra("video", false);

        isSpeaker = isVideoCall;
        isVideo = isVideoCall;

        imgBtnSpeaker.setBackgroundResource(isSpeaker ? R.drawable.btn_speaker_on : R.drawable.btn_speaker_off);
        imgBtnVideo.setImageResource(isVideo ? R.drawable.btn_video : R.drawable.btn_video_off);

        imgBtnSwitch.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        imgBtnVideo.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        imgBtnSwitch.setOnClickListener(v -> {
            if (stringeeCall != null) {
                stringeeCall.switchCamera(new StatusListener() {

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        Toast.makeText(OutgoingCallActivity.this, "Không thể chuyển camera", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        imgBtnSpeaker.setOnClickListener(v -> {
            isSpeaker = !isSpeaker;
            imgBtnSpeaker.setBackgroundResource(isSpeaker ? R.drawable.btn_speaker_on : R.drawable.btn_speaker_off);

            if (stringeeAudioManager != null) {
                stringeeAudioManager.setSpeakerphoneOn(isSpeaker);
            }
        });
        imgBtnMute.setOnClickListener(v -> {
            isMute = !isMute;
            imgBtnMute.setBackgroundResource(isMute ? R.drawable.btn_mute : R.drawable.btn_mic);

            if (stringeeCall != null) {
                stringeeCall.mute(isMute);
            }
        });
        imgBtnVideo.setOnClickListener(v -> {
            isVideo = !isVideo;
            imgBtnVideo.setImageResource(isVideo ? R.drawable.btn_video : R.drawable.btn_video_off);

            if (stringeeCall != null) {
                stringeeCall.enableVideo(isVideo);
            }
        });
        imgBtnEnd.setOnClickListener(v -> {
            txvStatus.setText(END);
            ending();
        });
    }

    public boolean checkPermission() {
        List<String> listPermission = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                listPermission.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            listPermission.add(Manifest.permission.RECORD_AUDIO);
        }
        if (isVideoCall) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                listPermission.add(Manifest.permission.CAMERA);
            }
        }

        if (listPermission.size() > 0) {
            String[] permissions = listPermission.toArray(new String[0]);
            requestPermissions(permissions, REQUEST_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    isGranted = false;
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        isNeverAskAgain = true;
                    }
                    break;
                } else {
                    isGranted = true;
                }
            }
        }

        if ((requestCode == REQUEST_PERMISSION) && (!isGranted)) {
            txvStatus.setText(END);
            clear();
        } else {
            calling();
        }
    }

    private void calling() {
        stringeeAudioManager = StringeeAudioManager.create(OutgoingCallActivity.this);
        stringeeAudioManager.start((selectedAudioDevice, availableAudioDevices) -> {});
        stringeeAudioManager.setSpeakerphoneOn(isVideoCall);

        stringeeCall = new StringeeCall(Stringee.client, getIntent().getStringExtra("from"), userTo);
        stringeeCall.setVideoCall(isVideoCall);

        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {

            @Override
            public void onSignalingStateChange(StringeeCall stringeeCall, final StringeeCall.SignalingState state, String reason, int sipCode, String sipReason) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Debug.log(TAG + ":onSignalingStateChange", signalingState != null ? signalingState.toString() : "null");
                    signalingState = state;
                    switch (state) {
                        case RINGING:
                            txvStatus.setText(RING);
                            break;
                        case ANSWERED:
                            txvStatus.setText(START);
                            if (mediaState == StringeeCall.MediaState.CONNECTED) {
                                txvStatus.setText(STARTED);
                            }
                            break;
                        case BUSY:
                            txvStatus.setText(BUSY);
                            ending();
                            break;
                        case ENDED:
                            txvStatus.setText(END);
                            ending();
                            break;
                    }
                });
            }

            @Override
            public void onError(StringeeCall stringeeCall, int code, String desc) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Debug.log(TAG + ":callListener:onError", stringeeCall != null ? stringeeCall.toString() : "null");
                    txvStatus.setText(END);
                    clear();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String desc) {

            }

            @Override
            public void onMediaStateChange(StringeeCall stringeeCall, final StringeeCall.MediaState state) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Debug.log(TAG + ":onMediaStateChange", state != null ? state.toString() : "null");
                    mediaState = state;
                    if (mediaState == StringeeCall.MediaState.CONNECTED) {
                        if (signalingState == StringeeCall.SignalingState.ANSWERED) {
                            txvStatus.setText(STARTED);
                            imvAvatar.setVisibility(isVideoCall ? View.GONE : View.VISIBLE);
                            txvUser.setVisibility(isVideoCall ? View.GONE : View.VISIBLE);
                            txvStatus.setVisibility(isVideoCall ? View.GONE : View.VISIBLE);

                            startTime = LocalDateTime.now();
                        }
                    } else {
                        txvStatus.setText(RECONNECT);
                    }
                });
            }

            @Override
            public void onLocalStream(final StringeeCall stringeeCall) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Debug.log(TAG + ":onLocalStream", stringeeCall != null ? stringeeCall.toString() : "null");
                    if (stringeeCall.isVideoCall()) {
                        layoutLocal.removeAllViews();
                        layoutLocal.addView(stringeeCall.getLocalView());
                        stringeeCall.renderLocalView(true);
                    }
                });
            }

            @Override
            public void onRemoteStream(final StringeeCall stringeeCall) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (stringeeCall.isVideoCall()) {
                        layoutRemote.removeAllViews();
                        layoutRemote.addView(stringeeCall.getRemoteView());
                        stringeeCall.renderRemoteView(false);
                    }
                });
            }

            @Override
            public void onCallInfo(StringeeCall stringeeCall, final JSONObject jsonObject) {

            }
        });

        stringeeCall.makeCall(new StatusListener() {

            @Override
            public void onSuccess() {

            }
        });
    }

    private void ending() {
        stringeeCall.hangup(new StatusListener() {

            @Override
            public void onSuccess() {

            }
        });

        String times;
        LocalDateTime endTime = null;
        if (startTime != null) {
            endTime = LocalDateTime.now();
//            times = Duration.between(startTime, endTime).getSeconds() + " giây";
        }
//            times = 0 + " giây";


        Debug.log("startCall", startTime != null ? startTime.toString() : "null");
        Debug.log("endCall", endTime != null ? endTime.toString() : "null");
        String time = Common.computeCallTime(
                startTime != null ? Date.from(startTime.toInstant(ZoneOffset.UTC)) : null,
                endTime != null ? Date.from(endTime.toInstant(ZoneOffset.UTC)) : null,
                this
        );
        Debug.log("timeCall", time);
        Date dateSend = new Date();
        Message message = new Message(CipherUtils.Hash.sha256(id.substring(1)),
                isVideo ? Conversation.Type.VIDEO_CALL.label : Conversation.Type.CALL.label,
                time,
                dateSend);
        if(conversationWrapper != null) {
            conversationWrapper.getConversation().setTypeMessage(isVideo ? Conversation.Type.VIDEO_CALL.label : Conversation.Type.CALL.label);
            conversationWrapper.getConversation().setLastSender(CipherUtils.Hash.sha256(id.substring(1)));
            conversationWrapper.getConversation().setLastMessage(time);
            conversationWrapper.getConversation().setLastUpdated(dateSend);
            viewModel.sendCallMessage(conversationWrapper, message);
        } else {
            Conversation newConversation = Conversation.TEXT;
            newConversation.setLastMessage(time);
            newConversation.setLastSender(CipherUtils.Hash.sha256(id.substring(1)));
            newConversation.setLastUpdated(new Date());
            newConversation.setCreatedAt(new Date());
            newConversation.setUserJoined(new ArrayList<String>(){{
                add(CipherUtils.Hash.sha256(id.substring(1)));
                if(contactWrapInfo != null && contactWrapInfo.getUser() != null && contactWrapInfo.getUser().getPhoneNumber() != null) {
                    add(CipherUtils.Hash.sha256(contactWrapInfo.getUser().getPhoneNumber()));
                } else {
                    add(CipherUtils.Hash.sha256(userTo.substring(1)));
                }
            }});

            final ConversationWrapper conversationWrapper = new ConversationWrapper(null, newConversation);
            viewModel.sendCallMessage(conversationWrapper, message);
        }
        clear();
    }

    private void clear() {
        if (stringeeAudioManager != null) {
            stringeeAudioManager.stop();
            stringeeAudioManager = null;
        }
        sensorUtils.releaseSensor();

        imgBtnSwitch.setVisibility(View.GONE);
        layoutControl.setVisibility(View.GONE);
        imgBtnEnd.setVisibility(View.GONE);

        Common.postDelay(() -> {
            Stringee.isInCall = false;
            finish();
        }, isNeverAskAgain ? 5000 : 1000);
    }

    public void subscribeObserver() {
        viewModel.getUser().observe(this, user -> {
            if (user.getAvatarImageUrl() != null) {
                Picasso.get().load(user.getAvatarImageUrl()).into(imvAvatar);
            } else {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            }

            if (user.getUserName() != null) {
                txvUser.setText(user.getUserName());
            } else {
                txvUser.setText(userTo.substring(1));
            }
        });
    }

    @Override
    public void notifySendCallMessageSuccess(ConversationWrapper conversationWrapper) {
        if(conversationWrapper != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("conversation", conversationWrapper);
            Intent broadcast = new Intent(SEND_CALL_MESSAGE_SUCCESS)
                    .putExtras(bundle);
            LocalBroadcastManager.getInstance(BaseApplication.getInstance()).sendBroadcast(broadcast);
        }
    }
}
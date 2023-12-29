package com.zileanstdio.chatapp.Ui.call.incoming;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Utils.Common;
import com.zileanstdio.chatapp.Utils.SensorUtils;
import com.zileanstdio.chatapp.Utils.Stringee;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IncomingCallActivity extends BaseActivity {

    private final int REQUEST_PERMISSION = 1;
    private final String START = "Kết nối cuộc gọi";
    private final String STARTED = "Cuộc gọi đến";
    private final String RECONNECT = "Đang kết nối lại...";
    private final String END = "Cuộc gọi đã kết thúc";

    private String userFrom;
    private boolean isVideoCall = false;
    private boolean isGranted = true;
    private boolean isNeverAskAgain = false;
    private boolean isSpeaker = false;
    private boolean isMute = false;
    private boolean isVideo = false;

    private FrameLayout layoutLocal, layoutRemote;
    private LinearLayout layoutControl, layoutIncoming;
    private ShapeableImageView imvAvatar;
    private MaterialTextView txvUser, txvStatus;
    private ImageButton imgBtnSwitch;
    private ImageButton imgBtnAnswer, imgBtnReject;
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
        setContentView(R.layout.activity_incoming_call);

        sensorUtils = SensorUtils.getInstance(this);
        sensorUtils.acquireProximitySensor(getLocalClassName());

        Stringee.isInCall = true;
        String callId = getIntent().getStringExtra("callID");
        stringeeCall = Stringee.callsMap.get(callId);

        if (stringeeCall == null) {
            sensorUtils.releaseSensor();
            Common.postDelay(() -> {
                Stringee.isInCall = false;
                finish();
            }, 1000);
            return;
        }

        subscribeObserver();
        initView();
        initAction();
        ((IncomingCallViewModel) viewModel).setUserFrom(userFrom.substring(1));

        if (!checkPermission()) {
            return;
        }
        ringing();
    }

    @Override
    public ViewModel getViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(IncomingCallViewModel.class);
        }
        return viewModel;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.activity_incoming_call;
    }

    @Override
    public Integer getViewRootId() {
        return R.id.clIncomingCallActivity;
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

        layoutIncoming = findViewById(R.id.layout_incoming);
        imgBtnAnswer = findViewById(R.id.img_btn_answer);
        imgBtnReject = findViewById(R.id.img_btn_reject);

        layoutControl = findViewById(R.id.layout_control);
        imgBtnSpeaker = findViewById(R.id.img_btn_speaker);
        imgBtnMute = findViewById(R.id.img_btn_mute);
        imgBtnVideo = findViewById(R.id.img_btn_video);
        imgBtnEnd = findViewById(R.id.img_btn_end);
    }

    public void initAction() {
        userFrom = stringeeCall.getFrom();
        isVideoCall = stringeeCall.isVideoCall();
        isSpeaker = isVideoCall;
        isVideo = isVideoCall;

        txvUser.setText(userFrom);
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
                        Toast.makeText(IncomingCallActivity.this, "Không thể chuyển camera", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        boolean isVideoCall = getIntent().getBooleanExtra("isVideoCall", false);
        if (isVideoCall) {
            imgBtnAnswer.setImageResource(R.drawable.bg_btn_answer_video_call_selected);
        }else {
            imgBtnAnswer.setImageResource(R.drawable.bg_btn_answer_call_selected);
        }

        imgBtnAnswer.setOnClickListener(v -> {
            if (stringeeCall != null) {
                layoutIncoming.setVisibility(View.GONE);
                layoutControl.setVisibility(View.VISIBLE);
                imgBtnEnd.setVisibility(View.VISIBLE);
                imgBtnSwitch.setVisibility(isVideoCall ? View.VISIBLE : View.GONE);

                stringeeCall.answer(new StatusListener() {
                    @Override
                    public void onSuccess() {

                    }
                });
            }
        });
        imgBtnReject.setOnClickListener(v -> ending(false));
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
        imgBtnEnd.setOnClickListener(v -> ending(true));
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
            ending(false);
        } else {
            ringing();
        }
    }

    private void ringing() {
        stringeeAudioManager = StringeeAudioManager.create(this);
        stringeeAudioManager.start((selectedAudioDevice, availableAudioDevices) -> {});
        stringeeAudioManager.setSpeakerphoneOn(isVideo);

        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {

            @Override
            public void onSignalingStateChange(StringeeCall stringeeCall, StringeeCall.SignalingState state, String s, int i, String s1) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    signalingState = state;
                    if (signalingState == StringeeCall.SignalingState.ANSWERED) {
                        txvStatus.setText(START);
                        if (mediaState == StringeeCall.MediaState.CONNECTED) {
                            txvStatus.setText(STARTED);
                        }
                    } else if (signalingState == StringeeCall.SignalingState.ENDED) {
                        ending(true);
                    }
                });
            }

            @Override
            public void onError(StringeeCall stringeeCall, int i, String s) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    txvStatus.setText(END);
                    clear();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall stringeeCall, StringeeCall.SignalingState signalingState, String s) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (signalingState != StringeeCall.SignalingState.RINGING) {
                        txvStatus.setText(END);
                        clear();
                    }
                });
            }

            @Override
            public void onMediaStateChange(StringeeCall stringeeCall, StringeeCall.MediaState state) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    mediaState = state;
                    if (mediaState == StringeeCall.MediaState.CONNECTED) {
                        if (signalingState == StringeeCall.SignalingState.ANSWERED) {
                            txvStatus.setText(STARTED);
                            imvAvatar.setVisibility(isVideoCall ? View.GONE : View.VISIBLE);
                            txvUser.setVisibility(isVideoCall ? View.GONE : View.VISIBLE);
                            txvStatus.setVisibility(isVideoCall ? View.GONE : View.VISIBLE);
                        }
                    } else {
                        txvStatus.setText(RECONNECT);
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall stringeeCall) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isVideoCall) {
                        layoutLocal.removeAllViews();
                        layoutLocal.addView(stringeeCall.getLocalView());
                        stringeeCall.renderLocalView(true);
                    }
                });
            }

            @Override
            public void onRemoteStream(StringeeCall stringeeCall) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isVideoCall) {
                        layoutRemote.removeAllViews();
                        layoutRemote.addView(stringeeCall.getRemoteView());
                        stringeeCall.renderRemoteView(false);
                    }
                });
            }

            @Override
            public void onCallInfo(StringeeCall stringeeCall, JSONObject jsonObject) {

            }
        });

        stringeeCall.ringing(new StatusListener() {

            @Override
            public void onSuccess() {

            }
        });
    }

    private void ending(boolean isHangup) {
        txvStatus.setText(END);
        if (stringeeCall != null) {
            if (isHangup) {
                stringeeCall.hangup(new StatusListener() {

                    @Override
                    public void onSuccess() {

                    }
                });
            } else {
                stringeeCall.reject(new StatusListener() {

                    @Override
                    public void onSuccess() {

                    }
                });
            }
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
        layoutIncoming.setVisibility(View.GONE);
        layoutControl.setVisibility(View.GONE);
        imgBtnEnd.setVisibility(View.GONE);

        Common.postDelay(() -> {
            Stringee.isInCall = false;
            finish();
        }, isNeverAskAgain ? 5000 : 1000);
    }

    public void subscribeObserver() {
        ((IncomingCallViewModel) viewModel).getUser().observe(this, user -> {
            if (user.getAvatarImageUrl() != null) {
                Picasso.get().load(user.getAvatarImageUrl()).into(imvAvatar);
            } else {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            }

            if (user.getUserName() != null) {
                txvUser.setText(user.getUserName());
            } else {
                txvUser.setText(userFrom.substring(1));
            }
        });
    }
}
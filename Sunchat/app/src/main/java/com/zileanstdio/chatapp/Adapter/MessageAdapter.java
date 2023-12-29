package com.zileanstdio.chatapp.Adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.Message;
import com.zileanstdio.chatapp.Data.model.MessageWrapper;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.message.MessageActivity;
import com.zileanstdio.chatapp.Ui.message.MessageViewModel;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Common;
import com.zileanstdio.chatapp.Utils.Constants;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_SEND_MESSAGE = 1;
    public static final int VIEW_TYPE_RECEIVE_MESSAGE = 2;
    public static final int VIEW_TYPE_SEND_CALL = 3;
    public static final int VIEW_TYPE_RECEIVE_CALL = 4;
    public static final int VIEW_TYPE_SEND_VIDEO_CALL = 5;
    public static final int VIEW_TYPE_RECEIVE_VIDEO_CALL = 6;

    private User currentUser;

    final Context context;
    final MessageViewModel viewModel;
    final AsyncListDiffer<MessageWrapper> messageAsyncListDiffer;
    final DiffUtil.ItemCallback<MessageWrapper> diffCallback = new DiffUtil.ItemCallback<MessageWrapper>() {
        @Override
        public boolean areItemsTheSame(@NonNull MessageWrapper oldItem, @NonNull MessageWrapper newItem) {
            return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull MessageWrapper oldItem, @NonNull MessageWrapper newItem) {
            return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId()) && oldItem.getMessage().equals(newItem.getMessage());
        }
    };

    public MessageAdapter(Context context, MessageViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.messageAsyncListDiffer = new AsyncListDiffer<>(this, diffCallback);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_SEND_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_sender_role, parent, false);
            return new SendMessageViewHolder(view);
        } else if(viewType == VIEW_TYPE_RECEIVE_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_receiver_role, parent, false);
            return new ReceiverMessageViewHolder(view);
        } else if(viewType == VIEW_TYPE_SEND_CALL || viewType == VIEW_TYPE_SEND_VIDEO_CALL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_call_sender_role, parent, false);
            return new SendCallViewHolder(view);
        } else if(viewType == VIEW_TYPE_RECEIVE_CALL || viewType == VIEW_TYPE_RECEIVE_VIDEO_CALL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_call_receiver_role, parent, false);
            return new ReceiveCallViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageWrapper messageWrapper = getItem(position);

        viewModel.getCurrentUserLiveData().observe((LifecycleOwner) context, user -> {

            if(getItemViewType(position) == VIEW_TYPE_SEND_MESSAGE) {
                ((SendMessageViewHolder) holder).bindData(messageWrapper.getMessage());
            } else if(getItemViewType(position) == VIEW_TYPE_RECEIVE_MESSAGE) {
                ((ReceiverMessageViewHolder) holder).bindData(messageWrapper.getMessage(), user);
            } else if(getItemViewType(position) == VIEW_TYPE_SEND_CALL) {
                ((SendCallViewHolder) holder).bindData(messageWrapper.getMessage());
            } else if(getItemViewType(position) == VIEW_TYPE_RECEIVE_CALL) {
                ((ReceiveCallViewHolder) holder).bindData(messageWrapper.getMessage(), user);
            } else if(getItemViewType(position) == VIEW_TYPE_SEND_VIDEO_CALL) {
                ((SendCallViewHolder) holder).bindData(messageWrapper.getMessage());
            } else if(getItemViewType(position) == VIEW_TYPE_RECEIVE_VIDEO_CALL) {
                ((ReceiveCallViewHolder) holder).bindData(messageWrapper.getMessage(), user);
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        MessageWrapper messageWrapper = getItem(position);
        boolean sender = messageWrapper.getMessage().getSender().equals(viewModel.getUidLiveData().getValue());
        if(messageWrapper.getMessage().getType().equals(Constants.KEY_TYPE_TEXT)) {
            return sender ? VIEW_TYPE_SEND_MESSAGE : VIEW_TYPE_RECEIVE_MESSAGE;
        } else if(messageWrapper.getMessage().getType().equals(Constants.KEY_TYPE_CALL)) {
            return sender ? VIEW_TYPE_SEND_CALL : VIEW_TYPE_RECEIVE_CALL;
        } else if(messageWrapper.getMessage().getType().equals(Constants.KEY_TYPE_VIDEO_CALL)) {
            return sender ? VIEW_TYPE_SEND_VIDEO_CALL : VIEW_TYPE_RECEIVE_VIDEO_CALL;
        }
        return super.getItemViewType(position);

    }

    public void submitList(List<MessageWrapper> messageWrapperList) {
        messageAsyncListDiffer.submitList(messageWrapperList);
        notifyItemInserted(messageWrapperList.size() - 1);
    }

    @Override
    public int getItemCount() {
        return messageAsyncListDiffer.getCurrentList().size();
    }

    public MessageWrapper getItem(int position) {
        return messageAsyncListDiffer.getCurrentList().get(position);
    }

    class SendMessageViewHolder extends RecyclerView.ViewHolder {
        final MaterialTextView txvMessage;
        final MaterialTextView txvDateSend;
        final MaterialCardView cvContainerMessage;

        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txvMessage = itemView.findViewById(R.id.txv_message);
            txvDateSend = itemView.findViewById(R.id.txv_date_send);
            cvContainerMessage = itemView.findViewById(R.id.cv_container_message);
            txvDateSend.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void bindData(Message message) {
            txvMessage.setText(message.getMessage());
            txvDateSend.setText(Common.getReadableTime(message.getSendAt()));
            cvContainerMessage.setOnClickListener(v -> {
                showHideDateSendAnim();
            });
        }

        private void showHideDateSendAnim() {
            txvDateSend.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ValueAnimator animator = null;
            if(txvDateSend.getHeight() == 0) {
                animator = ValueAnimator.ofInt(0, txvDateSend.getMeasuredHeight());
            } else {
                animator = ValueAnimator.ofInt(txvDateSend.getMeasuredHeight(), 0);
            }
            animator.addUpdateListener(animation -> {
                txvDateSend.getLayoutParams().height = (int) animation.getAnimatedValue();
                txvDateSend.requestLayout();
            });
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }
    }

    class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        final MaterialTextView txvMessage;
        final MaterialTextView txvDateSend;
        final ShapeableImageView imvAvatar;
        final MaterialCardView cvContainerMessage;

        public ReceiverMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txvMessage = itemView.findViewById(R.id.txv_message);
            txvDateSend = itemView.findViewById(R.id.txv_date_send);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            cvContainerMessage = itemView.findViewById(R.id.cv_container_message);
        }

        public void bindData(Message message, User contactProfile) {
            if(contactProfile.getAvatarImageUrl() == null) {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            } else {
                Glide.with(context)
                        .load(contactProfile.getAvatarImageUrl())
                        .error(R.drawable.ic_default_user)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imvAvatar);
            }
            txvMessage.setText(message.getMessage());
            txvDateSend.setText(Common.getReadableTime(message.getSendAt()));
            cvContainerMessage.setOnClickListener(v -> {
                txvDateSend.setVisibility(txvDateSend.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

    }

    class SendCallViewHolder extends RecyclerView.ViewHolder {
        final MaterialTextView txvTitleCallType;
        final MaterialTextView txvDateSend;
        final MaterialCardView cvContainerMessage;
        final MaterialButton iconCallType;
        final MaterialTextView txvCallTime;

        public SendCallViewHolder(@NonNull View itemView) {
            super(itemView);
            txvTitleCallType = itemView.findViewById(R.id.txv_title_call_type);
            txvDateSend = itemView.findViewById(R.id.txv_date_send);
            cvContainerMessage = itemView.findViewById(R.id.cv_container_message);
            iconCallType = itemView.findViewById(R.id.icon_call_type);
            txvCallTime = itemView.findViewById(R.id.txv_call_time);
        }

        public void bindData(Message message) {
            if(message.getType().equals(Constants.KEY_TYPE_CALL)) {
                txvTitleCallType.setText(context.getString(R.string.title_outgoing_call));
                iconCallType.setIconResource(R.drawable.ic_phone_solid);
            } else {
                txvTitleCallType.setText(context.getString(R.string.title_outgoing_video_call));
                iconCallType.setIconResource(R.drawable.ic_video_solid);
            }
            txvCallTime.setText(message.getMessage());
            txvDateSend.setText(Common.getReadableTime(message.getSendAt()));
            cvContainerMessage.setOnClickListener(v -> {
                showHideDateSendAnim();
            });
        }

        private void showHideDateSendAnim() {
            txvDateSend.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ValueAnimator animator = null;
            if(txvDateSend.getHeight() == 0) {
                animator = ValueAnimator.ofInt(0, txvDateSend.getMeasuredHeight());
            } else {
                animator = ValueAnimator.ofInt(txvDateSend.getMeasuredHeight(), 0);
            }
            animator.addUpdateListener(animation -> {
                txvDateSend.getLayoutParams().height = (int) animation.getAnimatedValue();
                txvDateSend.requestLayout();
            });
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }
    }

    class ReceiveCallViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView txvTitleCallType;
        final MaterialTextView txvDateSend;
        final MaterialCardView cvContainerMessage;
        final MaterialButton iconCallType;
        final MaterialTextView txvCallTime;
        final ShapeableImageView imvAvatar;

        public ReceiveCallViewHolder(@NonNull View itemView) {
            super(itemView);
            txvTitleCallType = itemView.findViewById(R.id.txv_title_call_type);
            txvDateSend = itemView.findViewById(R.id.txv_date_send);
            cvContainerMessage = itemView.findViewById(R.id.cv_container_message);
            iconCallType = itemView.findViewById(R.id.icon_call_type);
            txvCallTime = itemView.findViewById(R.id.txv_call_time);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);

        }

        public void bindData(Message message, User contactProfile) {
            if(contactProfile.getAvatarImageUrl() == null) {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            } else {
                Glide.with(context)
                        .load(contactProfile.getAvatarImageUrl())
                        .error(R.drawable.ic_default_user)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imvAvatar);
            }
            if(message.getType().equals(Constants.KEY_TYPE_CALL)) {
                txvTitleCallType.setText(context.getString(R.string.title_incoming_call));
                iconCallType.setIconResource(R.drawable.ic_phone_solid);
            } else {
                txvTitleCallType.setText(context.getString(R.string.title_incoming_video_call));
                iconCallType.setIconResource(R.drawable.ic_video_solid);
            }
            txvCallTime.setText(message.getMessage());
            txvDateSend.setText(Common.getReadableTime(message.getSendAt()));

        }
    }


}

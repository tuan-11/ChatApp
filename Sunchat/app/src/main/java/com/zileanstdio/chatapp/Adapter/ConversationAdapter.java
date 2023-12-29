package com.zileanstdio.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.Conversation;
import com.zileanstdio.chatapp.Data.model.ConversationWrapper;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.main.connections.chat.ChatViewModel;
import com.zileanstdio.chatapp.Utils.CipherUtils;
import com.zileanstdio.chatapp.Utils.Common;
import com.zileanstdio.chatapp.Utils.Constants;
import com.zileanstdio.chatapp.Utils.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.subjects.BehaviorSubject;


public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    final Context context;
    final ChatViewModel viewModel;
    private List<ConversationWrapper> conversationWrapperList = new ArrayList<>();

    private boolean isSelected;


    private final AsyncListDiffer<ConversationWrapper> asyncListDiffer;

    public ConversationAdapter(Context context, ChatViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        DiffUtil.ItemCallback<ConversationWrapper> diffCallback = new DiffUtil.ItemCallback<ConversationWrapper>() {

            @Override
            public boolean areItemsTheSame(@NonNull ConversationWrapper oldItem, @NonNull ConversationWrapper newItem) {
                return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ConversationWrapper oldItem, @NonNull ConversationWrapper newItem) {
                return Objects.equals(oldItem.getDocumentId(), newItem.getDocumentId()) && oldItem.getConversation().equals(newItem.getConversation());
            }
        };
        asyncListDiffer = new AsyncListDiffer<>(this, diffCallback);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setConversationWrapperList(List<ConversationWrapper> conversationWrapperList) {
        this.conversationWrapperList = conversationWrapperList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recent_conversation_item, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        final int index = position;
        BehaviorSubject<User> userBehaviorSubject = BehaviorSubject.createDefault(new User());
        BehaviorSubject<Contact> contactBehaviorSubject = BehaviorSubject.createDefault(new Contact());
        ConversationWrapper conversationWrapper = conversationWrapperList.get(position);
        viewModel.getCurrentUser().observe((LifecycleOwner) holder.itemView.getContext(), user -> {
            for(String uid : conversationWrapper.getConversation().getUserJoined()) {
                if(!uid.equals(CipherUtils.Hash.sha256(user.getPhoneNumber()))) {
                    viewModel.getUserFromUid(uid).observe((LifecycleOwner)
                            holder.itemView.getContext(), userBehaviorSubject::onNext);
                    Debug.log("NumberPhone: " + CipherUtils.Hash.sha256(uid));
                    viewModel.getContact(CipherUtils.Hash.sha256(user.getPhoneNumber()), CipherUtils.Hash.sha256(uid))
                                    .observe((LifecycleOwner) holder.itemView.getContext(), contactBehaviorSubject::onNext);
                }
            }
        });

        viewModel.getDisposable().add(
            BehaviorSubject.combineLatest(userBehaviorSubject, contactBehaviorSubject, (user, contact) -> user != null || contact != null)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(aBoolean -> {
                    if(aBoolean) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            holder.bindData(index, conversationWrapper, contactBehaviorSubject.getValue(), userBehaviorSubject.getValue());
                        });
                    }
                })
        );

    }

    public void submitList(List<ConversationWrapper> conversationWrappers) {
        asyncListDiffer.submitList(conversationWrappers);
    }

    public ConversationWrapper getItem(int position) {
        return asyncListDiffer.getCurrentList().get(position);
    }

    @Override
    public int getItemCount() {
        return conversationWrapperList.size();
    }


    public class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ShapeableImageView imvAvatar;
        private ShapeableImageView viewStatus;
        final MaterialTextView txvLastMessage;
        final MaterialTextView txvName;
        final MaterialTextView txvDateSend;
        final MaterialCardView rootView;
        final View view;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            viewStatus = itemView.findViewById(R.id.view_status);
            txvName = itemView.findViewById(R.id.txv_name);
            txvLastMessage = itemView.findViewById(R.id.tv_last_message);
            txvDateSend = itemView.findViewById(R.id.tv_date_send);
            rootView = itemView.findViewById(R.id.cv_conversation_item);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void bindData(int position, ConversationWrapper conversationWrapper, Contact contact, User contactProfile) {
            Debug.log("bindData");
            if(contact.getContactName() != null) {
                this.txvName.setText(contact.getContactName());
            } else {
                this.txvName.setText(contactProfile.getUserName());
            }


            if(conversationWrapper.getConversation().getTypeMessage().equals(Constants.KEY_TYPE_CALL)) {
                if(conversationWrapper.getConversation().getLastSender().equals(CipherUtils.Hash.sha256(contactProfile.getPhoneNumber()))) {
                    txvLastMessage.setText(context.getString(R.string.title_incoming_call));
                } else {
                    txvLastMessage.setText(context.getString(R.string.title_outgoing_call));
                }
            } else if(conversationWrapper.getConversation().getTypeMessage().equals(Constants.KEY_TYPE_VIDEO_CALL)) {
                if(conversationWrapper.getConversation().getLastSender().equals(CipherUtils.Hash.sha256(contactProfile.getPhoneNumber()))) {
                    txvLastMessage.setText(context.getString(R.string.title_incoming_video_call));
                } else {
                    txvLastMessage.setText(context.getString(R.string.title_outgoing_video_call));
                }
            } else {
                String lastMessage = conversationWrapper.getConversation().getLastMessage();
                if(conversationWrapper.getConversation().getLastSender().equals(CipherUtils.Hash.sha256(contactProfile.getPhoneNumber()))) {
                    txvLastMessage.setText(lastMessage);
                } else {
                    txvLastMessage.setText("Bạn: " + lastMessage);
                }
            }
            this.txvDateSend.setText(Common.getReadableTime(conversationWrapper.getConversation().getLastUpdated()));
            if(contactProfile.getAvatarImageUrl() == null) {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            } else {
                Glide.with(context)
                    .load(contactProfile.getAvatarImageUrl())
                    .error(R.drawable.ic_default_user)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imvAvatar);
            }

            rootView.setOnClickListener(v -> {
                viewModel.getNavigator().navigateToMessage(conversationWrapper, contact, contactProfile);
            });
        }


        @Override
        public void onClick(View v) {

        }
    }

}

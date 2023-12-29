package com.zileanstdio.chatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.request.ListRequestViewModel;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ContactWrapInfo> infoList;
    private final Context context;
    private final ListRequestViewModel viewModel;

    public RequestAdapter(List<ContactWrapInfo> infoList, Context context, ListRequestViewModel viewModel) {
        this.infoList = infoList;
        this.context = context;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactWrapInfo wrapInfo = infoList.get(position);
        ((RequestViewHolder) holder).bindData(position,wrapInfo);
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imvAvatar;
        private final MaterialTextView txvContactName;
        private final MaterialTextView txvUserName;
        private final MaterialButton btnAccept;
        private final MaterialButton btnDeny;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            txvContactName = itemView.findViewById(R.id.tv_contact_name);
            txvUserName = itemView.findViewById(R.id.tv_user_name);
            btnAccept = itemView.findViewById(R.id.btn_accept_request);
            btnDeny = itemView.findViewById(R.id.btn_deny_request);
        }

        public void bindData(int position, ContactWrapInfo contactWrapInfo) {
            if(contactWrapInfo.getContact().getContactName() != null) {
                this.txvContactName.setText(contactWrapInfo.getContact().getContactName());
            } else if(contactWrapInfo.getUser().getUserName() != null ) {
                this.txvContactName.setText(contactWrapInfo.getUser().getUserName());
            }

            if(contactWrapInfo.getUser().getPhoneNumber() != null) {
                this.txvUserName.setText(contactWrapInfo.getUser().getPhoneNumber());
            }

            if(contactWrapInfo.getUser().getAvatarImageUrl() == null) {
                imvAvatar.setImageResource(R.drawable.ic_default_user);
            } else {
                Glide.with(context)
                        .load(contactWrapInfo.getUser().getAvatarImageUrl())
                        .error(R.drawable.ic_default_user)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imvAvatar);
            }
            btnAccept.setOnClickListener(v -> {
                viewModel.getNavigator().acceptCallback(position);
            });
            btnDeny.setOnClickListener(v -> {
                viewModel.getNavigator().denyCallback(position);
            });
        }
    }
}


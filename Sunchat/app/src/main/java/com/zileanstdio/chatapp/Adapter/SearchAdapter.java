package com.zileanstdio.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;
import com.zileanstdio.chatapp.Data.model.Contact;
import com.zileanstdio.chatapp.Data.model.ContactWrapInfo;
import com.zileanstdio.chatapp.Data.model.User;
import com.zileanstdio.chatapp.R;
import com.zileanstdio.chatapp.Ui.search.SearchViewModel;

import java.util.List;

@SuppressLint("SetTextI18n")
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<User> users;
    private final Context context;
    private final SearchViewModel viewModel;

    public SearchAdapter(Context context, SearchViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUsers(List<User> users){
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_result_item, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position) {
        User user = users.get(position);
        Contact contact = null;
        if(viewModel.getContactHashMap().size() > 0 && viewModel.getContactHashMap().containsKey(user.getPhoneNumber())) {
            contact = viewModel.getContactHashMap().get(user.getPhoneNumber());
            switch (contact.getRelationship()) {
                case 1:
                    holder.txvIsFriend.setText("Đã là bạn");
                    holder.txvIsFriend.setVisibility(View.VISIBLE);
                    holder.btnAddFriend.setVisibility(View.GONE);
                    break;
                case -1:
                    holder.txvIsFriend.setText("");
                    holder.txvIsFriend.setVisibility(View.VISIBLE);
                    holder.btnAddFriend.setEnabled(false);
                    holder.btnAddFriend.setText("Đã gửi");
                    holder.btnAddFriend.setVisibility(View.VISIBLE);
                    break;
                case 0:
                    holder.txvIsFriend.setText("Đang chờ kết bạn");
                    holder.txvIsFriend.setVisibility(View.VISIBLE);
                    holder.btnAddFriend.setVisibility(View.GONE);
                    break;
                default:
                    holder.txvIsFriend.setText("");
                    holder.txvIsFriend.setVisibility(View.GONE);
                    holder.btnAddFriend.setEnabled(true);
                    holder.btnAddFriend.setText("Kết bạn");
                    holder.btnAddFriend.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            holder.txvIsFriend.setText("");
            holder.txvIsFriend.setVisibility(View.GONE);
            holder.btnAddFriend.setEnabled(true);
            holder.btnAddFriend.setText("Kết bạn");
            holder.btnAddFriend.setVisibility(View.VISIBLE);
        }
        if ((user.getAvatarImageUrl() != null) && !user.getAvatarImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getAvatarImageUrl() != null ? user.getAvatarImageUrl() : R.drawable.ic_default_user)
                    .error(R.drawable.ic_default_user)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imvAvatar);
        } else {
            holder.imvAvatar.setImageResource(R.drawable.ic_default_user);
        }
        if ((user.getUserName() != null) && !user.getUserName().isEmpty()) {
            holder.txvName.setText(user.getUserName());
        } else {
            holder.txvName.setText("Người dùng Sunchat");
        }
        Contact finalContact = contact;
        holder.btnAddFriend.setOnClickListener(v -> {
            viewModel.getNavigator().sendFriendRequest(position, new ContactWrapInfo(finalContact, user));
        });
        holder.txvPhoneNumber.setText(user.getPhoneNumber());
    }


    @Override
    public int getItemCount() {
        if (users != null) {
            return users.size();
        }
        return 0;
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        public ShapeableImageView imvAvatar;
        public MaterialTextView txvName;
        public MaterialTextView txvPhoneNumber;
        public MaterialButton btnAddFriend;
        public MaterialTextView txvIsFriend;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            txvName = itemView.findViewById(R.id.txv_name);
            txvPhoneNumber = itemView.findViewById(R.id.txv_phone);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend);
            txvIsFriend = itemView.findViewById(R.id.txv_in_friend_relationship);
        }
    }
}
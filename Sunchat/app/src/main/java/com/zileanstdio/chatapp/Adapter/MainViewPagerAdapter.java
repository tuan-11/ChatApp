package com.zileanstdio.chatapp.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zileanstdio.chatapp.Ui.main.connections.chat.ChatView;
import com.zileanstdio.chatapp.Ui.main.connections.contact.ContactView;
import com.zileanstdio.chatapp.Ui.main.connections.profile.ProfileView;

public class MainViewPagerAdapter extends FragmentStateAdapter {
    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new ContactView();
            case 2:
                return new ProfileView();
            default:
                return new ChatView();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

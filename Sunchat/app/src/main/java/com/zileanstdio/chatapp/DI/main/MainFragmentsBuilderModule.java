package com.zileanstdio.chatapp.DI.main;

import com.zileanstdio.chatapp.Ui.main.connections.chat.ChatView;
import com.zileanstdio.chatapp.Ui.main.connections.contact.ContactView;
import com.zileanstdio.chatapp.Ui.main.connections.profile.ProfileView;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract ChatView contributeChatView();

    @ContributesAndroidInjector
    abstract ContactView contributeContactView();

    @ContributesAndroidInjector
    abstract ProfileView contributeProfileView();

}

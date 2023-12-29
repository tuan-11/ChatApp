package com.zileanstdio.chatapp.DI.message;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.message.MessageViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

// TODO: 12/11/2022

@Module
public abstract class MessageViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MessageViewModel.class)
    public abstract ViewModel bindMessageViewModel(MessageViewModel viewModel);
}

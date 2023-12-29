package com.zileanstdio.chatapp.DI.call;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.call.outgoing.OutgoingCallViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class OutgoingCallViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(OutgoingCallViewModel.class)
    public abstract ViewModel bindOutgoingCallViewModel(OutgoingCallViewModel viewModel);
}
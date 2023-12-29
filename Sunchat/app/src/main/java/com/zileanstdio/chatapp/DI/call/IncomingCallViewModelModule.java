package com.zileanstdio.chatapp.DI.call;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.call.incoming.IncomingCallViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class IncomingCallViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(IncomingCallViewModel.class)
    public abstract ViewModel bindIncomingCallViewModel(IncomingCallViewModel viewModel);
}
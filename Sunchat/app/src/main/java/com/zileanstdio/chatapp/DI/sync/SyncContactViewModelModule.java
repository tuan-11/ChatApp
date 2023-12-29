package com.zileanstdio.chatapp.DI.sync;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.sync.SyncContactViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SyncContactViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SyncContactViewModel.class)
    public abstract ViewModel bindSyncContactViewModel(SyncContactViewModel viewModel);
}

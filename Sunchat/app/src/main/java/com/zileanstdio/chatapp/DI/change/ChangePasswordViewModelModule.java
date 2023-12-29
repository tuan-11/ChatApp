package com.zileanstdio.chatapp.DI.change;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.change.ChangePasswordViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ChangePasswordViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordViewModel.class)
    public abstract ViewModel bindViewModel(ChangePasswordViewModel viewModel);
}
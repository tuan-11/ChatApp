package com.zileanstdio.chatapp.DI.start;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.start.StartViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class StartViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(StartViewModel.class)
    public abstract ViewModel bindStartViewModel(StartViewModel viewModel);
}
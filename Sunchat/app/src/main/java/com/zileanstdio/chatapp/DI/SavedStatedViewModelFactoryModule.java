package com.zileanstdio.chatapp.DI;

import androidx.lifecycle.AbstractSavedStateViewModelFactory;

import com.zileanstdio.chatapp.ViewModel.SavedStateViewModelProviderFactory;


import dagger.Binds;
import dagger.Module;


@Module
public abstract class SavedStatedViewModelFactoryModule {

    @Binds
    public abstract AbstractSavedStateViewModelFactory bindSavedStateViewModelFactory(SavedStateViewModelProviderFactory factory);

}

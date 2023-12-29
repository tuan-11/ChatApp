package com.zileanstdio.chatapp.DI;

import androidx.lifecycle.ViewModelProvider;

import com.zileanstdio.chatapp.ViewModel.ViewModelProviderFactory;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ViewModelFactoryModule {

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelProviderFactory factory);

}

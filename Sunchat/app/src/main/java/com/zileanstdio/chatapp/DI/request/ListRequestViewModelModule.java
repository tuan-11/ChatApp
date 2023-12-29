package com.zileanstdio.chatapp.DI.request;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.request.ListRequestViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ListRequestViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ListRequestViewModel.class)
    public abstract ViewModel bindListRequestViewModel(ListRequestViewModel viewModel);
}

package com.zileanstdio.chatapp.DI.register;

import androidx.lifecycle.ViewModel;

import com.zileanstdio.chatapp.DI.ViewModelKey;
import com.zileanstdio.chatapp.Ui.register.RegisterViewModel;
import com.zileanstdio.chatapp.Ui.register.enterName.EnterNameViewModel;
import com.zileanstdio.chatapp.Ui.register.enterPassword.EnterPasswordViewModel;
import com.zileanstdio.chatapp.Ui.register.enterPhoneNumber.EnterPhoneNumberViewModel;
import com.zileanstdio.chatapp.Ui.register.verifyOtp.VerifyOtpViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class RegisterViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(RegisterViewModel.class)
    public abstract ViewModel bindRegisterViewModel(RegisterViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EnterNameViewModel.class)
    public abstract ViewModel bindEnterNameViewModel(EnterNameViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EnterPhoneNumberViewModel.class)
    public abstract ViewModel bindEnterPhoneNumberViewModel(EnterPhoneNumberViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(VerifyOtpViewModel.class)
    public abstract ViewModel bindVerifyOtpViewModel(VerifyOtpViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EnterPasswordViewModel.class)
    public abstract ViewModel bindEnterPasswordViewModel(EnterPasswordViewModel viewModel);
}

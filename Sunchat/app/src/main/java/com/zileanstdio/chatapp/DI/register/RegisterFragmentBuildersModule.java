package com.zileanstdio.chatapp.DI.register;

import com.zileanstdio.chatapp.DI.register.enterName.EnterNameModule;
import com.zileanstdio.chatapp.DI.register.enterNumberPhone.EnterPhoneNumberModule;
import com.zileanstdio.chatapp.DI.register.verifyOtp.VerifyOtpModule;
import com.zileanstdio.chatapp.Ui.register.enterName.EnterNameView;
import com.zileanstdio.chatapp.Ui.register.enterPassword.EnterPasswordView;
import com.zileanstdio.chatapp.Ui.register.enterPhoneNumber.EnterPhoneNumberView;
import com.zileanstdio.chatapp.Ui.register.selectGenderAndBirthDate.SelectGenderAndBirthDateView;
import com.zileanstdio.chatapp.Ui.register.verifyOtp.VerifyOtpView;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class RegisterFragmentBuildersModule {

    @ContributesAndroidInjector(modules = {
        EnterNameModule.class
    })
    abstract EnterNameView contributeEnterNameView();

    @ContributesAndroidInjector(modules = {
        EnterPhoneNumberModule.class
    })
    abstract EnterPhoneNumberView contributeEnterPhoneNumberView();

    @ContributesAndroidInjector(modules = {
        VerifyOtpModule.class
    })
    abstract VerifyOtpView contributeVerifyOtpView();

    @ContributesAndroidInjector
    abstract SelectGenderAndBirthDateView contributeSelectGenderAndBirthDateView();

    @ContributesAndroidInjector
    abstract EnterPasswordView contributeEnterPasswordView();

}

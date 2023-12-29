package com.zileanstdio.chatapp.ViewModel;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.Module;

public class SavedStateViewModelProviderFactory extends AbstractSavedStateViewModelFactory {
    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> creators;

    @Inject
    public SavedStateViewModelProviderFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> creators) {
        this.creators = creators;
    }

    public SavedStateViewModelProviderFactory(@NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs, Map<Class<? extends ViewModel>, Provider<ViewModel>> creators) {
        super(owner, defaultArgs);
        this.creators = creators;
    }


    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        Provider<? extends ViewModel> creator = creators.get(modelClass);
        if(creator != null) {
            for(Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> entry: creators.entrySet()) {
                if(modelClass.isAssignableFrom(entry.getKey())) {
                    creator = entry.getValue();
                    break;
                }
            }
        }
        if(creator == null) {
            throw new IllegalArgumentException("Unknown class " + modelClass);
        }

        try {
            return (T) creator.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

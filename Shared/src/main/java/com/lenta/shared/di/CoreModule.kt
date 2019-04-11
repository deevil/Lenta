package com.lenta.shared.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides


@Module
class CoreModule(val application: Application) {
    @Provides
    fun provideAppContext() = application.applicationContext!!

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }


}
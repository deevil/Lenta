package com.lenta.shared.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.lenta.shared.features.network_state.INetworkStateMonitor
import com.lenta.shared.features.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class CoreModule(val application: Application) {
    @Provides
    fun provideAppContext() = application.applicationContext!!

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideForegroundActivityProvider() = ForegroundActivityProvider()

    @Provides
    @Singleton
    fun provideNetworkStateMonitor() = NetworkStateMonitor()

    @Provides
    @Singleton
    fun provideINetworkStateMonitor(networkStateReceiver: NetworkStateMonitor): INetworkStateMonitor = networkStateReceiver

}
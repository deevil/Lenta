package com.lenta.shared.di

import android.content.Context
import android.content.SharedPreferences
import com.lenta.shared.features.network_state.INetworkStateMonitor
import com.lenta.shared.features.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [CoreModule::class])
interface CoreComponent {
    fun getSharedPreferences(): SharedPreferences
    fun getAppContext(): Context
    fun getForegroundActivityProvider(): ForegroundActivityProvider
    fun getNetworkStateMonitor(): NetworkStateMonitor
    fun getINetworkStateMonitor(): INetworkStateMonitor
}
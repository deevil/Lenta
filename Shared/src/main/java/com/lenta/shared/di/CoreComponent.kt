package com.lenta.shared.di

import android.content.Context
import android.content.SharedPreferences
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [CoreModule::class])
interface CoreComponent {
    fun getSharedPreferences(): SharedPreferences
    fun getAppContext(): Context
    fun getForegroundActivityProvider(): ForegroundActivityProvider
}
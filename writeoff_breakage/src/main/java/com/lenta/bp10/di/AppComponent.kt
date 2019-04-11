package com.lenta.bp10.di

import com.lenta.bp10.MainActivity
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FeatureScope
import dagger.Component

@Component(dependencies = [CoreComponent::class])
@FeatureScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
}


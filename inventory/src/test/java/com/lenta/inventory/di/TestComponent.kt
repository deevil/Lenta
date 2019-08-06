package com.lenta.inventory.di

import com.lenta.inventory.ProcessExciseAlcoProductServiceTest
import com.lenta.inventory.ProcessGeneralProductServiceTest
import com.lenta.shared.di.AppScope
import dagger.Component

@AppScope
@Component(modules = [TestModule::class])
interface TestComponent {
    fun inject(it: ProcessExciseAlcoProductServiceTest)
    fun inject(it: ProcessGeneralProductServiceTest)
}
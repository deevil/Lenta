package com.lenta.inventory.di

import com.lenta.inventory.MyTest
import com.lenta.inventory.ProcessExciseAlcoProductServiceTest
import com.lenta.shared.di.AppScope
import dagger.Component

@AppScope
@Component(modules = [TestModule::class])
interface TestComponent {
    fun inject(it: MyTest)
    fun inject(it: ProcessExciseAlcoProductServiceTest)

}
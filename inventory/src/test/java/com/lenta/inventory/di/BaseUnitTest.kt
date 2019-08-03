package com.lenta.inventory.di

import org.junit.Before


open class BaseUnitTest {

    companion object {
        internal lateinit var component: TestComponent
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        component = DaggerTestComponent.create()
    }

    fun getComponent(): TestComponent {
        return component
    }
}
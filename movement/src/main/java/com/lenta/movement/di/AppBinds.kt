package com.lenta.movement.di

import com.lenta.movement.features.task.basket.info.BasketPropertiesExtractor
import com.lenta.movement.features.task.basket.info.IBasketPropertiesExtractor
import com.lenta.movement.platform.IResourceManager
import com.lenta.movement.platform.navigation.ResourceManager
import com.lenta.shared.di.AppScope
import dagger.Binds
import dagger.Module

@Module
interface AppBinds {
    @AppScope
    @Binds
    fun bindResourceManger(resourceManager: ResourceManager): IResourceManager

    @AppScope
    @Binds
    fun bindBasketPropertiesExtractor(basketPropertiesExtractor: BasketPropertiesExtractor): IBasketPropertiesExtractor
}
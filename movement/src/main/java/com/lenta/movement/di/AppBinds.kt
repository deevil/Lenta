package com.lenta.movement.di

import com.lenta.movement.features.task.basket.info.BasketPropertiesExtractor
import com.lenta.movement.features.task.basket.info.IBasketPropertiesExtractor
import com.lenta.shared.di.AppScope
import dagger.Binds
import dagger.Module

@Module
interface AppBinds {
    @AppScope
    @Binds
    fun bindBasketPropertiesExtractor(basketPropertiesExtractor: BasketPropertiesExtractor): IBasketPropertiesExtractor
}
package com.lenta.movement.features.task.basket.info

import com.lenta.movement.models.*
import com.lenta.movement.platform.IResourcesManager
import javax.inject.Inject

class BasketPropertiesExtractor @Inject constructor(
        private val resourcesManager: IResourcesManager,
        private val taskManager: ITaskManager
) : IBasketPropertiesExtractor {
    override suspend fun extractProperties(basket: Basket?): List<BasketProperty> {
        if (basket == null) return listOf()

        val properties = mutableListOf<BasketProperty>()
        val diffSigns = taskManager.getTaskSettings().signsOfDiv
        for (diffSign in diffSigns) {
            if (diffSign.isGisControl()) continue
            properties.add(basket.convertToBasketPropertiesByDiffSign(diffSign))
        }
        return properties
    }


    private fun Basket.convertToBasketPropertiesByDiffSign(diffSign: GoodsSignOfDivision): BasketProperty {
        return BasketProperty(diffSign.toDescriptionResId(), getPropertyWithDiffSign(diffSign))
    }

    private fun Basket.getPropertyWithDiffSign(diffSign: GoodsSignOfDivision): String {
        return if (diffSign.needStringValue) {
            this.getStringDescription(diffSign)
        } else {
            getDivisionTitle(diffSign)
        }
    }

    private fun Basket.getDivisionTitle(diffSign: GoodsSignOfDivision): String {
        return if (isDivisionTrue(diffSign)) {
            resourcesManager.yesTitle
        } else {
            resourcesManager.noTitle
        }
    }
}
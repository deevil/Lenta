package com.lenta.movement.models.memory

import com.lenta.movement.models.Basket
import com.lenta.movement.models.GoodsSignOfDivision
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.shared.fmp.resources.dao_ext.getEoVolume
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.models.core.Supplier
import com.mobrun.plugin.api.HyperHive

class MemoryTaskBasketsRepository(
    hyperHive: HyperHive,
    private val taskManager: ITaskManager
): ITaskBasketsRepository {

    private val zmpUtz14V001 = ZmpUtz14V001(hyperHive)

    private val basketList = mutableListOf<Basket>()

    override fun getBasketByIndex(basketIndex: Int): Basket {
        return basketList[basketIndex]
    }

    override fun getAll(): List<Basket> {
        return basketList
    }

    override fun removeBasket(basket: Basket) {
        basketList.remove(basket)
    }

    override fun removeProductFromAllBaskets(product: ProductInfo) {
        basketList.forEach { basket ->
            basket.remove(product)

            if (basket.isEmpty()) {
                removeBasket(basket)
            }
        }
    }

    override fun removeProduct(basketIndex: Int, product: ProductInfo) {
       basketList[basketIndex]
    }

    override fun addProduct(product: ProductInfo, supplier: Supplier?, count: Int) {
        if (count == 0) return

        val suitableBasket = getSuitableBasketOrCreate(product, supplier)

        suitableBasket[product] = (suitableBasket[product] ?: 0) + 1

        addOrReplaceIfExist(suitableBasket)

        addProduct(product, supplier, count - 1)
    }

    override fun getSuitableBasketOrCreate(
        productInfo: ProductInfo,
        supplier: Supplier?
    ): Basket {
        return basketList.lastOrNull { basket ->
            basket.isSuitableForProduct(productInfo, supplier)
        } ?: Basket(
            index = basketList.size,
            volume = zmpUtz14V001.getEoVolume() ?: error("Объем корзины отсутствует"),
            supplier = supplier
        )
    }

    private fun Basket.isSuitableForProduct(
        productInfo: ProductInfo,
        selectedSupplier: Supplier?
    ): Boolean {
        if (isEmpty()) return true

        if (productInfo.volume > freeVolume) return false

        return taskManager.getTask().settings.signsOfDiv.all { signOfDivision ->
            when (signOfDivision) {
                GoodsSignOfDivision.ALCO -> {
                    keys.first().isAlco == productInfo.isAlco
                }
                GoodsSignOfDivision.LIF_NUMBER -> {
                    supplier?.code == selectedSupplier?.code
                }
                GoodsSignOfDivision.MATERIAL_NUMBER -> {
                    keys.first().materialNumber == productInfo.materialNumber
                }
                GoodsSignOfDivision.VET -> {
                    keys.first().isVet == productInfo.isVet
                }
                GoodsSignOfDivision.FOOD -> {
                    keys.first().isFood == productInfo.isFood
                }
                GoodsSignOfDivision.MARK_PARTS -> TODO()
                GoodsSignOfDivision.USUAL -> TODO()
                GoodsSignOfDivision.PARTS -> TODO()
                GoodsSignOfDivision.MTART -> TODO()
            }
        }
    }

    private fun addOrReplaceIfExist(basket: Basket) {
        if (basketList.contains(basket)) {
            basketList[basket.index] = basket
        } else {
            basketList.add(basket.index, basket)
        }
    }

}
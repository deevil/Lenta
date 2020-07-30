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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryTaskBasketsRepository(
    hyperHive: HyperHive,
    private val taskManager: ITaskManager
): ITaskBasketsRepository {

    private val zmpUtz14V001 = ZmpUtz14V001(hyperHive)

    private val basketList = mutableListOf<Basket>()

    override fun getBasketByIndex(basketIndex: Int): Basket? {
        return basketList.getOrNull(basketIndex)
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
        }

        basketList.removeAll { it.isEmpty() }
    }

    override fun removeProductFromBasket(basketIndex: Int, product: ProductInfo) {
        basketList[basketIndex].remove(product)

        basketList.removeAll { it.isEmpty() }
    }

    override suspend fun addProduct(product: ProductInfo, supplier: Supplier?, count: Int) {
        if (count == 0) return

        val suitableBasket = getSuitableBasketOrCreate(product, supplier)

        suitableBasket[product] = (suitableBasket[product] ?: 0) + 1

        if (basketList.contains(suitableBasket)) {
            basketList[suitableBasket.index] = suitableBasket
        } else {
            basketList.add(suitableBasket.index, suitableBasket)
        }

        addProduct(product, supplier, count - 1)
    }

    override suspend fun getSuitableBasketOrCreate(product: ProductInfo, supplier: Supplier?): Basket {
        val signOfDiv = taskManager.getTaskSettings().signsOfDiv

        return withContext(Dispatchers.IO) {
            basketList.lastOrNull { basket ->
                basket.checkSuitableProduct(product, supplier)
            } ?: Basket(
                    index = basketList.size,
                    volume = zmpUtz14V001.getEoVolume() ?: error(NULL_BASKET_VOLUME),
                    supplier = supplier.takeIf { signOfDiv.contains(GoodsSignOfDivision.LIF_NUMBER) },
                    isAlco = product.isAlco.takeIf { signOfDiv.contains(GoodsSignOfDivision.ALCO) },
                    isExciseAlco = product.isExcise.takeIf { signOfDiv.contains(GoodsSignOfDivision.MARK_PARTS) },
                    isNotExciseAlco = product.isNotExcise.takeIf { signOfDiv.contains(GoodsSignOfDivision.PARTS) },
                    isUsual = product.isUsual.takeIf { signOfDiv.contains(GoodsSignOfDivision.USUAL) },
                    isVet = product.isVet.takeIf { signOfDiv.contains(GoodsSignOfDivision.VET) },
                    isFood = product.isFood.takeIf { signOfDiv.contains(GoodsSignOfDivision.FOOD) }
            )
        }
    }

    override fun clear() {
        basketList.clear()
    }

    companion object {
        private const val NULL_BASKET_VOLUME = "Объем корзины отсутствует"
    }

}
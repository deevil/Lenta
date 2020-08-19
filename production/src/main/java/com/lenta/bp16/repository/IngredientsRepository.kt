package com.lenta.bp16.repository

import com.lenta.bp16.model.ingredients.GoodByOrder
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.TechOrderDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.model.ingredients.results.IngredientDataCompleteResult
import com.lenta.bp16.model.ingredients.results.IngredientsDataListResult
import com.lenta.bp16.model.ingredients.results.IngredientsListResult
import com.lenta.bp16.model.ingredients.results.UnblockOrderIngredientsResult
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.getResult
import javax.inject.Inject

class IngredientsRepository @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : IIngredientsRepository {

    private val allIngredients: MutableList<IngredientInfo> = mutableListOf()
    private val goodsByOrder: MutableList<GoodByOrder> = mutableListOf()
    private val ingredientsEanInfo: MutableList<OrderByBarcode> = mutableListOf()
    private val ordersByRemake: MutableList<TechOrderDataInfo> = mutableListOf()

    override suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, List<IngredientInfo>> {
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_RESOURCE_NAME, params, IngredientsListStatus::class.java)
                .getResult()

        return result.flatMap {
            allIngredients.clear()
            allIngredients.addAll(it.ingredientsList.orEmpty())
            Either.Right(allIngredients)
        }
    }

    override suspend fun getIngredientEanInfo(params: GetIngredientsParams): Either<Failure, List<OrderByBarcode>>{
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_RESOURCE_NAME,params, IngredientsListStatus::class.java)
                .getResult()

        return result.flatMap{
            ingredientsEanInfo.clear()
            ingredientsEanInfo.addAll(it.goodsEanList.orEmpty())
            Either.Right(ingredientsEanInfo)
        }
    }

    override suspend fun getGoodsByOrder(params: GetIngredientsParams): Either<Failure, List<GoodByOrder>>{
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_RESOURCE_NAME,params, IngredientsListStatus::class.java)
                .getResult()

        return result.flatMap{
            goodsByOrder.clear()
            goodsByOrder.addAll(it.goodsListByOrder.orEmpty())
            Either.Right(goodsByOrder)
        }
    }

    override suspend fun getIngredientListData(params: GetIngredientDataParams): Either<Failure, IngredientsDataListResult> {
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_DATA_RESOURCE_NAME, params, IngredientsDataListStatus::class.java)
                .getResult()
        return result.flatMap {
            addOrdersByRemake(it)
            Either.Right(it)
        }
    }

    override suspend fun unblockOrderIngredients(params: UnblockIngredientsParams): Either<Failure, Boolean> {
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_UNBLOCK_RESOURCE_NAME, params, UnlockOrderIngredientsDataStatus::class.java)
                .getResult()

        return result.flatMap {
            Either.Right(true)
        }
    }

    override suspend fun completeToPackIngredientData(params: IngredientDataCompleteParams): Either<Failure, Boolean> {
        val result = fmpRequestsHelper.restRequest(FMP_INGREDIENT_COMPLETE, params, IngredientDataCompleteStatus::class.java)
                .getResult()
        // TODO: Send to print pack label
        return result.flatMap {
            Either.Right(true)
        }
    }

    override suspend fun completeToPackMaterialData(params: IngredientDataCompleteParams): Either<Failure, Boolean> {
        val result = fmpRequestsHelper.restRequest(FMP_MATERIAL_COMPLETE, params, IngredientDataCompleteStatus::class.java)
                .getResult()

        return result.flatMap {
            Either.Right(true)
        }
    }

    override suspend fun getTechOrdersByRemake(): List<TechOrderDataInfo> {
        return ordersByRemake
    }

    private fun addOrdersByRemake(ingredientsResult: IngredientsDataListResult) {
        ingredientsResult.techOrdersDataInfoList?.let { list ->
            if (list.isNotEmpty()) {
                ordersByRemake.clear()
                ordersByRemake.addAll(list)
            }
        }
    }

    companion object {
        // получение компонентов по заказу и по материалу
        private const val FMP_ORDERS_RESOURCE_NAME = "ZMP_UTZ_PRO_10_V001"

        // получение ингредиентов по заказу или по материалу
        private const val FMP_ORDERS_DATA_RESOURCE_NAME = "ZMP_UTZ_PRO_11_V001"

        // Разблокировка объекта
        private const val FMP_ORDERS_UNBLOCK_RESOURCE_NAME = "ZMP_UTZ_PRO_06_V001"

        // Создание тары для заказа
        private const val FMP_INGREDIENT_COMPLETE = "ZMP_UTZ_PRO_04_V001"

        // сохранение результатов переделки материала
        private const val FMP_MATERIAL_COMPLETE = "ZMP_UTZ_PRO_12_V001"
    }

    internal class IngredientsListStatus : ObjectRawStatus<IngredientsListResult>()
    internal class IngredientsDataListStatus : ObjectRawStatus<IngredientsDataListResult>()
    internal class UnlockOrderIngredientsDataStatus : ObjectRawStatus<UnblockOrderIngredientsResult>()
    internal class IngredientDataCompleteStatus : ObjectRawStatus<IngredientDataCompleteResult>()
}

interface IIngredientsRepository {
    /**
     * Получение всех ингредиентов по заказу и материалу
     *
     * @param params - [GetIngredientsParams]
     */
    suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, List<IngredientInfo>>

    /**
     * Получение данных о товаре по ШК
     *
     * @param params - [GetIngredientsParams]
     * */
    suspend fun getIngredientEanInfo(params: GetIngredientsParams): Either<Failure, List<OrderByBarcode>>

    /**
     * Получение списка заказов с товарами
     *
     * @param params - [GetIngredientsParams]
     * */

    suspend fun getGoodsByOrder(params: GetIngredientsParams): Either<Failure, List<GoodByOrder>>

    /**
     * Получение ингредиентов из заказа
     *
     * @param params - [GetIngredientDataParams]
     */
    suspend fun getIngredientListData(params: GetIngredientDataParams): Either<Failure, IngredientsDataListResult>

    /**
     * Разбокировка ингредиента после комплектации
     *
     * @param params - [UnblockIngredientsParams]
     */
    suspend fun unblockOrderIngredients(params: UnblockIngredientsParams): Either<Failure, Boolean>

    /**
     * Сохранение данных для ингредиентов товара
     *
     * @param params - [IngredientDataCompleteParams]
     */
    suspend fun completeToPackIngredientData(params: IngredientDataCompleteParams): Either<Failure, Boolean>

    /**
     * Сохранение данных для материала ингредиента
     *
     * @param params - [IngredientDataCompleteParams]
     */
    suspend fun completeToPackMaterialData(params: IngredientDataCompleteParams): Either<Failure, Boolean>

    /**
     * Получаем список переделов
     */
    suspend fun getTechOrdersByRemake(): List<TechOrderDataInfo>
}
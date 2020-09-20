package com.lenta.bp16.repository

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ingredients.TechOrderDataInfo
import com.lenta.bp16.model.ingredients.params.*
import com.lenta.bp16.model.ingredients.results.*
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

    private val ordersByRemake: MutableList<TechOrderDataInfo> = mutableListOf()
    private val producersByEan: MutableList<ProducerDataInfo> = mutableListOf()

    override suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, IngredientsListResult> {

        return fmpRequestsHelper.restRequest(FMP_ORDERS_RESOURCE_NAME, params, IngredientsListStatus::class.java)
                .getResult()
    }

    override suspend fun getIngredientListData(params: GetIngredientDataParams): Either<Failure, IngredientsDataListResult> {
        return fmpRequestsHelper.restRequest(FMP_ORDERS_DATA_RESOURCE_NAME, params, IngredientsDataListStatus::class.java)
                .getResult()
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

    override suspend fun completeToPackMaterialData(params: MaterialDataCompleteParams): Either<Failure, Boolean> {
        val result = fmpRequestsHelper.restRequest(FMP_MATERIAL_COMPLETE, params, MaterialDataCompleteStatus::class.java)
                .getResult()

        return result.flatMap {
            Either.Right(true)
        }
    }

    override suspend fun getTechOrdersByRemake(): List<TechOrderDataInfo> {
        return ordersByRemake
    }

    override suspend fun getProducerByEan(): List<ProducerDataInfo> {
        return producersByEan
    }

    override suspend fun getMercuryDataInfo(params: MercuryDataInfoParams): Either<Failure, MercuryDataInfoResult> {
        return fmpRequestsHelper.restRequest(FMP_MERCURY_RESOURCE_NAME, params, MercuryDataInfoStatus::class.java)
    }

    companion object {
        // Получение компонентов по заказу и по материалу
        private const val FMP_ORDERS_RESOURCE_NAME = "ZMP_UTZ_PRO_10_V001"

        // Получение ингредиентов по заказу или по материалу
        private const val FMP_ORDERS_DATA_RESOURCE_NAME = "ZMP_UTZ_PRO_11_V001"

        // Разблокировка объекта
        private const val FMP_ORDERS_UNBLOCK_RESOURCE_NAME = "ZMP_UTZ_PRO_06_V001"

        // Создание тары для заказа
        private const val FMP_INGREDIENT_COMPLETE = "ZMP_UTZ_PRO_04_V001"

        // Сохранение результатов переделки материала
        private const val FMP_MATERIAL_COMPLETE = "ZMP_UTZ_PRO_12_V001"

        // Определение данных меркурианской партии по номеру ЕО
        private const val FMP_MERCURY_RESOURCE_NAME = "ZMP_UTZ_PRO_14_V001"
    }

    internal class IngredientsListStatus : ObjectRawStatus<IngredientsListResult>()
    internal class IngredientsDataListStatus : ObjectRawStatus<IngredientsDataListResult>()
    internal class UnlockOrderIngredientsDataStatus : ObjectRawStatus<UnblockOrderIngredientsResult>()
    internal class IngredientDataCompleteStatus : ObjectRawStatus<IngredientDataCompleteResult>()
    internal class MaterialDataCompleteStatus : ObjectRawStatus<MaterialDataCompleteResult>()
    internal class MercuryDataInfoStatus : ObjectRawStatus<MercuryDataInfoResult>()
}

interface IIngredientsRepository {
    /**
     * Получение всех ингредиентов по заказу и материалу
     *
     * @param params - [GetIngredientsParams]
     */
    suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, IngredientsListResult>

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
     * @param params - [MaterialDataCompleteParams]
     */
    suspend fun completeToPackMaterialData(params: MaterialDataCompleteParams): Either<Failure, Boolean>

    /**
     * Получаем список переделов
     */
    suspend fun getTechOrdersByRemake(): List<TechOrderDataInfo>

    /**
     * Получение списка производителей
     **/
    suspend fun getProducerByEan(): List<ProducerDataInfo>

    /**
     * Определение данных по меркурианской партии по номеру ЕО
     *
     * @param params - [MercuryDataInfoParams]
     * */
    suspend fun getMercuryDataInfo(params: MercuryDataInfoParams): Either<Failure, MercuryDataInfoResult>
}
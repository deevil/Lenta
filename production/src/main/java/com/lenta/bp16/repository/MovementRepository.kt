package com.lenta.bp16.repository

import com.lenta.bp16.model.movement.params.ProductInfoParams
import com.lenta.bp16.model.movement.params.WarehouseParams
import com.lenta.bp16.model.movement.result.ProductInfoResult
import com.lenta.bp16.model.movement.result.WarehouseResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class MovementRepository @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper,
        private val sessionInfo: ISessionInfo
) : IMovementRepository {

    override suspend fun getProductInfoList(params: ProductInfoParams): Either<Failure, ProductInfoResult> {
        return fmpRequestsHelper.restRequest(FMP_GOODS_INFO, params, ProductInfoStatus::class.java)
    }

    override suspend fun getWarehouseList(params: WarehouseParams): Either<Failure, WarehouseResult> {
        return fmpRequestsHelper.restRequest(FMP_WAREHOUSE_INFO, params, WarehouseListStatus::class.java)
    }

    companion object {
        private const val FMP_GOODS_INFO = "ZMP_UTZ_45_V001"
        private const val FMP_WAREHOUSE_INFO = "ZMP_UTZ_02_V001"
    }

    internal class ProductInfoStatus : ObjectRawStatus<ProductInfoResult>()
    internal class WarehouseListStatus : ObjectRawStatus<WarehouseResult>()

}

interface IMovementRepository {
    /**
     * Получение информации по товару
     *
     * @param params - [ProductInfoParams]
     * */
    suspend fun getProductInfoList(params: ProductInfoParams): Either<Failure, ProductInfoResult>

    /**
     * Получение списка складов
     *
     * @param params - [WarehouseParams]
     * */
    suspend fun getWarehouseList(params: WarehouseParams): Either<Failure, WarehouseResult>
}
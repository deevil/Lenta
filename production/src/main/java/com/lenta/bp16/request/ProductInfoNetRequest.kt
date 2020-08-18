package com.lenta.bp16.request

import com.lenta.bp16.model.movement.params.ProductInfoParams
import com.lenta.bp16.model.movement.result.ProductInfoResult
import com.lenta.bp16.repository.IMovementRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class ProductInfoNetRequest @Inject constructor(
        private val movementRepository: IMovementRepository
) : UseCase<List<ProductInfoResult>, ProductInfoParams> {
    override suspend fun run(params: ProductInfoParams): Either<Failure, List<ProductInfoResult>> {
        return movementRepository.getProductInfoList(params)
    }
}
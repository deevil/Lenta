package com.lenta.bp16.request

import com.lenta.bp16.model.movement.result.WarehouseResult
import com.lenta.bp16.repository.IMovementRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class WarehouseNetRequest @Inject constructor(
        private val movementRepository: IMovementRepository
) : UseCase<WarehouseResult, Nothing?> {
    override suspend fun run(params: Nothing?): Either<Failure, WarehouseResult> {
        return movementRepository.getWarehouseList(params)
    }
}
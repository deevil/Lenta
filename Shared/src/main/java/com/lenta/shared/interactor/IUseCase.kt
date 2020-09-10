package com.lenta.shared.interactor

interface IUseCase {

    interface SingleInOut<T, R> {
        suspend operator fun invoke(params: T): R
    }
}
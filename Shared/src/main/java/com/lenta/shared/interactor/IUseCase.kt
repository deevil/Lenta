package com.lenta.shared.interactor

interface IUseCase {

    interface SingleInOut<T, R> {
        suspend operator fun invoke(params: T): R
    }

    interface Out<R> {
        suspend operator fun invoke(): R
    }

    interface In<T> {
        suspend operator fun invoke(params: List<T>)
    }
}
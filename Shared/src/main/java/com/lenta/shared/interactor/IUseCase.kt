package com.lenta.shared.interactor

interface IUseCase {

    interface Out<R>{
        suspend operator fun invoke(): R
    }

    interface In<T>{
        suspend operator fun invoke(params: List<T>)
    }
}
package com.lenta.shared.di

import android.content.Context

object CoreInjectHelper {

    private val componentsMap: MutableMap<Class<*>, Any?> = mutableMapOf()

    fun <T> createComponent(clazz: Class<T>, initFunc: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        (componentsMap[clazz] as T).let { component ->
            return component ?: initFunc().apply {
                componentsMap[clazz] = this
            }
        }
    }

    fun <T> getComponent(clazz: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentsMap[clazz] as T
    }

    fun removeComponent(clazz: Class<*>) {
        componentsMap.remove(clazz)
    }

    fun provideCoreComponent(applicationContext: Context): CoreComponent{
        return if (applicationContext is CoreComponentProvider) {
            (applicationContext as CoreComponentProvider).provideCoreComponent()
        } else {
            throw IllegalStateException("The application context you have passed does not implement CoreComponentProvider")
        }
    }
}
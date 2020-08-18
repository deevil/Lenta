package com.lenta.bp16.model.warehouse

interface IWarehousePersistStorage {
    fun saveSelectedWarehouses(list: Set<String>)
    fun getSelectedWarehouses(): Set<String>
}
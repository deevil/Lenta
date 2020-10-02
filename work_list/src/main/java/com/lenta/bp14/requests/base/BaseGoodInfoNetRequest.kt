package com.lenta.bp14.requests.base

import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.requests.ProductInfoResult
import com.lenta.bp14.requests.pojo.ZPartDTO

abstract class BaseGoodInfoNetRequest {

    protected fun List<ZPartDTO>.mapToZPartList(): List<ZPart> = this.map { it.toZPart() }

    protected fun ProductInfoResult.extractStocks(zParts: List<ZPart>) = stocks.map { stock ->
        val zPartQuantity = zParts.filter { it.stock == stock.lgort }.quantitySum()
        Stock(
                storage = stock.lgort,
                quantity = stock.stock,
                zPartsQuantity = zPartQuantity,
                hasZPart = zParts.isNotEmpty()
        )
    }

    private fun List<ZPart>.quantitySum(): Double {
        var sum = 0.0
        for (zPart in this) {
            sum += zPart.quantity
        }
        return sum
    }
}
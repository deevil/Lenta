package com.lenta.bp14.models.not_exposed_products.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.extentions.map

class NotExposedProductsRepo : INotExposedProductsRepo {

    private val products = mutableListOf<INotExposedProductInfo>()

    private val resultsLiveData = MutableLiveData(products.toList())

    override fun getProduct(matNr: String?, ean: String?): INotExposedProductInfo? {
        val res = mutableListOf<INotExposedProductInfo>()
        matNr?.apply {
            res.addAll(products.filter { it.matNr == this })
        }
        ean?.apply {
            res.addAll(products.filter { it.ean == this })
        }
        return res.getOrNull(0)
    }

    override fun getProducts(): LiveData<List<INotExposedProductInfo>> {
        return resultsLiveData
    }

    override fun addOrReplaceProduct(product: INotExposedProductInfo): Boolean {
        products.removeAll { it.matNr == product.matNr }
        products.add(product)
        resultsLiveData.value = products
        return true
    }

    override fun removeProducts(matNumbers: Set<String>) {
        products.removeAll { matNumbers.contains(it.matNr) }
        resultsLiveData.value = products

    }

}


interface INotExposedProductsRepo {

    fun getProduct(matNr: String? = null, ean: String? = null): INotExposedProductInfo?

    fun getProducts(): LiveData<List<INotExposedProductInfo>>

    fun addOrReplaceProduct(product: INotExposedProductInfo): Boolean

    fun removeProducts(matNumbers: Set<String>)

}

data class NotExposedProductInfo(
        override val ean: String?,
        override val matNr: String,
        override val name: String,
        override val quantity: Double?,
        override val uom: String,
        override val isEmptyPlaceMarked: Boolean?) : INotExposedProductInfo


interface INotExposedProductInfo {
    val ean: String?
    val matNr: String
    val name: String
    val quantity: Double?
    val uom: String
    val isEmptyPlaceMarked: Boolean?
}
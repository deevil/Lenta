package com.lenta.bp14.models.not_exposed.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.models.core.Uom
import javax.inject.Inject

class NotExposedRepo @Inject constructor() : INotExposedRepo {

    private val products = mutableListOf<NotExposedProductInfo>()

    private val resultsLiveData = MutableLiveData(products.toList())

    override fun getProduct(matNr: String?, ean: String?): NotExposedProductInfo? {
        val res = mutableListOf<NotExposedProductInfo>()
        matNr?.apply {
            res.addAll(products.filter { it.matNr == this })
        }
        ean?.apply {
            res.addAll(products.filter { it.ean == this })
        }
        return res.getOrNull(0)
    }

    override fun getProducts(): LiveData<List<NotExposedProductInfo>> {
        return resultsLiveData
    }

    override fun addOrReplaceProduct(product: NotExposedProductInfo): Boolean {
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


interface INotExposedRepo {

    fun getProduct(matNr: String? = null, ean: String? = null): NotExposedProductInfo?

    fun getProducts(): LiveData<List<NotExposedProductInfo>>

    fun addOrReplaceProduct(product: NotExposedProductInfo): Boolean

    fun removeProducts(matNumbers: Set<String>)

}

data class NotExposedProductInfo(
        val ean: String?,
        val matNr: String,
        val name: String,
        val quantity: Double?,
        val uom: Uom?,
        val isEmptyPlaceMarked: Boolean?,
        val section: String?,
        val group: String?
)



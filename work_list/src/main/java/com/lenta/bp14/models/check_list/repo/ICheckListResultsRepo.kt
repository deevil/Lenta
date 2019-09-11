package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_price.ICheckPriceResult

interface ICheckPriceResultsRepo {

    fun getCheckPriceResult(matNr: String? = null, ean: String? = null): ICheckPriceResult?

    fun getCheckPriceResults(): LiveData<List<ICheckPriceResult>>

    fun addCheckPriceResult(checkPriceResult: ICheckPriceResult)

    fun removePriceCheckResult(matNr: String)

}

class CheckPriceResultsRepo : ICheckPriceResultsRepo {

    private val checkPriceResults = mutableListOf<ICheckListResult>()

    private val resultsLiveData = MutableLiveData<List<ICheckPriceResult>>(checkPriceResults)


    override fun getCheckPriceResult(matNr: String?, ean: String?): ICheckPriceResult? {
        val res = mutableListOf<ICheckPriceResult>()
        matNr?.apply {
            res.addAll(checkPriceResults.filter { it.matNr == this })
        }
        ean?.apply {
            res.addAll(checkPriceResults.filter { it.ean == this })
        }
        return res.getOrNull(0)
    }

    override fun getCheckPriceResults(): LiveData<List<ICheckPriceResult>> {
        return resultsLiveData
    }

    override fun addCheckPriceResult(checkPriceResult: ICheckPriceResult) {
        checkPriceResults.add(checkPriceResult)
        resultsLiveData.value = checkPriceResults
    }

    override fun removePriceCheckResult(matNr: String) {
        checkPriceResults.indexOfFirst { it.matNr == matNr }.let {
            if (it >= 0) {
                checkPriceResults.removeAt(it)
            }
        }
        resultsLiveData.value = checkPriceResults
    }

}
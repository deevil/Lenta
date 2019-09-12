package com.lenta.bp14.models.check_price.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_price.ICheckPriceResult

interface ICheckPriceResultsRepo {

    fun getCheckPriceResult(matNr: String? = null, ean: String? = null): ICheckPriceResult?

    fun getCheckPriceResults(): LiveData<List<ICheckPriceResult>>

    fun addCheckPriceResult(checkPriceResult: ICheckPriceResult)

    fun removePriceCheckResults(matNumbers: Set<String>)

}

class CheckPriceResultsRepo : ICheckPriceResultsRepo {

    private val checkPriceResults = mutableListOf<ICheckPriceResult>()

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
        if (checkPriceResults.contains(checkPriceResult)) {
            return
        }
        checkPriceResults.removeAll { it.matNr == checkPriceResult.matNr }
        checkPriceResults.add(checkPriceResult)
        resultsLiveData.value = checkPriceResults
    }


    override fun removePriceCheckResults(matNumbers: Set<String>) {
        checkPriceResults.removeAll { matNumbers.contains(it.matNr) }
        resultsLiveData.value = checkPriceResults
    }

}
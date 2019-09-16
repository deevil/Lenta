package com.lenta.bp14.models.check_price.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_price.ICheckPriceResult
import com.lenta.shared.utilities.Logg

interface ICheckPriceResultsRepo {

    fun getCheckPriceResult(matNr: String? = null, ean: String? = null): ICheckPriceResult?

    fun getCheckPriceResults(): LiveData<List<ICheckPriceResult>>

    fun addCheckPriceResult(checkPriceResult: ICheckPriceResult): Boolean

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

    override fun addCheckPriceResult(checkPriceResult: ICheckPriceResult): Boolean {
        checkPriceResults.indexOfFirst {
            Logg.d { "compare: ${it} / ${checkPriceResult}, res = ${it == checkPriceResult}" }
            it == checkPriceResult
        }.let { index ->
            if (index > -1) {
                return false
            }
        }
        checkPriceResults.removeAll { it.matNr == checkPriceResult.matNr }
        checkPriceResults.add(checkPriceResult)
        resultsLiveData.value = checkPriceResults
        return true
    }


    override fun removePriceCheckResults(matNumbers: Set<String>) {
        checkPriceResults.removeAll { matNumbers.contains(it.matNr) }
        resultsLiveData.value = checkPriceResults
    }

}
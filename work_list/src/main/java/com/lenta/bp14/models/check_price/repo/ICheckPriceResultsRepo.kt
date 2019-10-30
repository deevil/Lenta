package com.lenta.bp14.models.check_price.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_price.CheckPriceResult
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

interface ICheckPriceResultsRepo {

    fun getCheckPriceResult(matNr: String? = null, ean: String? = null): CheckPriceResult?

    fun getCheckPriceResults(): LiveData<List<CheckPriceResult>>

    fun addCheckPriceResult(checkPriceResult: CheckPriceResult): Boolean

    fun removePriceCheckResults(matNumbers: Set<String>)

}

class CheckPriceResultsRepo @Inject constructor() : ICheckPriceResultsRepo {

    private val checkPriceResults = mutableListOf<CheckPriceResult>()

    private val resultsLiveData = MutableLiveData<List<CheckPriceResult>>(checkPriceResults)


    override fun getCheckPriceResult(matNr: String?, ean: String?): CheckPriceResult? {
        val res = mutableListOf<CheckPriceResult>()
        matNr?.apply {
            res.addAll(checkPriceResults.filter { it.matNr == this })
        }
        ean?.apply {
            res.addAll(checkPriceResults.filter { it.ean == this })
        }
        return res.getOrNull(0)
    }

    override fun getCheckPriceResults(): LiveData<List<CheckPriceResult>> {
        return resultsLiveData
    }

    override fun addCheckPriceResult(checkPriceResult: CheckPriceResult): Boolean {
        checkPriceResults.indexOfFirst {
            Logg.d { "compare: $it / $checkPriceResult, res = ${it == checkPriceResult}" }
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
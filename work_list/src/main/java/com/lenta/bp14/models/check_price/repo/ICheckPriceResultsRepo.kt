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
    private var checkPriceResultsMatNrWithPosMap = mutableMapOf<String?, Int>()

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
        checkPriceResultsMatNrWithPosMap[checkPriceResult.matNr].let { lastPosition ->
            if (lastPosition != null) {
                checkPriceResults.removeAt(lastPosition)
            }
            checkPriceResults.add(checkPriceResult)
            checkPriceResultsMatNrWithPosMap[checkPriceResult.matNr
                    ?: ""] = checkPriceResults.size - 1

        }
        resultsLiveData.value = checkPriceResults
    }


    override fun removePriceCheckResults(matNumbers: Set<String>) {
        checkPriceResults.removeAll { matNumbers.contains(it.matNr) }
        checkPriceResultsMatNrWithPosMap.clear()
        checkPriceResultsMatNrWithPosMap.putAll(checkPriceResults.mapIndexed { index, iCheckPriceResult -> iCheckPriceResult.matNr to index }.toMap())
        resultsLiveData.value = checkPriceResults
    }

}
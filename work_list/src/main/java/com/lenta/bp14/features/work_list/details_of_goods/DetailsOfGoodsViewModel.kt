package com.lenta.bp14.features.work_list.details_of_goods

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import java.util.Collections.list

class DetailsOfGoodsViewModel : CoreViewModel(), PageSelectionListener {

    val enabledDeleteButton: MutableLiveData<Boolean> = MutableLiveData(false)
    val selectedPage = MutableLiveData(0)

    val expirationDates: MutableLiveData<List<ExpirationDateUi>> = MutableLiveData()
    val comments: MutableLiveData<List<CommentDateUi>> = MutableLiveData()

    init {
        expirationDates.value = getTestExpirationDates()
        comments.value = getTestComments()
    }

    private fun getTestComments(): List<CommentDateUi>? {
        return List(100) {
            CommentDateUi(
                    it + 1,
                    "С товаром все хорошо ${it + 1}",
                    "13 шт"
            )
        }
    }

    private fun getTestExpirationDates(): List<ExpirationDateUi>? {
        return List(100) {
            ExpirationDateUi(
                    it + 1,
                    "СГ - 09.12.19",
                    if (it % 2 == 0) "" else "ДП - 06.12.19",
                    "12 шт"
            )
        }
    }


    fun getTitle(): String? {
        return "???"
    }

    fun onClickDelete() {

    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }
}

data class ExpirationDateUi(
        val number: Int,
        val expirationDate: String,
        val productionDate: String,
        val quantity: String
)


data class CommentDateUi(
        val number: Int,
        val comment: String,
        val quantity: String
)

package com.lenta.bp15.features.task_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TaskCardViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        "ПНБ(ТК)-303"
    }

    val isExistComment by lazy {
        MutableLiveData(true)
    }

    val taskCard by lazy {
        TaskCardUi(
                type = "Тестовый тип",
                name = "Тестовое имя",
                quantity = "1122",
                description = "Описание задачи",
                comment = "Комментарии к задаче"
        )
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickNext() {

    }

    fun onBackPressed() {

    }


}
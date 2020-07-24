package com.lenta.bp14.features.task_list.search_filter

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITasksSearchHelper
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.tasks.SearchTaskFilter
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class SearchFilterTlViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var tasksSearchHelper: ITasksSearchHelper
    @Inject
    lateinit var generalRepo: IGeneralRepo


    val marketNumber by lazy { sessionInfo.market }

    val taskTypeList: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>().also { liveData ->
            launchUITryCatch {
                liveData.value = generalRepo.getTasksTypes().map { it.taskName }
            }
        }
    }

    val selectedPosition by lazy {
        MutableLiveData(0).also { liveData ->
            launchUITryCatch {
                generalRepo.getTasksTypes().indexOfFirst {
                    it.taskType == tasksSearchHelper.filterParams?.taskType
                }.let {
                    if (it > -1) {
                        liveData.value = it
                    }
                }

            }
        }
    }

    val goodField by lazy {
        MutableLiveData<String>().also { liveData ->
            launchUITryCatch {
                liveData.value = tasksSearchHelper.filterParams?.matNr.orEmpty()
            }
        }
    }
    val sectionField by lazy {
        MutableLiveData<String>().also { liveData ->
            launchUITryCatch {
                liveData.value = tasksSearchHelper.filterParams?.sectionId.orEmpty()
            }
        }
    }
    val goodsGroupField by lazy {
        MutableLiveData<String>().also { liveData ->
            launchUITryCatch {
                liveData.value = tasksSearchHelper.filterParams?.group.orEmpty()
            }
        }
    }
    val publicationDateField by lazy {
        MutableLiveData<String>().also { liveData ->
            launchUITryCatch {
                liveData.value = tasksSearchHelper.filterParams?.dateOfPublic.orEmpty()
            }
        }
    }


    fun onClickFind() {
        launchUITryCatch {
            tasksSearchHelper.filterParams = getFilterParams()

            navigator.showProgressLoadingData(::handleFailure)
            tasksSearchHelper.isNewSearchData = true
            tasksSearchHelper.updateFilteredTaskList().either(
                    {
                        navigator.openAlertScreen(it)
                    }
            ) {
                navigator.goBack()
            }
            navigator.hideProgress()
        }

    }

    private suspend fun getFilterParams(): SearchTaskFilter? {
        return SearchTaskFilter(
                taskType = getSelectedTaskType(),
                matNr = goodField.value.orEmpty(),
                sectionId = sectionField.value.orEmpty(),
                group = goodsGroupField.value.orEmpty(),
                dateOfPublic = publicationDateField.value.orEmpty()
        )
    }

    private suspend fun getSelectedTaskType(): String {
        return generalRepo.getTasksTypes().getOrNull(selectedPosition.value ?: 0)?.taskType.orEmpty()
    }


    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }




}
